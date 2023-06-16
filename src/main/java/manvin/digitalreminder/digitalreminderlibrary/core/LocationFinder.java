package manvin.digitalreminder.digitalreminderlibrary.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;


import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe que representa um buscador de localizações e endereços, sempre assincronamente.
 *
 * Os listeners são sempre invocados na thread especificada pelo cliente, enquanto que as operações sempre ocorrem em uma nova thread independente.
 *
 * É necessário definir explicitamente um Fetcher para as requisições por meio de LocationFinder.setFetcher.
 */
public class LocationFinder {

  /**
   * Interface que representa um listener ao qual é injetada o resultado de uma busca bem sucedida.
   */
  public interface SuccessListener {
    /**
     * Método invocado quando uma busca é bem sucedida.
     * @param result
     */
    void onSuccess(@NonNull final Pair<Location, String> result);
  }

  /**
   * Interface que representa um listener ao qual é injetada a exceção  de uma que falhou.
   */
  public interface FailListener {
    /**
     * Método invocado quando uma busca falha.
     *
     * @param reason
     */
    void onFail(final Exception reason);
  }

  /**
   * Construir um LocationFinder com o contexto e callBack Executor especificado.
   *
   * Os listeners são invocados a partir do executor especificado.
   *
   * @param context
   * @param callBackExecutor
   */
  public LocationFinder(@NonNull Context context, Executor callBackExecutor) {
    m_context = context;
    m_bgExecutor = Executors.newSingleThreadExecutor();
    m_callBackExecutor = callBackExecutor;
  }

  /**
   * Definir o listener invocado quando uma requisição é bem sucedida.
   *
   * Só é possível definir um success listener por vez.
   *
   * @param successListener
   */
  public void setOnSuccessListener(final SuccessListener successListener) {
    m_successListener = successListener;
  }

  /**
   * Definir o listener invocado quando uma requisição falha.
   *
   * Só é possível definir um fail listener por vez.
   *
   * @param failListener
   */
  public void setOnFailListener(final FailListener failListener) {
    m_failListener = failListener;
  }

  /**
   * Buscar informações sobre a localização especifica.
   *
   * @param location A localização alvo. Se for null, é considerada a localização atual do dispositivo, que será obtida internamente.
   *
   * @throws RuntimeException - Se um
   */
  public void find(final String location) {
    if (m_fetcher == null)
    {
      throw new RuntimeException("An Location Fetcher is necessary.");
    }

    m_bgExecutor.execute(() ->
    {
      if (ActivityCompat.checkSelfPermission(m_context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
              && ActivityCompat.checkSelfPermission(m_context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if (m_failListener != null) {
          m_callBackExecutor.execute(() -> m_failListener.onFail(new Exception("Insufficient permission")));
        }

        m_bgExecutor.shutdown();
        return;
      }

      if (location == null) {
        onActualAddressFetch();
      } else {
        onArbitraryAddressFetch(location);
      }

      m_bgExecutor.shutdown();
    });
  }

  /**
   * Define o Fetcher usado pela API para as suas requisições.
   *
   * @param fetcher
   */
  public static void setFetcher(@NonNull final GeocodingProvider fetcher)
  {
    m_fetcher = fetcher;
  }

  /**
   * Busca informações sobre a localização atual do dispositivo.
   *
   * Ao final do processo, ou successListener ou failListener são invocados.
   */
  @SuppressLint("MissingPermission")
  private void onActualAddressFetch()
  {
    final FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(m_context);
    final Task task = fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);

    task.addOnSuccessListener(m_bgExecutor, (OnSuccessListener<Location>) location ->
    {
      if (location == null) {
        if (m_failListener != null)
        {
          m_callBackExecutor.execute(()->m_failListener.onFail(new Exception("Fetch error")));
        }
        return;
      }

      try
      {
        final Optional<String> details = m_fetcher.fetchLocationDetails(location);

        if (details.isPresent())
        {
          if (m_successListener != null)
          {
            m_callBackExecutor.execute(()->m_successListener.onSuccess(new Pair<>(location, details.get())));
          }
        }
        else
        {
          m_callBackExecutor.execute(()->m_failListener.onFail(new Exception("Fetch error")));
        }
      }
      catch (Exception e)
      {
        if (m_failListener != null)
        {
          m_callBackExecutor.execute(()->m_failListener.onFail(new Exception("Fetch error")));
        }
      }
    });

    task.addOnFailureListener(m_bgExecutor, exception ->
    {
      if (m_failListener != null)
      {
        m_callBackExecutor.execute(()->m_failListener.onFail(exception));
      }
    });

    try
    {
      Tasks.await(task);
    }
    catch (Exception e)
    {
      if (m_failListener != null)
      {
        m_callBackExecutor.execute(()-> m_failListener.onFail(e));
      }
    }
  }

  /**
   * Busca informações sobre a endereço especifada.
   *
   * Ao final do processo, ou successListener ou failListener são invocados.
   *
   * @param targetAddress
   */
  @SuppressLint("MissingPermission")
  private void onArbitraryAddressFetch(final String targetAddress)
  {
    final FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(m_context);
    final Task task = fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null);

    task.addOnSuccessListener(m_bgExecutor, (OnSuccessListener<Location>) location ->
    {
      if (location == null)
      {
        if (m_failListener != null)
        {
          m_callBackExecutor.execute(()->m_failListener.onFail(new Exception("Fetch Error")));
        }
        return;
      }

      try
      {
        final Optional<String> reference = m_fetcher.fetchLocationDetailsForReference(location);

        if (!reference.isPresent())
        {
          m_callBackExecutor.execute(()->m_failListener.onFail(new Exception("Fetch Error")));
          return;
        }

        final Optional<Pair<Location, String>> targetAddressDetails = m_fetcher.fetchAddressDetails(targetAddress, reference.get());

        if (targetAddressDetails.isPresent())
        {
          if (m_successListener != null)
          {
            m_callBackExecutor.execute(()->m_successListener.onSuccess(targetAddressDetails.get()));
          }
        }
        else
        {
          m_callBackExecutor.execute(()->m_failListener.onFail(new Exception("Fetch Error")));
          return;
        }
      } catch (Exception e) {
        m_callBackExecutor.execute(()->m_failListener.onFail(e));
      }
    });

    task.addOnFailureListener(m_bgExecutor, exception->
    {
      m_callBackExecutor.execute(()->m_failListener.onFail(exception));
    });

    try
    {
      Tasks.await(task);
    }
    catch (Exception e)
    {
      m_callBackExecutor.execute(()->m_failListener.onFail(e));
    }
  }

  private static GeocodingProvider m_fetcher = null;
  private final ExecutorService m_bgExecutor;
  private final Executor m_callBackExecutor;
  private final Context m_context;
  private SuccessListener m_successListener;
  private FailListener m_failListener;
}
