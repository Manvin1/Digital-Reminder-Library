package manvin.digitalreminder.digitalreminderlibrary.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import manvin.digitalreminder.digitalreminderlibrary.broadcasts.LocationReminderBroadcast;
import manvin.digitalreminder.digitalreminderlibrary.broadcasts.TimeLocationReminderDispatcherBroadcast;
import manvin.digitalreminder.digitalreminderlibrary.broadcasts.TimeReminderBroadcast;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Classe que representa um gerenciador de lembretes, abstraindo o controle sobre seu ciclo de vida.
 *
 * Esta classe usa File IO para armazenar algumas informações dos lembretes registrados, o que pode funcionar de forma inesperada se diferentes threads a usarem ao mesmo tempo. Assim, esta classe não é thread-safe.
 */
public class ReminderManager {

  /**
   * Enum que especifica um dia da semana.
   */
  public enum DayOfWeek
  {
    Sunday,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
  }

  /**
   * Interface que representa um Listener para lembretes baseado no tempo.
   */
  public interface TimeReminderListener extends Parcelable
  {
    /**
     * Método invocado quando o lembrete é acionado.
     * @param context
     * @param dayOfWeek
     * @param time
     */
    void onReminder(final Context context, final DayOfWeek dayOfWeek, final LocalTime time);
  }

  /**
   * Interface que representa um Listener para lembretes baseado na Localização.
   */
  public interface LocationReminderListener extends Parcelable, Serializable
  {
    /**
     * Método invocado quando o lembrete é acionado.
     *
     * @param context
     * @param dayOfWeek
     * @param locationDetails
     */
    void onReminder(final Context context, final DayOfWeek dayOfWeek, final Pair<Location, String> locationDetails);
  }

  /**
   * Interface que representa um Listener para lembretes baseado na Localização e no tempo.
   */
  public interface TimeLocationReminderListener extends Parcelable, Serializable
  {
    /**
     * Método invocado quando o lembrete é acionado.
     *
     * @param context
     * @param dayOfWeek
     * @param time
     * @param locationDetails
     */
    void onReminder(final Context context, final DayOfWeek dayOfWeek, final LocalTime time, final Pair<Location, String> locationDetails);
  }

  /**
   * Construir um Reminder Manager com o contexto especificado.
   *
   * @param context
   */
  public ReminderManager(final Context context)
  {
    m_context = context;
  }

