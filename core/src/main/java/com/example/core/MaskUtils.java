package com.example.core;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaskUtils {

    // Aplica limite de caracteres sem conflito
    public static void applyMaxDigits(EditText editText, int maxDigits) {
        editText.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(maxDigits * 2)
        });
    }

    // Máscara estável para CPF/CNPJ
    public static class SimpleMaskTextWatcher implements TextWatcher {
        private final EditText editText;
        private final String mask;
        private boolean isUpdating;
        private String oldText = "";
        private final int maxDigits;

        public SimpleMaskTextWatcher(EditText editText, String mask, int maxDigits) {
            this.editText = editText;
            this.mask = mask;
            this.maxDigits = maxDigits;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (isUpdating) return;

            String str = unmask(editable.toString());

            // evita loop se nada mudou
            if (str.equals(oldText)) return;
            oldText = str;

            StringBuilder masked = new StringBuilder();
            int i = 0;
            for (char m : mask.toCharArray()) {
                if (m != '#' && str.length() > oldText.length()) {
                    masked.append(m);
                    continue;
                }
                if (i >= str.length()) break;
                masked.append(m == '#' ? str.charAt(i++) : m);
            }

            isUpdating = true;
            editText.setText(masked.toString());
            editText.setSelection(masked.length());
            isUpdating = false;
        }

        private String unmask(String s) {
            return s.replaceAll("[^\\d]", "");
        }
    }
}
