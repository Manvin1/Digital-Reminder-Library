package manvin.digitalreminder.digitalreminderlibrary.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import manvin.digitalreminder.digitalreminderlibrary.core.LocationReminderDetails;
import manvin.digitalreminder.digitalreminderlibrary.core.ReminderLocalStorage;
import manvin.digitalreminder.digitalreminderlibrary.core.ReminderManager;


/**
 * Classe que representa um Broadcast que deve ser usado para lembretes baseados em localização.
 */
public class LocationReminderBroadcast extends BroadcastReceiver {

  //****** INHERITED FROM BaseBroadcast ******
  @Override
  public void onReceive(Context context, Intent intent) {
    final GeofencingEvent event = GeofencingEvent.fromIntent(intent);
    try
    {
      final ReminderLocalStorage storage =  new ReminderLocalStorage(context);
      for (Geofence geofence : event.getTriggeringGeofences())
      {
        final LocationReminderDetails details = storage.get(geofence.getRequestId());

        final ReminderManager.DayOfWeek dayOfWeek = details.m_dayOfWeek;

        if (dayOfWeek != ReminderManager.getDayOfWeek())
        {
          continue;
        }

        final Location location = new Location("");
        final String address = details.m_address;
        final ReminderManager.LocationReminderListener listener = details.m_locationListener;

        location.setLatitude(details.m_latitude);
        location.setLongitude(details.m_longitude);

        listener.onReminder(context, dayOfWeek, new Pair<>(location, address));
      }
    }
    catch (Exception exception){
      throw new RuntimeException(exception);
    }
  }
  //****** END OF INHERITED ******
}