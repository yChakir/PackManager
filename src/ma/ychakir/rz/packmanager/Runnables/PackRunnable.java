package ma.ychakir.rz.packmanager.Runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import ma.ychakir.rz.packmanager.Controllers.Controller;
import ma.ychakir.rz.packmanager.Models.Pack;
import ma.ychakir.rz.packmanager.Models.RzFile;
import ma.ychakir.rz.packmanager.Utils.DialogUtil;
import ma.ychakir.rz.packmanager.Utils.IOUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yassine
 */
public class PackRunnable implements Runnable {

    private static int max;
    private static int current;
    private static PackResult result = PackResult.PACK_SUCCESS;
    private String dest;
    private Pack oldPack;
    private Pack newPack;
    private ObservableList<Map<String, String>> tableView;
    private Map<String, String> row;
    private JFXProgressBar pb;

    public PackRunnable(String dest,
                        Pack newPack,
                        Pack oldPack,
                        ObservableList<Map<String, String>> tableView,
                        Map<String, String> row,
                        JFXProgressBar pb) {
        this.dest = dest;
        this.newPack = newPack;
        this.oldPack = oldPack;
        this.tableView = tableView;
        this.row = row;
        this.pb = pb;
    }

    public static void setMax(int max) {
        PackRunnable.max = max;
    }

    public static void setCurrent(int current) {
        PackRunnable.current = current;
    }

    private synchronized boolean isLast() {
        return ++current == max;
    }

    public void setOldPack(Pack oldPack) {
        this.oldPack = oldPack;
    }

    public void setNewPack(Pack newPack) {
        this.newPack = newPack;
    }

    @Override
    public void run() {
        Map<String, List<RzFile>> patches = newPack.getPatches();
        String destination = row.get("Destination").isEmpty() ? "." : row.get("Destination");
        String name = row.get("Name");
        String source = row.get("Source");
        String fullPath = String.format("%s\\%s", source, name);

        //add map if not exists
        patches.computeIfAbsent(destination, k -> new ArrayList<>());

        RzFile file = new RzFile();
        file.setName(name);

        try {
            file.setSum(Long.toHexString(FileUtils.checksumCRC32(new File(fullPath))).toUpperCase());
        } catch (Exception e) {
            Platform.runLater(() -> DialogUtil.toast("Filed to calculate sum hash for " + fullPath, 5000));
            PackRunnable.result = PackResult.PACK_ERRORS;
        }

        //already added
        List<RzFile> newList = patches.get(destination);
        if (!newList.contains(file)) {
            //test if file already exist
            int exists;
            List<RzFile> oldList = oldPack != null ? oldPack.getPatches().get(destination) : null;

            if (oldList != null &&
                    !oldList.isEmpty() &&
                    (exists = oldList.indexOf(file)) != -1 &&
                    new File(String.format("%s/patches/%s.zip", dest, oldList.get(exists).getZip())).exists()) {
                file.setZip(oldList.get(exists).getZip());
            } else {
                //generate random string for zip file
                String zipName, zipPath;
                do {
                    zipName = IOUtil.randomString();
                    zipPath = String.format("%s%s.zip", dest.concat("/patches/"), zipName);
                } while (new File(zipPath).exists());

                //update RzFile info
                file.setZip(zipName);

                //zip the file
                try {
                    IOUtil.zipIt(fullPath, zipPath, name);
                } catch (IOException ex) {
                    Platform.runLater(() -> DialogUtil.toast("Filed to compress " + fullPath, 5000));
                    PackRunnable.result = PackResult.PACK_ERRORS;
                }
            }

            newList.add(file);
        }

        if (isLast()) {
            saveJson();
            cleanPatches();

            String message = "Pack created successfully.";

            if (PackRunnable.result == PackResult.PACK_FAILED)
                message = "Pack could not be created.";
            else if (PackRunnable.result == PackResult.PACK_ERRORS)
                message = "Pack was created but there was somme errors.";

            final String finalMessage = message;
            Platform.runLater(() -> {
                DialogUtil.toast(finalMessage, 5000);
                Controller.setIsPacking(false);
            });
        }

        //delete row from table view
        Platform.runLater(() -> {
            tableView.remove(row);
            pb.setProgress((double) current / (double) max);
        });
    }

    private void cleanPatches() {
        String patchesDir = dest.concat("/patches/");
        String[] files = new File(patchesDir).list();

        if (files != null) {
            List<String> exists = new ArrayList<>();
            for (String name : files) {
                Map<String, List<RzFile>> map = newPack.getPatches();

                map.forEach((s, list) -> {
                    for (RzFile file : list) {
                        if (name.equals(file.getZip().concat(".zip"))) {
                            exists.add(name);
                            return;
                        }
                    }
                });

                if (!exists.contains(name)) {
                    File del = new File(patchesDir + name);
                    try {
                        if (del.isDirectory())
                            FileUtils.deleteDirectory(del);
                        else
                            FileUtils.forceDelete(del);
                    } catch (IOException e) {
                        Platform.runLater(() -> DialogUtil.toast("Failed to delete " + del.getAbsolutePath(), 5000));
                        PackRunnable.result = PackResult.PACK_ERRORS;
                    }
                }
            }
        }
    }

    private void saveJson() {
        //save version.json
        try {
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .disableHtmlEscaping()
                    .create();
            FileUtils.writeStringToFile(new File(dest.concat("/version.json")),
                    gson.toJson(newPack),
                    "UTF-8");
        } catch (Exception e) {
            Platform.runLater(() -> DialogUtil.toast("Failed to save version.json", 5000));
            PackRunnable.result = PackResult.PACK_FAILED;
        }
    }

    private enum PackResult {
        PACK_FAILED,
        PACK_ERRORS,
        PACK_SUCCESS
    }
}
