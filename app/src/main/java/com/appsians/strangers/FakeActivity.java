package com.appsians.strangers;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FakeActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    StorageReference mreference;
    ImageView endCall,micBtn,vdoBtn;
    LottieAnimationView LoadinganimationView;
    ImageView connectingImage;
    Boolean isAudio,isVideo;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ArrayList<String> fakeUserList;
    PreviewView mPreviewView;
    int fakeUserSize,randomVal,randomVideoIndex;
    String fakeUserName,fakeVideoName;
    Random ran;
    VideoView videoView;
    File rootPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake);

        mPreviewView = findViewById(R.id.camera);



       fakeUserName = "";
       fakeVideoName = "";

       fakeUserList = new ArrayList<String>();

        endCall = findViewById(R.id.endCallfake);
        micBtn = findViewById(R.id.micBtnfake);
        isAudio = true;
        vdoBtn = findViewById(R.id.videoBtnfake);
        isVideo = true;

        LoadinganimationView = findViewById(R.id.loadingAnimationfake);
        connectingImage = findViewById(R.id.connectingImagefake);

        videoView = findViewById(R.id.videoview);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        database.getReference().child("fakeUser")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot child: snapshot.getChildren()){
                            String fakeUservalue = child.getValue(String.class);
                            fakeUserList.add(fakeUservalue);

                        }
                        fakeUserSize = fakeUserList.size();
                        ran = new Random();
                        randomVal = ran.nextInt(Math.abs(fakeUserSize));
                       // Toast.makeText(FakeActivity.this, "random"+randomVal, Toast.LENGTH_LONG).show();
                        randomVideoIndex = randomVal;
                        fakeUserName = fakeUserList.get(randomVideoIndex).toString();
                        fakeVideoName = fakeUserName + ".mp4";
                         connectRandomGirl();
                     //   Toast.makeText(FakeActivity.this, ""+fakeUserList + " ... " + fakeVideoName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }
    public void connectRandomGirl(){
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference videoRef = storageRef.child("videos/"+fakeVideoName);
        //  StorageReference  islandRef = storageRef.child("file.txt");

         rootPath = new File(Environment.getExternalStorageDirectory(), "videos");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath,fakeVideoName);


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

                if(allPermissionsGranted()){
                    startCamera(); //start camera if permission has been granted by user
                } else{
                    ActivityCompat.requestPermissions(FakeActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }

                mPreviewView = findViewById(R.id.camera);



                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        localFile.delete();
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
        if(rootPath.isDirectory()){
            File[] files = rootPath.listFiles();
            if (files!=null){
                for(File f : files){
                    f.delete();
                }
            }
        }
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
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        // Camera camera =  cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);


        // Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
        androidx.camera.core.Camera camera =  cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }



    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
}