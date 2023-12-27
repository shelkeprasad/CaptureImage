package com.example.boofcvintegreate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import boofcv.android.camera.CameraPreview;

public class QrCodeActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private CameraPreview cameraPreview;
    int PERMISSION_ALL = 1;
    private ImageView[] markerImageViews = new ImageView[4];

    private ImageView imageView,markerImageView,markerImageViewTopLeft,markerImageViewTopRight,markerImageViewBottomLeft,markerImageViewBottomRight;



    String[] PERMISSIONS = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_qr_code);

        imageView = findViewById(R.id.imageView);
        markerImageViewTopLeft = findViewById(R.id.markerTopLeft);
        markerImageViewTopRight = findViewById(R.id.markerTopRight);
        markerImageViewBottomLeft = findViewById(R.id.markerBottomLeft);
        markerImageViewBottomRight = findViewById(R.id.markerBottomRight);

        // new declare

        markerImageViews[0] = findViewById(R.id.markerTopLeft);
        markerImageViews[1] = findViewById(R.id.markerTopRight);
        markerImageViews[2] = findViewById(R.id.markerBottomLeft);
        markerImageViews[3] = findViewById(R.id.markerBottomRight);



        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            initiateQRCodeScan();
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
                initiateQRCodeScan();
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


    @Override
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
                        imageView.setImageBitmap(bitmap);

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

                          //  Toast.makeText(this, "QR Code Position values: (" + resultPoints[0] + ", " + resultPoints[1] + ")", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "QR Code not found in the image", Toast.LENGTH_SHORT).show();
                        }

                        /*    if (qrCodePosition != null) {
                            setMarkerAtPosition(qrCodePosition[0], qrCodePosition[1]);
                            imageView.setImageBitmap(bitmap);
                            saveBitmapToStorage(bitmap);

                            Toast.makeText(this, "QR Code Position: (" + qrCodePosition[0] + ", " + qrCodePosition[1] + ")", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "QR Code not found in the image", Toast.LENGTH_SHORT).show();
                        }*/
                    } else {
                        Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                }

              /*  Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        takeScreenshot();
                    }
                }, 15000);*/
               //   takeScreenshot();
            }
        } else {
               super.onActivityResult(requestCode, resultCode, data);
        }
    }

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
  /*  private void setMarkerAtPosition(ResultPoint[] resultPoints) {
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
    }*/



    ///////////// new

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


 /*   private void setMarkerAtPosition(ResultPoint[] resultPoints) {
        if (resultPoints != null && resultPoints.length == 4) {
            ResultPoint topLeft = resultPoints[0];
            ResultPoint topRight = resultPoints[1];
            ResultPoint bottomLeft = resultPoints[2];
            ResultPoint bottomRight = resultPoints[3];

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    30, 30
            );


            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            params.leftMargin = (int) topLeft.getX();
            params.topMargin = (int) topLeft.getY();

            // top right

            RelativeLayout.LayoutParams paramstopright = new RelativeLayout.LayoutParams(
                    30, 30
            );


            paramstopright.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            paramstopright.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            paramstopright.leftMargin = (int) topLeft.getX();
            paramstopright.topMargin = (int) topLeft.getY();


            // Set the layout parameters only once
            markerImageView.setLayoutParams(params);
            markerImageView.setLayoutParams(paramstopright);

            markerImageView.setVisibility(View.VISIBLE);
        } else {
            markerImageView.setVisibility(View.INVISIBLE);
        }
    }*/

   /* private void setMarkerAtPosition(ResultPoint[] resultPoints) {
        if (resultPoints != null && resultPoints.length == 4) {
            ResultPoint topLeft = resultPoints[0];

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    30,
                    30
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            params.leftMargin = (int) topLeft.getX();
            params.topMargin = (int) topLeft.getY();
            markerImageView.setLayoutParams(params);

            markerImageView.setVisibility(View.VISIBLE);
        } else {
            markerImageView.setVisibility(View.INVISIBLE);
        }
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
        View rootView = getWindow().getDecorView().getRootView();
        if (rootView == null) {
            Toast.makeText(this, "Failed to capture screenshot: Root view is null", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(QrCodeActivity.this, "Failed to capture screenshot: Drawing cache is null", Toast.LENGTH_SHORT).show();
                        rootView.setDrawingCacheEnabled(false);
                        return;
                    }

                    if (originalBitmap.getWidth() <= 0 || originalBitmap.getHeight() <= 0) {
                        Toast.makeText(QrCodeActivity.this, "Failed to capture screenshot: Invalid bitmap dimensions", Toast.LENGTH_SHORT).show();
                        rootView.setDrawingCacheEnabled(false);
                        return;
                    }
                    Bitmap bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                    rootView.setDrawingCacheEnabled(false);
                    ImageView imageView = findViewById(R.id.imageView);

                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }
    public void saveBitmapToStorage(Bitmap bitmap) {
        String fileName = "Qrcode.jpg";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(this, "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}