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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.en.ENEditController;
import org.olat.course.nodes.en.ENRunController;
import org.olat.course.nodes.en.EnrollmentManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.resource.OLATResource;

/**
 * Description:<BR>
 * Enrollement Course Node: users can enroll in group / groups / areas
 * <P>
 * 
 * Initial Date: Sep 8, 2004
 * @author Felix Jost, Florian Gnaegi
 */
public class ENCourseNode extends AbstractAccessableCourseNode {
	private static final String PACKAGE = Util.getPackageName(ENCourseNode.class);
	private static final String PACKAGE_COND = Util.getPackageName(ConditionEditController.class);
	
	public static final String TYPE = "en";

	/**
	 * property name for the initial enrollment date will be set only the first
	 * time the users enrolls to this node.
	 */
	public static final String PROPERTY_INITIAL_ENROLLMENT_DATE = "initialEnrollmentDate";
	/**
	 * property name for the recent enrollemtn date will be changed everytime the
	 * user enrolls to this node.
	 */
	public static final String PROPERTY_RECENT_ENROLLMENT_DATE = "recentEnrollmentDate";

	/**
	 * property name for the initial waiting-list date will be set only the first
	 * time the users is put into the waiting-list of this node.
	 */
	public static final String PROPERTY_INITIAL_WAITINGLIST_DATE = "initialWaitingListDate";
	/**
	 * property name for the recent waiting-list date will be changed everytime the
	 * user is put into the waiting-list of this node.
	 */
	public static final String PROPERTY_RECENT_WAITINGLIST_DATE = "recentWaitingListDate";

	/** CONFIG_GROUPNAME configuration parameter key. */
	public static final String CONFIG_GROUPNAME = "groupname";
	/** CONFIG_GROUPNAME configuration parameter key. */
	public static final String CONFIG_GROUP_IDS = "groupkeys";
	/** CONFIG_GROUPNAME configuration parameter key  */
	public static final String CONFIG_GROUP_SORTED = "groupsort";
	
	/** CONFIG_AREANAME configuration parameter key. */
	public static final String CONFIG_AREANAME = "areaname";
	/** CONFIG_AREANAME configuration parameter key. */
	public static final String CONFIG_AREA_IDS = "areakeys";
	
	/** CONFIG_ALLOW_MULTIPLE_ENTROLL_COUNT configuration parameter */
	public static final String CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT = "allow_multiple_enroll_count";
	
	/** CONF_CANCEL_ENROLL_ENABLED configuration parameter key. */
	public static final String CONF_CANCEL_ENROLL_ENABLED = "cancel_enroll_enabled";

	private static final int CURRENT_CONFIG_VERSION = 3;

