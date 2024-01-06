package com.example.boofcvintegreate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private ImageView imageView, markerTopLeft, markerTopRight, markerBottomLeft, markerBottomRight;
    private Button btn;
    private VideoView videoView;

    private TextView currentTextView;
    private boolean isVideoPlaying = false;

    private ImageView[] markerImageViews = new ImageView[4];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imgview);
        videoView = findViewById(R.id.videoview);
        // textView = findViewById(R.id.textView);

        markerTopLeft = findViewById(R.id.markerTopLeft);
        markerTopRight = findViewById(R.id.markerTopRight);
        markerBottomLeft = findViewById(R.id.markerBottomLeft);
        markerBottomRight = findViewById(R.id.markerBottomRight);


        markerImageViews[0] = findViewById(R.id.markerTopLeft);
        markerImageViews[1] = findViewById(R.id.markerTopRight);
        markerImageViews[2] = findViewById(R.id.markerBottomLeft);
        markerImageViews[3] = findViewById(R.id.markerBottomRight);

        initializeCamera();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                takeScreenshot();
            }
        }, 3000);

        markerTopLeft.setOnClickListener(View -> {
            RelativeLayout relativeLayout = findViewById(R.id.relativelayout);
            removeTextView(relativeLayout);

            Toast.makeText(this, "Click Circle  ... ", Toast.LENGTH_SHORT).show();
        });
        markerBottomLeft.setOnClickListener(View -> {

            if (!isVideoPlaying) {
                openVideoView();
            } else {
                showImageView();
            }
        });
        markerBottomRight.setOnClickListener(View -> {
            openLink();
        });
        markerTopRight.setOnClickListener(View -> {
            showText();
        });

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

    @Override
    public void onBackPressed() {
        if (isVideoPlaying) {
            showImageView();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                TextureView textureView = findViewById(R.id.textureview);

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
                                    ContextCompat.getMainExecutor(CameraActivity.this),

                                    new Consumer<SurfaceRequest.Result>() {
                                        @Override
                                        public void accept(SurfaceRequest.Result result) {

                                        }
                                    });
                        }
                    }
                });

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // working single img
    private void takeScreenshot() {
        TextureView textureView = findViewById(R.id.textureview);
        Bitmap screenshotBitmap = textureView.getBitmap();

        saveBitmapToStorage(screenshotBitmap);

        Toast.makeText(CameraActivity.this, "Success capturing...", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textureView.setVisibility(View.GONE);
                imageView.setImageBitmap(screenshotBitmap);
                ResultPoint[] resultPoints = findQrCodePositionValue(screenshotBitmap);
                Log.d("resultpoints", Arrays.toString(resultPoints));
                setMarkerAtPosition(resultPoints, screenshotBitmap);
            }
        }, 3000);
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
                40, 40
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = x;
        params.topMargin = y;
        marker.setLayoutParams(params);
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


    private void openLink() {
        RelativeLayout relativeLayout = findViewById(R.id.relativelayout);
        removeTextView(relativeLayout);

        Toast.makeText(this, "click on openlink  .......", Toast.LENGTH_SHORT).show();
        String link = "https://www.google.com/search?q=android+studio&oq=andro&gs_lcrp=EgZjaHJvbWUqEggEEAAYQxiDARixAxiABBiKBTIGCAAQRRg5MgYIARBFGDsyDggCEEUYJxg7GIAEGIoFMgYIAxAjGCcyEggEEAAYQxiDARixAxiABBiKBTISCAUQABhDGIMBGLEDGIAEGIoFMgwIBhAAGEMYgAQYigUyCggHEAAYsQMYgAQyCggIEAAYsQMYgAQyCggJEAAYsQMYgATSAQk4NDMwajBqMTWoAgCwAgA&sourceid=chrome&ie=UTF-8";

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(link);
        ViewGroup parent = findViewById(R.id.relativelayout);
        parent.addView(webView);

    }

    private void showText() {
        RelativeLayout relativeLayout = findViewById(R.id.relativelayout);
        String textToShow = "Hello Android";

        currentTextView = new TextView(this);
        currentTextView.setText(textToShow);
        currentTextView.setTextColor(getResources().getColor(android.R.color.white));
        currentTextView.setBackgroundColor(getResources().getColor(android.R.color.black));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.markerTopRight);
        layoutParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.markerTopRight);
        layoutParams.topMargin = 10;
        layoutParams.rightMargin = 10;

        currentTextView.setLayoutParams(layoutParams);
        relativeLayout.addView(currentTextView);

    }

    private void removeTextView(RelativeLayout relativeLayout) {
        if (currentTextView != null) {
            relativeLayout.removeView(currentTextView);
            currentTextView = null;
        }
    }

    private void openVideoView() {
        isVideoPlaying = true;
        RelativeLayout relativeLayout = findViewById(R.id.relativelayout);
        removeTextView(relativeLayout);

        markerTopLeft.setVisibility(View.GONE);
        markerTopRight.setVisibility(View.GONE);
        markerBottomLeft.setVisibility(View.GONE);
        markerBottomRight.setVisibility(View.GONE);

        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        String videoLink = "https://video-ssl.itunes.apple.com/itunes-assets/Video82/v4/a3/ef/25/a3ef253a-208e-3cbc-cbf0-bc444dae2f8d/mzvf_6313901593442783545.640x354.h264lc.U.p.m4v";


        if (videoLink != null) {
            videoView.setMediaController(new android.widget.MediaController(this));

            videoView.setVideoURI(Uri.parse(videoLink));
            videoView.start();
        } else {
            Toast.makeText(this, "video url null ....", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageView() {

        markerTopLeft.setVisibility(View.VISIBLE);
        markerTopRight.setVisibility(View.VISIBLE);
        markerBottomLeft.setVisibility(View.VISIBLE);
        markerBottomRight.setVisibility(View.VISIBLE);

        isVideoPlaying = false;
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
    }



        /*private void initializeCamera() {
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

    }*/

    /*   private void takeScreenshot() {
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
                        Toast.makeText(CameraActivity.this, " Drawing cache is null", Toast.LENGTH_SHORT).show();
                        rootView.setDrawingCacheEnabled(false);
                        return;
                    }

                    if (originalBitmap.getWidth() <= 0 || originalBitmap.getHeight() <= 0) {
                        Toast.makeText(CameraActivity.this, " Invalid bitmap dimensions", Toast.LENGTH_SHORT).show();
                        rootView.setDrawingCacheEnabled(false);
                        return;
                    }
                    Bitmap bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);

                    saveBitmapToStorage(originalBitmap);
                    Toast.makeText(CameraActivity.this, "sucess capture ......", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },4000);

                }
            });
        }



    }*/


    // working /

  /*  private void takeScreenshot() {
        Toast.makeText(this, "Take screenshot call...", Toast.LENGTH_SHORT).show();

        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);

        Bitmap originalBitmap = rootView.getDrawingCache();
        if (originalBitmap == null) {
            Toast.makeText(CameraActivity.this, "Drawing cache is null", Toast.LENGTH_SHORT).show();
            rootView.setDrawingCacheEnabled(false);
            return;
        }

        Bitmap screenshotBitmap = Bitmap.createBitmap(originalBitmap);
        Canvas canvas = new Canvas(screenshotBitmap);

        View cameraPreview = previewView;
        if (cameraPreview != null) {
            cameraPreview.draw(canvas);
        }

        saveBitmapToStorage(screenshotBitmap);
        Toast.makeText(CameraActivity.this, "Success capturing...", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },3000);
    }*/

    //
       /* private void takeScreenshot() {
        Toast.makeText(this, "Take screenshot call...", Toast.LENGTH_SHORT).show();

        TextureView textureView = findViewById(R.id.textureview);
        Bitmap cameraBitmap = textureView.getBitmap();

        View rootView = getWindow().getDecorView().getRootView();
        Bitmap otherViewsBitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(otherViewsBitmap);
        rootView.draw(canvas);

        Bitmap combinedBitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas combinedCanvas = new Canvas(combinedBitmap);
        combinedCanvas.drawBitmap(otherViewsBitmap, 0, 0, null);
        combinedCanvas.drawBitmap(cameraBitmap, 0, 0, null);

        saveBitmapToStorage(combinedBitmap);

        Toast.makeText(CameraActivity.this, "Success capturing...", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // finish();
                textureView.setVisibility(View.GONE);
                imageView.setImageBitmap(combinedBitmap);
                ResultPoint[] resultPoints = findQrCodePositionValue(combinedBitmap);
                Log.d("resultpoints", Arrays.toString(resultPoints));
                setMarkerAtPosition(resultPoints,combinedBitmap);


            }
        }, 2000);
    }*/


}