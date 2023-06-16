package manvin.digitalreminder.digitalreminderlibrary.broadcasts;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.time.LocalTime;

import manvin.digitalreminder.digitalreminderlibrary.core.LocationReminderDetails;
import manvin.digitalreminder.digitalreminderlibrary.core.ReminderLocalStorage;
import manvin.digitalreminder.digitalreminderlibrary.core.ReminderManager;

/**
 * Classe que representa um Broadcast que deve ser usado para lembretes baseados em localização e tempo.
 *
 * Este broadcast deve ser notificado baseado no tempo. Quando é notificado, lança uma alerta de proximidade para a região alvo.
 */
public class TimeLocationReminderDispatcherBroadcast extends TimeReminderBroadcast {

  //****** INHERITED FROM TimeReminderBroadcast ******
  @Override
  public void onReceive(Context context, Intent intent) {
    final String id = intent.getIdentifier();

    ThrowLocationReminder(context, intent.getIdentifier());

    try
    {
      final ReminderLocalStorage storage =  new ReminderLocalStorage(context);
      final LocationReminderDetails details = storage.get(id);

      final ReminderManager.DayOfWeek dayOfWeek = details.m_dayOfWeek;
      final LocalTime time = details.m_time;

      RegisterNextAlarm(context, intent, dayOfWeek, time);
    }
    catch(Exception exception)
    {
      throw new RuntimeException(exception);
    }

  }
  //****** END OF INHERITED ******

  /**
   * Lança um alerta para a região alvo representada pelo id.
   *
   * @param context
   * @param id O id de um LocationReminderDetails previamente registrado.
   */
  @SuppressLint("MissingPermission")
  private void ThrowLocationReminder(final Context context, final String id)
  {
    try
    {
      final ReminderLocalStorage storage =  new ReminderLocalStorage(context);
      final LocationReminderDetails details = storage.get(id);

      final Location location = new Location("");

      location.setLatitude(details.m_latitude);
      location.setLongitude(details.m_longitude);

      final Geofence geofance = new Geofence.Builder()
              .setRequestId(id)
              .setCircularRegion(location.getLatitude(), location.getLongitude(), 1000)
              .setExpirationDuration(DURATION_MS)
              .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
              .build();

      final GeofencingRequest.Builder operationBuilder = new GeofencingRequest.Builder();

      operationBuilder
              .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
              .addGeofence(geofance);

      final GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);
      final Intent localIntent = new Intent(context, TimeLocationReminderBroadcast.class);

      final PendingIntent operation = PendingIntent.getBroadcast(context, ReminderManager.REQUEST_TIME_LOCATION_CODE, localIntent, ReminderManager.REQUEST_MUTABLE_CREATE_FLAGS);

      geofencingClient.addGeofences(operationBuilder.build(), operation);
    }
    catch(Exception exception)
    {
      throw new RuntimeException(exception);
    }
  }

  private int DURATION_MS = 1000 * 30;
}