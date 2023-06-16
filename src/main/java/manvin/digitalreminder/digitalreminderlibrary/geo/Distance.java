package manvin.digitalreminder.digitalreminderlibrary.geo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Classe que representa uma distância de forma agnóstica.
 */
public class Distance implements Parcelable, Serializable
{

    /**
     * Consruir uma distância para o valor especificado.
     *
     * @param distanceInM
     */
    public Distance(final double distanceInM)
    {
        m_distanceInM = distanceInM;
    }

    /**
     * Obter uma nova instância de Distance a partir dos quilômetros especificado.
     *
     * @param distance
     *
     * @return instância de distance.
     */
    public static Distance FromKM(final double distance)
    {
        return new Distance(distance * 1000);
    }

    /**
     * Obter uma nova instância de Distance a partir dos metros especificado.
     *
     * @param distance
     *
     * @return instância de distance.
     */
    public static Distance FromM(final double distance)
    {
        return new Distance(distance);
    }

    /**
     * Obter uma nova instância de Distance a partir dos centimetros especificado.
     *
     * @param distance
     *
     * @return instância de distance.
     */
    public static Distance FromCM(final double distance)
    {
        return new Distance(distance / 1000);
    }

    /**
     * Obter a distância como quilômetros.
     *
     * @return o equivalente em quilômetros da distância representa.
     */
    public double asKM()
    {
        return m_distanceInM / 1_000;
    }

    /**
     * Obter a distância como metros.
     *
     * @return o equivalente em metros da distância representa.
     */
    public double asMeter()
    {
        return m_distanceInM;
    }

    /**
     * Obter a distância como centimetros.
     *
     * @return o equivalente em centimetros da distância representa.
     */
    public double asCM()
    {
        return m_distanceInM * 100;
    }

    public static final Creator<Distance> CREATOR = new Creator<Distance>() {
        @Override
        public Distance createFromParcel(Parcel in) {
            return new Distance(in);
        }

        @Override
        public Distance[] newArray(int size) {
            return new Distance[size];
        }
    };

    //****** INHERITED FROM Parcelable ******
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeDouble(m_distanceInM);
    }
    //****** END OF INHERITED ******

    /**
     * Construir uma Distância a partir do parcel especificado.
     *
     * @param in
     */
    private Distance(Parcel in)
    {
        m_distanceInM = in.readDouble();
    }

    private double m_distanceInM;
}