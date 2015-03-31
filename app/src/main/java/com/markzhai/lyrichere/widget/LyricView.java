package com.markzhai.lyrichere.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.markzhai.lyrichere.R;
import com.markzhai.lyrichere.model.Lyric;
import com.markzhai.lyrichere.utils.LyricUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yifan on 5/13/14.
 */
public class LyricView extends TextView {
    private static final String TAG = LyricView.class.getSimpleName();

    private static final int DY = 50;
    private Lyric mLyric;
    private Paint mCurrentPaint;
    private Paint mPaint;
    private float mMiddleX;
    private float mMiddleY;
    private int mHeight;

    private int mLyricIndex = 0;
    private int mLyricSentenceLength;
    private boolean mTouchActionMoving = false;
    private boolean mIsNeedUpdate = false;
    private float mLastEffectY = 0;

    // Use duration of current line of lyric to sleep
    // private long currentDuringTime;
    private int mIsTouched = 0;

    private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        }
    };

    public LyricView(Context context) {
        super(context);
        init(context);
    }

    public LyricView(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public LyricView(Context context, AttributeSet attr, int i) {
        super(context, attr, i);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        int backgroundColor = sharedPreferences.getInt(context.getString(R.string.pref_key_background_color), Color.BLACK);
        int highlightColor = sharedPreferences.getInt(context.getString(R.string.pref_key_highlight_color), Color.RED);
        int normalColor = sharedPreferences.getInt(context.getString(R.string.pref_key_normal_color), Color.WHITE);

        setBackgroundColor(backgroundColor);

        // Non-highlight part
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(36);
        mPaint.setColor(normalColor);
        mPaint.setTypeface(Typeface.SERIF);

        // highlight part, current lyric
        mCurrentPaint = new Paint();
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setColor(highlightColor);
        mCurrentPaint.setTextSize(36);
        mCurrentPaint.setTypeface(Typeface.SANS_SERIF);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mCurrentPaint.setTextAlign(Paint.Align.CENTER);
        setHorizontallyScrolling(true);
        setMovementMethod(new ScrollingMovementMethod());
    }

    private int drawText(Canvas canvas, Paint paint, String text, float startY) {
        int line = 0;
        float textWidth = mPaint.measureText(text);
        final int width = getWidth() - 85;
        if (textWidth > width) {
            int length = text.length();
            int startIndex = 0;
            int endIndex = Math.min((int) ((float) length * (width / textWidth)), length - 1);
            int perLineLength = endIndex - startIndex;
            //Log.d(TAG, String.valueOf(endIndex));

            LinkedList<String> lines = new LinkedList<String>();
            lines.add(text.substring(startIndex, endIndex));
            while (endIndex < length - 1) {
                startIndex = endIndex;
                endIndex = Math.min(startIndex + perLineLength, length - 1);
                lines.add(text.substring(startIndex, endIndex));
            }
            int linesLength = lines.size();
            for (String str : lines) {
                ++line;
                if (startY < mMiddleY)
                    canvas.drawText(str, mMiddleX, startY - (linesLength - line) * DY, paint);
                else
                    canvas.drawText(str, mMiddleX, startY + (line - 1) * DY, paint);
            }
        } else {
            ++line;
            mPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, mMiddleX, startY, paint);
        }
        //Log.d(TAG, "Draw line: " + line);
        return line;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyric == null)
            return;
        List<Lyric.Sentence> sentenceList = mLyric.sentenceList;
        if (sentenceList == null || sentenceList.isEmpty() || mLyricIndex == -2) {
            return;
        }
        canvas.drawColor(0xEFeffff);

        float currY;

        if (mLyricIndex > -1) {
            // Current line with highlighted color
            currY = mMiddleY + DY * drawText(
                    canvas, mCurrentPaint, sentenceList.get(mLyricIndex).content, mMiddleY);
        } else {
            // First line is not from timestamp 0
            currY = mMiddleY + DY;
        }

        // Draw sentences afterwards
        int size = sentenceList.size();
        for (int i = mLyricIndex + 1; i < size; i++) {
            if (currY > mHeight) {
                break;
            }
            // Draw and Move down
            currY += DY * drawText(canvas, mPaint, sentenceList.get(i).content, currY);
            // canvas.translate(0, DY);
        }

        currY = mMiddleY - DY;

        // Draw sentences before current one
        for (int i = mLyricIndex - 1; i >= 0; i--) {
            if (currY < 0) {
                break;
            }
            // Draw and move upwards
            currY -= DY * drawText(canvas, mPaint, sentenceList.get(i).content, currY);
            // canvas.translate(0, DY);
        }

        if (mIsTouched > 0) {
            mPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.format("%s - %s", mLyric.artist, mLyric.title), 10, 50, mPaint);
            canvas.drawText("offset: " + mLyric.offset, 10, 150, mPaint);
            if (mLyricIndex >= 0) {
                int seconds = (int) ((mLyric.sentenceList.get(mLyricIndex).fromTime / 1000));
                int minutes = seconds / 60;
                seconds = seconds % 60;
                canvas.drawText(String.format("%02d:%02d", minutes, seconds), 10, 100, mPaint);
            }
            --mIsTouched;
            mPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        mMiddleX = w * 0.5f; // remember the center of the screen
        mHeight = h;
        mMiddleY = h * 0.5f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final boolean superResult = super.onTouchEvent(event);

        boolean handled = false;
        boolean offsetChanged = false;

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mIsTouched = 3;
                mTouchActionMoving = true;
                if (mTouchActionMoving) {
                    float y = event.getY();
                    if (mLastEffectY != 0) {
                        if (mLastEffectY - y > 10) {
                            int times = (int) ((mLastEffectY - y) / 10);
                            mLastEffectY = y;
                            mLyric.offset += times * -100;
                            offsetChanged = true;
                        } else if (mLastEffectY - y < -10) {
                            int times = -(int) ((mLastEffectY - y) / 10);
                            mLastEffectY = y;
                            mLyric.offset += times * 100;
                            offsetChanged = true;
                        }
                    }
                }
                handled = true;
                break;
            case MotionEvent.ACTION_DOWN:
                handled = true;
                mLastEffectY = event.getY();
                mIsTouched = 3;
                break;
            case MotionEvent.ACTION_UP:
                System.currentTimeMillis();
                mTouchActionMoving = false;
                mLastEffectY = 0;
                handled = true;
                break;
            default:
                break;
        }

        if (handled) {
            if (offsetChanged) {
                mIsNeedUpdate = true;
            }
            invalidate();
            return true;
        }

        return superResult;
    }

    /**
     * @param time Timestamp of current sentence
     * @return Timestamp of next sentence, -1 if is last sentence.
     */
    public long updateIndex(long time) {
        // Current index is last sentence
        if (mLyricIndex >= mLyricSentenceLength - 1) {
            mLyricIndex = mLyricSentenceLength - 1;
            return -1;
        }

        // Get index of sentence whose timestamp is between its startTime and currentTime.
        mLyricIndex = LyricUtils.getSentenceIndex(mLyric, time, mLyricIndex, mLyric.offset);

        // New current index is last sentence
        if (mLyricIndex >= mLyricSentenceLength - 1) {
            mLyricIndex = mLyricSentenceLength - 1;
            return -1;
        }

        return mLyric.sentenceList.get(mLyricIndex + 1).fromTime + mLyric.offset;
    }

    public synchronized void setLyric(Lyric lyric, boolean resetIndex) {
        mLyric = lyric;
        mLyricSentenceLength = mLyric.sentenceList.size();
        if (resetIndex) {
            mLyricIndex = 0;
        }
    }

    public int getLyricIndex() {
        return mLyricIndex;
    }

    public void setLyricIndex(int index) {
        mLyricIndex = index;
    }

    public String getCurrentSentence() {
        if (mLyricIndex >= 0 && mLyricIndex < mLyricSentenceLength) {
            return mLyric.sentenceList.get(mLyricIndex).content;
        }
        return null;
    }

    /**
     * Check if view need to update due to user input.
     *
     * @return Whether need update view.
     */
    public boolean checkUpdate() {
        if (mIsNeedUpdate) {
            mIsNeedUpdate = false;
            return true;
        }
        return false;
    }

    public Lyric getLyric() {
        return mLyric;
    }

    public synchronized void setLyric(Lyric lyric) {
        setLyric(lyric, true);
    }
}