	public ENCourseNode() {
		super(TYPE);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		migrateConfig();
		ENEditController childTabCntrllr = new ENEditController(getModuleConfiguration(), ureq, wControl, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		migrateConfig();
		// Do not allow guests to enroll to groups
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new ENRunController(getModuleConfiguration(), ureq, wControl, userCourseEnv, this);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_en_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	public boolean isUsedForEnrollment(List<BusinessGroup> groups, OLATResource courseResource) {
		if(groups == null || groups.isEmpty()) return false;
		
		ModuleConfiguration mc = getModuleConfiguration();
		String groupNames = (String) mc.get(CONFIG_GROUPNAME);
		List<Long> groupKeys = mc.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		if(groupKeys != null && groupKeys.size() > 0) {
			for(BusinessGroup group:groups) {
				if(groupKeys.contains(group.getKey())) {
					return true;
				}
			}
		} else if(StringHelper.containsNonWhitespace(groupNames)) {
			String[] groupNameArr = groupNames.split(",");
			for(BusinessGroup group:groups) {
				for(String groupName:groupNameArr) {
					if(groupName != null && group.getName() != null && groupName.equals(group.getName())) {
						return true;
					}
				}
			}
		}
		
		List<Long> areaKeys = mc.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
		if(areaKeys == null || areaKeys.isEmpty()) {
			String areaNames = (String) mc.get(CONFIG_AREANAME);
			areaKeys = CoreSpringFactory.getImpl(BGAreaManager.class).toAreaKeys(areaNames, courseResource);
		}
		if(areaKeys != null && areaKeys.size() > 0) {
			List<Long> areaGroupKeys = CoreSpringFactory.getImpl(BGAreaManager.class).findBusinessGroupKeysOfAreaKeys(areaKeys);
			for(BusinessGroup group:groups) {
				if(areaGroupKeys.contains(group.getKey())) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		boolean isValid = ENEditController.isConfigValid(getModuleConfiguration());
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			// FIXME: refine statusdescriptions
			String shortKey = "error.nogroupdefined.short";
			String longKey = "error.nogroupdefined.long";
			String[] params = new String[] { getShortTitle() };
			String translPackage = Util.getPackageName(ENEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(ENEditController.PANE_TAB_ENCONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		// this must be nulled before isConfigValid() is called!!
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages

		List<StatusDescription> condErrs = isConfigValidWithTranslator(cev, PACKAGE_COND, getConditionExpressions());
		List<StatusDescription> missingNames = new ArrayList<>();
		/*
		 * check group and area names for existence
		 */
		
		ModuleConfiguration mc = getModuleConfiguration();
		String areaNames = (String) mc.get(CONFIG_AREANAME);
		List<Long> areaKeys = mc.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
		List<String> missingAreas = getMissingAreas(areaKeys, areaNames, cev);
		if(missingAreas.size() > 0) {
			missingNames.add(addStatusErrorMissing(missingAreas));
		}
		
		String groupNames = (String) mc.get(CONFIG_GROUPNAME);
		List<Long> groupKeys = mc.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		List<String> missingGroups = getMissingBusinessGroups(groupKeys, groupNames, cev);
		if(missingGroups.size() > 0) {
			missingNames.add(addStatusErrorMissing(missingGroups));
		}
		
		missingNames.addAll(condErrs);
		/*
		 * sort -> Errors > Warnings > Infos and remove NOERRORS, if
		 * Error/Warning/Info around.
		 */
		oneClickStatusCache = StatusDescriptionHelper.sort(missingNames);
		return oneClickStatusCache;
	}
	
	private StatusDescription addStatusErrorMissing(List<String> missingObjects) {
		String labelKey = missingObjects.size() == 1 ? "error.notfound.name" : "error.notfound.names";
		StringBuilder missing = new StringBuilder();
		for(String missingObject:missingObjects) {
			if(missing.length() > 0) missing.append(", ");
			missing.append(missingObject);
		}
		
		StatusDescription sd = new StatusDescription(StatusDescription.WARNING, labelKey, "solution.checkgroupmanagement",
				new String[] { "NONE", missing.toString() }, PACKAGE_COND);
		sd.setDescriptionForUnit(getIdent());
		return sd;
	}
	
	public List<String> getMissingAreas(List<Long> areaKeys, String areaNames, CourseEditorEnv cev) {
		List<String> missingNames = new ArrayList<>();
		if(areaKeys == null || areaKeys.isEmpty()) {
			if (areaNames != null) {
				String[] areas = areaNames.split(",");
				for (int i = 0; i < areas.length; i++) {
					String trimmed = areas[i] != null ?
							FilterFactory.getHtmlTagsFilter().filter(areas[i]).trim() : areas[i];
					if (!trimmed.equals("") && !cev.existsArea(trimmed)) {
						missingNames.add(trimmed);
					}
				}
			}
		} else {
			Set<Long> missingAreas = new HashSet<>();
			List<BGArea> existingAreas =  CoreSpringFactory.getImpl(BGAreaManager.class).loadAreas(areaKeys);
			
			List<String> knowNames = new ArrayList<>();
			if (areaNames != null) {
				String[] areas = areaNames.split(",");
				for (int i = 0; i < areas.length; i++) {
					String trimmed = areas[i] != null ? FilterFactory.getHtmlTagsFilter().filter(areas[i]).trim() : areas[i];
					knowNames.add(trimmed);
				}
			}
			
			a_a:
			for(Long areaKey:areaKeys) {
				for(BGArea area:existingAreas) {
					if(area.getKey().equals(areaKey)) {
						String trimmed = area.getName() != null ? FilterFactory.getHtmlTagsFilter().filter(area.getName()).trim() : area.getName();
						knowNames.remove(trimmed);
						continue a_a;
					}
				}
				missingAreas.add(areaKey);
			}
			
			if(missingAreas.size() > 0 ) {
				if(knowNames.size() > 0) {
					missingNames.addAll(knowNames);
				} else {
					for(Long missingArea:missingAreas) {
						missingNames.add(missingArea.toString());
					}
				}
			}
		}
		return missingNames;
	}
	
	public List<String> getMissingBusinessGroups(List<Long> groupKeys, String groupNames, CourseEditorEnv cev) {
		List<String> missingNames = new ArrayList<>();
		if(groupKeys == null || groupKeys.isEmpty()) {
			if (groupNames != null) {
				String[] groups = groupNames.split(",");
				for (int i = 0; i < groups.length; i++) {
					String trimmed = groups[i] != null ?
							FilterFactory.getHtmlTagsFilter().filter(groups[i]).trim() : groups[i];
					if (!trimmed.equals("") && !cev.existsGroup(trimmed)) {
						missingNames.add(trimmed);
					}
				}
			}
		} else {
			Set<Long> missingGroups = new HashSet<>();
			List<BusinessGroupShort> existingGroups =  CoreSpringFactory.getImpl(BusinessGroupService.class).loadShortBusinessGroups(groupKeys);
			
			List<String> knowNames = new ArrayList<>();
			if (groupNames != null) {
				String[] groups = groupNames.split(",");
				for (int i = 0; i < groups.length; i++) {
					String trimmed = groups[i] != null ? FilterFactory.getHtmlTagsFilter().filter(groups[i]).trim() : groups[i];
					knowNames.add(trimmed);
				}
			}
			
			a_a:
			for(Long groupKey:groupKeys) {
				for(BusinessGroupShort group:existingGroups) {
					if(group.getKey().equals(groupKey)) {
						String trimmed = group.getName() != null ? FilterFactory.getHtmlTagsFilter().filter(group.getName()).trim() : group.getName();
						knowNames.remove(trimmed);
						continue a_a;
					}
				}
				missingGroups.add(groupKey);
			}
			
			if(missingGroups.size() > 0 ) {
				if(knowNames.size() > 0) {
					missingNames.addAll(knowNames);
				} else {
					for(Long missingGroup:missingGroups) {
						missingNames.add(missingGroup.toString());
					}
				}
			}
		}
		return missingNames;
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
		cpm.deleteNodeProperties(this, PROPERTY_INITIAL_ENROLLMENT_DATE);
		cpm.deleteNodeProperties(this, PROPERTY_RECENT_ENROLLMENT_DATE);
	}
	
	/**
	 * Init config parameter with default values for a new course node.
	 */
	private void initDefaultConfig() {
		ModuleConfiguration config = getModuleConfiguration();
		// defaults
		config.set(CONF_CANCEL_ENROLL_ENABLED, Boolean.TRUE);
		config.set(CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT,1);
		config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
		config.setBooleanEntry(CONFIG_GROUP_SORTED, false);
	}
	
    @Override
    public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse, CopyCourseContext context) {
    	super.postCopy(envMapper, processType, course, sourceCrourse, context);
	    postImportCopy(envMapper);
	}
    
    @Override	
    public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
    	super.postImport(importDirectory, course, envMapper, processType);
     	postImportCopy(envMapper);
    }
    
	public void postImportCopy(CourseEnvironmentMapper envMapper) {
		ModuleConfiguration mc = getModuleConfiguration();
		String groupNames = (String)mc.get(ENCourseNode.CONFIG_GROUPNAME);
		List<Long> groupKeys = mc.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		if(groupKeys == null || groupKeys.isEmpty()) {
			groupKeys = envMapper.toGroupKeyFromOriginalNames(groupNames);
		} else {
			groupKeys = envMapper.toGroupKeyFromOriginalKeys(groupKeys);
		}
		mc.set(ENCourseNode.CONFIG_GROUP_IDS, groupKeys);
	
		String areaNames = (String)mc.get(ENCourseNode.CONFIG_AREANAME);
		List<Long> areaKeys =  mc.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
		if(areaKeys == null || areaKeys.isEmpty()) {
			areaKeys = envMapper.toAreaKeyFromOriginalNames(areaNames);
		} else {
			areaKeys = envMapper.toAreaKeyFromOriginalKeys(areaKeys);
		}
		mc.set(ENCourseNode.CONFIG_AREA_IDS, areaKeys);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);

		ModuleConfiguration mc = getModuleConfiguration();
		List<Long> groupKeys = mc.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		if(groupKeys != null) {
			String groupNames = envMapper.toGroupNames(groupKeys);
			mc.set(ENCourseNode.CONFIG_GROUPNAME, groupNames);
		}

		List<Long> areaKeys = mc.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
		if(areaKeys != null) {
			String areaNames = envMapper.toAreaNames(areaKeys);
			mc.set(ENCourseNode.CONFIG_AREANAME, areaNames);
		}
		
		if(backwardsCompatible) {
			mc.remove(ENCourseNode.CONFIG_GROUP_IDS);
			mc.remove(ENCourseNode.CONFIG_AREA_IDS);
		}
	}
	
	/**
	 * Migrate (add new config parameter/values) config parameter for a existing course node.
	 */
	private void migrateConfig() {
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		if (version < CURRENT_CONFIG_VERSION) {
			// Loaded config is older than current config version => migrate
			if (version == 1) {
				// migrate V1 => V2
				config.set(CONF_CANCEL_ENROLL_ENABLED, Boolean.TRUE);
				version = 2;
			} 
			if(version <= 2){
				// migrate V2 -> V3
				config.set(CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT, 1);
				version = 3;
			} 
			if(version <= 3) {
				// migrate V3 -> V4
				config.setBooleanEntry(CONFIG_GROUP_SORTED, false);
				version = 4;
			}
			config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
		}
	}
	
	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		EnrollmentManager enrollmentManager = CoreSpringFactory.getImpl(EnrollmentManager.class);
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		List<Identity> assessedUsers = pm.getAllIdentitiesWithCourseAssessmentData(null);
		
		int count = 0;
		for(Identity assessedIdentity: assessedUsers) {
			enrollmentManager.syncAssessmentStatus(course, Collections.singletonList(this), assessedIdentity, publisher);
			if(++count % 10 == 0) {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		DBFactory.getInstance().commitAndCloseSession();
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
}
