package com.dalti.laposte.core.repositories;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.dalti.laposte.R;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.BindingAdapters;
import com.dalti.laposte.core.ui.NoteState;
import com.dalti.laposte.core.ui.VectorDrawableUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.queue.common.PostOfficeAvailability.BasicState;
import dz.jsoftware95.queue.common.PostOfficeAvailability.LiquidityState;
import dz.jsoftware95.queue.common.PostOfficeAvailability.ServiceState;
import dz.jsoftware95.silverbox.android.backend.DatabaseUtils;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

public class Selection {

    private final Service service;

    private final Map<Integer, MutableLiveData<Progress>> progresses;

    private final MediatorLiveData<Statistics> statistics = new MediatorLiveData<>();
    private final Observer<? super Progress> statisticsUpdater = progress -> {
        Statistics oldStatistics = statistics.getValue();
        if (oldStatistics != null)
            statistics.setValue(oldStatistics.update(progress));
        else {
            ProgressInfo progressInfo = new ProgressInfo(progress.getCurrentTokenInt(), progress.getWaitingInt());
            statistics.setValue(new Statistics(Collections.singletonMap(progress.getRank(), progressInfo)));
        }
    };

    private final Drawable thumbnails;

    private final LiveData<String> basicStateDescription;
    private final Drawable basicStateIcon;
    private final Drawable basicStateLogo;

    private final String secondaryStateDescription;
    private final Drawable secondaryStateIcon;
    private final Drawable secondaryStateLogo;

    private final String note;
    private final Drawable noteIcon1;
    private final Drawable noteIcon2;

    private final Map<Integer, List<Service.DayEvent>> schedule;

    private final Runnable timeUpdater;
    private final MutableLiveData<Long> timeBeforeClosing;
    private final LiveData<Integer> closingTimerVisibility;

