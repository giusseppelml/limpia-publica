package owl.app.limpia_publica.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ganesh.intermecarabic.Arabic864;
import com.honeywell.mobility.print.LinePrinter;
import com.honeywell.mobility.print.LinePrinterException;
import com.honeywell.mobility.print.PrintProgressEvent;
import com.honeywell.mobility.print.PrintProgressListener;
import com.honeywell.mobility.print.PrinterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import owl.app.limpia_publica.R;
import owl.app.limpia_publica.api.Api;
import owl.app.limpia_publica.api.RequestHandler;
import owl.app.limpia_publica.models.Cobrador;
import owl.app.limpia_publica.utils.DateTime;
import owl.app.limpia_publica.utils.KeyValue;
import owl.app.limpia_publica.utils.SharedPrefManager;

public class ComercioActivity extends AppCompatActivity{

    private Cobrador user = SharedPrefManager.getInstance(this).getUser();

    //aqui empieza crud
    private EditText editContribuyente, editImporte, editComercio;
    private ProgressBar progressBar;
    private Button buttonCobrar;

    //termina crud
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //empieza geolizacion

    private String dirGeo;
    private String lagLogGeo;

    //termina geolizacion
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //empieza carga de imagen

    private String contriValue;
    private String imporValue;
    private String comerValue;

    private String direcValue;
    private String coorValue;

    private Button btnBuscar;
    private ImageView imageView;
    private EditText editTextName;
    private Bitmap bitmap;
    private TextView leyenda;

    private boolean verificar = true;
    private ProgressDialog loadinglml;

