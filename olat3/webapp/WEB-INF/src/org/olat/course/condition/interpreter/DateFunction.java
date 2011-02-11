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
* <p>
*/ 

package org.olat.course.condition.interpreter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * @author gnaegi
 */
public class DateFunction extends AbstractFunction {

	public static final String name = "date";
	private Date d;

	/**
	 * Default constructor to use the current date
	 * 
	 * @param userCourseEnv
	 */
	public DateFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		d = new Date();
		/*
		 * argument check
		 */
		if (inStack.length > 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.date"));
		} else if (inStack.length < 1) { return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name,
				"", "error.moreargs", "solution.provideone.date")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.date", "solution.example.date.infunction"));
		String datetime = (String) inStack[0];
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		sdf.setLenient(false);
		try {
			d = sdf.parse(datetime);
		} catch (ParseException pe) {
			return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, datetime, "error.argtype.date",
					"solution.example.date.infunction"));
		}
		/*
		 * the real function evaluation which is used during run time
		 */
		return new Double(d.getTime());
	}

	protected Object defaultValue() {
		return new Double(d.getTime());
	}

}
