package com.softteco.roadlabpro.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by ppp on 04.05.2015.
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    public static String FILE_PREFIX = "file://";
    public static String SECONDARY_STORAGE_ENV = "SECONDARY_STORAGE";
    public static String APP_LOCAL_DIRECTORY = "/RoadLabPro/";
    public static String AUDIO_FILES_DIRECTORY = "Audio/";
    public static String IMAGE_FILES_DIRECTORY = "Images/";

    public static String clearAllRecordsDir() {
        String dataDirStr = getDataDir(false);
        File dataDir = new File(dataDirStr);
        FileUtils.deleteFile(dataDir, true);
        dataDir.mkdirs();
        Log.d(TAG, " ALLDATA DIR WAS CLEANED! ");
        return dataDirStr;
    }

    public static void copyFile(final String sourceDir, final String sourceFileName,
        final String destinationDir, final String destinationFileName) {
        File sourceFile = new File(sourceDir + sourceFileName);
        if (!sourceFile.exists()) {
            Log.d(TAG, " File not exist: " + sourceFile);
            return;
        }
        File destDir = new File(destinationDir);
        File destinationFile = new File(destinationDir + destinationFileName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try {
            org.apache.commons.io.FileUtils.copyFile(sourceFile, destinationFile);
        } catch (Exception e) {
            Log.e(TAG, "copyFile", e);
        }
    }

    public static String getDataDir() {
        return getDataDir(true);
    }

    public static String getDataDir(boolean mkDirs) {
        File downloadDir = Environment.getExternalStorageDirectory();
        String localPath = downloadDir.getPath() + APP_LOCAL_DIRECTORY;
        checkDir(localPath, mkDirs);
        return localPath;
    }

    public static String checkDir(String path, boolean mkDirs) {
        File outFile = new File(path);
        if (mkDirs) {
            outFile.mkdirs();
        }
        return path;
    }

    public static String getExternalSdCardPath() {
        String path = System.getenv(SECONDARY_STORAGE_ENV);
        return path;
    }

    public static String getExportDataDir(Context c) {
        String sdCard = getExternalSdCardPath();
        String packageName = c.getPackageName();
        String path = sdCard + "/Android/" + packageName;
        return path;
    }

//    public static String getOutDataDir(Context c, String pathToCreate, boolean mkDirs) {
//        String sdCardPath = getExportDataDir(c);
//        File dataDir = Environment.getExternalStorageDirectory();
//        if (!TextUtils.isEmpty(sdCardPath)) {
//            File sdDataDir = new File(sdCardPath);
//            if (sdDataDir.exists() && sdDataDir.canWrite()) {
//                dataDir = sdDataDir;
//            }
//        }
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        String localPath = dataDir.getPath() + APP_LOCAL_DIRECTORY + pathToCreate;
//        checkDir(localPath, mkDirs);
//        return localPath;
//    }

//    public static String getDataDir(String pathToCreate, boolean mkDirs) {
//        File dataDir = Environment.getDataDirectory();
//        String localPath = dataDir.getPath() + APP_LOCAL_DIRECTORY + pathToCreate;
//        checkDir(localPath, mkDirs);
//        return localPath;
//    }

    public static String getDataDir(String pathToCreate, boolean mkDirs) {
        File downloadDir = Environment.getExternalStorageDirectory();
        String localPath = downloadDir.getPath() + APP_LOCAL_DIRECTORY + pathToCreate;
        checkDir(localPath, mkDirs);
        return localPath;
    }

    public static String getImagesDir() {
        return getImagesDir(false);
    }

    public static String getAudioDir() {
        return getAudioDir(false);
    }

    public static String getImagesDir(boolean mkDirs) {
        String localPath = getDataDir(IMAGE_FILES_DIRECTORY, mkDirs);
        return localPath;
    }

    public static String getAudioDir(boolean mkDirs) {
        String localPath = getDataDir(AUDIO_FILES_DIRECTORY, mkDirs);
        return localPath;
    }

    public static String getRandomAudioFileName() {
        final String fileName = UUID.randomUUID().toString() + Constants.MP3_EXT;
        return fileName;
    }

    public static String saveImageFile(final Bitmap b) {
        final String fileName = UUID.randomUUID().toString() + Constants.JPG_EXT;
        final String fullPath = getImagesDir(true) + fileName;
        return saveFile(fileName, fullPath, b);
    }

    public static String saveAudioFile(final Context c, final Bitmap b) {
        final String fileName = UUID.randomUUID().toString() + Constants.MP3_EXT;
        final String fullPath = getAudioDir(true) + fileName;
        return saveFile(fileName, fullPath, b);
    }

    public static String createFile(final String fileFullName) {
        final File f = new File(fileFullName);
        if (!f.exists()) {
            f.mkdirs();
            try {
                if (!f.createNewFile()) {
                    f.delete();
                    f.createNewFile();
                }
            } catch (Exception e) {
                Log.e(TAG, "createFile", e);
                return "";
            }
        } else {
            return "";
        }
        return fileFullName;
    }

    private static String saveFile(final String fileName, final String fullPath, final Bitmap b) {
        final File f = new File(fullPath);
        if (!f.exists()) {
            f.mkdirs();
            try {
                if (!f.createNewFile()) {
                    f.delete();
                    f.createNewFile();
                }
            } catch (Exception e) {
                Log.e(TAG, "saveFile", e);
                return "";
            }
        } else {
            return "";
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "saveFile", e);
            return "";
        }
        return fileName;
    }

    public static boolean deleteImageFile(final String file) {
        final String fileName = getImagesDir() + file;
        return deleteFile(fileName);
    }

    public static boolean deleteAudioFile(final String file) {
        final String fileName = getAudioDir() + file;
        return deleteFile(fileName);
    }

    private static boolean deleteFile(final String file) {
        final File f = new File(file);
        if (f.exists()) {
            return f.delete();
        }
        return false;
    }

    public static boolean deleteFile(String filePath, boolean deleteRecursive) {
        final File file = new File(filePath);
        return deleteFile(file, deleteRecursive);
    }

    public static boolean deleteFile(File file, boolean deleteRecursive) {
        if (file.isDirectory() && deleteRecursive) {
            for (File child : file.listFiles()) {
                deleteFile(child, deleteRecursive);
            }
        }
        return file.delete();
    }

    public static String getImageFilePathUrl(final String file) {
        final String filePath = getImagesDir() + file;
        final String filePathUrl = FILE_PREFIX + filePath;
        return filePathUrl;
    }

    public static File createTmpFile(Context c, String filename) {
        File cacheDir = c.getExternalCacheDir();
        String filePath = cacheDir.getAbsolutePath() + File.separator + filename;
        return new File(filePath);
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        String extension = "";
        if (!TextUtils.isEmpty(fileName)) {
            int pointPlace = fileName.lastIndexOf(".");
            int length = fileName.length();
            extension = fileName.substring(pointPlace + 1, length);
        }
        return extension;
    }

    public static Bitmap loadFileFromExternalStorage(final Context c, final String file) {
        final String filePath = getImageFilePathUrl(file);
        final File f = new File(filePath);
        if (f.exists()) {
            FileInputStream fi;
            Bitmap bitmap;
            try {
                fi = new FileInputStream(f);
                 bitmap = BitmapFactory.decodeStream(fi);
                fi.close();
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
            return bitmap;
        }
        return null;
    }

    //importing database
    public static void importDB(Context context, String currentDBPath, String backupDBPath) {
        try {
            File backupDB = new File(currentDBPath);
            File currentDB = new File(backupDBPath);
            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            //Toast.makeText(context, backupDB.toString(), Toastfr.LENGTH_LONG).show();
            Log.d(TAG, "importDB completed,\ncurrentDBPath: " + currentDBPath + "\nbackupDBPath: " + backupDBPath);
        } catch (Exception e) {
            //Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "importDB error: " + e == null || e.getMessage() == null ? "" : e.getMessage());
        }
    }

    //exporting database
    public static void exportDB(Context context, String currentDBPath, String backupDBPath) {
        try {
            File currentDB = new File(currentDBPath);
            File backupDB = new File(backupDBPath);
            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Log.d(TAG, "exportDB completed,\ncurrentDBPath: " + currentDBPath + "\nbackupDBPath: " + backupDBPath);
            //Toast.makeText(context, backupDB.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "exportDB error: " + e == null || e.getMessage() == null ? "" : e.getMessage());
            //Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static String getExportItemName(final Context context, final long id, final long dateMillis) {
        return getExportItemName(context, id, dateMillis, null);
    }

    public static String getExportItemName(final Context context, final long id, final long dateMillis,
        MeasurementItemType type) {
        Date date = new Date(dateMillis);
        String dateStr = new SimpleDateFormat(TimeUtil.DATE_SIMPLE_FILENAME_FORMAT, Locale.US).format(date);
        int formatIdRes = R.string.fr_settings_db_template_for_export;
        if (type != null && MeasurementItemType.TAG.equals(type)) {
            formatIdRes = R.string.fr_settings_db_template_for_export_tag;
        }
        return context.getString(formatIdRes, String.format(Locale.US, "%03d", id), dateStr);
    }

    public static String getExportItemNameTag(TagModel tag, String projectName, String roadName) {
       long tagId = 0;
        String projectNameStr = "";
        String roadNameStr = "";
        if (tag != null) {
            tagId = tag.getId();
        }
        if (projectName != null) {
            projectNameStr = projectName;
        }
        if (roadName != null) {
            roadNameStr = roadName;
        }
        return String.format(Locale.US, Constants.EXPORT_ITEM_NAME_TAGS_FORMAT, tagId, projectNameStr, roadNameStr);
    }
}
