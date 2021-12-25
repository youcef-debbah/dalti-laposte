package com.dalti.laposte.admin.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.admin.BR;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.repositories.AdminDashboardRepository;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.DialogSupplier;
import com.dalti.laposte.core.ui.Form;
import com.dalti.laposte.core.ui.IconInputDialog;
import com.dalti.laposte.core.ui.NoteState;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@AndroidEntryPoint
public class ServiceNoteFormActivity extends AbstractQueueActivity implements Form {

    public static final String SERVICE_ID_KEY = "SERVICE_ID_KEY";
    public static final String ICON_1_KEY = "ICON_1_KEY";
    public static final String ICON_2_KEY = "ICON_2_KEY";
    public static final String ICON_COLOR_1_KEY = "ICON_COLOR_1_KEY";
    public static final String ICON_COLOR_2_KEY = "ICON_COLOR_2_KEY";
    public static final String CLOSE_TIME_KEY = "CLOSE_TIME_KEY";
    public static final String DEFAULT_CLOSE_TIME_KEY = "DEFAULT_CLOSE_TIME_KEY";

    private long serviceID = Item.AUTO_ID;

    private TextView noteEngInput;
    private TextView noteFreInput;
    private TextView noteArbInput;

    private DialogSupplier<IconInputDialog<ServiceNoteFormActivity>> firstIconDialog;
    private DialogSupplier<IconInputDialog<ServiceNoteFormActivity>> secondIconDialog;

    private AppCompatImageView firstIconPreview;
    private AppCompatImageView secondIconPreview;
    private Spinner firstIconTypeInput;
    private Spinner secondIconTypeInput;

    private int firstIconInput;
    private int secondIconInput;
    private int firstIconColorInput;
    private int secondIconColorInput;

    protected AdminDashboardRepository adminDashboardRepository;
    protected ExtraRepository extraRepository;

    private int compactIconSize;
    private int largeIconSize;

    private MaterialTimePicker closeTimePicker;
    private long closeTimeInput = -1;
    private final MutableLiveData<String> closeTime = new MutableLiveData<>(getCloseTimeText(-1));

