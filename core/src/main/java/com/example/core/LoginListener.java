package com.example.core; // Ou onde as interfaces de callback estão (módulo core ou data)

/**
 * Interface de callback para notificar o sucesso ou falha do processo de login.
 * Permite que a Activity/Fragment espere a conclusão da chamada de rede assíncrona
 * e receba o objeto Worker logado.
 */
public interface LoginListener {

    /**
     * Chamado quando o login e a recuperação do objeto Worker são bem-sucedidos.
     */
    void onSuccess();

    /**
     * Chamado quando ocorre uma falha na autenticação ou na busca do Worker.
     * @param errorMsg Uma mensagem de erro para ser exibida ao usuário.
     */
    void onFailure(String errorMsg);
}