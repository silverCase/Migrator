package storage;

import java.util.UUID;

public class MigrationRule {
    public String id;
    public String commitId;
    public String fromLibraryName;
    public String toLibraryName;
    public int frequency;
    public double accuracy;
    public int isValid;

    public MigrationRule(
        final String commitId,
        final String fromLibraryName,
        final String toLibraryName,
        final int frequency,
        final double accuracy
    ) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.commitId = commitId.split("_")[1];
        this.fromLibraryName = fromLibraryName;
        this.toLibraryName = toLibraryName;
        this.frequency = frequency;
        this.accuracy = accuracy;
        this.isValid = 0;
    }
}
