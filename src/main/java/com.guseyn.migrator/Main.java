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
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> gitRepos = FileUtils.readLines(new File(args[0]));
        for (int i = 0; i < gitRepos.size(); i++) {
            String[] gitReposSplit = gitRepos.get(i).split("/");
            String RepoName = gitReposSplit[gitReposSplit.length-1];
            String outputFile = "out/" + RepoName + ".json";
            File file = new File("clone/commits.txt");
            file.createNewFile();
            CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData.main(new String[]{gitRepos.get(i)});
            //String[] newArgs = new String[] {gitRepos.get(i), outputFile};
            FindingMigrationRules.main(args);
            // DetectingCodeSegmentsByMigrationRules.main(args);
            FileUtils.forceDelete(new File("clone/commits.txt"));
            Gson gson = new Gson();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            List lst = new ArrayList();
            lst.add(MemoryStorage.migrationRules);
            lst.add(MemoryStorage.repoLibraries.get(0).repoLink);
            writer.write(gson.toJson(lst));
            writer.close();
            System.out.println("done");
        };
    }
}
