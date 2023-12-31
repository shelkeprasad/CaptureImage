package com.example.boofcvintegreate;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;

public class CanvasView extends View {

    private float canvasTranslateX = 0;
    private float canvasTranslateY = 0;
    private float scaleFactor = 1.0f;
    private Bitmap bitmap, circleBitmap, rectangleBitmap;
    private Paint paint;
    private Matrix matrix;
    private ScaleGestureDetector scaleGestureDetector;
    private float circleX = 200;
    private float circleY = 200;
    private float circleRadius = 100;
    private float rectLeft = 300;
    private float rectTop = 400;
    private float rectRight = 500;
    private float rectBottom = 600;
    private WebView webView;
    private CanvasViewCallback canvasViewCallback;


    private float originalX;
    private float originalY;

    public interface CanvasViewCallback {
        void onBackPressed();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40);

        matrix = new Matrix();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        circleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.circle_img);
        rectangleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rectangle_img);

        webView = new WebView(context);
    }

    public void setCanvasViewCallback(CanvasViewCallback callback) {
        this.canvasViewCallback = callback;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(canvasTranslateX, canvasTranslateY);

        matrix.reset();
        matrix.postTranslate(canvasTranslateX, canvasTranslateY);
        matrix.postScale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);

        canvas.concat(matrix);
        float bitmapX = 400;
        float bitmapY = 650;

        canvas.drawCircle(circleX, circleY, circleRadius, paint);

        if (circleBitmap != null) {
            Rect srcRect = new Rect(0, 0, circleBitmap.getWidth(), circleBitmap.getHeight());
            RectF destRect = new RectF(circleX - circleRadius, circleY - circleRadius, circleX + circleRadius, circleY + circleRadius);
            canvas.drawBitmap(circleBitmap, srcRect, destRect, paint);
        }

        rectangleBitmap = resizeBitmap(rectangleBitmap, (int) (rectRight - rectLeft), (int) (rectBottom - rectTop));
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);


        if (rectangleBitmap != null) {
            Rect srcRect = new Rect(0, 0, rectangleBitmap.getWidth(), rectangleBitmap.getHeight());
            RectF destRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
            canvas.drawBitmap(rectangleBitmap, srcRect, destRect, paint);
        }

        // canvas.drawCircle(circleX, circleY, circleRadius, paint);
        //  canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);
        canvas.drawText("Hello Android....", 100, 100, paint);
        //canvas.drawBitmap(bitmap,bitmapX,bitmapY,paint);

    }

    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();

        float scaledX = (event.getX() - canvasTranslateX) / scaleFactor;
        float scaledY = (event.getY() - canvasTranslateY) / scaleFactor;

      /*  if (isInsideCircle(scaledX, scaledY)) {
            //  openVideo();
            openVideos();
        }
        if (isInsideRect(scaledX, scaledY)) {
            openLink(scaledX,scaledY);
            //  String link = "https://www.google.com/search?q=android+studio&oq=andro&gs_lcrp=EgZjaHJvbWUqEggEEAAYQxiDARixAxiABBiKBTIGCAAQRRg5MgYIARBFGDsyDggCEEUYJxg7GIAEGIoFMgYIAxAjGCcyEggEEAAYQxiDARixAxiABBiKBTISCAUQABhDGIMBGLEDGIAEGIoFMgwIBhAAGEMYgAQYigUyCggHEAAYsQMYgAQyCggIEAAYsQMYgAQyCggJEAAYsQMYgATSAQk4NDMwajBqMTWoAgCwAgA&sourceid=chrome&ie=UTF-8";
            //  WebUrlOpen.openUrl(getContext(),link);
        }*/

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (isInsideCircle(scaledX, scaledY)) {
                    openVideos();
                } else if (isInsideRect(scaledX, scaledY)) {
                    openLink(scaledX, scaledY);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:

            case MotionEvent.ACTION_MOVE:

                int historySize = event.getHistorySize();
                int pointerCount = event.getPointerCount();

                for (int i = 0; i < historySize; i++) {
                    for (int j = 0; j < pointerCount; j++) {

                        int pointerId = event.getPointerId(j);
                        int pointerIndex = event.findPointerIndex(pointerId);

                        if (pointerIndex != -1) {
                            canvasTranslateX += event.getX(pointerIndex) - event.getHistoricalX(j, i);
                            canvasTranslateY += event.getY(pointerIndex) - event.getHistoricalY(j, i);
                        }
                    }
                }
                invalidate();
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            invalidate();
            return true;
        }
    }

   /* private boolean isInsideCircle(float x, float y) {
        float distance = (float) Math.sqrt(Math.pow(x - circleX, 2) + Math.pow(y - circleY, 2));
        return distance <= circleRadius;
    }*/


    /// new working
  /*  private boolean isInsideCircle(float x, float y) {
        float[] point = {x, y};
        Matrix inverseMatrix = new Matrix();
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(point);

        float scaledX = point[0];
        float scaledY = point[1];

        float distance = (float) Math.sqrt(Math.pow(scaledX - circleX, 2) + Math.pow(scaledY - circleY, 2));
        float circleValue  = circleRadius * scaleFactor;
        return distance <= circleValue;
       // return distance <= circleRadius * scaleFactor; // Consider the scale factor
    }*/

  /*  private boolean isInsideCircle(float x, float y) {
        float[] point = {x, y};
        Matrix inverseMatrix = new Matrix();
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(point);

        float scaledX = point[0];
        float scaledY = point[1];

        // Calculate the scaled radius based on the current scaleFactor
        float scaledRadius = circleRadius * scaleFactor;

        float distance = (float) Math.sqrt(Math.pow(scaledX - circleX, 2) + Math.pow(scaledY - circleY, 2));
        boolean ischeck =distance <= scaledRadius && x >= (circleX - scaledRadius) && x <= (circleX + scaledRadius)
                && y >= (circleY - scaledRadius) && y <= (circleY + scaledRadius);

        return ischeck;
    }*/



    private boolean isInsideCircle(float x, float y) {
        float[] point = {x, y};
        Matrix inverseMatrix = new Matrix();
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(point);

        float scaledX = point[0];
        float scaledY = point[1];

        float scaledRadius = circleRadius * scaleFactor;
        float distance = (float) Math.sqrt(Math.pow(scaledX - circleX, 2) + Math.pow(scaledY - circleY, 2));

        return distance <= scaledRadius;
    }



   /* private boolean isInsideRect(float x, float y) {
        return x >= rectLeft && x <= rectRight && y >= rectTop && y <= rectBottom;
    }*/

