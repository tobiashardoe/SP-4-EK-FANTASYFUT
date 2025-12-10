package fpl.gui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class BackgroundMusic {

    private static MediaPlayer mediaPlayer;

    public static void playMusic() {
        URL url = BackgroundMusic.class.getResource("/music/Ride.mp3");
        System.out.println(url);

        if (url == null) {
            System.err.println("ERROR: Could not find /music/Ride.mp3 on classpath");
            return;
        }

        Media media = new Media(url.toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setVolume(0.5);
        mediaPlayer.play();
    }
}
