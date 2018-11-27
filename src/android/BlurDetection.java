package com.cordovaplugincamerapreview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

public class BlurDetection implements Laplacian.TileProcessingListener {

  public interface BlurDetectorListener {
    void onBlurCalculated(Double blur);
  }
  
  private static final String TAG = "PP/CameraPreview";
  Float lapMatrix[][];
  ArrayList<Double> tileResults;
  private AtomicInteger tileWorkCounter = new AtomicInteger();
  private BlurDetectorListener eventListener;

  public void setEventListener(BlurDetectorListener listener) {
    eventListener = listener;
  }

  public BlurDetection() {
    lapMatrix = new Float[][] {{0f, 1f, 0f}, {1f, -4f, 1f}, {0f, 1f, 0f}};
  }

  public void detectBlur(Bitmap image) {
    Bitmap grayscale = toGrayscale(image);
    Log.d(TAG, "Starting blur detection.");
    int[] pixels = new int[grayscale.getWidth() * grayscale.getHeight()];
    grayscale.getPixels(pixels, 0, grayscale.getWidth(), 0, 0, grayscale.getWidth(), grayscale.getHeight());
    int tileMatrixEdgeSize = 3;
    int tileEdgeSize = 100 * tileMatrixEdgeSize > grayscale.getWidth() ? grayscale.getWidth() / tileMatrixEdgeSize : 100;
    int tileFormationSize = grayscale.getWidth() < grayscale.getHeight() ? (int)(grayscale.getWidth() * 0.2) : (int)(grayscale.getHeight() * 0.2);
    int tileOriginStep = tileFormationSize / tileMatrixEdgeSize;
    int tileOriginX = (grayscale.getWidth() / 2) - ((tileMatrixEdgeSize * tileOriginStep) / 2);
    int tileOriginY = (grayscale.getHeight() / 2) - ((tileMatrixEdgeSize * tileOriginStep) / 2);
    ArrayList<Rect> tiles = new ArrayList<>(tileMatrixEdgeSize * tileMatrixEdgeSize);
    for (int i = 0; i < tileMatrixEdgeSize; i++) {
      for (int j = 0; j < tileMatrixEdgeSize; j++) {
        Rect rect = new Rect((i * tileOriginStep) + tileOriginX, (j * tileOriginStep) + tileOriginY, (i * tileOriginStep) + tileOriginX + tileEdgeSize, (j * tileOriginStep) + tileOriginY + tileEdgeSize);    
        tiles.add(rect);
      }
    }
    tileResults = new ArrayList<>(tileMatrixEdgeSize * tileMatrixEdgeSize);
    Iterator<Rect> tilesIterator = tiles.iterator();
    while(tilesIterator.hasNext()) {
      Log.d(TAG, "Sending tile to be processed.");
      tileWorkCounter.incrementAndGet();
      Laplacian tileConvolver = new Laplacian();
      Laplacian.LaplacianParams params = new Laplacian.LaplacianParams(pixels, tilesIterator.next(), grayscale.getWidth(), grayscale.getHeight());
      tileConvolver.setEventListener(this);
      tileConvolver.execute(params);
    }
  }

  public void tileProcessed(Double result) {
    tileResults.add(result);
    int tilesRemaining = this.tileWorkCounter.decrementAndGet();
    if (tilesRemaining == 0) {
      Log.d(TAG, "All tiles processed.");
      Double totalVariance = 0.0;
      Iterator<Double> resultsIterator = tileResults.iterator();
      while(resultsIterator.hasNext()) {
        totalVariance += resultsIterator.next();
      }
      Double averageVariance = totalVariance / tileResults.size();
      eventListener.onBlurCalculated(averageVariance);
    }
  }

  private Bitmap toGrayscale(Bitmap original) {
    int width, height;
    height = original.getHeight();
    width = original.getWidth();
    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, original.getConfig());
    Canvas c = new Canvas(bmpGrayscale);
    Paint paint = new Paint();
    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
    paint.setColorFilter(f);
    c.drawBitmap(original, 0, 0, paint);
    return bmpGrayscale;
  }
}
