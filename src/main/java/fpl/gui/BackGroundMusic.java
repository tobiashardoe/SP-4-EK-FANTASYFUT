package fpl.gui;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class BackGroundMusic {

    private static MediaPlayer mediaPlayer;

    public static void playMusic() {

        if (mediaPlayer != null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                String path = BackGroundMusic.class
                        .getResource("/playlist/Ride.mp3")
                        .toExternalForm();

                Media media = new Media(path);
                mediaPlayer = new MediaPlayer(media);

                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(0.3);
                mediaPlayer.play();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("⚠ Kunne ikke loade musikfilen!");
            }
        });
    }
}
