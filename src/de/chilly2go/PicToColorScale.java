package de.chilly2go;

import de.chilly2go.JasonAltschuler.KMeans;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

public class PicToColorScale implements Callable<DIPreturn>
{
  public static final boolean CLUSTER_COLORS                       = true;
  public static       int     CLUSTER_COLORS_COUNT                 = 7;
  public static final boolean DRAW_HEAT_SOURCE_RECTANGLE           = true;
  public static final int     DRAW_HEAT_SOURCE_RECTANGLE_SIZE      = 22;
  public static final int     DRAW_HEAT_SOURCE_MAX_DEVIATION       = 500;
  public static final int     DRAW_HEAT_SOURCE_MIN_PIXELS          = 3;
  public static final boolean WRITE_DEBUG_PIXEL_VALUES             = false;
  public static final boolean WRITE_JPG                            = false;
  public static final int     MIN_DIFFERENCE_TO_SELECT_HEAT_SOURCE = 2500;
  public static final int     MIN_ADJUST_BY                        = -200;
  private             File    file;
  
  public PicToColorScale(File file)
  {
    this.file = file;
  }
  
  public PicToColorScale(File file, int clusters)
  {
    this.file = file;
    CLUSTER_COLORS_COUNT = clusters;
  }
  
  public int hsvToRgb(float H, float S, float V)
  {
    
    float R, G, B;
    
    H /= 360f;
    S /= 100f;
    V /= 100f;
    
    if (S == 0)
    {
      R = V * 255;
      G = V * 255;
      B = V * 255;
    }
    else
    {
      float var_h = H * 6;
      if (var_h == 6)
      {
        var_h = 0; // H must be < 1
      }
      int var_i = (int) Math.floor((double) var_h); // Or ... var_i =
      // floor( var_h )
      float var_1 = V * (1 - S);
      float var_2 = V * (1 - S * (var_h - var_i));
      float var_3 = V * (1 - S * (1 - (var_h - var_i)));
      
      float var_r;
      float var_g;
      float var_b;
      if (var_i == 0)
      {
        var_r = V;
        var_g = var_3;
        var_b = var_1;
      }
      else if (var_i == 1)
      {
        var_r = var_2;
        var_g = V;
        var_b = var_1;
      }
      else if (var_i == 2)
      {
        var_r = var_1;
        var_g = V;
        var_b = var_3;
      }
      else if (var_i == 3)
      {
        var_r = var_1;
        var_g = var_2;
        var_b = V;
      }
      else if (var_i == 4)
      {
        var_r = var_3;
        var_g = var_1;
        var_b = V;
      }
      else
      {
        var_r = V;
        var_g = var_1;
        var_b = var_2;
      }
      
      R = var_r * 255; // RGB results from 0 to 255
      G = var_g * 255;
      B = var_b * 255;
    }
    
    //      String rs = Integer.toHexString((int) (R));
    //      String gs = Integer.toHexString((int) (G));
    //      String bs = Integer.toHexString((int) (B));
    //
    //      if (rs.length() == 1)
    //      { rs = "0" + rs; }
    //      if (gs.length() == 1)
    //      { gs = "0" + gs; }
    //      if (bs.length() == 1)
    //      { bs = "0" + bs; }
    //      return "#" + rs + gs + bs;
    return (255 << 24) | (((int) R) << 16) | (((int) G) << 8) | ((int) B);
  }
  
  /**
   * check if there are at least 2 surrounding pixels with a value bigger max - DRAW_HEAT_SOURCE_MAX_DEVIATION
   *
   * @param raster
   *     image field
   * @param x
   *     x coordinate
   * @param y
   *     y coordinate
   * @param max
   *     max value to compare against
   *
   * @return boolean found or not
   */
  private boolean checkNeighborForSimilarValue(Raster raster, int x, int y, int max)
  {
    return raster.getSample(x, y, 0) > (max - DRAW_HEAT_SOURCE_MAX_DEVIATION);
  }
  
