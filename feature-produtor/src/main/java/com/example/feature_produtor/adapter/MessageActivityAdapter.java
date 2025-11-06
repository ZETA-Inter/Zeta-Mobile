package com.example.feature_produtor.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feature_produtor.R; // Garanta que este R seja o do seu módulo feature-produtor
import com.example.feature_produtor.model.chatbot.Message;

import java.util.List;

public class MessageActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;

    public MessageActivityAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    // --- Sobrescreve para usar a view correta ---
    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER) {
            // Layout do USUÁRIO (balão à direita)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.msg_user_activity, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            // Layout do CHATBOT (balão à esquerda)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.msg_bot_activity, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof UserMessageViewHolder) {
            // ... (lógica de bind do usuário)
            ((UserMessageViewHolder) holder).textMessage.setText(message.getText());

        } else if (holder instanceof BotMessageViewHolder) {
            BotMessageViewHolder botHolder = (BotMessageViewHolder) holder;

            if (message.getType() == Message.TYPE_LOADING) {
                // AQUI: Define o texto como "..." quando é loading
                botHolder.textMessage.setText("Carregando a melhor resposta...");
                //

            } else { // Message.TYPE_BOT
                botHolder.textMessage.setText(message.getText());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // --- ViewHolders Internos ---

    // ViewHolder para Mensagens do Chatbot (Layout item_message_chatbot.xml)
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public BotMessageViewHolder(View itemView) {
            super(itemView);
            // Substitua R.id.textMessage se o ID for diferente no seu XML de item do bot
            textMessage = itemView.findViewById(R.id.textMessage);
        }

        public void bind(Message message) {
            textMessage.setText(message.getText());
        }
    }

    // ViewHolder para Mensagens do Usuário (Layout item_message_user.xml)
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        public UserMessageViewHolder(View itemView) {
            super(itemView);
            // Substitua R.id.textMessage se o ID for diferente no seu XML de item do usuário
            textMessage = itemView.findViewById(R.id.textMessage);
        }

        public void bind(Message message) {
            textMessage.setText(message.getText());
        }
    }

    // adicionar novas mensagens
    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }
}