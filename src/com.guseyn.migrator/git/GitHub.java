package git;

import com.guseyn.broken_xml.ParsedXML;
import file.File;
import java.io.IOException;

public class GitHub {
    public static String repoNameByRepoLink(String repoLink) {
        String[] repoLinkParts = repoLink.split("/");
        if (repoLinkParts.length < 5) {
            System.err.println("Cannot get repo name from  (" + repoLink + ") ");
            return "";
        }
        String appName = repoLinkParts[repoLinkParts.length - 1];
        if (appName.endsWith(".git")) {
            appName = appName.substring(0, appName.length() - 4);
        }
        return appName;
    }

    public static String clonedRepo(String pathToClone, String linkToClone) throws IOException, InterruptedException {
        System.out.println("==> Start cloning: " + linkToClone);
        String gitToken = new ParsedXML(File.content("resources/git-creds.xml")).document().roots().get(0).texts().get(0).value();
        String[] partsOfLinkToClone = linkToClone.split("//");
        if (partsOfLinkToClone.length < 2) {
            throw new IllegalArgumentException("git url is not correct");
        }
        String linkToCloneWithCreds = partsOfLinkToClone[0] + "//" + gitToken + "@" + partsOfLinkToClone[1];
        String cmdStr = "cd " + pathToClone + " && git clone " + linkToCloneWithCreds;
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        System.out.println("<== Complete clone");
        return pathToClone;
    }

    public static String generatedFileWithCommitLogs(String pathToClone, String repoName) throws IOException, InterruptedException {
        final String logFileName = "commits.txt";
        System.out.println("==> Start generate logs for: " + logFileName);
        String cmdStr = "cd " + pathToClone + "/" + repoName + " && git log --name-status  --reverse >../" + logFileName;
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        p.waitFor();
        System.out.println("<== Complete generate logs");
        return logFileName;
    }
}
