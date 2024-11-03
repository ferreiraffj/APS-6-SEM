package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class ManageUserActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonSearch, btnBackToHome;
    private TextView textViewResult;
    private FirebaseFirestore db;
    private String userEmail;
    private int nvlPermission;
    private String userId; // Para armazenar o ID do documento do usuário

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonSearch = findViewById(R.id.buttonSearch);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        textViewResult = findViewById(R.id.textViewResult);
        db = FirebaseFirestore.getInstance();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserByEmail();
                textViewResult.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEditPermissionDialog(nvlPermission);
                    }
                });
            }
        });

        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManageUserActivity.this, HomeActivity.class));
                finish();
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

        db.collection("users") // nome da sua coleção
                .whereEqualTo("email", email) // chave de armazenamento de e-mail
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean userFound = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userFound = true;
                            String userEmail = document.getString("email");
                            int nvlPermission = Objects.requireNonNull(document.getLong("permissionLevel")).intValue();
                            userId = document.getId();
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

    private void showEditPermissionDialog(int currentPermissionLevel){
        View dialogView = getLayoutInflater().inflate(R.layout.activity_user_detail, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Configuração do Spinner
         @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Spinner spinnerPermissionLevels = dialogView.findViewById(R.id.spinnerPermissionLevels);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.permission_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerPermissionLevels.setAdapter(adapter);

        // Define o valor atual do Spinner
        spinnerPermissionLevels.setSelection(currentPermissionLevel - 1);

        // Configuração do botão de atualização
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button buttonUpdatePermission = dialogView.findViewById(R.id.buttonUpdatePermission);

        buttonUpdatePermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newPermissionLevel = Integer.parseInt((String) spinnerPermissionLevels.getSelectedItem());
                updatePermissionLevel(newPermissionLevel);
            }
        });

        builder.setTitle("Editar nível de permissão")
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updatePermissionLevel(int newPermissionLevel){
        if (userId == null){
            Toast.makeText(this, "ID do usuário não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("permissionLevel", newPermissionLevel)
                .addOnSuccessListener(aVoid ->{
                    Toast.makeText(ManageUserActivity.this, "Permissão atualizada com sucesso", Toast.LENGTH_SHORT).show();
                    textViewResult.setText(String.valueOf(newPermissionLevel));
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(ManageUserActivity.this, "Erro ao atualizar permissão" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}