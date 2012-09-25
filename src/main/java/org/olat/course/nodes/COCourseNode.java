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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.StackedController;
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
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.nodes.co.CORunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
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
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, StackedController stackPanel, ICourse course, UserCourseEnvironment euce) {
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
	
	@Override
	public void postImport(CourseEnvironmentMapper envMapper) {
		super.postImport(envMapper);
		
		ModuleConfiguration mc = getModuleConfiguration();
		String groupNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
		if(groupKeys == null) {
			groupKeys = envMapper.toGroupKeyFromOriginalNames(groupNames);
		} else {
			groupKeys = envMapper.toGroupKeyFromOriginalKeys(groupKeys);
		}
		mc.set(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS, groupKeys);

		String areaNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		if(areaKeys == null) {
			areaKeys = envMapper.toAreaKeyFromOriginalNames(areaNames);
		} else {
			areaKeys = envMapper.toAreaKeyFromOriginalKeys(areaKeys);
		}
		mc.set(COEditController.CONFIG_KEY_EMAILTOAREA_IDS, areaKeys);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		
		ModuleConfiguration mc = getModuleConfiguration();
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
		if(groupKeys != null) {
			String groupNames = envMapper.toGroupNames(groupKeys);
			mc.set(COEditController.CONFIG_KEY_EMAILTOGROUPS, groupNames);
		}

		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		if(areaKeys != null) {
			String areaNames = envMapper.toAreaNames(areaKeys);	
			mc.set(COEditController.CONFIG_KEY_EMAILTOAREAS, areaNames);
		}
		
		if(backwardsCompatible) {
			mc.remove(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
			mc.remove(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		}
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
		@SuppressWarnings("unchecked")
		List<String> emailList = (List<String>) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
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
		List<StatusDescription> condErrs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		List<StatusDescription> missingNames = new ArrayList<StatusDescription>();
		/*
		 * check group and area names for existence
		 */
		String nodeId = getIdent();
		ModuleConfiguration mc = getModuleConfiguration();

		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		if(areaKeys != null) {
			BGAreaManager areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
			List<BGArea> areas = areaManager.loadAreas(areaKeys);

			a_a:
			for(Long areaKey:areaKeys) {
				for(BGArea area:areas) {
					if(area.getKey().equals(areaKey)) {
						continue a_a;
					}
				}
				
				StatusDescription sd = new StatusDescription(StatusDescription.WARNING, "error.notfound.name", "solution.checkgroupmanagement",
						new String[] { "NONE", areaKey.toString() }, translatorStr);
				sd.setDescriptionForUnit(nodeId);
				missingNames.add(sd);
			}
		} else {
			String areaStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
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
		}

		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
		if(groupKeys != null) {
			BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			List<BusinessGroupShort> groups = bgs.loadShortBusinessGroups(groupKeys);
			
			a_a:
			for(Long activeGroupKey:groupKeys) {
				for(BusinessGroupShort group:groups) {
					if(group.getKey().equals(activeGroupKey)) {
						continue a_a;
					}
				}
				
				StatusDescription sd = new StatusDescription(StatusDescription.WARNING, "error.notfound.name", "solution.checkgroupmanagement",
						new String[] { "NONE", activeGroupKey.toString() }, translatorStr);
				sd.setDescriptionForUnit(nodeId);
				missingNames.add(sd);
			}
		} else {
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
