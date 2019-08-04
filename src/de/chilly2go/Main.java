package de.chilly2go;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;

public class Main
{
  
  public static int hsvToRgb(float H, float S, float V)
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
  
  public static int[] picuteToColorScale(String path, String file)
  {
    File f, input      = new File(file);
    int  width, height = 0;
    int  min           = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    try
    {
      BufferedImage image = ImageIO.read(input);
      //      min = 7000;
      width = image.getWidth();
      height = image.getHeight();
      Raster      raster  = image.getData();
      PrintWriter writer1 = new PrintWriter(new File("color-hex.csv"));
      PrintWriter writer2 = new PrintWriter(new File("color-value.csv"));
      for (int i = 0; i < height; i++)
      {
        StringBuilder linewriter  = new StringBuilder();
        StringBuilder linewriter2 = new StringBuilder();
        for (int j = 0; j < width; j++)
        {
          Color c = new Color(image.getRGB(j, i));
          linewriter.append(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
          linewriter.append(",");
          int tmp = raster.getSample(j, i, 0);
          if (tmp < min)
          { min = tmp; }
          if (tmp > max)
          { max = tmp; }
          linewriter2.append(tmp);
          linewriter2.append(",");
        }
        linewriter.append('\n');
        linewriter2.append('\n');
        writer1.write(linewriter.toString());
        writer2.write(linewriter2.toString());
      }
      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      for (int i = 0; i < height; i++)
      {
        for (int j = 0; j < width; j++)
        {
          float hue = (float) (javafx.scene.paint.Color.BLUE.getHue() +
                               (javafx.scene.paint.Color.RED.getHue() - javafx.scene.paint.Color.BLUE.getHue()) *
                               (raster.getSample(j, i, 0) - min) / (max - min));
          bufferedImage.setRGB(j, i, Main.hsvToRgb(hue, 100, 100));
        }
      }
      //      ImageWriter     writer = (ImageWriter) ImageIO.getImageWritersByFormatName("png").next();
      //      ImageWriteParam param  = writer.getDefaultWriteParam();
      //      param.setCompressionMode(param.MODE_EXPLICIT);
      //      param.setCompressionType("JPEG-LS");
      //      writer.setOutput(ImageIO.createImageOutputStream(new File(f)));
      //      writer.write(null, new IIOImage(image, null, null), param);
      String filename = file.substring(0, file.length() - 5);
      ImageIO.write(bufferedImage, "png", new File(filename + "_Output_min" + min + "_max" + max + ".png"));
      ImageIO.write(bufferedImage, "jpg", new File(filename + "_Output_min" + min + "_max" + max + ".jpg"));
      
      //      System.out.println(ImageIO.write(image, "tiff", f));
      System.out.print("File: " + filename);
      System.out.print(" Min: " + min);
      System.out.println(" Max: " + max);
    } catch (IOException e)
    {
      System.err.println("inputfile: " + input);
      e.printStackTrace();
    }
    return new int[]{min, max};
  }
  
  public static void main(String[] args)
  {
    ArrayList<String> pictures = new ArrayList<>();
    String path =
        "H:\\Dropbox\\uni_master\\Thesis\\Recherche\\images\\test_distanz\\4\\4\\";
    String extension              = "tiff";
    String minmax_variations_file = "variations.csv";
    try
    {
      PrintStream out = new PrintStream(new FileOutputStream(path + minmax_variations_file));
      int[]       minmax;
      out.println("min,max");
      for (File file : new File(path).listFiles())
      {
        if (file.isFile() && file.getName().endsWith(extension))
        {
          minmax = picuteToColorScale(path, file.getPath());
          out.println(minmax[0] + "," + minmax[1]);
        }
      }
      out.close();
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
  }
}
