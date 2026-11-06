package com.example.misfinanzas;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.misfinanzas.modelo.Transaccion;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        db.collection("Transacciones").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Transaccion t = doc.toObject(Transaccion.class);

                if (t.getLatitud() != 0.0 && t.getLongitud() != 0.0) {
                    LatLng posicion = new LatLng(t.getLatitud(), t.getLongitud());

                    mMap.addMarker(new MarkerOptions()
                            .position(posicion)
                            .title(t.getConcepto() + " ($" + t.getMonto() + ")")
                            .snippet(t.getDireccion())); // InfoWindow al tocar el pin

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 12));
                }
            }
        });
    }
}