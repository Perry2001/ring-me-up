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
import android.webkit.WebSettings;
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

public class MainActivity extends AppCompatActivity {
    private ImageView logoutBtn;
    private Ringtone ringtone;
    private WebView webView;
    private TextView loading;
    private AppCompatButton lockBtn;

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

        // Set up the lock button action
        lockBtn.setOnClickListener(v -> lock());
    }

    private void initWidgets() {
        logoutBtn = findViewById(R.id.logout_ImageView);
        webView = findViewById(R.id.webview);
        loading = findViewById(R.id.loading_TextView);
        lockBtn = findViewById(R.id.lock_Button);
    }

    private void setUpButtons() {
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });
    }

    private void setUpButtonBG() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("lock");

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long lockStatus = snapshot.child("status").getValue(Long.class);
                    Drawable lockIcon;

                    if (lockStatus == 0) {
                        lockBtn.setText("Lock");
                        lockIcon = getResources().getDrawable(R.drawable.ic_lock, null);
                        lockBtn.setBackgroundResource(R.drawable.custom_primary_btn);
                    } else {
                        lockBtn.setText("Unlock");
                        lockIcon = getResources().getDrawable(R.drawable.ic_unlock, null);
                        lockBtn.setBackgroundResource(R.drawable.custom_red_btn);
                    }

                    lockBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, lockIcon, null);
                } else {
                    Log.d("TAG", "Snapshot does not exist");
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
                if (snapshot.exists()) {
                    long lockStatus = snapshot.child("status").getValue(Long.class);
                    db.child("status").setValue(lockStatus == 0 ? 1 : 0);
                } else {
                    Log.d("TAG", "Snapshot does not exist");
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
                loading.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!pageLoadFailed) {
                    loading.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                } else {
                    loading.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                pageLoadFailed = true;
                Log.d("WEB_VIEW_ERROR", "Error: " + error.getDescription());
                loading.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                pageLoadFailed = true;
                Log.d("WEB_VIEW_HTTP_ERROR", "HTTP Error: " + errorResponse.getStatusCode());
                loading.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("doorbell");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String ipAdd = snapshot.child("ipAddress").getValue(String.class);
                    if (ipAdd != null && !ipAdd.startsWith("http://")) {
                        ipAdd = "http://" + ipAdd;
                    }
                    Log.d("WebView URL", "Loading URL: " + ipAdd + ":7123");
                    webView.loadUrl(ipAdd + ":7123");
                } else {
                    Log.d("TAG", "Snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "Failed to fetch IP Address: " + error.getMessage());
            }
        });
    }

    private void playRingtone() {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, soundUri);
        ringtone.play();
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Doorbell",
                    "Doorbell", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 1000, 200, 340});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void getNotify() {
        String alertMessage = "Doorbell clicked";
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Doorbell")
                .setContentTitle("Doorbell")
                .setSmallIcon(R.drawable.ic_logo)
                .setAutoCancel(true)
                .setContentText(alertMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100, 1000, 200, 340})
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.notify(0, builder.build());
    }

    private void setUpDoorbellNotify() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("doorbell");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isDoorbellClicked = snapshot.child("isClick").getValue(Boolean.class);
                    if (isDoorbellClicked) {
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
                Log.d("TAG", "Failed to fetch doorbell status: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }
}
