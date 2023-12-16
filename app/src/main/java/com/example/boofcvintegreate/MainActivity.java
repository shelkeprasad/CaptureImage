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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    super.onCaptureSuccess(image);
                    Bitmap bitmap = imageProxyToBitmap(image);
                    imageView2.setImageBitmap(bitmap);
                    image.close();
                    isCheck();

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


    public void isCheck() {

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

        double percentage = 80.0;
     //   double dynamicThreshold = calculateDynamicThreshold(boofImage1, boofImage2,percentage);
        if (meanDifference < percentage) {
            Toast.makeText(this, " Both Images are the same", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Both Images are different", Toast.LENGTH_LONG).show();
        }
    }


}