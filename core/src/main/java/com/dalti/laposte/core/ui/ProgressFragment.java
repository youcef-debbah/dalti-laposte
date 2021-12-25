package com.dalti.laposte.core.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dalti.laposte.BR;
import com.dalti.laposte.R;
import com.dalti.laposte.core.model.DashboardModel;
import com.dalti.laposte.core.repositories.Progress;
import com.dalti.laposte.core.repositories.Selection;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import dz.jsoftware95.silverbox.android.common.ViewLifecycleRegistry;
import dz.jsoftware95.silverbox.android.frontend.BasicRefreshBehaviour;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

public abstract class ProgressFragment extends AbstractQueueFragment {

    private static volatile boolean firstInit = true;

    private ViewGroup progressRankContainer;
    private ViewLifecycleRegistry currentViewLifecycle;

    private Selection selection;

    private AppCompatImageView firstNoteIcon;
    private AppCompatImageView secondNoteIcon;

    private int compactIconSize;
    private int largeIconSize;

    private Trace trace;

    protected abstract DashboardModel getModel();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @CallSuper
    protected void setSelection(Selection selection) {
        this.selection = selection;
    }

    public final Selection getSelectionValue() {
        return selection;
    }

    public final LiveData<Selection> getSelection() {
        return getModel().getSelection();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, getLayout(), container);
        onCreateView(binding, savedInstanceState);
        return binding.getRoot();
    }

    protected abstract void onCreateView(ViewDataBinding binding, Bundle savedInstanceState);

    protected abstract int getLayout();

    public abstract int getRankContainerID();

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRefresh(view);
        progressRankContainer = view.findViewById(getRankContainerID());

        compactIconSize = getResources().getDimensionPixelSize(R.dimen.compact_icon);
        largeIconSize = getResources().getDimensionPixelSize(R.dimen.large_icon);

        firstNoteIcon = view.findViewById(R.id.first_icon);
        secondNoteIcon = view.findViewById(R.id.second_icon);
        updateNoteIcons(null);

        resetCurrentViewLifecycle();

        if (firstInit) {
            firstInit = false;
            trace = FirebasePerformance.getInstance().newTrace("first_selection_load");
            trace.start();
        }

        getModel().getSelection().observe(currentViewLifecycle, this::updateSelection);
    }

    private void updateNoteIcons(Selection selection) {
        boolean hasIcon1 = false;
        boolean hasIcon2 = false;
        if (firstNoteIcon != null) {
            Drawable drawable = selection != null ? selection.getNoteIcon1() : null;
            hasIcon1 = drawable != null;
            firstNoteIcon.setVisibility(hasIcon1 ? View.VISIBLE : View.GONE);
            firstNoteIcon.setImageDrawable(drawable);
        }

        if (secondNoteIcon != null) {
            Drawable drawable = selection != null ? selection.getNoteIcon2() : null;
            hasIcon2 = drawable != null;
            secondNoteIcon.setVisibility(hasIcon2 ? View.VISIBLE : View.GONE);
            secondNoteIcon.setImageDrawable(drawable);
        }

        if (!hasIcon1 && hasIcon2) {
            ContextUtils.setSize(firstNoteIcon, 0);
            ContextUtils.setSize(secondNoteIcon, largeIconSize);
        } else if (hasIcon1 && !hasIcon2) {
            ContextUtils.setSize(firstNoteIcon, largeIconSize);
            ContextUtils.setSize(secondNoteIcon, 0);
        } else {
            ContextUtils.setSize(firstNoteIcon, compactIconSize);
            ContextUtils.setSize(secondNoteIcon, compactIconSize);
        }
    }

    private void setupRefresh(View view) {
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        if (refreshLayout != null) {
            QueueUtils.style(refreshLayout);
            final DashboardModel model = getModel();
            final BasicRefreshBehaviour refreshBehaviour = new BasicRefreshBehaviour(getViewLifecycle(), refreshLayout, model);
            model.addModelObserver(refreshBehaviour);
            refreshLayout.setOnRefreshListener(refreshBehaviour);
        }
    }

    public void resetCurrentViewLifecycle() {
        if (currentViewLifecycle != null)
            currentViewLifecycle.markViewAsDestroyed();
        currentViewLifecycle = new ViewLifecycleRegistry(getViewLifecycleOwner());
    }

    protected LifecycleOwner getCurrentViewLifecycle() {
        return currentViewLifecycle;
    }

    private void updateSelection(Selection selection) {
        if (trace != null) {
            trace.stop();
            trace = null;
        }

        setSelection(selection);
        updateNoteIcons(selection);
        ViewGroup container = this.progressRankContainer;
        if (container != null) {
            container.removeAllViews();
            if (selection != null) {
                for (MutableLiveData<Progress> progress : selection.getProgresses().values())
                    addProgressView(progress, container);
            }
        }
    }

    protected void addProgressView(MutableLiveData<Progress> progress, ViewGroup container) {
        ViewDataBinding rankView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.section_core_progress, container, true);
        progress.observe(currentViewLifecycle, new UnMainObserver<ViewDataBinding, Progress>(currentViewLifecycle, rankView) {
            @Override
            protected void onUpdate(@NonNull ViewDataBinding rankView, @Nullable Progress progress) {
                rankView.setVariable(BR.progress, progress);
            }
        });
    }

    public void openServiceActivity(View view) {
        startActivity(ServicesListActivity.class);
    }

    public boolean isUnknownAvailability() {
        Selection selection = getSelectionValue();
        return Selection.hasService(selection) && selection.getService().getAvailability() == null;
    }

    public Integer getWhenUnknownAvailability() {
        return isUnknownAvailability() ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public CharSequence getNoSelectionText() {
        SpannableString text = new SpannableString(QueueUtils.getString(R.string.no_service_selected));
        text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return text;
    }

    public String getNamespace() {
        return "progress_fragment";
    }
}
