public class Main {
  public static void main(String[] args) {
    String[] fileNames = {"../example", "../learning_cooking_from_youtube", "../me_working_from_home", "../music_videos_of_2020", "../vloggers_of_the_world"};
    //String[] fileNames = {"../example"};
    for(String fileName: fileNames) {
      DataCenter newCenter = new DataCenter(fileName);
      newCenter.parseInput();
      newCenter.getSolution();
      newCenter.printOutput();
    }
  }

}