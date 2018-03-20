package owl.app.limpia_publica.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import owl.app.limpia_publica.R;
import owl.app.limpia_publica.adapters.MenuAdapter;
import owl.app.limpia_publica.adapters.MenuOnItemClickListener;
import owl.app.limpia_publica.fragments.DomicilioFragment;
import owl.app.limpia_publica.models.Cobrador;
import owl.app.limpia_publica.models.Opciones;
import owl.app.limpia_publica.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    private List<Opciones> menuList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar myToolbar;
    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_menu);
        this.menuList = getMenuList();

        layoutManager = new LinearLayoutManager(this);
        adapter = new MenuAdapter(menuList, R.layout.card_view_menu, new MenuOnItemClickListener() {

            @Override
            public void onItemClick(Opciones menu, int position) {

                switch (position){
                    case 0:
                        /*
                        manager = getSupportFragmentManager();
                        DomicilioFragment domicilioFragment = new DomicilioFragment();
                        domicilioFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.transparente);
                        domicilioFragment.show(manager,"fragment_dialog");*/

                        Intent intentDomicilio = new Intent(MainActivity.this, DomicilioActivity.class);
                        startActivity(intentDomicilio);
                        break;
                    case 1:
                        Intent intent = new Intent(MainActivity.this, ComercioActivity.class);
                        startActivity(intent);
                        break;

                }
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private List<Opciones> getMenuList(){
        ArrayList<Opciones> menuArrayList = new ArrayList(){{
            add(new Opciones("Domicilio", R.mipmap.ic_home));
            add(new Opciones("Comercio", R.mipmap.ic_store));
        }};

        return menuArrayList;
    }

    private void setToolbar() {
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.cerrarSesion:
                showInfoAlert();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showInfoAlert() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Cerrar Sesión?")
                .setMessage("Seguro que desea cerrar sesion?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPrefManager.getInstance(getApplicationContext()).logout();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

}
