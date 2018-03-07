package com.ranze.simpledownload.core;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by ranze on 2018/1/18.
 */
@Entity(indices = {@Index("url")})
public class Segment {
    @PrimaryKey
    @NonNull
    public String tag;
    public String url;
    public long startPos;
    public long endPos;
    public long writePos;
    public String filePath;
    public String fileName;


    @Override
    public String toString() {
        return "Segment{" +
                "tag='" + tag + '\'' +
                ", url='" + url + '\'' +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", writePos=" + writePos +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
