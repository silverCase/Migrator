package storage;

public class MigrationRule {
    public String firstLibraryName;
    public String secondLibraryName;
    public int frequency;
    public double accuracy;
    public int isValid;

    public MigrationRule(final String firstLibraryName, final String secondLibraryName,
                         final int frequency,
                         final double accuracy) {
        this.firstLibraryName = firstLibraryName;
        this.secondLibraryName = secondLibraryName;
        this.frequency = frequency;
        this.accuracy = accuracy;
        this.isValid = 0;
    }
}
