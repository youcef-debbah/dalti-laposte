package com.dalti.laposte.client.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.dalti.laposte.client.BR;
import com.dalti.laposte.client.R;
import com.dalti.laposte.client.model.TurnAlarmModel;
import com.dalti.laposte.client.repository.TurnAlarmRepository;
import com.dalti.laposte.core.repositories.AlarmPhonePreference;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.NotificationUtils;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.entity.TurnAlarm;
import com.dalti.laposte.core.ui.AbstractQueueFragment;
import com.dalti.laposte.core.ui.DialogSupplier;
import com.dalti.laposte.core.ui.IntegerInputDialog;
import com.dalti.laposte.core.ui.SelectOneInputDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.common.ViewLifecycleRegistry;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

@AndroidEntryPoint
public final class AlarmsFragment extends AbstractQueueFragment {

    public static final String TURN_ALARM_ID = "TURN_ALARM_ID=";
    public static final int ID_LENGTH = TURN_ALARM_ID.length();

    private ViewLifecycleRegistry currentViewLifecycle;

    private ViewGroup alarmsContainer;

    private TurnAlarmModel model;

    private DialogSupplier<IntegerInputDialog<TurnAlarmModel>> newAlarmDialog;

    private DialogSupplier<IntegerInputDialog<TurnAlarmModel>> beforehandDurationDialog;
    private DialogSupplier<IntegerInputDialog<TurnAlarmModel>> maxQueueLengthDialog;
    private DialogSupplier<IntegerInputDialog<TurnAlarmModel>> snoozeDialog;

