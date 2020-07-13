package git;

import java.util.ArrayList;

public class Commit {
    public String commitID;
    public String developerName;
    public String commitDate;
    public String commitText;
    public ArrayList<CommitFiles> filePath = new ArrayList<CommitFiles>();

    public Commit(String commitID, String developerName, String commitDate, String commitText, ArrayList<CommitFiles> filePath) {
        this.commitID = commitID;
        this.developerName = developerName;
        this.commitDate = commitDate;
        this.commitText = commitText;
        this.filePath.addAll(filePath);
    }
}
