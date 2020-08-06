package com.ant.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE = 123;
    private static final String TAG = "States";
    private final int Pick_audio = 2;

    private ImageButton btnRew, btnPlay, btnPause, btnFF;
    private MediaPlayer mPlayer;
    private TextView songName, startTime, songTime;
    private SeekBar songPrgs;
    private static int currTime =0, strtTime =0, endTime =0, ffTime = 5000, rewTime = 5000;
    private Handler handler = new Handler();
    private Uri fileUri;
    AppDatabase db;
    SongDao songDao;
    String nameEx;
    Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        btnFF = findViewById(R.id.btnForward);
        btnFF.setOnClickListener(this);
        btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(this);
        btnRew = findViewById(R.id.btnBackward);
        btnRew.setOnClickListener(this);
        mPlayer = new MediaPlayer();
        song = new Song();
        Log.d(TAG, "song pos is: " + song.currentPosition);
        songName = findViewById(R.id.songName);
        songPrgs = findViewById(R.id.seekBar);
        songTime = findViewById(R.id.txtSongTime);
        startTime = findViewById(R.id.startTime);

    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            strtTime = mPlayer.getCurrentPosition();
            startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(strtTime),
                    TimeUnit.MILLISECONDS.toSeconds(strtTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(strtTime))));
            songPrgs.setProgress(strtTime);
            handler.postDelayed(this, 100);
        }
    };

    private Runnable UpdateSongName = new Runnable() {
        @Override
        public void run() {
            songName.setText(nameEx);
        }
    };

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnPlay:
//                Toast.makeText(MainActivity.this, "Playing Audio", Toast.LENGTH_SHORT).show();
//                mPlayer.start();
//                endTime = mPlayer.getDuration();
//                strtTime = mPlayer.getCurrentPosition();
//                if (currTime == 0){
//                    songPrgs.setMax(endTime);
//                    currTime = 1;
//                }
//                songTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
//                        TimeUnit.MILLISECONDS.toSeconds(endTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime))));
//                startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(strtTime),
//                        TimeUnit.MILLISECONDS.toSeconds(strtTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(strtTime))));
//                songPrgs.setProgress(strtTime);
//                handler.postDelayed(UpdateSongTime, 100);
//                btnPlay.setEnabled(false);
//                btnPause.setEnabled(true);
                break;
            case R.id.btnPause:
//                btnPlay.setEnabled(true);
//                btnPause.setEnabled(false);
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        db = App.getInstance().getDatabase();
//                        songDao = db.songDao();
//                        Song song = songDao.getSongById(1);
//                        song.currentPosition = mPlayer.getCurrentPosition();
//                        songDao.update(song);
//
//                    }
//                });
//                thread.start();
//                //Log.d(TAG, "curr pos is^ "+mPlayer.getCurrentPosition());
//                mPlayer.pause();
                break;
            case R.id.btnBackward:
                if (strtTime - rewTime > 0){
                    strtTime = strtTime - ffTime;
                    mPlayer.seekTo(strtTime);
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnForward:
                if (strtTime + ffTime <= endTime){
                    strtTime = strtTime + ffTime;
                    mPlayer.seekTo(strtTime);
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
//--------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
//                if (mPlayer.isPlaying()){
//                    pause();
//                }
//                Intent audioPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                audioPickerIntent.setType("audio/*");
                Intent intent = new Intent(this, ActivityFolder.class);
                startActivityForResult(intent, 2);
//                int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
//                    startActivityForResult(intent, Pick_audio);
//                } else {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE);
//                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission granted
//                } else {
//                    // permission denied
//                }
//                return;
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent songReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, songReturnedIntent);

//        switch (requestCode) {
//            case Pick_audio:
//                if (resultCode == RESULT_OK) {
//                    fileUri = songReturnedIntent.getData();
//                    if(fileUri==null){
//                        songName.setText("path is null");
//                    } else {
//                        songName.setText(getFileName(fileUri));
//                    }
//                    Thread thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            db = App.getInstance().getDatabase();
//                            songDao = db.songDao();
//                            Song song = new Song();
//                            song.title = String.valueOf(songName.getText());
//                            song.uri = String.valueOf(fileUri);
//                            song.currentPosition = 0;
//                            songDao.insert(song);
//                        }
//                    });
//                    thread.start();
//                }
//        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    Log.d(TAG, "Display name is: " + result);
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void pause(){
        btnPlay.setEnabled(true);
        btnPause.setEnabled(false);
        mPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");
//        if (mPlayer.isPlaying()){
//            return;
//        } else {
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    db = App.getInstance().getDatabase();
//                    songDao = db.songDao();
//                    Song song = songDao.getSongById(1);
//                    if (song == null) {
//                        return;
//                    } else {
//                        nameEx = songDao.getSongById(1).title;
//                        song.uri = songDao.getSongById(1).uri;
//                        song.currentPosition = songDao.getSongById(1).currentPosition;
//                    }
//                    try {
//                        mPlayer = new MediaPlayer();
//                        mPlayer.setDataSource(getApplicationContext(), Uri.parse(song.uri));
//                        mPlayer.prepare();
//                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                        mPlayer.seekTo(song.currentPosition);
//                        Log.d(TAG, "song curr poss in onResume is: " + song.currentPosition);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    handler.post(UpdateSongName);
//                }
//            });
//            thread.start();
//        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                db = App.getInstance().getDatabase();
//                songDao = db.songDao();
//                Song song = songDao.getSongById(1);
//                song.currentPosition = mPlayer.getCurrentPosition();
//                Log.d(TAG, "curr pos in onDestroy is^ "+song.currentPosition);
//                songDao.update(song);
//
//            }
//        });
//        thread.start();
    }
}