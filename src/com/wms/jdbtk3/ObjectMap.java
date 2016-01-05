/* *  *  *  *  *  * Class to maintain bindings for data fields (database tables, etc) and real * java objects *  *  * which to reflect the values of those fields *  *  *  *  *  * Multiple objects can be bound to one fiend via: "object.methodCall(arg0)" *  *  * all methods that are bound to fields are currently limited to one argument. *  *  *  *  *  *  *  *  * Created on Jul 6, 2004 *  *  *  *  *  * TODO To change the template for this generated file go to *  *  * Window - Preferences - Java - Code Style - Code Templates *  *   */package com.wms.jdbtk3;import java.util.*;import java.lang.reflect.*;/** *  *  * @author mstemen *  *  *  *  *  * TODO To change the template for this generated type comment go to Window - *  *  * Preferences - Java - Code Style - Code Templates *  *   */public class ObjectMap extends Hashtable <Object, Object> {	boolean mapped = false;	// the number of currently bound objects	int numMapped = 0;	/**     *      *      *      *      *      * @uml.property name="mappedObjects"     *      *      * @uml.associationEnd     * @uml.property name="mappedObjects" multiplicity="(0     *      *      * 0)"     *      *       */	// the array of bound objects	Hashtable <String, Object>mappedObjects = new Hashtable<String, Object>();	/**     *      *      *      *      *      * @uml.property name="mappedClasses"     *      *      * @uml.associationEnd     * @uml.property name="mappedClasses" multiplicity="(0     *      *      * 1)" qualifier="getObjectName:java.lang.String     *      *      * mappedClass:java.lang.Class"     *      *       */	// the array of respectively bound classes	Hashtable <String, Class >mappedClasses = new Hashtable<String, Class>();	/**     *      *      * @return Returns the mappedObject.     *      *       */	private Object getMappedObject(String objectName) {		return mappedObjects.get(objectName);	}	private Class getTypeForString(String typeName) {		Class typedParam = null;		if (typeName.equals("int")) {			typedParam = Integer.TYPE;		} else if (typeName.equals("long")) {			typedParam = Long.TYPE;		} else if (typeName.equals("float")) {			typedParam = Float.TYPE;		} else if (typeName.equals("String")) {			typedParam = String.class;		} else if (typeName.equals("boolean")) {			typedParam = Boolean.TYPE;		} else {			System.err					.println("getTypeForString - Error: unknown type to convert. Type name was: "							+ typeName);		}		return typedParam;	}	private Object getObjectForString(String typeName) {		Object typedObject = null;		if (typeName.equals("int")) {			typedObject = new Integer(0);		} else if (typeName.equals("long")) {			typedObject = new Long(0);		} else if (typeName.equals("float")) {			typedObject = new Float(0.0);		} else if (typeName.equals("String")) {			typedObject = new String("");		} else if (typeName.equals("boolean")) {			typedObject = new Boolean(false);		} else {			// System.err.println( "Error: unknown type to convert. Type name			// was: " + typeName );		}		return typedObject;	}	private String getObjectName(String methodSig) {		String retStr = new String();		StringTokenizer st = new StringTokenizer(methodSig, ".");		retStr = st.nextToken();		retStr.trim();		return retStr;	}	private String getMethodName(String methodSig) {		String retStr = new String();		StringTokenizer st = new StringTokenizer(methodSig, ".");		st.nextToken();		String remainder = st.nextToken();		st = new StringTokenizer(remainder, "(");		retStr = st.nextToken();		retStr.trim();		return retStr;	}	private Object[] parseParametersForObjects(String fieldName) {		// look up the method signeture		String methodSig = (String) get(fieldName);		// parse the parameters		// rip off the parens		methodSig = methodSig.replace('(', ' ');		methodSig = methodSig.replace(')', ' ');		methodSig = methodSig.trim();		StringTokenizer st = new StringTokenizer(methodSig, ",");		// count the number of parameters		int paramCount = st.countTokens();		int paramIndex = 0;		Object[] returnObjs = new Object[paramCount];		while (st.hasMoreElements()) {			returnObjs[paramIndex++] = getObjectForString((String) st					.nextElement());		}		return returnObjs;	}	private Class[] parseParametersForTypes(String fieldName) {		// look up the method signeture		String methodSig = (String) get(fieldName);		// parse the parameters		// rip off the parens		methodSig = methodSig.replace('(', ' ');		methodSig = methodSig.replace(')', ' ');		methodSig = methodSig.trim();		StringTokenizer st = new StringTokenizer(methodSig, " ");		// count the number of parameters		int paramCount = 1;		int paramIndex = 0;		String token = st.nextToken();		token = st.nextToken();		Class[] returnTypes = new Class[paramCount];		returnTypes[paramIndex++] = getTypeForString(token);		return returnTypes;	}	/**     *      *      * the 'object will be mapped by field to a real data base record     *      *      *      *      *      * @author mstemen     *      *      *      *      *      * TODO To change the template for this generated type comment go to Window -     *      *      * Preferences - Java - Code Style - Code Templates     *      *       */	public boolean bindFieldRelationship(String fieldName, String methodName) {		/**         *          *          * Adds a new relationship between a field name in a record and a method         *          *          * call on a respective object         *          *          *          *          *           */		boolean success = false;		// try to construct a class of the object		try {			// TODO: this could be simplified more			Class mappedClass = (Class) mappedClasses.get(this					.getObjectName(methodName));			Method methods[] = mappedClass.getMethods();			// use refletion to prove out the existence of the method.			String matchingMethod = this.getMethodName(methodName);			for (int i = 0; i < methods.length; i++) {				if (methods[i].getName().equals(matchingMethod)) {					success = true;					mapped = true;					super.put(fieldName, methodName);					break;				}			}		} catch (Exception e) {			System.out.println("Class creation exception: " + e.getMessage());		}		return success;	}	/**     *      *      * @param mappedObject     *      *      * The mappedObject to set.     *      *       */	public void addMappedObject(String objectName, Object mappedObject) {		// add the class as well		try {			Class mappedClass = Class					.forName(mappedObject.getClass().getName());			this.mappedObjects.put(objectName, mappedObject);			this.mappedClasses.put(objectName, mappedClass);		} catch (Exception e) {			System.out.println("Class creation exception: " + e.getMessage());		}	}	private boolean updateFieldWithValue(String fieldName, Object value) {		boolean success = false;		try {			// for now, only know how to update primitive types and strings		} catch (Exception e) {			System.out.println(e.getMessage());		}		return success;	}	/**     *      *      * The dataMap will contain name value pairs of updated data to from some     *      *      * field that is going to be updated to some respective field in the object     *      *      * we contain.     *      *      *      *      *      * @param dataMap     *      *      * @return     *      *       */	public boolean updateWithHashtable(Hashtable dataMap) {		boolean success = false;		if (!mapped || dataMap.size() == 0)			return success;		Enumeration fieldKeys = dataMap.keys();		while (fieldKeys.hasMoreElements()) {			// obtain the current real data			String key = (String) fieldKeys.nextElement();			Object newData = dataMap.get(key);			// the method is respective to the current data, shold be one for			// one			String method = (String) get(key);			Object[] args = this.parseParametersForObjects(key);			Class[] paramTypes = this.parseParametersForTypes(key);			try {				Class mappedClass = (Class) mappedClasses.get(this						.getObjectName(method));				Method methodToCall = mappedClass.getMethod(this						.getMethodName(method), paramTypes);				// set the values to the args				// right now, there should only be one value in the arg list				if (args.length > 1) {					System.out							.println("Too many arguments in method to bind to ");					success = false;					break;				}				args[0] = newData;				Object mappedObject = mappedObjects.get(this						.getObjectName(method));				methodToCall.invoke(mappedObject, args);				success = true;			} catch (Exception e) {				System.out.println(e.getMessage());			}		}		return success;	}	public boolean updateWithMap(Collection dataMap) {		boolean success = false;		if (!mapped || dataMap.size() == 0)			return success;		return success;	}	public boolean updateWithVector(Vector dataMap) {		boolean success = false;		if (!mapped || dataMap.size() == 0)			return success;		return success;	}}