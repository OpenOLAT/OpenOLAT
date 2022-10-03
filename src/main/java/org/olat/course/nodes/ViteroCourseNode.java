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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.vitero.ViteroEditController;
import org.olat.course.nodes.vitero.ViteroPeekViewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.ui.ViteroBookingsRunController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 8680935159748506305L;

	public static final String TYPE = "vitero";

	// configuration
	public static final String CONF_VC_CONFIGURATION = "vc_configuration";

	public ViteroCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			UserCourseEnvironment userCourseEnv) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// create edit controller
		ViteroEditController childTabCntrllr = new ViteroEditController(ureq, wControl, this, course, userCourseEnv);
		
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode,
				userCourseEnv, childTabCntrllr);
		nodeEditCtr.addControllerListener(childTabCntrllr);
		return nodeEditCtr;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller runCtr;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			runCtr = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else {
			// check if user is moderator of the virtual classroom
			boolean moderator = userCourseEnv.isAdmin();
			// create run controller
			Long resourceId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, resourceId);
			String courseTitle = userCourseEnv.getCourseEnvironment().getCourseTitle();
			runCtr = new ViteroBookingsRunController(ureq, wControl, null, ores, getIdent(), courseTitle, moderator, userCourseEnv.isCourseReadOnly());
		}
		Controller controller = TitledWrapperHelper.getWrapper(ureq, wControl, runCtr, userCourseEnv, this, "o_vitero_icon");
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		return new ViteroPeekViewController(ureq, wControl, userCourseEnv, getIdent());
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
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
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// load configuration
		ViteroManager provider = CoreSpringFactory.getImpl(ViteroManager.class);
		// remove meeting
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(course.getResourceableTypeName(), course.getResourceableId());
		provider.deleteAll(null, ores, getIdent());
	}
}