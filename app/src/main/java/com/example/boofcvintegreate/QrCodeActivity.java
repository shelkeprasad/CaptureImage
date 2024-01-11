package com.example.boofcvintegreate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import boofcv.android.camera.CameraPreview;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private CameraPreview cameraPreview;
    int PERMISSION_ALL = 1;
    private ImageView[] markerImageViews = new ImageView[4];
    private ZXingScannerView scannerView;
    private TextView txt;
    private boolean ischeck = false;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private TextureView textureView;
    public static TinyDB tinyDB;


    private ImageView imageView, markerImageView, markerImageViewTopLeft, markerImageViewTopRight, markerImageViewBottomLeft, markerImageViewBottomRight;


    String[] PERMISSIONS = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_qr_code);
        tinyDB = new TinyDB(this);


        imageView = findViewById(R.id.imageView);
        txt = findViewById(R.id.txt);
        textureView = findViewById(R.id.textureviewqrcode);

        markerImageViewTopLeft = findViewById(R.id.markerTopLeft);
        markerImageViewTopRight = findViewById(R.id.markerTopRight);
        markerImageViewBottomLeft = findViewById(R.id.markerBottomLeft);
        markerImageViewBottomRight = findViewById(R.id.markerBottomRight);
        scannerView = findViewById(R.id.scannerView);


        markerImageViews[0] = findViewById(R.id.markerTopLeft);
        markerImageViews[1] = findViewById(R.id.markerTopRight);
        markerImageViews[2] = findViewById(R.id.markerBottomLeft);
        markerImageViews[3] = findViewById(R.id.markerBottomRight);


        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {

            scannerView = new ZXingScannerView(this);
            setContentView(scannerView);
            scannerView.setResultHandler(this);
            scannerView.startCamera();

            // initiateQRCodeScan();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "back pressed", Toast.LENGTH_LONG).show();
        super.onBackPressed();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0 && arePermissionsGranted(grantResults)) {
                // initiateQRCodeScan();
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                Toast.makeText(this, "All permissions are required to scan QR codes", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean arePermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void initiateQRCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a QR Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
        integrator.setTimeout(50);
    }


  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {


            if (result.getContents() == null) {
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
            } else {
                String scannedData = result.getContents();
                String filePath = result.getBarcodeImagePath();
                Toast.makeText(this, "Scanned: " + scannedData, Toast.LENGTH_LONG).show();
                File imageFile = new File(filePath);


                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                      //  imageView.setImageBitmap(bitmap);

                     //   int[] qrCodePosition = findQrCodePosition(bitmap);
                        ResultPoint[] resultPoints = findQrCodePositionValue(bitmap);


                        if (resultPoints != null) {
                            setMarkerAtPosition(resultPoints,bitmap);
                            saveBitmapToStorage(bitmap);
                            String toastMessage = "QR Code Corners:\n" +
                                    "Top-left: (" + resultPoints[0].getX() + ", " + resultPoints[0].getY() + ")\n" +
                                    "Top-right: (" + resultPoints[1].getX() + ", " + resultPoints[1].getY() + ")\n" +
                                    "Bottom-right: (" + resultPoints[2].getX() + ", " + resultPoints[2].getY() + ")\n" +
                                    "Bottom-left: (" + resultPoints[3].getX() + ", " + resultPoints[3].getY() + ")";

                            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(this, "QR Code not found in the image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
               super.onActivityResult(requestCode, resultCode, data);
        }
    }*/


    private void setMarkerAtPosition(int x, int y) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = x;
        params.topMargin = y;
        //  markerImageView.setLayoutParams(params);
        //   markerImageView.setVisibility(View.VISIBLE);
    }
    private void setMarkerAtPosition(ResultPoint[] resultPoints, Bitmap bitmap) {
        if (resultPoints != null && resultPoints.length == 4) {
            for (int i = 0; i < resultPoints.length; i++) {
                setMarker(resultPoints[i], markerImageViews[i], bitmap);
            }
        } else {
            Toast.makeText(this, "Qr point  not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setMarker(ResultPoint point, ImageView markerImageView, Bitmap bitmap) {
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);

                int shownImageWidth = imageView.getWidth();
                int shownImageHeight = imageView.getHeight();
                Log.d("check width", String.valueOf(shownImageWidth));

                float scaleX = bitmap.getWidth() / (float) shownImageWidth;
                float scaleY = bitmap.getHeight() / (float) shownImageHeight;

                int scaledX = (int) (point.getX() / scaleX);
                int scaledY = (int) (point.getY() / scaleY);

                Log.d("check height", String.valueOf(shownImageHeight));
                Log.d("check scalex", String.valueOf(scaleX));
                Log.d("check x", String.valueOf(scaleY));
                Log.d("check x", String.valueOf(scaledX));
                Log.d("check y", String.valueOf(scaledY));


                setMarkerLayoutParams(markerImageView, scaledX, scaledY);

                markerImageView.setVisibility(View.VISIBLE);

                return true;
            }
        });
    }

    private void setMarkerLayoutParams(ImageView marker, int x, int y) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                30, 30
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = x;
        params.topMargin = y;
        marker.setLayoutParams(params);
    }







