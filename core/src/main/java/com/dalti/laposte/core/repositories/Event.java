package com.dalti.laposte.core.repositories;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Represent a GA4 <a href="https://developers.google.com/analytics/devguides/collection/ga4/reference/events">event</a>
 * <p>
 * consider for future use:
 * <p>
 * <ul>
 *     <li>search</li>
 *     <li>tutorial_begin</li>
 *     <li>tutorial_complete</li>
 *     <li>select_promotion</li>
 *     <li>view_promotion</li>
 * </ul>
 */
public abstract class Event {

    // common values

    public interface Trigger {
        String CLICK_CONTROLLER = "click_controller";
        String CLEAR_NOTIFICATION = "clear_notification";
        String CLICK_NOTIFICATION_ACTION = "click_notification_action";
    }

    // common types

    public interface ItemParam {
        /**
         * The ID of the item.
         * <p>
         * Name: item_id
         * <p>
         * Type: string
         * <p>
         * Required: yes (unless item_name is set)
         * <p>
         * Example: SKU_12345
         */
        String ITEM_ID = FirebaseAnalytics.Param.ITEM_ID;

        /**
         * The name of the item.
         * <p>
         * Name: item_name
         * <p>
         * Type: string
         * <p>
         * Required: yes (unless item_id is set)
         * <p>
         * Example: Black Coat
         */
        String ITEM_NAME = FirebaseAnalytics.Param.ITEM_NAME;

        /**
         * A product affiliation to designate a supplying company or brick and mortar store location.
         * <p>
         * Event-level and item-level affiliation parameters are independent.
         * <p>
         * Name: affiliation
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Google Store
         */
        String AFFILIATION = FirebaseAnalytics.Param.AFFILIATION;

        /**
         * The coupon name/code associated with the event.
         * <p>
         * Event-level and item-level coupon parameters are independent.
         * <p>
         * Name: coupon
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: SUMMER_FUN
         */
        String COUPON = FirebaseAnalytics.Param.COUPON;

        /**
         * The currency, in 3-letter ISO 4217 format.
         * <p>
         * If set, event-level currency is ignored.
         * <p>
         * Multiple currencies per event is not supported. Each item should set the same currency.
         * <p>
         * Name: currency
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: USD
         */
        String CURRENCY = FirebaseAnalytics.Param.CURRENCY;

        /**
         * The monetary discount value associated with the item.
         * <p>
         * Name: discount
         * <p>
         * Type: number
         * <p>
         * Required: no
         * <p>
         * Example: 1.25
         */
        String DISCOUNT = FirebaseAnalytics.Param.DISCOUNT;

        /**
         * The index/position of the item in a list.
         * <p>
         * Name: index
         * <p>
         * Type: number
         * <p>
         * Required: no
         * <p>
         * Example: 5
         */
        String INDEX = FirebaseAnalytics.Param.INDEX;

        /**
         * The brand of the item.
         * <p>
         * Name: item_brand
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Google
         */
        String ITEM_BRAND = FirebaseAnalytics.Param.ITEM_BRAND;

        /**
         * The category of the item. If used as part of a category hierarchy or taxonomy
         * then this will be the first category.
         * <p>
         * Name: item_category
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Apparel
         */
        String ITEM_CATEGORY = FirebaseAnalytics.Param.ITEM_CATEGORY;

        /**
         * The second category hierarchy or additional taxonomy for the item.
         * <p>
         * Name: item_category2
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Adult
         */
        String ITEM_CATEGORY_2 = FirebaseAnalytics.Param.ITEM_CATEGORY2;

        /**
         * The third category hierarchy or additional taxonomy for the item.
         * <p>
         * Name: item_category3
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Crew
         */
        String ITEM_CATEGORY_3 = FirebaseAnalytics.Param.ITEM_CATEGORY3;

        /**
         * /The fourth category hierarchy or additional taxonomy for the item.
         * <p>
         * Name: item_category4
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Crew
         */
        String ITEM_CATEGORY_4 = FirebaseAnalytics.Param.ITEM_CATEGORY4;

        /**
         * The fifth category hierarchy or additional taxonomy for the item.
         * <p>
         * Name: item_category5
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Short sleeve
         */
        String ITEM_CATEGORY_5 = FirebaseAnalytics.Param.ITEM_CATEGORY5;

