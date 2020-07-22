package storage;

public class MigrationSegment {
    public String repoLink;
    public String commitId;
    public String fromCode;
    public String toCode;
    public String fileName;
    public String fromLibVersion;
    public String toLibVersion;

    public MigrationSegment(final String repoLink,
                            final String commitId,
                            final String fromCode,
                            final String toCode,
                            final String fileName,
                            final String fromLibVersion,
                            final String toLibVersion) {
        this.repoLink = repoLink;
        this.commitId = commitId;
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.fileName = fileName;
        this.fromLibVersion = fromLibVersion;
        this.toLibVersion = toLibVersion;
    }
}
