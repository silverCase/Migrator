package com.guseyn.migrator;

import com.google.gson.Gson;
import com.guseyn.migrator.stages.CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData;
import com.guseyn.migrator.stages.FindingMigrationRules;
import com.guseyn.migrator.storage.MemoryStorage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String gitRepo = args[0];
        String outputFile = args[1];
        Files.createDirectory(Paths.get("clone"));
        Files.createFile(Paths.get("clone/commits.txt"));
        CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData.main(new String[]{gitRepo});
        FindingMigrationRules.main(args);
        // DetectingCodeSegmentsByMigrationRules.main(args);
        FileUtils.deleteDirectory(new File("clone"));
        Gson gson = new Gson();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(gson.toJson(MemoryStorage.migrationRules));
        writer.close();
        System.out.println("done");
    }
}
