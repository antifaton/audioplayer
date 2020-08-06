package com.ant.audioplayer;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDao {

    @Query("SELECT * FROM Song")
    LiveData<List<Song>> getAll();

    @Query("SELECT * FROM song WHERE id = :id")
    Song getSongById(long id);

    @Insert
    void insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);
}