/*    private void setMarkerLayoutParams(ImageView marker, int x, int y) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) marker.getLayoutParams();
        if (params == null) {
            params = new RelativeLayout.LayoutParams(
                   20,
                   20
            );
        }

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.leftMargin = x - marker.getWidth() / 2;
        params.topMargin = y - marker.getHeight() / 2;
        marker.setLayoutParams(params);
    }*/

  /*  private void setMarkerLayoutParams(ImageView marker, int x, int y) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                30,
                30
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.leftMargin = x - marker.getWidth() / 2; // Adjust for the marker's width
        params.topMargin = y - marker.getHeight() / 2; // Adjust for the marker's height
        marker.setLayoutParams(params);
    }*/



/*    private void setMarkerAtPosition(ResultPoint[] resultPoints) {
        if (resultPoints != null && resultPoints.length == 4) {
            ResultPoint topLeft = resultPoints[0];
            ResultPoint topRight = resultPoints[1];
            ResultPoint bottomLeft = resultPoints[2];
            ResultPoint bottomRight = resultPoints[3];

            setMarkerLayoutParams(markerImageViewTopLeft, (int) topLeft.getX(), (int) topLeft.getY());
            setMarkerLayoutParams(markerImageViewTopRight, (int) topRight.getX(), (int) topRight.getY());
            setMarkerLayoutParams(markerImageViewBottomLeft, (int) bottomLeft.getX(), (int) bottomLeft.getY());
            setMarkerLayoutParams(markerImageViewBottomRight, (int) bottomRight.getX(), (int) bottomRight.getY());

            markerImageViewTopLeft.setVisibility(View.VISIBLE);
            markerImageViewTopRight.setVisibility(View.VISIBLE);
            markerImageViewBottomLeft.setVisibility(View.VISIBLE);
            markerImageViewBottomRight.setVisibility(View.VISIBLE);
        } else {
            markerImageViewTopLeft.setVisibility(View.INVISIBLE);
            markerImageViewTopRight.setVisibility(View.INVISIBLE);
            markerImageViewBottomLeft.setVisibility(View.INVISIBLE);
            markerImageViewBottomRight.setVisibility(View.INVISIBLE);
        }
    }

    private void setMarkerLayoutParams(ImageView marker, int x, int y) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                30, 30
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = x;
        params.topMargin = y;
        marker.setLayoutParams(params);
    }*/

    private int[] findQrCodePosition(Bitmap bitmap) {
        int[] position = null;
        try {
            MultiFormatReader reader = new MultiFormatReader();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
            Result result = reader.decode(binaryBitmap);

            if (result != null) {
                if (result.getBarcodeFormat().equals(com.google.zxing.BarcodeFormat.QR_CODE)) {
                    position = new int[]{(int) result.getResultPoints()[0].getX(), (int) result.getResultPoints()[0].getY()};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position;
    }

    private ResultPoint[] findQrCodePositionValue(Bitmap bitmap) {
        ResultPoint[] resultPoints = null;
        try {
            MultiFormatReader reader = new MultiFormatReader();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
            Result result = reader.decode(binaryBitmap);

            if (result != null) {
                if (result.getBarcodeFormat().equals(com.google.zxing.BarcodeFormat.QR_CODE)) {
                    resultPoints = result.getResultPoints();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultPoints;
    }


    private void takeScreenshot() {
        Toast.makeText(this, "take screenshot call...", Toast.LENGTH_SHORT).show();

        View rootView = getWindow().getDecorView().getRootView();
        if (rootView == null) {
            Toast.makeText(this, "Root view is null", Toast.LENGTH_SHORT).show();
            return;
        }

        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    rootView.setDrawingCacheEnabled(true);

                    Bitmap originalBitmap = rootView.getDrawingCache();
                    if (originalBitmap == null) {
                        Toast.makeText(QrCodeActivity.this, " Drawing cache is null", Toast.LENGTH_SHORT).show();
                        rootView.setDrawingCacheEnabled(false);
                        return;
                    }

                    if (originalBitmap.getWidth() <= 0 || originalBitmap.getHeight() <= 0) {
                        Toast.makeText(QrCodeActivity.this, " Invalid bitmap dimensions", Toast.LENGTH_SHORT).show();
                        rootView.setDrawingCacheEnabled(false);
                        return;
                    }
                    Bitmap bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);

                    saveBitmapToStorage(originalBitmap);
                    Toast.makeText(QrCodeActivity.this, "sucess capture ......", Toast.LENGTH_SHORT).show();

                    imageView.setImageBitmap(bitmap);

                    // finish();
                  /*  new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.run();
                            }
                        }
                    }, 1000);*/
                }
            });
        }


    }
    public void saveBitmapToStorage(Bitmap bitmap) {
        Toast.makeText(this, "call save", Toast.LENGTH_SHORT).show();
        String fileName = "Qrscannormal.jpg";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(this, "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        if (result != null && !result.getText().isEmpty()&&result.getResultPoints().length>=4){
            Toast.makeText(this, "sucess ..open cameara", Toast.LENGTH_SHORT).show();

            ResultPoint[] resultPoints = result.getResultPoints();
            Log.d("qrcodepoint", Arrays.toString(resultPoints));
            tinyDB.putListResultPoint("resultPoints", new ArrayList<>(Arrays.asList(resultPoints)));

          //  tinyDB.putListResultPoint("resultPoints", Arrays.asList(resultPoints));
            Intent intent = new Intent(QrCodeActivity.this, CameraActivity.class);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(this, "Please scaned again ..", Toast.LENGTH_SHORT).show();
        }

       /* scannerView.resumeCameraPreview(this);

        String results = result.getText();
        Toast.makeText(this, results, Toast.LENGTH_SHORT).show();

        if (ischeck == false){
            ischeck = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    takeScreenshot();
                    // takeScreenshotview(getWindow().getDecorView().getRootView());

                }
            },2000);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    scannerView.setVisibility(View.GONE);
                    setContentView(R.layout.activity_qr_code);

                    imageView.setVisibility(View.VISIBLE);
                    txt.setVisibility(View.VISIBLE);
                }
            }, 6000);
        }

     *//*   Runnable screenshotCallback = new Runnable() {
            @Override
            public void run() {
                scannerView.setVisibility(View.GONE);
                setContentView(R.layout.activity_qr_code);
                imageView.setVisibility(View.VISIBLE);
                txt.setVisibility(View.VISIBLE);
            }
        };*/


    }

    /*private void initializeCamera() {
        Toast.makeText(this, "initialize camera.....", Toast.LENGTH_SHORT).show();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        if (textureView == null) {
            // Handle the case where textureView is null
            Toast.makeText(this, "TextureView is null", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                if (textureView == null) {
                    // Handle the case where textureView is null
                    Toast.makeText(this, "TextureView is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        textureView.removeOnLayoutChangeListener(this);

                        // Continue with the camera initialization
                        Preview preview = new Preview.Builder()
                                .setTargetResolution(new Size(textureView.getWidth(), textureView.getHeight()))
                                .setTargetRotation(textureView.getDisplay().getRotation())
                                .build();

                        imageCapture = new ImageCapture.Builder().build();

                        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                        preview.setSurfaceProvider(new Preview.SurfaceProvider() {
                            @Override
                            public void onSurfaceRequested(@NonNull SurfaceRequest request) {
                                SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                                if (surfaceTexture != null) {
                                    request.provideSurface(new Surface(surfaceTexture),
                                            ContextCompat.getMainExecutor(QrCodeActivity.this),
                                            new Consumer<SurfaceRequest.Result>() {
                                                @Override
                                                public void accept(SurfaceRequest.Result result) {
                                                    // Handle provided surface result if needed
                                                }
                                            });
                                }
                            }
                        });

                        cameraProvider.bindToLifecycle(QrCodeActivity.this, cameraSelector, preview, imageCapture);
                    }
                });

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error initializing camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }*/

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


    // unused code
  /*  ImageCapture imageCapture = new ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build();

            imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
        @Override
        public void onCaptureSuccess(@NonNull ImageProxy image) {
            super.onCaptureSuccess(image);

            int width = image.getWidth();
            int height = image.getHeight();
            Log.d("CapturedImageSize", "Width: " + width + ", Height: " + height);

            image.close();
        }

        @Override
        public void onError(@NonNull ImageCaptureException exception) {
            super.onError(exception);
        }
    });*/


    // share point

/*
        if (result!= null){
            ArrayList<PointF> pointsList = new ArrayList<>();
            for (ResultPoint resultPoint : result.getResultPoints()) {
                pointsList.add(new PointF((float) resultPoint.getX(), (float) resultPoint.getY()));
            }
            Intent intent = new Intent(QrCodeActivity.this, CameraActivity.class);
            intent.putParcelableArrayListExtra("resultPoints", pointsList);

            //   Intent intent = new Intent(QrCodeActivity.this, CameraActivity.class);
            //  intent.putExtra("resultPoints", resultPoints);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(this, "Result are getting null ..", Toast.LENGTH_SHORT).show();
        }*/
}