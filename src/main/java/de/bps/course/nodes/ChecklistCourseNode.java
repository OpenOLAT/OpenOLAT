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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * Checklist Course Node
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 * @author skoeber <skoeber@bps-system.de>
 */
public class ChecklistCourseNode extends AbstractAccessableCourseNode {
	private static final long serialVersionUID = -8978938639489414749L;
	
	private static final String TYPE = "cl";
	public static final String CONF_COURSE_ID = "cl_course_id";
	public static final String CONF_COURSE_NODE_ID = "cl_course_node_id";
	public static final String CONF_CHECKLIST = "cl_checklist";
	public static final String CONF_CHECKLIST_COPY = "cl_checklist_copy";
	public static final String PROPERTY_CHECKLIST_KEY = CONF_CHECKLIST;

	public ChecklistCourseNode() {
		super(TYPE);
		initDefaultConfig();
	}
	
	private void initDefaultConfig() {
		//
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, null);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller controller = TitledWrapperHelper.getWrapper(ureq, wControl, null, userCourseEnv, this, "o_cl_icon");
		return new NodeRunConstructionResult(controller);
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		return new StatusDescription[] { StatusDescription.NOERROR };
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }
		return StatusDescription.NOERROR;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
}
