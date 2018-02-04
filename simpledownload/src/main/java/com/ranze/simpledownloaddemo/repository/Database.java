package com.ranze.simpledownloaddemo.repository;

import android.arch.persistence.room.RoomDatabase;

import com.ranze.simpledownloaddemo.core.Segment;

/**
 * Created by ranze on 2018/1/19.
 */
@android.arch.persistence.room.Database(entities = {Segment.class}, version = 1, exportSchema = false)
abstract class Database extends RoomDatabase {
    public abstract TaskDao taskDao();
}
