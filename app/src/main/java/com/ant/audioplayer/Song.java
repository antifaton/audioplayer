package com.ant.audioplayer;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song extends ListItem implements RowType {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public int duration;
    public int currentPosition;
    public String folderPath;

    public String getFolderPath() {
        return folderPath;
    }

    public long getId(){
        return id;
    }

    public int getDuration(){
        return this.duration;
    }

    public int getCurrentPosition(){
        return this.currentPosition;
    }

}
