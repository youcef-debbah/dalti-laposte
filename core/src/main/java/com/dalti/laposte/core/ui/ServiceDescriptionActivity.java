package com.dalti.laposte.core.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;

import com.dalti.laposte.BR;
import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.LoadedService;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.repositories.ServicesListRepository;
import com.dalti.laposte.core.repositories.ServicesRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import dz.jsoftware95.silverbox.android.middleware.BindingUtil;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

@AndroidEntryPoint
public class ServiceDescriptionActivity extends AbstractQueueActivity {

    private LiveData<LoadedService> service;
    private ServicesRepository servicesRepository;
    private ServicesListRepository servicesListRepository;
    private QueueActivitySupport activitySupport;

    @Inject
    public void setup(ServicesRepository servicesRepository,
                      ServicesListRepository servicesListRepository,
                      QueueActivitySupport activitySupport) {
        this.servicesRepository = servicesRepository;
        this.servicesListRepository = servicesListRepository;
        this.activitySupport = activitySupport;
    }

    @Override
    protected Integer getActionBarID() {
        return R.id.action_bar;
    }

    @Override
    protected void setupLayout(Bundle savedState) {
        ViewDataBinding binding = BindingUtil.setContentView(this, R.layout.activity_service_description);
        binding.setVariable(BR.activity, this);

        NestedScrollView scroll = binding.getRoot().findViewById(R.id.main_nested_scroll_view);
        if (scroll != null) {
            int margin = getResources().getDimensionPixelSize(R.dimen.large) * 5;
            scroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (scroll.getHeight() > 0) {
                        logScheduleVisibility(scroll, margin);
                        scroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    private void logScheduleVisibility(NestedScrollView scroll, int margin) {
        View scrollContent = scroll.getChildAt(0);
        if (scrollContent != null) {
            long id = getLongExtra(Service.ID, -1);
            if (scrollContent.getBottom() < scroll.getHeight()) {
                onScheduleVisible(id, "spontaneous");
            } else {
                NestedScrollView.OnScrollChangeListener scrollListener = (scrollview, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    View content = scrollview.getChildAt(0);
                    if (content != null) {
                        if (content.getBottom() < (scroll.getHeight() + scrollY + margin)) {
                            onScheduleVisible(id, "intentional");
                            scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) null);
                        }
                    }
                };
                scroll.setOnScrollChangeListener(scrollListener);
            }
        }
    }

    private static void onScheduleVisible(long id, String type) {
        Teller.logViewItemEvent(Teller.newItem(id, Service.TABLE_NAME + "_schedule", type));
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        service = servicesRepository.getLoadedData(getLongExtra(Service.ID, -1));

        TableLayout scheduleTable = findViewById(R.id.service_schedule_table);
        if (scheduleTable != null)
            service.observe(this, new ScheduleBuilder(this, scheduleTable));
    }

    public LiveData<LoadedService> getService() {
        return service;
    }

    public void openMap(View v) {
        long id = getLongExtra(Service.ID, -1);
        Intent data = new Intent(this, MapActivity.class);
        data.putExtra(Service.ID, id);
        startActivity(data);
        Teller.logViewItemEvent(Teller.newItem(id, Service.TABLE_NAME + "_map"));
    }

    public void selectService(View v) {
        long serviceID = getLongExtra(Service.ID, -1);
        servicesListRepository.setCurrentService(serviceID);
        Teller.logSelectContentEvent(String.valueOf(serviceID), Service.TABLE_NAME);
        openActivity(activitySupport.getMainActivity());
    }

    private static final class ScheduleBuilder extends UnMainObserver<TableLayout, LoadedService> {

        private final int padding;
        private final String[] days;

        public ScheduleBuilder(@NonNull BasicActivity activity,
                               @NonNull TableLayout scheduleTable) {
            super(activity, scheduleTable);
            padding = activity.getResources().getDimensionPixelOffset(R.dimen.tiny);
            days = activity.getResources().getStringArray(R.array.day_names);
        }

        @Override
        protected void onUpdate(@NonNull TableLayout scheduleTable, @Nullable LoadedService service) {
            scheduleTable.setColumnStretchable(1, true);
            scheduleTable.removeAllViews();
            if (service != null) {
                Map<Integer, List<Service.DayEvent>> schedule = service.getSchedule();
                for (Map.Entry<Integer, List<Service.DayEvent>> daySchedule : schedule.entrySet())
                    addDayScheduleView(scheduleTable, days[daySchedule.getKey()], daySchedule.getValue(), padding);
            } else {
                MaterialTextView emptyNote = QueueUtils.newBody1Text(scheduleTable.getContext(), null);
                emptyNote.setPadding(padding, padding, padding, padding);
                emptyNote.setGravity(Gravity.CENTER);
                scheduleTable.addView(emptyNote);
            }
        }

        private static void addDayScheduleView(TableLayout scheduleTable,
                                               String day, List<Service.DayEvent> events, int padding) {
            Context context = scheduleTable.getContext();
            TableRow currentDay = new TableRow(context);
            currentDay.setPadding(0, padding, 0, padding);
            scheduleTable.addView(currentDay);
            MaterialTextView dayName = QueueUtils.newSubtitle1Text(context, day);
            currentDay.addView(dayName);

            ViewGroup lines = ContextUtils.newVerticalLayout(context);
            lines.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            currentDay.addView(lines);

            ViewGroup currentLine = null;
            for (Service.DayEvent event : events) {
                if (event == null) {
                    scheduleTable.addView(new MaterialDivider(context));
                    currentLine = null;
                } else {
                    if (currentLine == null) {
                        currentLine = ContextUtils.newHorizontalLayout(context);
                        lines.addView(currentLine);
                    }

                    currentLine.addView(QueueUtils.newBody1Text(context, event.getLabel()));
                    MaterialTextView time = QueueUtils.newSubtitle2Text(context, event.getTime());
                    time.setPadding(padding, 0, padding, 0);
                    currentLine.addView(time);

                    if (event.isCloseEvent())
                        currentLine = null;
                }
            }
        }
    }

    public String getNamespace() {
        return "service_description_activity";
    }
}
