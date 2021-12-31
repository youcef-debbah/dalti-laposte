package com.dalti.laposte.core.repositories;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.R;
import com.dalti.laposte.core.entity.CoreAPI;
import com.dalti.laposte.core.entity.WebPage;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.WebPageDetails;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.api.WebPageInfo;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.backend.hasLiveDataCache;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.observers.ObserversUtil;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public class WebPageRepository extends LazyRepository<WebPageDAO>
        implements hasLiveDataCache<String, WebPage> {

    private final ConcurrentMap<String, LiveDataWrapper<WebPage>> cache;
    private final Lazy<CoreAPI> coreAPI;
    private final int bufferSize;
    private final RepositoryUtil repositoryUtil;

    @Inject
    public WebPageRepository(Lazy<WebPageDAO> webPageDAO,
                             Lazy<CoreAPI> coreAPI,
                             RepositoryUtil repositoryUtil) {
        super(webPageDAO);
        this.coreAPI = coreAPI;
        this.bufferSize = AppConfig.getInstance().getRemoteInt(LongSetting.DEFAULT_BUFFER_SIZE);
        this.cache = new ConcurrentHashMap<>(4);
        this.repositoryUtil = repositoryUtil;
    }

    @Override
    public ConcurrentMap<String, LiveDataWrapper<WebPage>> getCache() {
        return cache;
    }

    @Override
    public void initCacheEntry(String key, LiveDataWrapper<WebPage> output) {
        execute(newPostValueJob(this, output, key));
    }

    private static Job newPostValueJob(WebPageRepository repository, LiveDataWrapper<WebPage> output, String pageName) {
        return new DuoDatabaseJob<LiveDataWrapper<WebPage>, WebPageRepository>(output, repository) {
            @Override
            protected void doFromBackground(@NonNull LiveDataWrapper<WebPage> output, @NonNull WebPageRepository repository) {
                final LiveData<WebPage> source = repository.requireDAO().load(pageName);
                newPostWebPageJob(output, source, pageName, repository.bufferSize).execute();
            }
        };
    }

    public static Job newPostWebPageJob(final LiveDataWrapper<WebPage> output,
                                        final LiveData<WebPage> source,
                                        @NotNull final String pageName,
                                        int bufferSize) {
        return new UnJob<LiveDataWrapper<WebPage>>(output) {
            @Override
            protected void doFromMain(@NonNull LiveDataWrapper<WebPage> output) {
                output.setSource(source, ObserversUtil.newBackgroundMapper(output.getLiveData(), (WebPage webPage) ->
                        webPage != null && webPage.hasContent() ? webPage : new WebPage(pageName, getEncodedContent())
                ));
            }

            private String getEncodedContent() {
                return Base64.encodeToString(getContent(), Base64.NO_PADDING);
            }

            private byte[] getContent() {
                final String fileName = WebPageDetails.getPageFileName(pageName);
                Context context = AbstractQueueApplication.requireInstance();
                try (InputStream input = context.getAssets().open(fileName)) {
                    byte[] buffer = new byte[bufferSize];
                    ByteArrayOutputStream output = new ByteArrayOutputStream(bufferSize);
                    int read;
                    while ((read = input.read(buffer)) > 0)
                        output.write(buffer, 0, read);

                    if (WebPageDetails.DECOMPRESS)
                        return GlobalUtil.decompressData(output.toByteArray());
                    else
                        return output.toByteArray();
                } catch (IOException e) {
                    Teller.warn("reading content failed for: " + fileName, e);
                    return QueueUtils.getSimpleWebPage(context, R.string.web_page_not_found).getBytes();
                }
            }
        };
    }

    @MainThread
    public LiveData<WebPage> loadPageContent(String webPageName) {
        return getFromCache(webPageName);
    }

    public void refreshPage(String pageName) {
        if (pageName != null)
            execute(new RefreshPageJob(this, pageName, false));
        else
            postPublish(DataEvent.DATA_FETCHED);
    }

    private static final class RefreshPageJob extends UnJob<WebPageRepository> {

        public static final String NAME = "refresh_page_job";
        private final String pageName;
        private final boolean feedback;

        public RefreshPageJob(@NotNull WebPageRepository repository,
                              String pageName,
                              boolean feedback) {
            super(AppWorker.NETWORK, repository);
            this.pageName = pageName;
            this.feedback = feedback;
        }

        @Override
        protected void doFromBackground(@NotNull WebPageRepository repository) throws InterruptedException {
            String url = null;
            try {
                Call<WebPageInfo> call = repository.coreAPI.get().fetchWebPage(pageName);
                url = QueueUtils.getUrl(call);
                Response<WebPageInfo> response = call.execute();

                if (response.isSuccessful()) {
                    WebPageInfo downloadedPage = response.body();
                    if (downloadedPage != null) {
                        try {
                            WebPage page = WebPage.from(downloadedPage);
                            if (page != null) {
                                repository.waitForDAO(getClass().getSimpleName()).save(page);
                                Teller.info("page fetched successfully: " + pageName);
                            } else {
                                Teller.logMissingInfo("web page not found: " + pageName, feedback);
                            }
                        } catch (Exception e) {
                            Teller.warn("failed to save web page: " + pageName, e);
                            QueueUtils.toast(R.string.could_not_persist_data, feedback);
                        }
                    } else
                        Teller.logMissingInfo("null body", feedback);

                    repository.repositoryUtil.handleResponse(downloadedPage, NAME);
                } else
                    repository.repositoryUtil.handleUnsuccessfulResponse(response, "unsuccessful page fetch fetch: " + url, null, feedback, NAME);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "failed to fetch page: " + url, feedback);
            } finally {
                repository.postPublish(DataEvent.DATA_FETCHED);
            }
        }
    }
}
