package storage;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorage {
    public static List<String> gitRepoLinks = new ArrayList<>();
    public static List<Repository> repositories = new ArrayList<>();
    public static List<RepoCommit> repoCommits = new ArrayList<>();
    public static List<RepoLibrary> repoLibraries = new ArrayList<>();
}
