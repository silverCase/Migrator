package migration;

import java.util.ArrayList;
import java.util.List;

public class MigrationSegment implements Comparable<MigrationSegment> {
    public List<String> blockCode;
    public int frequency; // home many times we did see this fragment
    public double similarityDegree; // how similar two code are
    public List<String> addedCode;
    public List<String> removedCode;
    public int isVaildMapping; // if the mapping is valid or not (0 or 1)
    public String fileName; // name of the file that has change
    public String fromLibraryVersion;
    public String toLibraryVersion;
    public String migrationRuleId;
    public String repoLink;
    public String commitId;

    public MigrationSegment() {
        this.blockCode = new ArrayList<>();
        this.frequency = 1;
        this.addedCode = new ArrayList<>();
        this.removedCode = new ArrayList<>();
    }

    public MigrationSegment(String removedCode, String addedCode, double similarityDegree) {
        this.blockCode = new ArrayList<>();
        this.addedCode = new ArrayList<>();
        this.removedCode = new ArrayList<>();
        this.similarityDegree = similarityDegree;
        this.addedCode.add(addedCode);
        this.removedCode.add(removedCode);
    }

    public MigrationSegment(storage.MigrationSegment migrationSegment) {
        this.blockCode = new ArrayList<>();
        this.frequency = 1;
        this.addedCode = new ArrayList<>();
        this.removedCode = new ArrayList<>();
        this.repoLink = migrationSegment.repoLink;
        this.commitId = migrationSegment.commitId;
        this.fromLibraryVersion = migrationSegment.fromLibraryVersion;
        this.toLibraryVersion = migrationSegment.toLibraryVersion;
        this.fileName = migrationSegment.fileName;
    }

    public MigrationSegment(String fromLibVersion, String toLibVersion) {
        this.blockCode = new ArrayList<>();
        this.frequency = 1;
        this.addedCode = new ArrayList<>();
        this.removedCode = new ArrayList<>();
        this.fromLibraryVersion =  fromLibVersion;
        this.toLibraryVersion = toLibVersion;
    }

    public MigrationSegment(ArrayList<String> blockCode, ArrayList<String> addedCode, ArrayList<String> removedCode) {
        this.blockCode = blockCode;
        this.removedCode = removedCode;
        this.addedCode = addedCode;
        this.frequency = 1;
    }

    @Override
    public int compareTo(final MigrationSegment o) {
        int segmentSize = this.addedCode.size() * this.removedCode.size() - o.addedCode.size() * o.removedCode.size();
        if (segmentSize == 0) {
            segmentSize = o.frequency - this.frequency;
        }
        return segmentSize;
    }
}
