package info.nightscout.androidaps.plugins.pump.omnipod.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.logging.L;
import info.nightscout.androidaps.plugins.general.overview.events.EventDismissNotification;
import info.nightscout.androidaps.plugins.general.overview.events.EventNewNotification;
import info.nightscout.androidaps.plugins.general.overview.notifications.Notification;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.data.RLHistoryItem;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.MedtronicNotificationType;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationManager;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.OmnipodCommandType;
import info.nightscout.androidaps.plugins.pump.omnipod.service.OmnipodPumpStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.service.RileyLinkOmnipodService;
import info.nightscout.androidaps.utils.OKDialog;

/**
 * Created by andy on 4/8/19.
 */
// FIXME
public class OmnipodUtil extends RileyLinkUtil {

    private static final Logger LOG = LoggerFactory.getLogger(L.PUMPCOMM);

    private static boolean lowLevelDebug = true;
    private static RileyLinkOmnipodService omnipodService;
    private static OmnipodPumpStatus omnipodPumpStatus;
    private static OmnipodCommandType currentCommand;
    private static Gson gsonInstance = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static Gson getGsonInstance() {
        return gsonInstance;
    }

    public static int makeUnsignedShort(int b2, int b1) {
        int k = (b2 & 0xff) << 8 | b1 & 0xff;
        return k;
    }

    public static byte[] getByteArrayFromUnsignedShort(int shortValue, boolean returnFixedSize) {
        byte highByte = (byte) (shortValue >> 8 & 0xFF);
        byte lowByte = (byte) (shortValue & 0xFF);

        if (highByte > 0) {
            return createByteArray(highByte, lowByte);
        } else {
            return returnFixedSize ? createByteArray(highByte, lowByte) : createByteArray(lowByte);
        }

    }


    public static byte[] createByteArray(byte... data) {
        return data;
    }


    public static byte[] createByteArray(List<Byte> data) {

        byte[] array = new byte[data.size()];

        for (int i = 0; i < data.size(); i++) {
            array[i] = data.get(i);
        }

        return array;
    }


    public static void sendNotification(MedtronicNotificationType notificationType) {
        Notification notification = new Notification( //
                notificationType.getNotificationType(), //
                MainApp.gs(notificationType.getResourceId()), //
                notificationType.getNotificationUrgency());
        MainApp.bus().post(new EventNewNotification(notification));
    }


    public static void sendNotification(MedtronicNotificationType notificationType, Object... parameters) {
        Notification notification = new Notification( //
                notificationType.getNotificationType(), //
                MainApp.gs(notificationType.getResourceId(), parameters), //
                notificationType.getNotificationUrgency());
        MainApp.bus().post(new EventNewNotification(notification));
    }


    public static void dismissNotification(MedtronicNotificationType notificationType) {
        MainApp.bus().post(new EventDismissNotification(notificationType.getNotificationType()));
    }


    public static boolean isLowLevelDebug() {
        return lowLevelDebug;
    }


    public static void setLowLevelDebug(boolean lowLevelDebug) {
        OmnipodUtil.lowLevelDebug = lowLevelDebug;
    }


    public static OmnipodCommunicationManager getOmnipodCommunicationManager() {
        return (OmnipodCommunicationManager) RileyLinkUtil.rileyLinkCommunicationManager;
    }


    public static RileyLinkOmnipodService getOmnipodService() {
        return OmnipodUtil.omnipodService;
    }


    public static void setOmnipodService(RileyLinkOmnipodService medtronicService) {
        OmnipodUtil.omnipodService = medtronicService;
    }

    public static OmnipodCommandType getCurrentCommand() {
        return OmnipodUtil.currentCommand;
    }


    // FIXME
    public static void setCurrentCommand(OmnipodCommandType currentCommand) {
        OmnipodUtil.currentCommand = currentCommand;

        if (currentCommand != null)
            historyRileyLink.add(new RLHistoryItem(currentCommand));

    }


    public static boolean isSame(Double d1, Double d2) {
        double diff = d1 - d2;

        return (Math.abs(diff) <= 0.000001);
    }


    public static void displayNotConfiguredDialog(Context context) {
        OKDialog.show(context, MainApp.gs(R.string.combo_warning),
                MainApp.gs(R.string.medtronic_error_operation_not_possible_no_configuration), null);
    }

    public static OmnipodPumpStatus getPumpStatus() {
        return omnipodPumpStatus;
    }

    public static void setPumpStatus(OmnipodPumpStatus omnipodPumpStatus) {
        OmnipodUtil.omnipodPumpStatus = omnipodPumpStatus;
    }

    private static Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (dateTime, typeOfSrc, context) ->
                        new JsonPrimitive(ISODateTimeFormat.dateTime().print(dateTime)))
                .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) ->
                        ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()))
                .registerTypeAdapter(DateTimeZone.class, (JsonSerializer<DateTimeZone>) (timeZone, typeOfSrc, context) ->
                        new JsonPrimitive(timeZone.getID()))
                .registerTypeAdapter(DateTimeZone.class, (JsonDeserializer<DateTimeZone>) (json, typeOfT, context) ->
                        DateTimeZone.forID(json.getAsString()));

        return gsonBuilder.create();
    }
}
