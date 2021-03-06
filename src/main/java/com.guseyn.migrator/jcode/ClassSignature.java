package com.guseyn.migrator.jcode;

import java.io.File;
import java.nio.file.Paths;

public class ClassSignature {
	static String libraryPath = Paths.get(".").toAbsolutePath().normalize().toString() + "/librariesClasses/jar";

	public static void main(String[] args) {
		File folder = new File(libraryPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {

				new ClassSignature().buildTFfiles(listOfFiles[i].getName());
			}
		}
	}

	void buildTFfiles(String libraryName) {
		try {
			String cmdStr = "jar -tf " + libraryPath + "/" + libraryName + ">>" + libraryPath + "/tfs/" + libraryName + ".txt";
			System.out.println("Run command " + cmdStr);
			Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmdStr });
			p.waitFor();
			System.out.println("Process completed: ");

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
