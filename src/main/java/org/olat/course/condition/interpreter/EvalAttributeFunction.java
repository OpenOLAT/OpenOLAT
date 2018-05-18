/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.condition.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;


/**
 * @author Felix Jost
 */
public class EvalAttributeFunction extends AbstractFunction {
	
	private static final OLog log = Tracing.createLoggerFor(EvalAttributeFunction.class);
	
	/***************************************************************
	 * Function types                                              *
	 *                                                             *
	 * ATTENTION: Don't change the order!                          *
	 *                                                             *
	 ***************************************************************/ 
	
	// user must have exaktly this expression in one of the values of given attribute
	public static final int 		FUNCTION_TYPE_HAS_ATTRIBUTE = 0;
	private static final String FUNCTION_NAME_HAS_ATTRIBUTE = "hasAttribute";
	// one of the values of given attribute must start with the given expression
	public static final int 		FUNCTION_TYPE_ATTRIBUTE_STARTS_WITH = 1;
	private static final String FUNCTION_NAME_ATTRIBUTE_STARTS_WITH = "attributeStartswith";
	// one of the values of given attribute must end with the given expression
	public static final int 		FUNCTION_TYPE_ATTRIBUTE_ENDS_WITH = 2;
	private static final String FUNCTION_NAME_ATTRIBUTE_ENDS_WITH = "attributeEndswith";
	// given expression must be part of one of the values of given attribute
	public static final int 		FUNCTION_TYPE_IS_IN_ATTRIBUTE = 3;
	private static final String FUNCTION_NAME_IS_IN_ATTRIBUTE = "isInAttribute";
	// neither of the values of given attribute may contain the given expression
	public static final int 		FUNCTION_TYPE_IS_NOT_IN_ATTRIBUTE = 4;
	private static final String FUNCTION_NAME_IS_NOT_IN_ATTRIBUTE = "isNotInAttribute";
	// neither of the values of given attribute may match the given expression
	public static final int 		FUNCTION_TYPE_HAS_NOT_ATTRIBUTE = 5;
	private static final String FUNCTION_NAME_HAS_NOT_ATTRIBUTE = "hasNotAttribute";
	
	private int functionType;
	public String name;
		//private static final Pattern multiValueSeparatorValue = Pattern.compile(ShibbolethModule.MULTIVALUE_SEPARATOR);
	
	/**
	 * @param userCourseEnv
	 * @param functionType the type of function (use defined constants)
	 */
	public EvalAttributeFunction(UserCourseEnvironment userCourseEnv, int functionType) {
		super(userCourseEnv);
		this.functionType = functionType;
		if (functionType == FUNCTION_TYPE_HAS_ATTRIBUTE) {
			this.name = FUNCTION_NAME_HAS_ATTRIBUTE;
		} else if (functionType == FUNCTION_TYPE_ATTRIBUTE_STARTS_WITH) {
			this.name = FUNCTION_NAME_ATTRIBUTE_STARTS_WITH;
		} else if (functionType == FUNCTION_TYPE_ATTRIBUTE_ENDS_WITH) {
			this.name = FUNCTION_NAME_ATTRIBUTE_ENDS_WITH;
		} else if (functionType == FUNCTION_TYPE_IS_IN_ATTRIBUTE) {
			this.name = FUNCTION_NAME_IS_IN_ATTRIBUTE;
		} else if (functionType == FUNCTION_TYPE_HAS_NOT_ATTRIBUTE) {
			this.name = FUNCTION_NAME_HAS_NOT_ATTRIBUTE;
		} else if (functionType == FUNCTION_TYPE_IS_NOT_IN_ATTRIBUTE) {
			this.name = FUNCTION_NAME_IS_NOT_IN_ATTRIBUTE;
		} else {
			throw new OLATRuntimeException("This function type index is undefined: " + functionType, null);
		}
	}

