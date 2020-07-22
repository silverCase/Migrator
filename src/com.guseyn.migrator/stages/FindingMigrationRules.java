package stages;

import cp.CartesianProduct;
import cp.CartesianProductObject;
import git.GitHub;
import java.util.ArrayList;
import java.util.List;
import storage.MemoryStorage;
import storage.MigrationRule;
import storage.RepoLibrary;

public class FindingMigrationRules {
    // TODO: This code cannot find migration in last commit
    public static void main(String[] args) {
        System.out.println("step 2 started");
        List<RepoLibrary> listOfAllRepoLibraries = MemoryStorage.repoLibraries;
        List<RepoLibrary> listOfAddedRepoLibraries = new ArrayList<>();
        List<RepoLibrary> listOfRemovedLibraries = new ArrayList<>();
        List<CartesianProductObject> repoLibrariesAsCartesianProduct = new ArrayList<>();
        String oldCommitId = "";
        String oldPomPath = "";
        String newCommitId = "";
        String oldRepoLink = "";
        int totalNumberOfRepoLibraries = listOfAllRepoLibraries.size();
        int counterForProcessingLibraries = 0;
        System.out.println("***** Start searching for Cartesian Products *****");
        for (RepoLibrary repoLibrary: listOfAllRepoLibraries) {
            counterForProcessingLibraries++;
            newCommitId = repoLibrary.commitId;
            if (!repoLibrary.repoLink.equals(oldRepoLink)) {
                oldRepoLink = repoLibrary.repoLink;
                listOfAddedRepoLibraries.clear();
                listOfRemovedLibraries.clear();
                oldCommitId = "";
                oldPomPath = "";
            }
            if (!oldPomPath.equals(repoLibrary.pomPath)) {
                listOfAddedRepoLibraries.clear();
                listOfRemovedLibraries.clear();
                oldCommitId = "";
                oldPomPath = repoLibrary.pomPath;
            }
            if (!oldCommitId.equals(repoLibrary.commitId)) {
                if (listOfAddedRepoLibraries.size() > 0 && listOfRemovedLibraries.size() > 0) {
                    System.out.println("(" + counterForProcessingLibraries + "-" + totalNumberOfRepoLibraries + ")--> Find CP between:" + repoLibrary.commitId + "<==>" + oldCommitId);
                    if (!GitHub.areTwoCommitsSequential(oldCommitId, newCommitId)) {
                        System.err.println("==>This CartesianProductObject is ignored because of incorrect commit order in between");
                        return;
                    } else {
                        repoLibrariesAsCartesianProduct = CartesianProduct.repoLibrariesAsCartesianProduct(repoLibrariesAsCartesianProduct, listOfAddedRepoLibraries, listOfRemovedLibraries);
                    }
                }
                oldCommitId = repoLibrary.commitId;
                listOfAddedRepoLibraries.clear();
                listOfRemovedLibraries.clear();
            }
            if (repoLibrary.isAdded) {
                listOfAddedRepoLibraries.add(repoLibrary);
            } else {
                listOfRemovedLibraries.add(repoLibrary);
            }
        }
        if (listOfAddedRepoLibraries.size() > 0 && listOfRemovedLibraries.size() > 0) {
            if (!GitHub.areTwoCommitsSequential(oldCommitId, newCommitId)) {
                System.err.println("==>This CartesianProductObject is ignored because of incorrect commit order in between");
                return;
            } else {
                repoLibrariesAsCartesianProduct = CartesianProduct.repoLibrariesAsCartesianProduct(repoLibrariesAsCartesianProduct, listOfAddedRepoLibraries, listOfRemovedLibraries);
            }
        }
        System.out.println("***** Filtering Cartesian Product *****");
        List<CartesianProductObject> filteredCartesianProductObjects = CartesianProduct.filteredListOfCartesianProductObjects(repoLibrariesAsCartesianProduct);

        Double requiredThreshold = 1.0;
        int requiredFrequency = 1;
        int numberOfValidMigrationRules = 0;
        int numberOfValidUpgradeRules = 0;
        int numberOfFalseRules = 0;

        StringBuilder upgrades = new StringBuilder();
        StringBuilder migrations = new StringBuilder();

        for (CartesianProductObject cartesianProductObject: filteredCartesianProductObjects) {
            if (cartesianProductObject.accuracy >= requiredThreshold && cartesianProductObject.frequency >= requiredFrequency) {
                String[] firstLibraryNameParts = cartesianProductObject.firstLibraryName.split(":");
                String[] secondLibraryNameParts = cartesianProductObject.secondLibraryName.split(":");
                if (!firstLibraryNameParts[0].equals(secondLibraryNameParts[1])) {
                    migrations.append(cartesianProductObject.firstLibraryName)
                        .append(" <======> ").append(cartesianProductObject.secondLibraryName)
                        .append("\t| frequency:")
                        .append(cartesianProductObject.frequency)
                        .append("| Ratio:")
                        .append(cartesianProductObject.accuracy * 100)
                        .append("%|");
                    migrations.append("\n");
                    numberOfValidMigrationRules++;
                    MemoryStorage.migrationRules.add(
                        new MigrationRule(
                            cartesianProductObject.firstLibraryName,
                            cartesianProductObject.secondLibraryName,
                            cartesianProductObject.frequency,
                            cartesianProductObject.accuracy
                        )
                    );
                } else {
                    upgrades.append(cartesianProductObject.firstLibraryName)
                        .append(" <======> ")
                        .append(cartesianProductObject.secondLibraryName)
                        .append("\t| frequency:")
                        .append(cartesianProductObject.frequency)
                        .append("| Ratio:")
                        .append(cartesianProductObject.accuracy * 100)
                        .append("%|");
                    upgrades.append("\n");
                    numberOfValidUpgradeRules++;
                    // TODO: return back ????
                    MemoryStorage.migrationRules.add(
                        new MigrationRule(
                            cartesianProductObject.firstLibraryName,
                            cartesianProductObject.secondLibraryName,
                            cartesianProductObject.frequency,
                            cartesianProductObject.accuracy
                        )
                    );
                }
            } else {
                numberOfFalseRules++;
            }
        }
        System.out.println("\n**************************************");
        System.out.println("************* Migrations **************");
        System.out.println("**************************************\n");
        System.out.println(migrations.toString());

        System.out.println("\n**************************************");
        System.out.println("************* upgrades **************");
        System.out.println("**************************************\n");
        System.out.println(upgrades.toString());

        System.out.println("\n**************************************");
        System.out.println("************* Summary Report **************");
        System.out.println("**************************************");
        System.out.println("With threshold: " + requiredThreshold + "\nTotal Migrations: " + filteredCartesianProductObjects.size()
            + "\nValid Rules:" + (numberOfValidMigrationRules + numberOfValidUpgradeRules) + " (Migration:"
            + numberOfValidMigrationRules + ", Upgrade:" + numberOfValidMigrationRules + ")\nFalse Rules:"
            + numberOfFalseRules);
        System.out.println("**************************************\n");
        System.out.println("step 2 finished");
    }
}
