package com.markzhai.lyrichere.model;

/**
 * Created by markzhai on 2015/4/9.
@ModelView(query = "SELECT * FROM LyricModel WHERE title = ''", databaseName = AppDatabase.NAME)
public class LyricModelView extends BaseModelView<LyricModel> {
    @Column
    long model_order;
}
 */