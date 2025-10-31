package com.example.core;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

public final class MaskUtils {

    private MaskUtils() {}

    // === Máscaras ===
    public static void attachCpfMask(@NonNull EditText et) {
        et.addTextChangedListener(new SimpleMaskTextWatcher(et, "###.###.###-##", 11));
    }

    public static void attachCnpjMask(@NonNull EditText et) {
        et.addTextChangedListener(new SimpleMaskTextWatcher(et, "##.###.###/####-##", 14));
    }

    // === Limita apenas números ===
    public static void applyMaxDigits(@NonNull EditText et, int maxDigits) {
        et.setFilters(new InputFilter[]{ new DigitsLengthFilter(maxDigits) });
    }

    /** Filtro para garantir número máximo de dígitos (ignorando máscara) */
    static class DigitsLengthFilter implements InputFilter {
        private final int max;
        DigitsLengthFilter(int max){ this.max = max; }

        @Override
        public CharSequence filter(CharSequence src, int start, int end,
                                   android.text.Spanned dest, int dstart, int dend) {
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < dest.length(); i++) {
                char c = dest.charAt(i);
                if (Character.isDigit(c)) digits.append(c);
            }
            for (int i = start; i < end; i++) {
                char c = src.charAt(i);
                if (Character.isDigit(c)) digits.append(c);
            }
            return (digits.length() <= max) ? null : "";
        }
    }

    /** TextWatcher para formatar CPF/CNPJ conforme o usuário digita */
    static class SimpleMaskTextWatcher implements TextWatcher {
        private final EditText et;
        private final String pattern; // Ex: ###.###.###-##
        private final int maxDigits;
        private boolean isUpdating;

        SimpleMaskTextWatcher(EditText et, String pattern, int maxDigits) {
            this.et = et;
            this.pattern = pattern;
            this.maxDigits = maxDigits;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isUpdating) return;

            String digits = s.toString().replaceAll("\\D+", "");
            if (digits.length() > maxDigits) digits = digits.substring(0, maxDigits);

            StringBuilder out = new StringBuilder();
            int di = 0;
            for (int i = 0; i < pattern.length() && di < digits.length(); i++) {
                char p = pattern.charAt(i);
                if (p == '#') {
                    out.append(digits.charAt(di++));
                } else {
                    out.append(p);
                }
            }

            isUpdating = true;
            et.setText(out.toString());
            et.setSelection(out.length());
            isUpdating = false;
        }
    }
}
