package jcode;

import java.util.ArrayList;
import java.util.List;

public class MethodObj implements Comparable<MethodObj> {
	public String returnType;
	public String methodName;
	public List<String> inputParam;
	public String scope;
	public String fullMethodName;
	public String packageName;
	public Double frequency;

	public MethodObj(String returnType, String methodName, String scope) {
		inputParam = new ArrayList<>();
		this.returnType = returnType;
		this.methodName = methodName;
		this.scope = scope;

	}

	public MethodObj() {
		inputParam = new ArrayList<>();
		this.returnType = "";
		this.methodName = "";
		this.scope = "";
		this.fullMethodName = "";
	}

	public void addInputParam(String inputType) {
		inputParam.add(inputType.trim());
	}

	public String getInputParamAsString() {
		String inputParams = "";
		for (String param : inputParam) {
			if (inputParams.isEmpty()) {
				inputParams = param;
			} else {
				inputParams = inputParams + "," + param;
			}
		}
		return inputParams;
	}

	public int countOfInputParam() {
		return inputParam.size();
	}

	// TODO: need to be able to process method like sum({1,2,3})
	// This function receive line of code and return method signature
	public static MethodObj generatedSignature(String lineCode) {
		lineCode = lineCode.trim();
		MethodObj methodObj = new MethodObj();
		// find index of input param
		int startParamIndex = lineCode.indexOf("(");
		int endParamIndex = lineCode.lastIndexOf(")");
		if (startParamIndex == -1 || endParamIndex == -1) {
			// System.err.println("line donot have method: "+ lineCode);
			return null;
		}
		String inputTypes = lineCode.substring(startParamIndex + 1, endParamIndex).trim();
		inputTypes = inputTypes.replaceAll("<([^<]*)>", "<T>"); // replace Answer1<T,A> to Answer1<T> avoid bad split
		// save all input param in list
		if (inputTypes.indexOf(",") > 0) {
			String[] paramsSP = inputTypes.split(",");
			for (String inputType : paramsSP) {
				methodObj.addInputParam(inputType.trim());
			}
		} else {
			// has only one param
			if (inputTypes.length() > 0) {
				methodObj.addInputParam(inputTypes.trim());
			}
		}
		// find function name
		String nameWithDefine = lineCode.substring(0, startParamIndex).trim();
		if (nameWithDefine.indexOf(" ") > 0) {
			// find function name
			int startNameIndex = nameWithDefine.lastIndexOf(" ");
			methodObj.methodName = lineCode.substring(startNameIndex + 1, nameWithDefine.length()).trim();
			// find return type
			String returnTypeWithDefine = lineCode.substring(0, startNameIndex).trim();
			if (returnTypeWithDefine.indexOf(" ") > 0) {
				int startReturnTypeIndex = returnTypeWithDefine.lastIndexOf(" ");
				methodObj.returnType = returnTypeWithDefine
						.substring(startReturnTypeIndex + 1).trim();
				// find scope
				methodObj.scope = lineCode.substring(0, startReturnTypeIndex).trim();
			} else {
				methodObj.returnType = returnTypeWithDefine;
			}
		} else {
			methodObj.methodName = nameWithDefine;
		}
		// check if their is static instance
		methodObj.packageName = packageName(methodObj.methodName);
		methodObj.methodName = getMethodNameWithoutPackage(methodObj.methodName);

		methodObj.fullMethodName = lineCode;

		if (methodObj.returnType.equals("public") || methodObj.returnType.equals("protected")
				|| methodObj.returnType.equals("private")) {
			methodObj.scope = methodObj.returnType;
			methodObj.returnType = "void";
		}
		return methodObj;
	}

	// Get name without packages
	public static String getMethodNameWithoutPackage(String name) {
		// remove dots if it's found
		if (name.contains(".")) {
			int lastDotIndex = name.lastIndexOf(".");
			name = name.substring(lastDotIndex + 1).trim();
		}
		return name;
	}

	// Get name without packages
	public static String packageName(String name) {
		// remove dots if it's found
		if (name.contains(".")) {
			int lastDotIndex = name.lastIndexOf(".");
			name = name.substring(0, lastDotIndex).trim();
		} else {
			name = "";
		}
		return name;
	}

