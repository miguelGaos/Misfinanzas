package com.example.misfinanzas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etCorreoLogin, etPassLogin;
    private Button btnLogin;
    private TextView tvIrRegistro;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("SesionApp", MODE_PRIVATE);
        if (prefs.getBoolean("isLogged", false)) {
            iniciarMainActivity();
        }

        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etCorreoLogin = findViewById(R.id.etCorreoLogin);
        etPassLogin = findViewById(R.id.etPassLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);

        tvIrRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        btnLogin.setOnClickListener(v -> iniciarSesion());
    }

    private void iniciarSesion() {
        String correo = etCorreoLogin.getText().toString().trim();
        String pass = etPassLogin.getText().toString().trim();

        if (correo.isEmpty() || pass.isEmpty()) return;

        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando...");

        auth.signInWithEmailAndPassword(correo, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                // Obtener el rol del usuario desde Firestore
                db.collection("Usuarios").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    String rol = documentSnapshot.getString("rol");

                    // Guardar sesión y rol en SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLogged", true);
                    editor.putString("rol", rol);
                    editor.apply();

                    iniciarMainActivity();
                });
            } else {
                Toast.makeText(this, "Error de credenciales", Toast.LENGTH_SHORT).show();
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");
            }
        });
    }

    private void iniciarMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
