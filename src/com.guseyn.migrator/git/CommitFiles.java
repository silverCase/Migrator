package git;

public class CommitFiles {
    public String fileOperation;
    public String firstFile;
    public String secondFile;

    public CommitFiles(String fileOperation, String firstFile, String secondFile) {
        this.fileOperation = fileOperation;
        this.firstFile = firstFile;
        this.secondFile = secondFile;
    }
}
