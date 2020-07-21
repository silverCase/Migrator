package pom;

import com.guseyn.broken_xml.Element;
import com.guseyn.broken_xml.ParsedXML;
import com.guseyn.broken_xml.Text;
import com.guseyn.broken_xml.XmlDocument;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import storage.MemoryStorage;
import storage.RepoLibrary;

public class Pom {
    public static String generatedRepoLibraries(String clonedRepo, String pomPath, String repoLink, String commitID, String previousVersionLibraries) throws IOException {
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

    private static String deserializedListOfJavaProjectLibraries(String projectVersionPath) throws IOException {
        if (projectVersionPath.length() == 0) {
            System.err.println("project does not have pom.xml file");
            return "";
        }
        StringBuilder versionLibraries = new StringBuilder();
        System.out.println("Search for  library at :" + projectVersionPath);

        File inputFile = new File(projectVersionPath);
        if (inputFile.exists()) {
            XmlDocument document = new ParsedXML(file.File.content(projectVersionPath)).document();
            Element root = document.roots().get(0);

            // get public properties for library version
            HashMap<String, String> propertiesList = new HashMap<>();
            Element properties = elementChild(root, "properties");
            if (properties != null) {
                List<Element> propertiesListNode = properties.children();
                for (Element property : propertiesListNode) {
                    propertiesList.put("${" + property.name() + "}", textsAsOneValue(property.texts()));
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
                List<Element> listOfDependencies = dependencies.children();
                // System.out.println("----------------------------");

                for (Element dependency : listOfDependencies) {
                    List<Element> librariesList = dependency.children();
                    String groupId = "";
                    String artifactId = "";
                    String version = "";
                    for (Element libraryInfo : librariesList) {
                        if (libraryInfo.name().equals("groupId")) {
                            groupId = textsAsOneValue(libraryInfo.texts());
                        }
                        if (libraryInfo.name().equals("artifactId")) {
                            artifactId = textsAsOneValue(libraryInfo.texts());
                        }
                        if (libraryInfo.name().equals("version")) {
                            version = textsAsOneValue(libraryInfo.texts());
                            if (version.startsWith("${")) {
                                if (propertiesList.get(version) != null) {
                                    if (propertiesList.get(version).contains("-")) {
                                        System.out.println("ok");
                                    }
                                    version = propertiesList.get(version).replace(":", "-");
                                }
                            }
                        }
                    }
                    version = version.replace(",", ";");
                    String libraryLink = String.join(":", groupId, artifactId, version);
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
            List<Element> studentList = classElement.children();
            for (Element element : studentList) {
                if (element.name().equals(name)) {
                    return element;
                }
            }
        } catch (Exception ex) {
            System.out.println("No child found under the name:" + name);
        }
        return null;
    }

    private static String textsAsOneValue(List<Text> texts) {
        return texts.stream().map(Text::value).collect(Collectors.joining(""));
    }
}
