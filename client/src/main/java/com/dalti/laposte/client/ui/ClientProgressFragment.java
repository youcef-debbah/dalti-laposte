package com.dalti.laposte.client.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.client.repository.ClientActivationRepository;
import com.dalti.laposte.core.model.DashboardModel;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.Estimation;
import com.dalti.laposte.core.repositories.Event;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.Selection;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.InputDialog;
import com.dalti.laposte.core.ui.ProgressFragment;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.observers.DuoMainObserver;

@AndroidEntryPoint
public class ClientProgressFragment extends ProgressFragment implements DialogInterface.OnClickListener {

    private static final String TICKET_RANK = "ticketRank";
    private static final String TICKET_INPUT = "ticketInput";

    @Inject
    ClientActivationRepository activationRepository;

    private DashboardModel model;

    @Nullable
    private AlertDialog ticketDialog;

    @Nullable
    private NumberPicker ticketInput;
    private int ticketRank = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        model = getViewModel(DashboardModel.class);
    }

    @Override
    public DashboardModel getModel() {
        return model;
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_client_progress;
    }

    @Override
    public int getRankContainerID() {
        return R.id.progress_rank_container;
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        outState.putInt(TICKET_RANK, ticketRank);
        if (ticketInput != null)
            outState.putInt(TICKET_INPUT, QueueUtils.getConfirmedInput(ticketInput));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreateView(ViewDataBinding binding, Bundle savedInstanceState) {
        binding.setVariable(BR.fragment, this);
        setupTicketDialog(savedInstanceState);
    }

    @Override
    protected void addProgressView(MutableLiveData<Progress> progress, ViewGroup container) {
        ViewDataBinding rankView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.section_client_progress, container, true);
        rankView.setLifecycleOwner(getCurrentViewLifecycle());
        progress.observe(getCurrentViewLifecycle(), new DuoMainObserver<ViewDataBinding, ClientProgressFragment, Progress>(getCurrentViewLifecycle(), rankView, this) {
            @Override
            protected void onUpdate(@NonNull ViewDataBinding rankView,
                                    @NonNull ClientProgressFragment fragment,
                                    @Nullable Progress progress) {
                rankView.setVariable(BR.fragment, fragment);
                rankView.setVariable(BR.progress, progress);
            }
        });
    }

    private void setupTicketDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCustomTitle(InputDialog.newTitleView(context, R.string.enter_ticket_number));

        final NumberPicker ticketInput = new NumberPicker(context);
        ticketInput.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        ticketInput.setMinValue(1);
        ticketInput.setMaxValue(AppConfig.getInstance().getAsInt(StringNumberSetting.MAX_TOKEN));
        QueueUtils.showKeyboardOnFocus(ticketInput);

        LinearLayoutCompat layout = new LinearLayoutCompat(context);
        layout.setOrientation(LinearLayoutCompat.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.addView(ticketInput);

        builder.setView(layout);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
        AlertDialog ticketDialog = builder.create();

        this.ticketInput = ticketInput;
        this.ticketDialog = ticketDialog;

        if (savedInstanceState != null) {
            int oldRank = savedInstanceState.getInt(TICKET_RANK, -1);
            if (oldRank > -1)
                openTicketDialog(oldRank);
            ticketInput.setValue(savedInstanceState.getInt(TICKET_INPUT));
        }
    }


    @Override
    public void onClick(DialogInterface dialog, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            if (AppConfig.getInstance().getActivationState().isActive()) {
                NumberPicker ticketInput = this.ticketInput;
                int ticketRank = this.ticketRank;
                if (ticketInput != null && ticketRank > -1) {
                    Selection selection = getSelectionValue();
                    if (Selection.hasService(selection)) {
                        long id = selection.getProgressID(ticketRank);
                        int ticket = QueueUtils.getConfirmedInput(ticketInput);
                        model.setTicket(ticket, id);
                        Teller.logSetTicket(id, ticket);
                    } else
                        QueueUtils.handleServiceMissing();
                } else
                    Teller.logUnexpectedCondition();
            } else
                Teller.logUnexpectedCondition();
        }

        dialog.cancel();
        this.ticketRank = -1;
    }

    public void openTicketDialog(int ticketRank) {
        AppConfig appConfig = AppConfig.getInstance();
        if (appConfig.getActivationState().isActive()) {
            AlertDialog ticketDialog = this.ticketDialog;
            NumberPicker ticketInput = this.ticketInput;
            if (ticketDialog != null && ticketInput != null) {
                Selection selection = getSelectionValue();
                if (Selection.hasService(selection)) {
                    Progress progress = selection.getProgressValue(ticketRank);
                    if (progress != null) {
                        Integer ticket = progress.getTicket();
                        if (ticket != null && ticket > 0)
                            ticketInput.setValue(ticket);
                        else
                            ticketInput.setValue(progress.getCurrentTokenInt() + progress.getWaitingInt() + 1);

                        this.ticketRank = ticketRank;
                        ticketDialog.show();

                        final TextView input = QueueUtils.getTextInput(ticketInput);
                        if (input != null)
                            input.requestFocus();

                        Teller.logSelectContentEvent(String.valueOf(progress.getId()), Progress.TABLE_NAME + "_ticket_dialog");
                    } else
                        Teller.logUnexpectedCondition();
                } else
                    QueueUtils.handleServiceMissing();
            } else
                Teller.logUnexpectedCondition();
        } else
            activationRepository.toastActivationNeeded("open_ticket_dialog");
    }

    public void deleteTicket(long progress) {
        model.setTicket(null, progress);
        Teller.logClearTicket(progress, Event.Trigger.CLICK_CONTROLLER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ticketDialog != null) {
            ticketDialog.dismiss();
            ticketDialog = null;
        }
        ticketInput = null;
    }

    public LiveData<Estimation> getEstimation(Long progressID) {
        return model.getEstimation(progressID);
    }

    public void help(View v) {
        startActivity(ClientHelpActivity.class);
    }

    public String getNamespace() {
        return "client_progress_fragment";
    }
}
