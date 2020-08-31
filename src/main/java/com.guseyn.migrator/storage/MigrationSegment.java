package storage;

public class MigrationSegment {
    public String repoLink;
    public String commitId;
    public String fromCode;
    public String toCode;
    public String fileName;
    public String fromLibraryVersion;
    public String toLibraryVersion;

    public MigrationSegment(final String repoLink,
                            final String commitId,
                            final String fromCode,
                            final String toCode,
                            final String fileName,
                            final String fromLibraryVersion,
                            final String toLibraryVersion) {
        this.repoLink = repoLink;
        this.commitId = commitId;
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.fileName = fileName;
        this.fromLibraryVersion = fromLibraryVersion;
        this.toLibraryVersion = toLibraryVersion;
    }
}
