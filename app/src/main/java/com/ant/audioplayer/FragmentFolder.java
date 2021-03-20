package com.ant.audioplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.androidexception.filepicker.dialog.DirectoryPickerDialog;

public class FragmentFolder extends Fragment {

    private static final String TAG = "Player_FragmentFolder";
    Handler handler = new Handler();
    List<ListItem> folders = new ArrayList<>();
    File folder;
    File[] file;
    //List<Folder> folderFiles = new ArrayList<>();
    AppDatabase db;
    SongDao songDao;
    FolderDao folderDao;
    AdapterRecycler adapterRecycler;
    RecyclerView recyclerView;
    Button buttonFolder;
    View v;
    String arg;
    int countFolders = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_folder, container, false);
        recyclerView = view.findViewById(R.id.fragmentList);
        buttonFolder = view.findViewById(R.id.fragmentButtonNewFolder);
        buttonFolder.setOnClickListener(v -> {
            if (permissionGranted()) {
                DirectoryPickerDialog directoryPickerDialog = new DirectoryPickerDialog(Objects.requireNonNull(getActivity()),
                        () -> Toast.makeText(getContext(), "Canceled!!", Toast.LENGTH_SHORT).show(),
                        (File... files) -> addFolder(files[0].getPath())
                );
                directoryPickerDialog.show();
            } else {
                requestPermission();
            }
            Log.d(TAG, "onClick method end");
        });
        adapterRecycler = new AdapterRecycler(folders);
        adapterRecycler.setOnItemClickListener((int position, View v) -> {
            this.v = v;
            Thread thread = new Thread(() -> {
                db = App.getInstance().getDatabase();
                folderDao = db.folderDao();
                arg = folderDao.getFolderById(position + 1).uri;
                Log.d(TAG, "arg is: " + arg);
                handler.post(updateArgFolder);
            });
            thread.start();
        });
        recyclerView.setAdapter(adapterRecycler);
        return view;
    }

    private final Runnable updateArgFolder = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: handler");
            if (arg != null) {
                Bundle bundle = new Bundle();
                bundle.putString("argFolderPath", arg);
                Log.d(TAG, "Bundle is: " + bundle.toString());
                Navigation.findNavController(v).navigate(R.id.action_fragmentFolder_to_fragmentSongs, bundle);
            } else {
                Log.d(TAG, "Runnable: arg is null ");
            }
        }
    };

    private boolean permissionGranted() {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    public void addFolder(String path) {
        if (path == null) {
            Log.d(TAG, "path is null");
        } else {
            folder = new File(path);
            file = folder.listFiles();
            Thread thread = new Thread(() -> {
                db = App.getInstance().getDatabase();
                songDao = db.songDao();
                folderDao = db.folderDao();
                Folder fold = new Folder();
                fold.name = folder.getName();
                Log.d(TAG, "fold.name = " + fold.name);
                fold.uri = folder.getPath();
                Log.d(TAG, "fold.uri = " + fold.uri);
                if (folders.size() == 0) {
                    folderDao.insert(fold);
                    int b = 0;
                    for (File a : file) {
                        Song song = new Song();
                        song.name = a.getName();
                        song.uri = a.getPath();
                        song.currentPosition = 0;
                        song.folderPath = fold.uri;
                        if (song.name.endsWith("mp3")) {
                            songDao.insert(song);
                            Log.d(TAG, "add song: " + a.getName());
                        } else {
                            b++;
                            Log.d(TAG, b + "." + a.getName() + " is not mp3");
                        }
                    }
                } else {
                    for (int i=0; i<folders.size(); i++) {
                        Log.d(TAG, "addFolder: a.uri = "+folders.get(i).uri);
                        Log.d(TAG, "addFolder: fold.uri = "+fold.uri);
                        if (!folders.get(i).uri.equals(fold.uri)){
                            countFolders++;
                        }
                        Log.d(TAG, "addFolder: countFolders is "+countFolders+" folder size is "+folders.size());
                        if (countFolders != folders.size()) {
                            Log.d(TAG, "addFolder: folder is already added");
                        } else {
                            folderDao.insert(fold);
                            Log.d(TAG, "addFolder: folder added");
                            int b = 0;
                            for (File c : file) {
                                Song song = new Song();
                                song.name = c.getName();
                                song.uri = c.getPath();
                                song.currentPosition = 0;
                                song.folderPath = fold.uri;
                                if (song.name.endsWith("mp3")) {
                                    songDao.insert(song);
                                    Log.d(TAG, "add song: " + c.getName());
                                } else {
                                    b++;
                                    Log.d(TAG, b + "." + c.getName() + " is not mp3");
                                }
                            }
                        }
                    }
                    countFolders = 0;
                }
            });
            thread.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        db = App.getInstance().getDatabase();
        LiveData<List<Folder>> listLiveData = db.folderDao().getAll();
        //folderFiles = (List<Folder>) listLiveData;
        listLiveData.observe(this, books -> {                                                 //new Observer<List<Song>>()
            folders.clear();
            folders.addAll(books);
            Log.d(TAG, "onResume: LiveData -> folders.size is "+folders.size());
            adapterRecycler.notifyDataSetChanged();
        });
    }
}
