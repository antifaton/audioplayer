package com.ant.audioplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityFolder extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE = 123;
    private static final String TAG = "States";
    private final int Pick_audio = 1;

    private List<Song> songs = new ArrayList<>();
    AppDatabase db;
    SongDao songDao;
    Button button;
    Uri fileUri;
    String songText;

    private RecyclerView recyclerView;
    private AdapterRecycler adapterRecycler;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        recyclerView = findViewById(R.id.list);
        button = findViewById(R.id.buttonNewSong);
        button.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapterRecycler = new AdapterRecycler(songs);
        recyclerView.setAdapter(adapterRecycler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNewSong:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                int permissionStatus = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(intent, Pick_audio);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE);
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent songReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, songReturnedIntent);

        switch (requestCode) {
            case Pick_audio:
                if (resultCode == RESULT_OK) {
                    fileUri = songReturnedIntent.getData();
                    if(fileUri==null){
                        songText = "path is null";
                    } else {
                        songText = getFileName(fileUri);
                    }
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            db = App.getInstance().getDatabase();
                            songDao = db.songDao();
                            Song song = new Song();
                            song.title = songText;
                            song.uri = String.valueOf(fileUri);
                            song.currentPosition = 0;
                            songDao.insert(song);
                        }
                    });
                    thread.start();
                }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    //Log.d(TAG, "Display name is: " + result);
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

    @Override
    public void onResume(){
        super.onResume();
        db = App.getInstance().getDatabase();
        LiveData<List<Song>> listLiveData = db.songDao().getAll();
        listLiveData.observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> books) {
                songs.clear();
                songs.addAll(books);
                adapterRecycler.notifyDataSetChanged();
                for (Song a: songs) {
                    Log.d(TAG, "Songs count: " +a.getId() + ". " + a.getTitle());
                }
            }
        });
    }
}
