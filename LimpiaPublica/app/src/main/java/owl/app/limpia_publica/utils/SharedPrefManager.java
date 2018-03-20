package owl.app.limpia_publica.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import owl.app.limpia_publica.activities.LoginActivity;
import owl.app.limpia_publica.models.Cobrador;

/**
 * Created by giusseppe on 07/03/2018.
 */

public class SharedPrefManager {
    //the constants
    private static final String SHARED_PREF_NAME = "simplifiedcodingsharedpref";
    private static final String KEY_NOMBRE = "keyusername";
    private static final String KEY_FOTO = "keyemail";
    private static final String KEY_ESTADO = "keygender";
    private static final String KEY_ROLE = "keyrole";
    private static final String KEY_USUARIO = "keyusuario";
    private static final String KEY_ID = "keyid";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //method to let the user login
    //this method will store the user data in shared preferences
    public void userLogin(Cobrador cobrador) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, cobrador.getId());
        editor.putString(KEY_NOMBRE, cobrador.getNombre());
        editor.putString(KEY_FOTO, cobrador.getFoto());
        editor.putString(KEY_ESTADO, cobrador.getEstado());
        editor.putString(KEY_ROLE, cobrador.getRole());
        editor.putString(KEY_USUARIO, cobrador.getUsuario());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NOMBRE, null) != null;
    }

    //this method will give the logged in user
    public Cobrador getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new Cobrador(
                sharedPreferences.getInt(KEY_ID, -1),
                sharedPreferences.getString(KEY_NOMBRE, null),
                sharedPreferences.getString(KEY_FOTO, null),
                sharedPreferences.getString(KEY_ESTADO, null),
                sharedPreferences.getString(KEY_ROLE, null),
                sharedPreferences.getString(KEY_USUARIO, null)
        );
    }

    //this method will logout the user
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        //mCtx.startActivity(new Intent(mCtx, LoginActivity.class));
    }
}
