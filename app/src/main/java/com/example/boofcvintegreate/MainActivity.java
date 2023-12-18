package com.example.boofcvintegreate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import boofcv.alg.misc.GPixelMath;
import boofcv.android.ConvertBitmap;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView1, imageView2;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private PreviewView previewView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.viewFinderimage);
        Button captureButton = findViewById(R.id.captureButton);
        Button captureButtontwo = findViewById(R.id.captureButton2);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            initializeCamera();
        }

        captureButton.setOnClickListener(view -> captureImage());
        captureButtontwo.setOnClickListener(view -> captureImageTwo());

    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            } else {
                Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProviderFuture != null) {
            cameraProviderFuture.addListener(() -> {
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = cameraProviderFuture.get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                cameraProvider.unbindAll();
            }, ContextCompat.getMainExecutor(this));
        }
    }

    public void captureImage() {
        if (imageCapture != null) {
            Toast.makeText(this, "Capture One", Toast.LENGTH_SHORT).show();
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    super.onCaptureSuccess(image);
                    Bitmap bitmap = imageProxyToBitmap(image);
                    imageView1.setImageBitmap(bitmap);
                    image.close();

                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    super.onError(exception);
                }
            });
        }
    }

    public void captureImageTwo() {
        if (imageCapture != null) {
            Toast.makeText(this, "Capture two", Toast.LENGTH_SHORT).show();
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    super.onCaptureSuccess(image);
                    Bitmap bitmap = imageProxyToBitmap(image);
                    imageView2.setImageBitmap(bitmap);
                    image.close();

                    double threshold = 0.80;
                    double similarity  =  checkImgSimilarity();

                    if (similarity >= threshold) {
                        Toast.makeText(MainActivity.this, "Images  are same ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Images are different ", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    super.onError(exception);
                }
            });
        }
    }

    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    public double isCheckImage() {

        Bitmap image1 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
        Bitmap image2 = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();

        Planar<GrayU8> boofImage1 = ConvertBitmap.bitmapToPlanar(image1, (Planar<GrayU8>) null, GrayU8.class, null);
        Planar<GrayU8> boofImage2 = ConvertBitmap.bitmapToPlanar(image2, (Planar<GrayU8>) null, GrayU8.class, null);

        Planar<GrayU8> diff = boofImage1.createSameShape();
        GPixelMath.diffAbs(boofImage1, boofImage2, diff);
        double sum = 0;
        for (int y = 0; y < diff.height; y++) {
            for (int x = 0; x < diff.width; x++) {
                sum += diff.getBand(0).unsafe_get(x, y);
            }}
        double meanDifference = sum / (diff.width * diff.height);
        Log.d("check percentage", String.valueOf(meanDifference));
        return meanDifference;
    }

    public  double checkImgSimilarity() {

        Bitmap bmpIMg1 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap();
        Bitmap bmpImg2 = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();

        if (isBitmapEmpty(bmpIMg1) || isBitmapEmpty(bmpImg2)) {
            return 0.0;
        }

        bmpIMg1 = toGrayscale(bmpIMg1);
        bmpImg2 = toGrayscale(bmpImg2);

        bmpIMg1 = ThumbnailUtils.extractThumbnail(bmpIMg1, 32, 32);
        bmpImg2 = ThumbnailUtils.extractThumbnail(bmpImg2, 32, 32);

        int[] pixels1 = getPixelArray(bmpIMg1);
        int[] pixels2 = getPixelArray(bmpImg2);

        int averageColor1 = getAverageOfPixelArray(pixels1);
        int averageColor2 = getAverageOfPixelArray(pixels2);

        int[] p1 = getPixelDeviateWeightsArray(pixels1, averageColor1);
        int[] p2 = getPixelDeviateWeightsArray(pixels2, averageColor2);
        int hammingDistance = getHammingDistance(p1, p2);
        double similarity = calSimilarity(hammingDistance);
        return similarity;
    }

    public static boolean isBitmapEmpty(Bitmap bitmap) {
        return bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0;
    }


    public static double calSimilarity(int hammingDistance) {
        int length = 32 * 32;
        double similarity = (length - hammingDistance) / (double) length;
        similarity = java.lang.Math.pow(similarity, 2);
        return similarity;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static int getAverageOfPixelArray(int[] pixels) {
        long sumRed = 0;
        for (int i = 0; i < pixels.length; i++) {
            sumRed += Color.red(pixels[i]);
        }
        int averageRed = (int) (sumRed / pixels.length);
        return averageRed;
    }

    public static int[] getPixelDeviateWeightsArray(int[] pixels,
                                                    final int averageColor) {
        int[] dest = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
          //  dest[i] = Color.red(pixels[i]) - averageColor > 0  1 : 0;
            dest[i] = Color.red(pixels[i]) - averageColor > 0 ? 1 : 0;

        }
        return dest;
    }

    public static int getHammingDistance(int[] a, int[] b) {
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
          //  sum += a[i] == b[i]  0 : 1;
            sum += (a[i] == b[i]) ? 0 : 1;
        }
        return sum;
    }


    private static int[] getPixelArray(Bitmap bmp) {
        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        return pixels;
    }

}