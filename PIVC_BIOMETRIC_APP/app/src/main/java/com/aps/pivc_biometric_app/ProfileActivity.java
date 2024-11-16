package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileEmail, tvProfilePermissionLevel;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePermissionLevel = findViewById(R.id.tvProfilePermissionLevel);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String currentUserUID = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        db.collection("users").document(currentUserUID)
                .get()
                .addOnCompleteListener(task -> {
                  if(task.isSuccessful()){
                      DocumentSnapshot document = task.getResult();
                      if (document.exists()){
                          String userEmail = document.getString("email");
                          int permissionLevel = document.getLong("permissionLevel").intValue();

                          tvProfileEmail.setText("Usuario: " + userEmail);
                          tvProfilePermissionLevel.setText("Nível de acesso: " + String.valueOf(permissionLevel));
                      } else {
                          Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                      }
                  } else {
                      Toast.makeText(this, "Erro ao recuperar informações do usuário", Toast.LENGTH_SHORT).show();
                  }
                });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
}