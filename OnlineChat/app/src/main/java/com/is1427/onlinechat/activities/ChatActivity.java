package com.is1427.onlinechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.is1427.onlinechat.utilities.Constants;
import android.os.Bundle;

import com.is1427.onlinechat.R;
import com.is1427.onlinechat.databinding.ActivityChatBinding;
import com.is1427.onlinechat.models.User;

public class ChatActivity extends AppCompatActivity {
    public @NonNull
    ActivityChatBinding binding;
    private User receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
    }
    private void loadReceiverDetails(){
        receiverUser=(User) getIntent().getSerializableExtra(com.is1427.onlinechat.utilities.Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }
}