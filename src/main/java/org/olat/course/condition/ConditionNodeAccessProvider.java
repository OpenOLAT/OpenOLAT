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
package org.olat.course.condition;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.CoursePaginationController;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
@Order(10)
public class ConditionNodeAccessProvider implements NodeAccessProvider {
	
	public static String TYPE = "condition";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(ConditionNodeAccessProvider.class, locale);
		return translator.translate("access.provider.name");
	}
	
	@Override
	public String getToolTipHelpText(Locale locale) {
		Translator translator = Util.createPackageTranslator(ConditionNodeAccessProvider.class, locale);
		return translator.translate("access.provider.toolTip");
	}

	@Override
	public boolean isSupported(String courseNodeType) {
		return true;
	}

	@Override
	public boolean isGuestSupported() {
		return true;
	}

	@Override
	public boolean isConditionExpressionSupported() {
		return true;
	}

	@Override
	public boolean isScoreCalculatorSupported() {
		return true;
	}

	@Override
	public void updateConfigDefaults(CourseNode courseNode, boolean newNode) {
		//
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnv, CourseEditorTreeModel editorModel) {
		if (courseNode instanceof AbstractAccessableCourseNode) {
			AbstractAccessableCourseNode acccessableCourseNode = (AbstractAccessableCourseNode) courseNode;
			ConditionAccessEditConfig accessEditConfig = acccessableCourseNode.getAccessEditConfig();
			if (!accessEditConfig.isCustomAccessConditionController()) {
				return new TabbableConditionNodeConfigController(ureq, wControl, courseNode, userCourseEnv, editorModel,
						accessEditConfig);
			}
		}
		return null;
	}

	@Override
	public String getCourseTreeCssClass() {
		return "";
	}

	@Override
	public CoursePaginationController getCoursePaginationController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		return new ConditionCourseTreeModelBuilder(userCourseEnv);
	}

	@Override
	public boolean onNodeVisited(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		return false;
	}

	@Override
	public boolean isAssessmentConfirmationEnabled(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		return false;
	}

	@Override
	public void onAssessmentConfirmed(CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean confirmed) {
		// nothing to do
	}

	@Override
	public void onScoreUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Float score,
			Boolean userVisibility) {
		// nothing to do
	}
	@Override
	public void onPassedUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Boolean passed, Boolean userVisibility) {
		// nothing to do
	}

	@Override
	public void onStatusUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			AssessmentEntryStatus status) {
		// nothing to do
	}
	
}
