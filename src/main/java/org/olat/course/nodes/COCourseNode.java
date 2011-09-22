/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.nodes.co.CORunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<BR/> Course node of type contact form. Can be used to display
 * an email form that has a preconfigured email address. <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 */
public class COCourseNode extends AbstractAccessableCourseNode {
	private static final String PACKAGE = Util.getPackageName(COCourseNode.class);

	private static final String TYPE = "co";

	/**
	 * Default constructor for course node of type single page
	 */
	public COCourseNode() {
		super(TYPE);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce) {
		COEditController childTabCntrllr = new COEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment()
				.getCourseGroupManager(), euce, childTabCntrllr);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		Controller controller;
		// Do not allow guests to send anonymous emails
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new CORunController(getModuleConfiguration(), ureq, wControl, userCourseEnv, this);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_co_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		/**
		 * configuration is valid if the provided e-mail container result in at list
		 * one recipient e-mail adress. Hence we have always to perform the very
		 * expensive operation to fetch the e-mail adresses for tutors,
		 * participants, group and area members. simple config here!
		 */
		List emailList = (List) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
		boolean isValid = (emailList != null && emailList.size() > 0);
		Boolean email2coaches = getModuleConfiguration().getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES);
		Boolean email2partips = getModuleConfiguration().getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS);
		isValid = isValid || (email2coaches != null && email2coaches.booleanValue());
		isValid = isValid || (email2partips != null && email2partips.booleanValue());
		String email2Areas = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		isValid = isValid || (!"".equals(email2Areas) && email2Areas != null);
		String email2Groups = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		isValid = isValid || (!"".equals(email2Groups) && email2Groups != null);
		//
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			String shortKey = "error.norecipients.short";
			String longKey = "error.norecipients.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(COEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(COEditController.PANE_TAB_COCONFIG);
		}
		return sd;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List condErrs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		List missingNames = new ArrayList();
		/*
		 * check group and area names for existence
		 */
		ModuleConfiguration mc = getModuleConfiguration();
		String areaStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		String nodeId = getIdent();
		if (areaStr != null) {
			String[] areas = areaStr.split(",");
			for (int i = 0; i < areas.length; i++) {
				String trimmed = areas[i] != null ? areas[i].trim() : areas[i];
				if (!trimmed.equals("") && !cev.existsArea(trimmed)) {
					StatusDescription sd = new StatusDescription(StatusDescription.WARNING, "error.notfound.name", "solution.checkgroupmanagement",
							new String[] { "NONE", trimmed }, translatorStr);
					sd.setDescriptionForUnit(nodeId);
					missingNames.add(sd);
				}
			}
		}
		String groupStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		if (groupStr != null) {
			String[] groups = groupStr.split(",");
			for (int i = 0; i < groups.length; i++) {
				String trimmed = groups[i] != null ? groups[i].trim() : groups[i];
				if (!trimmed.equals("") && !cev.existsGroup(trimmed)) {
					StatusDescription sd = new StatusDescription(StatusDescription.WARNING, "error.notfound.name", "solution.checkgroupmanagement",
							new String[] { "NONE", trimmed }, translatorStr);
					sd.setDescriptionForUnit(nodeId);
					missingNames.add(sd);
				}
			}
		}
		missingNames.addAll(condErrs);
		oneClickStatusCache = StatusDescriptionHelper.sort(missingNames);
		return oneClickStatusCache;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

}
