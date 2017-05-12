package ma.ychakir.rz.packmanager.Controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ma.ychakir.rz.packmanager.Exceptions.NoUrlException;
import ma.ychakir.rz.packmanager.Models.AwesomeIcons;
import ma.ychakir.rz.packmanager.Models.Pack;
import ma.ychakir.rz.packmanager.Runnables.PackRunnable;
import ma.ychakir.rz.packmanager.Utils.AwesomeDude;
import ma.ychakir.rz.packmanager.Utils.DialogUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable {
    private static Stage stage;
    private static boolean isPacking;
    public TableView<Map<String, String>> tableView;
    public AnchorPane anchor;
    public JFXDialogLayout dialogLayout;
    public StackPane stackPane;
    public JFXButton btnImport;
    public JFXButton btnSave;
    public JFXButton btnRemove;
    public JFXButton btnAbout;
    public JFXProgressBar pb;
    public JFXToolbar toolBar;
    private TableColumn<Map<String, String>, String> columnName = new TableColumn<>("Name");
    private TableColumn<Map<String, String>, String> columnSource = new TableColumn<>("Source");
    private TableColumn<Map<String, String>, String> columnDestination = new TableColumn<>("Destination");

    public static void setIsPacking(boolean isPacking) {
        Controller.isPacking = isPacking;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        Controller.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setIsPacking(false);
        DialogUtil.setPane(anchor);

        stage.setOnCloseRequest(e -> {
            e.consume();
            close();
        });

        AwesomeDude.addIconButton(btnImport, AwesomeIcons.ICON_FOLDER_OPEN, 30);
        AwesomeDude.addIconButton(btnSave, AwesomeIcons.ICON_SAVE, 30);
        AwesomeDude.addIconButton(btnRemove, AwesomeIcons.ICON_REMOVE, 30);
        AwesomeDude.addIconButton(btnAbout, AwesomeIcons.ICON_INFO_SIGN, 30);

        columnName.setCellValueFactory(new MapValueFactory("Name"));
        columnName.setPrefWidth(200);

        columnSource.setCellValueFactory(new MapValueFactory("Source"));
        columnSource.setPrefWidth(200);

        columnDestination.setCellValueFactory(new MapValueFactory("Destination"));
        columnDestination.setPrefWidth(200);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableView.getColumns().addAll(columnName, columnSource, columnDestination);
        toolBar.setCenter(tableView);
    }

    private void close() {
        JFXDialog dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
        JFXButton btnYes = new JFXButton("Yes"), btnNo = new JFXButton("No");
        List<JFXButton> actions = new ArrayList<>();

        AwesomeDude.addIconButton(btnYes, AwesomeIcons.ICON_OK, 22);
        AwesomeDude.addIconButton(btnNo, AwesomeIcons.ICON_BAN_CIRCLE, 22);

        actions.add(btnYes);
        actions.add(btnNo);

        dialog.setOnDialogClosed(e -> {
            dialogLayout.setVisible(false);
            stackPane.setVisible(false);
        });
        btnNo.setOnAction(e -> dialog.close());
        btnYes.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        dialogLayout.setHeading(new Text("Do you want to close the program ?"));
        dialogLayout.setBody();
        dialogLayout.setActions(actions);

        stackPane.setVisible(true);
        dialogLayout.setVisible(true);
        dialog.show();
    }

    public void showImport() {
        if (!isPacking) {
            JFXDialog dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
            JFXTextField textField = new JFXTextField();
            RequiredFieldValidator validator = new RequiredFieldValidator();
            JFXButton btnOk = new JFXButton("Ok"), btnCancel = new JFXButton("Cancel");
            ToggleGroup rbGroupe = new ToggleGroup();
            JFXRadioButton rbData = new JFXRadioButton("Client Data.");
            JFXRadioButton rbResource = new JFXRadioButton("Client Resource.");
            JFXRadioButton rbDirectory = new JFXRadioButton("Specific Directory.");
            JFXRadioButton rbCurrent = new JFXRadioButton("Current Directory.");
            List<JFXButton> actions = new ArrayList<>();

            rbData.setSelected(true);
            rbData.setToggleGroup(rbGroupe);
            rbCurrent.setToggleGroup(rbGroupe);
            rbResource.setToggleGroup(rbGroupe);
            rbDirectory.setToggleGroup(rbGroupe);

            textField.setPromptText("Destination directory.");
            textField.setMinWidth(220);
            textField.setDisable(true);
            textField.setLabelFloat(true);
            textField.getValidators().add(validator);

            validator.setMessage("Please enter directory name.");

            AwesomeDude.addIconButton(btnOk, AwesomeIcons.ICON_OK, 22);
            AwesomeDude.addIconButton(btnCancel, AwesomeIcons.ICON_BAN_CIRCLE, 22);

            actions.add(btnOk);
            actions.add(btnCancel);

            rbDirectory.selectedProperty().addListener(observable -> {
                textField.setDisable(!rbDirectory.isSelected());
                textField.validate();
            });
            textField.textProperty().addListener(observable -> textField.validate());
            dialog.setOnDialogClosed(e -> {
                dialogLayout.setVisible(false);
                stackPane.setVisible(false);
            });
            btnCancel.setOnAction(e -> dialog.close());
            btnOk.setOnAction(e -> {
                if (rbData.isSelected()) {
                    importPatch("<Data>");
                } else if (rbResource.isSelected()) {
                    importPatch("<Resource>");
                } else if (rbCurrent.isSelected()) {
                    importPatch("<Current>");
                } else if (rbDirectory.isSelected() && textField.validate()) {
                    importPatch(textField.getText());
                } else {
                    textField.requestFocus();
                    return;
                }
                dialog.close();
            });

            VBox vBox = new VBox();
            HBox hBox = new HBox();
            vBox.setSpacing(15);
            hBox.setSpacing(20);

            vBox.getChildren().add(rbData);
            vBox.getChildren().add(rbResource);
            vBox.getChildren().add(rbCurrent);
            hBox.getChildren().add(rbDirectory);
            hBox.getChildren().add(textField);
            vBox.getChildren().add(hBox);
            vBox.getChildren().add(new Label());

            dialogLayout.setHeading(new Text("Please choose a destination to the files:\n"));
            dialogLayout.setBody(vBox);
            dialogLayout.setActions(actions);

            stackPane.setVisible(true);
            dialogLayout.setVisible(true);
            dialog.show();
        } else {
            DialogUtil.toast("Please waite until packing finish.", 5000);
        }
    }

    private void importPatch(String destination) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import patch.");
        List<File> files = chooser.showOpenMultipleDialog(stage);

        if (files == null || files.size() == 0) {
            DialogUtil.toast("No file selected !", 5000);
        } else {
            ObservableList<Map<String, String>> observableList = tableView.getItems();

            files.forEach(file -> {
                Map<String, String> element = new HashMap<>();
                element.put("Name", file.getName());
                element.put("Source", file.getParent());
                element.put("Destination", destination);
                observableList.add(element);
            });

            DialogUtil.toast(String.format("%d File(s) imported.", files.size()), 5000);
        }
    }

    public void remove() {
        if (!isPacking) {
            ObservableList<Map<String, String>> i = tableView.getSelectionModel().getSelectedItems();
            int size;
            if (i != null && (size = i.size()) != 0) {
                tableView.getItems().removeAll(i);
                tableView.getSelectionModel().clearSelection();
                DialogUtil.toast(String.format("%d File(s) removed.", size), 5000);
            } else {
                DialogUtil.toast("No row selected !", 5000);
            }
        } else {
            DialogUtil.toast("Please waite until packing finish.", 5000);
        }
    }

    public void about() {
        JFXSnackbar snackbar = new JFXSnackbar(anchor);
        snackbar.show("Pack Manager For vLauncher, Coded by Volon.", "Github", 6000, e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/yChakir"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            } finally {
                snackbar.close();
            }
        });
    }

    public void save() {
        if (!isPacking) {
            JFXDialog dialog = new JFXDialog(stackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
            JFXTextField textField = new JFXTextField();
            RequiredFieldValidator validator = new RequiredFieldValidator();
            JFXButton btnOk = new JFXButton("Ok"), btnCancel = new JFXButton("Cancel");
            List<JFXButton> actions = new ArrayList<>();

            textField.setPromptText("Download url.");
            textField.setLabelFloat(true);
            textField.getValidators().add(validator);
            validator.setMessage("Please enter a valid download Url.");

            AwesomeDude.addIconButton(btnOk, AwesomeIcons.ICON_OK, 22);
            AwesomeDude.addIconButton(btnCancel, AwesomeIcons.ICON_BAN_CIRCLE, 22);

            actions.add(btnOk);
            actions.add(btnCancel);

            dialog.setOnDialogClosed(e -> {
                dialogLayout.setVisible(false);
                stackPane.setVisible(false);
            });
            btnCancel.setOnAction(e -> dialog.close());
            btnOk.setOnAction(e -> {
                if (textField.validate()) {
                    try {
                        build(textField.getText());
                    } catch (Exception ex) {
                        DialogUtil.toast(ex.getMessage(), 5001);
                    } finally {
                        dialog.close();
                    }
                } else {
                    textField.requestFocus();
                }
            });

            VBox box = new VBox();
            box.getChildren().add(new Label(""));
            box.getChildren().add(textField);

            dialogLayout.setHeading(new Text("Please enter the download url:"));
            dialogLayout.setBody(box);
            dialogLayout.setActions(actions);

            stackPane.setVisible(true);
            dialogLayout.setVisible(true);
            dialog.show();
        } else {
            DialogUtil.toast("Please waite until packing finish.", 5000);
        }
    }

    private void build(String url) throws NoUrlException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("save Pack.");
        File file = chooser.showDialog(stage);

        if (file == null || !file.exists()) {
            DialogUtil.toast("No directory selected !", 5000);
        } else {
            ObservableList<Map<String, String>> items = tableView.getItems();
            String oldVersion = file.getAbsolutePath().concat("/version.json");
            Pack newPack = new Pack(url, new HashMap<>());
            Pack oldPack = null;
            try {
                File f = new File(oldVersion);
                if (f.exists()) {
                    String s = String.join("", Files.readAllLines(f.toPath(), Charset.forName("UTF-8")));
                    oldPack = new Gson().fromJson(s, Pack.class);
                }
            } catch (Exception ex) {
                DialogUtil.toast("Unable to parse old version file.", 5000);
            }

            ExecutorService executor = Executors.newFixedThreadPool(5);
            PackRunnable.setCurrent(0);
            PackRunnable.setMax(items.size());

            for (Map<String, String> row : items) {
                if (!isPacking) setIsPacking(true);
                executor.submit(new PackRunnable(file.getAbsolutePath(), newPack, oldPack, items, row, pb));
            }

            executor.shutdown();

        }
    }
}
