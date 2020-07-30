package jcode;

import bash.BashCommand;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import migration.MigrationSegment;

public class CleanJavaCode {
	ClassStructure classStructure = new ClassStructure();

	public CleanJavaCode() {

	}

	// get all segment that found in one project
	public List<MigrationSegment> listOfCleanedFiles(String path, ArrayList<String> diffsFilePath) throws IOException, InterruptedException {
		System.out.println("\n**************** Start cleanning file from Java code **************");
		List<MigrationSegment> segmentList = new ArrayList<>();
		for (String diffPath : diffsFilePath) {
			System.out.println("=========>======" + diffPath);
			System.out.println("Start Cleanning File:" + diffPath);
			List<MigrationSegment> listOfblocks = listOfCleanedMigrationSegments(diffPath);
			if (listOfblocks.size() > 0) {
				segmentList.addAll(listOfblocks);
			}

		}
		return segmentList;
	}

	public List<MigrationSegment> listOfCleanedMigrationSegments(String diffFilePath) throws InterruptedException, IOException {
		List<MigrationSegment> listOfBlocks = listOfMigrationSegments(diffFilePath);
		StringBuilder fileWithoutJavaCode = new StringBuilder();
		// read list of blocks
        for (MigrationSegment segment : listOfBlocks) {
            System.out.println("Remove:" + segment.removedCode.size());
            System.out.println("Add:" + segment.addedCode.size());
            for (String lineIn : segment.blockCode) {
                fileWithoutJavaCode.append(lineIn);
                fileWithoutJavaCode.append("\n");
                System.out.println(lineIn);
            }
            System.out.println("----------------------------");
        }
        // write code segment to new clean file
        if (fileWithoutJavaCode.length() > 0) {
            String cleanDiffPath = diffFilePath.replace(".txt", "_clean.txt");
            FileWriter fr = new FileWriter(cleanDiffPath); // After '.' write
            fr.write(fileWithoutJavaCode.toString()); // Warning: this will REPLACE your old file content!
            fr.close();
            System.out.println("Complete Clean Up File successfully\n");
        } else {
            BashCommand.deleteFolder(diffFilePath);
            BashCommand.deleteFolder(diffFilePath.replace(".txt", "_before.java"));
            BashCommand.deleteFolder(diffFilePath.replace(".txt", "_after.java"));
            System.err.println("Delete File because it dosenot have migration");

			    // delete DR if there is no files there
            String commitDir = diffFilePath.substring(0, diffFilePath.lastIndexOf("/"));
            File folderDir = new File(commitDir);
            if (folderDir.isDirectory()) {
                if (Objects.requireNonNull(folderDir.list()).length <= 0) {
                    BashCommand.deleteFolder(commitDir);
                    System.err.println("Directory is empty! will delete it");
                }
            }
        }
		return listOfBlocks;
	}