	/**
	 * Searches expression <code>ex</code> in multivalue enabled attribute value <code>values</code>
	 * @param ex The expression to search for.
	 * @param values The attribute value(s), separated by ,
	 * @return true if found, false otherwise
	 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
	 */
	private boolean findExpressionInMultiValue(String ex, String values, int type) {
		try {
			if (ex == null || values == null) return false;											// empty params?
		  Pattern multiValueSeparatorEx = Pattern.compile(",");
			String[] a = multiValueSeparatorEx.split(ex);												// split on ,
			Pattern multiValueSeparatorValue = Pattern.compile(";");
			String[] b = multiValueSeparatorValue.split(values);								// split on ;
			if (a == null || (a.length == 1 && a[0] == "")) return false;				// empty array?
			if (b == null || (b.length == 1 && b[0] == "")) return false;				// empty array?
			if (log.isDebug()) {
				log.debug("a: " + Arrays.toString(a));
				log.debug("b: " + Arrays.toString(b));
			}
			if (type == FUNCTION_TYPE_HAS_ATTRIBUTE) {
				List<String> l = Arrays.asList(a);
				if (l.retainAll(Arrays.asList(b))) return true;										// all values are the same -> excellent :-)
				if (!l.isEmpty()) return true;																		// some equally values found, return true
				// l now contains all values which were also contained in values param, so if you want to work with that...
			} else if (type == FUNCTION_TYPE_ATTRIBUTE_STARTS_WITH) {
				for (int i = 0; i < a.length; ++i) {															// for every attribute value
					for (int j = 0; j < b.length; ++j) {
						if ((b[j].startsWith(a[i]))) return true;											// if match then return true
					}
				}
			} else if (type == FUNCTION_TYPE_ATTRIBUTE_ENDS_WITH) {
				for (int i = 0; i < a.length; ++i) {															// for every attribute value
					for (int j = 0; j < b.length; ++j) {
						if ((b[j].endsWith(a[i]))) return true;												// if match then return true
					}
				}
			} else if (type == FUNCTION_TYPE_IS_IN_ATTRIBUTE) {
				for (int i = 0; i < a.length; ++i) {															// for every attribute value
					for (int j = 0; j < b.length; ++j) {
						if ((b[j].indexOf(a[i])) > -1 ) return true;									// if match then return true
					}
				}
			} else if (type == FUNCTION_TYPE_IS_NOT_IN_ATTRIBUTE) {
				boolean somethingFound = false;
				for (int i = 0; i < a.length; ++i) {															// for every attribute value
					for (int j = 0; j < b.length; ++j) {
						if ((b[j].indexOf(a[i])) > -1) somethingFound = true;				// if match then return true
					}
				}
				return !somethingFound;
			}
			return false;																												// only return false if nothing found
		} catch (Exception e) {
			return false;																												// some String was null or something else unexpected 
		}
	}
	
	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 2) {
			return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.providetwo.attrvalue"));
		} else if (inStack.length < 2) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.providetwo.attrvalue")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.attributename", "solution.example.name.infunction"));
		if (!(inStack[1] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.attribvalue", "solution.example.name.infunction"));
		String attributeId = (String) inStack[0];
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// remember the reference to the attribute for this condtion
			cev.addSoftReference("attribute", attributeId, false);
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}


		/*
		 * the real function evaluation which is used during run time
		 */
		String attName = (String) inStack[0];
		String attValue = (String) inStack[1];
		
		IdentityEnvironment ienv = getUserCourseEnv().getIdentityEnvironment();
		Identity ident = ienv.getIdentity();
		Map<String, String> attributes = ienv.getAttributes();
		if (attributes == null) return ConditionInterpreter.INT_FALSE;
		String value = attributes.get(attName);
		
		boolean match = false;
		boolean debug = log.isDebug();
		if (debug) {
			log.debug("value    : " + value);
			log.debug("attrValue: " + attValue);
			log.debug("fT       :  " + functionType);
		}
		if (value != null) {
			if (functionType <= FUNCTION_TYPE_IS_NOT_IN_ATTRIBUTE) {
				match = findExpressionInMultiValue(attValue, value, functionType);
			} else if (functionType == FUNCTION_TYPE_HAS_NOT_ATTRIBUTE) {
				match = !findExpressionInMultiValue(attValue, value, FUNCTION_TYPE_HAS_ATTRIBUTE);
			}
		}		

		if (debug) {
			log.debug("identity '" + ident.getKey() + "' tested on attribute '" + attName + "' to have value '" +
					attValue + "' user's value was '" + value + "', match=" + match);
		}
		return match ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE;
	}

	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}

}