        /**
         * The ID of the list in which the item was presented to the user.
         * <p>
         * If set, event-level item_list_id is ignored.
         * If not set, event-level item_list_id is used, if present.
         * <p>
         * Name: item_list_id
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: related_products
         */
        String ITEM_LIST_ID = FirebaseAnalytics.Param.ITEM_LIST_ID;

        /**
         * The name of the list in which the item was presented to the user.
         * <p>
         * If set, event-level item_list_name is ignored.
         * If not set, event-level item_list_name is used, if present.
         * <p>
         * Name: item_list_name
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: Related products
         */
        String ITEM_LIST_NAME = FirebaseAnalytics.Param.ITEM_LIST_NAME;

        /**
         * The item variant or unique code or description for additional item details/options.
         * <p>
         * Name: item_variant
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: green
         */
        String ITEM_VARIANT = FirebaseAnalytics.Param.ITEM_VARIANT;

        /**
         * The location associated with the item. It's recommended to use the Google Place ID that corresponds to the associated item. A custom location ID can also be used.
         * <p>
         * If set, event-level location_id is ignored.
         * If not set, event-level location_id is used, if present.
         * <p>
         * Name: location_id
         * <p>
         * Type: string
         * <p>
         * Required: no
         * <p>
         * Example: L_12345
         */
        String LOCATION_ID = FirebaseAnalytics.Param.LOCATION_ID;

        /**
         * The monetary price of the item, in units of the specified currency parameter.
         * <p>
         * Name: price
         * <p>
         * Type: number
         * <p>
         * Required: no
         * <p>
         * Example: 5.85
         */
        String PRICE = FirebaseAnalytics.Param.PRICE;


        /**
         * Item quantity.
         * <p>
         * Name: quantity
         * <p>
         * Type: number
         * <p>
         * Required: no
         * <p>
         * Example: 1
         */
        String QUANTITY = FirebaseAnalytics.Param.QUANTITY;
    }

    // auto events

    public interface ScreenView {

        String NAME = FirebaseAnalytics.Event.SCREEN_VIEW;

        interface Param {
            String SCREEN_NAME = FirebaseAnalytics.Param.SCREEN_NAME;
            String SCREEN_CLASS = FirebaseAnalytics.Param.SCREEN_CLASS;
        }
    }

    // recommended events

    public interface GenerateLead {
        /**
         * Log this event when a lead has been generated to understand the efficacy of your
         * re-engagement campaigns
         */
        String NAME = FirebaseAnalytics.Event.GENERATE_LEAD;

        interface Param {
            /**
             * Currency of the items associated with the event, in 3-letter ISO 4217 format.
             * <p>
             * If set, item-level currency is ignored.
             * If not set, currency from the first item in items is used.
             * <p>
             * If you set value then currency is required for revenue metrics to be computed accurately.
             * <p>
             * Name: currency
             * <p>
             * Type: string
             * <p>
             * Required: yes (if value is set)
             * <p>
             * Example: USD
             */
            String CURRENCY = FirebaseAnalytics.Param.CURRENCY;

            /**
             * The monetary value of the event.
             * <p>
             * value is typically required for meaningful reporting.
             * If you mark the event as a conversion then it's recommended you set value.
             * currency is required if you set value.
             * <p>
             * Name: value
             * <p>
             * Type: number
             * <p>
             * Required: no (but recommended esp for conversions)
             * <p>
             * Example: 9.75
             */
            String VALUE = FirebaseAnalytics.Param.VALUE;
        }
    }

    public interface Purchase {
        /**
         * This event signifies when one or more items is purchased by a user.
         */
        String NAME = FirebaseAnalytics.Event.PURCHASE;

        interface Param {
            /**
             * The method used to login (in case the purchase is also considered as a sign-up).
             * <p>
             * This is a Custom Param!
             * <p>
             * Name: method
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: Google
             */
            String METHOD = FirebaseAnalytics.Param.METHOD;

            /**
             * Currency of the items associated with the event, in 3-letter ISO 4217 format.
             * <p>
             * If set, item-level currency is ignored.
             * If not set, currency from the first item in items is used.
             * <p>
             * If you set value then currency is required for revenue metrics to be computed accurately.
             * <p>
             * Name: currency
             * <p>
             * Type: string
             * <p>
             * Required: yes (if value is set)
             * <p>
             * Example: USD
             */
            String CURRENCY = FirebaseAnalytics.Param.CURRENCY;

