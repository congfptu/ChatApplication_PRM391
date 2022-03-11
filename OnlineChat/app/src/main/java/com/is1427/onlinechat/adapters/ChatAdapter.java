package com.is1427.onlinechat.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import com.is1427.onlinechat.activities.MainActivity;
import com.is1427.onlinechat.activities.ViewImageActivity;
import com.is1427.onlinechat.databinding.ItemContainerReceiverdMessageBinding;
import com.is1427.onlinechat.databinding.ItemContainerSentMessageBinding;
import com.is1427.onlinechat.models.ChatMessage;
import com.is1427.onlinechat.utilities.Constants;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessage;
    private Bitmap receiverProfileImage;
    private final String senderId;
    public static Context ct;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessage, Bitmap receiverProfileImage, String senderId, Context ct) {
        this.chatMessage = chatMessage;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.ct = ct;
    }

    public static Context getCt() {
        return ct;
    }

    public void setCt(Context ct) {
        this.ct = ct;
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

    private static Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;
        private String currentImage;

        SentMessageViewHolder(@NonNull ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(@NonNull ChatMessage chatMessage) {
            if (chatMessage.getImageMessage() == null) {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setText(chatMessage.getMessage());
            } else {
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
                binding.imageMessage.setImageBitmap(getConversionImage(chatMessage.getImageMessage()));

                currentImage = chatMessage.getImageMessage();
            }
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageMessage.setOnClickListener(this::onImageClick);


        }

        private void onImageClick(View view) {
            Intent intent = new Intent(ct, ViewImageActivity.class);
            if (currentImage != null) {
                intent.putExtra("image_sent", currentImage);
                ct.startActivity(intent);

            }


        }
    }

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceiverdMessageBinding binding;
        private String currentImage;

        ReceiverMessageViewHolder(@NonNull ItemContainerReceiverdMessageBinding itemContainerReceiverdMessageBinding) {
            super(itemContainerReceiverdMessageBinding.getRoot());
            binding = itemContainerReceiverdMessageBinding;
        }

        void setData(@NonNull ChatMessage chatMessage, Bitmap receiverProfileImage) {
            currentImage=chatMessage.getImageMessage();
            if (chatMessage.getImageMessage() == null) {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setText(chatMessage.getMessage());
            } else {
                binding.textMessage.setVisibility(View.GONE);
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.imageMessage.setImageBitmap(getConversionImage(chatMessage.getImageMessage()));
            }
            binding.imageMessage.setOnClickListener(this::onImageClick);
            binding.textDateTime.setText(chatMessage.dateTime);


            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }

        private void onImageClick(View view) {
            Intent intent = new Intent(ct, ViewImageActivity.class);
            if (currentImage != null) {
                intent.putExtra("image_received", currentImage);
                ct.startActivity(intent);
            }

        }
    }
}
