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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.nodes.co.CORunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Description:<BR/> Course node of type contact form. Can be used to display
 * an email form that has a preconfigured email address. <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author Dirk Furrer
 */
public class COCourseNode extends AbstractAccessableCourseNode {
    private static final String PACKAGE = Util.getPackageName(COCourseNode.class);

	public static final String TYPE = "co";

	public COCourseNode() {
		super(TYPE);
	}

    @Override
    public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
        COEditController childTabCntrllr = new COEditController(getModuleConfiguration(), ureq, wControl, euce);
        CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
        return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
    }

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
            UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
        Controller controller;
        // Do not allow guests to send anonymous emails
        Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
        } else if(userCourseEnv.isCourseReadOnly()) {
            Translator trans = Util.createPackageTranslator(COCourseNode.class, ureq.getLocale());
            String title = trans.translate("freezenoaccess.title");
            String message = trans.translate("freezenoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            controller = new CORunController(this, ureq, wControl, userCourseEnv);
        }
        Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_co_icon");
        return new NodeRunConstructionResult(ctrl);
    }
    
    @Override
    public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
        super.postCopy(envMapper, processType, course, sourceCourse, context);
        postImportCopy(envMapper);
    }
    
    @Override
    public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
        super.postImport(importDirectory, course, envMapper, processType);
        postImportCopy(envMapper);
    }
    
    private void postImportCopy(CourseEnvironmentMapper envMapper) {
        ModuleConfiguration mc = getModuleConfiguration();
        //remap group keys
        List<Long> coachesGroupKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID, Long.class);
        if(coachesGroupKeys == null || coachesGroupKeys.isEmpty()) {
            String coachesGroupNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP);
            coachesGroupKeys = envMapper.toGroupKeyFromOriginalNames(coachesGroupNames);
        } else {
            coachesGroupKeys = envMapper.toGroupKeyFromOriginalKeys(coachesGroupKeys);
        }
        mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID, coachesGroupKeys);

        List<Long> participantsGroupKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID, Long.class);
        if(participantsGroupKeys == null || participantsGroupKeys.isEmpty()) {
            String participantsGroupNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP);
            participantsGroupKeys = envMapper.toGroupKeyFromOriginalNames(participantsGroupNames);
        } else {
            participantsGroupKeys = envMapper.toGroupKeyFromOriginalKeys(participantsGroupKeys);
        }
        mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID, participantsGroupKeys);
        
        //remap area keys
        String coachesAreaNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
        List<Long> coachesAreaKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS, Long.class);
        if(coachesAreaKeys == null || coachesAreaKeys.isEmpty()) {
            coachesAreaKeys = envMapper.toAreaKeyFromOriginalNames(coachesAreaNames);
        } else {
            coachesAreaKeys = envMapper.toAreaKeyFromOriginalKeys(coachesAreaKeys);
        }
        mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS, coachesAreaKeys);
        
        String participantsAreaNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA);
        List<Long> participantsAreaKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID, Long.class);
        if(participantsAreaKeys == null || participantsAreaKeys.isEmpty()) {
            participantsAreaKeys = envMapper.toAreaKeyFromOriginalNames(participantsAreaNames);
        } else {
            participantsAreaKeys = envMapper.toAreaKeyFromOriginalKeys(participantsAreaKeys);
        }
        mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID, participantsAreaKeys);
        
        //remap the deprecated emailGroupsIds
        List<Long> deprecatedGroupKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS, Long.class);
        if(deprecatedGroupKeys != null && !deprecatedGroupKeys.isEmpty()) {
        	deprecatedGroupKeys = envMapper.toGroupKeyFromOriginalKeys(deprecatedGroupKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS, deprecatedGroupKeys);
        }
        
        //remap the deprecated emailAreasIds
        List<Long> deprecatedAreaKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOAREA_IDS, Long.class);
        if(deprecatedAreaKeys != null && !deprecatedAreaKeys.isEmpty()) {
        	deprecatedAreaKeys = envMapper.toAreaKeyFromOriginalKeys(deprecatedAreaKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOAREA_IDS, deprecatedAreaKeys);
        }
    }
    
	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		
		ModuleConfiguration mc = getModuleConfiguration();
		mc.set("emailToGroupCoachesIds", null);
		mc.set("emailToGroupCoaches", null);

		mc.set("emailToGroupParticipantsIds", null);
		mc.set("emailToGroupParticipants", null);

		mc.set("emailToAreaCoachesIds", null);
		mc.set("emailToAreaCoaches", null);

		mc.set("emailToAreaParticipantsIds", null);
		mc.set("emailToAreaParticipants", null);
	}

    @Override
    public StatusDescription isConfigValid() {
        if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

        /**
         * configuration is valid if the provided e-mail container result in at list
         * one recipient e-mail adress. Hence we have always to perform the very
         * expensive operation to fetch the e-mail adresses for tutors,
         * participants, group and area members. simple config here!
         */
        @SuppressWarnings("unchecked")
        List<String> emailList = (List<String>) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
        boolean isValid = (emailList != null && !emailList.isEmpty());
        Boolean email2owners = getModuleConfiguration().getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOOWNERS);
        isValid = isValid || (email2owners != null && email2owners.booleanValue());
        String email2AreaCoaches = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
        isValid = isValid || (StringHelper.containsNonWhitespace(email2AreaCoaches));
        String email2GroupCoaches = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP);
        isValid = isValid || (StringHelper.containsNonWhitespace(email2GroupCoaches));
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL, false);
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE, false);
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_ASSIGNED, false);
        String email2AreaParticipants = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA);
        isValid = isValid || (StringHelper.containsNonWhitespace(email2AreaParticipants));
        String email2GroupParticipants = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP);
        isValid = isValid || (StringHelper.containsNonWhitespace(email2GroupParticipants));
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL, false);
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE, false);

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

    @Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
        oneClickStatusCache = null;
        // only here we know which translator to take for translating condition
        // error messages
        String translatorStr = Util.getPackageName(ConditionEditController.class);
        List<StatusDescription> condErrs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
        List<StatusDescription> missingNames = new ArrayList<>();
        /*
         * check group and area names for existence
         */
        String nodeId = getIdent();
        ModuleConfiguration mc = getModuleConfiguration();

        @SuppressWarnings("unchecked")
        List<Long> areaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS);
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
            String areaStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
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
        List<Long> groupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID);
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
            String groupStr = (String) mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP);
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

    @Override
	public RepositoryEntry getReferencedRepositoryEntry() {
        return null;
    }

    @Override
	public boolean needsReferenceToARepositoryEntry() {
        return false;
    }

    @Override
    public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
        ModuleConfiguration mc = getModuleConfiguration();
        int version = mc.getConfigurationVersion();
        
        /*
         * if no version was set before -> version is 1
         */
        if (version <= 2) {
            //check for deprecated Configs
            if(mc.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES)){
                mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP, mc.get(COEditController.CONFIG_KEY_EMAILTOGROUPS));
                mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID, mc.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS));
                mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA, mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS));
                mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS, mc.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS));
            } 
            if(mc.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS)){
                mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP, mc.get(COEditController.CONFIG_KEY_EMAILTOGROUPS));
                mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID, mc.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS));
                mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA, mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS));
                mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID, mc.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS));
            }
            
            // new keys and defaults are
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOOWNERS, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE, false);
            mc.setConfigurationVersion(3);
        }
        
        if(isNewNode){     
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES_ASSIGNED, false);
            mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID, new ArrayList<Long>());
            mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP, null);
            mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA, null);
            mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS, new ArrayList<Long>());
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL, false);
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE, false);
            mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP, null);
            mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID, new ArrayList<Long>());
            mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA, null);
            mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID, new ArrayList<Long>());
            mc.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOOWNERS, false);
        }
    }
}