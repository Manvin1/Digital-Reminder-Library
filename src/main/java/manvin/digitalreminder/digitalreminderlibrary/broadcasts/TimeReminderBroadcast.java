package manvin.digitalreminder.digitalreminderlibrary.broadcasts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import manvin.digitalreminder.digitalreminderlibrary.core.ReminderManager;

import java.time.LocalTime;
import java.util.Calendar;

/**
 * Classe que representa um Broadcast que deve ser usado para lembretes baseados no tempo.
 */
public class TimeReminderBroadcast extends BroadcastReceiver {

  //****** INHERITED FROM BroadcastReceiver ******
  @Override
  public void onReceive(Context context, Intent intent) {
    final Bundle extras = intent.getExtras();

    final ReminderManager.DayOfWeek dayOfWeek = (ReminderManager.DayOfWeek) extras.getSerializable("dayOfWeek");
    final LocalTime time = (LocalTime) extras.getSerializable("time");
    final ReminderManager.TimeReminderListener listener = extras.getParcelable("listener");

    listener.onReminder(context, dayOfWeek, time);
    RegisterNextAlarm(context, intent);
  }
  //****** END OF INHERITED ******

  /**
   * Registra o próximo alarme baseado no tempo.
   *
   * @param context
   * @param intent
   */
  protected void RegisterNextAlarm(final Context context, final Intent intent)
  {
    final Bundle extras = intent.getExtras();

    final ReminderManager.DayOfWeek dayOfWeek = (ReminderManager.DayOfWeek) extras.getSerializable("dayOfWeek");
    final LocalTime time = (LocalTime) extras.getSerializable("time");

    final Calendar target = ReminderManager.getNextWeekDayAfterNow(dayOfWeek, time);
    final AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
    final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ReminderManager.REQUEST_TIME_CODE, intent, ReminderManager.REQUEST_IMMUTABLE_CREATE_FLAGS);

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
  }

  /**
   * Registra o próximo alarme baseado no tempo.
   *
   * O dia da semana e o time especificados serão usados.
   *
   * @param context
   * @param intent
   * @param dayOfWeek
   * @param time
   */
  protected void RegisterNextAlarm(final Context context, final Intent intent, final ReminderManager.DayOfWeek dayOfWeek, final LocalTime time)
  {
    final Calendar target = ReminderManager.getNextWeekDayAfterNow(dayOfWeek, time);
    final AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
    final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ReminderManager.REQUEST_TIME_CODE, intent, ReminderManager.REQUEST_IMMUTABLE_CREATE_FLAGS);

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
  }
}