    private DialogSupplier<SelectOneInputDialog<TurnAlarmModel>> minLiquidityDialog;
    private DialogSupplier<SelectOneInputDialog<TurnAlarmModel>> priorityDialog;
    private DialogSupplier<SelectOneInputDialog<TurnAlarmModel>> ringtoneDialog;
    @Keep
    private AlarmsUpdater updater;
    private final LiveDataWrapper<Integer> noAlarmIconVisibility = new LiveDataWrapper<>(ContextUtils.VIEW_INVISIBLE);

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        model = getViewModel(TurnAlarmModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final ViewDataBinding binding = BindingUtil.inflate(this, inflater, R.layout.fragment_alarms, container);
        binding.setVariable(BR.fragment, this);
        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        DialogSupplier.saveDialog(newAlarmDialog, outState);
        DialogSupplier.saveDialog(beforehandDurationDialog, outState);
        DialogSupplier.saveDialog(maxQueueLengthDialog, outState);
        DialogSupplier.saveDialog(snoozeDialog, outState);
        DialogSupplier.saveDialog(minLiquidityDialog, outState);
        DialogSupplier.saveDialog(priorityDialog, outState);
        DialogSupplier.saveDialog(ringtoneDialog, outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        createDialogs(view, savedInstanceState);
        resetCurrentViewLifecycle();
        setupFab();
        alarmsContainer = view.findViewById(R.id.alarms_container);

        TurnAlarmRepository repository = model.getRepository();
        LiveData<List<TurnAlarm>> alarms = repository.getTurnAlarms();
        alarms.observe(currentViewLifecycle, this::updateAlarms);
        noAlarmIconVisibility.setSource(Transformations.map(alarms, list -> (list == null || !list.isEmpty()) ? ContextUtils.VIEW_INVISIBLE : ContextUtils.VIEW_VISIBLE));
        repository.addObserver(updater = new AlarmsUpdater(getViewLifecycleOwner(), this));
    }

    private void setupFab() {
        FloatingActionButton fab = requireActivity().findViewById(R.id.dashboard_fab);
        if (fab != null)
            fab.setOnClickListener(v -> DialogSupplier.showDialog(newAlarmDialog, null));
    }

    private static final class AlarmsUpdater extends UnMainObserver<AlarmsFragment, BackendEvent> {

        protected AlarmsUpdater(@NotNull LifecycleOwner lifecycleOwner,
                                @NotNull AlarmsFragment context) {
            super(lifecycleOwner, context);
        }

        @Override
        protected void onUpdate(@NotNull AlarmsFragment context,
                                @Nullable BackendEvent data) {
            if (data != null && data.shouldStopRefreshing())
                context.updateViewVariables();
        }
    }

    protected void createDialogs(@NonNull View view, Bundle savedInstanceState) {
        Context context = requireContext();

        newAlarmDialog = newBeforehandDialogBuilder(savedInstanceState, TurnAlarm.TABLE_NAME)
                .setDefaultValue(TurnAlarm.Settings.DEFAULT_BEFOREHAND_INPUT)
                .setInputListener((m, v, i) -> m.getRepository().addAlarm(i))
                .setTitle(R.string.new_alarm_dialog_title)
                .setPrefix(R.string.beforehand_dialog_pretext)
                .loadState(model, view, savedInstanceState)
        ;
        beforehandDurationDialog = newBeforehandDialogBuilder(savedInstanceState, TurnAlarm.BEFOREHAND_DURATION)
                .setTitle(R.string.beforehand_dialog_title)
                .setPrefix(R.string.beforehand_dialog_pretext)
                .loadState(model, view, savedInstanceState)
        ;

        maxQueueLengthDialog = new IntegerInputDialog.Builder<TurnAlarmModel>(context, TurnAlarm.MAX_QUEUE_LENGTH)
                .setMax(TurnAlarm.Settings.MAX_QUEUE_LENGTH_INPUT)
                .setMin(TurnAlarm.Settings.MIN_QUEUE_LENGTH_INPUT)
                .setStartLabel(R.string.at_max)
                .setEndLabel(R.string.people)
                .setTitle(R.string.queue_length_dialog_title)
                .setPrefix(R.string.queue_length_dialog_pretext)
                .loadState(model, view, savedInstanceState)
        ;

        snoozeDialog = new IntegerInputDialog.Builder<TurnAlarmModel>(context, TurnAlarm.SNOOZE)
                .setMin(TurnAlarm.Settings.MIN_SNOOZE_INPUT)
                .setMax(TurnAlarm.Settings.MAX_SNOOZE_INPUT)
                .setStartLabel(R.string.snooze_for)
                .setEndLabel(R.string.minutes)
                .setTitle(R.string.snooze_dialog_title)
                .loadState(model, view, savedInstanceState)
        ;

        minLiquidityDialog = new SelectOneInputDialog.Builder<TurnAlarmModel>(context, TurnAlarm.LIQUIDITY_OPTIONS, TurnAlarm.MIN_LIQUIDITY)
                .setDescriptions(R.array.alarm_liquidity_options_description)
                .setTitle(R.string.min_liquidity_dialog_title)
                .setPrefix(R.string.min_liquidity_dialog_pretext)
                .loadState(model, view, savedInstanceState)
        ;

        priorityDialog = new SelectOneInputDialog.Builder<TurnAlarmModel>(context, TurnAlarm.PRIORITY_OPTIONS, TurnAlarm.PRIORITY)
                .setDescriptions(R.array.alarm_priority_options_description)
                .setTitle(R.string.priority_dialog_title)
                .loadState(model, view, savedInstanceState)
        ;

        ringtoneDialog = new SelectOneInputDialog.Builder<TurnAlarmModel>(context, TurnAlarm.RINGTONE_OPTIONS, TurnAlarm.RINGTONE)
                .setTitle(R.string.ringtone_dialog_title)
                .setPrefix(R.string.ringtone_dialog_pretext)
                .loadState(model, view, savedInstanceState)
        ;
    }

    private IntegerInputDialog.Builder<TurnAlarmModel> newBeforehandDialogBuilder(Bundle savedInstanceState, String name) {
        return new IntegerInputDialog.Builder<TurnAlarmModel>(requireContext(), name)
                .setMax(TurnAlarm.Settings.MAX_BEFOREHAND_INPUT)
                .setMin(TurnAlarm.Settings.MIN_BEFOREHAND_INPUT)
                .setStartLabel(R.string.approximately)
                .setEndLabel(R.string.minutes)
                ;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DialogSupplier.dismissDialog(newAlarmDialog);
        DialogSupplier.dismissDialog(beforehandDurationDialog);
        DialogSupplier.dismissDialog(maxQueueLengthDialog);
        DialogSupplier.dismissDialog(snoozeDialog);
        DialogSupplier.dismissDialog(minLiquidityDialog);
        DialogSupplier.dismissDialog(priorityDialog);
        DialogSupplier.dismissDialog(ringtoneDialog);
        updater = null;
    }

    public void resetCurrentViewLifecycle() {
        if (currentViewLifecycle != null)
            currentViewLifecycle.markViewAsDestroyed();
        currentViewLifecycle = new ViewLifecycleRegistry(getViewLifecycleOwner());
    }

    private void updateViewVariables() {
        ViewGroup container = this.alarmsContainer;
        if (container != null) {
            for (int i = container.getChildCount() - 1; i > -1; i--) {
                View view = container.getChildAt(i);
                Long id = extractId(view);
                if (id != null) {
                    ViewDataBinding binding = DataBindingUtil.findBinding(view);
                    if (binding != null)
                        binding.invalidateAll();
                }
            }
        }
    }

    private void updateAlarms(List<TurnAlarm> turnAlarms) {
        ViewGroup container = this.alarmsContainer;
        if (container != null) {
            if (turnAlarms == null || turnAlarms.isEmpty())
                container.removeAllViews();
            else
                updateAlarms(container, turnAlarms);
        }
    }

    private void updateAlarms(ViewGroup container, List<TurnAlarm> turnAlarms) {
        int viewsCount = container.getChildCount();
        if (viewsCount == 0) {
            for (TurnAlarm turnAlarm : turnAlarms)
                newAlarmView(container, turnAlarm, true);
        } else {
            Map<Long, TurnAlarm> data = toMap(turnAlarms);
            Map<Long, View> updatedViews = updateViews(container, viewsCount, data);
            addViews(container, data, updatedViews);
        }
    }

    private Map<Long, View> updateViews(ViewGroup container, int viewsCount, Map<Long, TurnAlarm> data) {
        List<View> toRemove = new LinkedList<>();
        Map<Long, View> updatedViews = new LinkedHashMap<>(viewsCount);
        for (int i = viewsCount - 1; i > -1; i--) {
            View view = container.getChildAt(i);
            Long id = extractId(view);
            if (id != null) {
                TurnAlarm turnAlarm = data.get(id);
                if (turnAlarm != null)
                    updatedViews.put(id, updateAlarmView(view, turnAlarm));
                else
                    toRemove.add(view);
            } else
                toRemove.add(view);
        }

        for (View view : toRemove)
            container.removeView(view);

        return updatedViews;
    }

    private void addViews(ViewGroup container, Map<Long, TurnAlarm> data, Map<Long, View> updatedViews) {
        int i = -1;
        for (TurnAlarm alarm : data.values()) {
            i++;
            View updatedView = updatedViews.get(alarm.getId());
            if (updatedView != null) {
                // already updated, put the view in the right index
                View currentView = container.getChildAt(i);
                if (currentView != updatedView) {
                    container.removeView(updatedView);
                    container.addView(updatedView, i);
                }
            } else {
                // add a new view in the right index
                container.addView(newAlarmView(container, alarm, false), i);
            }
        }
    }

    private Map<Long, TurnAlarm> toMap(List<TurnAlarm> turnAlarms) {
        Map<Long, TurnAlarm> data = new LinkedHashMap<>(turnAlarms.size());
        for (TurnAlarm turnAlarm : turnAlarms)
            data.put(turnAlarm.getId(), turnAlarm);
        return data;
    }

    private Long extractId(View view) {
        if (view != null) {
            String tag = StringUtil.toString(view.getTag());
            if (tag != null && tag.startsWith(TURN_ALARM_ID))
                return StringUtil.parseLong(tag.substring(ID_LENGTH));
        }
        return null;
    }

    private View updateAlarmView(View view, TurnAlarm turnAlarm) {
        ViewDataBinding binding = DataBindingUtil.findBinding(view);
        return updateAlarmBinding(turnAlarm, binding);
    }

    private View newAlarmView(ViewGroup container, TurnAlarm turnAlarm, boolean attach) {
        ViewDataBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.card_turn_alarm, container, attach);
        return updateAlarmBinding(turnAlarm, binding);
    }

