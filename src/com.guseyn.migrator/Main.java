import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import stages.CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData;
import stages.FindingMigrationRules;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Files.createDirectory(Paths.get("resources/clone"));
        Files.createFile(Paths.get("resources/clone/commits.txt"));
        CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData.main(args);
        FindingMigrationRules.main(args);
        // DetectingCodeSegmentsByMigrationRules.main(args);
        FileUtils.deleteDirectory(new File("resources/clone"));
        System.out.println("done");
    }
}
