package owl.app.limpia_publica.models;

/**
 * Created by giusseppe on 04/03/2018.
 */

public class Opciones {
    private String Nombre;
    private int Imagen;

    public Opciones(){}

    public Opciones(String nombre, int imagen) {
        Nombre = nombre;
        Imagen = imagen;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public int getImagen() {
        return Imagen;
    }

    public void setImagen(int imagen) {
        Imagen = imagen;
    }
}
