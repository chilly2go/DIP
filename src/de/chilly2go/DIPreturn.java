package de.chilly2go;

import java.io.File;

/**
 * Return Object to DIP job (color transform and stuff)
 */
public class DIPreturn
{
  private int     min;
  private int     max;
  private int     countHotPixels  = -1;
  private boolean heatRegionFound = false;
  private double  elapsedClustering;
  private double  elapsedTotal;
  private File    file;
  
  public DIPreturn(int min, int max, File file)
  {
    this.min = min;
    this.max = max;
    this.file = file;
  }
  
  public DIPreturn(int min, int max, File file, double elapsedClustering)
  {
    this.min = min;
    this.max = max;
    this.elapsedClustering = elapsedClustering;
    this.file = file;
  }
  
  public DIPreturn(int min, int max, File file, int countHotPixels)
  {
    this.min = min;
    this.max = max;
    this.countHotPixels = countHotPixels;
    this.file = file;
  }
  
  public DIPreturn min(int min)
  {
    this.min = min;
    return this;
  }
  
  public int min()
  {
    return min;
  }
  
  public DIPreturn elapsedClustering(double elapsed)
  {
    this.elapsedClustering = elapsed;
    return this;
  }
  
  public double elapsedClustering()
  {
    return elapsedClustering;
  }
  
  public DIPreturn elapsedTotal(double elapsedTotal)
  {
    this.elapsedTotal = elapsedTotal;
    return this;
  }
  
  public double elapsedTotal()
  {
    return elapsedTotal;
  }
  
  public DIPreturn path(File path)
  {
    this.file = path;
    return this;
    
  }
  
  public File path()
  {
    return file;
  }
  
  public DIPreturn max(int max)
  {
    this.max = max;
    return this;
  }
  
  public int max()
  {
    return max;
  }
  
  public int countHotPixels()
  {
    return countHotPixels;
  }
  
  public DIPreturn countHotPixels(int countHotPixels)
  {
    this.countHotPixels = countHotPixels;
    return this;
  }
  
  public DIPreturn heatRegionFound(boolean heatRegionFound)
  {
    this.heatRegionFound = heatRegionFound;
    return this;
  }
  
  public boolean heatRegionFound()
  {
    return this.heatRegionFound;
  }
}
