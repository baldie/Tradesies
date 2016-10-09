package com.mobile.tradesies;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by David on 4/18/2015.
 */
public class ImageUtil {

    public static String toBase64(Bitmap bmp) {
        return Base64.encodeToString(ToByteArray(bmp), Base64.NO_WRAP);
    }

    public static String toBase64(Drawable drawable) {
        return Base64.encodeToString(ToByteArray(drawable), Base64.NO_WRAP);
    }

    private static byte[] ToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private static byte[] ToByteArray(Drawable drawable){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = drawableToBitmap(drawable);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final int width = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().height() : drawable.getIntrinsicHeight();

        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width,
                height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap reduceSize(Bitmap bitmapOrg, int newDimension){
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        int newWidth = newDimension;
        int newHeight = newDimension;

        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        return Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
    }

    public static Bitmap reduceSize(ContentResolver resolver, int required_size, Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        Bitmap bmp = BitmapFactory.decodeStream(resolver.openInputStream(selectedImage), null, o2);
        return reduceSize(bmp, required_size);
    }

    public static Bitmap rotate90right(Bitmap bitmapOrg){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        // recreate the new Bitmap
        return Bitmap.createBitmap(bitmapOrg, 0, 0, bitmapOrg.getWidth(), bitmapOrg.getHeight(), matrix, true);
    }

    public static java.net.URI saveToFileSystem(Bitmap bitmap){
        String uniqueID = UUID.randomUUID().toString();
        File destination = new File(Environment.getExternalStorageDirectory(), uniqueID + ".png");

        FileOutputStream fo;
        try {
            if (destination.createNewFile()) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);

                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return destination.toURI();
    }

    public static Bitmap getBitmapFromUri(ContentResolver resolver, java.net.URI uri){
        Bitmap bmp = null;
        try {
            Uri imageUri = android.net.Uri.parse(uri.toString());
            bmp = MediaStore.Images.Media.getBitmap(resolver, imageUri);
        }
        catch(IOException ioex){
            ioex.printStackTrace();
        }
        return bmp;
    }

    public static String mapServerPath(String relativePath){
        return Config.API_ENDPOINT + "/" + relativePath.replace("\\", "/");
    }
}