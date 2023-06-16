package manvin.digitalreminder.digitalreminderlibrary.core;

import android.location.Location;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Classe que representa os detalhes de um lembrete baseado na localização, possivelmente tanto na localização quanto no tempo.
 *
 * Detalhes para lembretes baseados apenas na localização não possuem (= null) os campos  para timeLocationListener e time.
 * Detalhes para lembretes baseados na localização e no tempo não possuem (= null) os campos para locationListener.
 */
public class LocationReminderDetails implements Serializable
{
  /**
   * Enum que especifica o tipo de lembrete baseado na localização.
   */
  enum Type
  {
    LOCATION,
    TIME_LOCATION,
  }

  /**
   * Construir os detalhes de um lembrete baseado apenas na localização.
   *
   * O tipo será Type.LOCATION implicitamente.
   *
   * @param dayOfWeek
   * @param location
   * @param address
   * @param listener
   */
  public LocationReminderDetails(final ReminderManager.DayOfWeek dayOfWeek, final Location location, final String address, final ReminderManager.LocationReminderListener listener)
  {
    m_type = Type.LOCATION;
    m_dayOfWeek = dayOfWeek;
    m_time = null;
    m_latitude = location.getLatitude();
    m_longitude = location.getLongitude();
    m_address = address;
    m_locationListener = listener;
    m_timeLocationListener = null;
  }

  /**
   * Construir os detalhes de um lembrete baseado tanto na localização quanto no tempo.
   *
   * O tipo será Type.TIME_LOCATION implicitamente.
   * @param dayOfWeek
   * @param time
   * @param location
   * @param address
   * @param listener
   */
  public LocationReminderDetails(final ReminderManager.DayOfWeek dayOfWeek, final LocalTime time, final Location location, final String address, final ReminderManager.TimeLocationReminderListener listener)
  {
    m_type = Type.TIME_LOCATION;
    m_dayOfWeek = dayOfWeek;
    m_time = time;
    m_latitude = location.getLatitude();
    m_longitude = location.getLongitude();
    m_address = address;
    m_locationListener = null;
    m_timeLocationListener = listener;
  }

  public final Type m_type;
  public final ReminderManager.DayOfWeek m_dayOfWeek;
  public final LocalTime m_time;
  public final Double m_latitude;
  public final Double m_longitude;
  public final String m_address;
  public final ReminderManager.LocationReminderListener m_locationListener;
  public final ReminderManager.TimeLocationReminderListener m_timeLocationListener;
}