// working
 /*   private boolean isInsideRect(float x, float y) {
        // Adjust x and y based on translation and scaling
        float[] point = {x, y};
        Matrix inverseMatrix = new Matrix();
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(point);

        float scaledX = point[0];
        float scaledY = point[1];

        //  return scaledX >= rectLeft && scaledX <= rectRight && scaledY >= rectTop && scaledY <= rectBottom;
        return scaledX >= rectLeft && scaledX <= rectRight;
    }*/

    private boolean isInsideRect(float x, float y) {
        float[] point = {x, y};
        Matrix inverseMatrix = new Matrix();
        matrix.invert(inverseMatrix);
        inverseMatrix.mapPoints(point);

        float scaledX = point[0];
        float scaledY = point[1];

        boolean isInside = scaledX >= rectLeft && scaledX <= rectRight && scaledY >= rectTop && scaledY <= rectBottom;
        return isInside;
    }

  /*  private void openLink() {
        String link = "https://www.google.com/search?q=android+studio&oq=andro&gs_lcrp=EgZjaHJvbWUqEggEEAAYQxiDARixAxiABBiKBTIGCAAQRRg5MgYIARBFGDsyDggCEEUYJxg7GIAEGIoFMgYIAxAjGCcyEggEEAAYQxiDARixAxiABBiKBTISCAUQABhDGIMBGLEDGIAEGIoFMgwIBhAAGEMYgAQYigUyCggHEAAYsQMYgAQyCggIEAAYsQMYgAQyCggJEAAYsQMYgATSAQk4NDMwajBqMTWoAgCwAgA&sourceid=chrome&ie=UTF-8";

        WebView webView = new WebView(getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(link);
        ViewGroup parent = (ViewGroup) getParent();
        parent.addView(webView);
    }*/


    private void openLink(float x, float y) {
        if (isInsideRect(x, y)) {
            String link = "https://www.google.com/search?q=android+studio&oq=andro&gs_lcrp=EgZjaHJvbWUqEggEEAAYQxiDARixAxiABBiKBTIGCAAQRRg5MgYIARBFGDsyDggCEEUYJxg7GIAEGIoFMgYIAxAjGCcyEggEEAAYQxiDARixAxiABBiKBTISCAUQABhDGIMBGLEDGIAEGIoFMgwIBhAAGEMYgAQYigUyCggHEAAYsQMYgAQyCggIEAAYsQMYgAQyCggJEAAYsQMYgATSAQk4NDMwajBqMTWoAgCwAgA&sourceid=chrome&ie=UTF-8";

            WebView webView = new WebView(getContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(link);
            ViewGroup parent = (ViewGroup) getParent();
            parent.addView(webView);
        }
    }


    public boolean isWebViewVisible() {
        return webView != null && webView.getParent() != null;
    }

    public WebView getWebView() {
        return webView;
    }

    private void openVideos() {
        String videoLink = "https://www.youtube.com/watch?v=15CdvG0RkKU";

        if (videoLink.contains("youtube.com") || videoLink.contains("youtu.be")) {
            WebView webView = new WebView(getContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(videoLink);

            ViewGroup parent = (ViewGroup) getParent();
            parent.addView(webView);
        } else {
            Toast.makeText(getContext(), "Unsupported video link", Toast.LENGTH_SHORT).show();
        }
    }
}



