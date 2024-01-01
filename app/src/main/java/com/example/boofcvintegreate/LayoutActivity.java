package com.example.boofcvintegreate;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class LayoutActivity extends AppCompatActivity {
    private ImageView circleImageView;
    private ImageView rectangleImageView;
    private TextView textView;

    private float scaleFactor = 1.0f;
    private float lastX, lastY;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private GestureDetector circleGestureDetector;
    private GestureDetector rectangleGestureDetector;
    private GestureDetector textGestureDetector;

    private VideoView videoView;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        circleImageView = findViewById(R.id.circleImageView);
        rectangleImageView = findViewById(R.id.rectangleImageView);
        textView = findViewById(R.id.textView);

        circleImageView.setClickable(true);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

     /*   gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                openVideos();
                return true;
            }
        });*/

        circleGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                openVideoView();
                return true;
            }
        });


        rectangleGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                openLink();
                return true;
            }
        });

        textGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Toast.makeText(LayoutActivity.this, "clicl on text ..........", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        Bitmap originalCircleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.small_circle);
        Bitmap originalRectangleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rectangle_small);

        Bitmap resizedCircleBitmap = resizeBitmap(originalCircleBitmap, scaleFactor);
        Bitmap resizedRectangleBitmap = resizeBitmap(originalRectangleBitmap, scaleFactor);

        circleImageView.setImageBitmap(resizedCircleBitmap);
        rectangleImageView.setImageBitmap(resizedRectangleBitmap);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);

                int action = event.getAction();

                if (v.getId() == R.id.circleImageView) {
                    circleGestureDetector.onTouchEvent(event);
                } else if (v.getId() == R.id.rectangleImageView) {
                    rectangleGestureDetector.onTouchEvent(event);
                } else if (v.getId() == R.id.textView) {
                    textGestureDetector.onTouchEvent(event);
                }
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - lastX;
                        float deltaY = event.getY() - lastY;

                        v.setTranslationX(v.getTranslationX() + deltaX);
                        v.setTranslationY(v.getTranslationY() + deltaY);

                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        };

        circleImageView.setOnTouchListener(onTouchListener);
        rectangleImageView.setOnTouchListener(onTouchListener);
        textView.setOnTouchListener(onTouchListener);

    }

    private void openVideos() {
        String videoLink = "https://www.youtube.com/watch?v=15CdvG0RkKU";

        if (videoLink!=null) {
            Toast.makeText(this, "click open video", Toast.LENGTH_SHORT).show();

            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(videoLink);

            ViewGroup parent = findViewById(R.id.relativelayout);
            parent.addView(webView);
        } else {
            Toast.makeText(this, "Unsupported video link", Toast.LENGTH_SHORT).show();
        }
    }
    private void openVideoView(){
        String videoLink = "https://video-ssl.itunes.apple.com/itunes-assets/Video82/v4/a3/ef/25/a3ef253a-208e-3cbc-cbf0-bc444dae2f8d/mzvf_6313901593442783545.640x354.h264lc.U.p.m4v";

        videoView = findViewById(R.id.videoview);
        Toast.makeText(this, "click on circle  ...", Toast.LENGTH_SHORT).show();

        if (videoLink!=null){
            videoView.setMediaController(new android.widget.MediaController(this));

            videoView.setVideoURI(Uri.parse(videoLink));
            videoView.start();
        }else {
            Toast.makeText(this, "video url null ....", Toast.LENGTH_SHORT).show();
        }
    }

    private void openLink() {
        Toast.makeText(this, "click on openlink  .......", Toast.LENGTH_SHORT).show();
            String link = "https://www.google.com/search?q=android+studio&oq=andro&gs_lcrp=EgZjaHJvbWUqEggEEAAYQxiDARixAxiABBiKBTIGCAAQRRg5MgYIARBFGDsyDggCEEUYJxg7GIAEGIoFMgYIAxAjGCcyEggEEAAYQxiDARixAxiABBiKBTISCAUQABhDGIMBGLEDGIAEGIoFMgwIBhAAGEMYgAQYigUyCggHEAAYsQMYgAQyCggIEAAYsQMYgAQyCggJEAAYsQMYgATSAQk4NDMwajBqMTWoAgCwAgA&sourceid=chrome&ie=UTF-8";

            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(link);
            ViewGroup parent = findViewById(R.id.relativelayout);
            parent.addView(webView);

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private boolean zoomingOut = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactorDelta = detector.getScaleFactor();

            Log.d("ScaleFactor", String.valueOf(scaleFactorDelta));
            if (scaleFactorDelta < 1.0f) {
                zoomingOut = true;
            } else {
                zoomingOut = false;
            }

            scaleFactor *= scaleFactorDelta;
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 3.0f));

            circleImageView.setScaleX(scaleFactor);
            circleImageView.setScaleY(scaleFactor);

            rectangleImageView.setScaleX(scaleFactor);
            rectangleImageView.setScaleY(scaleFactor);

            textView.setScaleX(scaleFactor);
            textView.setScaleY(scaleFactor);

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (zoomingOut) {
                scaleFactor = 1.0f;
                Log.d("ScaleFactorend", String.valueOf(scaleFactor));

                circleImageView.setScaleX(scaleFactor);
                circleImageView.setScaleY(scaleFactor);

                rectangleImageView.setScaleX(scaleFactor);
                rectangleImageView.setScaleY(scaleFactor);

                textView.setScaleX(scaleFactor);
                textView.setScaleY(scaleFactor);
            }
        }
    }
    private Bitmap resizeBitmap(Bitmap originalBitmap, float scale) {
        int width = Math.round(originalBitmap.getWidth() * scale);
        int height = Math.round(originalBitmap.getHeight() * scale);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }
}