  /**
   * Registrar um lembrete baseado no tempo.
   *
   * Quando o lembrete for acionado, o listener é notificado.
   *
   * Note que o listener tem de ser Parcelable.
   *
   * @param id
   * @param dayOfWeek
   * @param time
   * @param listener
   */
  @SuppressLint("MissingPermission")
  public void registerTimeReminder(final String id, final DayOfWeek dayOfWeek, @NonNull final LocalTime time, @NonNull final TimeReminderListener listener)
  {
    final AlarmManager alarmManager = (AlarmManager) m_context.getSystemService(Context.ALARM_SERVICE);
    final Intent intent = new Intent(m_context, TimeReminderBroadcast.class);

    intent.setIdentifier(id);
    intent.putExtra("dayOfWeek", dayOfWeek);
    intent.putExtra("time", time);
    intent.putExtra("listener", listener);

    final Calendar target = getNextWeekDayAfterNow(dayOfWeek, time);
    final PendingIntent operation = PendingIntent.getBroadcast(m_context, REQUEST_TIME_CODE, intent, REQUEST_IMMUTABLE_CREATE_FLAGS);

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), operation);
  }

  /**
   * Registrar um lembrete baseado na Localização..
   *
   * Quando o lembrete for acionado, o listener é notificado.
   *
   * Note que o listener tem de ser Parcelable.
   *
   * @param id
   * @param dayOfWeek
   * @param locationDetails
   * @param listener
   */
  @SuppressLint("MissingPermission")
  public void registerLocationReminder(final String id, final DayOfWeek dayOfWeek, final Pair<Location, String> locationDetails, final LocationReminderListener listener)
  {
    if (ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED )
    {

      throw new RuntimeException("Manifest.permission.ACCESS_FINE_LOCATION and Manifest.permission.ACCESS_BACKGROUND_LOCATION are necessary.");
    }

    final Location location = locationDetails.first;

    final Geofence geofance = new Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(location.getLatitude(), location.getLongitude(), 1000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build();

    final GeofencingRequest.Builder operationBuilder = new GeofencingRequest.Builder();

    operationBuilder
            .setInitialTrigger(0)
            .addGeofence(geofance);

    try
    {
      final ReminderLocalStorage storage = new ReminderLocalStorage(m_context);
      final LocationReminderDetails state = new LocationReminderDetails(dayOfWeek, locationDetails.first, locationDetails.second, listener);

      storage.append(new Pair<>(id, state));

      final Intent intent = new Intent(m_context, LocationReminderBroadcast.class);
      final GeofencingClient geofencingClient = LocationServices.getGeofencingClient(m_context);

      final PendingIntent operation = PendingIntent.getBroadcast(m_context, REQUEST_LOCATION_CODE, intent, REQUEST_MUTABLE_CREATE_FLAGS);

      geofencingClient.addGeofences(operationBuilder.build(), operation)
              .addOnSuccessListener((result)->{
                /* TODO */})
              .addOnFailureListener((exception)->
              {
                /* TODO */
              });
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Registrar um lembrete baseado na Localização e no tempo.
   *
   * Quando o lembrete for acionado, o listener é notificado.
   *
   * Note que o listener tem de ser Parcelable.
   * @param id
   * @param dayOfWeek
   * @param time
   * @param locationDetails
   * @param listener
   */
  @SuppressLint("MissingPermission")
  public void registerTimeLocationReminder(final String id, final DayOfWeek dayOfWeek, @NonNull final LocalTime time, final Pair<Location, String> locationDetails, final TimeLocationReminderListener listener)
  {
    final AlarmManager alarmManager = (AlarmManager) m_context.getSystemService(Context.ALARM_SERVICE);
    final Intent intent = new Intent(m_context, TimeLocationReminderDispatcherBroadcast.class);

    intent.setIdentifier(id);

    final Calendar target = getNextWeekDayAfterNow(dayOfWeek, time);
    final PendingIntent operation = PendingIntent.getBroadcast(m_context, REQUEST_TIME_LOCATION_DISPATCHER_CODE, intent, REQUEST_IMMUTABLE_CREATE_FLAGS);

    try {
      final ReminderLocalStorage storage = new ReminderLocalStorage(m_context);
      final LocationReminderDetails state = new LocationReminderDetails(dayOfWeek, time, locationDetails.first, locationDetails.second, listener);

      storage.append(new Pair<>(id, state));

      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), operation);
    }
    catch(Exception exception)
    {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Desregistra um lembrete baseado no tempo.
   *
   * Caso não haja um lembrete baseado no tempo com o id especificado, este métdo é noop.
   *
   * @param id
   */
  public void unregisterTimeReminder(final String id)
  {
    final AlarmManager manager = (AlarmManager) m_context.getSystemService(Context.ALARM_SERVICE);
    final Intent intent = new Intent(m_context, TimeReminderBroadcast.class);

    intent.setIdentifier(id);

    final PendingIntent operation = PendingIntent.getBroadcast(m_context, REQUEST_TIME_CODE, intent, REQUEST_DELETE_FLAGS);

    if (operation != null)
    {
      manager.cancel(operation);
    }
  }

  /**
   * Desregistra um lembrete baseado na Localização.
   *
   * Caso não haja um lembrete baseado no tempo com o id especificado, este métdo é noop.
   *
   * @param id
   */
  public void unregisterLocationReminder(final String id)
  {
    final GeofencingClient geofencingClient = LocationServices.getGeofencingClient(m_context);
    final ArrayList<String> ids = new ArrayList<>();

    ids.add(id);
    geofencingClient.removeGeofences(ids);

    try {
      final ReminderLocalStorage storage = new ReminderLocalStorage(m_context);
      storage.remove(id);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Desregistra um lembrete baseado na Localizaçã e no Tempo.
   *
   * Caso não haja um lembrete baseado no tempo com o id especificado, este métdo é noop.
   *
   * @param id
   */
  public void unregisterTimeLocationReminder(final String id)
  {
    final AlarmManager manager = (AlarmManager) m_context.getSystemService(Context.ALARM_SERVICE);
    final Intent intent = new Intent(m_context, TimeLocationReminderDispatcherBroadcast.class);

    intent.setIdentifier(id);

    final PendingIntent operation = PendingIntent.getBroadcast(m_context, REQUEST_TIME_CODE, intent, REQUEST_DELETE_FLAGS);

    if (operation != null)
    {
      manager.cancel(operation);
    }
  }

  /**
   * Obter o dia da semana com o horário especificado que seja posterior ao dia atual.
   *
   * Ou seja, o dia resultado é sempre após o dia atual (e não anterior ou o mesmo).
   *
   * @param day
   * @param time
   *
   * @return próximo ocorrência do dia da semana com o horário especificado não menor que o atual.
   */
  public static Calendar getNextWeekDayAfterNow(final DayOfWeek day, final LocalTime time)
  {
    final Calendar now = Calendar.getInstance();
    final Calendar target = (Calendar) now.clone();

    target.set(Calendar.DAY_OF_WEEK, dayOfWeekToCalendarDayOfWeek(day));
    target.set(Calendar.HOUR_OF_DAY, time.getHour());
    target.set(Calendar.MINUTE, time.getMinute());
    target.set(Calendar.SECOND, time.getSecond());

    if (target.compareTo(now) <= 0)
    {
      target.add(Calendar.DAY_OF_WEEK, 7);
    }

    return target;
  }

  /**
   * Converter uma instância de DayOfWeek para uma das constantes de day of week da Calendar Class.
   *
   * @param dayOfWeek
   *
   * @return constante equivalente de Calendar Class para o dia da semana.
   */
  public static int dayOfWeekToCalendarDayOfWeek(final DayOfWeek dayOfWeek)
  {
    switch (dayOfWeek)
    {
      case Sunday: return Calendar.SUNDAY;
      case Monday: return Calendar.MONDAY;
      case Tuesday: return Calendar.TUESDAY;
      case Wednesday: return Calendar.WEDNESDAY;
      case Thursday: return Calendar.THURSDAY;
      case Friday: return Calendar.FRIDAY;
      case Saturday: return Calendar.SATURDAY;
      default: throw new RuntimeException("Unknown DayOfWeek");
    }
  }

  /**
   * Converter uma constante de Calendar Day Of Week de DayOfWeek para uma instância de DayOfWeek.
   *
   * @param dayOfWeek
   *
   * @return uma instância de DayOfWeek equivalente a constante equivalente de Calendar Class
   */
  public static DayOfWeek calendarDayOfWeekToDayOfWeek(final int dayOfWeek)
  {
    switch (dayOfWeek)
    {
      case Calendar.SUNDAY: return DayOfWeek.Sunday;
      case Calendar.MONDAY: return DayOfWeek.Monday;
      case Calendar.TUESDAY: return DayOfWeek.Tuesday;
      case Calendar.WEDNESDAY: return DayOfWeek.Wednesday;
      case Calendar.THURSDAY: return DayOfWeek.Thursday;
      case Calendar.FRIDAY: return DayOfWeek.Friday;
      case Calendar.SATURDAY: return DayOfWeek.Saturday;
      default: throw new RuntimeException("Unknown Calendar DayOfWeek");
    }
  }

  /**
   * Retorna o Dia da Semana que o dia atual do sistema está.
   *
   * @return dia da semana atual.
   */
  public static DayOfWeek getDayOfWeek()
  {
    final Calendar now = Calendar.getInstance();
    return calendarDayOfWeekToDayOfWeek(now.get(Calendar.DAY_OF_WEEK));
  }

  public static final int REQUEST_TIME_CODE = 1;
  public static final int REQUEST_LOCATION_CODE = 2;
  public static final int REQUEST_TIME_LOCATION_DISPATCHER_CODE = 3;
  public static final int REQUEST_TIME_LOCATION_CODE = 4;
  public static final int REQUEST_IMMUTABLE_CREATE_FLAGS = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
  public static final int REQUEST_MUTABLE_CREATE_FLAGS = PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
  public static final int REQUEST_DELETE_FLAGS = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE;
  final Context m_context;
}
