package com.appsians.strangers;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class FakeActivity extends AppCompatActivity {
    FirebaseStorage storage;
    StorageReference mreference;
    ImageView endCall,micBtn,vdoBtn;
    LottieAnimationView LoadinganimationView;
    ImageView connectingImage;
    Boolean isAudio,isVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake);

        endCall = findViewById(R.id.endCallfake);
        micBtn = findViewById(R.id.micBtnfake);
        isAudio = true;
        vdoBtn = findViewById(R.id.videoBtnfake);
        isVideo = true;

        LoadinganimationView = findViewById(R.id.loadingAnimationfake);
       connectingImage = findViewById(R.id.connectingImagefake);

        VideoView videoView = findViewById(R.id.videoview);

        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference videoRef = storageRef.child("videos/test.mp4");
      //  StorageReference  islandRef = storageRef.child("file.txt");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "videos");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath,"test.mp4");

        videoRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                Uri videoUri = Uri.fromFile(localFile);
                videoView.setVideoURI(videoUri);
                videoView.requestFocus();

                LoadinganimationView.setVisibility(View.GONE);
                connectingImage.setVisibility(View.GONE);

                videoView.start();
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        finish();
                    }
                });
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });

    }

    public void endCall(View v) {
        finish();
    }
    public void micBtnClick(View v) {
     if(isAudio){
         micBtn.setImageResource(R.drawable.btn_mute_normal);
         isAudio = false;
     }
     else{
         micBtn.setImageResource(R.drawable.btn_unmute_normal);
         isAudio = true;
     }
    }
    public void vdoBtnClick(View v) {
        if(isVideo){
            vdoBtn.setImageResource(R.drawable.btn_video_muted);
            isVideo = false;
        }
        else{
            vdoBtn.setImageResource(R.drawable.btn_video_normal);
            isVideo = true;
        }
    }
    }