            /**
             * The monetary value of the event.
             * <p>
             * value is typically required for meaningful reporting.
             * If you mark the event as a conversion then it's recommended you set value.
             * currency is required if you set value.
             * <p>
             * Name: value
             * <p>
             * Type: number
             * <p>
             * Required: no (but recommended esp for conversions)
             * <p>
             * Example: 9.75
             */
            String VALUE = FirebaseAnalytics.Param.VALUE;

            /**
             * The unique identifier of a transaction.
             * <p>
             * Name: transaction_id
             * <p>
             * Type: string
             * <p>
             * Required: yes
             * <p>
             * Example: T_12345
             */
            String TRANSACTION_ID = FirebaseAnalytics.Param.TRANSACTION_ID;

            /**
             * A product affiliation to designate a supplying company or brick and mortar store location.
             * <p>
             * Event-level and item-level affiliation parameters are independent.
             * <p>
             * Name: affiliation
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: Google Store
             */
            String AFFILIATION = FirebaseAnalytics.Param.AFFILIATION;

            /**
             * The coupon name/code associated with the event.
             * <p>
             * Event-level and item-level coupon parameters are independent.
             * <p>
             * Name: coupon
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: SUMMER_FUN
             */
            String COUPON = FirebaseAnalytics.Param.COUPON;

            /**
             * Shipping cost associated with a transaction.
             * <p>
             * Name: shipping
             * <p>
             * Type: number
             * <p>
             * Required: no
             * <p>
             * Example: 4.35
             */
            String SHIPPING = FirebaseAnalytics.Param.SHIPPING;

            /**
             * Tax cost associated with a transaction.
             * <p>
             * Name: tax
             * <p>
             * Type: number
             * <p>
             * Required: no
             * <p>
             * Example: 1.35
             */
            String TAX = FirebaseAnalytics.Param.TAX;

            /**
             * The items for the event.
             * <p>
             * Name: items
             * <p>
             * Type: Item[]
             * <p>
             * Required: yes
             */
            String ITEMS = FirebaseAnalytics.Param.ITEMS;
        }
    }

    public interface SelectContent {
        /**
         * This event signifies that a user has selected some content of a certain type.
         * <p>
         * This event can help you identify popular content and categories of content in your app.
         */
        String NAME = FirebaseAnalytics.Event.SELECT_CONTENT;

        interface Param {
            /**
             * The type of selected content.
             * <p>
             * Name: content_type
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: product
             */
            String CONTENT_TYPE = FirebaseAnalytics.Param.CONTENT_TYPE;

            /**
             * An identifier for the item that was selected.
             * <p>
             * Name: item_id
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: I_12345
             */
            String ITEM_ID = FirebaseAnalytics.Param.ITEM_ID;
        }
    }

    public interface SelectItem {
        /**
         * This event signifies an item was selected from a list.
         */
        String NAME = FirebaseAnalytics.Event.SELECT_ITEM;

        interface Param {
            /**
             * The ID of the list in which the item was presented to the user.
             * <p>
             * Ignored if set at the item-level.
             * <p>
             * Name: item_list_id
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: related_products
             */
            String ITEM_LIST_ID = FirebaseAnalytics.Param.ITEM_LIST_ID;

            /**
             * The name of the list in which the item was presented to the user.
             * <p>
             * Ignored if set at the item-level.
             * <p>
             * Name: item_list_name
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: Related products
             */
            String ITEM_LIST_NAME = FirebaseAnalytics.Param.ITEM_LIST_NAME;

            /**
             * The items for the event.
             * <p>
             * The items array is expected to have a single element, representing the selected item.
             * <p>
             * If multiple elements are provided, only the first element in items will be used.
             * <p>
             * Name: items
             * <p>
             * Type: Item[]
             * <p>
             * Required: yes
             */
            String ITEMS = FirebaseAnalytics.Param.ITEMS;
        }
    }

    public interface ViewItem {
        /**
         * This event signifies that some content was shown to the user.
         * <p>
         * Use this event to discover the most popular items viewed.
         */
        String NAME = FirebaseAnalytics.Event.VIEW_ITEM;

