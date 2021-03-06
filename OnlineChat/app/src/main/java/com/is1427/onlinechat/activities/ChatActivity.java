package com.is1427.onlinechat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.is1427.onlinechat.R;
import com.is1427.onlinechat.adapters.ChatAdapter;
import com.is1427.onlinechat.databinding.ActivityChatBinding;
import com.is1427.onlinechat.models.ChatMessage;
import com.is1427.onlinechat.models.User;
import com.is1427.onlinechat.network.ApiClient;
import com.is1427.onlinechat.network.ApiService;
import com.is1427.onlinechat.utilities.Constants;
import com.is1427.onlinechat.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    public @NonNull
    ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private String encodedImage;
    private Boolean isReceiverAvailable = false;
    ArrayList<String> inValidMessage =new ArrayList<>();
    private HashMap<String,String> emojis= new HashMap<String,String>();

    private Boolean isSendImage=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setInValidMessage();
        setContentView(R.layout.activity_chat);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setDefaultEmojis();
        //block screenshot from android app
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();

    }
//data of Invalid Message
 public void setInValidMessage(){
     inValidMessage.add("lon");
     inValidMessage.add("cac");
     inValidMessage.add("dcm");
     inValidMessage.add("vcl");
     inValidMessage.add("vl");
     inValidMessage.add("dm");
     inValidMessage.add("cc");
     inValidMessage.add("me");
     inValidMessage.add("djtme");
     inValidMessage.add("dit");
     inValidMessage.add("cut");
     inValidMessage.add("vc");
     inValidMessage.add("djt");

 }
 //data of emoji
    public void setDefaultEmojis(){
        emojis.put(":)",getEmojiFromUnicode(0x1F642));
        emojis.put(":(",getEmojiFromUnicode(0x1F612));
        emojis.put("<3",getEmojiFromUnicode(0x2764));
        emojis.put(":D",getEmojiFromUnicode(0x1F603));
        emojis.put("=)",getEmojiFromUnicode(0x263A));
        emojis.put(":o",getEmojiFromUnicode(0x1F632));


    }
    //set data of recycler view chat activity
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID),
                this
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }
    //Invisible invalid word
 public String convertInvalidWord(String word){
        String rs="";
        for (int i=0;i<word.length();i++)
            rs+="*";
        return rs;
 }
 //send Heart Emoji
    private void sendEmojiMessage(String emoji){

        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,emoji);
        message.put(Constants.KEY_TIMESTAMP,new Date());
        //add data into firebase
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
            // updateConversion(binding.inputMessage.getText().toString());
            updateConversion(emoji);
        }else{
            //put data into conversation
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,emoji);
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        if(!isReceiverAvailable){
            try{
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, emoji);

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());


            }catch(Exception exception){


            }
        }
        binding.inputMessage.setText(null);
    }
    //send message image
    private void sendImageMessage(){

        String words=encodedImage;
            HashMap<String,Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            message.put(Constants.KEY_IMAGE_MESSAGE,words.trim());
            message.put(Constants.KEY_TIMESTAMP,new Date());
            //add data of message into firebase
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if(conversionId != null){
                // updateConversion(binding.inputMessage.getText().toString());
             //update last message of recent conversation
                updateConversion("???? g???i m???t ???nh");
            }else{
                HashMap<String,Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE,"???? g???i m???t ???nh");
                conversion.put(Constants.KEY_TIMESTAMP,new Date());
                addConversion(conversion);
            }
            if(!isReceiverAvailable){
                try{
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);

                    JSONObject data = new JSONObject();
                    data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_IMAGE_MESSAGE, "???? g???i m???t ???nh");

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA, data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                     //push notification if receiver user is not available
                    sendNotification(body.toString());


                }catch(Exception exception){

                }
            }
        binding.inputMessage.setText(null);
        }

        //send text message
    private void sendMessage(){
        //check valid message
        String words=binding.inputMessage.getText().toString().trim();
        if(!words.trim().equals(""))
        {
       String[] arrayWords= words.split("\\s");
       for (int i=0;i<arrayWords.length;i++){
           String word=arrayWords[i];
           if (inValidMessage.contains(word))
           words=words.replaceAll("\\b"+word+"\\b",convertInvalidWord(word));
       }
       for (int i=0;i<inValidMessage.size();i++){
           String word=inValidMessage.get(i);
           if (words.contains(word))
               words= words.replace(word,convertInvalidWord(word));
       }
            String[] listWords= words.split("\\s");
            for (int i = 0; i < listWords.length; i++) {
                String str = listWords[i];
                String emoji = emojis.get(str);
                if (emoji != null) {
                  words=words.replace(str,emoji);
                }
            }
        //add data of message into firebase
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);

        message.put(Constants.KEY_MESSAGE,words.trim());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
           // updateConversion(binding.inputMessage.getText().toString());
           updateConversion(words);
        }else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,words.trim());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        //process send notification
        if(!isReceiverAvailable){
            try{
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, words.trim());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                //get token of receiver to send notification
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());


            }catch(Exception exception){
            }
        }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){

                    try{
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") ==  1){
                                JSONObject error = (JSONObject) results.get(0);
                             //   showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }
                else{
                    showToast("Error:" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });

    }
    //check user online or offline
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
                if (error != null){
                    return;
                }
                if (value != null) {
                    if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                        int availability = Objects.requireNonNull(
                                value.getLong(Constants.KEY_AVAILABILITY)
                        ).intValue();
                        isReceiverAvailable = availability == 1;
                    }
                    receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                    if(receiverUser.image == null){
                        receiverUser.image = value.getString(Constants.KEY_IMAGE);
                        chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                        chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                    }
                }
                if (isReceiverAvailable){
                    binding.textAvailability.setVisibility(View.VISIBLE);
                    binding.iconOnline.setVisibility(View.VISIBLE);
                    binding.iconVideoOnline.setVisibility(View.VISIBLE);
                }else{
                    binding.textAvailability.setVisibility(View.GONE);
                    binding.iconOnline.setVisibility(View.GONE);
                    binding.iconVideoOnline.setVisibility(View.GONE);
                }

        });
    }
   //load recycler view if have new data in firebase
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener =  (value,error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.imageMessage= documentChange.getDocument().getString(Constants.KEY_IMAGE_MESSAGE);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                //load recyclerview
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    };
   //get image bitmap from encodeImage
    private Bitmap getBitmapFromEncodedString(String encodedImage){

        if(encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else{
            return null;
        }

    }
  //load data of receiver message
    private void loadReceiverDetails(){
        receiverUser=(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }
    //event
    private void setListeners(){
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        binding.layoutSend.setOnClickListener(view -> sendMessage());
        binding.imageSendImage.setOnClickListener(view ->sendPicture());
        binding.inputMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
               //send icon shortcut keyboard
            @Override
            public void afterTextChanged(Editable editable) {
                String first, last,replace;
                String message = binding.inputMessage.getText().toString();
                if (message.equals("")) {
                    binding.layoutSend.setVisibility(View.INVISIBLE);
                    binding.sendLike.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutSend.setVisibility(View.VISIBLE);
                    binding.sendLike.setVisibility(View.INVISIBLE);
               try{
                    String[] strings = message.split("\\s");
                    first = strings[0];
                    last = strings[strings.length - 1];
                    for (int i = 0; i < strings.length; i++) {
                        String str = strings[i];
                        String emoji = emojis.get(str);
                        if (emoji != null) {
                            if (first.equals(str)&& message.charAt(first.length())==' ') {
                                try {
                                    message = message.replace(str + " ", emoji + " ");
                                } catch (Exception e) {
                                    return;
                                }
                                binding.inputMessage.setText(message);
                                binding.inputMessage.setSelection(message.length());
                                return;
                            }
                            else if (last.equals(str)&&message.charAt(message.length()-1)==' ')  {
                                str=" "+str+" ";
                                emoji=" "+emoji+" ";

                                try {
                                    message = message.replace( str , emoji);
                                } catch (Exception e) {
                                    return;
                                }
                                int cursorPosition = binding.inputMessage.getSelectionEnd();


                                binding.inputMessage.setText(message);
                                binding.inputMessage.setSelection(message.length());
                                return;
                            }
                            else {
                                if(!last.equals(str)){
                                    str=" "+str+" ";
                                    emoji=" "+emoji+" ";
                                    try {
                                        message = message.replace( str , emoji);
                                    } catch (Exception e) {
                                        return;
                                    }
                                    int cursorPosition = binding.inputMessage.getSelectionEnd();
                                    binding.inputMessage.setText(message);
                                    binding.inputMessage.setSelection(cursorPosition-1);

                                    return;
                                }
                            }

                        }
                    }
                }
           catch(Exception e){
                        return;
                    }
            }
            }
    });
                binding.sendLike.setOnClickListener(view -> sendLikeMessage());
    }
    //send heart quick drop
    private void sendLikeMessage() {
        int uniCode=0x2764;
        String emoji= getEmojiFromUnicode(uniCode);
        sendEmojiMessage(emoji);

    }
    //get emoji from unicode
    private String getEmojiFromUnicode(int unicode){
      return new String(Character.toChars(unicode));
    }


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            encodedImage = encodeImage(bitmap);
                            sendImageMessage();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
    );
    //get picture from library of phone
    private void sendPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);

    }
  //encode image
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 400;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }
    //get real time
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }

    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionCompleteListener = task -> {
      if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
          DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
          conversionId = documentSnapshot.getId();
      }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}