package com.ant.audioplayer;

import android.view.autofill.AutofillId;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public int duration;
    public int currentPosition;
    public String uri;

    public long getId(){
        return id;
    }

    public String getTitle(){
        return this.title;
    }

    public int getDuration(){
        return this.duration;
    }

    public int getCurrentPosition(){
        return this.currentPosition;
    }

    public String getUri(){
        return this.uri;
    }

}
