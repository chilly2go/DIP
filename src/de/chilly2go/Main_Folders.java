package de.chilly2go;

import java.io.File;
import java.util.concurrent.*;

/**
 * Started as a copy of Main. Just to handle all Folders inside the specified foldes. not just the images With the
 * addition of clustering the runtime skyrocketed. To improve over all runtime classes were changed to enable
 * multithreading.
 */
public class Main_Folders
{
  
  public static void main(String[] args)
  {
    int[]      clusters  = new int[]{0, 2, 3, 4, 5, 7, 10, 15, 20};
    final long startTime = System.currentTimeMillis();
    for (int cluster : clusters
    )
    {
      
      LinkedBlockingQueue<Future<DIPreturn>> queue = new LinkedBlockingQueue<Future<DIPreturn>>();
      String path =
          "H:\\Dropbox\\uni_master\\Thesis\\Recherche\\images\\test_distanz\\5\\";
      String extension = "tiff";
      try
      {
        ThreadPoolExecutor ex = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        // start picture jobs
        final long startTimeJob = System.currentTimeMillis();
        
        for (File dir : new File(path).listFiles())
        {
          if (dir.isDirectory())
          {
            for (File file : new File(dir.getPath()).listFiles())
            {
              if (file.isFile() && file.getName().endsWith(extension))
              {
                if (true)
                { queue.add(ex.submit(new PicToColorScale(file, cluster))); }
              }
            }
          }
          Thread.sleep(500);
        }
        // check for finished jobs
        while (!queue.isEmpty())
        {
          Future<DIPreturn> res = queue.poll();
          if (res.isDone())
          {
            DIPreturn ret = res.get();
          }
          else
          {
            // job not yet finished... put it into the queue again and sleep a little (no busy waiting!)
            queue.add(res);
            Thread.sleep(500);
          }
        }
        final long endTimeJob = System.currentTimeMillis();
        System.out.println("Job-Zeit: " + (double) ((long) endTimeJob - startTimeJob) / 1000 + " Sekunden");
        ex.shutdown();
        // everything is done. there should not be anything blocking here
        ex.awaitTermination(30, TimeUnit.SECONDS);
        if (!ex.isTerminated()) ex.shutdownNow();
      } catch (InterruptedException | ExecutionException e)
      {
        e.printStackTrace();
      }
    }
    final long endTime = System.currentTimeMillis();
    System.out.println("Gesamtzeit: " + (double) ((long) endTime - startTime) / 1000 + " Sekunden");
    
  }
}