	public List<MigrationSegment> listOfMigrationSegments(String diffFilePath) throws IOException {

		List<MigrationSegment> listOfBlocks = new ArrayList<>();

		List<String> listOfAddLibraryClassesName = classStructure.libraryClasses(MigratedLibraries.toLibrary);
		List<String> listOfRemovedLibraryClassesName = classStructure
				.libraryClasses(MigratedLibraries.fromLibrary);
		List<String> listOfAllClassesNames = new ArrayList<>();
		listOfAllClassesNames.addAll(listOfAddLibraryClassesName);
		listOfAllClassesNames.addAll(listOfRemovedLibraryClassesName);
		HashMap<String, String> listOfClassInstances = listOfLibraryClassesInstance(listOfAllClassesNames, diffFilePath);
		List<String> listOfAllRemovedStaticMethod = classStructure.staticMethods(MigratedLibraries.fromLibrary);
		List<String> listOfAllAddedStaticMethod = classStructure.staticMethods(MigratedLibraries.toLibrary);
		// This will hold previous segment to
		// catch case when complete block removed and complete block is removed
		// TODO: we need to add ability to read method that's on multi line

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(diffFilePath)));
        String line;
        boolean isAppendedLine = false; // if developer used '.' to refer to instance from prev line
        boolean isScannedLine = false;

        ArrayList<String> chunkOfText = new ArrayList<String>();// save junk temp before move
        while ((line = br.readLine()) != null) {

            // ignore empty line that that has only >, or <
            if (line.trim().length() <= 1) {
                continue;
            }

            // ignore commented line
            String lineClean = cleanLineOfCode(line);
            if (lineClean.startsWith("*") || lineClean.startsWith("/")) {
                continue;
            }

            isScannedLine = false; // if the line has been processed already
            if (line.length() <= 0) {
                continue;
            }

            if (lineStartsWithNumber(line)) {
                MigrationSegment cleanBlockSegment = isGoodBlock(
                    chunkOfText, listOfClassInstances, listOfAddLibraryClassesName, listOfRemovedLibraryClassesName
                );
                if (cleanBlockSegment.addedCode.size() > 0 && cleanBlockSegment.removedCode.size() > 0) {
                    // Segment segment= new
                    // Segment(cleanBlockSegment.blockCode,cleanBlockSegment.countAddLines,cleanBlockSegment.countRemovedLines);
                    cleanBlockSegment.addFileName(diffFilePath);
                    listOfBlocks.add(cleanBlockSegment);
                }
                chunkOfText = new ArrayList<>();
            }

            // if it use library class
            for (String className : listOfAllClassesNames) {
                if (line.contains(className)) {
                    chunkOfText.add(line);
                    isScannedLine = true;
                    isAppendedLine = true;
                    break;
                }
            }
            if (isScannedLine) {
                continue;
            }

            // if it use library instance
            for (String classInstance : listOfClassInstances.keySet()) {
                if (line.contains(classInstance + ".")) {
                    // line=line.replace(classInstance +".",
                    // listOfClassesInstances.get(classInstance) +".");
                    chunkOfText.add(line);
                    isScannedLine = true;
                    isAppendedLine = true;
                    // make sure we used added and removed library
                    break;
                }
            }
            if (isScannedLine) {
                continue;
            }

            // see if he use static method
            if (line.trim().startsWith("<")) {
                for (String methodName : listOfAllRemovedStaticMethod) {
                    // either line stated with method with space or return data from method assgin
                    // to object
                    if (line.contains(" " + methodName + "(") || line.contains("=" + methodName + "(")) {
                        chunkOfText.add(line);
                        isScannedLine = true;
                        isAppendedLine = true;

                        break;
                    }
                }
            }
            if (line.trim().startsWith(">")) {
                for (String methodName : listOfAllAddedStaticMethod) {
                    if (line.contains(" " + methodName + "(") || line.contains("=" + methodName + "(")) {
                        chunkOfText.add(line);
                        isScannedLine = true;
                        isAppendedLine = true;

                        break;
                    }
                }
            }

            if (isScannedLine) {
                continue;
            }

            /*
			* In case developer used '.' instance ex return new OkHttpClient.Builder() >
			* .connectTimeout(15, TimeUnit.SECONDS) > .build(); Read this data and change
			* it to return new OkHttpClient.Builder().connectTimeout(15,
			* TimeUnit.SECONDS).build();
			*/
            if (startsWithDot(line) && isAppendedLine) {
                String previousLine = chunkOfText.get(chunkOfText.size() - 1);
                String newLineConcat = previousLine.trim() + cleanLineOfCode(line);
                chunkOfText.set(chunkOfText.size() - 1, newLineConcat);
            } else {

                // another line of code added or removed
                if (line.trim().startsWith(">") || line.trim().startsWith("<")) {
                    isAppendedLine = false;
                } else {
                    chunkOfText.add(line);
                }
            }

        } // while

        // check if last block is valid
        MigrationSegment cleanBlockSegment = isGoodBlock(
            chunkOfText,
            listOfClassInstances,
            listOfAddLibraryClassesName,
            listOfRemovedLibraryClassesName
        );
        if (cleanBlockSegment.addedCode.size() > 0 && cleanBlockSegment.removedCode.size() > 0) {
            cleanBlockSegment.addFileName(diffFilePath);
            listOfBlocks.add(cleanBlockSegment);
        }
        br.close();
		return listOfBlocks;
	}

	// check if the line is started with "." that mean it continue for prevous line
	boolean startsWithDot(String line) {
		line = line.trim();
		if (line.length() < 2) {
			return false;
		}
		String lineWithDot = line.trim().substring(1).trim();
        return lineWithDot.indexOf(".") == 0;
	}

	// Return line without >,<
	String cleanLineOfCode(String line) {
		if (line.length() < 2) {
			return "";
		}
		return line.substring(1).trim();
	}

	// We know the block is good it it has added and removed lines
	MigrationSegment isGoodBlock(
	    List<String> blockOfChunks, HashMap<String, String> listOfClassesInstances,
        List<String> listOfAddLibraryClassesName, List<String> listOfRemovedLibraryClassesName) {

		List<String> blockOfChainingChunks = new ArrayList<>();

        for (String lineOfCode : blockOfChunks) {
            String operation = lineOfCode.substring(0, 1); // either >, or <
            String cleanLine = cleanLineOfCode(lineOfCode);
            if (cleanLine.indexOf(").") > 0) {

                // in case we have ex, return person.setName("Peter").setAge(21).introduce();
                if (cleanLine.startsWith("return")) {
                    cleanLine = cleanLine.substring(7).trim(); // 7 refer to next char after return
                }
                int classNameIndex = cleanLine.indexOf(".");
                String className = cleanLine.substring(0, classNameIndex).trim();

                // case when direct create new instance, new
                // Person().setName("Peter").setAge(21).introduce()
                if (className.startsWith("new ") && className.contains("(")) {
                    className = className.substring(4,
                        className.indexOf("(")
                    ); // 4 for first char after 'new'
                } else if (!className.startsWith("new ") && className.contains("(")) {
                    // He call static method
                    // ex: setName("Peter").setAge(21).introduce();
                    className = "";
                    classNameIndex = -1;
                }

                String methodsName = cleanLine.substring(classNameIndex + 1);

                String[] listOfMethodParts = methodsName.split("\\)\\.");
                for (String methodName : listOfMethodParts) {
                    if (!methodName.endsWith(";")) {
                        methodName = methodName + ")";
                    }
                    String methodClassInstance = (className.isEmpty() ? methodName : (className + "." + methodName));
                    methodClassInstance = methodClassInstance.indexOf(
                        ".") == 0 ? methodClassInstance.substring(1)
                        : methodClassInstance;
                    String fullCall = operation + " 	" + methodClassInstance;
                    blockOfChainingChunks.add(fullCall);
                }

                // If the code has loops
            } else if (
                cleanLine.startsWith("if ") ||
                cleanLine.startsWith("if(") ||
                cleanLine.startsWith("while ") ||
                    cleanLine.startsWith("while(")) {
                List<String> listOfMethodsCall = new ClassObj().findMethods(cleanLine.trim());
                if (listOfMethodsCall.size() > 0) {
                    for (String methodCall : listOfMethodsCall) {
                        blockOfChainingChunks.add(operation + " 	" + methodCall);
                    }
                }
            } else {
                blockOfChainingChunks.add(lineOfCode);
            }
        }
		blockOfChainingChunks.clear();
        blockOfChunks.addAll(blockOfChainingChunks);
		blockOfChainingChunks.clear();

		MigrationSegment segment = new MigrationSegment();
		ArrayList<String> blockOfSignatureJunks = new ArrayList<String>();
		if (blockOfChunks.size() == 0) {
			return segment;
		}

        for (String junText : blockOfChunks) {
            // we do not process import statement
            if (junText.trim().contains("import ")) {
                continue;
            }
            if (junText.startsWith(">")) {

                // build added lines block
                String cleanLine = cleanLineOfCode(junText);
                if (cleanLine.length() > 0) {

                    // find class name
                    String className = foundClassName(
                        listOfAddLibraryClassesName,
                        listOfClassesInstances,
                        cleanLine.trim()
                    );
                    // convert function name to signature
                    Translate translate = new Translate(MigratedLibraries.toLibrary);
                    String signature = "";
                    int equalIndex = junText.indexOf("=");
                    if (equalIndex > 0) {
                        // we have return type search for same method name, return type, and number of
                        // parameters
                        signature = translate.methodSignature(
                            cleanLine.substring(equalIndex + 1).trim(), className);
                    }
                    if (!className.isEmpty() && signature.isEmpty()) {
                        // call instance of the class, search for same method name, class name, and
                        // number of parameters
                        signature = translate.methodClassSignature(className, cleanLine.trim());
                    }
                    // if you cannot find use search for same method name, and number of parameters
                    if (signature.isEmpty()) {
                        signature = translate.methodSignature(cleanLine.trim());
                    }

                    // Find signature
                    if (!signature.isEmpty()) {
                        if (!segment.addedCode.contains(signature)) {
                            segment.addedCode.add(signature);
                            // blockOfJunck.set(i,"> "+ signature);
                            blockOfSignatureJunks.add("> 	" + signature);
                        }
                    }
                }
            } else if (junText.startsWith("<")) {

                // build remove lines block
                String cleanLine = cleanLineOfCode(junText);
                if (cleanLine.length() > 0) {

                    // convert function name to signature
                    // find class name
                    String className = foundClassName(listOfRemovedLibraryClassesName,
                        listOfClassesInstances,
                        cleanLine.trim()
                    );
                    // convert function name to signature
                    Translate translate = new Translate(MigratedLibraries.fromLibrary);
                    String signature = "";
                    int equalIndex = junText.indexOf("=");
                    if (equalIndex > 0) {
                        // we have return type search for same method name, return type, and number of
                        // parameters
                        signature = translate.methodSignature(
                            cleanLine.substring(equalIndex + 1).trim(), className);
                    }
                    if (!className.isEmpty() && signature.isEmpty()) {
                        // call instance of the class, search for same method name, class name, and
                        // number of parameters
                        signature = translate.methodClassSignature(className, cleanLine.trim());
                    }
                    // if you cannot find use search for same method name, and number of parameters
                    if (signature.isEmpty()) {
                        signature = translate.methodSignature(cleanLine.trim());
                    }
                    if (!signature.isEmpty()) {
                        if (!segment.removedCode.contains(signature)) {
                            segment.removedCode.add(signature);
                            // blockOfChunk.set(i,"< "+ signature);
                            blockOfSignatureJunks.add("< 	" + signature);
                        }
                    }
                }
            } else {
                blockOfSignatureJunks.add(junText);
            }

        }

		// check if it valid chunk has to have hasAddCode=true,hasRemoveCode=true
		if ((segment.addedCode.size() > 0 && segment.removedCode.size() > 0)) {
			// segment.blockCode.addAll(blockOfJunck);
			segment.blockCode.addAll(blockOfSignatureJunks);

		} else {
			// clean the data
			segment.removedCode.clear();
			segment.addedCode.clear();
		}

		// Segment segment = new Segment(blockOfChunk,countAddLines,countRemovedLines);
		return segment;
	}

	String foundClassName(List<String> listOfLibraryClassesName, HashMap<String, String> listOfClassesInstances, String lineCode) {
		String className = "";

		// find class name from instance name
		int equalIndex = lineCode.indexOf("=");
		if (equalIndex > 0) {

			/*
			 * In case he use instace from class like 'metaData' use instance of control <
			 * IMocksControl control = EasyMock.createStrictControl(); < DatabaseMetaData
			 * metaData = control.createMock(DatabaseMetaData.class);
			 */
			String instanceDefine = lineCode.substring(equalIndex + 1, lineCode.length()).trim();
			for (String classInstance : listOfClassesInstances.keySet()) {
				if (instanceDefine.startsWith(classInstance + ".")
						|| instanceDefine.startsWith("this." + classInstance + ".")) {
					className = listOfClassesInstances.get(classInstance);
					break;
				}
			}

			/*
			 * In case he use static instance from the class < IMocksControl control =
			 * EasyMock.createStrictControl();
			 */
			if (className.isEmpty()) {
				for (String classInstance : listOfLibraryClassesName) {
					if (instanceDefine.startsWith(classInstance + ".")) {
						className = classInstance;
						break;
					}
				}
			}

			String varaibleDefine = lineCode.substring(0, equalIndex).trim();

			// line has new class instance like (final int age=12)
			if (className.isEmpty()) {
				// ex--> final int age
				int spaceIndex = varaibleDefine.lastIndexOf(" ");
				if (spaceIndex > 0) {
					// ex--> final int
					String define = varaibleDefine.substring(0, spaceIndex).trim();
					spaceIndex = define.lastIndexOf(" ");
					// ex--> int
					if (spaceIndex > 0) {
						className = define.substring(spaceIndex + 1, define.length()).trim();
					} else {
						className = define;
					}
				}
			}

			if (className.isEmpty()) {
				// check if it class instance has been initialized here
				// ex--> age=12
				for (String classInstance : listOfClassesInstances.keySet()) {
					if (varaibleDefine.equals(classInstance)) {
						className = listOfClassesInstances.get(classInstance);
						break;
					}
				}
			}

		} else {
			// in case we just call method using class instance
			if (className.isEmpty()) {
				// find class name if the line has instance from class
				/*
				 * lineCode.startsWith(classInstance +".") for class instance
				 * lineCode.contains(classInstance +".") for use in if statement
				 */
				for (String classInstance : listOfClassesInstances.keySet()) {
					if (lineCode.startsWith(classInstance + ".") || lineCode.startsWith("this." + classInstance + ".")
							|| lineCode.contains(" " + classInstance + ".")) {
						className = listOfClassesInstances.get(classInstance);
						break;
					}
				}

			}
			if (className.isEmpty()) {
				// find if he call direct static instance from library class
				for (String classInstance : listOfLibraryClassesName) {
					if (lineCode.trim().startsWith(classInstance + ".")
							|| lineCode.contains(" " + classInstance + ".")) {
						className = classInstance;
						break;
					}
				}
			}
		}
		return className;
	}

	boolean lineStartsWithNumber(String line) {
        return Character.isDigit(line.charAt(0));
    }

	// get list of of class instances
	public HashMap<String, String> listOfLibraryClassesInstance(List<String> listOfClasses, String diffFilePath) {
		HashMap<String, String> listOfClassesInstance = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(diffFilePath)));
			String line;
			String instanceName;
			while ((line = br.readLine()) != null) {
				// we donot process import statement
				if (line.trim().contains("import ")) {
					continue;
				}
				line = cleanLineOfCode(line);
				for (String libraryClassName : listOfClasses) {
					if (line.contains(libraryClassName)) {
						if (line.length() > 2) {

							ClassObjInfo classObjInfo = instanceName(line, libraryClassName);
							if (classObjInfo.instanceName != null) {
								if (listOfClassesInstance.get(classObjInfo.instanceName) == null) {
									listOfClassesInstance.put(classObjInfo.instanceName.trim(), classObjInfo.className);
								}
							}
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return listOfClassesInstance;
	}

	// This method return instance name
	public ClassObjInfo instanceName(String line, String ClassName) {
		ClassObjInfo classObjInfo = new ClassObjInfo();
		classObjInfo.instanceName = null;
		classObjInfo.className = null;
		// in case define and initiailize the instance
		int hasEqual = line.indexOf("=");
		int hasbraket = line.indexOf("(");
		if (hasEqual > 0 && hasEqual < hasbraket) {

			String instanceSide = line.substring(0, hasEqual).trim();
			int hasspace = instanceSide.lastIndexOf(" ");
			if (hasspace > 0) {
				classObjInfo.instanceName = instanceSide.substring(hasspace + 1, instanceSide.length()).trim();
				String classNameInfo = instanceSide.substring(0, hasspace).trim();
				hasspace = classNameInfo.lastIndexOf(" ");
				if (hasspace > 0) {
					classObjInfo.className = classNameInfo.substring(hasspace + 1, classNameInfo.length()).trim();
				} else {
					classObjInfo.className = classNameInfo;
				}
			} else {
				classObjInfo.instanceName = instanceSide.trim();
				classObjInfo.className = ClassName;
			}
		} else if (line.contains(ClassName + " ")) { // space mean some one define like ClassName instance;
			int classIndex = line.indexOf(ClassName + " ") + ClassName.length() + 1;
			String instanceName = line.substring(classIndex).trim();
			if (instanceName.indexOf(";") > 0) {
				classObjInfo.instanceName = instanceName.substring(0, instanceName.indexOf(";")).trim();
				classObjInfo.className = ClassName;
			}
		}
		// In case we have this example List<Description>
		if (classObjInfo.className != null) {
			int indexOfBraket = classObjInfo.className.indexOf("<");
			int indexOfEndBraket = classObjInfo.className.lastIndexOf(">");
			if (indexOfBraket > 0 && indexOfEndBraket > 0) {
				classObjInfo.className = classObjInfo.className.substring(indexOfBraket + 1, indexOfEndBraket).trim();
			}
		}
		if (classObjInfo.instanceName != null) {
			if (classObjInfo.instanceName.startsWith("this.") && classObjInfo.instanceName.length() > 5) {
				classObjInfo.instanceName = classObjInfo.instanceName.substring(5);
			}
		}
		return classObjInfo;
	}

	// check if the file used that library
	public boolean isUsedNewLibrary(String filePath, String addedLibraryName) throws IOException {
		List<String> listOfLibraryVersions = classStructure.libraryPackages(addedLibraryName);
		boolean isUsed = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));

		String line;
		while ((line = br.readLine()) != null) {
		    if (line.contains("import")) {
		        for (String packageName : listOfLibraryVersions) {
		            if (line.contains(packageName + ".")) { // Why dot because when any one import package he have
																// to add dot like (package.* or package.ClassName
                        isUsed = true;
                        break;
		            }
		        }
		    }
		}
		br.close();
		return isUsed;
	}

}
