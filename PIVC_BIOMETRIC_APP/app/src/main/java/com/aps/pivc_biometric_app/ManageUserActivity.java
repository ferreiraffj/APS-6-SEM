package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class ManageUserActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonSearch;
    private TextView textViewResult;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonSearch = findViewById(R.id.buttonSearch);
        textViewResult = findViewById(R.id.textViewResult);
        db = FirebaseFirestore.getInstance();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserByEmail();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void searchUserByEmail() {
        String email = editTextEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um e-mail.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users") // Substitua "usuarios" pelo nome da sua coleção
                .whereEqualTo("email", email) // Substitua "email" pela chave que você usa para armazenar o e-mail
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean userFound = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userFound = true;
                            String userEmail = document.getString("email");
                            int nvlPermission = Objects.requireNonNull(document.getLong("permissionLevel")).intValue();
                            // Aqui você pode pegar outras informações do usuário, se necessário
                            textViewResult.setText("Usuário encontrado: " + userEmail + " (. Nível: " + nvlPermission + ")");
                        }
                        if (!userFound) {
                            textViewResult.setText("Nenhum usuário encontrado com esse e-mail.");
                        }
                    } else {
                        Toast.makeText(this, "Erro ao buscar usuário.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}