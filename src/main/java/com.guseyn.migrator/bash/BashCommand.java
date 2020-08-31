package com.guseyn.migrator.bash;

import java.io.IOException;

public class BashCommand {
    // This command to create a folder
    public static void createFolder(String folderPath) throws InterruptedException, IOException {
        System.out.println("==> create folder : " + folderPath);
        String cmdStr = "mkdir -p " + folderPath;
        System.out.println(cmdStr);
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        System.out.println("<== folder is created");
    }

    // This function will create diff from two file
    public static void writeDiffsToOutputFile(String oldFilePath, String newUpdatedFilePath, String outputDiffFilePath) throws IOException, InterruptedException {
        String cmdStr = "diff " + oldFilePath + " " + newUpdatedFilePath + ">" + outputDiffFilePath;
        System.out.println("==> Generate Diff FileCookBook: " + outputDiffFilePath);
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        System.out.println("<== Generate done");
    }

    // delete folder
    public static void deleteFolder(String folderPath) throws InterruptedException, IOException {
        System.out.println("==> Start deleting ...");
        String cmdStr = " rm -rf " + folderPath + "";
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        System.out.println("<== Complete delete");
    }

    // copy file
    public static void copyFile(String fromFilePath, String toFilePath) throws InterruptedException, IOException {
        // System.out.println("==> Start coping ...");
        String cmdStr = " cp " + fromFilePath + " " + toFilePath;
        // System.out.println(cmdStr);
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
    }

    // copy file
    public static void copyFolder(String fromFilePath, String toFilePath) throws InterruptedException, IOException {
        // System.out.println("==> Start coping ...");
        String cmdStr = " cp -r " + fromFilePath + " " + toFilePath;
        // System.out.println(cmdStr);
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        // System.out.println("<== Complete Copy");
    }

    // copy file
    public static void moveFile(String fromFilePath, String toFilePath) throws InterruptedException, IOException {
        // System.out.println("==> Start coping ...");
        String cmdStr = " mv " + fromFilePath + " " + toFilePath;
        // System.out.println(cmdStr);
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        // System.out.println("<== Complete Copy");
    }
}
