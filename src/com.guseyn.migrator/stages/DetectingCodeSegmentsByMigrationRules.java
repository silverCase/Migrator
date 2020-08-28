package stages;

import cp.CartesianProduct;
import file.File;
import git.GitHub;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import migration.Migration;
import migration.MigrationSegment;
import storage.MemoryStorage;
import storage.MigrationRule;
import storage.RepoCommit;
import storage.RepoLibrary;
import storage.Repository;

public class DetectingCodeSegmentsByMigrationRules {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("step 3 started");
        final String pathWhereJavaLibsShouldBeStored = "/librariesClasses/jar";
        File.createdFolder(pathWhereJavaLibsShouldBeStored);
        System.out.println("*****Loading all projects libraries (will take some time) *****");
        List<MigrationRule> migrationRules = Migration.migrationRulesWithoutVersion(0);
        List<RepoLibrary> repoLibraries = MemoryStorage.repoLibraries;
        List<RepoLibrary> listOfAddedLibraries = new ArrayList<>();
        List<RepoLibrary> listOfRemovedLibraries = new ArrayList<>();
        List<RepoLibrary> listOfLibrariesForTheCurrentCommit = new ArrayList<>();
        String oldCommitId = "";
        String newCommitId = "";
        String oldPomPath = "";
        String currentRepoLink = "";
        // *********************************************************************
        // 1- Search for migration using library changes in pom file
        // *********************************************************************
        for (MigrationRule migrationRule: migrationRules) {
            System.out.println("==> Start search for migration rule " + migrationRule.fromLibraryName + "<==> " + migrationRule.toLibraryName);
            MigratedLibrary.id = migrationRule.id;
            String fromValidLibrary = null;
            String toValidLibrary = null;
            List<MigrationSegment> listOfSegments = new ArrayList<>();
            for (RepoLibrary library: repoLibraries) {
                newCommitId = library.commitId;
                if (!library.repoLink.equals(currentRepoLink)) {
                    currentRepoLink = library.repoLink;
                    listOfAddedLibraries.clear();
                    listOfRemovedLibraries.clear();
                    listOfLibrariesForTheCurrentCommit.clear();
                    oldPomPath = "";
                }
                if (oldPomPath.isEmpty()) {
                    oldCommitId = "";
                    oldPomPath = library.pomPath;
                }
                if (!oldCommitId.equals(newCommitId)) {
                    if (listOfAddedLibraries.size() > 0 || listOfRemovedLibraries.size() > 0) {
                        if (!GitHub.areTwoCommitsSequential(oldCommitId, newCommitId)) {
                            System.err.println("==>This process ignored because of incorrect order of commits in between " + oldCommitId + "==> " + newCommitId);
                            continue;
                        } else {
                            String foundPreviousLibraryName = CartesianProduct.foundLibraryNameInListOfLibraries(listOfRemovedLibraries, migrationRule.toLibraryName);
                            if (foundPreviousLibraryName.length() > 0) {
                                continue;
                            }
                            String foundToLibrary = CartesianProduct.foundLibraryNameInListOfLibraries(listOfAddedLibraries, ":" + migrationRule.toLibraryName);
                            String foundFromLibrary = CartesianProduct.foundLibraryNameInListOfLibraries(listOfRemovedLibraries, ":" + migrationRule.fromLibraryName);
                            String foundFromLibraryButItStillIsInCurrentListOfLibraries = CartesianProduct.foundLibraryNameInListOfLibraries(listOfLibrariesForTheCurrentCommit, ":" + migrationRule.fromLibraryName);
                            if (foundToLibrary.length() > 0 && foundFromLibrary.length() == 0) {
                                foundFromLibrary = foundFromLibraryButItStillIsInCurrentListOfLibraries;
                            }
                            if (foundToLibrary.length() == 0 && foundFromLibrary.length() > 0) {
                                foundToLibrary = CartesianProduct.foundLibraryNameInListOfLibraries(listOfLibrariesForTheCurrentCommit, ":" + migrationRule.toLibraryName);
                            }
                            if (foundFromLibrary.length() > 0 && foundToLibrary.length() > 0) {
                                MigratedLibrary.fromLibrary = foundFromLibrary;
                                MigratedLibrary.toLibrary = foundToLibrary;
                                String[] oldCommitIdParts = oldCommitId.split("_");
                                int prevCommitNumber = Integer.parseInt(oldCommitIdParts[0].substring(1)) - 1;
                                Optional<RepoCommit> previousRepoCommitOptional = MemoryStorage.repoCommits.stream().filter(repoCommit -> repoCommit.repoLink.equals(library.repoLink) && repoCommit.commitId.contains(
                                    "v" + prevCommitNumber)).findFirst();
                                if (previousRepoCommitOptional.isPresent()) {
                                    String previousCommitId = previousRepoCommitOptional.get().commitId;
                                    System.out.println("-----------------\n" + currentRepoLink
                                        + "- Found migration\nCommit from :" + previousCommitId + "==> "
                                        + oldCommitId + "\nLibrary from: " + MigratedLibrary.fromLibrary + "==> "
                                        + MigratedLibrary.toLibrary + "\nAppLink: ");
                                    List<MigrationSegment> listOfBlocks = Migration.startCloning(currentRepoLink, previousCommitId, oldCommitId);
                                    if (listOfBlocks.size() > 0) {
                                        listOfSegments.addAll(listOfBlocks);
                                        fromValidLibrary = foundFromLibrary;
                                        toValidLibrary = foundToLibrary;
                                        System.out.println("==> Start saving all founded segments to database");
                                        for (MigrationSegment segment: listOfSegments) {
                                            MemoryStorage.migrationSegments.add(new storage.MigrationSegment(
                                                segment.repoLink,
                                                segment.commitId,
                                                arrayListAsString(segment.removedCode),
                                                arrayListAsString(segment.addedCode),
                                                segment.fileName,
                                                segment.fromLibraryVersion,
                                                segment.toLibraryVersion
                                            ));
                                        }
                                        System.out.println("<== complete saving all founded segments to database");
                                    } else {
                                        System.err.println("Cannot find previous commit before :" + oldCommitId);
                                    }
                                }
                            }
                        }
                    }
                    listOfLibrariesForTheCurrentCommit.addAll(listOfAddedLibraries);
                    listOfAddedLibraries.clear();
                    for (RepoLibrary removedLibrary: listOfRemovedLibraries) {
                        for (int i = 0; i < listOfLibrariesForTheCurrentCommit.size(); i++) {
                            if (listOfLibrariesForTheCurrentCommit.get(i).libraryName.equals(removedLibrary.libraryName)) {
                                listOfLibrariesForTheCurrentCommit.remove(i);
                                break;
                            }
                        }
                    }
                    listOfRemovedLibraries.clear();
                    oldCommitId = library.commitId;
                }
                if (library.isAdded) {
                    listOfAddedLibraries.add(library);
                } else {
                    listOfRemovedLibraries.add(library);
                }
            }
            List<MigrationSegment> listOfBlocks = selfAdmittedMigration(
                fromValidLibrary,
                toValidLibrary,
                migrationRule.fromLibraryName,
                migrationRule.toLibraryName
            );
            if (listOfBlocks.size() > 0) {
                listOfSegments.addAll(listOfBlocks);
            }
            /*
             * After find all migration segments start apply Algorithm Run CP and
             * Substitution Algorithm on cleaned files
             */
            int isValidMigration = 0;
            if (listOfSegments.size() > 0) {
                isValidMigration = 1;
            } else {
                isValidMigration = 2;
                System.out.println("Did not find any migration segments for this rule");
            }
            Optional<MigrationRule> migrationRuleFromStorageWithId = MemoryStorage.migrationRules.stream().filter(rule -> rule.id.equals(migrationRule.id)).findFirst();
            if (migrationRuleFromStorageWithId.isPresent()) {
                migrationRuleFromStorageWithId.get().isValid = isValidMigration;
            }
        }
        System.out.println("step 3 finished");
    }

    // *********************************************************************
    // 2- Search for migration that defined using commit text
    // *********************************************************************

    private static List<MigrationSegment> selfAdmittedMigration(
        String fromValidLibrary,
        String toValidLibrary,
        String fromLibraryName,
        String toLibraryName
    ) throws IOException, InterruptedException {
        List<MigrationSegment> segmentList = new ArrayList<>();

        if (fromValidLibrary == null || toValidLibrary == null) {
            System.err.println(
                "Either fromLibrary=" + fromValidLibrary + "  or toLibrary=" + toValidLibrary + " is not valid");
            return segmentList;
        }

        List<RepoCommit> listOfRepoCommits = MemoryStorage.repoCommits.stream().filter(
            repoCommit -> {
                boolean libraryAlreadyIsInSegments;
                Optional<storage.MigrationSegment> segmentWithLibrary = MemoryStorage.migrationSegments
                    .stream()
                    .filter(migrationSegment -> migrationSegment.repoLink.equals(repoCommit.repoLink))
                    .findFirst();
                libraryAlreadyIsInSegments = segmentWithLibrary.isPresent();
                return repoCommit.commitText.contains(fromLibraryName) &&
                    repoCommit.commitText.contains(toLibraryName) &&
                    libraryAlreadyIsInSegments;
            }
        ).collect(Collectors.toList());


        System.out.println("Start searching for self-admitted migration between " + fromValidLibrary + "==>"
            + toValidLibrary + " by the developer");
        MigratedLibrary.toLibrary = toValidLibrary;
        MigratedLibrary.fromLibrary = fromValidLibrary;

        for (RepoCommit repoCommit : listOfRepoCommits) {
            Optional<Repository> repoCurrentRepoCommitOptional = MemoryStorage.repositories.stream().filter(repository -> repository.link.equals(repoCommit.repoLink)).findFirst();
            String[] oldCommitIdParts = repoCommit.commitId.split("_");
            int prevCommitNumber = Integer.parseInt(oldCommitIdParts[0].substring(1)) - 1;
            Optional<RepoCommit> previousRepoCommitOptional = MemoryStorage.repoCommits.stream().filter(commit -> commit.repoLink.equals(repoCommit.repoLink) && commit.commitId.contains("v" + prevCommitNumber)).findFirst();
            if (repoCurrentRepoCommitOptional.isPresent() && previousRepoCommitOptional.isPresent()) {
                String prevCommitId = previousRepoCommitOptional.get().commitId;
                List<MigrationSegment> listOfBlocks = Migration.startCloning(repoCommit.repoLink, prevCommitId, repoCommit.commitId);
                if (listOfBlocks.size() > 0) {
                    segmentList.addAll(listOfBlocks);
                    System.out.println("==> Start saving all founded segments to database");
                    for (MigrationSegment segment: listOfBlocks) {
                        MemoryStorage.migrationSegments.add(
                            new storage.MigrationSegment(
                                segment.repoLink,
                                segment.commitId,
                                arrayListAsString(segment.removedCode),
                                arrayListAsString(segment.addedCode),
                                segment.fileName,
                                segment.fromLibraryVersion,
                                segment.toLibraryVersion
                            )
                        );
                    }
                    System.out.println("<== complete saving all founded segments in database");
                }
            }
        }
        return segmentList;
    }

    public static String arrayListAsString(List<String> listOfCode) {
        StringBuilder textCode = new StringBuilder();
        for (String lineCode : listOfCode) {
            textCode.append(lineCode).append('\n');
        }
        return textCode.toString();
    }

    public static class MigratedLibrary {
        public static String fromLibrary;
        public static String toLibrary;
        public static String id;
    }
}
