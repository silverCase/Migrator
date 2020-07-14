package storage;

public class RepoCommits {
    public String repoLink;
    public String commitId;
    public String commitDate;
    public String developerName;
    public String commitText;

    public RepoCommits(final String repoLink, final String commitId, final String commitDate,
                       final String developerName, final String commitText) {
        this.repoLink = repoLink;
        this.commitId = commitId;
        this.commitDate = commitDate;
        this.developerName = developerName;
        this.commitText = commitText;
    }
}