    private View updateAlarmBinding(TurnAlarm turnAlarm, ViewDataBinding binding) {
        if (binding != null) {
            View alarmView = binding.getRoot();
            if (turnAlarm != null) {
                if (!model.getRepository().hasPendingUpdate(turnAlarm.getId()))
                    binding.setVariable(BR.alarm, turnAlarm);
                alarmView.setTag(TURN_ALARM_ID + turnAlarm.getId());
            }
            binding.setVariable(BR.fragment, this);
            return alarmView;
        } else
            return null;
    }

    public void deleteAlarm(Long id) {
        if (id != null)
            model.getRepository().deleteAlarm(id);
    }

    public void testAlarm(TurnAlarm alarm) {
        if (alarm != null) {
            NotificationUtils.startTurnAlarmNotification(NotificationUtils.NOTIFICATION_ID_DEFAULT, NotificationUtils.NOTIFICATION_TAG_TURN_ALARM_TEST,
                    null, null, alarm.getBeforehandDuration(), alarm.getMaxQueueLength() / 2,
                    getString(R.string.test_service_description), alarm.isVibrate(), alarm.getRingtone(), alarm.getPriority(), null);
        }
    }

    public void enableSMS(Long id) {
        if (id != null && id > Item.AUTO_ID) {
            AppConfig appConfig = AppConfig.getInstance();
            String oldInput = appConfig.get(StringSetting.LATEST_TURN_ALARM_PHONE);
            if (StringUtil.isBlank(oldInput))
                editPhone(id);
            else {
                appConfig.put(new AlarmPhonePreference(id), oldInput);
                model.getRepository().updatePhone(id, oldInput);
            }
        }
    }

