package com.guseyn.migrator.jcode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClassStructure {
	/*
	 * to find library signature first unzip library generate class schema by run
	 * this commands in library folder javap * ../libraryName.txt List jar classes
	 * jar -tf picasso-2.5.2-sources.jar | grep '.java'
	 */
	public ClassStructure() {
	}

	// get list of class name for any input library depend on library sechman that
	// we already have
	public List<String> libraryClasses(String libraryInfo) throws IOException {
		String[] libraryInfoParts = libraryInfo.split(":");
		String groupId = libraryInfoParts[0];
		String artifactId = libraryInfoParts[1];
		String version = libraryInfoParts[2];
		ArrayList<String> listOfClasses = new ArrayList<String>();
		String libraryPath = "librariesClasses/" + artifactId + "-" + version + ".jar.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(libraryPath)));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.contains(" class ") || line.startsWith("class ") || line.contains(" interface ") || line.startsWith("interface ")) {
				String searchFor = line.contains("class") ? "class" : "interface";
				String className;
				String[] classInfo = line.split(searchFor);
				String[] packgeWithClass = classInfo[1].trim().split(" ");
				String[] packageInfo = packgeWithClass[0].split("\\.");
				className = packageInfo[packageInfo.length - 1];

				if (className.isEmpty()) {
					continue;
				}

				if (!listOfClasses.contains(className)) {
					// System.out.println(className);
					listOfClasses.add(className);
				}
			}
		}
		return listOfClasses;
	}

	// get list of packages name for any input library depend on library sechman that
	// we already have
	public List<String> libraryPackages(String libraryInfo) throws IOException {
		ArrayList<String> listOfPackages = new ArrayList<String>();
		String[] libraryInfoParts = libraryInfo.split(":");
		String groupId = libraryInfoParts[0];
		String artifactId = libraryInfoParts[1];
		String version = libraryInfoParts[2];
		String libraryPath = "librariesClasses/" + artifactId + "-" + version + ".jar.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(libraryPath)));
		String line;

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.contains(" class ") || line.startsWith("class ") || line.contains(" interface ")
				|| line.startsWith("interface ")) {
				String searchFor = line.contains("class") ? "class" : "interface";
				String packageName = "";
				String[] classInfo = line.split(searchFor);
				String[] packageWithClass = classInfo[1].trim().split(" ");
				String[] packageInfo = packageWithClass[0].split("\\.");
				for (int i = 0; i < packageInfo.length - 1; i++) {
					String folder = packageInfo[i];
					if (packageName.isEmpty()) {
						packageName = folder;
					} else {
						packageName += "." + folder;
					}
				}
				if (packageName.isEmpty()) {
					continue;
				}

				if (!listOfPackages.contains(packageName)) {
					listOfPackages.add(packageName);
				}
			}
		}
		return listOfPackages;
	}

	// This function return list of static methods for direct call
	public List<String> staticMethods(String libraryInfo) throws IOException {
		List<String> listOfStaticMethods = new ArrayList<>();
		List<ClassObj> listOfClassesObj = libraryClassesObjects(libraryInfo);
		for (ClassObj classObj : listOfClassesObj) {
			for (MethodObj methodObj : classObj.classMethods) {
				if (methodObj.scope.trim().contains(" static")) {

					listOfStaticMethods.add(methodObj.methodName);
				}
			}

		}
		return listOfStaticMethods;
	}

	/*
	 * This function return list of objects with all classes and methods that been
	 * // in the library
	 */
	public List<ClassObj> libraryClassesObjects(String libraryInfo) throws IOException {
		String[] libraryInfoParts = libraryInfo.split(":");
		String groupId = libraryInfoParts[0];
		String artifactId = libraryInfoParts[1];
		String version = libraryInfoParts[2];
		ArrayList<ClassObj> listOfClassesObj = new ArrayList<ClassObj>();
		String libraryPath = "librariesClasses/" + artifactId + "-" + version + ".jar.txt";
		ClassObj classObj = null;
		boolean readingMethods = false;// when this flag is active that mean the line is method
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(libraryPath)));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.trim().equals("}") && readingMethods) {
				readingMethods = false; // complete reading methods for this class
				listOfClassesObj.add(classObj);
				classObj = null;
			}
			if (readingMethods) {
				classObj.addMethod(line);
			}
			// new class to process
			if (line.contains("class ") || line.contains("interface ")) {
				classObj = new ClassObj();
				classObj.setClassName(line);
				readingMethods = true;
			}
		}
		return listOfClassesObj;
	}

}
