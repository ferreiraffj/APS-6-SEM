package com.aps.pivc_biometric_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int userPermissionLevel;
    private int nvlPermission;
    private String userId; // Para armazenar o ID do documento do usuário
    Button btnSairParaLogin;

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        userPermissionLevel = sharedPreferences.getInt("permissionLevel", 1);
        Toast.makeText(this, "Nível de acesso: " + userPermissionLevel, Toast.LENGTH_SHORT).show();

        btnSairParaLogin=findViewById(R.id.btnSairParaLogin);
        btnSairParaLogin.setOnClickListener(view -> {
            Toast.makeText(this, "Você escolheu sair", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem editPermissionItem = menu.findItem(R.id.editPermissionToolbar);
        if(editPermissionItem != null){
            editPermissionItem.setVisible(userPermissionLevel == 4);
        }
        Toast.makeText(this, "Nível de permissão: " + userPermissionLevel, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.perfilToolbar){
            Toast.makeText(this, "Perfil.", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.editPermissionToolbar){
            Toast.makeText(this, "Gerenciamento de permissões.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, ManageUserActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        // Verificação de toolbar aberta
        if(isToolbarVisible()){
            new AlertDialog.Builder(this)
                    .setTitle("Sair do app")
                    .setMessage("Você deseja realmente sair?")
                    .setPositiveButton("Sim", (dialog, which) ->{
                        //Realiza o logout
                        FirebaseAuth.getInstance().signOut();

                        // Limpa as informações de login
                        SharedPreferences.Editor editor = (SharedPreferences.Editor) getSharedPreferences("data", MODE_PRIVATE);
                        editor.remove("email");
                        editor.remove("password");
                        editor.putBoolean("isLogin", false);
                        editor.apply();

                        // Redirecionamento a tela de login
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isToolbarVisible(){
        return toolbar.getVisibility() == View.VISIBLE;
    }
}