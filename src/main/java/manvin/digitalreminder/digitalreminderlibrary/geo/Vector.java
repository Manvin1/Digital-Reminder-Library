package manvin.digitalreminder.digitalreminderlibrary.geo;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Classe que representa um par de valores.
 *
 * @param <T> O tipo dos valores, que deve ser numérico, comparável e serializável.
 */
public class Vector<T extends Number & Comparable & Serializable> implements Parcelable, Serializable {

    /**
     * Construir um vetor com os valores especificados.
     *
     * @param x
     * @param y
     */
    public Vector(final T x, final T y)
    {
        this.m_x = x;
        this.m_y = y;
    }

    /**
     * Construir um vetor a partir do parcel especificado.
     *
     * @param in
     */
    protected Vector(Parcel in) {
        m_x = (T) in.readSerializable();
        m_y = (T) in.readSerializable();
    }

    /**
     * Retornar o valor X do vetor.
     *
     * @return x
     */
    public T GetX() {
        return m_x;
    }

    /**
     * Retornar o valor Y do vetor.
     *
     * @return y
     */
    public T GetY() {
        return m_y;
    }

    /**
     * Definir o valor x do vetor.
     *
     * @param x
     */
    public void SetX(T x) {
        this.m_x = x;
    }

    /**
     * Definir o valor y do vetor.
     *
     * @param y
     */
    public void SetY(T y) {
        this.m_y = y;
    }

    public static final Creator<Vector> CREATOR = new Creator<Vector>() {
        @Override
        public Vector createFromParcel(Parcel in) {
            return new Vector(in);
        }

        @Override
        public Vector[] newArray(int size) {
            return new Vector[size];
        }
    };

    //****** INHERITED FROM Parcelable ******
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable((Serializable) m_x);
        dest.writeSerializable((Serializable) m_y);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    //****** END OF INHERITED ******

    private T m_x;
    private T m_y;
}
