package com.ant.audioplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentFolder extends Fragment {

    private static final int REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE = 123;
    private final int Pick_audio = 1;

    private static final String TAG = "States";
    List<Song> songs = new ArrayList<>();
    AdapterRecycler adapterRecycler;
    Button buttonAdd;
    AppDatabase db;
    SongDao songDao;
    Uri fileUri;
    String songText;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_folder, container, false);
        recyclerView = view.findViewById(R.id.fragmentList);
        buttonAdd = view.findViewById(R.id.fragmentButtonNewSong);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                int permissionStatus = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(intent, Pick_audio);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_READEXTERNAL_STORAGE);
                }
            }
        });
        adapterRecycler = new AdapterRecycler(songs);
        adapterRecycler.setOnItemClickListener(new AdapterRecycler.onItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.d(TAG, "Clicked: " + position + " and Position is " + (position + 1));
                Bundle bundle = new Bundle();
                bundle.putInt("argPos", position+1);
                Log.d(TAG, "Bundle is: " + bundle.toString());
                Navigation.findNavController(v).navigate(R.id.action_fragmentFolder_to_player, bundle);
            }
        });
        recyclerView.setAdapter(adapterRecycler);
        return view;
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
    public void onActivityResult(int requestCode, int resultCode, final Intent songReturnedIntent) {
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
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
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
//                for (Song a: songs) {
//                    Log.d(TAG, "Songs count: " +a.getId() + ". " + a.getTitle());
//                }
            }
        });
    }
}
