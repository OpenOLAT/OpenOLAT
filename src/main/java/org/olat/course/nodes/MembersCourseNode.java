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
package org.olat.course.nodes;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.info.InfoCourseNodeEditController;
import org.olat.course.nodes.members.MembersCourseNodeEditController;
import org.olat.course.nodes.members.MembersCourseNodeRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;


/**
 * 
 * Description:<br>
 *  The course node show all members of the course
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = -8404722446386415061L;
	
	public static final String TYPE = "cmembers";
	
	public MembersCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public StatusDescription isConfigValid() {
		return StatusDescription.NOERROR;
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		String translatorStr = Util.getPackageName(InfoCourseNodeEditController.class);
		List<StatusDescription> statusDescs =isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, StackedController stackPanel, ICourse course, UserCourseEnvironment euce) {
		MembersCourseNodeEditController childTabCntrllr = new MembersCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment()
				.getCourseGroupManager(), euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		
		MembersCourseNodeRunController infoCtrl = new MembersCourseNodeRunController(ureq, wControl, this, userCourseEnv);
		Controller titledCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, infoCtrl, this, "o_cmembers_icon");
		return new NodeRunConstructionResult(titledCtrl);
	}
}