package com.example.misfinanzas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.misfinanzas.modelo.Transaccion;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.content.SharedPreferences;
import java.util.List;
import java.util.Locale;

public class FormularioActivity extends AppCompatActivity {
    private TextInputEditText etConcepto, etMonto;
    private TextInputLayout tilConcepto, tilMonto;
    private RadioButton rbIngreso, rbGasto;
    private Button btnGuardar, btnEliminar;

    private FirebaseFirestore db;
    private String transaccionId = null;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etConcepto = findViewById(R.id.etConcepto);
        etMonto = findViewById(R.id.etMonto);
        tilConcepto = findViewById(R.id.tilConcepto);
        tilMonto = findViewById(R.id.tilMonto);
        rbIngreso = findViewById(R.id.rbIngreso);
        rbGasto = findViewById(R.id.rbGasto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        configurarValidacionesEnTiempoReal();

        // Modo Edición
        if (getIntent().hasExtra("id")) {
            transaccionId = getIntent().getStringExtra("id");
            etConcepto.setText(getIntent().getStringExtra("concepto"));

            double monto = getIntent().getDoubleExtra("monto", 0);
            if (monto % 1 == 0) etMonto.setText(String.valueOf((long) monto));
            else etMonto.setText(String.valueOf(monto));

            if ("Ingreso".equals(getIntent().getStringExtra("tipo"))) rbIngreso.setChecked(true);
            else rbGasto.setChecked(true);

            btnGuardar.setText("Actualizar Transacción");

            SharedPreferences prefs = getSharedPreferences("SesionApp", MODE_PRIVATE);
            String rolUsuario = prefs.getString("rol", "Operario");

            if ("Administrador".equals(rolUsuario)) {
                btnEliminar.setVisibility(View.VISIBLE);
            } else {
                btnEliminar.setVisibility(View.GONE);
            }
        }

        btnGuardar.setOnClickListener(v -> guardarTransaccion());
        btnEliminar.setOnClickListener(v -> eliminarTransaccion());
    }

    private void configurarValidacionesEnTiempoReal() {
        TextWatcher validador = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarCampos(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etConcepto.addTextChangedListener(validador);
        etMonto.addTextChangedListener(validador);
    }

    private boolean validarCampos(boolean mostrarErrores) {
        String concepto = etConcepto.getText().toString().trim();
        String montoString = etMonto.getText().toString().trim();
        boolean valido = true;

        if (concepto.isEmpty() || concepto.length() < 3) {
            if(mostrarErrores) tilConcepto.setError("Debe tener al menos 3 caracteres");
            valido = false;
        } else {
            tilConcepto.setError(null);
        }

        if (montoString.isEmpty() || Double.parseDouble(montoString) <= 0) {
            if(mostrarErrores) tilMonto.setError("El monto debe ser mayor a $0");
            valido = false;
        } else {
            tilMonto.setError(null);
        }
        return valido;
    }

    private void guardarTransaccion() {
        if (!validarCampos(true)) return;
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Obteniendo GPS...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            habilitarBoton("Permiso denegado. Intenta de nuevo.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            double lat = 0.0;
            double lng = 0.0;
            String direccion = "Ubicación desconocida";

            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();

                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> direcciones = geocoder.getFromLocation(lat, lng, 1);
                    if (direcciones != null && !direcciones.isEmpty()) {
                        direccion = direcciones.get(0).getAddressLine(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            subirAFirestore(lat, lng, direccion);
        }).addOnFailureListener(e -> {
            subirAFirestore(0.0, 0.0, "Sin ubicación");
        });
    }

    private void subirAFirestore(double lat, double lng, String direccion) {
        String concepto = etConcepto.getText().toString().trim();
        double monto = Double.parseDouble(etMonto.getText().toString().trim());
        String tipo = rbIngreso.isChecked() ? "Ingreso" : "Gasto";

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Transaccion transaccion = new Transaccion(concepto, monto, tipo, lat, lng, direccion, uid);

        if (transaccionId == null) {
            db.collection("Transacciones").add(transaccion)
                    .addOnSuccessListener(documentReference -> {
                        mostrarNotificacionLocal("Nueva Transacción", "Guardada en: " + direccion);
                        finalizarFormulario("Guardado correctamente");
                    })
                    .addOnFailureListener(e -> habilitarBoton("Error al guardar"));
        } else {
            db.collection("Transacciones").document(transaccionId).set(transaccion)
                    .addOnSuccessListener(aVoid -> {
                        mostrarNotificacionLocal("Transacción Actualizada", "Actualizada exitosamente.");
                        finalizarFormulario("Actualizado correctamente");
                    })
                    .addOnFailureListener(e -> habilitarBoton("Error al actualizar"));
        }
    }

    private void eliminarTransaccion() {
        if (transaccionId != null) {
            btnEliminar.setEnabled(false);
            db.collection("Transacciones").document(transaccionId).delete()
                    .addOnSuccessListener(aVoid -> finalizarFormulario("Eliminado correctamente"))
                    .addOnFailureListener(e -> btnEliminar.setEnabled(true));
        }
    }

    private void habilitarBoton(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        btnGuardar.setEnabled(true);
        btnGuardar.setText(transaccionId == null ? "Guardar Transacción" : "Actualizar Transacción");
    }

    private void finalizarFormulario(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarNotificacionLocal(String titulo, String mensaje) {
        String canalId = "canal_finanzas";
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(canalId, "Notificaciones de Transacciones", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(canal);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, canalId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        nm.notify(1, builder.build());
    }
}