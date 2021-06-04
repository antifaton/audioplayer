package com.ant.audioplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.IOException;

public class MyService extends Service {

    private static final String TAG = "States";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_FORWARD = "ACTION_FORWARD";
    public static final String ACTION_BACKWARD = "ACTION_BACKWARD";


    MediaPlayer mPlayer;
    MyBinder binder = new MyBinder();
    private Handler handler = new Handler();
    private TextView songName, startTime, songTime;
    private static int currTime = 0, strtTime = 0, endTime = 0, ffTime = 5000, rewTime = 5000;
    Integer argPos;
    int prevArgPos;
    AppDatabase db;
    SongDao songDao;
    String songUri;
    String nameEx;
    Song nextSong;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d(TAG, "MyService onBind");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "My foreground service onCreate().");
        mPlayer = new MediaPlayer();
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null)
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        argPos = intent.getExtras().getInt("argPosExtra");
                        Log.d(TAG, "onStartCommand: Start service");
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                db = App.getInstance().getDatabase();
                                songDao = db.songDao();
                                Song song = songDao.getSongById(argPos);
                                if (song == null) {
                                    Log.d(TAG, "song is null");
                                    return;
                                } else {
                                    //nameEx = songDao.getSongById(argPos).title;
                                    song.uri = songDao.getSongById(argPos).uri;
                                    Log.d(TAG, "run: songUri is "+song.uri);
                                    Log.d(TAG, "run: songFolderPath is" + song.folderPath);
                                    song.currentPosition = songDao.getSongById(argPos).currentPosition;
                                }
                                try {
                                    mPlayer.pause();
                                    mPlayer = new MediaPlayer();
                                    mPlayer.setDataSource(song.uri);
                                    mPlayer.prepare();
                                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mPlayer.seekTo(song.currentPosition);
                                    //mPlayer.start();
                                    mPlayer.setOnCompletionListener(mp -> {
                                        mPlayer.stop();
                                        nextSong();
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        break;
                    case ACTION_PLAY:
                        Log.d(TAG, "onStartCommand: play button clicked");
                        if (mPlayer.isPlaying()) {
                            Log.d(TAG, "onStartCommand: player is playing");
                        } else {
                            mPlayer.start();
                        }
                        break;
                    case ACTION_PAUSE:
                        Log.d(TAG, "onStartCommand: pause button clicked");
                        Thread thread2 = new Thread(() -> {
                            db = App.getInstance().getDatabase();
                            songDao = db.songDao();
                            Song song = songDao.getSongById(argPos);
                            song.currentPosition = mPlayer.getCurrentPosition();
                            songDao.update(song);
                        });
                        thread2.start();
                        mPlayer.pause();
                        break;
                    case ACTION_FORWARD:
                        Log.d(TAG, "onStartCommand: forward button clicked");
                        if((mPlayer.getCurrentPosition()+5000) >= mPlayer.getDuration()){
                            Log.d(TAG, "onStartCommand: Action_Forward: can't seekTo +5000 ");
                        } else {
                            Log.d(TAG, "onStartCommand: mPlayer current position is "+mPlayer.getCurrentPosition());
                            mPlayer.seekTo(mPlayer.getCurrentPosition()+5000);
                            Log.d(TAG, "onStartCommand: mPlayer current position is "+mPlayer.getCurrentPosition());
                        }
                        break;
                    case ACTION_BACKWARD:
                        Log.d(TAG, "onStartCommand: backward button clicked");
                        if((mPlayer.getCurrentPosition()-5000) < 0){
                            mPlayer.seekTo(0);
                            Log.d(TAG, "onStartCommand: Action_Backward: can't seekTo -5000 ");
                        } else {
                            mPlayer.seekTo(mPlayer.getCurrentPosition()-5000);
                        }
                        break;
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void nextSong(){
        prevArgPos = argPos;
        argPos++;
        Thread thread = new Thread(() -> {
            db = App.getInstance().getDatabase();
            songDao = db.songDao();
            Song prevSong = songDao.getSongById(prevArgPos);
            prevSong.currentPosition = 0;
            songDao.update(prevSong);
            Song song = songDao.getSongById(argPos);
            if (song == null) {
                Log.d(TAG, "song is null");
                return;
            } else {
                nameEx = songDao.getSongById(argPos).name;
                song.uri = songDao.getSongById(argPos).uri;
                Log.d(TAG, "nextSong run: songUri is "+song.uri);
                Log.d(TAG, "nextSong path is: " + song.uri.substring(0, song.folderPath.length()));
                song.currentPosition = songDao.getSongById(argPos).currentPosition;
            }
            if(!song.uri.substring(0, song.folderPath.length()).equals(song.folderPath)){

                Log.d(TAG, "nextSong()_run: song.uri doesn't equals song.folderPath ("+song.folderPath+")");
                mPlayer.release();
            } else {
                try {
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(song.uri);
                    mPlayer.prepare();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.seekTo(song.currentPosition);
                    mPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /* Used to build and start foreground service. */
    private void startForegroundService() {
        Log.d(TAG, "Start foreground service.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("my_service", "My Background Service");
        } else {

            // Create notification default intent.
            Intent intent = new Intent();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            // Create notification builder.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            // Make notification show big text.
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle("Music player implemented by foreground service.");
            bigTextStyle.bigText("Android foreground service is a android service which can run in foreground always, it can be controlled by user via notification.");
            // Set big text style.
            builder.setStyle(bigTextStyle);

            builder.setWhen(System.currentTimeMillis());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
            builder.setLargeIcon(largeIconBitmap);
            // Make the notification max priority.
            builder.setPriority(Notification.PRIORITY_MAX);
            // Make head-up notification.
            builder.setFullScreenIntent(pendingIntent, true);

            // Add Play button intent in notification.
            Intent playIntent = new Intent(this, MyService.class);
            playIntent.setAction(ACTION_PLAY);
            PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
            NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingPlayIntent);
            builder.addAction(playAction);

            // Add Pause button intent in notification.
            Intent pauseIntent = new Intent(this, MyService.class);
            pauseIntent.setAction(ACTION_PAUSE);
            PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
            NotificationCompat.Action prevAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingPrevIntent);
            builder.addAction(prevAction);

            // Build the notification.
            Notification notification = builder.build();

            // Start foreground service.
            startForeground(1, notification);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName) {
        Intent resultIntent = new Intent(this, MainActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent playButtonIntent = new Intent(this, MyService.class).setAction(ACTION_PLAY);
        Intent pauseButtonIntent = new Intent(this, MyService.class).setAction(ACTION_PAUSE);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playButtonIntent, 0);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseButtonIntent, 0);

        RemoteViews remoteViews = new RemoteViews("com.ant.audioplayer", R.layout.layout_notification);
        remoteViews.setOnClickPendingIntent(R.id.notificationPlay, playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.notificationPause, pausePendingIntent);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("App is running in background")
                .setContentText("1 row")
                .setContent(remoteViews)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent) //intent
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notificationBuilder.build());
        startForeground(1, notification);
    }


    private void stopForegroundService() {
        Log.d(TAG, "Stop foreground service.");
        // Stop foreground service and remove the notification.
        stopForeground(true);
        // Stop the foreground service.
        stopSelf();
    }

    MediaPlayer getmPlayer(){
        return this.mPlayer;
    }

    class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}
