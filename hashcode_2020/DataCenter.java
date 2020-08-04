import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;

public class DataCenter {
  String fileName;
  ArrayList<Integer> videos = new ArrayList<>();
  ArrayList<Endpoint> endpoints = new ArrayList<>();
  ArrayList<CacheServer> servers = new ArrayList<>();
  int V; // num of videos
  int E; // num of endpoints
  int R; // num of requests
  int C; // num of caches
  int X; // cache size

  public DataCenter(String fileName) {
    this.fileName = fileName;
  }

  public void parseInput() {
    int bufferSize = 8*1024;
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new FileReader(fileName + ".in"), bufferSize);
      String line = bufferedReader.readLine();
      String[] firstLine = line.split(" ");
      V = Integer.parseInt(firstLine[0]);
      E = Integer.parseInt(firstLine[1]);
      R = Integer.parseInt(firstLine[2]);
      C = Integer.parseInt(firstLine[3]);
      X = Integer.parseInt(firstLine[4]);
      for (int i = 0; i < C; i++) {
        servers.add(new CacheServer(i, V, X));
      }

      line = bufferedReader.readLine();
      String[] secondLine = line.split(" ");
      for (int i = 0; i < V; i++) {
        videos.add(Integer.parseInt(secondLine[i]));
      }

      for (int i = 0; i < E; i++) {
        line = bufferedReader.readLine();
        String[] endPoint = line.split(" ");
        int LD = Integer.parseInt(endPoint[0]);
        int K = Integer.parseInt(endPoint[1]);
        Endpoint newPoint = new Endpoint(LD, K);
        for (int j = 0; j < K; j++) {
          line = bufferedReader.readLine();
          String[] connection = line.split(" ");
          int c = Integer.parseInt(connection[0]);
          int LC = Integer.parseInt(connection[1]);
          newPoint.addConnection(c, LC);
        }
        endpoints.add(newPoint);
      }

      for (int i = 0; i < R; i++) {
        line = bufferedReader.readLine();
        String[] request = line.split(" ");
        int RV = Integer.parseInt(request[0]);
        int RE = Integer.parseInt(request[1]);
        int RN = Integer.parseInt(request[2]);
        endpoints.get(RE).addRequest(RV, RN);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void getSolution() {
    // loop over endpoints and find video scores
    for (Endpoint e : endpoints) {
      e.calculateVideoScores(videos, X, servers);
    }

    boolean end = false;
    while (!end) {
      end = true;
      for (Endpoint e : endpoints) {
        //e.loopThroughRequests(videos, servers);
        Integer key = e.getFirstKey();
        if (key != null) {
          e.storeVideoByRequest(key, videos, servers);
        }
        if (!e.requests.isEmpty()) {
          end = false;
        }
      }
    }

    /*
    // loop over videos and find cache with greatest score
    boolean end = false;
    while (!end) {
      end = true;
      for (int video = 0; video < V; video++) {
        Long maxScore = new Long(0);
        Integer cacheId = 0;
        for (CacheServer c : servers) {
          Long cacheVideoScore = c.videoScores.get(video).videoScore;
          if (cacheVideoScore > maxScore) {
            maxScore = cacheVideoScore;
            cacheId = c.serverId;
          }
        }
        if (maxScore > 0) {
          boolean willContinue = servers.get(cacheId).storeVideo(video, videos.get(video));
          if (willContinue) {
            for (Endpoint e : endpoints) {
              e.removeVideoScores(video, cacheId, videos, X, servers);
            }
            end = false;
          }
        }
      }
    }
    */
    
    /*
    // loop over cacheservers and store videos
    for (CacheServer c : servers) {
      for (Integer video : c.getSortedVideosByScore()) {
        boolean willContinue = c.storeVideo(video, videos.get(video));
        if (!willContinue) {
          break;
        }
      }
    }
    */
  }

  public void printOutput() {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(fileName + ".out", "UTF-8");
      int count = 0;
      ArrayList<CacheServer> usedCaches = new ArrayList<>();
      for (CacheServer cache : servers) {
        if (cache.videosStored.size() > 0) {
          usedCaches.add(cache);
          count++;
        }
      }
      writer.print(count);
      writer.println();
      
      for (CacheServer cache : usedCaches) {
        writer.print(cache.serverId);
        for (Integer video : cache.videosStored) {
          writer.print(" " + video);
        }
        writer.println();
      }
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}