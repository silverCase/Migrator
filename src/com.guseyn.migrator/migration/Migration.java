package migration;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import stages.DetectingCodeSegmentsByMigrationRules;
import storage.MemoryStorage;
import storage.MigrationRule;

public class Migration {
    static String pathClone = Paths.get(".").toAbsolutePath().normalize().toString() + "/Clone/Process/";

    public static List<MigrationRule> migrationRulesWithoutVersion(int isValid) {
        List<MigrationRule> migrationRules = MemoryStorage.migrationRules.stream().filter(migrationRule -> migrationRule.isValid == isValid).sorted(
            Comparator.comparingInt(migrationRule -> migrationRule.frequency)).collect(
            Collectors.toList());
        Collections.reverse(migrationRules);
        return migrationRules;
    }

    public static List<MigrationSegment> segmentListFromCloningProcess(String repoLink, String previousCommitName, String migrateAtCommitName) {

        List<MigrationSegment> segmentList = new ArrayList<>();
        // list of changed files
        List<String> listOfChangedFiles = cloneMigratedCommits(repoLink, previousCommitName, migrateAtCommitName);
        if (listOfChangedFiles.size() > 0) {
            String outputDiffsPath = pathClone + "../Diffs/" + DetectingCodeSegmentsByMigrationRules.MigratedLibrary.id + "/" + migrateAtCommitName + "/";
            // list of changed cleaned files
            ArrayList<String> diffsFilePath = generateFragments(listOfChangedFiles, previousCommitName,
                migrateAtCommitName, outputDiffsPath);

            if (diffsFilePath.size() > 0) {
                // clean java code from the data
                segmentList = cleanJavaCode.getListOfCleanedFiles(outputDiffsPath, diffsFilePath);
            }

        }
        return segmentList;
    }

    // This method responsible for clone two commits that has migration to find file
    // changes between them
    public static  List<String> cloneMigratedCommits(String repoLink, String previousCommitName, String migrateAtCommitName) {
        List<String> listOfChangedFiles = new ArrayList<String>();

        // Download The library Jar signatures
        DownloadLibrary downloadLibrary = new DownloadLibrary(pathToSaveJAVALibrary);

        downloadLibrary.download(MigratedLibraries.fromLibrary, false);
        downloadLibrary.buildTFfiles(MigratedLibraries.fromLibrary);

        downloadLibrary.download(MigratedLibraries.toLibrary, false);
        downloadLibrary.buildTFfiles(MigratedLibraries.toLibrary);

        if (downloadLibrary.isLibraryFound(MigratedLibraries.fromLibrary) == false
            || downloadLibrary.isLibraryFound(MigratedLibraries.toLibrary) == false) {

            System.err.println(
                "Cannot download either " + MigratedLibraries.fromLibrary + " or " + MigratedLibraries.toLibrary);
            return listOfChangedFiles;
        }

        // Clone App if isnot cloned already
        GitHubOP gitHubOP = new GitHubOP(appURL, pathClone);
        if (gitHubOP.isAppExist(gitHubOP.appFolder) == false) {

            gitHubOP.deleteFolder(pathClone); // clear folder from previous project process
            terminalCommand.createFolder(pathClone);
            gitHubOP.cloneApp();
            gitHubOP.generateLogs(LOG_FILE_NAME);
        }
        // get list of change files
        listOfChangedFiles = gitHubOP.getlistOfChangedFiles(migrateAtCommitName);
        if (listOfChangedFiles.size() == 0) {
            System.out.println("\t==>Cannot find any changes in Java files in this commit");
            return listOfChangedFiles;
        }
        // clone first commit
        if (gitHubOP.isAppExist(previousCommitName) == false) {
            gitHubOP.copyApp(previousCommitName);
            String firstCommitID = GitHubOP.getCommitID(previousCommitName);
            gitHubOP.gitCheckout(previousCommitName, firstCommitID);
        }

        // clone second commit
        if (gitHubOP.isAppExist(migrateAtCommitName) == false) {
            gitHubOP.copyApp(migrateAtCommitName);
            String secondCommitID = GitHubOP.getCommitID(migrateAtCommitName);
            gitHubOP.gitCheckout(migrateAtCommitName, secondCommitID);
        }

        return listOfChangedFiles;

    }

    // This function will generate fragments of code changes
    public static ArrayList<String> generateFragments(List<String> listOfChangedFiles, String previousCommitName,
                                        String migrateAtCommitName, String outputDiffsPath) {

        // list of diffs files path
        ArrayList<String> diffsFilePath = new ArrayList<String>();
        boolean isDiffFolderCreated = false;
        // generate diffs
        String outputDiffFilePath = "";
        for (String changedFilePath : listOfChangedFiles) {
            String changedFilePathSplit[] = changedFilePath.split("/");
            String chnagedFileName = changedFilePathSplit[changedFilePathSplit.length - 1];
            System.out.print("Detect change in " + changedFilePath);
            String newUpdatedFilePath = pathClone + migrateAtCommitName + "/" + changedFilePath;
            String oldFilePath = pathClone + previousCommitName + "/" + changedFilePath;
            outputDiffFilePath = outputDiffsPath + "diff_" + chnagedFileName + ".txt";
            // Make sure the file has call from old function before update and call from new
            // library after update
            if (cleanJavaCode.isUsedNewLibrary(oldFilePath, MigratedLibraries.fromLibrary)
                && cleanJavaCode.isUsedNewLibrary(newUpdatedFilePath, MigratedLibraries.toLibrary)) {
                // create folder only for one time
                if (isDiffFolderCreated == false) {
                    isDiffFolderCreated = true;
                    terminalCommand.createFolder(outputDiffsPath);
                }
                terminalCommand.createDiffs(oldFilePath, newUpdatedFilePath, outputDiffFilePath);
                // Copy real file before and after migration
                terminalCommand.copyFile(oldFilePath, outputDiffFilePath.replace(".txt", "_before.java"));
                terminalCommand.copyFile(newUpdatedFilePath, outputDiffFilePath.replace(".txt", "_after.java"));

                diffsFilePath.add(outputDiffFilePath);

            } else {
                System.err.print("|ignored because old or new file NOT used libraries functions \n");
            }
        }

        return diffsFilePath;

    }
}
