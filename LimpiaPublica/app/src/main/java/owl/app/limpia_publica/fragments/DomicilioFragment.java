package owl.app.limpia_publica.fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import owl.app.limpia_publica.R;
import owl.app.limpia_publica.api.Api;
import owl.app.limpia_publica.api.RequestHandler;
import owl.app.limpia_publica.models.Cobrador;
import owl.app.limpia_publica.utils.DateTime;
import owl.app.limpia_publica.utils.KeyValue;
import owl.app.limpia_publica.utils.SharedPrefManager;

import static android.view.View.GONE;

public class DomicilioFragment extends DialogFragment {

    private Cobrador user = SharedPrefManager.getInstance(getContext()).getUser();

    //aqui empieza crud
    private EditText editContribuyente, editImporte;
    private ProgressBar progressBar;
    private Button buttonCobrar;

    //termina crud
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------
    //empieza geolizacion

    private String dirGeo;
    private String lagLogGeo;

    //termina geolizacion

    public DomicilioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_domicilio, container, false);

        editContribuyente = (EditText)view.findViewById(R.id.editTextContribuyente);
        editImporte = (EditText)view.findViewById(R.id.editTextImporte);
        buttonCobrar = (Button) view.findViewById(R.id.cobrarButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        buttonCobrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createHero(dirGeo, lagLogGeo, DateTime.getDateTime());
            }
        });

        //Aqui termina crud
        //-----------------------------------------------------------------------------------------
        //-----------------------------------------------------------------------------------------
        //Aqui empieza geolizacion

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
                    1000);
        } else {
            locationStart();
        }

        //Aqui termina geolizacion

        //DialogFragment dialogFragment;
        //dialogFragment.getDialog().setCanceledOnTouchOutside(true);

        return view;
    }

    //Aqui empieza crud

    private void createHero(String direcciones, String ubicacion, String fechaHora) {
        String contribuyente = editContribuyente.getText().toString().trim();
        String importe = editImporte.getText().toString().trim();
        String tipo = "vivienda";

        String fecha = fechaHora;
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

        //if validation passes

        //contribuyente, importe, tipo, fecha, direccion, coordenadas, cobrador
        HashMap<String, String> params = new HashMap<>();
        params.put(KeyValue.KEY_CONTRIBUYENTE, contribuyente);
        params.put(KeyValue.KEY_IMPORTE, importe);
        params.put(KeyValue.KEY_TIPO, tipo);
        params.put(KeyValue.KEY_FECHA, fecha);
        params.put(KeyValue.KEY_DIRECCION, direccion);
        params.put(KeyValue.KEY_COORDENADAS, coordenadas);
        params.put(KeyValue.KEY_COBRADOR, String.valueOf(user.getId()));


        //Calling the create hero API
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_CREATE_PAGO_DOMICILIO, params, Api.CODE_POST_REQUEST);
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
            progressBar.setVisibility(View.VISIBLE);
        }


        //this method will give the response from the request
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                    //refreshing the herolist after every operation
                    //so we get an updated list
                    //we will create this method right now it is commented
                    //because we haven't created it yet
                    //refreshHeroList(object.getJSONArray("heroes"));
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
        LocationManager mlocManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(getActivity());
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
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
        //Toast.makeText(getContext(), "localizacion agregada", Toast.LENGTH_SHORT).show();
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
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
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

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        Context context;
        public Context getMainActivity() {
            return context;
        }
        public void setMainActivity(Context context) {
            this.context = context;
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
            setLocation(loc);
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


    @Override
    public void onDismiss(DialogInterface dialog) {
        Toast.makeText(getContext(),"se cerro?",Toast.LENGTH_SHORT).show();
        super.onDismiss(dialog);
        dialog.cancel();
    }

}
