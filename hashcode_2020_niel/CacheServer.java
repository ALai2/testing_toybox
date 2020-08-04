import java.util.ArrayList;
import java.util.Collections;

public class CacheServer {
  Integer serverId;
  
  ArrayList<Request> storedRequests = new ArrayList<>();
  
  ArrayList<Video> videoScores = new ArrayList<>();
  ArrayList<Integer> videosStored = new ArrayList<>();
  Integer cacheSize;

  public CacheServer(Integer serverId, Integer V, Integer cacheSize){
    this.serverId = serverId;
    for (int i = 0; i < V; i++) {
      videoScores.add(new Video(i, new Long(0)));
    }
    this.cacheSize = cacheSize;
  }
  
  public CacheServer(Integer serverId, Integer cacheSize) {
      this.serverId = serverId;
      this.cacheSize = cacheSize;
  }
  
  public ArrayList<Request> getSortedStoredRequestsByStreamTime() {
      Collections.sort(this.storedRequests, (Request a, Request b) -> a.streamTime.compareTo(b.streamTime));
      ArrayList<Request> sortedRequests = new ArrayList<>();
      
      for (Request request : this.storedRequests) {
          sortedRequests.add(request);
      }
      
      return sortedRequests;
  }
  
  public ArrayList<Request> getSortedStoredRequestsBySavedTime() {
      Collections.sort(this.storedRequests, (Request a, Request b) -> a.savedTime.compareTo(b.savedTime));
      ArrayList<Request> sortedRequests = new ArrayList<>();
      
      for (Request request : this.storedRequests) {
          sortedRequests.add(request);
      }
      
      return sortedRequests;
  }
  
  public void storeRequest(Request request, Integer latency, Integer dataCenterLatency) {
      request.calculateStreamTime(latency);
      request.calculateSavedTime();
      this.storedRequests.add(request);
  }
  
  public void addScore(Integer videoId, Long videoScore) {
    Video currentVideo = videoScores.get(videoId);
    if (currentVideo != null) {
      currentVideo.videoScore += videoScore;
    } else {
      videoScores.set(videoId, new Video(videoId, videoScore));
    }
  }

  public ArrayList<Integer> getSortedVideosByScore() {
    Collections.sort(videoScores, (Video a, Video b) -> b.videoScore.compareTo(a.videoScore));
    ArrayList<Integer> sortedVideos = new ArrayList<>();
    for (Video pair : videoScores) {
      if (pair.videoScore > 0) {
        sortedVideos.add(pair.videoId);
      }
    }
    return sortedVideos;
  }

  public boolean storeVideo(Integer videoId, Integer videoSize) {
    if (videoSize > cacheSize) {
      return false;
    }
    videosStored.add(videoId);
    cacheSize -= videoSize;
    return true;
  }
}