package com.example.boofcvintegreate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        circleImageView = findViewById(R.id.circleImageView);
        rectangleImageView = findViewById(R.id.rectangleImageView);
        textView = findViewById(R.id.textView);

        circleImageView.setClickable(true);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                openVideos();
                return true;
            }
        });
        // new add

        circleGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                openVideos();
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
        Bitmap originalRectangleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.small_circle);

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
                        if (gestureDetector.onTouchEvent(event)) {
                            // Click event handled by gestureDetector
                        } else {
                            // Click event handled manually
                          //  openVideos();
                        }
                        break;
                }
                return true;
            }
        };

        circleImageView.setOnTouchListener(onTouchListener);
        rectangleImageView.setOnTouchListener(onTouchListener);
        textView.setOnTouchListener(onTouchListener);

 /*       circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideos();
            }
        });

        rectangleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLink();
            }
        });*/
    }

    private void openVideos() {
        String videoLink = "https://www.youtube.com/watch?v=15CdvG0RkKU";

        if (videoLink.contains("youtube.com") || videoLink.contains("youtu.be")) {
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
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

            // Implement scaling logic here
            // You can apply scaling to your images or other views
            circleImageView.setScaleX(scaleFactor);
            circleImageView.setScaleY(scaleFactor);

            rectangleImageView.setScaleX(scaleFactor);
            rectangleImageView.setScaleY(scaleFactor);

            textView.setScaleX(scaleFactor);
            textView.setScaleY(scaleFactor);

            return true;
        }
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, float scale) {
        int width = Math.round(originalBitmap.getWidth() * scale);
        int height = Math.round(originalBitmap.getHeight() * scale);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }
}