    private String verificarLoop = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comercio);

        Bundle bundle = getIntent().getExtras();
        if(bundle.getBoolean("seguridad")){
            verificarLoop = "true";
        }

        leyenda = (TextView)findViewById(R.id.leyendaImagen);
        editContribuyente = (EditText)findViewById(R.id.editTextComercioContribuyente);
        editComercio = (EditText)findViewById(R.id.editTextComercioLocal);
        editImporte = (EditText)findViewById(R.id.editTextComercioImporte);
        buttonCobrar = (Button) findViewById(R.id.cobrarComercioButton);
        progressBar = (ProgressBar) findViewById(R.id.ComercioprogressBar);

        btnBuscar = (Button) findViewById(R.id.btnBuscar);
        editTextName = (EditText) findViewById(R.id.editText);
        imageView  = (ImageView) findViewById(R.id.imageView);

        //Aqui termina crud
        //-----------------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------------
        //Aqui empieza geolizacion

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
                    1000);
        } else {
            locationStart();
        }

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
                verificar = false;
                leyenda.setText("Imagen Cargada");
            }
        });

        buttonCobrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoAlert();
            }
        });
    }
    //Aqui empieza crud

    private void createHero(String direcciones, String ubicacion) {
        String contribuyente = editContribuyente.getText().toString().trim();
        String importe = editImporte.getText().toString().trim();
        String comercio = editComercio.getText().toString().trim();
        String detalle = editTextName.getText().toString().trim();

        String direccion = direcciones;
        String coordenadas = ubicacion;

        //validating the inputs
        if (TextUtils.isEmpty(contribuyente)) {
            editContribuyente.setError("Ingresar un Nombre");
            editContribuyente.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(importe)) {
            editImporte.setError("Ingresar Monto");
            editImporte.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(detalle)) {
            detalle = "null";
        }

        //if validation passes

        //contribuyente, importe, tipo, fecha, direccion, coordenadas, cobrador
        HashMap<String, String> params = new HashMap<>();
        params.put(KeyValue.KEY_CONTRIBUYENTE, contribuyente);
        params.put(KeyValue.KEY_COMERCIO, comercio);
        params.put(KeyValue.KEY_IMPORTE, importe);
        params.put(KeyValue.KEY_DETALLE, detalle);
        params.put(KeyValue.KEY_TIPO, KeyValue.VALUE_COMERCIO);
        params.put(KeyValue.KEY_FECHA, DateTime.getDateTime());
        params.put(KeyValue.KEY_DIRECCION, direccion);
        params.put(KeyValue.KEY_COORDENADAS, coordenadas);
        params.put(KeyValue.KEY_COBRADOR, String.valueOf(user.getId()));


        //Calling the create hero API
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_CREATE_PAGO_COMERCIO, params, Api.CODE_POST_REQUEST);
        request.execute();
    }

    //inner class to perform network request extending an AsyncTask
    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {

        //the url where we need to send the request
        String url;

        //the parameters
        HashMap<String, String> params;

        //the request code to define whether it is a GET or POST
        int requestCode;

        //constructor to initialize values
        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        //when the task started displaying a progressbar
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setVisibility(View.VISIBLE);
            loadinglml = ProgressDialog.show(ComercioActivity.this,
                    "Procesando Pago...","Espere por favor...",
                    false,false);
        }


        //this method will give the response from the request
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //progressBar.setVisibility(GONE);
            loadinglml.dismiss();
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(ComercioActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                    verificar = true;
                    Intent intent = new Intent(ComercioActivity.this, PrintActivity.class);
                    intent.putExtra("contribuyente", editContribuyente.getText().toString().trim());
                    intent.putExtra("comercio", editComercio.getText().toString().trim());
                    intent.putExtra("importe", editImporte.getText().toString().trim());
                    intent.putExtra("tipo", "Comercio");
                    intent.putExtra("verificar", true);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //the network operation will be performed in background
        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == Api.CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);


            if (requestCode == Api.CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);

            return null;
        }
    }


    //Aqui termina crud
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //Aqui empieza geolizacion

    //Apartir de aqui empezamos a obtener la direciones y coordenadas
    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
                    1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, (LocationListener) Local);
        //mensaje1.setText("Localizacion agregada");
        //mensaje1.setText("Localizacion agregada");
        //mensaje2.setText("");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }
    public void setLocation(Location loc) {
//Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    //mensaje2.setText(DirCalle.getAddressLine(0));
                    dirGeo = DirCalle.getAddressLine(0).toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Aqui empieza la Clase Localizacion

    public class Localizacion implements LocationListener {
        ComercioActivity comercioActivity;
        public ComercioActivity getMainActivity() {
            return comercioActivity;
        }
        public void setMainActivity(ComercioActivity comercioActivity) {
            this.comercioActivity = comercioActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
// Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
// debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            String Text = "Lat = "+ loc.getLatitude() + "\n Long = " + loc.getLongitude();
            //mensaje1.setText(Text);
            lagLogGeo = Text;
            //Toast.makeText(getMainActivity(), Text, Toast.LENGTH_LONG).show();
            this.comercioActivity.setLocation(loc);
        }
        @Override
        public void onProviderDisabled(String provider) {
// Este metodo se ejecuta cuando el GPS es desactivado
            //mensaje1.setText("GPS Desactivado");
            Toast.makeText(getMainActivity(), "GPS Desactivado", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onProviderEnabled(String provider) {
// Este metodo se ejecuta cuando el GPS es activado
            //mensaje1.setText("GPS Activado");
            Toast.makeText(getMainActivity(), "GPS Activado",Toast.LENGTH_LONG).show();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    //Aqui termina geolizacion
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //Aqui empieza carga de imagen

    public String getStringImagen(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    private void uploadImage(String direcciones, String ubicacion){

        contriValue = editContribuyente.getText().toString().trim();
        imporValue = editImporte.getText().toString().trim();
        comerValue = editComercio.getText().toString().trim();

        direcValue = direcciones;
        coorValue = ubicacion;

        //Mostrar el diálogo de progreso
        final ProgressDialog loading = ProgressDialog.show(this,"Subiendo...","Espere por favor...",
                false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Api.URL_CREATE_PAGO_COMERCIO_FOTO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Descartar el diálogo de progreso
                        loading.dismiss();
                        //Mostrando el mensaje de la respuesta
                        Toast.makeText(ComercioActivity.this, s , Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(ComercioActivity.this, PrintActivity.class);
                        intent.putExtra("contribuyente", editContribuyente.getText().toString().trim());
                        intent.putExtra("comercio", editComercio.getText().toString().trim());
                        intent.putExtra("importe", editImporte.getText().toString().trim());
                        intent.putExtra("tipo", "Comercio");
                        intent.putExtra("verificar", true);
                        startActivity(intent);
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Descartar el diálogo de progreso
                        loading.dismiss();
                        //Showing toast
                        Toast.makeText(ComercioActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Convertir bits a cadena
                String imagen = getStringImagen(bitmap);
                //Obtener el nombre de la imagen
                String nombre = editTextName.getText().toString().trim();
                //Creación de parámetros
                Map<String,String> params = new Hashtable<String, String>();
                //Agregando de parámetros

                params.put(KeyValue.KEY_CONTRIBUYENTE, contriValue);
                params.put(KeyValue.KEY_COMERCIO, comerValue);
                params.put(KeyValue.KEY_IMPORTE, imporValue);
                params.put(KeyValue.KEY_DETALLE, nombre);
                params.put(KeyValue.KEY_IMAGEN, imagen);
                params.put(KeyValue.KEY_TIPO, KeyValue.VALUE_COMERCIO);
                params.put(KeyValue.KEY_FECHA, DateTime.getDateTime());
                params.put(KeyValue.KEY_DIRECCION, direcValue);
                params.put(KeyValue.KEY_COORDENADAS, coorValue);
                params.put(KeyValue.KEY_COBRADOR, String.valueOf(user.getId()));
                params.put(KeyValue.VERIFICAR_LOOP, verificarLoop);
                verificarLoop = "false";
                //Parámetros de retorno
                return params;
            }
        };
        //Creación de una cola de solicitudes
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Agregar solicitud a la cola
        requestQueue.add(stringRequest);
        //Intent intent = new Intent(ComercioActivity.this, MainActivity.class);
        //startActivity(intent);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Imagen"), Api.PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Api.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Cómo obtener el mapa de bits de la Galería
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Configuración del mapa de bits en ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showInfoAlert() {
        new AlertDialog.Builder(ComercioActivity.this)
                .setTitle("Cobro a Comercio")
                .setMessage("Quieres Realizar el cobro?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(verificar){
                            createHero("domicilio conocido", "lag: 1000, Log: 0001");
                            //createHero(dirGeo, lagLogGeo);
                        }else{
                            uploadImage("domicilio conocido", "lag: 1000, Log: 0001");
                            //uploadImage( dirGeo, lagLogGeo);
                        }
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }
}