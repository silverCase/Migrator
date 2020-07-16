package cp;

import java.util.ArrayList;
import java.util.List;
import storage.RepoLibrary;

public class CartesianProduct {
    public List<CartesianProductObject> repoLibrariesAsCartesianProduct(List<RepoLibrary> firstListOfRepoLibs, List<RepoLibrary> secondListOfRepoLibs) {
        final List<CartesianProductObject> repoLibrariesAsCartesianProduct;
        List<RepoLibrary> secondListOfRepoLibsTmp = new ArrayList<>(secondListOfRepoLibs);
        List<RepoLibrary> firstListOfRepoLibsTmp = new ArrayList<>(firstListOfRepoLibs);
        for (RepoLibrary secondRepoLibrary: secondListOfRepoLibsTmp) {
            for (RepoLibrary firstRepoLibrary: firstListOfRepoLibsTmp) {
                // continue;
            }
        }
        return null;
    }

    public static String libraryWithoutVersion(String librarName) {

        String[] AppInfo = librarName.split(":");

        return AppInfo[0] + ":" + AppInfo[1] + ":xxx";
    }

    public boolean isUpgradeProcess(String libraryName1, String libraryName2) {

        String[] librarName1sp = libraryName1.split(":");
        String[] librarName2sp = libraryName2.split(":");
        if (librarName1sp[0].trim().startsWith(librarName2sp[0].trim())
            || librarName2sp[0].trim().startsWith(librarName1sp[0].trim())
            || librarName1sp[1].trim().startsWith(librarName2sp[1].trim())
            || librarName2sp[1].trim().startsWith(librarName1sp[1].trim())) {
            return true;
        }

        return false;
    }
}
