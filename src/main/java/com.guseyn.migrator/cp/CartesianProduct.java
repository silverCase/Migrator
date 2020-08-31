package com.guseyn.migrator.cp;

import java.util.ArrayList;
import java.util.List;
import storage.RepoLibrary;

public class CartesianProduct {
    public static List<CartesianProductObject> repoLibrariesAsCartesianProduct(String commitId, List<CartesianProductObject> repoLibrariesAsCartesianProductObjects, List<RepoLibrary> firstListOfRepoLibs, List<RepoLibrary> secondListOfRepoLibs) {
        if (firstListOfRepoLibs.size() > 100 || secondListOfRepoLibs.size() > 100) {
            return repoLibrariesAsCartesianProductObjects;
        }
        List<RepoLibrary> secondListOfRepoLibsTmp = new ArrayList<>(secondListOfRepoLibs);
        List<RepoLibrary> firstListOfRepoLibsTmp = new ArrayList<>(firstListOfRepoLibs);
        for (RepoLibrary secondRepoLibrary: secondListOfRepoLibs) {
            for (RepoLibrary firstRepoLibrary: firstListOfRepoLibs) {
                if (isUpgradeProcess(firstRepoLibrary.libraryName, secondRepoLibrary.libraryName)) {
                    firstListOfRepoLibsTmp.remove(firstRepoLibrary);
                    secondListOfRepoLibsTmp.remove(secondRepoLibrary);
                }
            }
        }
        for (RepoLibrary secondRepoLibrary: secondListOfRepoLibsTmp) {
            for (RepoLibrary firstRepoLibrary: firstListOfRepoLibsTmp) {
                firstRepoLibrary.libraryName = libraryNameWithoutVersion(firstRepoLibrary.libraryName);
                secondRepoLibrary.libraryName = libraryNameWithoutVersion(secondRepoLibrary.libraryName);
                CartesianProductObject cartesianProduct = new CartesianProductObject(commitId, firstRepoLibrary.libraryName, secondRepoLibrary.libraryName);
                int cartesianProductIndex = isFoundCartesianProductObjectUnique(repoLibrariesAsCartesianProductObjects, firstRepoLibrary.libraryName, secondRepoLibrary.libraryName);
                if (cartesianProductIndex != -1) {
                    repoLibrariesAsCartesianProductObjects.get(cartesianProductIndex).frequency += 1;
                } else {
                    repoLibrariesAsCartesianProductObjects.add(cartesianProduct);
                }
            }
        }
        return repoLibrariesAsCartesianProductObjects;
    }

    private static String libraryNameWithoutVersion(String libraryName) {
        String[] libraryNameParts = libraryName.split(":");
        if (libraryNameParts.length < 2) {
            return libraryNameParts[0] + ":xxx";
        }
        return libraryNameParts[0] + ":" + libraryNameParts[1] + ":xxx";
    }

    private static boolean isUpgradeProcess(String firstLibraryName, String secondLibraryName) {
        String[] firstLibraryNameParts = firstLibraryName.split(":");
        String[] secondLibraryNameParts = secondLibraryName.split(":");
        if (secondLibraryNameParts.length < 2 || firstLibraryNameParts.length < 2) {
            return false;
        }
        return firstLibraryNameParts[0].trim().startsWith(secondLibraryNameParts[0].trim())
            || secondLibraryNameParts[0].trim().startsWith(firstLibraryNameParts[0].trim())
            || firstLibraryNameParts[1].trim().startsWith(secondLibraryNameParts[1].trim())
            || secondLibraryNameParts[1].trim().startsWith(firstLibraryNameParts[1].trim());
    }

    private static int isFoundCartesianProductObjectUnique(List<CartesianProductObject> cartesianProductObjects, String firstLibraryName, String secondLibraryName) {
        int foundIndex = -1;
        for (int i = 0; i < cartesianProductObjects.size(); i++) {
            CartesianProductObject cartesianProductObject = cartesianProductObjects.get(i);
            if ((cartesianProductObject.firstLibraryName.equals(firstLibraryName) && cartesianProductObject.secondLibraryName.equals(secondLibraryName))) {
                foundIndex = i;
                break;
            }
        }

        return foundIndex;
    }

    public static List<CartesianProductObject> filteredListOfCartesianProductObjects(List<CartesianProductObject> cartesianProductObjects) {

        for (int i = 0; i < cartesianProductObjects.size(); i++) {
            CartesianProductObject cpObject1 = cartesianProductObjects.get(i);
            int maxFrequency = 0;
            // find the max show times
            for (CartesianProductObject cpObject2 : cartesianProductObjects) {
                if (!cpObject2.isCleaned) {
                    if (cpObject1.firstLibraryName.equals(cpObject2.firstLibraryName) /* || cpObject1.value1.equals(cpObject2.value2) */) {
                        if (cpObject2.frequency > maxFrequency) {
                            maxFrequency = cpObject2.frequency;
                        }
                    }
                }
            }
            // divide the max show times on other value
            for (CartesianProductObject cpObject2 : cartesianProductObjects) {
                if (!cpObject2.isCleaned) {
                    if (cpObject1.firstLibraryName.equals(cpObject2.firstLibraryName) /* || cpObject1.value1.equals(cpObject2.value2) */) {
                        // We use 1.0 to convert double to int
                        cpObject2.accuracy = cpObject2.frequency * 1.0 / maxFrequency
                            * 1.0;
                        cpObject2.isCleaned = true;
                    }
                }
            }
        }

        return cartesianProductObjects;
    }

    public static String foundLibraryNameInListOfLibraries(List<RepoLibrary> libraries, String libraryName) {
        for (RepoLibrary library : libraries) {
            if (library.libraryName.contains(libraryName)) {
                return library.libraryName;
            }
        }
        return "";
    }
}
