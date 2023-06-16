package manvin.digitalreminder.digitalreminderlibrary.core;

import android.content.Context;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;


/**
 * Classe usada para serializar e desserializar LocationReminderDetails usando o sistema de arquivos do dispositivo.
 *
 * Esses objetos serão distribuidos por meio de um mapa que associa um id a esses objetos. Espera-se que esse id seja o mesmo dos geofence objetos que são usados para despachar lembretes baseado em localização.
 *
 */
public class ReminderLocalStorage {

  /**
   * Construir uma ReminderLocalStorage que usa o contexto especificado.
   *
   *
   * @param context
   *
   * @throws IOException
   */
  public ReminderLocalStorage(final Context context) throws IOException {
    m_context = context;
    m_file = new File(m_context.getDataDir(), FILE_NAME);

    if (!m_file.exists())
    {
      makeFile(m_file);
    }
  }

  /**
   * Desserializar o mapa.
   *
   * @return uma lista dos objetos
   *
   * @throws RuntimeException
   */
  public HashMap<String,LocationReminderDetails> deserialize(){
    try(final FileInputStream stream = new FileInputStream(m_file))
    {
      if (stream.available() == 0)
      {
        return new HashMap<>();
      }

      final ObjectInputStream objectStream = new ObjectInputStream(stream);
      return (HashMap<String,LocationReminderDetails>) objectStream.readObject();
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Serializar o mapa.
   *
   * @param states
   *
   * @throws RuntimeException
   */
  public void serialize(HashMap<String,LocationReminderDetails> states)  {
    try(final ObjectOutputStream buffer = new ObjectOutputStream(new FileOutputStream(m_file, false)))
    {
      buffer.writeObject(states);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Anexar no mapa o par especificado.
   *
   * É equivalente a desserializar o mapa, adicionar um novo par, e serializá-lo novamente.
   *
   * @param state par
   *
   * @throws RuntimeException
   */
  public void append(Pair<String,LocationReminderDetails> state)
  {
    try(final FileInputStream stream = new FileInputStream(m_file))
    {
      if (stream.available() == 0)
      {
        final HashMap<String,LocationReminderDetails> map = new HashMap<>();

        map.put(state.first, state.second);
        serialize(map);

        return;
      }

      final HashMap<String,LocationReminderDetails> map = deserialize();

      map.put(state.first, state.second);
      serialize(map);
    }
    catch(Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Obter uma chave para a key especificada.
   *
   * É equivalente a desserializar o mapa e obter o valor que usa esta chave, se algum.
   *
   * @param key
   *
   * @return o valor.
   *
   * @throws RuntimeException
   */
  public LocationReminderDetails get(final String key)
  {
    final HashMap<String,LocationReminderDetails> map = deserialize();
    return map.get(key);
  }

  /**
   * Remove do mapa o par com a key especificada.
   *
   * É equivalente a desserializar o mapa, remover o par que usa a key, se presente, e serializar o mapa noamente.
   * @param key
   *
   * @throws RuntimeException
   */
  public void remove(final String key)
  {
    final HashMap<String,LocationReminderDetails> map = deserialize();

    map.remove(key);
    serialize(map);
  }

  /**
   * Criar o diretório especificado, caso não exista.
   *
   * @param file
   *
   * @throws IOException
   */
  private void makeFile(final File file) throws IOException {
    new FileOutputStream(file).close();
  }

  private static String FILE_NAME = "reminderStateLocalStorage";
  private File m_file;
  private Context m_context;
}
