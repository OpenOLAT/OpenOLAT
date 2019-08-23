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
 * @author Dirk Furrer
 */
public class COCourseNode extends AbstractAccessableCourseNode {
    private static final String PACKAGE = Util.getPackageName(COCourseNode.class);

    private static final String TYPE = "co";

    /**
     * Default constructor for course node of type single page
     */
    public COCourseNode() {
        super(TYPE);
        updateModuleConfigDefaults(true);
    }

    /**
     * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
     *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
     */
    @Override
    public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
        updateModuleConfigDefaults(false);
        COEditController childTabCntrllr = new COEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
        return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, euce, childTabCntrllr);
    }

    /**
     * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
     *      org.olat.core.gui.control.WindowControl,
     *      org.olat.course.run.userview.UserCourseEnvironment,
     *      org.olat.course.run.userview.NodeEvaluation)
     */
    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
            UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
    	updateModuleConfigDefaults(false);
        Controller controller;
        // Do not allow guests to send anonymous emails
        Roles roles = ureq.getUserSession().getRoles();
        if (roles.isGuestOnly()) {
            Translator trans = Util.createPackageTranslator(COCourseNode.class, ureq.getLocale());
            String title = trans.translate("guestnoaccess.title");
            String message = trans.translate("guestnoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else if(userCourseEnv.isCourseReadOnly()) {
            Translator trans = Util.createPackageTranslator(COCourseNode.class, ureq.getLocale());
            String title = trans.translate("freezenoaccess.title");
            String message = trans.translate("freezenoaccess.message");
            controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
        } else {
            controller = new CORunController(getModuleConfiguration(), ureq, wControl, userCourseEnv);
        }
        Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_co_icon");
        return new NodeRunConstructionResult(ctrl);
    }
    
    @Override
    public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse) {
        super.postCopy(envMapper, processType, course, sourceCrourse);
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
        if(deprecatedGroupKeys != null && deprecatedGroupKeys.size() > 0) {
        	deprecatedGroupKeys = envMapper.toGroupKeyFromOriginalKeys(deprecatedGroupKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS, deprecatedGroupKeys);
        }
        
        //remap the deprecated emailAreasIds
        List<Long> deprecatedAreaKeys = mc.getList(COEditController.CONFIG_KEY_EMAILTOAREA_IDS, Long.class);
        if(deprecatedAreaKeys != null && deprecatedAreaKeys.size() > 0) {
        	deprecatedAreaKeys = envMapper.toAreaKeyFromOriginalKeys(deprecatedAreaKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOAREA_IDS, deprecatedAreaKeys);
        }
    }

    @Override
    public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
        super.postExport(envMapper, backwardsCompatible);
        
        ModuleConfiguration mc = getModuleConfiguration();
        @SuppressWarnings("unchecked")
        List<Long> coachesGroupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID);
        List<Long> participantsGroupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID);
        if(coachesGroupKeys != null ) {
            String coachesGroupNames = envMapper.toGroupNames(coachesGroupKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP, coachesGroupNames);
        }
        if(participantsGroupKeys != null){
            String participantsGroupNames = envMapper.toGroupNames(participantsGroupKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP, participantsGroupNames);
        }
        
        @SuppressWarnings("unchecked")
        List<Long> coachesAreaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS);
        if(coachesAreaKeys != null) {
            String coachesAreaNames = envMapper.toAreaNames(coachesAreaKeys);
            mc.set(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA, coachesAreaNames);
        }
        
        @SuppressWarnings("unchecked")
        List<Long> participantsAreaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID);
        if(participantsAreaKeys != null) {
            String participantsAreaNames = envMapper.toAreaNames(participantsAreaKeys);    
            mc.set(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA, participantsAreaNames);
        }
        
        if(backwardsCompatible) {
            mc.remove(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
            mc.remove(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS);
        }
    }

    /**
     * @see org.olat.course.nodes.CourseNode#isConfigValid()
     */
    @Override
    public StatusDescription isConfigValid() {
    	updateModuleConfigDefaults(false);
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
        Boolean email2owners = getModuleConfiguration().getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOOWNERS);
        isValid = isValid || (email2owners != null && email2owners.booleanValue());
        String email2AreaCoaches = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
        isValid = isValid || (StringHelper.containsNonWhitespace(email2AreaCoaches));
        String email2GroupCoaches = (String) getModuleConfiguration().get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP);
        isValid = isValid || (StringHelper.containsNonWhitespace(email2GroupCoaches));
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL, false);
        isValid = isValid || getModuleConfiguration().getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE, false);
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

    @Override
    public void updateModuleConfigDefaults(boolean isNewNode) {
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