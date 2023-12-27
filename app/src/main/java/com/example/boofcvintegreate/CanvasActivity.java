package com.example.boofcvintegreate;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Canvas;
import android.os.Bundle;

public class CanvasActivity extends AppCompatActivity {

    private CanvasView canvasView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        canvasView = findViewById(R.id.canvasView);
    }
}