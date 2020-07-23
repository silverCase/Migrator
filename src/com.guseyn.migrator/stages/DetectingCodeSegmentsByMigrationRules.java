package stages;

import file.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import migration.Migration;
import storage.MemoryStorage;
import storage.MigrationRule;
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
        List<RepoLibrary> listOfAddeedLibraries = new ArrayList<>();
        List<RepoLibrary> listOfRemovedLibraries = new ArrayList<>();
        List<RepoLibrary> listOfLibrariesForTheCurrentCommit = new ArrayList<>();
        String oldCommitId = "";
        String newCommitId = "";
        String oldPomPath = "";
        String repoLink = "";
        // *********************************************************************
        // 1- Search for migration using library changes in pom file
        // *********************************************************************
        for (MigrationRule migrationRule: migrationRules) {
            System.out.println("==> Start search for migration rule " + migrationRule.fromLibraryName + "<==> " + migrationRule.toLibraryName);
            MigratedLibrary.id = migrationRule.id;
            String fromValidLibrary = null;
            String toValidLibrary = null;
            // List<>
        }
        System.out.println("step 3 finished");
    }

    private static class MigratedLibrary {
        public static String fromLibrary;
        public static String toLibrary;
        public static String id;
    }
}
