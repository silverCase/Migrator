import csv.CsvWithGitRepoLinks;
import git.GitHub;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import storage.MemoryStorage;
import storage.Repository;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> listOfGitRepoLinks =  CsvWithGitRepoLinks.listOfLinks();
        List<String> allLinks = MemoryStorage.gitRepoLinks;
        for (String gitRepoLink: listOfGitRepoLinks) {
            Optional<Repository> repoId = MemoryStorage.repositories.stream().filter(repo -> gitRepoLink.equals(repo.link)).findFirst();
            if (repoId.isPresent()) {
                continue;
            }
            String repoName = GitHub.repoNameByRepoLink(gitRepoLink);
            if (repoName.isEmpty()) {
                continue;
            }
            String pathWhereRepoShouldBeCloned = Paths.get(".").toAbsolutePath().normalize().toString() + "/clone/";
            String clonedRepo = GitHub.clonedRepo(pathWhereRepoShouldBeCloned, gitRepoLink);
            String fileNameWithLogs = GitHub.generatedFileWithCommitLogs(pathWhereRepoShouldBeCloned, repoName);
        }
    }
}