  private DIPreturn picuteToColorScale(File file)
  {
    int       width, height  = 0;
    int       min            = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    int       countHotPixels = 0;
    double    elapsed        = 0;
    KMeans    clustering;
    DIPreturn diPreturn      = new DIPreturn(min, max, file);
    try
    {
      //      min = 7000;
      PrintWriter   writer1, writer2;
      BufferedImage image = ImageIO.read(file);
      width = image.getWidth();
      height = image.getHeight();
      Raster     raster                   = image.getData();
      double[][] points                   = new double[width * height][1];
      int        p                        = 0;
      int[]      rectCenter               = new int[2];
      boolean    rectangleCoordsEstimated = false;
      if (WRITE_DEBUG_PIXEL_VALUES)
      {
        writer1 = new PrintWriter(new File("color-hex.csv"));
        writer2 = new PrintWriter(new File("color-value.csv"));
      }
      for (int i = 0; i < height; i++)
      {
        StringBuilder linewriter  = new StringBuilder();
        StringBuilder linewriter2 = new StringBuilder();
        for (int j = 0; j < width; j++)
        {
          Color c = new Color(image.getRGB(j, i));
          if (WRITE_DEBUG_PIXEL_VALUES)
          {
            linewriter.append(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
            linewriter.append(",");
          }
          int tmp = raster.getSample(j, i, 0);
          if (tmp < min)
          { min = tmp; }
          if (tmp > max)
          { max = tmp; }
          points[p++][0] = tmp;
          if (WRITE_DEBUG_PIXEL_VALUES)
          {
            linewriter2.append(tmp);
            linewriter2.append(",");
          }
        }
        if (WRITE_DEBUG_PIXEL_VALUES)
        {
          linewriter.append('\n');
          linewriter2.append('\n');
          writer1.write(linewriter.toString());
          writer2.write(linewriter2.toString());
        }
      }
      if (MIN_ADJUST_BY != 0)
      { min += MIN_ADJUST_BY; }
      if (CLUSTER_COLORS)
      {
        final long startTime = System.currentTimeMillis();
        clustering =
            new KMeans.Builder(CLUSTER_COLORS_COUNT, points)
                .iterations(50)
                .pp(false)
                .ppe(true)
                .epsilon(.001)
                .useEpsilon(true)
                .predefineCentroidWith(max)
                .build();
        final long endTime = System.currentTimeMillis();
        elapsed = (double) ((long) endTime - startTime) / 1000;
      }
      diPreturn.min(min).max(max).elapsedClustering(elapsed);
      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      for (int y = 0; y < height; y++)
      {
        for (int x = 0; x < width; x++)
        {
          double tmp = raster.getSample(x, y, 0);
          // draw info rectangle?
          if (DRAW_HEAT_SOURCE_RECTANGLE && max - min > MIN_DIFFERENCE_TO_SELECT_HEAT_SOURCE &&
              !rectangleCoordsEstimated)
          {
            // pixel is max or close to it
            if (tmp >= max - 10)
            {
              int requiredNeighboringHeatPixels = (DRAW_HEAT_SOURCE_MIN_PIXELS - 1);
              // get topLeft corner (distance accoding to required heat pixels
              int heatAreaFromTop               = y - requiredNeighboringHeatPixels;
              int heatAreaFromLeft              = x - requiredNeighboringHeatPixels;
              // iterate over all pixels for the square surrounding the maxTemp pixel.
              for (int heatY = heatAreaFromTop; heatY < y + requiredNeighboringHeatPixels; heatY++)
              {
                for (int heatX = heatAreaFromLeft; heatX < x + requiredNeighboringHeatPixels; heatX++)
                {
                  // check bounds
                  if (heatX > 0 && heatY > 0 && heatX < width && heatY < height)
                  {
                    if (checkNeighborForSimilarValue(raster, heatX, heatY, max))
                    { countHotPixels++; }
                  }
                }
              }
              if (countHotPixels >= requiredNeighboringHeatPixels)
              {
                // at least 2 other pixels in same range found. proper "heat source"
                rectCenter[0] = y;
                rectCenter[1] = x;
                rectangleCoordsEstimated = true;
              }
              else
              {
                // currently doing nothing it not enough pixels could be found.
              }
              diPreturn.countHotPixels(countHotPixels);
            }
          }
          if (CLUSTER_COLORS)
          {
            tmp = clustering.assignFromCentroid(tmp);
          }
          // convert to new color
          float hue = (float) (javafx.scene.paint.Color.BLUE.getHue() +
                               (javafx.scene.paint.Color.RED.getHue() - javafx.scene.paint.Color.BLUE.getHue()) *
                               (tmp - min) / (max - min));
          bufferedImage.setRGB(x, y, this.hsvToRgb(hue, 100, 100));
        }
      }
      if (DRAW_HEAT_SOURCE_RECTANGLE && rectangleCoordsEstimated)
      {
        int fromTop  = rectCenter[0] - (int) Math.ceil(DRAW_HEAT_SOURCE_RECTANGLE_SIZE / 2);
        int fromLeft = rectCenter[1] - (int) Math.ceil(DRAW_HEAT_SOURCE_RECTANGLE_SIZE / 2);
        int y;
        int x;
        /* draw rectangle for heat source */
        // top line
        y = fromTop;
        for (x = fromLeft; x < DRAW_HEAT_SOURCE_RECTANGLE_SIZE + fromLeft + 1; x++)
        {
          checkImageDraw(width, height, bufferedImage, y, x);
        }
        // bottom line
        y = fromTop + DRAW_HEAT_SOURCE_RECTANGLE_SIZE;
        for (x = fromLeft; x < DRAW_HEAT_SOURCE_RECTANGLE_SIZE + fromLeft + 1; x++)
        {
          checkImageDraw(width, height, bufferedImage, y, x);
        }
        // left line
        x = fromLeft;
        for (y = fromTop; y < DRAW_HEAT_SOURCE_RECTANGLE_SIZE + fromTop; y++)
        {
          checkImageDraw(width, height, bufferedImage, y, x);
        }
        // right line
        x = fromLeft + DRAW_HEAT_SOURCE_RECTANGLE_SIZE + 1;
        for (y = fromTop; y < DRAW_HEAT_SOURCE_RECTANGLE_SIZE + fromTop + 1; y++)
        {
          checkImageDraw(width, height, bufferedImage, y, x);
        }
      }
      String filename = file.getPath().substring(0, file.getPath().length() - 5);
      ImageIO.write(bufferedImage, "png",
                    new File(
                        filename + "_Output_min" + min + "_max" + max + "_clusters" + CLUSTER_COLORS_COUNT + ".png"));
      if (WRITE_JPG)
      {
        ImageIO.write(bufferedImage,
                      "jpg",
                      new File(
                          filename + "_Output_min" + min + "_max" + max + "_clusters" + CLUSTER_COLORS_COUNT + ".jpg"));
      }
    } catch (IOException e)
    {
      System.err.println("inputfile: " + file.getPath());
      // not proper... but works for now
      e.printStackTrace();
    }
    return diPreturn;
  }
  
  /**
   * Draw black pixel at given coordinates. check if coordinates are within image bounds. do nothing if not
   *
   * @param width
   *     image width
   * @param height
   *     image height
   * @param image
   *     image reference
   * @param y
   *     target y coordinate
   * @param x
   *     target x coordinate
   */
  private void checkImageDraw(int width, int height, BufferedImage image, int y, int x)
  {
    // y / x not out of bounds
    if (!(y < 0 || x < 0) || !(y > height || x > width))
    {
      // make it black
      image.setRGB(x, y, this.hsvToRgb(0, 100, 0));
    }
  }
  
  @Override
  public DIPreturn call()
  {
    final long startTime = System.currentTimeMillis();
    DIPreturn  dipreturn = this.picuteToColorScale(this.file);
    final long endTime   = System.currentTimeMillis();
    final long elapsed   = endTime - startTime;
    dipreturn.elapsedTotal((double) elapsed / 1000);
    System.out.println(
        "DIP for (" + this.file.getParentFile().getName() + ")" + this.file.getName() + " took " +
        dipreturn.elapsedTotal() +
        " seconds " +
        "(Clustering: " +
        dipreturn.elapsedClustering() + " seconds)| Min: " + dipreturn.min() + " Max: " + dipreturn.max() +
        " | Heat source found / confirmed: " + dipreturn.heatRegionFound() + " (" + dipreturn.countHotPixels() + ") |" +
        " Clusters: " + CLUSTER_COLORS_COUNT);
    return dipreturn;
  }
}
