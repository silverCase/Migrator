package migration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JavaClassStructure {

    // get list of class name for any input library depend on library sechman that
    // we already have
    public static ArrayList<String> getLibraryClasses(String libraryInfo) throws IOException {
        String[] LibraryInfos = libraryInfo.split(":");
        String DgroupId = LibraryInfos[0];
        String DartifactId = LibraryInfos[1];
        String Dversion = LibraryInfos[2];
        ArrayList<String> listOfClasses = new ArrayList<String>();
        String libraryPath = "librariesClasses/" + DartifactId + "-" + Dversion + ".jar.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(libraryPath)));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.contains(" class ") || line.startsWith("class ") || line.contains(" interface ")
                || line.startsWith("interface ")) {
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

    // get list of packges name for any input library depend on library sechman that
    // we already have
    public static ArrayList<String> getLibraryPackages(String libraryInfo) throws IOException {
        ArrayList<String> listOfPackages = new ArrayList<String>();
        String[] LibraryInfos = libraryInfo.split(":");
        String DgroupId = LibraryInfos[0];
        String DartifactId = LibraryInfos[1];
        String Dversion = LibraryInfos[2];
        String libraryPath = "librariesClasses/" + DartifactId + "-" + Dversion + ".jar.txt";
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
    public ArrayList<String> getStaticMethods(String libraryInfo) {
        ArrayList<String> listOfStaticMethods = new ArrayList<String>();
        ArrayList<ClassObj> listOfClassesObj = getLibraryClassesObj(libraryInfo);
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
    // TODO: continue
    public ArrayList<ClassObj> getLibraryClassesObj(String libraryInfo) {
        String[] LibraryInfos = libraryInfo.split(":");
        String DgroupId = LibraryInfos[0];
        String DartifactId = LibraryInfos[1];
        String Dversion = LibraryInfos[2];
        ArrayList<ClassObj> listOfClassesObj = new ArrayList<ClassObj>();
        String libraryPath = "librariesClasses/" + DartifactId + "-" + Dversion + ".jar.txt";
        ClassObj classObj = null;
        boolean readingMethods = false;// when this flag is active that mean the line is method
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(libraryPath)));
            String line;
            String searchFor = "Compiled from ";
            String className;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("}") && readingMethods) {
                    readingMethods = false; // complete reading methods for this class
                    listOfClassesObj.add(classObj);
                    classObj = null;
                }
                if (readingMethods == true) {
                    if (classObj != null) {
                        classObj.addMethod(line);
                    }
                }
                // new class to process
                if (line.contains("class ") || line.contains("interface ")) {
                    classObj = new ClassObj();
                    classObj.setClassName(line);
                    // listOfClasses.add(className);
                    readingMethods = true;
                }

            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return listOfClassesObj;
    }

    public class ClassObj {
        public String className;
        public ArrayList<MethodObj> classMethods;
        public String scope;
    }

    public void classObjWithClassName(ClassObj classObj, String lineCode) {
        lineCode = lineCode.trim();
        int classNameIndex = lineCode.indexOf("class");
        int interfaceNameIndex = lineCode.indexOf("interface");
        if (classNameIndex == -1 && interfaceNameIndex == -1) {
            System.err.println("line does not have class or interface: " + lineCode);
            return;
        }
        String className;
        // define class
        if (classNameIndex >= 0) {
            className = lineCode.substring(classNameIndex + 5).trim();
            if (className.contains("implements")) {
                className = className.substring(0, className.indexOf("implements")).trim();
            }
            if (className.contains("extends")) {
                className = className.substring(0, className.indexOf("extends")).trim();
            }
            if (classNameIndex > 0) {
                classObj.scope = lineCode.substring(0, classNameIndex).trim();
            }
        } else {
            className = lineCode.substring(interfaceNameIndex + 9).trim();
            if (interfaceNameIndex > 0) {
                classObj.scope = lineCode.substring(0, interfaceNameIndex).trim();
            }
        }

        // remove open class bracket if it found
        int indexOfLastOpenBracket = className.lastIndexOf("{");
        if (indexOfLastOpenBracket > 0) {
            className = className.substring(0, indexOfLastOpenBracket).trim();
        }

        classObj.className = className;
    }

    public class MethodObj implements Comparable<MethodObj> {
        public String returnType;
        public String methodName;
        public ArrayList<String> inputParam;
        public String scope;
        public String fullMethodName;
        public String packageName;
        public Double frequency;

        @Override
        public int compareTo(final MethodObj o) {
            return (int) (o.frequency - this.frequency);
        }
    }
}
