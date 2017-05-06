package ma.ychakir.rz.packmanager.Utils;

import com.jfoenix.controls.JFXSnackbar;
import javafx.scene.layout.Pane;

/**
 * @author Yassine
 */
public class DialogUtil {
    private static Pane pane;

    public static void setPane(Pane pane) {
        DialogUtil.pane = pane;
    }

    public static void toast(String message, int timeout) {
        JFXSnackbar snackbar = new JFXSnackbar(pane);
        snackbar.getStyleClass().removeAll();
        snackbar.show(message, "Okey", timeout, e -> snackbar.close());
    }
}
