package com.guseyn.migrator.jcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class DownloadLibrary {
	// TODO need to update when machine change
	public String libraryPath;

	public DownloadLibrary(String pathToSaveJar) {
		this.libraryPath = Paths.get(".").toAbsolutePath().normalize().toString() + pathToSaveJar;
	}

	public boolean isLibraryFound(String LibraryInfo) {
		String[] libraryInfoParts = LibraryInfo.split(":");
		if (libraryInfoParts.length < 3) {
			return false;
		}
		// String DgroupId=LibraryInfos[0];
		String artifactId = libraryInfoParts[1];
		String version = libraryInfoParts[2];
		String jarFilePath = libraryPath + "/" + artifactId + "-" + version + ".jar";
		// System.out.println(jarFilePath);
		File tmpDir = new File(jarFilePath);
		return tmpDir.exists();
	}

	// Delete files that donseot have signatures
	public void isLibraryValidToGenerateSignatures(String LibraryName) throws IOException, InterruptedException {
		boolean isFound = false;
		String tfFilePath = libraryPath + "/tfs/" + LibraryName.replace(".jar", ".jar.txt");
		String jarFilePath = libraryPath + "/" + LibraryName;
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
				generateFunctionSignature(LibraryName, libraryPath + "/../");
			}
			br.close();
		}
	}

	public boolean deleteFile(String path) {
		File tmpLibDir = new File(path);
		if (tmpLibDir.exists()) {
			return tmpLibDir.delete();
		}
		return false;
	}

	// Generate function Signature from code
	public void generateFunctionSignature(String libraryName, String pathToSaveLibrarySignature) throws InterruptedException, IOException {
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
		// TODO: remove jar library and tfs files
	}

	// Download Library from URL
	public void download(String libraryInfo, boolean isDocs) throws InterruptedException, IOException {
		// we already have the library
		if (isLibraryFound(libraryInfo)) {
			System.out.println(" Good! library signature (" + libraryInfo + ") already there donot need to download");
			return;
		}
		String[] libraryInfoParts = libraryInfo.split(":");
		if (libraryInfoParts.length < 3) {
			System.err.println(" Error in library name (" + libraryInfo + ")");
			return;
		}
		String groupId = libraryInfoParts[0];
		String artifactId = libraryInfoParts[1];
		String version = libraryInfoParts[2];

		String cmdStr="cd "+ libraryPath + " && curl -L -O http://search.maven.org/remotecontent?filepath=" + groupId.replace(".", "/")
		+ "/" + artifactId + "/" + version+ "/" + artifactId + "-" + version + (isDocs?"-javadoc":"") + ".jar";
		// String cmdStr = "cd " + libraryPath + " &&  curl -L -O http://central.maven.org/maven2/"
		// 		+ DgroupId.replace(".", "/") + "/" + DartifactId + "/" + Dversion + "/" + DartifactId + "-" + Dversion
		// 		+ (isDocs ? "-javadoc" : "") + ".jar";
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
	public void buildTFfiles(String libraryInfo) throws IOException, InterruptedException {
		String[] libraryInfoParts = libraryInfo.split(":");
		if (libraryInfoParts.length < 3) {
			System.err.println(" Error in library name (" + libraryInfo + ")");
			return;
		}
		// String DgroupId=LibraryInfos[0];
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
		isLibraryValidToGenerateSignatures(libraryName);
	}
}
