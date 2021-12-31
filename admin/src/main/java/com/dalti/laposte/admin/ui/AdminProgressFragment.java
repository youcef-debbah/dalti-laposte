package com.dalti.laposte.admin.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.AdminDashboardRepository;
import com.dalti.laposte.core.model.DashboardModel;
import com.dalti.laposte.core.repositories.AdminAction;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.Selection;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.ui.ProgressFragment;
import com.dalti.laposte.core.util.QueueConfig;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import dz.jsoftware95.silverbox.android.observers.DuoMainObserver;

@AndroidEntryPoint
public class AdminProgressFragment extends ProgressFragment {

    protected DashboardModel model;
    protected AdminDashboardRepository repository;

    @Inject
    public void init(AdminDashboardRepository adminDashboardRepository) {
        repository = adminDashboardRepository;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        model = getViewModel(DashboardModel.class);
    }

    @Override
    protected DashboardModel getModel() {
        return model;
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_admin_progress;
    }

    @Override
    public int getRankContainerID() {
        return R.id.progress_rank_container;
    }

    @Override
    protected void onCreateView(ViewDataBinding binding, Bundle savedInstanceState) {
        binding.setVariable(BR.fragment, this);
    }

    @Override
    protected void addProgressView(MutableLiveData<Progress> progress, ViewGroup container) {
//        super.addProgressView(progress, container);
        ViewDataBinding rankView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.section_admin_progress, container, true);
        rankView.setLifecycleOwner(getCurrentViewLifecycle());
        progress.observe(getCurrentViewLifecycle(), new ProgressUpdater(getCurrentViewLifecycle(), rankView, this));
    }

    public void showServiceStateForm(int rank) {
        Selection selection = getSelectionValue();
        Progress progress;
        if (Selection.hasService(selection) && (progress = selection.getProgressValue(rank)) != null) {
            Intent intent = new Intent(requireContext(), ServiceStateFormActivity.class);
            intent.putExtra(ServiceStateFormActivity.SERVICE_DESCRIPTION_KEY, selection.getService().getDescription());
            intent.putExtra(ServiceStateFormActivity.PROGRESS_ID_KEY, progress.getId());
            intent.putExtra(ServiceStateFormActivity.PROGRESS_ICON_KEY, progress.getIcon());
            intent.putExtra(ServiceStateFormActivity.CURRENT_TOKEN_KEY, progress.getCurrentTokenInt());
            intent.putExtra(ServiceStateFormActivity.WAITING_KEY, progress.getWaitingInt());
            intent.putExtra(ServiceStateFormActivity.AVAILABILITY_KEY, PostOfficeAvailability.ensureActiveInstance(selection.getService().getAvailability()).getCode());
            startActivity(intent);
        } else
            QueueUtils.handleServiceMissing();
    }

    public void showAdminAlarmForm(int rank) {
        Selection selection = getSelectionValue();
        Progress progress;
        if (Selection.hasService(selection) && (progress = selection.getProgressValue(rank)) != null) {
            Intent intent = new Intent(requireContext(), AdminAlarmFormActivity.class);
            intent.putExtra(Progress.ID, progress.getId());
            startActivity(intent);
        } else
            QueueUtils.handleServiceMissing();
    }

    public void reSend(int rank) {
        doProgressAction(rank, StaticAdminAction.NONE);
    }

    public void onIncCurrentClick(View button, int rank) {
        SystemWorker.MAIN.executeDelayed(() -> button.setEnabled(true), QueueConfig.INC_TOKEN_DELAY);
        button.setEnabled(false);
        incCurrent(rank);
    }

    public void incCurrent(int rank) {
        doProgressAction(rank, StaticAdminAction.INC_CURRENT);
    }

    public void incWaiting(int rank) {
        doProgressAction(rank, StaticAdminAction.INC_WAITING);
    }

    private void doProgressAction(int rank, AdminAction action) {
        Selection selection = getSelectionValue();
        if (Selection.hasService(selection))
            repository.setToken(action, IdentityManager.getProgressID(selection.getService().getId(), rank));
        else
            QueueUtils.handleServiceMissing();
    }

    public void resetProgress(int rank) {
        Selection selection = getSelectionValue();
        if (Selection.hasService(selection))
            repository.resetToken(IdentityManager.getProgressID(selection.getService().getId(), rank));
        else
            QueueUtils.handleServiceMissing();
    }

    public void showServiceNoteForm() {
        Selection selection = getSelectionValue();
        if (Selection.hasService(selection)) {
            Service service = selection.getService();
            Intent intent = new Intent(requireContext(), ServiceNoteFormActivity.class);
            intent.putExtra(ServiceNoteFormActivity.SERVICE_ID_KEY, service.getId());

            Long closeTime = service.getCurrentCloseTime();
            if (closeTime != null)
                intent.putExtra(ServiceNoteFormActivity.CLOSE_TIME_KEY, closeTime);

            Long defaultCloseTime = selection.getLastCloseEvent();
            if (defaultCloseTime != null)
                intent.putExtra(ServiceNoteFormActivity.DEFAULT_CLOSE_TIME_KEY, TimeUtils.formatAsShortTime(defaultCloseTime));

            startActivity(intent);
        } else
            QueueUtils.handleServiceNeeded("open_note_form");
    }

    private static class ProgressUpdater extends DuoMainObserver<ViewDataBinding, AdminProgressFragment, Progress> {

        protected ProgressUpdater(@NonNull LifecycleOwner lifecycle,
                                  @NotNull ViewDataBinding binding,
                                  @NonNull AdminProgressFragment fragment) {
            super(lifecycle, binding, fragment);
        }

        @Override
        protected void onUpdate(@NonNull ViewDataBinding rankView,
                                @NonNull AdminProgressFragment fragment,
                                @Nullable Progress progress) {
            rankView.setVariable(BR.fragment, fragment);
            rankView.setVariable(BR.progress, progress);

            int rank = progress != null ? progress.getRank() : 0;
            View button = rankView.getRoot().findViewById(R.id.progress_setting_button);
            if (button != null)
                button.setOnLongClickListener(v -> {
                    fragment.showAdminAlarmForm(rank);
                    return true;
                });
        }
    }

    public String getNamespace() {
        return "admin_progress_fragment";
    }
}
