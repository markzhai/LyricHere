package com.markzhai.lyrichere.model;

import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.provider.BaseSyncableProviderModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Lyric extends BaseSyncableProviderModel {
    private static final String TAG = Lyric.class.getSimpleName();

    public String title;
    public String artist;
    public String album;
    public String by;
    public String author;
    public int offset;
    public long length;
    public List<Sentence> sentenceList = new ArrayList<Sentence>(100);

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Title: " + title + "\n")
                .append("Artist: " + artist + "\n")
                .append("Album: " + album + "\n")
                .append("By: " + by + "\n")
                .append("Author: " + author + "\n")
                .append("Length: " + length + "\n")
                .append("Offset: " + offset + "\n");
        if (sentenceList != null) {
            for (Sentence sentence : sentenceList) {
                stringBuilder.append(sentence.toString() + "\n");
            }
        }
        return stringBuilder.toString();
    }


    @Override
    public Uri getDeleteUri() {
        return null;
    }

    @Override
    public Uri getInsertUri() {
        return null;
    }

    @Override
    public Uri getUpdateUri() {
        return null;
    }

    @Override
    public Uri getQueryUri() {
        return null;
    }

    public void addSentence(String content, long time) {
        sentenceList.add(new Sentence(content, time));
    }

    public static class SentenceComparator implements Comparator<Sentence> {
        @Override
        public int compare(Sentence sent1, Sentence sent2) {
            return (int) (sent1.fromTime - sent2.fromTime);
        }
    }

    public class Sentence {
        public String content;
        public long fromTime;

        public Sentence(String content, long fromTime) {
            this.content = content;
            this.fromTime = fromTime;
        }

        public String toString() {
            return String.valueOf(fromTime) + ": " + content;
        }
    }
}