    public void editPhone(Long id) {
        if (id != null && id > Item.AUTO_ID) {
            Intent intent = new Intent(requireContext(), ClientPhoneFormActivity.class);
            intent.putExtra(TurnAlarm.ID, id);
            startActivity(intent);
        }
    }

    // boolean alarm properties

    public boolean isEnabledActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.ENABLED);
    }

    public void updateEnabled(Long id, Boolean value, View view) {
        if (id != null && value != null && view != null && view.isEnabled()) {
            view.setEnabled(false);
            model.getRepository().updateEnabled(id, value);
        }
    }

    public boolean isVibrateActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.VIBRATE);
    }

    public void updateVibrate(Long id, Boolean value, View view) {
        if (id != null && value != null && view != null && view.isEnabled()) {
            view.setEnabled(false);
            model.getRepository().updateVibrate(id, value);
        }
    }

    public Integer getExtraInputVisibility() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    // numeric alarm properties

    public void openBeforehandDurationDialog(Long id, Long value) {
        if (id != null && value != null)
            DialogSupplier.showDialog((int) TimeUnit.MILLISECONDS.toMinutes(value), beforehandDurationDialog, newUpdateBeforehandDurationAction(id));
    }

    private static InputListener<Integer, TurnAlarmModel> newUpdateBeforehandDurationAction(long id) {
        return (model, root, value) -> {
            if (value != null) {
                View inputView = ContextUtils.findViewWithNestedTags(root, TURN_ALARM_ID + id, TurnAlarm.BEFOREHAND_DURATION);
                if (inputView != null && inputView.isEnabled()) {
                    inputView.setEnabled(false);
                    model.getRepository().updateBeforehandDuration(id, TimeUnit.MINUTES.toMillis(value.longValue()));
                } else
                    Teller.logUnexpectedCondition("null input view: " + (inputView == null));
            }
        };
    }

    public void openMaxQueueLengthDialog(Long id, Integer value) {
        if (id != null && value != null)
            DialogSupplier.showDialog(value, maxQueueLengthDialog, newUpdateMaxQueueLengthAction(id));
    }

    private static InputListener<Integer, TurnAlarmModel> newUpdateMaxQueueLengthAction(long id) {
        return (model, root, value) -> {
            if (value != null) {
                View inputView = ContextUtils.findViewWithNestedTags(root, TURN_ALARM_ID + id, TurnAlarm.MAX_QUEUE_LENGTH);
                if (inputView != null && inputView.isEnabled()) {
                    inputView.setEnabled(false);
                    model.getRepository().updateMaxQueueLength(id, value);
                } else
                    Teller.logUnexpectedCondition("null input view: " + (inputView == null));
            }
        };
    }

    public void openSnoozeDialog(Long id, Integer value) {
        if (id != null && value != null)
            DialogSupplier.showDialog((int) TimeUnit.MILLISECONDS.toMinutes(value), snoozeDialog, newSnoozeAction(id));
    }

    private static InputListener<Integer, TurnAlarmModel> newSnoozeAction(long id) {
        return (model, root, value) -> {
            if (value != null) {
                View inputView = ContextUtils.findViewWithNestedTags(root, TURN_ALARM_ID + id, TurnAlarm.SNOOZE);
                if (inputView != null && inputView.isEnabled()) {
                    inputView.setEnabled(false);
                    model.getRepository().updateSnooze(id, (int) TimeUnit.MINUTES.toMillis(value));
                } else
                    Teller.logUnexpectedCondition("null input view: " + (inputView == null));
            }
        };
    }

    public boolean isBeforehandDurationActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.BEFOREHAND_DURATION);
    }

    public boolean isMaxQueueLengthActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.MAX_QUEUE_LENGTH);
    }

    public boolean isSnoozeActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.SNOOZE);
    }

    // name properties

    public void openMinLiquidityDialog(Long id, Integer value) {
        if (id != null && value != null)
            DialogSupplier.showDialog(value, minLiquidityDialog, newUpdateMinLiquidityAction(id));
    }

    private static InputListener<Integer, TurnAlarmModel> newUpdateMinLiquidityAction(long id) {
        return (model, root, value) -> {
            if (value != null) {
                View inputView = ContextUtils.findViewWithNestedTags(root, TURN_ALARM_ID + id, TurnAlarm.MIN_LIQUIDITY);
                if (inputView != null && inputView.isEnabled()) {
                    inputView.setEnabled(false);
                    model.getRepository().updateMinLiquidity(id, value);
                } else
                    Teller.logUnexpectedCondition("null input view: " + (inputView == null));
            }
        };
    }

    public boolean isMinLiquidityActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.MIN_LIQUIDITY);
    }

    public void openPriorityDialog(Long id, Integer value) {
        if (id != null && value != null)
            DialogSupplier.showDialog(value, priorityDialog, newUpdatePriorityAction(id));
    }

    private static InputListener<Integer, TurnAlarmModel> newUpdatePriorityAction(long id) {
        return (model, root, value) -> {
            if (value != null) {
                View inputView = ContextUtils.findViewWithNestedTags(root, TURN_ALARM_ID + id, TurnAlarm.PRIORITY);
                if (inputView != null && inputView.isEnabled()) {
                    inputView.setEnabled(false);
                    model.getRepository().updatePriority(id, value);
                } else
                    Teller.logUnexpectedCondition("null input view: " + (inputView == null));
            }
        };
    }

    public boolean isPriorityActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.PRIORITY);
    }

    public void openRingtoneDialog(Long id, Integer value) {
        if (id != null && value != null)
            DialogSupplier.showDialog(value, ringtoneDialog, newUpdateRingtoneAction(id));
    }

    private static InputListener<Integer, TurnAlarmModel> newUpdateRingtoneAction(long id) {
        return (model, root, value) -> {
            if (value != null) {
                View inputView = ContextUtils.findViewWithNestedTags(root, TURN_ALARM_ID + id, TurnAlarm.RINGTONE);
                if (inputView != null && inputView.isEnabled()) {
                    inputView.setEnabled(false);
                    model.getRepository().updateRingtone(id, value);
                } else
                    Teller.logUnexpectedCondition("null input view: " + (inputView == null));
            }
        };
    }

    public boolean isRingtoneActive(Long id) {
        return model.getRepository().isActive(id, TurnAlarm.RINGTONE);
    }

    public LiveData<Integer> noAlarmIconVisibility() {
        return noAlarmIconVisibility.getLiveData();
    }
}
