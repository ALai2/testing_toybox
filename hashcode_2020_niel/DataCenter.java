import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

public class DataCenter {
  String fileName;
  ArrayList<Integer> videoSizes = new ArrayList<>();
  ArrayList<Video> videos = new ArrayList<>();
  ArrayList<Endpoint> endpoints = new ArrayList<>();
  ArrayList<CacheServer> servers = new ArrayList<>();
  ArrayList<Request> requests = new ArrayList<>();
  int V; // num of videos
  int E; // num of endpoints
  int R; // num of requests
  int C; // num of caches
  int X; // cache size
  int assignedRequestCount = 0;

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
          Video newVideo = new Video(i, Integer.parseInt(secondLine[i]));
          this.videos.add(newVideo);
          
          videoSizes.add(Integer.parseInt(secondLine[i]));
      }

      for (int i = 0; i < E; i++) {
        line = bufferedReader.readLine();
        String[] endPoint = line.split(" ");
        int LD = Integer.parseInt(endPoint[0]);
        int K = Integer.parseInt(endPoint[1]);
        Endpoint newPoint = new Endpoint(LD, K, i);
        for (int j = 0; j < K; j++) {
          line = bufferedReader.readLine();
          String[] connection = line.split(" ");
          int c = Integer.parseInt(connection[0]);
          int LC = Integer.parseInt(connection[1]);
          newPoint.addConnection(c, LC);
          newPoint.addLink(c, LC);
        }
        endpoints.add(newPoint);
      }

      for (int i = 0; i < R; i++) {
        line = bufferedReader.readLine();
        String[] request = line.split(" ");
        int RV = Integer.parseInt(request[0]);
        int RE = Integer.parseInt(request[1]);
        int RN = Integer.parseInt(request[2]);
        
        Request newRequest = new Request(this.endpoints.get(RE), this.videos.get(RV), RN);
        this.requests.add(newRequest);
        
        endpoints.get(RE).addRequest(RV, RN);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void findVideoAssignments() {
      int count = 0;
      for (Request request : this.requests) {
//          System.out.println("assigning a request");
          ArrayList<Integer> orderedCaches = request.endpoint.getCachesSortedByEndpointLatency();
//          System.out.println(orderedCaches);
          this.assignRequest(orderedCaches, request);
      }
  }
  
  public void assignRequest(ArrayList<Integer> orderedCaches, Request request) {
      while (request.orderedCacheIndex < orderedCaches.size()) {
          boolean succeeded = this.insertRequestIntoCache(orderedCaches.get(request.orderedCacheIndex), request);
                              
          if (succeeded) {
              break;
          } else {
              request.orderedCacheIndex++;
          }
      }
  }
  
  public boolean cacheIsOverloaded(CacheServer cache) {
      int totalRequestSize = 0;
      
      for (Request request : cache.storedRequests) {
          totalRequestSize += request.video.size;
      }
      
      if (totalRequestSize > cache.cacheSize) {
          System.out.println("found overload. " + this.assignedRequestCount);
      }
      
      return totalRequestSize > cache.cacheSize ? true : false;
  }
  
  public boolean cacheHasDuplicates(CacheServer cache) {
      HashSet<Video> seenVideos = new HashSet<Video>();
      
      for (Request request : cache.storedRequests) {
          if (seenVideos.contains(request.video)) {
              return true;
          }
          seenVideos.add(request.video);
      }
      
      return false;
  }
  
  public boolean insertRequestIntoCache(Integer cacheId, Request request) {
      CacheServer cache = this.servers.get(cacheId);
      request.calculateStreamTime(request.endpoint.getLatencyFromCache(cacheId));
      request.calculateSavedTime();

      for (Request r : cache.storedRequests) {
          if (r.video.videoId == request.video.videoId) {
              return true;
          }
      }
      
      ArrayList<Request> removedRequests = new ArrayList<>();
      
      if (this.fileName == "../example" || this.fileName == "../learning_cooking_from_youtube" || this.fileName == "../me_working_from_home") {
          removedRequests = this.knapsackCache(cache, request);
      } else {
          removedRequests = this.removeLowest(cache, request);
      }
      
      if (removedRequests.contains(request)) {
          return false;
      } else {
          cache.storedRequests.add(request);
          for (Request removed : removedRequests) {
              cache.storedRequests.remove(removed);
          }
          for (Request removed : removedRequests) {
              removed.orderedCacheIndex++;
              this.assignRequest(removed.endpoint.getCachesSortedByEndpointLatency(), removed);
          }
          return true;
      }
      
  }
  
  public ArrayList<Request> removeLowest(CacheServer cache, Request newRequest) {
      int capacity = cache.cacheSize;
      int requestSize = newRequest.video.size;
      int currentUsage = 0;
      ArrayList<Request> removedRequests = new ArrayList<>();
      
      for (Request r : cache.storedRequests) {
          currentUsage += r.video.size;
      }
      
      if (capacity - currentUsage >= requestSize) {
          return removedRequests;
      }
      
      int delta = capacity - currentUsage - requestSize;
      int removedTimeSaved = 0;
      
      ArrayList<Request> sortedStoredRequests = cache.getSortedStoredRequestsBySavedTime();
      
      while (delta < 0) {
          if (sortedStoredRequests.size() == 0) {
              break;
          }
          Request lowest = sortedStoredRequests.get(0);
          int usage = lowest.video.size;
          int timeSaved = lowest.savedTime;
          removedTimeSaved += timeSaved;
          
          if (removedTimeSaved >= newRequest.savedTime) {
              removedRequests.clear();
              removedRequests.add(newRequest);
              break;
          }
          
          removedRequests.add(lowest);
          sortedStoredRequests.remove(lowest);
          
          delta += usage; 
      }
      
      return removedRequests;
  }
  
  public ArrayList<Request> knapsackCache(CacheServer cache, Request newRequest) {
      int capacity = cache.cacheSize;
      ArrayList<Request> options = new ArrayList<>();
      int n = cache.storedRequests.size() + 1;

      for (Request r : cache.storedRequests) {
          options.add(r);
      }
      options.add(newRequest);
      
      int i, w; 
      int K[][] = new int[n + 1][capacity + 1]; 

//      System.out.println(options.get(0).toString());
//      System.out.println(options.get(0).video.size);
//      System.out.println(options.get(0).savedTime);
      for (i = 0; i <= n; i++) { 
          for (w = 0; w <= capacity; w++) { 
              if (i == 0 || w == 0) 
                  K[i][w] = 0; 
              else if (options.get(i - 1).video.size <= w) 
                  K[i][w] = Math.max(options.get(i - 1).savedTime +  
                            K[i - 1][w - options.get(i - 1).video.size], K[i - 1][w]); 
              else
                  K[i][w] = K[i - 1][w]; 
          } 
      } 

      // stores the result of Knapsack 
      int res = K[n][capacity];
//      System.out.println("Saved time in cache: " + res);
      ArrayList<Request> removedRequests = new ArrayList<>();
      for (Request r : options) {
          removedRequests.add(r);
      }

      w = capacity; 
      for (i = n; i > 0 && res > 0; i--) { 

          // either the result comes from the top 
          // (K[i-1][w]) or from (val[i-1] + K[i-1] 
          // [w-wt[i-1]]) as in Knapsack table. If 
          // it comes from the latter one/ it means 
          // the item is included. 
          if (res == K[i - 1][w]) 
              continue; 
          else { 

              // This item is included. 
              removedRequests.remove(options.get(i - 1));

              // Since this weight is included its 
              // value is deducted 
              res = res - options.get(i - 1).savedTime; 
              w = w - options.get(i - 1).video.size; 
          } 
      }
      
      return removedRequests;
  }

  public void getSolution() {
    // loop over endpoints and find video scores
    for (Endpoint e : endpoints) {
      e.calculateVideoScores(videoSizes, X, servers);
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
            servers.get(cacheId).videoScores.get(video).videoScore = new Long(0);
            end = false;
          }
        }
      }
    }
    */
    
    // loop over cacheservers and store videos
    for (CacheServer c : servers) {
      for (Integer video : c.getSortedVideosByScore()) {
        boolean willContinue = c.storeVideo(video, videoSizes.get(video));
        if (!willContinue) {
          break;
        }
      }
    }
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
  
  public void printNewOutput() {
      PrintWriter writer = null;
      try {
        writer = new PrintWriter(fileName + ".out", "UTF-8");
        int count = 0;
        ArrayList<CacheServer> usedCaches = new ArrayList<>();
        for (CacheServer cache : servers) {
          if (cache.storedRequests.size() > 0) {
            usedCaches.add(cache);
            count++;
          }
        }
        writer.print(count);
        writer.println();
        
        for (CacheServer cache : usedCaches) {
          writer.print(cache.serverId);
          for (Request request : cache.storedRequests) {
            writer.print(" " + request.video.videoId);
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



