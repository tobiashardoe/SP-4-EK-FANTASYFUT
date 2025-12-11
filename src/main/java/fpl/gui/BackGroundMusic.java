package fpl.gui;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class BackGroundMusic {

    private static MediaPlayer mediaPlayer;

    public static void playMusic() {
        try {
            String path = BackGroundMusic.class.getResource("/playlist/Ride.mp3").toExternalForm();
            Media media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // loop
            mediaPlayer.setVolume(0.3); // styr volume 0.0 - 1.0
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("⚠ Kunne ikke loade musikfilen!");
        }
    }
}
