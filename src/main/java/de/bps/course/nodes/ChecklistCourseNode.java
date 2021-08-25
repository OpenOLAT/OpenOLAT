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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

import de.bps.course.nodes.cl.ChecklistEditController;
import de.bps.olat.modules.cl.Checklist;
import de.bps.olat.modules.cl.ChecklistDisplayController;
import de.bps.olat.modules.cl.ChecklistManager;
import de.bps.olat.modules.cl.Checkpoint;
import de.bps.olat.modules.cl.CheckpointMode;
import de.bps.olat.modules.cl.CheckpointResult;

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
	private static final Logger log = Tracing.createLoggerFor(ChecklistCourseNode.class);
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
		ModuleConfiguration config = getModuleConfiguration();
		// add an empty checkpoint entry as default if none existent
		if (config.get(CONF_CHECKLIST) == null) {
			Checklist initialChecklist = new Checklist();
			// set to config
			config.set(CONF_CHECKLIST, initialChecklist);
			// save to db
			ChecklistManager.getInstance().saveChecklist(initialChecklist);
		}
	}
	
	/**
	 * Internal helper: save node configuration
	 * @param cpm
	 * @param checklistKey
	 */
	private void setChecklistKey(final CoursePropertyManager cpm, Long checklistKey) {
		Property checklistKeyProperty = cpm.createCourseNodePropertyInstance(this, null, null, PROPERTY_CHECKLIST_KEY, null, checklistKey, null, null);
		cpm.saveProperty(checklistKeyProperty);
		/*
		 * Save reference to checklist additionally in module configuration since the CoursePropertyManager is not always available. 
		 */
		getModuleConfiguration().set(CONF_CHECKLIST, ChecklistManager.getInstance().loadChecklist(checklistKey));
	}
	
	/**
	 * Internal helper: load node configuration
	 * @param cpm
	 * @return checklistKey
	 */
	private Long getChecklistKey(final CoursePropertyManager cpm) {
		Property checklistKeyProperty = cpm.findCourseNodeProperty(this, null, null, PROPERTY_CHECKLIST_KEY);
		
		return checklistKeyProperty != null ? checklistKeyProperty.getLongValue() : null;
	}
	
	/**
	 * Internal helper: delete node configuration
	 * @param cpm
	 */
	private void deleteChecklistKeyConf(final CoursePropertyManager cpm) {
		cpm.deleteNodeProperties(this, PROPERTY_CHECKLIST_KEY);
		getModuleConfiguration().remove(CONF_CHECKLIST);
	}
	
	/**
	 * Load referenced checklist or create a new one
	 * @param cpm
	 * @return Checklist
	 */
	public Checklist loadOrCreateChecklist(final CoursePropertyManager cpm) {
		Checklist checklist;
		
		if(getChecklistKey(cpm) == null) {
			// this is a copied node, the checklist is referenced by createInstanceForCopy()
			if(getModuleConfiguration().get(CONF_CHECKLIST_COPY) != null) {
				checklist = ChecklistManager.getInstance().loadChecklist((Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST_COPY));
				getModuleConfiguration().remove(CONF_CHECKLIST_COPY);
			} else 
			// this is part of a copied course, the original checklist will be copied
			if(getModuleConfiguration().get(CONF_CHECKLIST) != null) {
				Checklist confChecklist = (Checklist)getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST);
				Checklist orgChecklist = ChecklistManager.getInstance().loadChecklist(confChecklist);
				checklist = ChecklistManager.getInstance().copyChecklist(orgChecklist);
			} else {
				// no checklist available, create new one
				checklist = new Checklist();
				ChecklistManager.getInstance().saveChecklist(checklist);
			}
			// set referenced checklist in configuration
			setChecklistKey(cpm, checklist.getKey());
		} else {
			checklist = ChecklistManager.getInstance().loadChecklist(getChecklistKey(cpm));
		}
		
		return checklist;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		ChecklistEditController editController = new ChecklistEditController(ureq, wControl, this, course);
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		NodeEditController nodeEditController = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, editController);
		nodeEditController.addControllerListener(editController);
		return nodeEditController;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		boolean canEdit = userCourseEnv.isAdmin();
		boolean canManage;
		if(canEdit) {
			canManage = true;
		} else {
			GroupRoles role = GroupRoles.owner;
			if (userCourseEnv.isParticipant()) {
				role = GroupRoles.participant;
			} else if (userCourseEnv.isCoach()) {
				role = GroupRoles.coach;
			}
			canManage = userCourseEnv.isCoach() || cgm.hasRight(ureq.getIdentity(), CourseRights.RIGHT_GROUPMANAGEMENT, role);
		}
		Checklist checklist = loadOrCreateChecklist(userCourseEnv.getCourseEnvironment().getCoursePropertyManager());
		ChecklistDisplayController checkController = new ChecklistDisplayController(ureq, wControl, checklist,
				canEdit, canManage, userCourseEnv.isCourseReadOnly(), course);
		checkController.addLoggingResourceable(LoggingResourceable.wrap(this));
		
		// Add title and descrition
		Controller controller = TitledWrapperHelper.getWrapper(ureq, wControl, checkController, userCourseEnv, this, "o_cl_icon");
		return new NodeRunConstructionResult(controller);
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;
		String transPackage = ChecklistEditController.class.getPackage().getName();
		
		// no configuration available hence there is no checklist with checkpoints
		if(getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST) == null) {
			sd = new StatusDescription(ValidationStatus.ERROR, "config.nocheckpoints.short", "config.nocheckpoints.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(ChecklistEditController.PANE_TAB_CLCONFIG);
			return sd;
		}
		
		Checklist checklist = (Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST);
		// checklist without any checkpoints makes no sense
		if (!checklist.hasCheckpoints()) {
			sd = new StatusDescription(ValidationStatus.ERROR, "config.nocheckpoints.short", "config.nocheckpoints.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(ChecklistEditController.PANE_TAB_CLCONFIG);
			return sd;
		}
		
		// information, if all checkpoints are invisible
		boolean allUnvisible = true;
		boolean noLearners = false;
		if (checklist.hasCheckpoints()) {
			List<Checkpoint> checkpoints = ((Checklist)getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST)).getCheckpoints();
			for (Checkpoint checkpoint : checkpoints) {
				if (!checkpoint.getMode().equals(CheckpointMode.MODE_HIDDEN)) allUnvisible = false;
			}
			if(allUnvisible) {
				Condition cond = getPreConditionVisibility();
				if(cond.isEasyModeCoachesAndAdmins()) noLearners = true;
				if(!noLearners) {
					sd = new StatusDescription(ValidationStatus.WARNING, "config.allhidden.short", "config.allhidden.long", null, transPackage);
					sd.setDescriptionForUnit(getIdent());
					sd.setActivateableViewIdentifier(ChecklistEditController.PANE_TAB_CLCONFIG);
				}
			}
		}
		return sd;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// delete checklist in db
		Checklist checklist = loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());
		ChecklistManager.getInstance().deleteChecklist(checklist);
		// delete node configuration
		deleteChecklistKeyConf(course.getCourseEnvironment().getCoursePropertyManager());
	}
	
	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		ChecklistManager cm = ChecklistManager.getInstance();
		Checklist checklist = loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());
		Checklist copy = cm.copyChecklistInRAM(checklist);
		String exportContent = getXStream().toXML(copy);
		ExportUtil.writeContentToFile(getExportFilename(), exportContent, exportDirectory, WebappHelper.getDefaultCharset());
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		if(getChecklistKey(cpm) != null) deleteChecklistKeyConf(cpm);
		
		File importFile = new File(importDirectory, getExportFilename());
		String importContent = FileUtils.load(importFile, WebappHelper.getDefaultCharset());
		if(importContent == null || importContent.isEmpty()) {
			return;
		}
		
		Checklist checklist = (Checklist)getXStream().fromXML(importContent);
		if(checklist != null) {
			checklist = ChecklistManager.getInstance().copyChecklist(checklist);
			setChecklistKey(cpm, checklist.getKey());
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		String filename = "checklist_"
				+ StringHelper.transformDisplayNameToFileSystemName(getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		filename = ZipUtil.concat(archivePath, filename);
		
		Checklist checklist = loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());
		String exportContent = getXStream().toXML(checklist);
		try {
			exportStream.putNextEntry(new ZipEntry(filename));
			IOUtils.write(exportContent, exportStream, "UTF-8");
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
		return true;
	}
	
	protected static XStream getXStream() {
		XStream xstream = XStreamHelper.createXStreamInstance();
		Class<?>[] types = new Class[] {
				CheckpointResult.class, Checkpoint.class, Checklist.class,
			};
		xstream.addPermission(new ExplicitTypePermission(types));
		return xstream;
	}

	private String getExportFilename() {
		return "checklist_"+this.getIdent()+".xml";
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle, course, author);
		ChecklistManager cm = ChecklistManager.getInstance();
		// load checklist
		Checklist checklist = cm.loadChecklist((Checklist) getModuleConfiguration().get(ChecklistCourseNode.CONF_CHECKLIST));
		// remove old config
		copyInstance.getModuleConfiguration().remove(ChecklistCourseNode.CONF_CHECKLIST);
		// create new checklist with same settings and save to db
		Checklist initialChecklist = cm.copyChecklist(checklist);
		// set to config
		copyInstance.getModuleConfiguration().set(CONF_CHECKLIST_COPY, initialChecklist);
		
		return copyInstance;
	}
	
}
