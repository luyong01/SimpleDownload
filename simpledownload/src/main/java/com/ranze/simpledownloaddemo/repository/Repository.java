package com.ranze.simpledownloaddemo.repository;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.ranze.simpledownloaddemo.core.Segment;

import java.util.List;

/**
 * Created by ranze on 2018/1/20.
 */

public class Repository {
    private static volatile Repository sInstance;

    private TaskDao mTaskDao;

    public static Repository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (Repository.class) {
                if (sInstance == null) {
                    sInstance = new Repository(context);
                }
            }
        }
        return sInstance;
    }

    private Repository(Context context) {
        Database database = Room.databaseBuilder(context, Database.class, "SimpleDownloadDB").build();
        mTaskDao = database.taskDao();
    }

    public void saveSegment(Segment segment) {
        mTaskDao.save(segment);
    }

    public void saveSegment(List<Segment> segments) {
        mTaskDao.save(segments);
    }

    public void deleteSegment(Segment segment) {
        mTaskDao.delete(segment);
    }

    public void deleteSegment(List<Segment> segments) {
        mTaskDao.delete(segments);
    }

    public List<Segment> loadSegment(String url, boolean deleteAfterLoad) {
        List<Segment> segments = mTaskDao.load(url);
        if (deleteAfterLoad) {
            deleteSegment(segments);
        }
        return segments;
    }
}
