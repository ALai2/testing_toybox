import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

public class Endpoint {
  Integer centerLatency;
  Integer K;
  HashMap<Integer, Integer> connections = new HashMap<>();
  HashMap<Integer, Integer> requests = new HashMap<>();
  ArrayList<Connection> links = new ArrayList<>();
  Integer endpointId;
  
  public Endpoint(Integer centerLatency, Integer K, Integer id) {
    this.centerLatency = centerLatency;
    this.K = K; // number of connected caches
    this.endpointId = id;
  }

  public void addConnection(Integer cacheId, Integer cacheLatency) {
    this.connections.put(cacheId, cacheLatency);
  }

  public void addRequest(Integer videoId, Integer requestNum) {
    requests.put(videoId, requestNum);
  }
  
  public void addLink(Integer cacheId, Integer latency) {
      this.links.add(new Connection(cacheId, latency));
  }

  public void calculateVideoScores(ArrayList<Integer> videos, Integer cacheSize, ArrayList<CacheServer> caches) {
    for (Map.Entry<Integer, Integer> request : requests.entrySet()) {
      Integer videoId = request.getKey();
      Integer videoWeight = cacheSize - videos.get(videoId);
      for (Map.Entry<Integer, Integer> connection : connections.entrySet()) {
        Integer savedTime = (centerLatency - connection.getValue()) * request.getValue();
        Long videoScore = Long.valueOf(videoWeight * savedTime);
        caches.get(connection.getKey()).addScore(videoId, videoScore);
      }
      
    }
  }
  
  public Integer getLatencyFromCache(Integer id) {
      for (Connection connection : this.links) {
          if (connection.cacheId == id) {
              return connection.latency;
          }
      }
      return -1;
  }
  
  public ArrayList<Integer> getCachesSortedByEndpointLatency() {
      ArrayList<Integer> sortedCacheIds = new ArrayList<>();
      Collections.sort(this.links, (Connection a, Connection b) -> a.latency.compareTo(b.latency));
      
      for (Connection connection : this.links) {
          sortedCacheIds.add(connection.cacheId);
      }
      
      return sortedCacheIds;
  }
}