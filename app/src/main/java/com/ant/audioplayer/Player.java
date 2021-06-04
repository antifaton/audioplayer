package com.ant.audioplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Player extends Fragment implements View.OnClickListener {

    private static final String TAG = "Player/Player";
    private ImageButton btnRew, btnPlay, btnPause, btnFF;
    private Handler handler = new Handler();
    private TextView songName, startTime, songTime;
    private SeekBar songPrgs;
    private static int currTime = 0, strtTime = 0, endTime = 0, ffTime = 5000, rewTime = 5000;

    boolean bound = false;
    MyService myService;
    ServiceConnection serviceConnection;
    Intent serviceIntent;
    AppDatabase db;
    SongDao songDao;
    Integer argPos;
    String nameEx;
    int currTimeEx;
    int timeEx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player2, container, false);
        btnPlay = view.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);
        songName = view.findViewById(R.id.songName);
        songPrgs = view.findViewById(R.id.seekBar);
        songPrgs.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: "+String.format("%d min, %d sec"
                        , TimeUnit.MILLISECONDS.toMinutes(progress)
                        , TimeUnit.MILLISECONDS.toSeconds(progress)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))));
                //endTime = myService.getmPlayer().getDuration();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        songTime = view.findViewById(R.id.txtSongTime);
        startTime = view.findViewById(R.id.startTime);
        btnFF = view.findViewById(R.id.btnForward);
        btnFF.setOnClickListener(this);
        btnPause = view.findViewById(R.id.btnPause);
        btnPause.setOnClickListener(this);
        btnRew = view.findViewById(R.id.btnBackward);
        btnRew.setOnClickListener(this);
        Log.d(TAG, "onCreateView");
        serviceIntent = new Intent(getContext(), MyService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myService = ((MyService.MyBinder) service).getService();
                Log.d(TAG, "MainActivity onServiceConnected");
                bound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnPlay:
                if (myService.getmPlayer().isPlaying()){
                    Log.d(TAG, "onClick: player is playing");
                } else {
                    serviceIntent.setAction(MyService.ACTION_PLAY);
                    serviceIntent.putExtra("argPosExtra", argPos);
                    getContext().startService(serviceIntent);
                    //Log.d(TAG, "playing " + argPos + " song");
                    //mPlayer.start();
                    endTime = myService.getmPlayer().getDuration();
                    Log.d(TAG, "onClick: endTime is " + endTime);
                    strtTime = myService.getmPlayer().getCurrentPosition();
                    Log.d(TAG, "onClick: strtTime is " + strtTime);
                    songPrgs.setMax(endTime);
                    Log.d(TAG, "onClick: play -> songPrgs.setMax is " + songPrgs.getMax());
                    songTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(endTime),
                            TimeUnit.MILLISECONDS.toSeconds(endTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime))));
                    startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(strtTime),
                            TimeUnit.MILLISECONDS.toSeconds(strtTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(strtTime))));
                    songPrgs.setProgress(strtTime);
                    handler.postDelayed(UpdateSongTime, 100);
//                    btnPlay.setEnabled(false);
//                    btnPause.setEnabled(true);
                }
                break;
            case R.id.btnPause:
                if (myService.getmPlayer().isPlaying()) {
                    serviceIntent.setAction(MyService.ACTION_PAUSE);
                    getContext().startService(serviceIntent);
                } else {
                    Log.d(TAG, "onClick: player is paused");
                }
//                btnPlay.setEnabled(true);
//                btnPause.setEnabled(false);
                break;
            case R.id.btnBackward:
                serviceIntent.setAction(MyService.ACTION_BACKWARD);
                getContext().startService(serviceIntent);
                break;
            case R.id.btnForward:
                serviceIntent.setAction(MyService.ACTION_FORWARD);
                getContext().startService(serviceIntent);
//                handler.post(UpdateSongTime);
                break;
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            strtTime = myService.getmPlayer().getCurrentPosition();
            startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(strtTime),
                    TimeUnit.MILLISECONDS.toSeconds(strtTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(strtTime))));
            songPrgs.setProgress(strtTime);
//            Log.d(TAG, "run: myService.getmPlayer().getCurrentPosition(); is "+strtTime);
//            Log.d(TAG, "run: song progress is "+ songPrgs.getProgress());
            handler.postDelayed(this, 100);
        }
    };

    private Runnable UpdateSongName = new Runnable() {
        @Override
        public void run() {
            songName.setText(nameEx);
            strtTime = currTimeEx;
            endTime = timeEx;
            songPrgs.setProgress(strtTime);
            //Log.d(TAG, "run: start time is "+strtTime+" end time is "+endTime);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Thread thread = new Thread(() -> {
            db = App.getInstance().getDatabase();
            songDao = db.songDao();
            Song song = songDao.getSongById(argPos);
            if (song == null) {
                return;
            } else {
                nameEx = songDao.getSongById(argPos).name;
                song.uri = songDao.getSongById(argPos).uri;
                song.currentPosition = songDao.getSongById(argPos).currentPosition;
                currTimeEx = song.currentPosition;
                timeEx = song.duration;
                Log.d(TAG, "onResume: song.currPos is "+song.currentPosition+" song duration is "+song.duration
                        +" songDao duration is "+songDao.getSongById(argPos).duration
                        +" timeEx is "+timeEx);
            }
            handler.post(UpdateSongName);
        });
        thread.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        argPos = getArguments().getInt("argPos");
        if (!Objects.equals(serviceIntent.getAction(), MyService.ACTION_PAUSE) &&
                !Objects.equals(serviceIntent.getAction(), MyService.ACTION_PLAY)){
            serviceIntent.setAction(MyService.ACTION_START_FOREGROUND_SERVICE);
            serviceIntent.putExtra("argPosExtra", argPos);
            getContext().startService(serviceIntent);
            if (getActivity().bindService(serviceIntent, serviceConnection, 0)){
                Log.d(TAG, "onResume: Service is binded");
            } else {
                getActivity().bindService(serviceIntent, serviceConnection, 0);
                Log.d(TAG, "onResume: bindService");
            }
        } else {
            Log.d(TAG, "onResume: Action is : Foreground service is started");
        }
    }

    //    @Override
//    public void onStop() {
//        super.onStop();
//        getActivity().unbindService(serviceConnection);
//        Log.d(TAG, "onStop: unBindService");
//        bound = false;
//    }
}