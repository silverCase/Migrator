package stages;

import cp.CartesianProduct;
import file.File;
import git.GitHub;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import migration.Migration;
import migration.MigrationSegment;
import storage.MemoryStorage;
import storage.MigrationRule;
import storage.RepoCommit;
import storage.RepoLibrary;

public class DetectingCodeSegmentsByMigrationRules {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("step 3 started");
        final String logFileNameWithCommits = "commits.txt";
        final String pathWithClonedRepos = Paths.get(".").toAbsolutePath().normalize().toString() + "/clone/";
        final String pathWhereJavaLibsShouldBeStored = "/librariesClasses/jar";
        File.createdFolder(pathWhereJavaLibsShouldBeStored);
        System.out.println("*****Loading all projects libraries (will take some time) *****");
        List<MigrationRule> migrationRules = Migration.migrationRulesWithoutVersion(0);
        List<RepoLibrary> repoLibraries = MemoryStorage.repoLibraries;
        List<RepoLibrary> listOfAddedLibraries = new ArrayList<>();
        List<RepoLibrary> listOfRemovedLibraries = new ArrayList<>();
        List<RepoLibrary> listOfLibrariesForTheCurrentCommit = new ArrayList<>();
        String oldCommitId = "";
        String newCommitId = "";
        String oldPomPath = "";
        String currentRepoLink = "";
        // *********************************************************************
        // 1- Search for migration using library changes in pom file
        // *********************************************************************
        for (MigrationRule migrationRule: migrationRules) {
            System.out.println("==> Start search for migration rule " + migrationRule.fromLibraryName + "<==> " + migrationRule.toLibraryName);
            MigratedLibrary.id = migrationRule.id;
            String fromValidLibrary = null;
            String toValidLibrary = null;
            List<MigrationSegment> listOfSegments = new ArrayList<>();
            for (RepoLibrary library: repoLibraries) {
                newCommitId = library.commitId;
                if (!library.repoLink.equals(currentRepoLink)) {
                    currentRepoLink = library.repoLink;
                    listOfAddedLibraries.clear();
                    listOfRemovedLibraries.clear();
                    listOfLibrariesForTheCurrentCommit.clear();
                    oldCommitId = "";
                    newCommitId = "";
                    oldPomPath = "";
                }
                if (!oldPomPath.equals(library.pomPath)) {
                    listOfAddedLibraries.clear();
                    listOfRemovedLibraries.clear();
                    listOfLibrariesForTheCurrentCommit.clear();
                    oldCommitId = "";
                    oldPomPath = library.pomPath;
                }
                if (!oldCommitId.equals(newCommitId)) {
                    if (listOfAddedLibraries.size() > 0 || listOfRemovedLibraries.size() > 0) {
                        if (GitHub.areTwoCommitsSequential(oldCommitId, newCommitId)) {
                            System.err.println("==>This process ignored because of incorrect order of commits in between " + oldCommitId + "==> " + newCommitId);
                            return;
                        } else {
                            String foundPreviousLibraryName = CartesianProduct.foundLibraryNameInListOfLibraries(listOfRemovedLibraries, migrationRule.toLibraryName);
                            if (foundPreviousLibraryName.length() > 0) {
                                continue;
                            }
                            String foundToLibrary = CartesianProduct.foundLibraryNameInListOfLibraries(listOfAddedLibraries, ":" + migrationRule.toLibraryName);
                            String foundFromLibrary = CartesianProduct.foundLibraryNameInListOfLibraries(listOfRemovedLibraries, ":" + migrationRule.fromLibraryName);
                            String foundFromLibraryButItStillIsInCurreentListOfLibraries = CartesianProduct.foundLibraryNameInListOfLibraries(listOfLibrariesForTheCurrentCommit, ":" + migrationRule.fromLibraryName);
                            if (foundToLibrary.length() > 0 && foundFromLibrary.length() == 0) {
                                foundFromLibrary = foundFromLibraryButItStillIsInCurreentListOfLibraries;
                            }
                            if (foundToLibrary.length() == 0 && foundFromLibrary.length() > 0) {
                                foundToLibrary = CartesianProduct.foundLibraryNameInListOfLibraries(listOfLibrariesForTheCurrentCommit, ":" + migrationRule.toLibraryName);
                            }
                            if (foundFromLibrary.length() > 0 && foundToLibrary.length() > 0) {
                                MigratedLibrary.fromLibrary = foundFromLibrary;
                                MigratedLibrary.toLibrary = foundToLibrary;
                                final String finalOldCommitId = oldCommitId;
                                String[] oldCommitIdParts = oldCommitId.split("_");
                                int prevCommitNumber = Integer.parseInt(oldCommitIdParts[0].substring(1)) - 1;
                                Optional<RepoCommit> previousRepoCommitOptional = MemoryStorage.repoCommits.stream().filter(repoCommit -> repoCommit.repoLink.equals(library.repoLink) && repoCommit.commitId.contains(
                                    "v" + finalOldCommitId)).findFirst();
                                if (previousRepoCommitOptional.isPresent()) {
                                    String previousCommitId = previousRepoCommitOptional.get().commitId;
                                    System.out.println("-----------------\n" + currentRepoLink
                                        + "- Found migration\nCommit from :" + previousCommitId + "==> "
                                        + oldCommitId + "\nLibrary from: " + MigratedLibrary.fromLibrary + "==> "
                                        + MigratedLibrary.toLibrary + "\nAppLink: ");
                                    List<MigrationSegment> listOfBlocks = new ArrayList<>();

                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("step 3 finished");
    }

    public static class MigratedLibrary {
        public static String fromLibrary;
        public static String toLibrary;
        public static String id;
    }
}
