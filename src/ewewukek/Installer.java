package ewewukek;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Installer {
    public static final File installDir = new File(getUserHome(), ".ewewukek");
    public static final byte[] buffer = new byte[65536];

    public static void install(String project) {
        try {
            File jar = new File(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (!jar.isFile()) return;
            File baseDir = new File(installDir, project);
            if (!baseDir.exists()) baseDir.mkdirs();
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(jar));
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                if (entry.getName().startsWith("native")) {
                    if (entry.isDirectory()) {
                        File dir = new File(baseDir, entry.getName());
                        if (!dir.exists()) dir.mkdirs();
                    } else {
                        File file = new File(baseDir, entry.getName());
                        if (!file.exists()) {
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                            int read = 0;
                            while ((read = zipIn.read(buffer)) != -1) {
                                bos.write(buffer, 0, read);
                            }
                            bos.close();
                        }
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
            System.setProperty("org.lwjgl.librarypath", new File(baseDir, "native").toString());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getUserHome() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return System.getenv("USERPROFILE");
        else
            return System.getProperty("user.home");
    }
}