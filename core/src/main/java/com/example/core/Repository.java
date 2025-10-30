package com.example.core;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Repository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ===== Novo: escolhe a coleção conforme o tipo =====
    private DocumentReference ref(String uid, @Nullable TipoUsuario tipo) {
        // Se não informado, mantém o comportamento antigo (Fornecedor)
        String collection = "Fornecedor";
        if (tipo != null) {
            collection = (tipo == TipoUsuario.WORKER) ? "Produtor" : "Fornecedor";
        }
        return db.collection(collection).document(uid);
    }

    // ===== Mantido p/ compatibilidade: continua salvando em 'Fornecedor' =====
    private DocumentReference ref(String uid) {
        return db.collection("Fornecedor").document(uid);
    }

    // ===== Novo: upsert com TipoUsuario (recomendado usar este) =====
    public Task<Void> upsertFromAuth(FirebaseUser fu, @Nullable Map<String, Object> dados, @Nullable TipoUsuario tipo) {
        DocumentReference r = ref(fu.getUid(), tipo);

        return db.runTransaction(tr -> {
            DocumentSnapshot snap = tr.get(r);
            assert dados != null;
            Map<String, Object> toSet = new HashMap<>(dados);
            if (!snap.exists() || !snap.contains("createdAt")) {
                toSet.put("createdAt", FieldValue.serverTimestamp());
            }
            tr.set(r, toSet, SetOptions.merge());
            return null;
        });
    }

    // ===== Mantido p/ compatibilidade: continua usando 'Fornecedor' se chamarem esta versão =====
    public Task<Void> upsertFromAuth(FirebaseUser fu, @Nullable Map<String, Object> extra) {
        DocumentReference r = ref(fu.getUid());

        Map<String, Object> base = new HashMap<>();
        base.put("ownerUid", fu.getUid());
        base.put("name", fu.getDisplayName());
        base.put("email", fu.getEmail());
        base.put("lastLoginAt", FieldValue.serverTimestamp());

        if (extra != null) base.putAll(extra);

        return db.runTransaction(tr -> {
            DocumentSnapshot snap = tr.get(r);
            Map<String, Object> toSet = new HashMap<>(base);
            if (!snap.exists() || !snap.contains("createdAt")) {
                toSet.put("createdAt", FieldValue.serverTimestamp());
            }
            tr.set(r, toSet, SetOptions.merge());
            return null;
        });
    }
}
