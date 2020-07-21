import java.io.IOException;
import org.jdom2.JDOMException;
import stages.CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData;
import stages.FindingMigrationRules;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData.main(args);
        FindingMigrationRules.main(args);
        System.out.println("done");
    }
}
