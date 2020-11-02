package com.ant.audioplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Player extends Fragment implements View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE = 123;
    private static final String TAG = "States";
    private ImageButton btnRew, btnPlay, btnPause, btnFF;
//    private MediaPlayer mPlayer;
    private Handler handler = new Handler();
    private TextView songName, startTime, songTime;
    private SeekBar songPrgs;
//    private static int currTime = 0, strtTime = 0, endTime = 0, ffTime = 5000, rewTime = 5000;

    Intent serviceIntent;
    AppDatabase db;
    SongDao songDao;
    Integer argPos;
    String nameEx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player2, container, false);
        btnPlay = view.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        songName = view.findViewById(R.id.songName);
        songPrgs = view.findViewById(R.id.seekBar);
        songTime = view.findViewById(R.id.txtSongTime);
        startTime = view.findViewById(R.id.startTime);
        btnFF = view.findViewById(R.id.btnForward);
        btnFF.setOnClickListener(this);
        btnPause = view.findViewById(R.id.btnPause);
        btnPause.setOnClickListener(this);
        btnRew = view.findViewById(R.id.btnBackward);
        btnRew.setOnClickListener(this);
//        mPlayer = new MediaPlayer();
        //loadPlayer();
        Log.d(TAG, "onCreateView");
        return view;
    }

    @Override
    public void onClick(View v) {
        serviceIntent = new Intent(getContext(), MyService.class);
        //Intent playButtonIntent = new Intent(getContext(), MyService.class);

        switch (v.getId()) {
            case R.id.btnPlay:
                if (!Objects.equals(serviceIntent.getAction(), MyService.ACTION_PLAY)){
                    serviceIntent.setAction(MyService.ACTION_START_FOREGROUND_SERVICE);
                    serviceIntent.putExtra("argPosExtra", argPos);
                    getContext().startService(serviceIntent);
                } else {
                    serviceIntent.putExtra("argPosExtra", argPos);
                    serviceIntent.setAction(MyService.ACTION_PLAY);
                }
                Log.d(TAG, "playing " + argPos + " song");
                //mPlayer.start();
//                endTime = mPlayer.getDuration();
//                strtTime = mPlayer.getCurrentPosition();
//                if (currTime == 0) {
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
                btnPlay.setEnabled(false);
                btnPause.setEnabled(true);
                break;
            case R.id.btnPause:
                serviceIntent.setAction(MyService.ACTION_PAUSE);
                btnPlay.setEnabled(true);
                btnPause.setEnabled(false);
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        db = App.getInstance().getDatabase();
//                        songDao = db.songDao();
//                        Song song = songDao.getSongById(1);
//                        song.currentPosition = mPlayer.getCurrentPosition();
//                        songDao.update(song);
//                    }
//                });
//                thread.start();
//                mPlayer.pause();
                break;
            case R.id.btnBackward:
//                if (strtTime - rewTime > 0) {
//                    strtTime = strtTime - ffTime;
//                    mPlayer.seekTo(strtTime);
//                } else {
//                    Toast.makeText(getActivity(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.btnForward:
//                if (strtTime + ffTime <= endTime) {
//                    strtTime = strtTime + ffTime;
//                    mPlayer.seekTo(strtTime);
//                } else {
//                    Toast.makeText(getActivity(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
//                }
                break;
        }
    }

//    private Runnable UpdateSongTime = new Runnable() {
//        @Override
//        public void run() {
//            strtTime = mPlayer.getCurrentPosition();
//            startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(strtTime),
//                    TimeUnit.MILLISECONDS.toSeconds(strtTime) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(strtTime))));
//            songPrgs.setProgress(strtTime);
//            handler.postDelayed(this, 100);
//        }
//    };

    private Runnable UpdateSongName = new Runnable() {
        @Override
        public void run() {
            songName.setText(nameEx);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        argPos = getArguments().getInt("argPos");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db = App.getInstance().getDatabase();
                songDao = db.songDao();
                Song song = songDao.getSongById(argPos);
                if (song == null) {
                    return;
                } else {
                    nameEx = songDao.getSongById(argPos).title;
                    song.uri = songDao.getSongById(argPos).uri;
                    song.currentPosition = songDao.getSongById(argPos).currentPosition;
                }
//                try {
//                    mPlayer = new MediaPlayer();
//                    mPlayer.setDataSource(getContext(), Uri.parse(song.uri));
//                    mPlayer.prepare();
//                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mPlayer.seekTo(song.currentPosition);
//                    //Log.d(TAG, "song curr poss in onResume is: " + song.currentPosition);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                handler.post(UpdateSongName);
            }
        });
        thread.start();
    }

    public Integer getArgPos() {
        return argPos;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceIntent.setAction(MyService.ACTION_STOP_FOREGROUND_SERVICE);
        getContext().stopService(serviceIntent);
    }
}