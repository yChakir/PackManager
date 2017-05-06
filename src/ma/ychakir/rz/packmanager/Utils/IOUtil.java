package ma.ychakir.rz.packmanager.Utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Yassine
 */
public class IOUtil {
    public static void zipIt(String filePath, String zipPath, String zipName) throws IOException {
        File destination = new File(new File(zipPath).getParent());
        if (!destination.exists()) destination.mkdirs();

        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileInputStream in = new FileInputStream(filePath);

        ZipEntry entry = new ZipEntry(zipName);
        zos.putNextEntry(entry);

        IOUtils.copy(in, zos);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(zos);
        IOUtils.closeQuietly(fos);
    }

    public static String randomString() {
        final SecureRandom random = new SecureRandom();
        //return a random string
        return new BigInteger(32, random).toString(32);
    }
}
