package com.example.feature_produtor;

import android.os.Bundle;
// import android.os.Handler; // Removido: Não é mais necessário para delay
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import com.example.feature_produtor.api.ApiIA;
import com.example.core.network.RetrofitClientIA;
import com.example.feature_produtor.adapter.MessageAdapter;
import com.example.feature_produtor.dto.request.ChatRequest;
import com.example.feature_produtor.dto.response.ChatResponse;
import com.example.feature_produtor.model.chatbot.Message;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// A classe agora é ChatBotWorker (usando o nome do seu código enviado)
public class ChatBotWorker extends Fragment {

    private EditText editMessage;
    private ImageButton btnSend;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private ApiIA apiService;

    // NOVO: Variável para rastrear o balão de carregamento/resposta atual.
    private Message currentBotMessage;

    // ----------------------------------------------------------------------
    // Clico de Vida do Fragment
    // ----------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout XML do seu Fragment
        return inflater.inflate(R.layout.fragment_chat_bot_worker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicialização de Views e Variáveis (usando a view inflada)
        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        recyclerView = view.findViewById(R.id.recicler_bot);

        // 2. Configuração do RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        // 3. Inicialização do Cliente API
        apiService = RetrofitClientIA.getInstance(requireContext()).create(ApiIA.class);

        // Mensagem inicial do chatbot
        addBotMessage("Olá! Sou o Chatbot Zeta. Como posso te ajudar?");

        // 4. Configuração do Botão de Envio
        btnSend.setOnClickListener(v -> sendMessage());

        // 5. Configuração do botão de voltar
        view.findViewById(R.id.icon_meta).setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(this).popBackStack();
            } catch (IllegalStateException e) {
                // Caso o Fragment não esteja em um NavHost
                Log.e("ChatBot", "Erro ao voltar: Não está em um NavHost. " + e.getMessage());
            }
        });
    }

    // ----------------------------------------------------------------------
    // Lógica do Chatbot
    // ----------------------------------------------------------------------

    /**
     * Lógica de envio da mensagem do usuário e chamada da API.
     */
    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma mensagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Adiciona a mensagem do usuário à lista e limpa o campo
        addUserMessage(messageText);
        editMessage.setText("");

        // 2. Cria e adiciona o balão de LOADING. Este balão será ATUALIZADO depois.
        currentBotMessage = new Message("", Message.TYPE_LOADING);
        messageList.add(currentBotMessage);
        // Notifica o Adapter para exibir o "..."
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // 3. Faz a chamada à API
        ChatRequest request = new ChatRequest(messageText);
        Call<ChatResponse> call = apiService.sendMessage(request);

        setSending(true);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                setSending(false);

                String finalResponse;
                if (response.isSuccessful() && response.body() != null) {
                    finalResponse = response.body().getResponseText();
                } else {
                    finalResponse = "Desculpe, não consegui obter uma resposta do servidor. Código: " + response.code();
                    Log.e("ChatBot", "Erro na API: " + response.code());
                }

                // CHAVE: ATUALIZA o balão de loading para a resposta real.
                updateBotMessage(finalResponse);
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                setSending(false);
                String errorResponse = "Erro de conexão. Verifique sua internet ou tente novamente.";
                Log.e("ChatBot", "Erro de rede: " + t.getMessage(), t);

                // CHAVE: ATUALIZA o balão de loading para a mensagem de erro.
                updateBotMessage(errorResponse);
            }
        });
    }

    /**
     * Atualiza o objeto Message de carregamento com o texto final da API e notifica o Adapter.
     */
    private void updateBotMessage(String newText) {
        if (currentBotMessage != null) {
            // 1. Encontra a posição antes de modificar (para o notifyItemChanged)
            int position = messageList.indexOf(currentBotMessage);

            // 2. Modifica o objeto: Texto e Tipo
            currentBotMessage.setText(newText);
            currentBotMessage.setType(Message.TYPE_BOT); // Muda para o tipo BOT normal

            // 3. Notifica o Adapter para redesenhar o item (o texto "..." será substituído pelo newText)
            if (position != -1) {
                messageAdapter.notifyItemChanged(position);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            // 4. Limpa o rastreador
            currentBotMessage = null;
        }
    }


    // ----------------------------------------------------------------------
    // Métodos Auxiliares
    // ----------------------------------------------------------------------

    /**
     * Adiciona uma mensagem do usuário.
     */
    private void addUserMessage(String text) {
        messageAdapter.addMessage(new Message(text, Message.TYPE_USER));
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    /**
     * Adiciona uma mensagem do chatbot. Usado apenas para a mensagem inicial.
     */
    private void addBotMessage(String text) {
        messageAdapter.addMessage(new Message(text, Message.TYPE_BOT));
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    /**
     * Desabilita/habilita a UI durante o envio.
     */
    private void setSending(boolean sending) {
        editMessage.setEnabled(!sending);
        btnSend.setEnabled(!sending);
    }
}