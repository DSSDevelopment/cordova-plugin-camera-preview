package com.cordovaplugincamerapreview;

import java.util.ArrayList;
import java.util.Iterator;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

public class Laplacian extends AsyncTask<Laplacian.LaplacianParams, Void, Double> {

  public interface TileProcessingListener {
    void tileProcessed(Double result);
  }

  private TileProcessingListener eventListener;

  public void setEventListener(TileProcessingListener listener) {
    eventListener = listener;
  }

  public Laplacian() {}

  @Override
  protected Double doInBackground(LaplacianParams... params) {
    return convolveSingleTile(params[0].pixels, params[0].tile, params[0].imageWidth, params[0].imageHeight);
  }

  @Override
  protected void onPostExecute(Double variance) {
    eventListener.tileProcessed(variance);
  }

  private static Double convolveSingleTile(int[] pixels, Rect tile, int imageWidth, int imageHeight) {
    float[][] lapMatrix = new float[][]{{0f, 1f, 0f}, {1f, -4f, 1f}, {0f, 1f, 0f}};
    float sum = 0.0f;
    ArrayList<Double> values = new ArrayList<>(pixels.length);
    int[] pos = {0, 0};
    int width = tile.width();
    int height = tile.height();
    int originX = tile.left;
    int originY = tile.top;

    for (int x = originX; x <= originX + width; x++) {
      for (int y = originY; y < originY + height; y++) {
        boolean rejectEdgePixel = x - 1 < 0 || x + 2 > imageWidth || y - 1 < 0 || y + 2 > imageHeight;
        if (!rejectEdgePixel) {
          Double accumulator = 0.0;
          for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
              pos[0] = x + i - 1;
              pos[1] = y + j - 1;
              int pixel = pixels[(imageWidth * pos[1]) + (pos[0])];
              int r = (pixel >> 16) & 0xff;
              int g = (pixel >>  8) & 0xff;
              int b = pixel & 0xff;
              double brightness = Math.floor(0.35 * (double)r + 0.5 * (double)g + 0.15 * (double)b);
              accumulator += (brightness / 255.0) * lapMatrix[i][j];
            }
          }
          sum += accumulator;
          values.add(accumulator);
        }
      }
    }
    Double average = sum == 0.0 ? 1.0 : sum / values.size();
    Double variance = 0.0;
    Iterator<Double> valuesIterator = values.iterator();
    while(valuesIterator.hasNext()) {
      variance += Math.pow((valuesIterator.next() - average), 2.0d);
    }
    return variance * 10.0;
  }

  public static class LaplacianParams {
    int[] pixels;
    Rect tile;
    int imageWidth;
    int imageHeight;

    LaplacianParams(int[] pixels, Rect tile, int imageWidth, int imageHeight) {
      this.pixels = pixels;
      this.tile = tile;
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
    }
  }
}