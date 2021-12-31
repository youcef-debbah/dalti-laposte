package com.dalti.laposte.admin.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.databinding.ViewDataBinding;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.AdminDashboardRepository;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.Form;
import com.dalti.laposte.core.ui.SelectOneInputDialog;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;

import static dz.jsoftware95.queue.common.PostOfficeAvailability.BasicState;
import static dz.jsoftware95.queue.common.PostOfficeAvailability.DEFAULT_ACTIVE_STATE;
import static dz.jsoftware95.queue.common.PostOfficeAvailability.LiquidityState;
import static dz.jsoftware95.queue.common.PostOfficeAvailability.ServiceState;

@AndroidEntryPoint
public class ServiceStateFormActivity extends AbstractQueueActivity implements Form {

    public static final String PROGRESS_ID_KEY = "PROGRESS_ID_KEY";
    public static final String PROGRESS_ICON_KEY = "PROGRESS_ICON_KEY";
    public static final String SERVICE_DESCRIPTION_KEY = "SERVICE_DESCRIPTION_KEY";
    public static final String CURRENT_TOKEN_KEY = "CURRENT_TOKEN_KEY";
    public static final String WAITING_KEY = "WAITING_KEY";
    public static final String AVAILABILITY_KEY = "BASIC_STATE_KEY";

    private Long progressId;
    private Integer progressIcon;
    private String serviceDescription;
    private NumberPicker currentTokenInput;
    private NumberPicker waitingInput;
    private RadioGroup basicStateGroup;
    private RadioGroup liquidityStateGroup;
    private TextView maxWithdrawalInput;

    protected AdminDashboardRepository repository;

    @Inject
    public void setRepository(AdminDashboardRepository repository) {
        this.repository = repository;
    }

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        super.setupLayout(savedState);
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_service_state_form);
        binding.setVariable(BR.activity, this);
        View view = binding.getRoot();

        progressId = getLongExtra(PROGRESS_ID_KEY, -1);
        Assert.that(progressId > Item.AUTO_ID);
        progressIcon = getIntegerExtra(PROGRESS_ICON_KEY, R.drawable.ic_padded_man_24);
        serviceDescription = getStringExtra(SERVICE_DESCRIPTION_KEY);

        AppConfig appConfig = AppConfig.getInstance();

        currentTokenInput = view.findViewById(R.id.current_token_input);
        if (currentTokenInput != null) {
            currentTokenInput.setMaxValue(appConfig.getAsInt(StringNumberSetting.MAX_TOKEN));
            currentTokenInput.setValue(getIntegerState(savedState, CURRENT_TOKEN_KEY, 0));
        }

        waitingInput = view.findViewById(R.id.waiting_input);
        if (waitingInput != null) {
            waitingInput.setMaxValue(appConfig.getAsInt(StringNumberSetting.MAX_WAITING));
            waitingInput.setValue(getIntegerState(savedState, WAITING_KEY, 0));
        }

        int padding = getResources().getDimensionPixelOffset(R.dimen.small);
        PostOfficeAvailability availability = new PostOfficeAvailability(getIntegerState(savedState, AVAILABILITY_KEY, DEFAULT_ACTIVE_STATE));
        basicStateGroup = view.findViewById(R.id.basic_state_group);
        if (basicStateGroup != null)
            SelectOneInputDialog.Builder.populateRadioGroup(this, basicStateGroup, padding,
                    R.array.basic_states, null, availability.getBasicState().ordinal());

        liquidityStateGroup = view.findViewById(R.id.liquidity_state_group);
        if (liquidityStateGroup != null)
            SelectOneInputDialog.Builder.populateRadioGroup(this, liquidityStateGroup, padding,
                    R.array.liquidity_states, null, availability.getLiquidityState().ordinal());

        maxWithdrawalInput = view.findViewById(R.id.withdrawal_limit_input);
        if (maxWithdrawalInput != null) {
            maxWithdrawalInput.setText(String.valueOf(availability.getMaxWithdrawal()));
            QueueUtils.setEditorAction(maxWithdrawalInput, this::submitOnNoError, "max_withdrawal_input");
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        outState.putInt(AVAILABILITY_KEY, getCurrentAvailabilityInput().getCode());

        if (currentTokenInput != null)
            outState.putInt(CURRENT_TOKEN_KEY, QueueUtils.getConfirmedInput(currentTokenInput));

        if (waitingInput != null)
            outState.putInt(WAITING_KEY, QueueUtils.getConfirmedInput(waitingInput));

        super.onSaveInstanceState(outState);
    }

    public Long getProgressId() {
        return progressId;
    }

    public Integer getProgressIcon() {
        return progressIcon;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void showAdminAlarmForm(View view) {
        Intent intent = new Intent(this, AdminAlarmFormActivity.class);
        intent.putExtra(Progress.ID, progressId);
        startActivity(intent);
    }

    public void back(View v) {
        cancelResultAndFinish();
    }

    public void reset(View v) {
        if (progressId != null && progressId > 0)
            repository.resetToken(progressId);
        else
            QueueUtils.handleServiceMissing();

        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void submit() {
        if (progressId != null && progressId > 0) {
            PostOfficeAvailability availability = getCurrentAvailabilityInput();
            SetCustomAction action = new SetCustomAction(getCurrentTokenInput(), getWaitingInput(), availability.getCode());
            repository.setToken(action, progressId);
        } else
            QueueUtils.handleServiceMissing();

        setResult(Activity.RESULT_OK);
        finish();
    }

    @NotNull
    private PostOfficeAvailability getCurrentAvailabilityInput() {
        return new PostOfficeAvailability(
                getMaxWithdrawalInput(), getBasicStateInput(), getLiquidityInput(),
                ServiceState.SERVICE_STATE_ACTIVE);
    }

    private LiquidityState getLiquidityInput() {
        Integer input = liquidityStateGroup == null ? null : SelectOneInputDialog.idToIndex(liquidityStateGroup.getCheckedRadioButtonId());
        return GlobalUtil.getElement(input, LiquidityState.values(), LiquidityState.LIQUIDITY_STATE_UNKNOWN);
    }

    private BasicState getBasicStateInput() {
        Integer input = basicStateGroup == null ? null : SelectOneInputDialog.idToIndex(basicStateGroup.getCheckedRadioButtonId());
        return GlobalUtil.getElement(input, BasicState.values(), BasicState.BASIC_STATE_OPENED);
    }

    private int getMaxWithdrawalInput() {
        if (maxWithdrawalInput != null) {
            Integer input = StringUtil.parseAsInteger(maxWithdrawalInput.getText());
            if (input != null && input > 0)
                return input;
        }
        return 0;
    }

    private int getCurrentTokenInput() {
        if (currentTokenInput != null)
            return QueueUtils.getConfirmedInput(currentTokenInput);
        else
            return 0;
    }

    private int getWaitingInput() {
        if (waitingInput != null)
            return QueueUtils.getConfirmedInput(waitingInput);
        else
            return 0;
    }

    public String getNamespace() {
        return "service_state_activity";
    }
}
