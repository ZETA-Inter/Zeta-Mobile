package com.example.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthAdapter {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


    public void cadastrar(String email, String senha, TipoUsuario tipoUsuario, Map<String, Object> dadosUsuario, Context c) throws Exception {
        mAuth.createUserWithEmailAndPassword(email, senha)
                        .addOnCompleteListener(task ->{
                            if (task.isSuccessful()){
                                String uid = task.getResult().getUser().getUid();
                                try {
                                    cadastrarUsuario(uid, tipoUsuario, dadosUsuario, c);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            else {
                                // --- NOVO CÓDIGO AQUI ---
                                if (task.getException() != null) {
                                    // 1. IMPRIME O ERRO NO LOGCAT: Filtre por "FIREBASE_AUTH" no Android Studio
                                    Log.e("FIREBASE_AUTH", "Falha na criação da conta: " + task.getException().getMessage());

                                    // 2. MOSTRA O ERRO AO USUÁRIO
                                    String mensagemErro = "Falha no Cadastro: " + task.getException().getLocalizedMessage();
                                    Toast.makeText(c, mensagemErro, Toast.LENGTH_LONG).show();
                                } else {
                                    // Caso de erro inesperado
                                    Toast.makeText(c, "Erro desconhecido ao cadastrar.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }


    private void cadastrarUsuario(String uid, TipoUsuario tipoUsuario, Map<String, Object> dadosUsuario, Context c) throws Exception {
        String collection = tipoUsuario == TipoUsuario.WORKER ? "worker" : "company";
        String campo = tipoUsuario == TipoUsuario.WORKER ? "cpf" : "cnpj";

        String valorDoc = (String) dadosUsuario.get(campo);
        if (valorDoc == null || valorDoc.isEmpty()){
            throw new Exception("Campo " + campo + " deve ser preenchido");
        }

        Map<String, Object> doc = new HashMap<>(dadosUsuario);
        mFirestore.collection(collection)
                .document(uid)
                .set(dadosUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(c, "Usuario cadastrado com sucesso..!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Erro ao cadastrar usuario..!", Toast.LENGTH_SHORT).show();
                });

    }

    public void login(TipoUsuario tipoUsuario, String email, String senha, Context c){
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(c, "Login realizado com sucesso..!", Toast.LENGTH_SHORT).show();
                        String uid = task.getResult().getUser().getUid();
                        verificarPerfil(uid, tipoUsuario, c);
                    } else {
                        Toast.makeText(c, "Erro ao realizar login..!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarPerfil(String uid, TipoUsuario tipoUsuario, Context c){
        String collection = tipoUsuario == TipoUsuario.WORKER ? "worker" : "company";
        mFirestore.collection(collection)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()){
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

















//    public void salvar(Veiculo veiculo, Context c) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("veiculo").document().set(veiculo);
//        if (veiculo.getPlaca() != null) {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Toast.makeText(c, "Veiculo cadastrado com sucesso..!", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(c, "Erro ao cadastrar veiculo..!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//        } else {
//            Toast.makeText(c, "Digite algo para realizar o cadastro", Toast.LENGTH_SHORT).show();
//        }
//    }
}
