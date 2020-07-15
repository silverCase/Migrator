package stages;

import csv.CsvWithGitRepoLinks;
import git.Commit;
import git.CommitFiles;
import git.GitHub;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.jdom2.JDOMException;
import pom.Pom;
import storage.MemoryStorage;
import storage.RepoCommit;
import storage.Repository;

public class CollectingGitCommitsThenFillingUpRepositoriesAndRepoCommitsAndRepoLibrariesInMemoryStorageWithData {
    public static void main(String[] args) throws IOException, InterruptedException, JDOMException {
        List<String> listOfGitRepoLinks =  CsvWithGitRepoLinks.listOfLinks();
        List<String> allLinks = MemoryStorage.gitRepoLinks; // ???
        for (String gitRepoLink: listOfGitRepoLinks) {
            Optional<Repository> repository = MemoryStorage.repositories.stream().filter(repo -> gitRepoLink.equals(repo.link)).findFirst();
            if (repository.isPresent()) {
                continue;
            }
            String repoName = GitHub.repoNameByRepoLink(gitRepoLink);
            if (repoName.isEmpty()) {
                continue;
            }
            String pathWhereRepoShouldBeCloned = Paths.get(".").toAbsolutePath().normalize().toString() + "/clone/";
            String clonedRepo = GitHub.clonedRepo(pathWhereRepoShouldBeCloned, gitRepoLink) + repoName;
            String fileNameWithLogs = GitHub.generatedFileWithCommitLogs(pathWhereRepoShouldBeCloned, repoName);
            List<Commit> listOfCommits = GitHub.commitsFromLogFile(pathWhereRepoShouldBeCloned);
            MemoryStorage.repositories.add(new Repository(gitRepoLink));
            HashMap<String, String> previousVersionsOfLibraries = new HashMap<>();
            for (Commit commit: listOfCommits) {
                MemoryStorage.repoCommits.add(new RepoCommit(gitRepoLink, commit.commitID, commit.commitDate, commit.developerName, commit.commitText));
                if (commit.commitFiles.size() == 0) {
                    continue;
                }
                String[] commitIDParts = commit.commitID.split("_");
                String commitID = commitIDParts[1];
                commitID = GitHub.checkedoutCommit(clonedRepo, commitID);
                for (CommitFiles pomPath : commit.commitFiles) {
                    // make sure all commits has init library
                    previousVersionsOfLibraries.putIfAbsent(pomPath.firstFile, "");
                    String prevCommitLibraries = Pom.generatedRepoLibraries(clonedRepo,pomPath.firstFile, gitRepoLink,
                        commit.commitID, previousVersionsOfLibraries.get(pomPath.firstFile));
                    previousVersionsOfLibraries.put(pomPath.firstFile, prevCommitLibraries);
                    System.out.println(pomPath.firstFile + "==>" + previousVersionsOfLibraries);
                }
            }
            System.out.println("ok");
        }
    }
}
