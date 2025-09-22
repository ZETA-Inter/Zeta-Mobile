package com.example.core;

import android.util.Patterns;

public class Validators {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("\\D+", "");
        return digits.length() >= 10 && digits.length() <= 11; // BR
    }

    public static boolean isStrongPassword(String s) {
        if (s == null || s.length() < 6) return false; // regra mínima do Auth
        boolean hasLetter = s.matches(".*[A-Za-z].*");
        boolean hasDigit  = s.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }

    //Validação oficial de CNPJ (com dígitos verificadores).
    public static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null) return false;
        String n = cnpj.replaceAll("\\D+", "");
        if (n.length() != 14) return false;
        if (n.matches("(\\d)\\1{13}")) return false;

        int d1 = calcDigit(n.substring(0,12), new int[]{5,4,3,2,9,8,7,6,5,4,3,2});
        int d2 = calcDigit(n.substring(0,12) + d1, new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2});
        return n.equals(n.substring(0,12) + d1 + d2);
    }

    private static int calcDigit(String base, int[] weight) {
        int sum = 0, offset = weight.length - base.length();
        for (int i = 0; i < base.length(); i++) sum += (base.charAt(i) - '0') * weight[i + offset];
        int mod = sum % 11;
        return (mod < 2) ? 0 : 11 - mod;
    }
}
