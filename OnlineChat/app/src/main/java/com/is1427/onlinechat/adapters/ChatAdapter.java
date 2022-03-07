package com.is1427.onlinechat.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.is1427.onlinechat.databinding.ItemContainerReceiverdMessageBinding;
import com.is1427.onlinechat.databinding.ItemContainerSentMessageBinding;
import com.is1427.onlinechat.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessage;
    private Bitmap receiverProfileImage;

    private final String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessage, Bitmap receiverProfileImage, String senderId) {
        this.chatMessage = chatMessage;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        } else {
            return new ReceiverMessageViewHolder(
                    ItemContainerReceiverdMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessage.get(position));
        } else {
            ((ReceiverMessageViewHolder) holder).setData(chatMessage.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessage.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(@NonNull ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(@NonNull ChatMessage chatMessage) {
                binding.textMessage.setText(chatMessage.message.trim());
                binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceiverdMessageBinding binding;

        ReceiverMessageViewHolder(@NonNull ItemContainerReceiverdMessageBinding itemContainerReceiverdMessageBinding) {
            super(itemContainerReceiverdMessageBinding.getRoot());
            binding = itemContainerReceiverdMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
                binding.textMessage.setText(chatMessage.message);
                binding.textDateTime.setText(chatMessage.dateTime);
                if(receiverProfileImage != null){
                    binding.imageProfile.setImageBitmap(receiverProfileImage);
                }

        }

    }
}
