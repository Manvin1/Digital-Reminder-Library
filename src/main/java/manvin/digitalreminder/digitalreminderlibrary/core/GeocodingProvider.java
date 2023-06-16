package manvin.digitalreminder.digitalreminderlibrary.core;

import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Optional;

/**
 * Interface que representa um realizador de geocoding e reverse geocoding.
 *
 * Todas as implementações devem ser síncronas, pois sempre serão invocadas em outra thread.
 */
public interface GeocodingProvider {

  /**
   * Obter o endereço de uma localização (reverse geocoding).
   *
   * @param location
   *
   * @return o endereço.
   */
  Optional<String> fetchLocationDetails(@NonNull final Location location);

  /**
   * Obter o endereço de uma localização, de forma que o endereço seja usável como contexto para geocoding (reverse geocoding).
   *
   * A string retornada tem informação suficiente para prover contexto a reverse geocoding, contudo não a obscurece. Por exemplo, retorna apenas a cidade e o estado, mas não a rua ou bairro da localização.
   *
   * É usada em LocationFetcher.fetchAddressDetails.
   *
   * @param location
   *
   * @return o endereço
   */
  Optional<String> fetchLocationDetailsForReference(@NonNull final Location location);

  /**
   * Obter a localização do endereço especificado, usando o contexto ou referência indicado como base (geocoding).
   *
   * @param address
   * @param references
   *
   * @return a localização do endereço especificado.
   */
  Optional<Pair<Location, String>> fetchAddressDetails(@NonNull final String address, @NonNull final String references);
}
