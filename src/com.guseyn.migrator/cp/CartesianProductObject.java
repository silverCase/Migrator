package cp;

public class CartesianProductObject {
    public String firstLibraryName;
    public String secondLibraryName;
    public int frequency;
    public Double accuracy;
    public boolean isMappingCorrect;
    public boolean isCleaned;// save if we applied filter or not on this relation
    public String fromLibVersion;
    public String toLibVersion;

    public CartesianProductObject(final String firstLibraryName, final String secondLibraryName) {
        this.firstLibraryName = firstLibraryName;
        this.secondLibraryName = secondLibraryName;
        this.frequency = 1;
        this.accuracy = 0.0;
        this.isCleaned = false;
        this.isMappingCorrect = false;
    }
}
