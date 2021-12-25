package com.dalti.laposte.core.repositories;


import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dalti.laposte.R;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.NoteState;
import com.dalti.laposte.core.util.QueueUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.PostOfficeAvailability;
import dz.jsoftware95.queue.common.Scheduler;
import dz.jsoftware95.queue.common.Wilaya;
import dz.jsoftware95.silverbox.android.backend.VisualItem;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@Entity(tableName = Service.TABLE_NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Service implements VisualItem, Serializable {
    public static final int PAGE_SIZE = 32;
    public static final String TABLE_NAME = "service";
    public static final String ID = "service_id";

    public static final int[] DAYS = {
            Calendar.SATURDAY,
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
    };

    @PrimaryKey
    @ColumnInfo(name = ID)
    private long id;

    private int wilaya;

    private int extra;

    private Integer availability;

    private String addressEng;

    private String addressFre;

    private String addressArb;

    private String nameEng;

    private String nameFre;

    private String nameArb;

    private String descriptionEng;

    private String descriptionFre;

    private String descriptionArb;

    private String noteEng;

    private String noteFre;

    private String noteArb;

    private Integer noteState;

    private String map;

    private int postalCode;

    private String scheduleData;

    private boolean unknown;

    private Long closeTime;

    public Service() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWilaya() {
        return wilaya;
    }

    public void setWilaya(int wilaya) {
        this.wilaya = wilaya;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public Integer getAvailability() {
        return availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public String getAddressEng() {
        return addressEng;
    }

    public void setAddressEng(String addressEng) {
        this.addressEng = addressEng;
    }

    public String getAddressFre() {
        return addressFre;
    }

    public void setAddressFre(String addressFre) {
        this.addressFre = addressFre;
    }

    public String getAddressArb() {
        return addressArb;
    }

    public void setAddressArb(String addressArb) {
        this.addressArb = addressArb;
    }

    public String getNameEng() {
        return nameEng;
    }

    public void setNameEng(String nameEng) {
        this.nameEng = nameEng;
    }

    public String getNameFre() {
        return nameFre;
    }

    public void setNameFre(String nameFre) {
        this.nameFre = nameFre;
    }

    public String getNameArb() {
        return nameArb;
    }

    public void setNameArb(String nameArb) {
        this.nameArb = nameArb;
    }

    public String getDescriptionEng() {
        return descriptionEng;
    }

    public void setDescriptionEng(String descriptionEng) {
        this.descriptionEng = descriptionEng;
    }

    public String getDescriptionFre() {
        return descriptionFre;
    }

    public void setDescriptionFre(String descriptionFre) {
        this.descriptionFre = descriptionFre;
    }

    public String getDescriptionArb() {
        return descriptionArb;
    }

    public void setDescriptionArb(String descriptionArb) {
        this.descriptionArb = descriptionArb;
    }

    public String getNoteEng() {
        return noteEng;
    }

    public void setNoteEng(String noteEng) {
        this.noteEng = noteEng;
    }

    public String getNoteFre() {
        return noteFre;
    }

    public void setNoteFre(String noteFre) {
        this.noteFre = noteFre;
    }

    public String getNoteArb() {
        return noteArb;
    }

    public void setNoteArb(String noteArb) {
        this.noteArb = noteArb;
    }

    public Integer getNoteState() {
        return noteState;
    }

    public int getNoteStateInt() {
        return noteState != null ? noteState : NoteState.DEFAULT_ICON_COLOR_INDEX;
    }

    public void setNoteState(Integer noteState) {
        this.noteState = noteState;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    public String getScheduleData() {
        return scheduleData;
    }

    public void setScheduleData(String scheduleData) {
        this.scheduleData = scheduleData;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public void setUnknown(boolean unknown) {
        this.unknown = unknown;
    }

    public void setCloseTime(Long closeTime) {
        this.closeTime = closeTime;
    }

    public Long getCloseTime() {
        return closeTime;
    }

    public Long getCurrentCloseTime() {
        if (closeTime != null && closeTime > 0) {
            long today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
            long closeDate = TimeUnit.MILLISECONDS.toDays(closeTime);
            long timeout = AppConfig.getInstance().getRemoteLong(LongSetting.CLOSE_TIME_TIMEOUT_IN_DAYS);
            if (today - closeDate <= timeout)
                return closeTime;
        }

        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void update(AdminAction action) {
        action.apply(this);
        Check.isValid(this);
    }

    public boolean update(Map<String, String> data) {
        boolean updated = false;

        Integer newAvailability = StringUtil.parseInteger(data.get(GlobalConf.AVAILABILITY_KEY));
        if (!Objects.equals(newAvailability, availability)) {
            setAvailability(newAvailability);
            updated = true;
        }

        Integer newNoteState = StringUtil.parseInteger(data.get(GlobalConf.NOTE_STATE_KEY));
        if (!Objects.equals(newNoteState, noteState)) {
            setNoteState(newNoteState);
            updated = true;
        }

        Long newCloseTime = StringUtil.parseLong(data.get(GlobalConf.CLOSE_TIME_KEY));
        if (!Objects.equals(newCloseTime, closeTime)) {
            setCloseTime(newCloseTime);
            updated = true;
        }

        String newNoteEng = normalizeNote(data.get(GlobalConf.NOTE_ENG_KEY));
        if (!Objects.equals(newNoteEng, noteEng)) {
            setNoteEng(newNoteEng);
            updated = true;
        }

        String newNoteFre = normalizeNote(data.get(GlobalConf.NOTE_FRE_KEY));
        if (!Objects.equals(newNoteFre, noteFre)) {
            setNoteFre(newNoteFre);
            updated = true;
        }

        String newNoteArb = normalizeNote(data.get(GlobalConf.NOTE_ARB_KEY));
        if (!Objects.equals(newNoteArb, noteArb)) {
            setNoteArb(newNoteArb);
            updated = true;
        }

        return updated;
    }

    private String normalizeNote(String rawNote) {
        if (rawNote == null || GlobalConf.NULL_NOTE.equals(rawNote))
            return null;
        else {
            String note = rawNote;
            if (note.length() > GlobalConf.MAX_TEXT_PARAM_LENGTH)
                note = rawNote.substring(0, GlobalConf.MAX_TEXT_PARAM_LENGTH);
            return note;
        }
    }

    @Override
    public boolean equals(Object other) {
        ensurePersisted();
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        Service otherItem = (Service) other;

        return id == otherItem.id;
    }

    @Override
    public int hashCode() {
        return idHashcode();
    }

    @Override
    public @NotNull String toString() {
        return "Service{" +
                "id=" + id +
                ", " + PostOfficeAvailability.from(availability) +
                ", extra=" + extra +
                '}';
    }

    @Override
    public boolean areContentsTheSame(@NotNull VisualItem o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return id == service.id &&
                wilaya == service.wilaya &&
                extra == service.extra &&
                postalCode == service.postalCode &&
                Objects.equals(availability, service.availability) &&
                Objects.equals(addressEng, service.addressEng) &&
                Objects.equals(addressFre, service.addressFre) &&
                Objects.equals(addressArb, service.addressArb) &&
                Objects.equals(nameEng, service.nameEng) &&
                Objects.equals(nameFre, service.nameFre) &&
                Objects.equals(nameArb, service.nameArb) &&
                Objects.equals(descriptionEng, service.descriptionEng) &&
                Objects.equals(descriptionFre, service.descriptionFre) &&
                Objects.equals(descriptionArb, service.descriptionArb) &&
                Objects.equals(noteEng, service.noteEng) &&
                Objects.equals(noteFre, service.noteFre) &&
                Objects.equals(noteArb, service.noteArb) &&
                Objects.equals(noteState, service.noteState) &&
                Objects.equals(closeTime, service.closeTime) &&
                Objects.equals(map, service.map) &&
                Objects.equals(scheduleData, service.scheduleData);
    }

    public String getName() {
        return QueueUtils.getString(nameEng, nameFre, nameArb);
    }

    public String getDescription() {
        return QueueUtils.getString(descriptionEng, descriptionFre, descriptionArb);
    }

    public String getAddress() {
        return QueueUtils.getString(addressEng, addressFre, addressArb);
    }

    public String getNote() {
        return QueueUtils.getString(noteEng, noteFre, noteArb);
    }

    public String getWilayaName() {
        Wilaya wilaya = Wilaya.fromCode(this.wilaya);
        if (wilaya != null)
            return QueueUtils.getString(wilaya.getNameEng(), wilaya.getNameFre(), wilaya.getNameArb());
        else
            return QueueUtils.getString(R.string.unknown_symbol);
    }

    @DrawableRes
    public int getThumbnails() {
        return R.drawable.ic_logo_eccp_80;
    }

    public static Map<Integer, List<DayEvent>> parseSchedule(String scheduleData) {
        Map<Integer, List<Integer>> data = Scheduler.from(scheduleData);
        Map<Integer, List<DayEvent>> schedule = new LinkedHashMap<>(data.size());
        Context context = AbstractQueueApplication.requireInstance();
        for (int day : DAYS) {
            List<Integer> dayData = data.get(day);
            if (dayData != null)
                schedule.put(day, getDayEvents(context, dayData));
        }
        return schedule;
    }

    private static List<DayEvent> getDayEvents(Context context, List<Integer> openCloseTimes) {
        ArrayList<DayEvent> dayEvents = new ArrayList<>(openCloseTimes.size());

        for (Integer time : openCloseTimes)
            if (time != Scheduler.RESET)
                dayEvents.add(new OpenCloseEvent(context, time));
            else
                dayEvents.add(null);

        return dayEvents;
    }

    public interface DayEvent {

        long MAX_DELAY = TimeUnit.MINUTES.toMillis(1);

        int getDayTime();

        String getLabel();

        String getTime();

        boolean isCloseEvent();

        boolean isOpenEvent();

        static Long getLastCloseEvent(Calendar day, Map<Integer, List<DayEvent>> schedule) {
            if (day != null && schedule != null) {
                List<DayEvent> dayEvents = schedule.get(day.get(Calendar.DAY_OF_WEEK));
                if (dayEvents != null && !dayEvents.isEmpty())
                    for (int i = dayEvents.size() - 1; i >= 0; i--) {
                        DayEvent event = dayEvents.get(i);
                        if (event != null && event.isCloseEvent())
                            return DayEvent.setScheduleDayTime(day, event.getDayTime());
                    }
            }
            return null;
        }

        static long setScheduleDayTime(Calendar calendar, int dayTime) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Scheduler.TIME_UNITE, dayTime);
            return calendar.getTimeInMillis();
        }

        static Long getNextCloseEvent(Calendar day, Map<Integer, List<DayEvent>> schedule) {
            return getNextEventTime(day, schedule, true);
        }

        static Long getNextOpenEvent(Calendar day, Map<Integer, List<DayEvent>> schedule) {
            return getNextEventTime(day, schedule, false);
        }

        static Long getNextEventTime(Calendar day, Map<Integer, List<DayEvent>> schedule, boolean isCloseEvent) {
            if (schedule != null) {
                List<DayEvent> dayEvents = schedule.get(day.get(Calendar.DAY_OF_WEEK));
                if (dayEvents != null) {
                    long minEventTime = day.getTimeInMillis() - MAX_DELAY;
                    for (DayEvent event : dayEvents)
                        if (event != null && isCloseEvent == event.isCloseEvent()) {
                            long eventTimeToday = DayEvent.setScheduleDayTime(day, event.getDayTime());
                            if (eventTimeToday > minEventTime)
                                return eventTimeToday;
                        }
                }
            }

            return null;
        }
    }

    private static final class OpenCloseEvent implements DayEvent {

        private final int dayTime;
        final String time;
        final String label;
        final boolean close;

        OpenCloseEvent(Context context, int time) {
            this.dayTime = Math.abs(time);
            this.time = TimeUtils.formatAsShortTime(DayEvent.setScheduleDayTime(Calendar.getInstance(), this.dayTime));
            if (time > 0) {
                label = context.getString(R.string.open_time_label);
                close = false;
            } else {
                label = context.getString(R.string.close_time_label);
                close = true;
            }
        }

        @Override
        public int getDayTime() {
            return dayTime;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getTime() {
            return time;
        }

        @Override
        public boolean isCloseEvent() {
            return close;
        }

        @Override
        public boolean isOpenEvent() {
            return !close;
        }
    }
}
