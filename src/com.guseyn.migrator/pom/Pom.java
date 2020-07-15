package pom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import storage.MemoryStorage;
import storage.RepoLibrary;

public class Pom {
    public static String generatedRepoLibraries(String clonedRepo, String pomPath, String repoLink, String commitID, String previousVersionLibraries) throws JDOMException, IOException {
        String versionLibraries;
        String absolutePomPath = clonedRepo + "/" + pomPath;
        versionLibraries = deserializedListOfJavaProjectLibraries(absolutePomPath);
        ArrayList<String> listOfRemovedLibrary = listOfChangedLibraries(previousVersionLibraries, versionLibraries);
        if (listOfRemovedLibrary.size() > 0) {
            for (String libraryName : listOfRemovedLibrary) {
                System.err.println("Removed:" + libraryName);
                MemoryStorage.repoLibraries.add(new RepoLibrary(repoLink, commitID, libraryName, false, true, absolutePomPath));
            }
        }
        ArrayList<String> listOfAddedLibrary = listOfChangedLibraries(versionLibraries, previousVersionLibraries);
        if (listOfAddedLibrary.size() > 0) {
            for (String libraryName : listOfAddedLibrary) {
                System.err.println("Added:" + libraryName);
                MemoryStorage.repoLibraries.add(new RepoLibrary(repoLink, commitID, libraryName, true, false, absolutePomPath));
            }
        }
        previousVersionLibraries = versionLibraries;
        return previousVersionLibraries;
    }

    private static ArrayList<String> listOfChangedLibraries(String previousVersionLibraries,
                                                            String versionLibraries) {
        ArrayList<String> listOfLibrary = new ArrayList<String>();
        if (previousVersionLibraries.length() == 0 || versionLibraries.length() == 0) {
            // get all library that added in first commit
            if (previousVersionLibraries.length() != 0) {
                String[] addedLibraries = previousVersionLibraries.split(",");
                listOfLibrary.addAll(Arrays.asList(addedLibraries));
            }
            return listOfLibrary;
        }
        if (!previousVersionLibraries.equals(versionLibraries)) {
            String[] prev = previousVersionLibraries.split(",");
            String[] current = versionLibraries.split(",");
            for (String prevLibrary : prev) {
                if (prevLibrary.length() == 0) {
                    continue;
                }
                boolean isFound = false;
                for (String currentLibrary : current) {
                    if (prevLibrary.equals(currentLibrary)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    listOfLibrary.add(prevLibrary);
                }
            }
        }
        return listOfLibrary;
    }

    private static String deserializedListOfJavaProjectLibraries(String projectVersionPath) throws JDOMException, IOException {
        if (projectVersionPath.length() == 0) {
            System.err.println("project does not have pom.xml file");
            return "";
        }
        StringBuilder versionLibraries = new StringBuilder();
        System.out.println("Search for  library at :" + projectVersionPath);

        File inputFile = new File(projectVersionPath);
        if (inputFile.exists()) {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);

            Element root = document.getRootElement();

            // get public properties for library version
            HashMap<String, String> propertiesList = new HashMap<>();
            Element properties = elementChild(root, "properties");
            if (properties != null) {
                List<Element> propertiesListNode = properties.getChildren();
                for (Element property : propertiesListNode) {
                    propertiesList.put("${" + property.getName() + "}", property.getValue());
                }
            }

            // get library info
            Element dependencyManagement = elementChild(root, "dependencyManagement");
            Element dependencies = null;
            if (dependencyManagement != null) {
                dependencies = elementChild(dependencyManagement, "dependencies");
            } else {
                // dependencies may lives under root
                dependencies = elementChild(root, "dependencies");
            }
            if (dependencies != null) {
                List<Element> listOfDependencies = dependencies.getChildren();
                // System.out.println("----------------------------");

                for (Element dependency : listOfDependencies) {
                    List<Element> librariesList = dependency.getChildren();
                    String groupId = "";
                    String artifactId = "";
                    String version = "";
                    for (Element libraryInfo : librariesList) {
                        if (libraryInfo.getName().equals("groupId")) {
                            groupId = libraryInfo.getValue();
                        }
                        if (libraryInfo.getName().equals("artifactId")) {
                            artifactId = libraryInfo.getValue();
                        }
                        if (libraryInfo.getName().equals("version")) {
                            version = libraryInfo.getValue();
                            if (version.startsWith("${")) {
                                version = propertiesList.get(version);
                            }
                        }
                    }
                    String libraryLink = groupId + ":" + artifactId + ":" + version;

                    if (versionLibraries.length() == 0) {
                        versionLibraries = new StringBuilder(libraryLink);
                    } else {
                        if (!versionLibraries.toString().contains(libraryLink)) {
                            versionLibraries.append(",").append(libraryLink);
                        }
                    }
                }
            }
        }

        System.out.println("Found libraries-> " + versionLibraries);
        return versionLibraries.toString();
    }

    private static Element elementChild(Element classElement, String name) {
        try {
            List<Element> studentList = classElement.getChildren();
            for (Element element : studentList) {
                if (element.getName().equals(name)) {
                    return element;
                }
            }
        } catch (Exception ex) {
            System.out.println("No child found under the name:" + name);
        }
        return null;
    }
}
