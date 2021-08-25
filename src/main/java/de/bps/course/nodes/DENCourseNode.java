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

import java.util.ArrayList;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.bps.course.nodes.den.DENEditController;
import de.bps.course.nodes.den.DENManager;
import de.bps.course.nodes.den.DENRunController;

/**
 * Date enrollment course node
 * @author skoeber
 */
public class DENCourseNode extends AbstractAccessableCourseNode {

	public static final String TYPE = "den";
	/** is cancel of the enrollment allowed */
	public static final String CONF_CANCEL_ENROLL_ENABLED = "cancel_enroll_enabled";
	public static final String CONF_COURSE_ID = "den_course_id";
	public static final String CONF_COURSE_NODE_ID = "den_course_node_id";

	public DENCourseNode() {
		super(TYPE);
		initDefaultConfig();
	}

	private void initDefaultConfig() {
		ModuleConfiguration config = getModuleConfiguration();
		config.set(CONF_CANCEL_ENROLL_ENABLED, Boolean.TRUE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq,
			WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment userCourseEnv) {
		DENEditController childTabCntrllr = new DENEditController(getModuleConfiguration(), ureq, wControl, this, course, userCourseEnv);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// needed for DENEditController.isConfigValid()
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, userCourseEnv, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		// Do not allow guests to enroll to dates
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(CourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new DENRunController(ureq, wControl, getModuleConfiguration(), userCourseEnv, this);
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_en_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;

		if(!DENEditController.isConfigValid(getModuleConfiguration())) {
			String transPackage = Util.getPackageName(DENEditController.class);
			sd = new StatusDescription(StatusDescription.WARNING, "config.nodates.short", "config.nodates.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(DENEditController.PANE_TAB_DENCONFIG);
		}

		return sd;
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
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		cpm.deleteNodeProperties(this, CONF_CANCEL_ENROLL_ENABLED);
		DENManager denManager = DENManager.getInstance();
		//empty List as first argument, so all dates for this course node are going to delete
		denManager.persistDENSettings(new ArrayList<KalendarEvent>(), course, this);
	}

}