        interface Param {
            /**
             * Currency of the items associated with the event, in 3-letter ISO 4217 format.
             * <p>
             * If set, item-level currency is ignored.
             * If not set, currency from the first item in items is used.
             * <p>
             * If you set value then currency is required for revenue metrics to be computed accurately.
             * <p>
             * Name: currency
             * <p>
             * Type: string
             * <p>
             * Required: yes (if value is set)
             * <p>
             * Example: USD
             */
            String CURRENCY = FirebaseAnalytics.Param.CURRENCY;

            /**
             * The monetary value of the event.
             * <p>
             * value is typically required for meaningful reporting. If you mark the event as a conversion then it's recommended you set value.
             * <p>
             * currency is required if you set value.
             * <p>
             * Name: value
             * <p>
             * Type: number
             * <p>
             * Required: no (but recommended esp for conversions)
             * <p>
             * Example: 7.77
             */
            String VALUE = FirebaseAnalytics.Param.VALUE;

            /**
             * The items for the event.
             * <p>
             * Name: items
             * <p>
             * Type: Item[]
             * <p>
             * Required: yes
             */
            String ITEMS = FirebaseAnalytics.Param.ITEMS;
        }
    }

    public interface ViewItemList {
        /**
         * Log this event when the user has been presented with a list of items of a certain category.
         */
        String NAME = FirebaseAnalytics.Event.VIEW_ITEM_LIST;

        interface Param {
            /**
             * The ID of the list in which the item was presented to the user.
             * <p>
             * Ignored if set at the item-level.
             * <p>
             * Name: item_list_id
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: related_products
             */
            String ITEM_LIST_ID = FirebaseAnalytics.Param.ITEM_LIST_ID;

            /**
             * The name of the list in which the item was presented to the user.
             * <p>
             * Ignored if set at the item-level.
             * <p>
             * Name: item_list_name
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: Related products
             */
            String ITEM_LIST_NAME = FirebaseAnalytics.Param.ITEM_LIST_NAME;

            /**
             * The items for the event.
             * <p>
             * Name: items
             * <p>
             * Type: Item[]
             * <p>
             * Required: Yes
             */
            String ITEMS = FirebaseAnalytics.Param.ITEMS;
        }
    }

    public interface Login {
        /**
         * Send this event to signify that a user has logged in.
         */
        String NAME = FirebaseAnalytics.Event.LOGIN;

        interface Param {
            /**
             * /The method used to login.
             * <p>
             * Name: method
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: Google
             */
            String METHOD = FirebaseAnalytics.Param.METHOD;
        }
    }

    public interface SignUp {
        /**
         * This event indicates that a user has signed up for an account.
         * <p>
         * Use this event to understand the different behaviors of logged in and logged out users.
         */
        String NAME = FirebaseAnalytics.Event.SIGN_UP;

        interface Param {
            /**
             * The method used for sign up.
             * <p>
             * Name: method
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: Google
             */
            String METHOD = FirebaseAnalytics.Param.METHOD;
        }
    }

    public interface JoinGroup {
        /**
         * Log this event when a user joins a group such as a guild, team, or family.
         * Use this event to analyze how popular certain groups or social features are.
         */
        String NAME = "join_group";

        interface Param {
            /**
             * The ID of the group.
             * <p>
             * Name: group_id
             * <p>
             * Type: string
             * <p>
             * Required: no
             * <p>
             * Example: G_12345
             */
            String GROUP_ID = FirebaseAnalytics.Param.GROUP_ID;
        }
    }

    // costume events

    public interface LocalFailure {
        String NAME = "LOCAL_FAILURE";

        interface Param {
            String TYPE = "FAILURE_TYPE";
            String AUTO_HANDLED = "FAILURE_AUTO_HANDLED";
            String PAYLOAD = "FAILURE_PAYLOAD";
        }
    }

    public interface ContextInit {
        String NAME = "CONTEXT_INIT";

        interface Param {
            String TIMESTAMP = "CONTEXT_INIT_TIMESTAMP";
        }
    }

    public interface WorkerRequest {
        String NAME = "WORKER_REQUEST";

        interface Param {
            String ID = "WORKER_REQUEST_ID";
            String TIMESTAMP = "WORKER_REQUEST_TIMESTAMP";
            String WORKER_NAME = "WORKER_REQUEST_NAME";
            String UPTIME = "WORKER_REQUEST_UPTIME";
        }
    }

