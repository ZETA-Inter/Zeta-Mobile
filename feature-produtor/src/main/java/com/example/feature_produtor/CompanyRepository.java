package com.example.zeta_mobile.company;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class CompanyRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentReference ref(String uid) {
        return db.collection("company").document(uid);
    }

    // Cria/atualiza o doc da company a partir do usuário do Auth.
    public Task<Void> upsertFromAuth(FirebaseUser fu, @Nullable Map<String, Object> extra) {
        DocumentReference r = ref(fu.getUid());

        Map<String, Object> base = new HashMap<>();
        base.put("ownerCompany", fu.getUid());
        base.put("name", fu.getDisplayName());  // pode ser null no email/senha
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

    // Atualiza (merge) CNPJ e telefone.
    public Task<Void> updateContact(String uid, String cnpj, String phone) {
        Map<String, Object> m = new HashMap<>();
        m.put("cnpj", cnpj);
        m.put("phone", phone);
        return db.collection("company").document(uid).set(m, SetOptions.merge());
    }

}
