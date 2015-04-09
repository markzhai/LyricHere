package com.markzhai.lyrichere.model;

import com.markzhai.lyrichere.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Created by markzhai on 2015/4/8.
 */
@Table(databaseName = AppDatabase.NAME)
public class AModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column
    long time;

    @Column
    Date date;
}