    public interface WorkerSession {
        String NAME = "WORKER_SESSION";

        interface Param {
            /**
             * number of millis between job enqueue and it's actual run time
             */
            String DELAY = "WORKER_SESSION_DELAY";
            String ID = "WORKER_SESSION_ID";
            String WORKER_NAME = "WORKER_SESSION_NAME";
        }
    }

    public interface ServiceMissingWarning {
        String NAME = "SERVICE_MISSING_WARNING";

        interface Param {
            String SOURCE_OPERATION = "SERVICE_MISSING_SOURCE_OPERATION";
        }
    }

    public interface ActivationNeededWarning {
        String NAME = "ACTIVATION_NEEDED_WARNING";

        interface Param {
            String SOURCE_OPERATION = "ACTIVATION_NEEDED_SOURCE_OPERATION";
        }
    }

    public interface SetTicket {
        String NAME = "SET_TICKET";

        interface Param {
            String TICKET_ID = "TICKET_ID";//not reported
            String TICKET_NUMBER = "TICKET_NUMBER";
        }
    }

    public interface ClearTicket {
        String NAME = "CLEAR_TICKET";

        interface Param {
            String TICKET_ID = "TICKET_ID";//not reported
            String TICKET_CLEARING_TRIGGER = "TICKET_CLEARING_TRIGGER";
        }
    }

    public interface SnoozeAlarm {
        String NAME = "SNOOZE_ALARM";
    }

    public interface StartCamera {
        String NAME = "START_CAMERA";
    }

    public interface ScanQR {
        String NAME = "SCAN_QR";
    }

    public interface DenyPermission {
        String NAME = "DENY_PERMISSION";

        interface Param {
            String DENIED_PERMISSION = "DENIED_PERMISSION";
        }
    }

    public interface HideCompactDashboard {
        String NAME = "HIDE_COMPACT_DASHBOARD";

        interface Param {
            String COMPACT_UI_HIDING_TRIGGER = "COMPACT_UI_HIDING_TRIGGER";
        }
    }

    public interface ShowCompactDashboard {
        String NAME = "SHOW_COMPACT_DASHBOARD";

        interface Param {
            String COMPACT_UI_SHOWING_TRIGGER = "COMPACT_UI_SHOWING_TRIGGER";
        }
    }

    public interface SmsSent {
        String NAME = "SMS_SENT";

        interface Param {
            String SMS_ID = "SENT_SMS_ID";//not reported
            String SMS_TOKEN = "SENT_SMS_TOKEN";//not reported
        }
    }

    public interface SmsNotSent {
        String NAME = "SMS_NOT_SENT";

        interface Param {
            String SMS_ID = "UNSENT_SMS_ID";//not reported
            String SMS_TOKEN = "UNSENT_SMS_TOKEN";//not reported
            String SMS_OUTCOME = "UNSENT_SMS_OUTCOME";
        }
    }

    public interface SmsDelivered {
        String NAME = "SMS_DELIVERED";

        interface Param {
            String SMS_ID = "DELIVERED_SMS_ID";//not reported
            String SMS_TOKEN = "DELIVERED_SMS_TOKEN";//not reported
        }
    }

    public interface ActivationRejected {
        String NAME = "ACTIVATION_REJECTED";

        interface Param {
            String ACTIVATION_OUTCOME = "REJECTED_ACTIVATION_OUTCOME";
            String ACTIVATION_DATE = "ACTIVATION_DATE";
        }
    }

    public interface ShowDialog {
        String NAME = "SHOW_DIALOG";

        interface Param {
            String DIALOG_NAME = "SHOWN_DIALOG_NAME";
        }
    }

    public interface HideDialog {
        String NAME = "HIDE_DIALOG";

        interface Param {
            String DIALOG_NAME = "HIDDEN_DIALOG_NAME";
        }
    }

    public interface EditorSubmission {
        String NAME = "EDITOR_SUBMISSION";

        interface Param {
            String EDITOR_LABEL = "SUBMITTED_EDITOR_LABEL";
        }
    }

    public interface ControllerClick {
        String NAME = "CONTROLLER_CLICK";

