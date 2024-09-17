package com.example.ringmeup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ImageView logoutBtn;
    Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        setUpButtons();
        createNotification();
        setUpDoorbellNotify();
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
    }
}