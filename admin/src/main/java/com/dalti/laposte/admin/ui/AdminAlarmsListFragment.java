package com.dalti.laposte.admin.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.AdminAlarmsListModel;
import com.dalti.laposte.admin.repositories.AdminAlarmsListRepository;
import com.dalti.laposte.core.model.QueueRecyclerView;
import com.dalti.laposte.core.entity.AdminAlarm;
import com.dalti.laposte.core.repositories.LoadedProgress;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.ProgressRepository;
import com.dalti.laposte.core.ui.AbstractQueueFragment;
import com.dalti.laposte.core.util.QueueUtils;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.frontend.BasicRefreshBehaviour;
import dz.jsoftware95.silverbox.android.frontend.StatefulRecyclerView;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.observers.MainObserver;

@AndroidEntryPoint
public class AdminAlarmsListFragment extends AbstractQueueFragment {

    AdminAlarmsListModel model;

    @Inject
    AdminAlarmsListRepository alarmsListRepository;

    @Inject
    ProgressRepository progressRepository;
    @Keep
    private MainObserver<BackendEvent> backendObserver;
    @Keep
    private BasicRefreshBehaviour refreshBehaviour;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        model = getViewModel(AdminAlarmsListModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, R.layout.fragment_admin_alarms, container);
        binding.setVariable(BR.fragment, this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRefresh(view);
        setupRecycler(view);
    }

    private void setupRefresh(View view) {
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.refresh_layout);
        if (refreshLayout != null) {
            QueueUtils.style(refreshLayout);
            refreshBehaviour = new BasicRefreshBehaviour(getViewLifecycle(), refreshLayout, model);
            model.addModelObserver(refreshBehaviour);
            refreshLayout.setOnRefreshListener(refreshBehaviour);
        }
    }

    private void setupRecycler(@NonNull View view) {
        QueueRecyclerView recycler = view.findViewById(R.id.admin_alarms_list);
        FragmentActivity activity = requireActivity();

        final StatefulRecyclerView.PagedAdapter<AdminAlarm> adapter = new StatefulRecyclerView.PagedAdapter<>(R.layout.element_admin_alarm, BR.data);
        adapter.setInflater(activity);
        adapter.addVariable(BR.fragment, this);
        recycler.setStatefulAdapter(adapter);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recycler.setLayoutManager(layoutManager);

        model.getCurrentAdminAlarms().observe(getViewLifecycleOwner(), adapter::setData);
        backendObserver = recycler.newBackendObserver(model, getViewLifecycle());
        model.addDataObserver(backendObserver);
    }

    @Override
    public void onDestroyView() {
        View root = getView();
        if (root != null) {
            final StatefulRecyclerView recycler = root.findViewById(R.id.admin_alarms_list);
            recycler.close();
        }
        super.onDestroyView();
    }

    public void onItemSelected(AdminAlarm item) {
        if (item != null && item.getId() > Item.AUTO_ID
                && item.getProgress() != null && item.getProgress() > Item.AUTO_ID) {
            Intent intent = new Intent(getContext(), AdminAlarmActivity.class);
            intent.putExtra(AdminAlarm.ID, item.getId());
            intent.putExtra(Progress.ID, item.getProgress());
            startActivity(intent);
        }
    }

    public void deleteAlarm(Long id) {
        alarmsListRepository.deleteAdminAlarm(id);
    }

    public LiveData<Integer> getNoDataIconVisibility() {
        return model.getNoDataIconVisibility();
    }

    public void setEnabled(long alarmID, boolean enabled) {
        alarmsListRepository.updateAlarm(alarmID, alarm -> {
            if (!Objects.equals(alarm.getEnabled(), enabled)) {
                alarm.setEnabled(enabled);
                return true;
            } else
                return false;
        });
    }

    public LiveData<LoadedProgress> getLoadedProgress(Long progressID) {
        if (progressID != null && progressID > Item.AUTO_ID)
            return progressRepository.getLoadedData(progressID);
        else
            return null;
    }

    public String getNamespace() {
        return "admin_alarms_list_fragment";
    }
}
