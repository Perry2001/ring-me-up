package com.example.ringmeup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    ImageView logoutBtn;
    Ringtone ringtone;
    WebView webView;
    TextView loading;
    AppCompatButton lockBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        setUpButtons();
        createNotification();
        setUpDoorbellNotify();
        setUpWebView();
        setUpButtonBG();

        lockBtn.setOnClickListener(v-> lock());


    }

    private void setUpButtonBG() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("lock");

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    long lockStatus = (long) snapshot.child("status").getValue();
                    Drawable lockIcon;

                    // Set the drawable at the end (drawableEnd)


                    if(lockStatus == 0){
                        lockBtn.setText("Lock");
                        lockIcon = getResources().getDrawable(R.drawable.ic_lock);
                        lockBtn.setBackgroundResource(R.drawable.custom_primary_btn);
                    } else{
                        lockBtn.setText("Unlock");
                        lockIcon = getResources().getDrawable(R.drawable.ic_unlock);
                        lockBtn.setBackgroundResource(R.drawable.custom_red_btn);
                    }

                    lockBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, lockIcon, null);
                } else {
                    Log.d("TAG", "Snapshot not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Failed to fetch lock " + error.getMessage());
            }
        });
    }

    private void lock() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("lock");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    long lockStatus = (long) snapshot.child("status").getValue();

                    if(lockStatus == 0){

                        db.child("status").setValue(1);
                    } else{
                        db.child("status").setValue(0);
                    }
                } else {
                    Log.d("TAG", "Snapshot not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Failed to fetch lock " + error.getMessage());
            }
        });
    }

    private void setUpWebView() {
        webView.setWebViewClient(new WebViewClient() {
            private boolean pageLoadFailed = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pageLoadFailed = false;
                loading.setVisibility(View.VISIBLE); // Show loading when the page starts
                webView.setVisibility(View.GONE); // Hide WebView while loading
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!pageLoadFailed) {
                    // Hide the loading TextView only if the page loaded successfully
                    loading.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                } else {
                    // If the page failed to load, you can choose to keep the loading view visible
                    loading.setVisibility(View.VISIBLE); // or handle it differently
                    webView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Handle general errors
                int errorCode = error.getErrorCode();
                pageLoadFailed = true; // Set the flag to true if an error occurs

                // Display the loading TextView for cleartext error or any other loading error
                if (errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE || errorCode == WebViewClient.ERROR_UNKNOWN) {
                    loading.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                // Handle HTTP-specific errors
                Log.d("TAG", "WEB VIEW RESPONSE:  " + errorResponse.getStatusCode());
                pageLoadFailed = true; // Set the flag to true for HTTP errors
                if (errorResponse.getStatusCode() == 404) {
                    loading.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }
            }
        });




        DatabaseReference db = FirebaseDatabase.getInstance().getReference("doorbell");

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ipAdd = snapshot.child("ipAddress").getValue().toString();
                    webView.loadUrl(ipAdd);

                } else {
                    Log.d("TAG", "Snapshot doesn't exist ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Failed to fetch IP Address: " + error.getMessage());
            }
        });
    }



    private void playRingtone(){
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, soundUri);
        ringtone.play();
    }

    private void stopRingtone(){
        if (ringtone != null && ringtone.isPlaying()){
            ringtone.stop();
        }
    }

    private void createNotification() {
        //If version is greater than version oreo, notification proceeds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("Doorbell",
                    "Doorbell", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100,1000,200,340});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =  getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void getNotify(){
        Context context = this;
        if (context != null){

            String alertMessage = "Doorbell clicked";
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Doorbell");
            builder.setContentTitle("Doorbell");
            builder.setSmallIcon(R.drawable.ic_logo);
            builder.setAutoCancel(true);
            builder.setContentText(alertMessage);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVibrate(new long[] {100,1000,200,340});
            builder.setContentIntent(pendingIntent);


            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.notify(0, builder.build());
        }
    }

    private void setUpDoorbellNotify() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("doorbell");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    boolean isDoorbellClicked = (boolean) snapshot.child("isClick").getValue();

                    if (isDoorbellClicked){
                        getNotify();
                        playRingtone();
                    } else {
                        stopRingtone();
                    }
                    Log.d("TAG", "isClicked: " + isDoorbellClicked);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Failed to fetch doorbell data");
            }
        });
    }

    private void setUpButtons() {
        logoutBtn.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
        });
    }

    private void initWidgets() {
        logoutBtn = findViewById(R.id.logout_ImageView);
        webView = findViewById(R.id.webview);
        loading = findViewById(R.id.loading_TextView);
        lockBtn = findViewById(R.id.lock_Button);
    }
}