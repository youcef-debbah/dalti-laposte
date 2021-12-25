package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dalti.laposte.BR;
import com.dalti.laposte.R;
import com.dalti.laposte.core.model.QueueRecyclerView;
import com.dalti.laposte.core.model.ServicesListModel;
import com.dalti.laposte.core.repositories.Service;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.frontend.BasicRefreshBehaviour;
import dz.jsoftware95.silverbox.android.frontend.StatefulRecyclerView;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

@AndroidEntryPoint
public class ServicesListFragment extends AbstractQueueFragment {

    private ServicesListModel model;
    private QueueActivitySupport activitySupport;

    @Inject
    public void setup(final QueueActivitySupport queueActivitySupport) {
        activitySupport = queueActivitySupport;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        model = getViewModel(ServicesListModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, R.layout.fragment_services, container);
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
            final BasicRefreshBehaviour refreshBehaviour = new BasicRefreshBehaviour(getViewLifecycle(), refreshLayout, model);
            model.addModelObserver(refreshBehaviour);
            refreshLayout.setOnRefreshListener(refreshBehaviour);
        }
    }

    private void setupRecycler(@NonNull View view) {
        QueueRecyclerView recycler = view.findViewById(R.id.services_list);
        FragmentActivity activity = requireActivity();

        final StatefulRecyclerView.PagedAdapter<Service> adapter = new StatefulRecyclerView.PagedAdapter<>(R.layout.element_service, BR.data);
        adapter.setInflater(activity);
        adapter.addVariable(BR.fragment, this);
        recycler.setStatefulAdapter(adapter);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recycler.setLayoutManager(layoutManager);

        model.getCurrentServices().observe(getViewLifecycleOwner(), adapter::setData);
        model.addDataObserver(recycler.newBackendObserver(model, getViewLifecycle()));
    }

    @Override
    public void onDestroyView() {
        View root = getView();
        if (root != null) {
            final StatefulRecyclerView recycler = root.findViewById(R.id.services_list);
            recycler.close();
        }
        super.onDestroyView();
    }

    public void onServiceSelected(Long id) {
        model.setCurrentService(id);
        openActivity(activitySupport.getMainActivity());
    }

    public void onItemOpened(Service item) {
        Intent data = new Intent(requireContext(), ServiceDescriptionActivity.class);
        data.putExtra(Service.ID, item.getId());
        startActivity(data);
        Teller.logViewItemEvent(Teller.newItem(item.getId(), Service.TABLE_NAME));
    }

    public LiveData<Integer> noDataIconVisibility() {
        return model.getNoDataIconVisibility();
    }

    public String getNamespace() {
        return "services_list_fragment";
    }
}
