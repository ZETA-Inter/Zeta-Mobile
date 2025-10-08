package com.example.core.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.core.TipoUsuario;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.UserResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthAdapter {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    private Retrofit retrofit;

    // 1. O método 'cadastrar' agora espera que a validação de CPF/CNPJ tenha sido feita no Fragment
    public void cadastrar(String email, String senha, TipoUsuario tipoUsuario, Map<String, Object> dadosUsuario, Context c) {
        // Toda a lógica de validação de CPF/CNPJ foi removida daqui.

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful()){
                        String uid = task.getResult().getUser().getUid();
                        // Removido o try/catch e o throw RuntimeException
                        cadastrarUsuario(uid, tipoUsuario, dadosUsuario, c);

                    }
                    else {
                        if (task.getException() != null) {
                            Log.e("FIREBASE_AUTH", "Falha na criação da conta: " + task.getException().getMessage());
                            String mensagemErro = "Falha no Cadastro: " + task.getException().getLocalizedMessage();
                            Toast.makeText(c, mensagemErro, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(c, "Erro desconhecido ao cadastrar.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void cadastrarUsuario(String uid, TipoUsuario tipoUsuario, Map<String, Object> dadosUsuario, Context c) {
        String collection = tipoUsuario == TipoUsuario.WORKER ? "Produtor" : "Fornecedor";

        // MELHORIA DE SEGURANÇA: REMOVE A SENHA DO MAPA ANTES DE SALVAR NO FIRESTORE!
        if (dadosUsuario.containsKey("Senha")) {
            dadosUsuario.remove("Senha");
        }

        mFirestore.collection(collection)
                .document(uid)
                .set(dadosUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(c, "Usuario cadastrado com sucesso..!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // SE O FIRESTORE FALHAR, CHAMA A FUNÇÃO DE REVERSÃO
                    reverterCadastro(uid, "Erro ao gravar dados do usuário: " + e.getMessage(), c);
                    // Removemos o Toast redundante para que a reversão exiba a mensagem detalhada
                });
    }

    // 2. Método 'reverterCadastro' para limpar contas órfãs
    public void reverterCadastro(String uid, String motivo, Context c) {
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(uid)) {
            mAuth.getCurrentUser().delete()
                    .addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            // SUCESSO: O app limpou a conta órfã
                            Log.e("REVERSAO_CADASTRO", "Usuário (" + uid + ") DELETADO. Motivo da falha original: " + motivo);
                            Toast.makeText(c, "Erro: " + motivo + ". Por favor, tente novamente.", Toast.LENGTH_LONG).show();
                        } else {
                            // FALHA CRÍTICA: A conta órfã permaneceu
                            Log.e("REVERSAO_CADASTRO", "FALHA CRÍTICA: Não foi possível deletar o usuário (" + uid + "). CONTA ÓRFÃ.");
                            Toast.makeText(c, "Erro crítico no cadastro. Contate o suporte para resolver a conta órfã.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Log.w("REVERSAO_CADASTRO", "Usuário já deslogado ou nulo, impossível reverter.");
            Toast.makeText(c, "Erro ao finalizar o cadastro. Tente fazer login ou contate o suporte.", Toast.LENGTH_LONG).show();
        }
    }

    public void login(TipoUsuario tipoUsuario, String email, String senha, Context c){
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(c, "Login realizado com sucesso..!", Toast.LENGTH_SHORT).show();
                        String uid = task.getResult().getUser().getUid();
                        verificarPerfil(uid, tipoUsuario, c, email);
                    } else {
                        Toast.makeText(c, "Erro ao realizar login..!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarPerfil(String uid, TipoUsuario tipoUsuario, Context c, String email){
        // AVISO: As collections 'worker' e 'company' devem bater com 'Produtor' e 'Fornecedor'
        String collection = tipoUsuario == TipoUsuario.WORKER ? "Produtor" : "Fornecedor";
        Log.d("AuthAdapter", "Collection para busca do User: "+ collection);
        mFirestore.collection(collection)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        fetchUserFromBackend(email, tipoUsuario, c);
                        Toast.makeText(c, "Perfil encontrado..!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(c, "Perfil nao encontrado..!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao verificar perfil..!", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserFromBackend(String email, TipoUsuario tipoUsuario, Context c) {
        String url = "https://api-postgresql-zeta-fide.onrender.com";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS) // tempo para conectar
                .readTimeout(30, TimeUnit.SECONDS)    // tempo para esperar resposta
                .writeTimeout(20, TimeUnit.SECONDS)   // tempo para enviar dados
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiPostgresClient postgresClient = retrofit.create(ApiPostgresClient.class);

        Call<UserResponse> call = tipoUsuario == TipoUsuario.WORKER ?
                postgresClient.findWorkerByEmail(email) :
                postgresClient.findCompanyByEmail(email);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    UserResponse user = response.body();

                    try {
                        SharedPreferences prefs = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", user.getId());
                        editor.putString("name", user.getName());
                        editor.putString("email", user.getEmail());
                        editor.putString("tipo_usuario", tipoUsuario.name());
                        editor.apply();

                    } catch (Exception ex) {
                        Log.e("SESSION", "Erro ao salvar sessão: " + ex.getMessage());
                        Toast.makeText(c, "Erro ao salvar sessão local.", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(c, "Sessão salva com sucesso!", Toast.LENGTH_SHORT).show();
                    Log.d("SESSION", "Usuário salvo: " + user.getName() + " (ID: " + user.getId() + ")");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText(c, "Erro ao carregar User infos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}