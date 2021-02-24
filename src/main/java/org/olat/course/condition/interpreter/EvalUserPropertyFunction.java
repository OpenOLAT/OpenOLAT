/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.condition.interpreter;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.GenericSelectionPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Class to collect the different possible condition functions on the value of a userproperty
 *
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class EvalUserPropertyFunction extends AbstractFunction {

	private static final Logger log = Tracing.createLoggerFor(EvalUserPropertyFunction.class);

	// user must have exactly this expression in one of the values of given property
	public static final int FUNCTION_TYPE_HAS_PROPERTY = 0;
	public static final String FUNCTION_NAME_HAS_PROPERTY = "hasUserProperty";
	// one of the values of given property must start with the given expression
	public static final int FUNCTION_TYPE_PROPERTY_STARTS_WITH = 1;
	public static final String FUNCTION_NAME_PROPERTY_STARTS_WITH = "userPropertyStartswith";
	// one of the values of given property must end with the given expression
	public static final int FUNCTION_TYPE_PROPERTY_ENDS_WITH = 2;
	public static final String FUNCTION_NAME_PROPERTY_ENDS_WITH = "userPropertyEndswith";
	// given expression must be part of one of the values of given property
	public static final int FUNCTION_TYPE_IS_IN_PROPERTY = 3;
	public static final String FUNCTION_NAME_IS_IN_PROPERTY = "isInUserProperty";
	// neither of the values of given property may contain the given expression
	public static final int FUNCTION_TYPE_IS_NOT_IN_PROPERTY = 4;
	public static final String FUNCTION_NAME_IS_NOT_IN_PROPERTY = "isNotInUserProperty";
	// neither of the values of given property may match the given expression
	public static final int FUNCTION_TYPE_HAS_NOT_PROPERTY = 5;
	public static final String FUNCTION_NAME_HAS_NOT_PROPERTY = "hasNotUserProperty";

	private final int functionType;

	/**
	 * @param userCourseEnv
	 * @param functionType the type of function (use defined constants)
	 */
	public EvalUserPropertyFunction(UserCourseEnvironment userCourseEnv, int functionType) {
		super(userCourseEnv);
		this.functionType = functionType;

		if (functionType < 0 | functionType > 5){
			throw new OLATRuntimeException("This function type index is undefined: " + functionType, null);
		}
	}

	/**
	 * Searches the searchValue in the corresponding userValue with the intended method
	 * function
	 * 
	 * @param searchValue
	 *            The expression to search for.
	 * @param userValue
	 *            The property value(s)
	 * @return true if found, false otherwise
	 */
	private boolean checkPropertyValue(UserPropertyHandler propertyHandler, String searchValue, String userValue) {
		switch (functionType) {
			case FUNCTION_TYPE_HAS_PROPERTY:
				return checkHasProperty(propertyHandler, searchValue, userValue);
			case FUNCTION_TYPE_HAS_NOT_PROPERTY:
				return checkHasNotProperty(propertyHandler, searchValue, userValue);
			case FUNCTION_TYPE_PROPERTY_STARTS_WITH:
				return userValue.startsWith(searchValue);
			case FUNCTION_TYPE_PROPERTY_ENDS_WITH:
				return userValue.endsWith(searchValue);
			case FUNCTION_TYPE_IS_IN_PROPERTY:
				return userValue.indexOf(searchValue) > -1;
			case FUNCTION_TYPE_IS_NOT_IN_PROPERTY:
				return userValue.indexOf(searchValue) == -1;
			default:
				return false;
		}
	}
	
	private boolean checkHasProperty(UserPropertyHandler propertyHandler, String searchValue, String userValue) {
		if(propertyHandler instanceof GenericSelectionPropertyHandler && ((GenericSelectionPropertyHandler)propertyHandler).isMultiSelect()) {
			String[] userValues = userValue.split(GenericSelectionPropertyHandler.KEY_DELIMITER);
			for(String val:userValues) {
				if(StringHelper.containsNonWhitespace(val) && val.trim().equals(searchValue)) {
					return true;
				}
			}
			return false;
		}
		return userValue.equals(searchValue);
	}
	
	private boolean checkHasNotProperty(UserPropertyHandler propertyHandler, String searchValue, String userValue) {
		if(propertyHandler instanceof GenericSelectionPropertyHandler && ((GenericSelectionPropertyHandler)propertyHandler).isMultiSelect()) {
			String[] userValues = userValue.split(GenericSelectionPropertyHandler.KEY_DELIMITER);
			for(String val:userValues) {
				if(StringHelper.containsNonWhitespace(val) && val.trim().equals(searchValue)) {
					return false;
				}
			}
			return true;
		}
		return !userValue.equals(searchValue);
	}

	@Override
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 2) {
			String name = getFunctionName(functionType);
			return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.providetwo.attrvalue"));
		} else if (inStack.length < 2) {
			String name = getFunctionName(functionType);
			return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.providetwo.attrvalue"));
		}
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) {
			String name = getFunctionName(functionType);
			return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.attributename", "solution.example.name.infunction"));
		}
		if (!(inStack[1] instanceof String)){
			String name = getFunctionName(functionType);
			return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.attribvalue", "solution.example.name.infunction"));
		}else {
			String propValue = (String) inStack[1];
			if(!StringHelper.containsNonWhitespace(propValue)){
				String name = getFunctionName(functionType);
				return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
					"error.argtype.attribvalue", "solution.example.whiteSpace"));
			}
		}
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}

		/*
		 * the real function evaluation which is used during run time
		 */
		String propName = (String) inStack[0];
		String searchValue = (String) inStack[1];

		Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		if(ident == null) {
			return defaultValue();
		}
		User user = ident.getUser();

		String userValue = user.getPropertyOrIdentityEnvAttribute(propName, null);
		

		boolean match = false;
		boolean debug = log.isDebugEnabled();
		if (debug) {
			log.debug("value       : {}", userValue);
			log.debug("searchValue : {}", searchValue);
			log.debug("fT          : {}", functionType);
		}
		if (StringHelper.containsNonWhitespace(userValue)) {
			UserPropertyHandler propHandler = UserManager.getInstance().getUserPropertiesConfig()
					.getPropertyHandler(propName);
			match = checkPropertyValue(propHandler, searchValue, userValue);
		}

		if (debug) {
			log.debug("identity '{}' tested on properties '{}' to have value '{}' user's value was '{}', match={}",
					ident.getKey(), propName, searchValue, userValue, match);
		}
		return match ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_FALSE;
	}

	private String getFunctionName(int type){
		switch (type) {
		case FUNCTION_TYPE_HAS_PROPERTY:
			return FUNCTION_NAME_HAS_PROPERTY;
			
		case FUNCTION_TYPE_HAS_NOT_PROPERTY:
			return FUNCTION_NAME_HAS_NOT_PROPERTY;
			
		case FUNCTION_TYPE_PROPERTY_STARTS_WITH:
			return FUNCTION_NAME_PROPERTY_STARTS_WITH;
			
		case FUNCTION_TYPE_PROPERTY_ENDS_WITH:
			return FUNCTION_NAME_PROPERTY_ENDS_WITH;

		case FUNCTION_TYPE_IS_IN_PROPERTY:
			return FUNCTION_NAME_IS_IN_PROPERTY;

		case FUNCTION_TYPE_IS_NOT_IN_PROPERTY:
			return FUNCTION_NAME_IS_NOT_IN_PROPERTY;

		default:
			return "unknown EvalUserPropertyFunction";
		}
	}

}
