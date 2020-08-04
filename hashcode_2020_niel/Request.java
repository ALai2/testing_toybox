import java.util.HashMap;

public class Request {

    Endpoint endpoint;
    Video video;
    Integer requestCount;
    Integer streamTime = 0;
    Integer savedTime = 0;
    Integer orderedCacheIndex = 0;
    
    public Request(Endpoint endpoint, Video video, Integer requestCount) {
        this.endpoint = endpoint;
        this.video = video;
        this.requestCount = requestCount;
    }
    
    public void calculateStreamTime(Integer latency) {
        this.streamTime = latency * this.requestCount;
    }
    
    public void calculateSavedTime() {
        this.savedTime = this.endpoint.centerLatency * this.requestCount - this.streamTime;
    }
    
    public HashMap<Integer, Integer> getEndpointConnections() {
        return this.endpoint.connections;
    }
    
    public String toString() {
        return "(" + this.requestCount + ", " + this.video.videoId + ", " + this.endpoint.endpointId + ")";
    }
    
}
