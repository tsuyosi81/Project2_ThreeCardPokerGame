package edu.uic.cs342.poker.navigation;

import edu.uic.cs342.poker.service.GameService;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * Switches between screens, keeps the current game, and handles the theme
 * and the "reduce motion" setting.
 */
public class SceneManager {
    private static final String BASE = "/edu/uic/cs342/poker/";

    private static Stage stage;
    private static Scene scene;
    private static GameService game = new GameService();
    private static boolean darkTheme = false;
    private static boolean reduceMotion = false;

    public static void init(Stage s) {
        stage = s;
        scene = new Scene(new Pane(), 1100, 820);
        applyTheme();
        stage.setScene(scene);
        stage.setTitle("Three Card Poker");
        stage.setMinWidth(980);
        stage.setMinHeight(720);
        show("welcome.fxml");
        stage.show();
    }

    /** Loads an FXML file from the fxml folder and shows it in the window. */
    public static void show(String fxmlName) {
        Parent root;
        try {
            root = FXMLLoader.load(SceneManager.class.getResource(BASE + "fxml/" + fxmlName));
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + fxmlName, e);
        }
        scene.setRoot(root);

        // fade the new screen in so the change feels connected
        if (!reduceMotion) {
            FadeTransition fade = new FadeTransition(Duration.millis(250), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    // ---------- game ----------

    public static GameService getGame() {
        return game;
    }

    public static void newGame() {
        game = new GameService();
    }

    // ---------- theme ----------

    public static boolean isDarkTheme() {
        return darkTheme;
    }

    public static void toggleTheme() {
        darkTheme = !darkTheme;
        applyTheme();
    }

    /** Text for the theme button: says what clicking it will do. */
    public static String themeButtonText() {
        return darkTheme ? "Switch to Light" : "Switch to Dark";
    }

    private static void applyTheme() {
        scene.getStylesheets().setAll(stylesheets());
    }

    /** Both stylesheets the current theme uses (dialogs need them too). */
    public static String[] stylesheets() {
        String base = css("light.css");
        if (darkTheme) {
            return new String[]{base, css("dark.css")};
        }
        return new String[]{base};
    }

    private static String css(String name) {
        URL url = SceneManager.class.getResource(BASE + "css/" + name);
        return url.toExternalForm();
    }

    // ---------- reduce motion ----------

    public static boolean isReduceMotion() {
        return reduceMotion;
    }

    public static void setReduceMotion(boolean value) {
        reduceMotion = value;
    }

    public static Stage getStage() {
        return stage;
    }
}
