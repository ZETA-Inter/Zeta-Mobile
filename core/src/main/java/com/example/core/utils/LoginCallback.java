package com.example.core.utils;

public interface LoginCallback {
    void onLoginSuccess();
    void onLoginFailure(String mensagem);
}
