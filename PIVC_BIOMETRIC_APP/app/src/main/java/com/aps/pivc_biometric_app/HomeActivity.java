package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

//    Button btn_exit;
    Button btnUsuarios;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnUsuarios = findViewById(R.id.btnUsuarios);
        btnUsuarios.setOnClickListener(view -> {
            Log.d("HomeActivity", "Botão de usuários clicado");
            startActivity(new Intent(HomeActivity.this, ManageUserActivity.class));
            finish();
        });

//        btn_exit=findViewById(R.id.btn_exit);
//
//        btn_exit.setOnClickListener(view -> {
//            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
//            startActivity(intent);
//        });
    }


}