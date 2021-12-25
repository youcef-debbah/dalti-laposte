package com.dalti.laposte.core.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.Teller;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public class NoteState {

    public static final int[] COLORS = {
            R.color.brand_color,
            R.color.affirmative,
            R.color.warning,
            R.color.error,
    };

    // admin icons

    public static final int[] ICONS = {
            R.drawable.ic_null_24,
            R.drawable.ic_baseline_access_time_24,
            R.drawable.ic_baseline_accessible_24,
            R.drawable.ic_baseline_airline_seat_recline_normal_24,
            R.drawable.ic_baseline_alarm_24,
            R.drawable.ic_baseline_alarm_off_24,
            R.drawable.ic_baseline_announcement_24,
            R.drawable.ic_baseline_arrow_back_24,
            R.drawable.ic_baseline_assessment_24,
            R.drawable.ic_baseline_assignment_24,
            R.drawable.ic_baseline_assignment_late_24,
            R.drawable.ic_baseline_assignment_turned_in_24,
            R.drawable.ic_baseline_av_timer_24,
            R.drawable.ic_baseline_bar_chart_24,
            R.drawable.ic_baseline_block_24,
            R.drawable.ic_baseline_bug_report_24,
            R.drawable.ic_baseline_calendar_today_24,
            R.drawable.ic_baseline_campaign_24,
            R.drawable.ic_baseline_cancel_24,
            R.drawable.ic_baseline_cancel_schedule_send_24,
            R.drawable.ic_baseline_check_24,
            R.drawable.ic_baseline_check_circle_24,
            R.drawable.ic_baseline_confirmation_number_24,
            R.drawable.ic_baseline_construction_24,
            R.drawable.ic_baseline_date_range_24,
            R.drawable.ic_baseline_delete_24,
            R.drawable.ic_baseline_edit_24,
            R.drawable.ic_baseline_extension_24,
            R.drawable.ic_baseline_feedback_24,
            R.drawable.ic_baseline_filter_1_24,
            R.drawable.ic_baseline_filter_2_24,
            R.drawable.ic_baseline_filter_3_24,
            R.drawable.ic_baseline_filter_4_24,
            R.drawable.ic_baseline_filter_5_24,
            R.drawable.ic_baseline_filter_6_24,
            R.drawable.ic_baseline_filter_7_24,
            R.drawable.ic_baseline_filter_8_24,
            R.drawable.ic_baseline_filter_9_24,
            R.drawable.ic_baseline_filter_9_plus_24,
            R.drawable.ic_baseline_food_bank_24,
            R.drawable.ic_baseline_format_paint_24,
            R.drawable.ic_baseline_grade_24,
            R.drawable.ic_baseline_help_24,
            R.drawable.ic_baseline_history_24,
            R.drawable.ic_baseline_info_24,
            R.drawable.ic_baseline_list_24,
            R.drawable.ic_baseline_local_gas_station_24,
            R.drawable.ic_baseline_local_shipping_24,
            R.drawable.ic_baseline_loop_24,
            R.drawable.ic_baseline_map_24,
            R.drawable.ic_baseline_music_off_24,
            R.drawable.ic_baseline_notification_important_24,
            R.drawable.ic_baseline_notifications_24,
            R.drawable.ic_baseline_notifications_off_24,
            R.drawable.ic_baseline_pause_24,
            R.drawable.ic_baseline_pause_circle_filled_24,
            R.drawable.ic_baseline_pending_24,
            R.drawable.ic_baseline_pending_actions_24,
            R.drawable.ic_baseline_perm_data_setting_24,
            R.drawable.ic_baseline_perm_scan_wifi_24,
            R.drawable.ic_baseline_pest_control_24,
            R.drawable.ic_baseline_phone_24,
            R.drawable.ic_baseline_play_circle_outline_24,
            R.drawable.ic_baseline_priority_high_24,
            R.drawable.ic_baseline_public_24,
            R.drawable.ic_baseline_qr_code_24,
            R.drawable.ic_baseline_qr_code_scanner_24,
            R.drawable.ic_baseline_record_voice_over_24,
            R.drawable.ic_baseline_remove_circle_24,
            R.drawable.ic_baseline_report_24,
            R.drawable.ic_baseline_search_24,
            R.drawable.ic_baseline_search_off_24,
            R.drawable.ic_baseline_security_24,
            R.drawable.ic_baseline_send_24,
            R.drawable.ic_baseline_settings_24,
            R.drawable.ic_baseline_show_chart_24,
            R.drawable.ic_baseline_shuffle_24,
            R.drawable.ic_baseline_signal_cellular_alt_24,
            R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24,
            R.drawable.ic_baseline_signal_wifi_4_bar_lock_24,
            R.drawable.ic_baseline_signal_wifi_off_24,
            R.drawable.ic_baseline_sim_card_24,
            R.drawable.ic_baseline_smoke_free_24,
            R.drawable.ic_baseline_smoking_rooms_24,
            R.drawable.ic_baseline_sms_24,
            R.drawable.ic_baseline_sms_failed_24,
            R.drawable.ic_baseline_snooze_24,
            R.drawable.ic_baseline_speaker_24,
            R.drawable.ic_baseline_speed_24,
            R.drawable.ic_baseline_star_rate_24,
            R.drawable.ic_baseline_stop_circle_24,
            R.drawable.ic_baseline_support_agent_24,
            R.drawable.ic_baseline_sync_problem_24,
            R.drawable.ic_baseline_text_snippet_24,
            R.drawable.ic_baseline_thumb_down_alt_24,
            R.drawable.ic_baseline_thumb_up_alt_24,
            R.drawable.ic_baseline_thumbs_up_down_24,
            R.drawable.ic_baseline_timer_24,
            R.drawable.ic_baseline_timer_off_24,
            R.drawable.ic_baseline_trending_down_24,
            R.drawable.ic_baseline_trending_flat_24,
            R.drawable.ic_baseline_trending_up_24,
            R.drawable.ic_baseline_verified_24,
            R.drawable.ic_baseline_verified_user_24,
            R.drawable.ic_baseline_warning_24,
            R.drawable.ic_baseline_wheelchair_pickup_24,
            R.drawable.ic_baseline_wifi_24,
            R.drawable.ic_baseline_wifi_off_24,
    };

    public static final int DEFAULT_ICON_INDEX = 0;
    public static final int DEFAULT_ICON_COLOR_INDEX = 0;
    public static final int DEFAULT_ICON_STATE = from(DEFAULT_ICON_INDEX, DEFAULT_ICON_COLOR_INDEX, DEFAULT_ICON_INDEX, DEFAULT_ICON_COLOR_INDEX);
    public static final String DEFAULT_ICON_STATE_STRING = String.valueOf(DEFAULT_ICON_STATE);

    private static final int ICON_MASK = 0xF_FF;
    private static final int ICON_COLOR_MASK = 0xF;
    private static final int COLOR_OFFSET = 12;
    private static final int SECOND_OFFSET = 16;

    public static int getFirstIconIndex(int state) {
        return state & ICON_MASK;
    }

    public static int getFirstIconColorIndex(int state) {
        return (state >>> COLOR_OFFSET) & ICON_COLOR_MASK;
    }

    public static int getSecondIconIndex(int state) {
        return (state >>> SECOND_OFFSET) & ICON_MASK;
    }

    public static int getSecondIconColorIndex(int state) {
        return (state >>> (SECOND_OFFSET + COLOR_OFFSET)) & ICON_COLOR_MASK;
    }

    public static int setFirstIcon(int iconIndex, int state) {
        Assert.that(iconIndex == (iconIndex & ICON_MASK));
        return iconIndex | state;
    }

    public static int setFirstIconColor(int iconColorIndex, int state) {
        Assert.that(iconColorIndex == (iconColorIndex & ICON_COLOR_MASK));
        return (iconColorIndex << COLOR_OFFSET) | state;
    }

    public static int setSecondIcon(int iconIndex, int state) {
        Assert.that(iconIndex == (iconIndex & ICON_MASK));
        return (iconIndex << SECOND_OFFSET) | state;
    }

    public static int setSecondIconColor(int iconIndexColor, int state) {
        Assert.that(iconIndexColor == (iconIndexColor & ICON_COLOR_MASK));
        return (iconIndexColor << (SECOND_OFFSET + COLOR_OFFSET)) | state;
    }

    public static int from(int firstIconIndex, int secondIconIndex) {
        Assert.that(firstIconIndex == (firstIconIndex & ICON_MASK));
        Assert.that(secondIconIndex == (secondIconIndex & ICON_MASK));
        return firstIconIndex | (secondIconIndex << SECOND_OFFSET);
    }

    public static int from(int firstIconIndex, int firstIconColorIndex, int secondIconIndex, int secondIconColorIndex) {
        Assert.that(firstIconIndex == (firstIconIndex & ICON_MASK));
        Assert.that(secondIconIndex == (secondIconIndex & ICON_MASK));
        Assert.that(firstIconColorIndex == (firstIconColorIndex & ICON_COLOR_MASK));
        Assert.that(secondIconColorIndex == (secondIconColorIndex & ICON_COLOR_MASK));
        return firstIconIndex | (firstIconColorIndex << COLOR_OFFSET)
                | (secondIconIndex << SECOND_OFFSET) | (secondIconColorIndex << (SECOND_OFFSET + COLOR_OFFSET));
    }

    @DrawableRes
    public static Integer getFirstIconRes(int state) {
        int iconIndex = getFirstIconIndex(state);
        return iconIndex > DEFAULT_ICON_INDEX ? StringUtil.getInt(iconIndex, ICONS) : null;
    }

    @DrawableRes
    public static Integer getSecondIconRes(int state) {
        return StringUtil.getInt(getSecondIconIndex(state), ICONS);
    }

    @ColorRes
    public static int getFirstIconColorRes(int state) {
        Integer colorRes = StringUtil.getInt(getFirstIconColorIndex(state), COLORS);
        return colorRes != null ? colorRes : COLORS[DEFAULT_ICON_COLOR_INDEX];
    }

    @ColorRes
    public static int getSecondIconColorRes(int state) {
        Integer colorRes = StringUtil.getInt(getSecondIconColorIndex(state), COLORS);
        return colorRes != null ? colorRes : COLORS[DEFAULT_ICON_COLOR_INDEX];
    }

    public static int normalizeIconIndex(Integer input) {
        return input != null && input < ICONS.length && input > -1 ? input : DEFAULT_ICON_INDEX;
    }

    public static int normalizeIconColorIndex(Integer input) {
        return input != null && input < COLORS.length && input > -1 ? input : DEFAULT_ICON_COLOR_INDEX;
    }

    public static Drawable getDrawable(Context context, int iconIndex, int colorIndex) {
        Assert.nonNull(context);
        try {
            if (iconIndex != DEFAULT_ICON_INDEX) {
                Drawable drawable = AppCompatResources.getDrawable(context, ICONS[iconIndex]);
                if (drawable != null) {
                    Drawable compactDrawable = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTint(compactDrawable, context.getResources().getColor(COLORS[colorIndex]));
                    return compactDrawable;
                }
            }
        } catch (RuntimeException e) {
            Teller.error("could not inflate icon drawable: iconIndex=" + iconIndex + ", colorIndex=" + colorIndex, e);
        }

        return null;
    }
}
