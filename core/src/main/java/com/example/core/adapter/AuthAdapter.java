// Arquivo: com/example/core/adapter/AuthAdapter.java
package com.example.core.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.core.Login;
import com.example.core.R;
import com.example.core.Repository;
import com.example.core.TipoUsuario;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.UserResponse;
import com.example.core.notifications.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Versão B: o cadastro centraliza a persistência no Firestore (Repository.upsertFromAuth)
 * antes de avisar sucesso para a UI. Se o upsert falhar, reverte a conta criada.
 */
public class AuthAdapter {

    private static final String TAG = "AuthAdapter";

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Repository repo = new Repository(); // usado internamente nesta versão B

    // Listener de retorno para a UI
    public interface Listener {
        void onSuccess(String uid);
        void onError(String message);
    }

    /**
     * Cria usuário no FirebaseAuth e persiste o perfil no Firestore, escolhendo a coleção
     * conforme o TipoUsuario (Fornecedor para COMPANY, Produtor para WORKER).
     * Em caso de falha no Firestore, reverte a conta do Auth.
     */
    public void cadastrar(@NonNull String email,
                          @NonNull String senha,
                          @Nullable TipoUsuario tipo,
                          @Nullable Map<String, Object> dadosUsuario,
                          @NonNull Context ctx,
                          @NonNull Listener listener) {

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String msg = (task.getException() != null && task.getException().getMessage() != null)
                                ? task.getException().getMessage()
                                : "Erro no cadastro";
                        listener.onError(msg);
                        return;
                    }

                    FirebaseUser fu = mAuth.getCurrentUser();
                    if (fu == null) {
                        listener.onError("Usuário autenticado não encontrado após cadastro.");
                        return;
                    }

                    // Upsert no Firestore na coleção adequada (Fornecedor/Produtor)
                    repo.upsertFromAuth(fu, dadosUsuario, tipo)
                            .addOnSuccessListener(aVoid -> {
                                // Sucesso total: Auth + Firestore pronto
                                listener.onSuccess(fu.getUid());
                            })
                            .addOnFailureListener(e -> {
                                // Falha em persistir perfil: reverte conta para não ficar órfã
                                String motivo = (e.getMessage() != null) ? e.getMessage() : "Falha ao salvar perfil no Firestore";
                                reverterCadastro(fu.getUid(), motivo, ctx);
                                listener.onError("Falha ao salvar perfil: " + motivo);
                            });
                });
    }

    /**
     * Reverte a conta criada no Auth quando algo crítico falha após o cadastro (ex.: Firestore).
     */
    public void reverterCadastro(@NonNull String uid, @NonNull String motivo, @NonNull Context c) {
        FirebaseUser curr = mAuth.getCurrentUser();
        if (curr != null && uid.equals(curr.getUid())) {
            curr.delete().addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    Log.e(TAG, "Usuário (" + uid + ") DELETADO. Motivo: " + motivo);
                    Toast.makeText(c, "Erro: " + motivo + ". Tente novamente.", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "FALHA CRÍTICA: Não foi possível deletar o usuário (" + uid + "). CONTA ÓRFÃ.");
                    Toast.makeText(c, "Erro crítico no cadastro. Contate o suporte.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.w(TAG, "Usuário já deslogado ou nulo; impossível reverter via Auth.");
            Toast.makeText(c, "Erro ao finalizar o cadastro. Faça login novamente.", Toast.LENGTH_LONG).show();
        }
    }

    // ================================================================
    // As funções abaixo são as que você já tinha. Mantive para compatibilidade
    // com o restante do app. Ajuste conforme seu uso real.
    // ================================================================

    public void login(TipoUsuario tipoUsuario, String email, String senha, Context c) {
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(c, "Login realizado com sucesso..!", Toast.LENGTH_SHORT).show();
                        String uid = (task.getResult() != null && task.getResult().getUser() != null)
                                ? task.getResult().getUser().getUid() : null;
                        if (uid != null) {
                            verificarPerfil(uid, tipoUsuario, c, email);
                        } else {
                            Toast.makeText(c, "Falha ao obter UID do usuário.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(c, "Erro ao realizar login..!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final com.google.firebase.firestore.FirebaseFirestore mFirestore =
            com.google.firebase.firestore.FirebaseFirestore.getInstance();

    public void verificarPerfil(String uid, @Nullable TipoUsuario tipoUsuario, Context c, String email) {
        // Coleção compatível com seu cenário atual:
        String collection;
        if (tipoUsuario == null) {
            collection = "Fornecedor"; // default
        } else {
            collection = (tipoUsuario == TipoUsuario.WORKER) ? "Produtor" : "Fornecedor";
        }

        mFirestore.collection(collection)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        fetchUserFromBackend(email, tipoUsuario, c);
                        Toast.makeText(c, "Perfil encontrado..!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(c, "Perfil não encontrado..!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(c, "Erro ao verificar perfil..!", Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchUserFromBackend(String email, @Nullable TipoUsuario tipoUsuario, Context c) {
        ApiPostgresClient api = com.example.core.network.RetrofitClientPostgres.getApiService(c);

        Call<UserResponse> call = (tipoUsuario == TipoUsuario.WORKER)
                ? api.findWorkerByEmail(email)
                : api.findCompanyByEmail(email);

        call.enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(@NonNull Call<UserResponse> call,
                                             @NonNull Response<UserResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    UserResponse user = resp.body();
                    SharedPreferences prefs = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
                    // padronize tipos (String) para evitar confusão depois
                    prefs.edit()
                            .putInt("user_id", (user.getId() != null) ? user.getId() : -1)
                            .putString("name", user.getName())
                            .putString("email", user.getEmail())
                            .putString("image_url", user.getImageUrl())
                            .putString("tipo_usuario", (tipoUsuario != null) ? tipoUsuario.name() : "")
                            .putString("token", c.getString(R.string.core_api_token))
                            .apply();

                    String title = "Bem-vindo!";
                    String mensagem = "Olá " + user.getName() + ", aproveite seu primeiro acesso.";
                    NotificationHelper.sendNotification(c, title, mensagem, new Intent(c, Login.class));
                    Log.d(TAG, "Notificação de Login enviada com sucesso!");

                    Toast.makeText(c, "Sessão salva com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(c, "Erro ao buscar usuário: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Toast.makeText(c, "Falha de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}