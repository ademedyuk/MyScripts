package ru.demedyuk.scripts;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CleanDesktopScript {

    private static Logger log = Logger.getLogger(CleanDesktopScript.class);

    private static final String USER_HOME = System.getenv("HOMEPATH");
    private static String DESKTOP_PATH = USER_HOME + "\\Desktop";
    private static String garbageFolderName = "\\AllGarbage";
    private static String excludeFile = "exclude.txt";
    private Boolean isExternal = null;

    private List<String> excludeFiles = new ArrayList<>();

    public void run(String arg) {
        try {
            if ((isExternal = Boolean.valueOf(arg)) == null)
                throw new IllegalArgumentException("First argument cannot be null");
            log.info("Start сleaning");

            doClean();

            log.info("Cleaning completed successfully");
        } catch (Exception e) {
            log.error("Cleaning failed: " + e.getMessage());
        }
    }

    public void doClean() throws Exception {
        File desktopFolder = new File(DESKTOP_PATH);
        File garbageFolder = initGarbageFolder();

        List<File> garbageFiles = getGarbage(desktopFolder);

        if (garbageFiles.size() == 0) {
            log.info("Garbage files is not exists");
            return;
        }

        moveFiles(garbageFiles, garbageFolder);
    }

    private List<File> getGarbage(File fromFolder) throws Exception {
        File[] allFiles = fromFolder.listFiles();

        excludeFiles = isExternal ? getContentFromExternalFile(excludeFile) : getContentFromResources(excludeFile);

        if (excludeFiles == null) {
            throw new Exception(String.format("Exclude file '%s' not found", excludeFile));
        }

        List<File> needToMove = new ArrayList<>();
        for (File file : allFiles) {
            if (!isExclude(file)) {
                needToMove.add(file);
            }
        }

        return needToMove;
    }

    private List<String> getContentFromExternalFile(String fileName) {

        try {
            URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path dir = Paths.get(uri).getParent();
            Path excludeFilePath = dir.resolve(fileName);

            return Files.readAllLines(excludeFilePath);
        } catch (Exception e) {
            log.error("Error while reading external exclude file: " + e.getMessage());
        }

        return null;
    }

    private File initGarbageFolder() {
        File garbageFolder = new File(DESKTOP_PATH + garbageFolderName);
        if (!garbageFolder.exists())
            garbageFolder.mkdirs();
        return garbageFolder;
    }

    public void moveFiles(List<File> files, File toFolder) {
        for (File needToMoveFile : files) {
            needToMoveFile.renameTo(new File(toFolder, needToMoveFile.getName()));
            log.info("File deleted: " + needToMoveFile.getPath());
        }
    }

    public boolean isExclude(File file) {
        for (String filePattern : excludeFiles) {
            if (!filePattern.equals("") && file.getName().indexOf(filePattern) != -1)
                return true;
        }
        return false;
    }

    public List<String> getContentFromResources(String fileName) throws IOException {
        List<String> textFromFile = new ArrayList<>();

        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);

        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line;
        while ((line = reader.readLine()) != null) {
            textFromFile.add(line);
        }

        return textFromFile;
    }
}
