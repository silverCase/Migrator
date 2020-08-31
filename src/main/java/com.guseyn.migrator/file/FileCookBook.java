package com.guseyn.migrator.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class FileCookBook {
    public static String contentFromResources(String path) throws IOException {
        try (
            InputStream inputStream = FileCookBook.class.getResourceAsStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            return reader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static String content(String path) throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            return bufferedReader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static String createdFolder(String folderPath) throws InterruptedException, IOException {
        System.out.println("==> create folder : " + FileCookBook.class.getClassLoader().getResource(folderPath).getFile());
        String cmdStr = "mkdir -p " + folderPath;
        System.out.println(cmdStr);
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        System.out.println("<== folder is created");
        return folderPath;
    }
}
