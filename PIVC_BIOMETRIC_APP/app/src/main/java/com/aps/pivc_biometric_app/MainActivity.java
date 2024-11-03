package com.aps.pivc_biometric_app;

import static android.content.ContentValues.TAG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101010;
    ImageView imageViewLogin;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    EditText  inputEmail, inputPassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageViewLogin=findViewById(R.id.imageView);

        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        btnLogin=findViewById(R.id.btnlogin);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        progressDialog=new ProgressDialog(this);
        registerText=findViewById(R.id.registerText);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=inputEmail.getText().toString();
                String password=inputPassword.getText().toString();
                PeformAuth(email, password);
            }
        });

        registerText.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        sharedPreferences=getSharedPreferences("data", MODE_PRIVATE);
        boolean isLogin=sharedPreferences.getBoolean("isLogin", false);
        if (!isLogin){
            imageViewLogin.setVisibility(View.GONE);
        } else {
            imageViewLogin.setVisibility(View.VISIBLE);
        }

        // Função gerenciadora de biometria
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "Dispositivo suporta autenticação via biometria.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Autenticação via biometria não está disponível neste dispostivo", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Sensor indiposível ou em uso.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Erro na autenticação: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String email=sharedPreferences.getString("email", "");
                String password=sharedPreferences.getString("password", "");
                PeformAuth(email, password);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Autenticação falhou",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login com biometria")
                .setSubtitle("Use sua biometria para realizar o login")
                .setNegativeButtonText("Usar senha de desbloqueio")
                .build();

        imageViewLogin.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });

        String savedEmail = sharedPreferences.getString("email", "");
        if (!savedEmail.isEmpty()) {
            inputEmail.setText(savedEmail);  // Preenche automaticamente o e-mail
        }


    }
    private void PeformAuth(String email, String password) {
        progressDialog.setMessage("Login");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        Log.d(TAG, "Usuário autenticado com ID: " + userId);

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Long permissionLevel = document.getLong("permissionLevel");
                                                if (permissionLevel != null) {
                                                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                                                    editor.putString("email", email);
                                                    editor.putString("password", password);
                                                    editor.putInt("permissionLevel", permissionLevel.intValue());
                                                    editor.putBoolean("isLogin", true);
                                                    editor.apply();

                                                    Toast.makeText(MainActivity.this,
                                                            "Nível de permissão: " + permissionLevel,
                                                            Toast.LENGTH_SHORT).show();

                                                    imageViewLogin.setVisibility(View.VISIBLE);
                                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                } else {
                                                    Log.d(TAG, "Campo 'permissionLevel' ausente no documento.");
                                                    Toast.makeText(MainActivity.this, "Erro: Campo 'permissionLevel' ausente.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Log.d(TAG, "Documento de usuário não encontrado.");
                                                Toast.makeText(MainActivity.this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.w(TAG, "Erro ao recuperar documento de permissão.", task.getException());
                                            Toast.makeText(MainActivity.this, "Erro ao recuperar permissões", Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                } else {
                    Log.w(TAG, "Erro ao autenticar.", task.getException());
                    Toast.makeText(MainActivity.this, "Erro ao autenticar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

}