package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateContentActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextPreview, editTextPermissionLevel, editTextFullContent;
    private Button btnSaveContent;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_content);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextPreview = findViewById(R.id.editTextPreview);
        editTextPermissionLevel = findViewById(R.id.editTextPermissionLevel);
        editTextFullContent = findViewById(R.id.editTextFullContent);
        btnSaveContent = findViewById(R.id.btnSaveContent);

        db = FirebaseFirestore.getInstance();

        btnSaveContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveContent();
            }
        });
    }

    private void saveContent(){
        String title = editTextTitle.getText().toString();
        String preview = editTextPreview.getText().toString();
        String fullContent = editTextFullContent.getText().toString();
        int permissionLevel;

        try {
            permissionLevel = Integer.parseInt(editTextPermissionLevel.getText().toString());
        } catch (NumberFormatException e){
            Toast.makeText(this, "Insira um nível de permissão válido (1 a 4)", Toast.LENGTH_SHORT).show();
            return;
        }

        if(title.isEmpty() || preview.isEmpty() || fullContent.isEmpty()){
            Toast.makeText(this, "Título e prévia são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparação para salvar dados no Firestore
        Map<String, Object> content = new HashMap<>();
        content.put("title", title);
        content.put("preview", preview);
        content.put("permissionLevel", permissionLevel);
        content.put("fullContent", fullContent); // Adiciona o campo fullContent

        db.collection("contents")
                .add(content)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateContentActivity.this, "Contéudo salvo com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                   Toast.makeText(CreateContentActivity.this, "Erro ao salvar conteúdo" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}