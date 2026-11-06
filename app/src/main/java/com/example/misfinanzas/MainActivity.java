package com.example.misfinanzas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.misfinanzas.adaptador.TransaccionAdapter;
import com.example.misfinanzas.modelo.Transaccion;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvTransacciones;
    private TransaccionAdapter adaptador;
    private List<Transaccion> listaTransacciones;

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        listaTransacciones = new ArrayList<>();

        rvTransacciones = findViewById(R.id.rvTransacciones);
        rvTransacciones.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new TransaccionAdapter(listaTransacciones);
        rvTransacciones.setAdapter(adaptador);

        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregar);
        fabAgregar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FormularioActivity.class));
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> cerrarSesion());

        Button btnVerMapa = findViewById(R.id.btnVerMapa);
        btnVerMapa.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapaActivity.class));
        });

        iniciarEscuchaEnTiempoReal();
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("SesionApp", MODE_PRIVATE);
        prefs.edit().clear().apply();

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void iniciarEscuchaEnTiempoReal() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        listenerRegistration = db.collection("Transacciones")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Fallo al escuchar la colección.", error);
                        return;
                    }
                    if (value != null) {
                        listaTransacciones.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Transaccion t = doc.toObject(Transaccion.class);
                            listaTransacciones.add(t);
                        }
                        adaptador.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}