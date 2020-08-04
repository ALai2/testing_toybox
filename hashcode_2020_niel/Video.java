public class Video {
  Integer videoId;
  Long videoScore;
  Integer size;
  
  public Video(Integer videoId, Long videoScore) {
    this.videoId = videoId;
    this.videoScore = videoScore;
  }
  
  public Video(Integer videoId, Integer size) {
      this.videoId = videoId;
      this.size = size;
  }
  
}