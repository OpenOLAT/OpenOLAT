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

import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * A user is a participant coach if he/she is in at least particpant of a learning group of the learning group context of the course
 * or in the participant group of the repository entry
 * 
 * <P>
 * Initial Date:  28 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IsCourseParticipantFunction extends AbstractFunction {
	private static final Logger log = Tracing.createLoggerFor(IsCourseParticipantFunction.class);
	
	public static final String name = "isCourseParticipant";
	
	/**
	 * @param userCourseEnv
	 */
	public IsCourseParticipantFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {
		/*
		 * expression check only if cev != null
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}
		
		boolean isParticipant;
		if(inStack != null && inStack.length > 0
				&& inStack[0] instanceof String
				&& AnyCourseVariable.name.equalsIgnoreCase((String)inStack[0])) {
			//administrator of any course
			isParticipant = getUserCourseEnv().isParticipantOfAnyCourse();
		} else {
			isParticipant = getUserCourseEnv().isParticipant();
		}
		if (log.isDebugEnabled()) {
			Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
			log.debug("identity "+ident.getKey()+", coursecoach:"+isParticipant+", in course "+getUserCourseEnv().getCourseEnvironment().getCourseResourceableId());
		}
		
		return isParticipant ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}

}
