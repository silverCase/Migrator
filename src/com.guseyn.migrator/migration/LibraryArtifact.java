package migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class LibraryArtifact {
    public static boolean doesLibraryExist(String libraryPath, String libraryInfo) {
        boolean isFound;
        String[] libraryInfoParts = libraryInfo.split(":");
        if (libraryInfoParts.length < 3) {
            return false;
        }
        String artifactId = libraryInfoParts[1];
        String version = libraryInfoParts[2];
        String jarFilePath = libraryPath + "/" + artifactId + "-" + version + ".jar";
        File tmpDir = new File(jarFilePath);
        isFound = tmpDir.exists();
        return isFound;
    }

    // Delete files that do not have signatures
    public static boolean isLibraryWithValidSignature(String libraryPath, String libraryName) throws IOException, InterruptedException {
        boolean isFound;
        String tfFilePath = libraryPath + "/tfs/" + libraryName.replace(".jar", ".jar.txt");
        String jarFilePath = libraryPath + "/" + libraryName;
        File tmpDir = new File(jarFilePath);
        isFound = tmpDir.exists();
        if (isFound) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tfFilePath)));
            String line;
            boolean hasCode = false;
            if (br.readLine() != null) {
                hasCode = true;
            }
            br.close();
            if (!hasCode) {
                deleteFile(jarFilePath);
                deleteFile(tfFilePath);
            } else {
                generateFunctionSignature(libraryName, libraryPath + "/../");
            }
            br.close();
            return true;
        }
        return false;
    }

    public static void deleteFile(String path) {
        File tmpLibDir = new File(path);
        if (tmpLibDir.exists()) {
            tmpLibDir.delete();
        }
    }

    // Generate function Signature from code
    public static void generateFunctionSignature(String libraryName, String pathToSaveLibrarySignature) throws InterruptedException, IOException {
        // Delete file if it exists
        deleteFile(pathToSaveLibrarySignature + libraryName + ".txt");
        System.out.println("==> generate function Signature for " + libraryName);
        String cmdStr = "cd " + pathToSaveLibrarySignature + " && javap -classpath jar/" + libraryName
            + " $(jar -tf jar/" + libraryName + " | grep \"class$\" | sed s/\\.class$//) >>" + libraryName
            + ".txt";
        System.out.println("\nStart generate Function Signature " + cmdStr + " library......");
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        if(!p.waitFor(5, TimeUnit.MINUTES)) {
            //timeout - kill the process.
            p.destroy();
        }
        System.out.println("<== Process completed: ");
    }

    // Download Library from URL
    public static void download(String libraryPath, String libraryInfo, boolean isDocs) throws InterruptedException, IOException {
        // we already have the library
        if (doesLibraryExist(libraryPath, libraryInfo)) {
            System.out.println(" Good! library signature (" + libraryInfo + ") already there donot need to download");
            return;
        }
        String[] LibraryInfoParts = libraryInfo.split(":");
        if (LibraryInfoParts.length < 3) {
            System.err.println(" Error in library name (" + libraryInfo + ")");
            return;
        }
        String groupId = LibraryInfoParts[0];
        String artifactId = LibraryInfoParts[1];
        String version = LibraryInfoParts[2];

        String cmdStr = "cd " + libraryPath + " && curl -L -O http://search.maven.org/remotecontent?filepath="+ groupId.replace(".", "/")
            + "/"+ artifactId +"/"+ version+ "/"+ artifactId + "-" +
            version + (isDocs?"-javadoc":"") + ".jar";

        System.out.println(cmdStr);
        System.out.println("==> Start Download " + artifactId + " library......");
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        if(!p.waitFor(5, TimeUnit.MINUTES)) {
            //timeout - kill the process.
            p.destroy();
        }
        System.out.println("<== Download completed: ");
    }

    // Convert java.jar to source code so we could get signatures
    public static void buildTFfiles(String libraryPath, String libraryInfo) throws InterruptedException, IOException {
        String[] libraryInfoParts = libraryInfo.split(":");
        if (libraryInfoParts.length < 3) {
            System.err.println(" Error in library name (" + libraryInfo + ")");
            return;
        }
        // String groupId = libraryInfoParts[0];
        String artifactId = libraryInfoParts[1];
        String version = libraryInfoParts[2];
        String libraryName = artifactId + "-" + version + ".jar";
        String cmdStr = "jar -tf " + libraryPath + "/" + libraryName + ">>" + libraryPath + "/tfs/" + libraryName + ".txt";
        System.out.println("==> generate tfs....");
        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
        if(!p.waitFor(5, TimeUnit.MINUTES)) {
            //timeout - kill the process.
            p.destroy();
        }
        System.out.println("<== Process completed: ");
        isLibraryWithValidSignature(libraryPath, libraryName);
    }
}
