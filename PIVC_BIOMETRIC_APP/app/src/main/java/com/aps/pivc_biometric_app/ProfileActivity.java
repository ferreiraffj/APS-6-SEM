package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileEmail, tvProfilePermissionLevel;
    FirebaseFirestore db;

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

        db.collection("users").get()
                .addOnCompleteListener(task -> {
                  if(task.isSuccessful()){
                      for(QueryDocumentSnapshot document : task.getResult()){
                          String userEmail = document.getString("email");
                          int permissionLevel = document.getLong("permissionLevel").intValue();
                          tvProfileEmail.setText("Usuario: " + userEmail);
                          tvProfilePermissionLevel.setText("Nível de acesso: " + String.valueOf(permissionLevel));
                      }
                  } else {
                      Toast.makeText(this, "Erro ao recuperar informações do usuário", Toast.LENGTH_SHORT).show();
                  }
                });
    }
}