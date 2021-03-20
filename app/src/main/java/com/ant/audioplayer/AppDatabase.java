package com.ant.audioplayer;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class, Folder.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SongDao songDao();
    public abstract FolderDao folderDao();
}
