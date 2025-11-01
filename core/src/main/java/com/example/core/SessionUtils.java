// com/example/core/SessionUtils.java
package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionUtils {
    private SessionUtils() {}

    public static int getUserId(Context c) {
        SharedPreferences sp = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return sp.getInt("user_id", -1);
    }

    public static String getTipoUsuario(Context c) {
        SharedPreferences sp = c.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return sp.getString("tipo_usuario", "");
    }
}
