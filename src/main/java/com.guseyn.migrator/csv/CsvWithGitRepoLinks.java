package com.guseyn.migrator.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvWithGitRepoLinks {
    public static List<String> listOfLinks() throws IOException {
        ArrayList<String> listOfGitRepositories = new ArrayList<String>();
        String currentLine;
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/git-repositories.csv")))) {
            while ((currentLine = bufferedReader.readLine()) != null) {
                listOfGitRepositories.add(currentLine.trim());
            }
        }
        return listOfGitRepositories;
    }
}
