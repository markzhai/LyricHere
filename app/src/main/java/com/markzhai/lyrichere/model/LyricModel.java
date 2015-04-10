package com.markzhai.lyrichere.model;

import com.markzhai.lyrichere.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by markzhai on 2015/4/8.
 */
@Table(databaseName = AppDatabase.NAME)
public class LyricModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    public long _id;

    @Column
    public String title;

    @Column
    public String artist;

    @Column
    public String album;

    @Column
    public String by;

    @Column
    public String author;

    @Column
    public int offset;

    @Column
    public long length;

    @Override
    public String toString() {
        return "LyricModel{" +
                "_id=" + _id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", by='" + by + '\'' +
                ", author='" + author + '\'' +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }
}