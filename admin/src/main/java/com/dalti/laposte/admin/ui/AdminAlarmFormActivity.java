package com.dalti.laposte.admin.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.AdminAlarmFormModel;
import com.dalti.laposte.admin.repositories.AdminAlarmsListRepository;
import com.dalti.laposte.admin.repositories.AdminAlarmsRepository;
import com.dalti.laposte.core.entity.AdminAlarm;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.LoadedProgress;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.ProgressRepository;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.entity.TurnAlarm;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.Form;
import com.dalti.laposte.core.ui.SelectOneInputDialog;
import com.dalti.laposte.core.util.QueueUtils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

@AndroidEntryPoint
public class AdminAlarmFormActivity extends AbstractQueueActivity implements Form {

    public static final String KEY_EDIT_MODE = "KEY_EDIT_MODE";

    AdminAlarmFormModel model;

    @Inject
    AdminAlarmsRepository alarmsRepository;

    @Inject
    AdminAlarmsListRepository alarmsListRepository;

    @Inject
    ProgressRepository progressRepository;

    private long progressID;
    private Long alarmID;
    private MutableLiveData<Integer> progressIcon;
    private LiveDataWrapper<String> serviceDescription;

    TextView phoneInput;
    NumberPicker ticketInput;
    NumberPicker durationInput;
    NumberPicker queueInput;
    RadioGroup liquidityInput;

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        progressIcon = new MutableLiveData<>();
        serviceDescription = new LiveDataWrapper<>();
        model = getViewModel(AdminAlarmFormModel.class);
        model.setEditMode(isEditMode());

        setProgressID(getLongExtra(Progress.ID, Item.AUTO_ID));

        long alarmExtra = getLongExtra(AdminAlarm.ID, Item.AUTO_ID);
        if (alarmExtra > Item.AUTO_ID)
            alarmsRepository.getLoadedData(alarmExtra).observe(this, alarm -> {
                if (alarm != null) {
                    Long progressID = alarm.getProgress();
                    if (progressID != null && progressID > Item.AUTO_ID) {
                        alarmID = alarmExtra;
                        setProgressID(progressID);
                        SelectOneInputDialog.setSelectionByIndex(liquidityInput, alarm.getLiquidity());

                        if (phoneInput != null)
                            phoneInput.setText(StringUtil.trimPrefix(alarm.getPhone(), QueueUtils.getString(R.string.phone_prefix)));
                        if (ticketInput != null)
                            ticketInput.setValue(alarm.getTicket());
                        if (durationInput != null)
                            durationInput.setValue((int) TimeUnit.MILLISECONDS.toMinutes(alarm.getDuration()));
                        if (queueInput != null)
                            queueInput.setValue(alarm.getQueue());

                        updateModel();
                    }
                }
            });

        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_admin_alarm_form);
        binding.setVariable(BR.activity, this);
        binding.setVariable(BR.model, model);

        int padding = getResources().getDimensionPixelOffset(R.dimen.small);
        AppConfig appConfig = AppConfig.getInstance();
        View root = binding.getRoot();

        phoneInput = root.findViewById(R.id.phone_input);
        QueueUtils.setEditorAction(phoneInput, this::submitForm, "admin_alarm_phone_input");
        ticketInput = root.findViewById(R.id.ticket_input);
        if (ticketInput != null) {
            ticketInput.setMinValue(0);
            ticketInput.setMaxValue(appConfig.getAsInt(StringNumberSetting.MAX_TOKEN));
            ticketInput.setValue(model.getTicket());
        }
        queueInput = root.findViewById(R.id.queue_input);
        if (queueInput != null) {
            queueInput.setMinValue(appConfig.getAsInt(TurnAlarm.Settings.MIN_QUEUE_LENGTH_INPUT));
            queueInput.setMaxValue(appConfig.getAsInt(TurnAlarm.Settings.MAX_QUEUE_LENGTH_INPUT));
            queueInput.setValue(model.getQueue());
        }
        durationInput = root.findViewById(R.id.duration_input);
        if (durationInput != null) {
            durationInput.setMinValue(appConfig.getAsInt(TurnAlarm.Settings.MIN_BEFOREHAND_INPUT));
            durationInput.setMaxValue(appConfig.getAsInt(TurnAlarm.Settings.MAX_BEFOREHAND_INPUT));
            durationInput.setValue(model.getDuration());
            ContextUtils.setDisplay(durationInput, getString(R.string.duration_in_min));
            durationInput.setOnValueChangedListener((view, oldValue, newValue) -> {
                if (queueInput != null)
                    queueInput.setValue(AdminAlarmFormModel.getAutoQueueValue(newValue));
            });
        }
        liquidityInput = root.findViewById(R.id.liquidity_input);
        if (liquidityInput != null)
            SelectOneInputDialog.Builder.populateRadioGroup(this, liquidityInput, padding,
                    R.array.alarm_liquidity_options, null, model.getLiquidity());
    }

    public boolean isEditMode() {
        return getBooleanExtra(KEY_EDIT_MODE, false);
    }

    private void setProgressID(long progress) {
        Assert.that(progress > Item.AUTO_ID);
        progressID = progress;

        progressIcon.setValue(Progress.getProgressIcon(IdentityManager.getProgressRank(progress)));

        LiveData<LoadedProgress> progressData = progressRepository.getLoadedData(progress);
        serviceDescription.setSource(Transformations.map(progressData, LoadedProgress::toServiceDescription));
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        updateModel();
    }

    private void updateModel() {
        model.setProgressID(progressID);
        model.setAlarmID(alarmID);
        if (phoneInput != null)
            model.setPhone(StringUtil.getString(phoneInput));
        if (ticketInput != null)
            model.setTicket(QueueUtils.getConfirmedInput(ticketInput));
        if (durationInput != null)
            model.setDuration(QueueUtils.getConfirmedInput(durationInput));
        if (queueInput != null)
            model.setQueue(QueueUtils.getConfirmedInput(queueInput));
        if (liquidityInput != null) {
            Integer liquidity = SelectOneInputDialog.idToIndex(liquidityInput.getCheckedRadioButtonId());
            if (liquidity != null)
                model.setLiquidity(liquidity);
        }
    }

    public long getProgressID() {
        return progressID;
    }

    public LiveData<Integer> getProgressIcon() {
        return progressIcon;
    }

    public LiveData<String> getServiceDescription() {
        return serviceDescription.getLiveData();
    }

    public AdminAlarmFormModel getModel() {
        return model;
    }

    public boolean submitForm(TextView phoneInput) {
        updateModel();
        boolean stay = model.submit();
        if (!stay)
            finish();
        return stay;
    }

    @Override
    public void submit() {
        submitForm(phoneInput);
    }


    public void back(View view) {
        cancelResultAndFinish();
    }

    public String getNamespace() {
        return "admin_alarm_activity";
    }
}
