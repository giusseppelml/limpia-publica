package owl.app.limpia_publica.models;

/**
 * Created by giusseppe on 03/03/2018.
 */

public class Cobrador {
    private int id;
    private String nombre;
    private String foto;
    private String estado;
    private String role;
    private String usuario;

    public Cobrador(){}

    public Cobrador(int id, String nombre, String foto, String estado, String role, String usuario) {
        this.id = id;
        this.nombre = nombre;
        this.foto = foto;
        this.estado = estado;
        this.role = role;
        this.usuario = usuario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
