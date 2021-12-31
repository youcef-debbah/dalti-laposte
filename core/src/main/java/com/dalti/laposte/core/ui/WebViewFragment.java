package com.dalti.laposte.core.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dalti.laposte.R;
import com.dalti.laposte.core.model.WebPageModel;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.entity.WebPage;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.frontend.BasicRefreshBehaviour;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class WebViewFragment extends AbstractQueueFragment {

    private WebPageModel model;
    @Keep
    private BasicRefreshBehaviour refreshBehaviour;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        model = getViewModel(WebPageModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, R.layout.fragment_web_page, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRefresh(view);
        setupWebView(view);
    }

    private void setupRefresh(View view) {
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        if (refreshLayout != null) {
            QueueUtils.style(refreshLayout);
            refreshBehaviour = new WebPageRefreshBehaviour(getViewLifecycle(), refreshLayout, model);
            model.addModelObserver(refreshBehaviour);
            refreshLayout.setOnRefreshListener(refreshBehaviour);
        }
    }

    public void setupWebView(View view) {
        WebView webView = view.findViewById(R.id.web_view);
        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setDisplayZoomControls(false);
            webSettings.setAllowFileAccess(false);
            webSettings.setAllowContentAccess(false);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Teller.info("WebView console: " + consoleMessage.message() + " -- line " +
                            consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                    return true;
                }
            });

            model.getPageData().observe(getViewLifecycleOwner(), updateWebView(webView));
        }
    }

    public static Observer<WebPage> updateWebView(WebView webView) {
        return webPage -> {
            if (webPage != null)
                webView.loadData(webPage.getData(), webPage.getMimeType(), webPage.getEncoding());
        };
    }


    private static final class WebPageRefreshBehaviour extends BasicRefreshBehaviour {

        public WebPageRefreshBehaviour(@NotNull Lifecycle lifecycle,
                                       @NotNull SwipeRefreshLayout layout,
                                       @NotNull WebPageModel model) {
            super(lifecycle, layout, model);
        }

        @Override
        public void onRefresh() {
            if (model instanceof WebPageModel)
                ((WebPageModel) model).refreshCurrentPage();
        }
    }
}
