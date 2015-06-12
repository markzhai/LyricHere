package com.markzhai.lyrichere.utils;

import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class FileUtils {

    public static final int PASTE_MODE_COPY = 0;
    public static final int PASTE_MODE_MOVE = 1;
    private static final String TAG = FileUtils.class.getName();
    private static File COPIED_FILE = null;
    private static int pasteMode = 1;

    private FileUtils() {
    }

    public static synchronized void setPasteSrcFile(File f, int mode) {
        COPIED_FILE = f;
        pasteMode = mode % 2;
    }

    public static synchronized File getFileToPaste() {
        return COPIED_FILE;
    }

    public static synchronized int getPasteMode() {
        return pasteMode;
    }

    static boolean isMusic(File file) {

        Uri uri = Uri.fromFile(file);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));

        if (type == null)
            return false;
        else
            return (type.toLowerCase().startsWith("audio/"));

    }

    static boolean isVideo(File file) {

        Uri uri = Uri.fromFile(file);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));

        if (type == null)
            return false;
        else
            return (type.toLowerCase().startsWith("video/"));
    }

    public static boolean isPicture(File file) {

        Uri uri = Uri.fromFile(file);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));

        if (type == null)
            return false;
        else
            return (type.toLowerCase().startsWith("image/"));
    }

    public static boolean isProtected(File path) {
        return (!path.canRead() && !path.canWrite());
    }

    public static boolean isUnzippable(File path) {
        return (path.isFile() && path.canRead() && path.getName().endsWith(".zip"));
    }


    public static boolean isRoot(File dir) {

        return dir.getAbsolutePath().equals("/");
    }


    public static boolean isSdCard(File file) {

        try {
            return (file.getCanonicalPath().equals(Environment.getExternalStorageDirectory().getCanonicalPath()));
        } catch (IOException e) {
            return false;
        }
    }

    public static Map<String, Long> getDirSizes(File dir) {
        Map<String, Long> sizes = new HashMap<String, Long>();

        try {

            Process du = Runtime.getRuntime().exec("/system/bin/du -b -d1 " + dir.getCanonicalPath(), new String[]{}, Environment.getRootDirectory());

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    du.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\s+");

                String sizeStr = parts[0];
                Long size = Long.parseLong(sizeStr);

                String path = parts[1];

                sizes.put(path, size);
            }

        } catch (IOException e) {
            LogUtils.w(TAG, "Could not execute DU command for " + dir.getAbsolutePath(), e);
        }

        return sizes;

    }

    public static File getDownloadsFolder() {
        return new File("/sdcard/" + Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getDcimFolder() {
        return new File("/sdcard/" + Environment.DIRECTORY_DCIM);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}