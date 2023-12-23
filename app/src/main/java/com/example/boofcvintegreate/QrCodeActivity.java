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

        imageView = findViewById(R.id.imgscreen);
        markerImageView = findViewById(R.id.markerImageView);
        markerImageViewTopLeft = findViewById(R.id.markerImageView);
        markerImageViewTopRight = findViewById(R.id.markerTopRight);
        markerImageViewBottomLeft = findViewById(R.id.markerBottomLeft);
        markerImageViewBottomRight = findViewById(R.id.markerBottomRight);



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
                // function call
             //   takeScreenshot();

                String scannedData = result.getContents();
                String filePath = result.getBarcodeImagePath();
                Toast.makeText(this, "Scanned: " + scannedData, Toast.LENGTH_LONG).show();
                File imageFile = new File(filePath);

                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    if (bitmap != null) {
                     //   int[] qrCodePosition = findQrCodePosition(bitmap);
                        ResultPoint[] resultPoints = findQrCodePositionValue(bitmap);


                        if (resultPoints != null) {
                            setMarkerAtPosition(resultPoints);
                            imageView.setImageBitmap(bitmap);
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
        markerImageView.setLayoutParams(params);
        markerImageView.setVisibility(View.VISIBLE);
    }



    private void setMarkerAtPosition(ResultPoint[] resultPoints) {
        if (resultPoints != null && resultPoints.length == 4) {
            ResultPoint topLeft = resultPoints[0];
            ResultPoint topRight = resultPoints[1];
            ResultPoint bottomLeft = resultPoints[2];
            ResultPoint bottomRight = resultPoints[3];

            RelativeLayout.LayoutParams paramsTopLeft = new RelativeLayout.LayoutParams(
                    30, 30
            );
            paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            paramsTopLeft.leftMargin = (int) topLeft.getX();
            paramsTopLeft.topMargin = (int) topLeft.getY();
            markerImageViewTopLeft.setLayoutParams(paramsTopLeft);

            RelativeLayout.LayoutParams paramsTopRight = new RelativeLayout.LayoutParams(
                    30, 30
            );
            paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            paramsTopRight.leftMargin = (int) topRight.getX();
            paramsTopRight.topMargin = (int) topRight.getY();
            markerImageViewTopRight.setLayoutParams(paramsTopRight);

            RelativeLayout.LayoutParams paramsBottomLeft = new RelativeLayout.LayoutParams(
                    30, 30
            );
            paramsBottomLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            paramsBottomLeft.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            paramsBottomLeft.leftMargin = (int) bottomLeft.getX();
            paramsBottomLeft.topMargin = (int) bottomLeft.getY();
            markerImageViewBottomLeft.setLayoutParams(paramsBottomLeft);

            RelativeLayout.LayoutParams paramsBottomRight = new RelativeLayout.LayoutParams(
                    30, 30
            );
            paramsBottomRight.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            paramsBottomRight.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            paramsBottomRight.leftMargin = (int) bottomRight.getX();
            paramsBottomRight.topMargin = (int) bottomRight.getY();
            markerImageViewBottomRight.setLayoutParams(paramsBottomRight);

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
                    ImageView imageView = findViewById(R.id.imgscreen);

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