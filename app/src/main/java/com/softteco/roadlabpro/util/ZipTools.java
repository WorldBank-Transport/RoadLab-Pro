package com.softteco.roadlabpro.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipTools {

    public static String TAG = ZipTools.class.getSimpleName();

    public static String SLASH = "/";
    public static String ZIP_EXT = ".zip";

    final static int BUFFER = 8192;

    public static boolean createZipArchive(String srcFolder, File destFile) {
        try {
            FileOutputStream destFileStream = new FileOutputStream(destFile);
            ZipOutputStream outStream = new ZipOutputStream(new BufferedOutputStream(destFileStream));
            addDirIntoZipStream(srcFolder, "", outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            Log.e(TAG, "createZipArchive", e);
            return false;
        }
        return true;
    }

    private static boolean addDirIntoZipStream(String rootDir, String srcDir, ZipOutputStream out) throws Exception {
        Log.i(TAG, "createZipArchive, Adding dir: " + srcDir);
        File subDir = new File(rootDir + SLASH + srcDir);
        String curFile = "";
        if (subDir.isDirectory()) {
            String files[] = subDir.list();
            for (String sd : files) {
                // get a list of files from current directory
                curFile = subDir.getPath() + SLASH + sd;
                File f = new File(curFile);
                if (f.isDirectory()) {
                    addDirIntoZipStream(rootDir, srcDir + SLASH + sd, out);
                } else { //it is just a file
                    addFileIntoZipStream(rootDir, srcDir + SLASH + sd, out);
                }
            }
        } else {
            addFileIntoZipStream(rootDir, srcDir, out);
        }
        return true;
    }

    private static boolean addFileIntoZipStream(String rootDir, String filePath, ZipOutputStream out) throws Exception {
        Log.i(TAG, "createZipArchive, Adding file: " + filePath);
        BufferedInputStream origin = null;
        File f = new File(rootDir + SLASH + filePath);
        FileInputStream fi = new FileInputStream(f);
        origin = new BufferedInputStream(fi, BUFFER);
        ZipEntry entry = new ZipEntry(filePath);
        out.putNextEntry(entry);
        writeData(origin, out);
        return true;
    }

    public static boolean isZipFileExists(String path) {
        return new File(path).exists();
    }

    public static List<ZipEntry> getZipEntries(String path) {
        return getZipEntriesFile(path);
    }

    public static List<ZipEntry> getZipEntriesFile(String path) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(new File(path));
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
        if (zip != null) {
            return getZipEntriesFile(zip);
        }
        return null;
    }

    public static List<ZipEntry> getZipEntriesFile(ZipFile zip) {
        Enumeration<? extends ZipEntry> list = null;
        try {
            list = zip.entries();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
        return (List) Collections.list(list);
    }

    private static void writeData(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[BUFFER];
        int count = 0;
        while ((count = is.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, count);
            os.flush();
        }
    }

    public static List<String> getZipEntriesStr(String path) {
        List<ZipEntry> list = getZipEntries(path);
        return getZipEntriesStr(list, new String[] {SLASH});
    }

    public static List<String> getZipEntriesStr(List<ZipEntry> entryZipList, String [] filter) {
        List<String> entryStrList = new ArrayList<String>();
        if (entryZipList == null) {
            return null;
        }
        String name = "";
        for (ZipEntry e : entryZipList) {
            name = e.getName();
            if (!checkFilter(name, filter)) {
                entryStrList.add(name);
            }
        }
        return entryStrList;
    }

    public static boolean checkFilter(String str, String[] filter) {
        for (String s : filter) {
            if (str.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static byte[] readEntryFromZipFile(String path, String entryFilePath) {
        byte[] outStr = null;
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(entryFilePath)) {
            return null;
        }
        try {
            ZipFile zip = new ZipFile(new File(path));
            List<ZipEntry> entries = getZipEntries(path);
            ZipEntry readEntry = null;
            for (ZipEntry e : entries) {
                if (entryFilePath.equals(e.getName())) {
                    Log.d(TAG, "File name: " + e.getName() +
                            "; size: " + e.getSize() + "; compressed size: " + e.getCompressedSize());
                    readEntry = e;
                    break;
                }
            }
            if (readEntry != null) {
                outStr = readZipEntry(zip, readEntry);
            }
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
        return outStr;
    }

    private static byte[] readZipEntry(ZipFile zip, ZipEntry entry) throws IOException {
        InputStream is = zip.getInputStream(entry);
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        writeData(is, oStream);
        byte[] array = oStream.toByteArray();
        return array;
    }
}
