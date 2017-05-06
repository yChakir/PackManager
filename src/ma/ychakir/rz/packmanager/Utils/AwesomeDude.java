package ma.ychakir.rz.packmanager.Utils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * @author Yassine
 *         Edited from: http://www.jensd.de/wordpress/?p=132
 */
public class AwesomeDude {

    public static void addIconButton(Button btn, String iconName, int size) {
        Label icon = createIconLabel(iconName);
        icon.setStyle("-fx-font-size: " + size + "px;");
        btn.setGraphic(icon);
    }

    public static void addIconButton(Button btn, String iconName) {
        addIconButton(btn, iconName, 16);
    }

    public static Button createIconButton(String iconName) {
        return createIconButton(iconName, "", 16);
    }

    public static Button createIconButton(String iconName, String text) {
        return createIconButton(iconName, text, 16);
    }

    public static Button createIconButton(String iconName, int iconSize) {
        return createIconButton(iconName, "", iconSize);
    }

    public static Button createIconButton(String iconName, String text, int iconSize) {
        Label icon = createIconLabel(iconName);
        icon.setStyle("-fx-font-size: " + iconSize + "px;");
        Button btn = new Button(text);
        btn.setGraphic(icon);
        return btn;
    }

    public static Label createIconLabel(String iconName) {
        return createIconLabel(iconName, 16);
    }

    public static Label createIconLabel(String iconName, int iconSize) {
        Label label = new Label(iconName);
        label.getStyleClass().add("icons");
        label.setStyle("-fx-font-size: " + iconSize + "px;");

        return label;
    }
}
