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
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
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

	public IsAssessmentModeFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if(inStack != null && inStack.length == 2) {
				if (!(inStack[0] instanceof String)) {
					return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
							"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
				}
				
				String nodeId = (String) inStack[0];
				if (!cev.existsNode(nodeId)) {
					return handleException( new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, nodeId,
						"error.notfound.coursenodeid", "solution.copypastenodeid"));
				}
			}

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

		boolean open = false;
		if(inStack != null && inStack.length == 2) {
			String nodeId = (String) inStack[0];
			open = isAssessmentModeActive(lockedResource) || isScoreUserVisible(nodeId);
		} else {
			open = isAssessmentModeActive(lockedResource);
		}
		return open ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
	}
	
	public boolean isScoreUserVisible(String childId) {
		ScoreAccounting sa = getUserCourseEnv().getScoreAccounting();
		CourseNode foundNode = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(childId);
		if (foundNode == null) {
			return Boolean.FALSE;
		}
		ScoreEvaluation se = sa.evalCourseNode(foundNode);
		if (se == null) {
			return Boolean.FALSE;
		}
		// check if the results are visible
		return se.getUserVisible() != null && se.getUserVisible().booleanValue();
	}
	
	private boolean isAssessmentModeActive(OLATResourceable lockedResource) {
		Long resourceableId = getUserCourseEnv().getCourseEnvironment().getCourseResourceableId();
		if(lockedResource != null && lockedResource.getResourceableId().equals(resourceableId)) {
			RepositoryEntry entry = getUserCourseEnv().getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			AssessmentModeManager assessmentModeMgr = CoreSpringFactory.getImpl(AssessmentModeManager.class);
			return assessmentModeMgr.isInAssessmentMode(entry, new Date());
		}
		return false;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}