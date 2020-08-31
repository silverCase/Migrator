package com.guseyn.migrator.storage;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorage {
    public static List<String> gitRepoLinks = new ArrayList<>();
    public static List<Repository> repositories = new ArrayList<com.guseyn.migrator.storage.Repository>();
    public static List<RepoCommit> repoCommits = new ArrayList<com.guseyn.migrator.storage.RepoCommit>();
    public static List<storage.RepoLibrary> repoLibraries = new ArrayList<storage.RepoLibrary>();
    public static List<MigrationRule> migrationRules = new ArrayList<>();
    public static List<storage.MigrationSegment> migrationSegments = new ArrayList<storage.MigrationSegment>();
}
