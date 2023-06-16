package manvin.digitalreminder.digitalreminderlibrary.geo;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Classe que representa uma região geográfica de forma circular.
 */
public class Region implements Parcelable, Serializable
{
    /**
     * Construir uma região para o círculo especificado e com a descripção associada.
     *
     * @param circle
     * @param description
     */
    public Region(final Circle circle, final String description)
    {
        m_circle = circle;
        m_description = description;
    }

    /**
     * Obter o círculo que representa a região.
     *
     * @return o círculo.
     */
    public Circle GetCircle() {
        return m_circle;
    }

    /**
     * Obter a descrição da região.
     *
     * @return a descrição.
     */
    public String GetDescription() {
        return m_description;
    }

    /**
     * Definir o círculo associado a região.
     *
     * @param circle
     */
    public void SetCircle(final Circle circle) {
        this.m_circle = circle;
    }

    /**
     * Obter todos os detalhes principais da região.
     *
     * @return um par que representa a localização central da região e a sua descrição.
     */
    public Pair<Location, String> GetDetails()
    {
        return new Pair<>(m_circle.GetCenterAsLocation(), m_description);
    }

    /**
     * Definir a descrição da região.
     *
     * @param description
     */
    public void SetDescription(final String description) {
        this.m_description = description;
    }

    public static final Creator<Region> CREATOR = new Creator<Region>() {
        @Override
        public Region createFromParcel(Parcel in) {
            return new Region(in);
        }

        @Override
        public Region[] newArray(int size) {
            return new Region[size];
        }
    };

    //****** INHERITED FROM Parcelable ******
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeTypedObject(m_circle, 0);
        parcel.writeString(m_description);
    }
    //****** END OF INHERITED ******

    /**
     * Construir uma região para o parcel especificado.
     *
     * @param in
     */
    private Region(Parcel in)
    {
        m_circle = in.readTypedObject(Circle.CREATOR);
        m_description = in.readString();
    }

    private Circle m_circle;
    private String m_description;
}
