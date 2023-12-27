package com.example.boofcvintegreate;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.ResultPoint;

public class Referenceclass {



   /* private void setMarkerAtPosition(ResultPoint[] resultPoints, Bitmap bitmap) {
        if (resultPoints != null && resultPoints.length == 4) {
            ResultPoint topLeft = resultPoints[0];
            ResultPoint topRight = resultPoints[1];
            ResultPoint bottomLeft = resultPoints[2];
            ResultPoint bottomRight = resultPoints[3];

            setMarker(topLeft, R.id.markerTopLeft, bitmap);
            setMarker(topRight, R.id.markerTopRight, bitmap);
            setMarker(bottomLeft, R.id.markerBottomLeft, bitmap);
            setMarker(bottomRight, R.id.markerBottomRight, bitmap);
        } else {
            // Handle the case when QR code points are not found
            // ...
        }
    }


    private void setMarker(ResultPoint point, int markerId, Bitmap bitmap) {
        ImageView markerImageView = findViewById(markerId);

        // Get the dimensions of the shown image
        ViewTreeObserver viewTreeObserver = markerImageView.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                markerImageView.getViewTreeObserver().removeOnPreDrawListener(this);

                // Now you can obtain the dimensions
                int shownImageWidth = markerImageView.getWidth();
                int shownImageHeight = markerImageView.getHeight();

                // Calculate scaling factors
                float scaleX = bitmap.getWidth() / (float) shownImageWidth;
                float scaleY = bitmap.getHeight() / (float) shownImageHeight;

                // Apply scaling to the coordinates
                int scaledX = (int) (point.getX() / scaleX);
                int scaledY = (int) (point.getY() / scaleY);

                // Set the marker positions
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
    }*/
}