    @Inject
    public void setup(AdminDashboardRepository adminDashboardRepository,
                      ExtraRepository extraRepository) {
        this.adminDashboardRepository = adminDashboardRepository;
        this.extraRepository = extraRepository;
    }

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        super.setupLayout(savedState);
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_service_note_form);
        binding.setVariable(BR.activity, this);

        serviceID = getLongExtra(SERVICE_ID_KEY, Item.AUTO_ID);

        compactIconSize = getResources().getDimensionPixelSize(R.dimen.compact_icon);
        largeIconSize = getResources().getDimensionPixelSize(R.dimen.large_icon);

        noteEngInput = findViewById(R.id.note_eng_input);
        noteFreInput = findViewById(R.id.note_fre_input);
        noteArbInput = findViewById(R.id.note_arb_input);

        int noteState = AppConfig.getInstance().getAsInt(StringNumberSetting.NOTE_STATE);
        firstIconInput = NoteState.normalizeIconIndex(getIntegerState(savedState, ICON_1_KEY, NoteState.getFirstIconIndex(noteState)));
        secondIconInput = NoteState.normalizeIconIndex(getIntegerState(savedState, ICON_2_KEY, NoteState.getSecondIconIndex(noteState)));
        firstIconColorInput = NoteState.normalizeIconColorIndex(getIntegerState(savedState, ICON_COLOR_1_KEY, NoteState.getFirstIconColorIndex(noteState)));
        secondIconColorInput = NoteState.normalizeIconColorIndex(getIntegerState(savedState, ICON_COLOR_2_KEY, NoteState.getSecondIconColorIndex(noteState)));

        setupColorInput();
        setupIconPreviews(savedState);
        setupCloseTimeInput(savedState);
    }

    private void setupCloseTimeInput(Bundle savedState) {
        long closeTime = getLongState(savedState, CLOSE_TIME_KEY, -1);
        setCloseTimeInput(closeTime);
        closeTimePicker = newCloseTimePicker(closeTime);
    }

    private MaterialTimePicker newCloseTimePicker(long closeTime) {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText(R.string.select_next_close_time);

        if (closeTime > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(closeTime);
            builder.setHour(calendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(Calendar.MINUTE));
        } else
            builder.setHour(18).setMinute(0);

        MaterialTimePicker picker = builder.build();
        picker.addOnPositiveButtonClickListener(this::updateCloseTime);
        return picker;
    }

    private void updateCloseTime(View v) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, closeTimePicker.getMinute());
        calendar.set(Calendar.HOUR_OF_DAY, closeTimePicker.getHour());
        setCloseTimeInput(calendar.getTimeInMillis());
    }

    public void setCloseTimeInput(long time) {
        closeTimeInput = time;
        closeTime.setValue(getCloseTimeText(time));
    }

    private String getCloseTimeText(long time) {
        if (time < 0) {
            String defaultTime = getStringExtra(DEFAULT_CLOSE_TIME_KEY);
            if (defaultTime != null)
                return QueueUtils.getString(R.string.default_with_value, defaultTime);
            else
                return QueueUtils.getString(R.string.default_simple);
        } else
            return TimeUtils.formatAsShortTime(time);
    }

    public long getServiceID() {
        return serviceID;
    }

    public LiveData<String> getCloseTime() {
        return closeTime;
    }

    public void showCloseTimePicker(View v) {
        closeTimePicker.show(getSupportFragmentManager(), CLOSE_TIME_KEY);
    }

    public void resetCloseTime(View v) {
        setCloseTimeInput(-1);
        closeTimePicker = newCloseTimePicker(-1);
    }

    private void setupColorInput() {
        firstIconTypeInput = findViewById(R.id.first_icon_type_input);
        if (firstIconTypeInput != null) {
            firstIconTypeInput.setSelection(firstIconColorInput);
            firstIconTypeInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int newColorInput = NoteState.normalizeIconColorIndex(position);
                    if (firstIconColorInput != newColorInput) {
                        firstIconColorInput = newColorInput;
                        updateIconPreview();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    if (firstIconColorInput != NoteState.DEFAULT_ICON_COLOR_INDEX) {
                        firstIconColorInput = NoteState.DEFAULT_ICON_COLOR_INDEX;
                        updateIconPreview();
                    }
                }
            });
        }

        secondIconTypeInput = findViewById(R.id.second_icon_type_input);
        if (secondIconTypeInput != null) {
            secondIconTypeInput.setSelection(secondIconColorInput);
            secondIconTypeInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    int newColorInput = NoteState.normalizeIconColorIndex(position);
                    if (secondIconColorInput != newColorInput) {
                        secondIconColorInput = newColorInput;
                        updateIconPreview();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    if (secondIconColorInput != NoteState.DEFAULT_ICON_COLOR_INDEX) {
                        secondIconColorInput = NoteState.DEFAULT_ICON_COLOR_INDEX;
                        updateIconPreview();
                    }
                }
            });
        }
    }

    private void setupIconPreviews(Bundle savedState) {
        firstIconPreview = findViewById(R.id.first_icon);
        if (firstIconPreview != null) {
            firstIconDialog = new IconInputDialog.Builder<ServiceNoteFormActivity>(this, "first-icon")
                    .setTitle(R.string.select_upper_icon)
                    .loadState(this, firstIconPreview, savedState);
            firstIconPreview.setOnClickListener(this::showFirstIconDialog);
        }


        secondIconPreview = findViewById(R.id.second_icon);
        if (secondIconPreview != null) {
            secondIconDialog = new IconInputDialog.Builder<ServiceNoteFormActivity>(this, "second-icon")
                    .setTitle(R.string.select_lower_icon)
                    .loadState(this, secondIconPreview, savedState);
            secondIconPreview.setOnClickListener(this::showSecondIconDialog);
        }

        updateIconPreview();
    }

    private void updateIconPreview() {
        boolean hasIcon1 = false;
        boolean hasIcon2 = false;
        if (firstIconPreview != null) {
            Drawable drawable = NoteState.getDrawable(this, firstIconInput, firstIconColorInput);
            hasIcon1 = drawable != null;
            firstIconPreview.setVisibility(hasIcon1 ? View.VISIBLE : View.GONE);
            firstIconPreview.setImageDrawable(drawable);
            if (firstIconTypeInput != null)
                firstIconTypeInput.setEnabled(hasIcon1);
        }

        if (secondIconPreview != null) {
            Drawable drawable = NoteState.getDrawable(this, secondIconInput, secondIconColorInput);
            hasIcon2 = drawable != null;
            secondIconPreview.setVisibility(hasIcon2 ? View.VISIBLE : View.GONE);
            secondIconPreview.setImageDrawable(drawable);
            if (secondIconTypeInput != null)
                secondIconTypeInput.setEnabled(hasIcon2);
        }

        if (!hasIcon1 && hasIcon2) {
            ContextUtils.setSize(firstIconPreview, 0);
            ContextUtils.setSize(secondIconPreview, largeIconSize);
        } else if (hasIcon1 && !hasIcon2) {
            ContextUtils.setSize(firstIconPreview, largeIconSize);
            ContextUtils.setSize(secondIconPreview, 0);
        } else {
            ContextUtils.setSize(firstIconPreview, compactIconSize);
            ContextUtils.setSize(secondIconPreview, compactIconSize);
        }
    }

    public String getLastNoteEng() {
        return AppConfig.getInstance().get(StringSetting.NOTE_ENG);
    }

    public String getLastNoteFre() {
        return AppConfig.getInstance().get(StringSetting.NOTE_FRE);
    }

    public String getLastNoteArb() {
        return AppConfig.getInstance().get(StringSetting.NOTE_ARB);
    }

    @Override
    public void submit() {
        if (serviceID > Item.AUTO_ID)
            adminDashboardRepository.setNote(serviceID,
                    NoteState.from(firstIconInput, firstIconColorInput, secondIconInput, secondIconColorInput),
                    closeTimeInput,
                    StringUtil.getString(noteEngInput),
                    StringUtil.getString(noteFreInput),
                    StringUtil.getString(noteArbInput));
        else
            QueueUtils.handleServiceMissing();

        setResult(Activity.RESULT_OK);
        finish();
    }

    public void reset(View v) {
        if (serviceID > Item.AUTO_ID)
            adminDashboardRepository.setNote(serviceID, NoteState.DEFAULT_ICON_STATE, -1L, "", "", "");
        else
            QueueUtils.handleServiceMissing();

        setResult(Activity.RESULT_OK);
        finish();
    }

    public void back(View v) {
        cancelResultAndFinish();
    }

    public void showFirstIconDialog(View v) {
        DialogSupplier.showDialog(firstIconInput, firstIconDialog, newFirstIconListener());
    }

    private static InputListener<Integer, ServiceNoteFormActivity> newFirstIconListener() {
        return (context, view, input) -> {
            if (input != null) {
                context.firstIconInput = NoteState.normalizeIconIndex(input);
                context.updateIconPreview();
            }
        };
    }

    public void showSecondIconDialog(View v) {
        DialogSupplier.showDialog(secondIconInput, secondIconDialog, newSecondIconListener());
    }

    private static InputListener<Integer, ServiceNoteFormActivity> newSecondIconListener() {
        return (context, view, input) -> {
            if (input != null) {
                context.secondIconInput = NoteState.normalizeIconIndex(input);
                context.updateIconPreview();
            }
        };
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        DialogSupplier.saveDialog(firstIconDialog, outState);
        DialogSupplier.saveDialog(secondIconDialog, outState);
        outState.putInt(ICON_1_KEY, firstIconInput);
        outState.putInt(ICON_2_KEY, secondIconInput);
        outState.putInt(ICON_COLOR_1_KEY, firstIconColorInput);
        outState.putInt(ICON_COLOR_2_KEY, secondIconColorInput);
        outState.putLong(CLOSE_TIME_KEY, closeTimeInput);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        DialogSupplier.dismissDialog(firstIconDialog);
        DialogSupplier.dismissDialog(secondIconDialog);
        super.onDestroy();
    }

    public String getNamespace() {
        return "service_note_activity";
    }
}
