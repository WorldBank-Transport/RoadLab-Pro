package com.softteco.roadlabpro.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

/**
 * Utility class for work {@link android.graphics.Bitmap}.
 */
public final class BitmapUtil {

    private BitmapUtil() {
        /**/
    }

    public static final int DEFAULT_IMAGE_MAX_SIZE = 1024;

    /**
     * The method to calculate a sample size value that is a power of two based on a target
     * width and height.
     *
     * @param options   see {@link android.graphics.BitmapFactory.Options}
     * @param reqWidth  width of the surface where the {@link android.graphics.Bitmap} will be
     *                  drawn on, in pixels.
     * @param reqHeight height of the surface where the {@link android.graphics.Bitmap} will be
     *                  drawn on, in pixels.
     * @return the largest inSampleSize value that is a power of 2 and keeps both height and width
     * larger than reqWidth and reqHeight.
     */
    public static int calculateInSampleSize(final BitmapFactory.Options options,
                                            final int reqWidth, final int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void rotateImage(String filePath, int rotate) {
        try {
            Bitmap bm = decodeBitmap(filePath, DEFAULT_IMAGE_MAX_SIZE, DEFAULT_IMAGE_MAX_SIZE);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
                    matrix, true);
            FileOutputStream fos = new FileOutputStream(filePath);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            /**/
        }
    }

    /**
     * The method decodes a bitmap from a file containing it minimizing the memory use, known that the bitmap
     * will be drawn in a surface of reqWidth x reqHeight.
     *
     * @param path      absolute path to the file containing the image.
     * @param reqWidth  width of the surface where the {@link android.graphics.Bitmap} will be
     *                  drawn on, in pixels.
     * @param reqHeight height of the surface where the {@link android.graphics.Bitmap} will be
     *                  drawn on, in pixels.
     * @return decoded {@link android.graphics.Bitmap}
     */
    public static Bitmap decodeBitmap(final String path,
                                      final int reqWidth, final int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * The method decodes an byte array from the specified {@link android.graphics.Bitmap}.
     *
     * @param bitmap see {@link android.graphics.Bitmap}
     * @return this {@link android.graphics.Bitmap} as a byte array
     */
    public static byte[] getBytes(final Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        return stream.toByteArray();
    }

    /**
     * The method decodes an immutable bitmap from the specified byte array.
     *
     * @param image byte array of compressed image data
     * @return this byte array as a {@link android.graphics.Bitmap}
     */
    public static Bitmap getPhoto(final byte[] image) {
        return BitmapFactory.decodeByteArray(image, 75, image.length);
    }
}
