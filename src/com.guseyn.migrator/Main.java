import csv.CsvWithGitRepoLinks;
import git.Commit;
import git.CommitFiles;
import git.GitHub;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import storage.MemoryStorage;
import storage.RepoCommits;
import storage.Repository;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> listOfGitRepoLinks =  CsvWithGitRepoLinks.listOfLinks();
        List<String> allLinks = MemoryStorage.gitRepoLinks;
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
            HashMap<String, String> previousVersionsOfLibraries = new HashMap<String, String>();
            for (Commit commit: listOfCommits) {
                MemoryStorage.repoCommits.add(new RepoCommits(gitRepoLink, commit.commitID, commit.commitDate, commit.developerName, commit.commitText));
                if (commit.commitFiles.size() == 0) {
                    continue;
                }
                String[] commitIDParts = commit.commitID.split("_");
                String commitID = commitIDParts[1];
                commitID = GitHub.checkedoutCommit(clonedRepo, commitID);
                for (CommitFiles pomPath : commit.commitFiles) {
                    // make sure all commits has init library
                    previousVersionsOfLibraries.putIfAbsent(pomPath.firstFile, "");
                    String prevCommitLibraries = generateRepoLibraries(clonedRepo,pomPath.firstFile, gitRepoLink,
                        commit.commitID, previousVersionsOfLibraries.get(pomPath.firstFile));
                    previousVersionsOfLibraries.put(pomPath.firstFile, prevCommitLibraries);
                    System.out.println(pomPath.firstFile + "==>" + previousVersionsOfLibraries);
                }
            }
        }
    }

    private static String generateRepoLibraries(String clonedRepo, String pomPath, String repoLink, String commitID, String previousVersionLibraries) {
        /*try {
            String versionLibraries;
            String projectPath = clonedRepo + "/" + pomPath;
            versionLibraries = listOfJavaProjectLibrary(projectPath);
            ArrayList<String> listOfRemovedLibrary = listOfChangedLibrary(previousVersionLibraries, versionLibraries);
            if (listOfRemovedLibrary.size() > 0) {
                for (String libraryName : listOfRemovedLibrary) {
                    System.err.println("Removed:" + libraryName);
                    new ProjectLibrariesDB().addProjectLibrary(projectID, commitID, libraryName, Operation.removed,
                        pomPath);
                }
            }
            ArrayList<String> listOfAddedLibrary = listOfChangedLibrary(versionLibraries, previousVersionLibraries);
            if (listOfAddedLibrary.size() > 0) {
                for (String libraryName : listOfAddedLibrary) {
                    System.err.println("Added:" + libraryName);
                    new ProjectLibrariesDB().addProjectLibrary(projectID, commitID, libraryName, Operation.added,
                        pomPath);
                }
            }

            previousVersionLibraries = versionLibraries;
        } catch (Exception e) {
            // do something
        }*/
        return previousVersionLibraries;
    }

    private static ArrayList<String> listOfChangedLibrary(String previousVersionLibraries, String versionLibraries) {
        ArrayList<String> listOfLibrary = new ArrayList<String>();
        if (previousVersionLibraries.length() == 0 || versionLibraries.length() == 0) {
            // get all library that added in first commit
            if (previousVersionLibraries.length() != 0 && versionLibraries.length() == 0) {
                String[] addedLibraries = previousVersionLibraries.split(",");
                listOfLibrary.addAll(Arrays.asList(addedLibraries));
            }
            return listOfLibrary;
        }
        if (!previousVersionLibraries.equals(versionLibraries)) {
            String[] prev = previousVersionLibraries.split(",");
            String[] current = versionLibraries.split(",");
            for (String prevLibrary : prev) {
                if (prevLibrary.length() == 0) {
                    continue;
                }
                boolean isFound = false;
                for (String currentLibrary : current) {
                    if (prevLibrary.equals(currentLibrary)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    listOfLibrary.add(prevLibrary);
                }
            }
        }
        return listOfLibrary;
    }
}
