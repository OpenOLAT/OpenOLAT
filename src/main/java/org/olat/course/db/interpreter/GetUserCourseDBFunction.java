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

package org.olat.course.db.interpreter;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.condition.interpreter.ArgumentParseException;
import org.olat.course.db.CourseDBEntry;
import org.olat.course.db.CourseDBManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GetUserCourseDBFunction extends AbstractFunction {
	
	private static final Logger log = Tracing.createLoggerFor(GetUserCourseDBFunction.class);

	public static final String name = "getUserCourseDBValue";

	/**
	 * Constructor
	 * @param userCourseEnv
	 */
	public GetUserCourseDBFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 2) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.providetwo.attrvalue"));
		} else if (inStack.length < 1) { 
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name,
				"", "error.moreargs", "solution.providetwo.attrvalue")); 
		}
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) { 
			return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.attributename", "solution.example.name.infunction")); 
		}
		
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return empty string to continue with condition evaluation test
			return defaultValue();
		}
		
		CourseDBManager courseDbManager = CoreSpringFactory.getImpl(CourseDBManager.class);

		Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		
		String category = null;
		String key = null;
		if(inStack.length == 1) {
			category = null;
			key = (String) inStack[1];
		} else if (inStack.length == 2) {
			category = (String) inStack[0];
			key = (String) inStack[1];
		}
		
		Long courseId = getUserCourseEnv().getCourseEnvironment().getCourseResourceableId();
		Object value;
		try {
			CourseDBEntry entry = courseDbManager.getValue(courseId, ident, category, key);
			if(entry == null) {
				return defaultValue();
			}
			value = entry.getValue();
			if(value == null) {
				return defaultValue();
			}
		} catch (Exception e) {
			log.error("", e);
			return defaultValue();
		}
		return value.toString();
	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#defaultValue()
	 */
	@Override
	protected Object defaultValue() {
		return "";
	}
	
}