package manvin.digitalreminder.digitalreminderlibrary.geo;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Classe que representa um círculo.
 */
public class Circle implements Parcelable, Serializable
{
    /**
     * Construir um círculo com o raio especificado.
     *
     * @param radius
     */
    public Circle(final Distance radius)
    {
        m_center = new Vector<>(0.0, 0.0);
        m_radius = radius;
    }

    /**
     * Definir a posição do centro do círculo a partir de um Location Object.
     *
     * @param location
     */
    public void SetCenter(final Location location)
    {
        m_center.SetX(location.getLatitude());
        m_center.SetY(location.getLongitude());
    }

    /**
     * Obter a posição do centro do círculo como uma instância de Location Object.
     *
     * Este método envolve a construção de um Location Object a cada invocação.
     *
     * @return a posição do centro como um location object.
     */
    public Location GetCenterAsLocation()
    {
        final Location location = new Location("");
        location.setLatitude(m_center.GetX());
        location.setLongitude(m_center.GetY());

        return location;
    }

    /**
     * Obter o raio do círculo.
     *
     * @return o raio do círculo.
     */
    public Distance GetRadius()
    {
        return m_radius;
    }

    public static final Creator<Circle> CREATOR = new Creator<Circle>() {
        @Override
        public Circle createFromParcel(Parcel in) {
            return new Circle(in);
        }

        @Override
        public Circle[] newArray(int size) {
            return new Circle[size];
        }
    };

    //****** INHERITED FROM Parcelable ******
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeTypedObject(m_center, 0);
        parcel.writeTypedObject(m_radius, 0);
    }
    //****** END OF INHERITED ******

    /**
     * Construir um círculo a partir de um Parcel.
     * @param in
     */
    private Circle(Parcel in)
    {
        m_center = in.readTypedObject(Vector.CREATOR);
        m_radius = in.readTypedObject(Distance.CREATOR);
    }

    private Vector<Double> m_center;
    private Distance m_radius;
}