	public boolean inputParamCompare(MethodObj otherMethodObj) {
		boolean isEqual = false;
		// in case they have same number of param
		if (this.countOfInputParam() == otherMethodObj.countOfInputParam()) {
			isEqual = true;
		}

		/*
		 * in case we compare with '...' that could have any number of param public
		 * static void verify(java.lang.Object...);
		 */
		if (!isEqual && countOfInputParam() == 1) {
			if (this.inputParam.get(0).endsWith("...")) {
				isEqual = true;
			}
		}

		if (!isEqual && otherMethodObj.countOfInputParam() == 1) {
			if (otherMethodObj.inputParam.get(0).endsWith("...")) {
				isEqual = true;
			}
		}

		return isEqual;
	}

	public boolean inputParamDataType(MethodObj otherMethodObj) {

		if (this.countOfInputParam() == otherMethodObj.countOfInputParam() && this.countOfInputParam() == 0) {
			return true;
		}
		if (this.countOfInputParam() != otherMethodObj.countOfInputParam()) {
			return false;
		}

		int countOfParams = 0;
		ArrayList<String> listOfMyParams = new ArrayList<>(this.inputParam);

		ArrayList<String> listOfOtherParams = new ArrayList<>(otherMethodObj.inputParam);

		for (String listOfMyParam : listOfMyParams) {
			String methodDataType = listOfMyParam.trim();
			String[] inputParamParts = methodDataType.split(" ");
			methodDataType = inputParamParts[0];
			if (methodDataType.contains(".")) {
				String[] otherInputParamParts = methodDataType.split("\\.");
				methodDataType = otherInputParamParts[otherInputParamParts.length - 1];

			}
			for (int j = 0; j < listOfOtherParams.size(); j++) {

				String[] inputPramaOtherSp = listOfOtherParams.get(j).trim().split("\\s+");
				String otherDataType = inputPramaOtherSp[0];
				if (otherDataType.contains(".")) {

					inputPramaOtherSp = otherDataType.split("\\.");
					otherDataType = inputPramaOtherSp[inputPramaOtherSp.length - 1];
				}
				if (otherDataType.toLowerCase().startsWith(methodDataType.toLowerCase())) {
					listOfOtherParams.remove(j);
					countOfParams++;
					break;
				}
			}
		}
		return (countOfParams == otherMethodObj.inputParam.size()) && (countOfParams == this.inputParam.size());
	}

	public double inputParamComparePercent(MethodObj otherMethodObj) {
		double isEqual = 0;
		// in case they have same number of param
		if (this.countOfInputParam() == otherMethodObj.countOfInputParam()) {
			isEqual = 1.0;
		}

		/*
		 * in case we compare with '...' that could have any number of param public
		 * static void verify(java.lang.Object...);
		 */
		if (isEqual == 0 && countOfInputParam() == 1) {
			if (this.inputParam.get(0).endsWith("...")) {
				isEqual = 1.0;
			}
		}

		if (isEqual == 0 && otherMethodObj.countOfInputParam() == 1) {
			if (otherMethodObj.inputParam.get(0).endsWith("...")) {
				isEqual = 1.0;
			}
		}

		return Math.abs((this.countOfInputParam() - otherMethodObj.countOfInputParam()));
	}

	// Show different in Ratio
	public double inputParamCompareRatio(MethodObj otherMethodObj) {
		double diffRation = inputParamComparePercent(otherMethodObj);
		if (diffRation > 1) {
			diffRation = 1.0 - (diffRation / ((this.countOfInputParam() + otherMethodObj.countOfInputParam()) * 1.0));
		}

		return diffRation;
	}

	public boolean inputParamDataTypeCompare(MethodObj otherMethodObj) {
		boolean isEqual = true;
		// in case they have same number of param
		if (this.countOfInputParam() == otherMethodObj.countOfInputParam()) {
			for (int i = 0; i < this.countOfInputParam(); i++) {
				if (!this.inputParam.get(i).equals(otherMethodObj.inputParam.get(i))) {
					isEqual = false;
					break;
				}
			}
		} else {
			isEqual = false;
		}

		return isEqual;
	}

	/*
	 * see if two methods has good match To have excellent match they should be
	 * similar in number of params and not abstract
	 */
	boolean hasGoodMatch(String functionSignature) {
		boolean hasGoodMatch = true;
		// We prefer method name over abstract name,if we cannot find method we will use
		// abstract
		if (!functionSignature.contains("abstract ")) {
			hasGoodMatch = false;
		}

		if (hasGoodMatch && this.countOfInputParam() == 1) {
			if (this.inputParam.get(0).endsWith("...")) {
				hasGoodMatch = false;
			}
		}

		return hasGoodMatch;
	}

	public int compareTo(MethodObj o) {
		// TODO Auto-generated method stub
		return (int) (o.frequency - this.frequency);
	}
}
