package org.matwoess.jsourceprofiler.tool;

import org.matwoess.jsourceprofiler.common.IO;
import org.matwoess.jsourceprofiler.tool.cli.Main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestUtils {
  static void instrumentAndProfile(Path mainFile) {
    String[] args = new String[]{mainFile.toString()};
    Main.main(args);
  }

  static void instrumentAndProfileWithArgs(String... programArgs) {
    Main.main(programArgs);
  }

  static void instrumentFolderAndProfile(Path sourcesDir, String mainFile) {
    String[] args = new String[]{"-d", sourcesDir.toString(), sourcesDir.resolve(mainFile).toString()};
    Main.main(args);
  }

  static void instrumentFolder(Path sourcesDir) {
    Main.main(new String[]{"-i", sourcesDir.toString()});
  }

  static void generateReport() {
    Main.main(new String[]{"-r"});
  }

  static void createMockCounterData() {
    int nBlocks;
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(IO.getMetadataPath().toFile()))) {
      nBlocks = ois.readInt(); // number of blocks is the first value of the metadata file
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
    try (DataOutputStream dis = new DataOutputStream(new FileOutputStream(IO.getCountsPath().toFile()))) {
      dis.writeInt(nBlocks);
      for (int i = 0; i < nBlocks; i++) {
        dis.writeLong(1);
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public static Path downloadGithubRepoZip(Path projectsRoot, String folderName, String repoName) {
    String zipDownloadUrl = "https://api.github.com/repos/" + repoName + "/zipball";
    Path zipPath = projectsRoot.resolve(folderName + ".zip");
    System.out.printf("Downloading repository %s (from %s)...\n", repoName, zipDownloadUrl);
    int responseCode;
    HttpURLConnection connection;
    try {
      connection = (HttpURLConnection) new URL(zipDownloadUrl).openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
      responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        System.out.println("Failed to download. HTTP error code: " + responseCode);
        System.out.println("Message: " + connection.getResponseMessage());
        connection.disconnect();
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try (BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
         BufferedOutputStream zipOutputStream = new BufferedOutputStream(new FileOutputStream(zipPath.toFile()))) {
      inputStream.transferTo(zipOutputStream);
      System.out.printf("Repository downloaded successfully to %s.\n", zipPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      connection.disconnect();
    }
    return zipPath;
  }

  public static void unzipRepo(Path zipPath, Path destDirectory, String renameRootFolderTo) {
    System.out.printf("Unpacking %s...\n", zipPath.toString());
    String extractedFolderName = null;
    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
      ZipEntry entry = zipIn.getNextEntry();
      if (entry != null && entry.isDirectory()) {
        extractedFolderName = entry.getName();
      }
      while (entry != null) {
        Path destPath = destDirectory.resolve(entry.getName());
        if (entry.isDirectory()) {
          //noinspection ResultOfMethodCallIgnored
          destPath.toFile().mkdirs();
        } else {
          Files.copy(zipIn, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
      if (extractedFolderName != null && renameRootFolderTo != null) {
        System.out.printf("Renaming directory %s to %s...\n", extractedFolderName, renameRootFolderTo);
        Files.move(destDirectory.resolve(extractedFolderName), destDirectory.resolve(renameRootFolderTo));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Deleting zip file...");
    try {
      Files.delete(zipPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
