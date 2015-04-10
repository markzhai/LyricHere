package com.markzhai.lyrichere.model;

import com.markzhai.lyrichere.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.structure.BaseModelView;

/**
 * Created by markzhai on 2015/4/9.
 */
@ModelView(query = "SELECT * FROM LyricModel WHERE title = ''", databaseName = AppDatabase.NAME)
public class LyricModelView extends BaseModelView<LyricModel> {
    @Column
    long model_order;
}