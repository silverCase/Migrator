import java.io.IOException;
import stages.CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData;
import stages.DetectingCodeSegmentsByMigrationRules;
import stages.FindingMigrationRules;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData.main(args);
        FindingMigrationRules.main(args);
        DetectingCodeSegmentsByMigrationRules.main(args);
        System.out.println("done");
    }
}
