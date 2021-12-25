package com.dalti.laposte.core.model;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.NamedLongPreference;
import com.dalti.laposte.core.repositories.WebPage;
import com.dalti.laposte.core.repositories.WebPageRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dz.jsoftware95.silverbox.android.middleware.RepositoryModel;

@MainThread
@HiltViewModel
public class WebPageModel extends RepositoryModel<WebPageRepository> {

    private final MutableLiveData<String> livePageName;
    private final LiveData<WebPage> livePageData;

    /**
     * Creates a new Refreshable View Model with an empty refresh observers list
     *
     * @param application the application instance to be used by this model
     */
    @Inject
    protected WebPageModel(Application application,
                           WebPageRepository webPageRepository) {
        super(application, webPageRepository);
        livePageName = new MutableLiveData<>();
        livePageData = Transformations.switchMap(livePageName, webPageRepository::loadPageContent);
    }

    public LiveData<WebPage> getPageData() {
        return livePageData;
    }

    public void setPage(String pageName) {
        livePageName.setValue(pageName);
        final AppConfig appConfig = AppConfig.getInstance();
        final long lastFetch = appConfig.getLong(new NamedLongPreference(NamedLongPreference.PREFIX_LAST_FETCH + pageName));
        if (System.currentTimeMillis() - lastFetch > appConfig.getRemoteLong(LongSetting.WEB_PAGE_FETCH_COOLDOWN))
            refreshPage(pageName);
    }

    public void refreshCurrentPage() {
        refreshPage(livePageName.getValue());
    }

    public void refreshPage(String pageName) {
        markAsRefreshing();
        getRepository().refreshPage(pageName);
    }
}
