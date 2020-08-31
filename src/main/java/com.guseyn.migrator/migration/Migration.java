package com.guseyn.migrator.migration;

import com.guseyn.migrator.bash.BashCommand;
import com.guseyn.migrator.git.GitHub;
import com.guseyn.migrator.jcode.CleanJavaCode;
import com.guseyn.migrator.jcode.DownloadLibrary;
import com.guseyn.migrator.stages.DetectingCodeSegmentsByMigrationRules;
import com.guseyn.migrator.storage.MemoryStorage;
import com.guseyn.migrator.storage.MigrationRule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Migration {
    static String pathWhereRepoShouldBeCloned = Paths.get(".").toAbsolutePath().normalize().toString() + "/Clone/Process/";
    static String pathToSaveJAVALibrary = "/librariesClasses/jar";

    public static List<MigrationRule> migrationRulesWithoutVersion(int isValid) {
        List<MigrationRule> migrationRules = MemoryStorage.migrationRules.stream().filter(migrationRule -> migrationRule.isValid == isValid).sorted(
            Comparator.comparingInt(migrationRule -> migrationRule.frequency)).collect(
            Collectors.toList());
        Collections.reverse(migrationRules);
        return migrationRules;
    }

    // This function return list of cleaned segments
    public static List<MigrationSegment> startCloning(String repoLink, String previousCommitName, String migrateAtCommitName) throws IOException, InterruptedException {
        List<MigrationSegment> segmentList = new ArrayList<>();
        // list of changed files
        List<String> listOfChangedFiles = cloneMigratedCommits(repoLink, previousCommitName, migrateAtCommitName);
        if (listOfChangedFiles.size() > 0) {
            String outputDiffsPath = pathWhereRepoShouldBeCloned + "../Diffs/" + DetectingCodeSegmentsByMigrationRules.MigratedLibrary.id + "/" + migrateAtCommitName + "/";
            // list of changed cleaned files
            List<String> diffsFilePath = generateFragments(listOfChangedFiles, previousCommitName,
                migrateAtCommitName, outputDiffsPath);
            if (diffsFilePath.size() > 0) {
                // clean java code from the data
                segmentList =  new CleanJavaCode().listOfCleanedFiles(outputDiffsPath, diffsFilePath);
            }

        }
        return segmentList;
    }

    // This method responsible for clone two commits that has migration to find file
    // changes between them
    public static List<String> cloneMigratedCommits(String repoLink, String previousCommitName, String migrateAtCommitName) throws IOException, InterruptedException {
        List<String> listOfChangedFiles = new ArrayList<String>();

        // Download The library Jar signatures
        DownloadLibrary downloadLibrary = new DownloadLibrary(pathToSaveJAVALibrary);

        downloadLibrary.download(DetectingCodeSegmentsByMigrationRules.MigratedLibrary.fromLibrary, false);
        downloadLibrary.buildTFfiles(DetectingCodeSegmentsByMigrationRules.MigratedLibrary.fromLibrary);

        downloadLibrary.download(DetectingCodeSegmentsByMigrationRules.MigratedLibrary.toLibrary, false);
        downloadLibrary.buildTFfiles(DetectingCodeSegmentsByMigrationRules.MigratedLibrary.toLibrary);

        if (!downloadLibrary.isLibraryFound(DetectingCodeSegmentsByMigrationRules.MigratedLibrary.fromLibrary)
            || !downloadLibrary.isLibraryFound(DetectingCodeSegmentsByMigrationRules.MigratedLibrary.toLibrary)) {

            System.err.println("Cannot download either " + DetectingCodeSegmentsByMigrationRules.MigratedLibrary.fromLibrary + " or " + DetectingCodeSegmentsByMigrationRules.MigratedLibrary.toLibrary);
            return listOfChangedFiles;
        }

        // Clone Repo if has been not cloned yet
        String repoName = GitHub.repoNameByRepoLink(repoLink);
        if (!GitHub.doesRepoExist(repoName)) {

            GitHub.deleteFolder(pathWhereRepoShouldBeCloned); // clear folder from previous project process
            BashCommand.createFolder(pathWhereRepoShouldBeCloned);
            GitHub.clonedRepo(pathWhereRepoShouldBeCloned, repoLink);
            GitHub.generatedFileWithCommitLogs(pathWhereRepoShouldBeCloned, repoName);
        }
        // get list of change files
        listOfChangedFiles = GitHub.listOfChangedFiles(pathWhereRepoShouldBeCloned, migrateAtCommitName);
        if (listOfChangedFiles.size() == 0) {
            System.out.println("\t==>Cannot find any changes in Java files in this commit");
            return listOfChangedFiles;
        }
        // clone first commit
        if (!GitHub.doesRepoExist(previousCommitName)) {
            GitHub.copiedRepo(pathWhereRepoShouldBeCloned, repoName, previousCommitName);
            String firstCommitId = GitHub.commitId(previousCommitName);
            if (firstCommitId != null) {
                GitHub.checkedoutCommit(previousCommitName, firstCommitId);
            }
        }

        // clone second commit
        if (!GitHub.doesRepoExist(migrateAtCommitName)) {
            GitHub.copiedRepo(pathWhereRepoShouldBeCloned, repoName, migrateAtCommitName);
            String secondCommitId = GitHub.commitId(migrateAtCommitName);
            if (secondCommitId != null) {
                GitHub.checkedoutCommit(migrateAtCommitName, secondCommitId);
            }
        }

        return listOfChangedFiles;
    }

    // This function will generate fragments of code changes
    public static List<String> generateFragments(List<String> listOfChangedFiles, String previousCommitName,
                                   String migrateAtCommitName, String outputDiffsPath) throws IOException, InterruptedException {
        // list of diffs files path
        List<String> diffsFilePath = new ArrayList<>();
        boolean isDiffFolderCreated = false;
        // generate diffs
        String outputDiffFilePath;
        CleanJavaCode cleanJavaCode = new CleanJavaCode();
        for (String changedFilePath : listOfChangedFiles) {
            String[] changedFilePathParts = changedFilePath.split("/");
            String changedFileName = changedFilePathParts[changedFilePathParts.length - 1];
            System.out.print("Detect change in " + changedFilePath);
            String newUpdatedFilePath = pathWhereRepoShouldBeCloned + migrateAtCommitName + "/" + changedFilePath;
            String oldFilePath = pathWhereRepoShouldBeCloned + previousCommitName + "/" + changedFilePath;
            outputDiffFilePath = outputDiffsPath + "diff_" + changedFileName + ".txt";
            // Make sure the file has call from old function before update and call from new
            // library after update
            if (cleanJavaCode.isUsedNewLibrary(oldFilePath, DetectingCodeSegmentsByMigrationRules.MigratedLibrary.fromLibrary)
                && cleanJavaCode.isUsedNewLibrary(newUpdatedFilePath, DetectingCodeSegmentsByMigrationRules.MigratedLibrary.toLibrary)) {
                // create folder only for one time
                if (!isDiffFolderCreated) {
                    isDiffFolderCreated = true;
                    BashCommand.createFolder(outputDiffsPath);
                }
                BashCommand.writeDiffsToOutputFile(oldFilePath, newUpdatedFilePath, outputDiffFilePath);
                // Copy real file before and after migration
                BashCommand.copyFile(oldFilePath, outputDiffFilePath.replace(".txt", "_before.java"));
                BashCommand.copyFile(newUpdatedFilePath, outputDiffFilePath.replace(".txt", "_after.java"));

                diffsFilePath.add(outputDiffFilePath);

            } else {
                System.err.print("|ignored because old or new file NOT used libraries functions \n");
            }
        }
        return diffsFilePath;
    }
}