    public Selection(final @NonNull ServiceProgress serviceProgress) {
        AbstractQueueApplication appContext = AbstractQueueApplication.requireInstance();
        Service service = Objects.requireNonNull(serviceProgress.getService());
        this.service = service;

        int progressCount = 1 + service.getExtra();
        Map<Integer, MutableLiveData<Progress>> progresses = new LinkedHashMap<>(progressCount);
        List<Progress> existingProgresses = ServiceProgress.getProgresses(serviceProgress);
        for (int i = 0; i < progressCount; i++) {
            long progressID = IdentityManager.getProgressID(service.getId(), i);
            Progress existingProgress = DatabaseUtils.findItem(progressID, existingProgresses);
            if (existingProgress != null)
                addProgress(progresses, i, existingProgress);
            else
                addProgress(progresses, i, new Progress(IdentityManager.getProgressID(service.getId(), i)));
        }

        this.progresses = Collections.unmodifiableMap(progresses);
        this.thumbnails = VectorDrawableUtil.getDrawable(appContext, service.getThumbnails());

        schedule = Service.parseSchedule(service.getScheduleData());

        Integer availability = service.getAvailability();
        switch (BasicState.value(availability)) {
            case BASIC_STATE_OPENED:
                basicStateDescription = new MutableLiveData<>(appContext.getString(R.string.service_open));
                basicStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_check_circle_24, R.attr.checkedIconOnForeground, R.color.black);
                basicStateLogo = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_logo_office_open_80_40);
                break;
            case BASIC_STATE_OVERLOADED:
                basicStateDescription = new MutableLiveData<>(appContext.getString(R.string.service_overloaded));
                basicStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_warning_24, R.attr.warningIconOnForeground, R.color.black);
                basicStateLogo = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_logo_office_halfclose_80_40);
                break;
            case BASIC_STATE_CLOSED:
                basicStateDescription = getClosedStateLiveDescription(appContext);
                basicStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_cancel_24, R.attr.errorIconOnForeground, R.color.black);
                basicStateLogo = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_logo_office_close_80_40);
                break;
            default:
                basicStateDescription = new MutableLiveData<>(appContext.getString(R.string.unknown_service_state));
                basicStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_help_24, BindingAdapters.DEFAULT_TINT_COLOR);
                basicStateLogo = null;
        }

        int withdrawalCap = PostOfficeAvailability.getMaxWithdrawal(availability);
        switch (LiquidityState.valueIfNotClosed(availability)) {
            case LIQUIDITY_STATE_AVAILABLE:
                secondaryStateDescription = withdrawalCap > 0 ?
                        appContext.getString(R.string.liquidity_available_with_cap, withdrawalCap) :
                        appContext.getString(R.string.liquidity_available);
                secondaryStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_check_circle_24, R.attr.checkedIconOnForeground, R.color.black);
                secondaryStateLogo = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_logo_big_money_stack_80_40);
                break;
            case LIQUIDITY_STATE_LIMITED:
                secondaryStateDescription = withdrawalCap > 0 ?
                        appContext.getString(R.string.liquidity_limited_with_cap, withdrawalCap) :
                        appContext.getString(R.string.liquidity_limited);
                secondaryStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_warning_24, R.attr.warningIconOnForeground, R.color.black);
                secondaryStateLogo = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_logo_small_money_stack_80_40);
                break;
            case LIQUIDITY_STATE_DEPLETED:
                secondaryStateDescription = withdrawalCap > 0 ?
                        appContext.getString(R.string.liquidity_depleted_with_cap, withdrawalCap) :
                        appContext.getString(R.string.liquidity_depleted);
                secondaryStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_cancel_24, R.attr.errorIconOnForeground, R.color.black);
                secondaryStateLogo = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_logo_no_money_80_40);
                break;
            default:
                secondaryStateLogo = null;
                switch (ServiceState.value(availability)) {
                    case SERVICE_STATE_COMING_SOON:
                        secondaryStateDescription = appContext.getString(R.string.service_coming_soon);
                        secondaryStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_construction_24, R.attr.colorSecondary, R.color.black);
                        break;
                    case SERVICE_STATE_STOPPED_TEMPERATELY:
                        secondaryStateDescription = appContext.getString(R.string.service_paused);
                        secondaryStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_pause_circle_filled_24, R.attr.warningIconOnForeground, R.color.black);
                        break;
                    case SERVICE_STATE_STOPPED_PERMANENTLY:
                        secondaryStateDescription = appContext.getString(R.string.service_stopped);
                        secondaryStateIcon = VectorDrawableUtil.getDrawable(appContext, R.drawable.ic_baseline_stop_circle_24, R.attr.errorIconOnForeground, R.color.black);
                        break;
                    default:
                        secondaryStateDescription = "";
                        secondaryStateIcon = null;
                }
        }

        if (PostOfficeAvailability.isResettable(availability)) {
            Long closeTime = service.getCurrentCloseTime();
            timeBeforeClosing = new MutableLiveData<>(calcTimeBeforeClosing(schedule, closeTime));
            timeUpdater = newTimeUpdater(schedule, closeTime, timeBeforeClosing);
            appContext.addRoutine(timeUpdater);
        } else {
            timeBeforeClosing = new MutableLiveData<>(null);
            timeUpdater = null;
        }

        closingTimerVisibility = Transformations.map(timeBeforeClosing,
                time -> time != null ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE);

        int noteState = service.getNoteStateInt();
        noteIcon1 = NoteState.getDrawable(appContext, NoteState.getFirstIconIndex(noteState), NoteState.getFirstIconColorIndex(noteState));
        noteIcon2 = NoteState.getDrawable(appContext, NoteState.getSecondIconIndex(noteState), NoteState.getSecondIconColorIndex(noteState));
        note = service.getNote();
    }

    private static Runnable newTimeUpdater(Map<Integer, List<Service.DayEvent>> schedule,
                                           Long closingTime,
                                           MutableLiveData<Long> timeBeforeClosing) {
        return () -> timeBeforeClosing.postValue(calcTimeBeforeClosing(schedule, closingTime));

    }

    private static Long calcTimeBeforeClosing(Map<Integer, List<Service.DayEvent>> schedule, Long closingTime) {
        Calendar today = Calendar.getInstance();
        long now = today.getTimeInMillis();

        if (closingTime != null && closingTime > now) {
            return closingTime - now;
        } else {
            Long nextCloseTime = Service.DayEvent.getNextCloseEvent(today, schedule);
            if (nextCloseTime != null && nextCloseTime > now)
                return nextCloseTime - now;
            else
                return null;
        }
    }

    public Long getLastCloseEvent() {
        return Service.DayEvent.getLastCloseEvent(Calendar.getInstance(), schedule);
    }

    public LiveData<Long> getTimeBeforeClosing() {
        return timeBeforeClosing;
    }

    public LiveData<Integer> getClosingTimerVisibility() {
        return closingTimerVisibility;
    }

    private LiveData<String> getClosedStateLiveDescription(Context context) {
        String serviceClosed = context.getString(R.string.service_closed);
        String serviceOpenAt = context.getString(R.string.service_closed_next_open_at);
        String serviceOpenSoon = context.getString(R.string.service_will_open_soon);
        MutableLiveData<String> data = new MutableLiveData<>(serviceClosed);
        setServiceClosedDescription(data, serviceClosed, serviceOpenAt, serviceOpenSoon);
        return data;
    }

    private void setServiceClosedDescription(MutableLiveData<String> data, String serviceClosed,
                                             String serviceOpenAt, String serviceOpenSoon) {
        Calendar today = Calendar.getInstance();
        long now = today.getTimeInMillis();

        Long nexOpenTime = Service.DayEvent.getNextOpenEvent(today, schedule);
        if (nexOpenTime == null)
            data.postValue(serviceClosed);
        else {
            long tooLateToOpenDelay = nexOpenTime - now + Service.DayEvent.MAX_DELAY;
            if (now < nexOpenTime) {
                String openTime = TimeUtils.formatAsShortTime(nexOpenTime);
                data.postValue(String.format(serviceOpenAt, openTime));
                AppWorker.BACKGROUND.executeDelayed(() -> {
                    data.postValue(serviceOpenSoon);
                    AppWorker.BACKGROUND.executeDelayed(() -> setServiceClosedDescription(data, serviceClosed, serviceOpenAt, serviceOpenSoon), tooLateToOpenDelay);
                }, nexOpenTime - now);
            } else {
                data.postValue(serviceOpenSoon);
                AppWorker.BACKGROUND.executeDelayed(() -> setServiceClosedDescription(data, serviceClosed, serviceOpenAt, serviceOpenSoon), tooLateToOpenDelay);
            }
        }
    }

    private void addProgress(Map<Integer, MutableLiveData<Progress>> progresses,
                             int i, Progress progress) {
        MutableLiveData<Progress> liveData = new MutableLiveData<>(progress);
        progresses.put(i, liveData);
        statistics.addSource(liveData, statisticsUpdater);
    }

    @NonNull
    public Service getService() {
        return service;
    }

    public Drawable getThumbnails() {
        return thumbnails;
    }

    public Map<Integer, MutableLiveData<Progress>> getProgresses() {
        return progresses;
    }

    public Progress getProgressValue(int rank) {
        MutableLiveData<Progress> liveData = getProgress(rank);
        if (liveData != null)
            return liveData.getValue();
        return null;
    }

    public MutableLiveData<Progress> getProgress(int rank) {
        return progresses.get(rank);
    }

    public long getProgressID(int rank) {
        return IdentityManager.getProgressID(getService().getId(), rank);
    }

    public LiveData<String> getBasicStateDescription() {
        return basicStateDescription;
    }

    public Drawable getBasicStateIcon() {
        return basicStateIcon;
    }

    public Drawable getBasicStateLogo() {
        return basicStateLogo;
    }

    public String getSecondaryStateDescription() {
        return secondaryStateDescription;
    }

    public Drawable getSecondaryStateIcon() {
        return secondaryStateIcon;
    }

    public Drawable getSecondaryStateLogo() {
        return secondaryStateLogo;
    }

    public Integer getHasBasicState() {
        return (service != null && basicStateIcon != null) ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public Integer getHasSecondaryState() {
        return (isKnown() && secondaryStateIcon != null) ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public Integer getHasNote() {
        return (isKnown() && note != null) ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public boolean isResettable() {
        return service != null && PostOfficeAvailability.isResettable(service.getAvailability());
    }

    public boolean isKnown() {
        return service != null && PostOfficeAvailability.isKnown(service.getAvailability());
    }

    public Integer getUnknownUIVisibility() {
        return isKnown() ? ContextUtils.VIEW_GONE : ContextUtils.VIEW_VISIBLE;
    }

    public Integer getKnownUIVisibility() {
        return isKnown() ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
    }

    public Integer getPrimaryColor() {
        return isKnown() ? R.color.primary_color_selector : null;
    }

    public Integer getSecondaryColor() {
        return isKnown() ? R.color.secondary_color_selector : null;
    }

    public String getNote() {
        return note;
    }

    public Drawable getNoteIcon1() {
        return noteIcon1;
    }

    public Drawable getNoteIcon2() {
        return noteIcon2;
    }

    public static LiveData<Statistics> getStatistics(Selection selection) {
        return selection != null ? selection.statistics : null;
    }

    public Map<Integer, List<Service.DayEvent>> getSchedule() {
        return schedule;
    }

    public Runnable getTimeUpdater() {
        return timeUpdater;
    }

    @Override
    @NotNull
    public String toString() {
        return String.valueOf(service);
    }

    public static boolean hasService(Selection selection) {
        return selection != null && selection.service != null;
    }

    private static final class ProgressInfo {
        Integer token;
        Integer waiting;

        ProgressInfo(Integer token, Integer waiting) {
            this.token = token;
            this.waiting = waiting;
        }

        @Override
        public String toString() {
            return "ProgressInfo{" +
                    "token=" + token +
                    ", waiting=" + waiting +
                    '}';
        }
    }

    public static final class Statistics {

        private final int currentToken;
        private final int latestToken;
        private final Map<Integer, ProgressInfo> data;

        public Statistics(@NonNull final Map<Integer, ProgressInfo> data) {
            int totalTokens = 0;
            int totalWaiting = 0;

            for (ProgressInfo info : data.values()) {
                totalTokens += info.token;
                totalWaiting += info.waiting;
            }

            this.currentToken = totalTokens;
            this.latestToken = totalTokens + totalWaiting;
            this.data = data;
        }

        public int getCurrentToken() {
            return currentToken;
        }

        public int getLatestToken() {
            return latestToken;
        }

        Statistics update(Progress progress) {
            HashMap<Integer, ProgressInfo> newData = new HashMap<>(data);
            newData.put(progress.getRank(), new ProgressInfo(progress.getCurrentTokenInt(), progress.getWaitingInt()));
            return new Statistics(newData);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Statistics that = (Statistics) o;
            return currentToken == that.currentToken &&
                    latestToken == that.latestToken;
        }

        @Override
        public int hashCode() {
            return Objects.hash(currentToken, latestToken);
        }

        @Override
        public String toString() {
            return "Statistics{" +
                    "currentToken=" + currentToken +
                    ", latestToken=" + latestToken +
                    '}';
        }
    }
}
