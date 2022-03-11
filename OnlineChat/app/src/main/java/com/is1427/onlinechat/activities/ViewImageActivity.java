package com.is1427.onlinechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.is1427.onlinechat.databinding.ActivityUsersBinding;
import com.is1427.onlinechat.databinding.ActivityViewImageBinding;

import java.io.ByteArrayOutputStream;

public class ViewImageActivity extends AppCompatActivity {
     private ActivityViewImageBinding binding;
     public void bindingAction(){
         binding.imageBack.setOnClickListener(this::onClickImageBack);
         setImage();
     }

    private void onClickImageBack(View view) {
         onBackPressed();
    }
   public void setImage(){
       String image=getIntent().getStringExtra("image_sent");
       if(image!=null)
           binding.imageMessage.setImageBitmap(getConversionImage(image));
       else {
           String image1 = getIntent().getStringExtra("image_received");
           if(image1!=null)
               binding.imageMessage.setImageBitmap(getConversionImage(image1));
       }
   }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityViewImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bindingAction();



    }

    private static Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}