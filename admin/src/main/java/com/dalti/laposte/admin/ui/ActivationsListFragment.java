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
import com.dalti.laposte.admin.model.ActivationsListModel;
import com.dalti.laposte.core.model.QueueRecyclerView;
import com.dalti.laposte.core.entity.Activation;
import com.dalti.laposte.core.ui.AbstractQueueFragment;
import com.dalti.laposte.core.util.QueueUtils;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.frontend.BasicRefreshBehaviour;
import dz.jsoftware95.silverbox.android.frontend.StatefulRecyclerView;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.observers.MainObserver;

@AndroidEntryPoint
public class ActivationsListFragment extends AbstractQueueFragment {

    ActivationsListModel model;
    @Keep
    private MainObserver<BackendEvent> mainObserver;
    @Keep
    private BasicRefreshBehaviour refreshBehaviour;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        model = getViewModel(ActivationsListModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, R.layout.fragment_activations, container);
        binding.setVariable(BR.fragment, this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRefresh(view);
        setupRecycler(view);
    }

    private void setupRefresh(View view) {
        final SwipeRefreshLayout refreshLayout = view.findViewById(com.dalti.laposte.R.id.refresh_layout);
        if (refreshLayout != null) {
            QueueUtils.style(refreshLayout);
            refreshBehaviour = new BasicRefreshBehaviour(getViewLifecycle(), refreshLayout, model);
            model.addModelObserver(refreshBehaviour);
            refreshLayout.setOnRefreshListener(refreshBehaviour);
        }
    }

    private void setupRecycler(@NonNull View view) {
        QueueRecyclerView recycler = view.findViewById(R.id.activations_list);
        FragmentActivity activity = requireActivity();

        final StatefulRecyclerView.PagedAdapter<Activation> adapter = new StatefulRecyclerView.PagedAdapter<>(R.layout.element_activation, BR.data);
        adapter.setInflater(activity);
        adapter.addVariable(BR.fragment, this);
        recycler.setStatefulAdapter(adapter);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recycler.setLayoutManager(layoutManager);

        model.getCurrentActivations().observe(getViewLifecycleOwner(), adapter::setData);
        mainObserver = recycler.newBackendObserver(model, getViewLifecycle());
        model.addDataObserver(mainObserver);
    }

    @Override
    public void onDestroyView() {
        View root = getView();
        if (root != null) {
            final StatefulRecyclerView recycler = root.findViewById(R.id.activations_list);
            recycler.close();
        }
        super.onDestroyView();
    }

    public void onItemSelected(Activation item) {
        if (item != null && item.getId() > Item.AUTO_ID) {
            Intent intent = new Intent(getContext(), ActivationCodeActivity.class);
            intent.putExtra(Activation.ID, item.getId());
            startActivity(intent);
        }
    }

    public LiveData<Integer> getNoDataIconVisibility() {
        return model.getNoDataIconVisibility();
    }
}
