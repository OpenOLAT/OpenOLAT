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

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IsAssessmentModeFunction extends AbstractFunction {
	public static final String name = "isAssessmentMode";

	/**
	 * Constructor
	 * @param userCourseEnv
	 */
	public IsAssessmentModeFunction(UserCourseEnvironment userCourseEnv) {
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
		
		WindowControl wControl = getUserCourseEnv().getWindowControl();
		if(wControl == null) {
			return ConditionInterpreter.INT_FALSE;
		}
		ChiefController chiefController = wControl.getWindowBackOffice().getChiefController();
		if(chiefController == null) {
			return ConditionInterpreter.INT_FALSE;
		}
		OLATResourceable lockedResource = chiefController.getLockResource();
		if(lockedResource == null) {
			return ConditionInterpreter.INT_FALSE;
		}
		
		Long resourceableId = getUserCourseEnv().getCourseEnvironment().getCourseResourceableId();
		if(lockedResource.getResourceableId().equals(resourceableId)) {
			RepositoryEntry entry = getUserCourseEnv().getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			AssessmentModeManager assessmentModeMgr = CoreSpringFactory.getImpl(AssessmentModeManager.class);
			boolean inAssessment = assessmentModeMgr.isInAssessmentMode(entry, new Date());
			return inAssessment ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
		} else {
			return ConditionInterpreter.INT_FALSE;
		}
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}