        interface Param {
            String CONTROLLER_NAME = "CLICKED_CONTROLLER_NAME";
            String CONTROLLER_PAYLOAD = "CLICKED_CONTROLLER_PAYLOAD";
        }
    }

    public interface NewTurnAlarmLimited {
        String NAME = "NEW_TURN_ALARM_LIMITED";
    }

    public interface NewTurnAlarmExist {
        String NAME = "NEW_TURN_ALARM_EXIST";
    }

    public interface NewTurnAlarm {
        String NAME = "NEW_TURN_ALARM";

        interface Param {
            String NEW_VALUE = "NEW_ALARM_DURATION_IN_MINUTES";
        }
    }

    public interface DeleteTurnAlarmPhone {
        String NAME = "DELETE_TURN_ALARM_PHONE";

        interface Param {
            String TURN_ALARM_ID = "DELETED_ALARM_ID";//not reported
        }
    }

    public interface UpdateTurnAlarmPhone {
        String NAME = "UPDATE_TURN_ALARM_PHONE";

        interface Param {
            String TURN_ALARM_ID = "PHONE_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_PHONE";
        }
    }

    public interface UpdateTurnAlarmVibrate {
        String NAME = "UPDATE_TURN_ALARM_VIBRATION";

        interface Param {
            String TURN_ALARM_ID = "VIBRATION_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_VIBRATION";
        }
    }

    public interface UpdateTurnAlarmSnooze {
        String NAME = "UPDATE_TURN_ALARM_SNOOZE";

        interface Param {
            String TURN_ALARM_ID = "SNOOZE_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_SNOOZE";
        }
    }

    public interface UpdateTurnAlarmRingtone {
        String NAME = "UPDATE_TURN_ALARM_RINGTONE";

        interface Param {
            String TURN_ALARM_ID = "RINGTONE_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_RINGTONE";
        }
    }

    public interface UpdateTurnAlarmPriority {
        String NAME = "UPDATE_TURN_ALARM_PRIORITY";

        interface Param {
            String TURN_ALARM_ID = "PRIORITY_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_PRIORITY";
        }
    }

    public interface UpdateTurnAlarmMinLiquidity {
        String NAME = "UPDATE_TURN_ALARM_MIN_LIQUIDITY";

        interface Param {
            String TURN_ALARM_ID = "LIQUIDITY_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_LIQUIDITY";
        }
    }

    public interface UpdateTurnAlarmMaxQueueLength {
        String NAME = "UPDATE_TURN_ALARM_MAX_QUEUE_LENGTH";

        interface Param {
            String TURN_ALARM_ID = "MAX_QUEUE_LENGTH_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_MAX_QUEUE_LENGTH";
        }
    }

    public interface UpdateTurnAlarmDuration {
        String NAME = "UPDATE_TURN_ALARM_DURATION";

        interface Param {
            String TURN_ALARM_ID = "DURATION_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_DURATION";
        }
    }

    public interface UpdateTurnAlarmEnabled {
        String NAME = "UPDATE_TURN_ALARM_ENABLED";

        interface Param {
            String TURN_ALARM_ID = "ENABLED_UPDATED_TURN_ALARM_ID";//not reported
            String NEW_VALUE = "UPDATED_TURN_ALARM_ENABLED";
        }
    }

    public interface SmsVerificationStateUpdated {
        String NAME = "SMS_VERIFICATION_STATE_UPDATED";

        interface Param {
            String UPDATED_SMS_VERIFICATION_STATE = "UPDATED_SMS_VERIFICATION_STATE";
        }
    }

    public interface SmsVerificationCodeManualInput {
        String NAME = "SMS_VERIFICATION_CODE_MANUAL_INPUT";

        interface Param {
            String INPUT_LENGTH = "SMS_VERIFICATION_CODE_INPUT_LENGTH";
        }
    }

    public interface SuccessfulActivationAppCheck {
        String NAME = "SUCCESSFUL_ACTIVATION_APPCHECK";

        interface Param {
            String DURATION = "SUCCESSFUL_ACTIVATION_APPCHECK_DURATION";
        }
    }

    public interface FailedActivationAppCheck {
        String NAME = "FAILED_ACTIVATION_APPCHECK";

        interface Param {
            String DURATION = "FAILED_ACTIVATION_APPCHECK_DURATION";
        }
    }
}
