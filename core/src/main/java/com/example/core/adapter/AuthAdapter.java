package com.example.core.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.core.LoginListener;
import com.example.core.TipoUsuario;
import com.example.core.client.ApiPostgresClient;
import com.example.core.dto.response.UserResponse;
import com.example.core.network.RetrofitClientPostgres;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Method;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;

public class AuthAdapter {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


    public interface Listener {
        void onSuccess(String uid);
        void onError(String message);
    }

    // 1. O método 'cadastrar' agora espera que a validação de CPF/CNPJ tenha sido feita no Fragment
    public void cadastrar(String email, String senha, TipoUsuario tipo,
                          Map<String, Object> dadosUsuario, Context ctx,
                          Listener listener) {

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser() != null ? task.getResult().getUser().getUid() : null;
                        listener.onSuccess(uid);
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Erro no cadastro";
                        listener.onError(msg);
                    }
                });
    }

    private void cadastrarUsuario(String uid, TipoUsuario tipoUsuario, Map<String, Object> dadosUsuario, Context c) {

        String collection = tipoUsuario == TipoUsuario.WORKER ? "Produtor" : "Fornecedor";
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
                    reverterCadastro(uid, "Erro ao gravar dados do usuário: " + e.getMessage(), c);
                });
    }

    public void reverterCadastro(String uid, String motivo, Context c) {

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(uid)) {
            mAuth.getCurrentUser().delete()
                    .addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Log.e("REVERSAO_CADASTRO", "Usuário (" + uid + ") DELETADO. Motivo da falha original: " + motivo);
                            Toast.makeText(c, "Erro: " + motivo + ". Por favor, tente novamente.", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("REVERSAO_CADASTRO", "FALHA CRÍTICA: Não foi possível deletar o usuário (" + uid + "). CONTA ÓRFÃ.");
                            Toast.makeText(c, "Erro crítico no cadastro. Contate o suporte para resolver a conta órfã.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Log.w("REVERSAO_CADASTRO", "Usuário já deslogado ou nulo, impossível reverter.");
            Toast.makeText(c, "Erro ao finalizar o cadastro. Tente fazer login ou contate o suporte.", Toast.LENGTH_LONG).show();
        }
    }



    public void login(TipoUsuario tipoUsuario, String email, String senha, Context c, final LoginListener listener) {
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(c, "Login realizado com sucesso..!", Toast.LENGTH_SHORT).show();
                        String uid = task.getResult().getUser().getUid();

                        // Passamos o listener para o próximo passo assíncrono
                        verificarPerfil(uid, tipoUsuario, c, email, listener);
                    } else {
                        // Notifica o Fragment da falha no Firebase
                        String msg = task.getException() != null ? task.getException().getMessage() : "Erro ao realizar login";
                        listener.onFailure(msg);
                    }
                });
    }


    private void verificarPerfil(String uid, TipoUsuario tipoUsuario, Context c, String email, final LoginListener listener){
        String collection = tipoUsuario == TipoUsuario.WORKER ? "Produtor" : "Fornecedor";
        Log.d("AuthAdapter", "Collection para busca do User: "+ collection);
        mFirestore.collection(collection)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
                        Toast.makeText(c, "Perfil encontrado..!", Toast.LENGTH_SHORT).show();
                        // Passamos o listener para o último passo assíncrono
                        fetchUserFromBackend(email, tipoUsuario, c, listener);
                    }
                    else {
                        Toast.makeText(c, "Perfil não encontrado..!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        listener.onFailure("Perfil não encontrado no Firestore."); // Notifica falha
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao verificar perfil..!", Toast.LENGTH_SHORT).show();
                    listener.onFailure("Erro ao verificar perfil: " + e.getMessage()); // Notifica falha
                });
    }


    private void fetchUserFromBackend(String email, TipoUsuario tipoUsuario, Context c, final LoginListener listener) {
        ApiPostgresClient api = RetrofitClientPostgres.getApiService(c);

        Call<UserResponse> call = (tipoUsuario == TipoUsuario.WORKER)
                ? api.findWorkerByEmail(email)
                : api.findCompanyByEmail(email);

        call.enqueue(new retrofit2.Callback<UserResponse>() {
            @Override public void onResponse(@NonNull Call<UserResponse> call,
                                             @NonNull retrofit2.Response<UserResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    UserResponse user = resp.body();

                    // === NOVO: resolver company_id como String e salvar ===
                    String companyIdStr = resolveCompanyIdString(user, tipoUsuario);
                    if (companyIdStr != null && !companyIdStr.trim().isEmpty()) {
                        persistCompanyId(c, companyIdStr.trim());
                        Log.d("AuthAdapter", "company_id persistido: " + companyIdStr.trim());
                    } else {
                        Log.w("AuthAdapter", "company_id não encontrado no UserResponse (tipo=" + tipoUsuario + ").");
                    }

                    // Mantém os demais dados já salvos (compat com código existente)
                    SharedPreferences prefs = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putInt("user_id", user.getId())
                            .putString("name", user.getName())
                            .putString("email", user.getEmail())
                            .putString("tipo_usuario", tipoUsuario.name())
                            .apply();

                    Toast.makeText(c, "Sessão salva com sucesso!", Toast.LENGTH_SHORT).show();


                    listener.onSuccess();

                } else {
                    Toast.makeText(c, "Erro ao buscar usuário: " + resp.code(), Toast.LENGTH_SHORT).show();
                    listener.onFailure("Erro ao buscar dados do usuário: Código " + resp.code()); // Notifica falha
                }
            }
            @Override public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Toast.makeText(c, "Falha de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                listener.onFailure("Falha de rede: " + t.getMessage()); // Notifica falha
            }
        });
    }

    // ======= Helpers novos (mínimos) =======

    /** Salva company_id como String no SharedPreferences "user_session" (padrão esperado). */
    private void persistCompanyId(@NonNull Context c, @NonNull String companyId) {
        SharedPreferences prefs = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("company_id", companyId)
                .apply();
    }

    /**
     * Resolve o company_id como String a partir do UserResponse:
     * - COMPANY: usa user.getId()
     * - WORKER: tenta método getCompanyId() via reflexão (se existir no DTO).
     *   * Não quebra o build caso o DTO não tenha esse método.
     */
    private String resolveCompanyIdString(@NonNull UserResponse user, @NonNull TipoUsuario tipo) {
        if (tipo == TipoUsuario.COMPANY) {
            // Para empresa, o próprio id do usuário/empresa costuma ser o company_id
            return String.valueOf(user.getId());
        } else {
            // Para worker, tentamos obter companyId do DTO (sem exigir no compile)
            Integer companyId = tryGetCompanyIdByReflection(user);
            return (companyId != null) ? String.valueOf(companyId) : null;
        }
    }

    /**
     * Tenta invocar user.getCompanyId() por reflexão (se existir).
     * Retorna null se não existir ou se falhar.
     */
    private Integer tryGetCompanyIdByReflection(@NonNull UserResponse user) {
        try {
            Method m = user.getClass().getMethod("getCompanyId");
            Object v = m.invoke(user);
            if (v == null) return null;
            if (v instanceof Integer) return (Integer) v;
            // caso venha como Long/String
            if (v instanceof Long) return ((Long) v).intValue();
            if (v instanceof String) {
                try { return Integer.parseInt((String) v); } catch (NumberFormatException ignored) {}
            }
            return null;
        } catch (NoSuchMethodException nsme) {
            Log.w("AuthAdapter", "UserResponse não possui getCompanyId().");
            return null;
        } catch (Exception e) {
            Log.w("AuthAdapter", "Falha ao obter companyId via reflexão: " + e.getMessage());
            return null;
        }
    }
}
