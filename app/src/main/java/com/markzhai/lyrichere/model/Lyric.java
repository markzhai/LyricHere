package com.markzhai.lyrichere.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Lyric {
    private static final String TAG = Lyric.class.getSimpleName();
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private String mBy;
    private String mAuthor;
    private int mOffset;
    private long mLength;
    private List<Sentence> mSentenceList = new ArrayList<Sentence>(100);

    public List<Sentence> getSentenceList() {
        return mSentenceList;
    }

    public void setSentenceList(List<Sentence> sentenceList) {
        this.mSentenceList = sentenceList;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Title: " + getTitle() + "\n");
        stringBuilder.append("Artist: " + getArtist() + "\n");
        stringBuilder.append("Album: " + getAlbum() + "\n");
        stringBuilder.append("By: " + getBy() + "\n");
        stringBuilder.append("Author: " + getAuthor() + "\n");
        stringBuilder.append("Length: " + mLength + "\n");
        stringBuilder.append("Offset: " + mOffset + "\n");
        if (mSentenceList != null) {
            for (Sentence sentence : mSentenceList) {
                stringBuilder.append(sentence.toString() + "\n");
            }
        }
        return stringBuilder.toString();
    }

    public int getOffset() {
        return this.mOffset;
    }

    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    public void addOffset(int offset) {
        this.mOffset += offset;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public String getBy() {
        return mBy;
    }

    public void setBy(String by) {
        mBy = by;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public long getLength() {
        return mLength;
    }

    public void setLength(long length) {
        this.mLength = length;
    }

    public void addSentence(String content, long time) {
        mSentenceList.add(new Sentence(content, time));
    }

    public static class SentenceComparator implements Comparator<Sentence> {
        @Override
        public int compare(Sentence sent1, Sentence sent2) {
            return (int) (sent1.getFromTime() - sent2.getFromTime());
        }
    }

    public class Sentence {
        private String mContent;
        private long mFromTime;

        public Sentence(String content, long fromTime) {
            this.mContent = content;
            this.mFromTime = fromTime;
        }

        public String getContent() {
            return mContent;
        }

        public long getFromTime() {
            return mFromTime;
        }

        public String toString() {
            return String.valueOf(mFromTime) + ": " + mContent;
        }
    }
}