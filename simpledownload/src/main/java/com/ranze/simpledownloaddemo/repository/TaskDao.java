package com.ranze.simpledownloaddemo.repository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ranze.simpledownloaddemo.core.Segment;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by ranze on 2018/1/19.
 */
@Dao
interface TaskDao {
    @Insert(onConflict = REPLACE)
    void save(Segment segment);

    @Insert(onConflict = REPLACE)
    void save(List<Segment> segments);

    @Query("SELECT * FROM segment WHERE url = :url")
    List<Segment> load(String url);

    @Delete
    void delete(Segment segment);

    @Delete
    void delete(List<Segment> segments);
}
