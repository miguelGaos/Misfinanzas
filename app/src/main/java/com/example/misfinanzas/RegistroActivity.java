package com.example.misfinanzas;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    private TextInputEditText etCorreoReg, etPassReg;
    private RadioButton rbAdmin;
    private Button btnRegistrar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etCorreoReg = findViewById(R.id.etCorreoReg);
        etPassReg = findViewById(R.id.etPassReg);
        rbAdmin = findViewById(R.id.rbAdmin);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String correo = etCorreoReg.getText().toString().trim();
        String pass = etPassReg.getText().toString().trim();
        String rol = rbAdmin.isChecked() ? "Administrador" : "Operario";

        if (correo.isEmpty() || pass.length() < 6) {
            Toast.makeText(this, "Correo inválido o contraseña corta", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando...");

        auth.createUserWithEmailAndPassword(correo, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("correo", correo);
                usuario.put("rol", rol);

                // Guardar el rol en Firestore usando el UID del usuario
                db.collection("Usuarios").document(userId).set(usuario).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra y vuelve al Login
                });
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                btnRegistrar.setEnabled(true);
                btnRegistrar.setText("Crear Cuenta");
            }
        });
    }
}