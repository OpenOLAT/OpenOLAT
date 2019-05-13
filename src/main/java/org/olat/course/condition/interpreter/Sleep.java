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

import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * To use in debug mode to catch concurrency issues
 * 
 * Initial date: 29.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Sleep extends AbstractFunction {
	private static final Logger log = Tracing.createLoggerFor(Sleep.class);
	
	public static final String name = "sleep";

	/**
	 * @param userCourseEnv
	 */
	public Sleep(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		} else if(Settings.isDebuging()) {
			long sleep = 30000;
			if(inStack == null || inStack.length == 0) {
				//stay with default
			} else if(inStack[0] instanceof String) {
				String sleepStr = (String)inStack[0];
				sleep = Long.parseLong(sleepStr);
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
		return ConditionInterpreter.INT_TRUE;
	}

	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}
