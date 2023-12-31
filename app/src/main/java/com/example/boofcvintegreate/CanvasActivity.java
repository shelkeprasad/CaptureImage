package com.example.boofcvintegreate;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.ViewGroup;

public class CanvasActivity extends AppCompatActivity  implements CanvasView.CanvasViewCallback{

    private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        canvasView = findViewById(R.id.canvasView);
        canvasView.setCanvasViewCallback(this);
    }

    @Override
    public void onBackPressed() {
        if (canvasView != null && canvasView.isWebViewVisible()) {
            ViewGroup parent = (ViewGroup) canvasView.getParent();
            parent.removeView(canvasView.getWebView());
        } else {
            super.onBackPressed();
        }
    }
}
