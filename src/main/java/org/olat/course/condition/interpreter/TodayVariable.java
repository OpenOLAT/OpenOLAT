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


import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * Function to get the date of today 0:00 without hour, minute or seconds, just the day
 * <P>
 * Initial Date:  10.12.2013 <br>
 *
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 */
/**
 */
public class TodayVariable extends AbstractVariable {

	public static final String name = "today";

	/**
	 * Default constructor to use the current day
	 * @param userCourseEnv
	 */
	public TodayVariable(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}
	
	/**
	 * @see com.neemsoft.jmep.VariableCB#getValue()
	 */
	public Object getValue() {
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if(cev!=null) {
			return new Double(0);
		}
		CourseEnvironment ce = getUserCourseEnv().getCourseEnvironment();
		long time = ce.getCurrentTimeMillis();
		Date date = new Date(time);
		Date day = DateUtils.truncate(date, Calendar.DATE);
		Double dDay = new Double(day.getTime());
		return dDay;
	}

}
