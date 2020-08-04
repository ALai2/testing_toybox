import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Endpoint {
  Integer centerLatency;
  Integer K;
  HashMap<Integer, Integer> connections = new HashMap<>();
  HashMap<Integer, Integer> requests = new HashMap<>();
  HashMap<Integer, CacheServer> requestCachePairs = new HashMap<>();
  
  public Endpoint(Integer centerLatency, Integer K) {
    this.centerLatency = centerLatency;
    this.K = K; // number of connected caches
  }

  public void addConnection(Integer cacheId, Integer cacheLatency) {
    this.connections.put(cacheId, cacheLatency);
  }

  public void addRequest(Integer videoId, Integer requestNum) {
    requests.put(videoId, requestNum);
  }

  public void calculateVideoScores(ArrayList<Integer> videos, Integer cacheSize, ArrayList<CacheServer> caches) {
    for (Map.Entry<Integer, Integer> request : requests.entrySet()) {
      Long maxScore = new Long(0);
      Integer videoId = request.getKey();
      Integer maxCacheId = 0;
      Integer videoWeight = cacheSize - videos.get(videoId);
      for (Map.Entry<Integer, Integer> connection : connections.entrySet()) {
        Integer savedTime = (centerLatency - connection.getValue()) * request.getValue();
        Long videoScore = Long.valueOf(videoWeight * savedTime);
        if (videoScore > maxScore) {
          maxScore = videoScore;
          maxCacheId = connection.getKey();
        }
        caches.get(connection.getKey()).addScore(videoId, videoScore);
      }
      requestCachePairs.put(videoId, caches.get(maxCacheId));
      
    }
  }

  public void removeVideoScores(Integer videoId, Integer cacheId, ArrayList<Integer> videos, Integer cacheSize, ArrayList<CacheServer> caches) {
    if (requests.containsKey(videoId) && connections.containsKey(cacheId)) {
      Integer videoWeight = cacheSize - videos.get(videoId);
      for (Map.Entry<Integer, Integer> connection : connections.entrySet()) {
        Integer savedTime = (centerLatency - connection.getValue()) * requests.get(videoId);
        Long videoScore = Long.valueOf(videoWeight * savedTime * -1);
        caches.get(connection.getKey()).addScore(videoId, videoScore);
      }
    }
    requests.remove(videoId);
  }

  public Integer getFirstKey() {
    //return requests.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
    if (requests.isEmpty()) return null;
    //return requests.keySet().iterator().next();
    return requestCachePairs.entrySet().stream().max((entry1, entry2) -> (entry1.getValue().videoScores.get(entry1.getKey()).videoScore).compareTo(entry2.getValue().videoScores.get(entry2.getKey()).videoScore)).get().getKey();
  }

  public void loopThroughRequests(ArrayList<Integer> videos, ArrayList<CacheServer> caches) {
    while (!requests.isEmpty()) {
      Integer firstKey = getFirstKey();
      storeVideoByRequest(firstKey, videos, caches);
    }
  }

  public void storeVideoByRequest(Integer videoId, ArrayList<Integer> videos, ArrayList<CacheServer> caches) {
    CacheServer c = requestCachePairs.get(videoId);
    boolean willContinue = caches.get(c.serverId).storeVideo(videoId, videos.get(videoId));
    requests.remove(videoId);
    requestCachePairs.remove(videoId);
  }
}