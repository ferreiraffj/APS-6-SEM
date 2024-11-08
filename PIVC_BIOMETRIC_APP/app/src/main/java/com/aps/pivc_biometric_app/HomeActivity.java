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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aps.pivc_biometric_app.adapter.ContentAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int userPermissionLevel;
    Button btnSairParaLogin, btnAddContent;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;
    private List<DocumentSnapshot> contentList;

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

        // Inicialização da toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Buscando instâncias do firebase
        mAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        // Buscando permissões do usuário logado
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        userPermissionLevel = sharedPreferences.getInt("permissionLevel", 1);
        Toast.makeText(this, "Nível de acesso: " + userPermissionLevel, Toast.LENGTH_SHORT).show();

        recyclerView = findViewById(R.id.recyclerViewContents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contentList = new ArrayList<>();
        adapter = new ContentAdapter(this, contentList);
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("HomeActivity", "Usuário deslogado, redirecionando para MainActivity.");
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Carrega conteúdos do Firestore
        loadContentsFromFirestore();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomeActivity.this)
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
            }
        });
    }

    // Configuraçãdo do menu da Toolbar
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

    // Acções para os itens de menu da toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addContentToolbar){
            startActivity(new Intent(HomeActivity.this, CreateContentActivity.class));
            return true;
        }
        if (id == R.id.logoutToolbar){
            performLogout();
            return true;
        }
        if(id == R.id.editPermissionToolbar){
            startActivity(new Intent(HomeActivity.this, ManageUserActivity.class));
            finish();
        }
        if(id == R.id.perfilToolbar){
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout(){
        FirebaseAuth.getInstance().signOut();
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.remove("email");
        editor.remove("password");
        editor.putBoolean("isLogin", false);
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
    }

    private void loadContentsFromFirestore(){
        db.collection("contents")
                .whereLessThanOrEqualTo("permissionLevel", userPermissionLevel)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    contentList.clear();

                    for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                        int contentPermissionLevel = doc.getLong("permissionLevel").intValue();
                        if (contentPermissionLevel <= userPermissionLevel){
                            contentList.add(doc);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Erro ao carregar conteúdos" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}