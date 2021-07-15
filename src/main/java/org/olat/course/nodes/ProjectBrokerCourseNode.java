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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DirectoryFilter;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileNameSuffixFilter;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.nodes.projectbroker.ProjectBrokerControllerFactory;
import org.olat.course.nodes.projectbroker.ProjectBrokerCourseEditorController;
import org.olat.course.nodes.projectbroker.ProjectListController;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerExportGenerator;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupReference;
import org.olat.modules.ModuleConfiguration;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * @author Christian Guretzki
 */

public class ProjectBrokerCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = -8177448874150049173L;
	private static final Logger log = Tracing.createLoggerFor(ProjectBrokerCourseNode.class);

	private static final transient String PACKAGE_PROJECTBROKER = Util.getPackageName(ProjectListController.class);
	private static final transient String PACKAGE = Util.getPackageName(ProjectBrokerCourseNode.class);

	public static final transient String TYPE = "projectbroker";

	// NLS support:

	private static final transient String NLS_GUESTNOACCESS_TITLE = "guestnoaccess.title";
	private static final transient String NLS_GUESTNOACCESS_MESSAGE = "guestnoaccess.message";
	private static final transient String NLS_ERROR_MISSINGSCORECONFIG_SHORT = "error.missingscoreconfig.short";
	private static final transient String NLS_WARN_NODEDELETE = "warn.nodedelete";

	// MUST BE NON TRANSIENT
	private static final int CURRENT_CONFIG_VERSION = 3;

	/** CONF_DROPBOX_ENABLED configuration parameter key. */
	public static final transient String CONF_DROPBOX_ENABLED = "dropbox_enabled";
	/** CONF_DROPBOX_ENABLEMAIL configuration parameter key. */
	public static final transient String CONF_DROPBOX_ENABLEMAIL = "dropbox_enablemail";
	/** CONF_DROPBOX_CONFIRMATION configuration parameter key. */
	public static final transient String CONF_DROPBOX_CONFIRMATION = "dropbox_confirmation";

	/** CONF_SCORING_ENABLED configuration parameter key. */
	public static final transient String CONF_SCORING_ENABLED = "scoring_enabled";

	/** ACCESS_SCORING configuration parameter key. */
	public static final transient String ACCESS_SCORING = "scoring";
	/** ACCESS_DROPBOX configuration parameter key. */
	public static final transient String ACCESS_DROPBOX = "dropbox";
	public static final transient String ACCESS_RETURNBOX = "returnbox";
	public static final transient String ACCESS_PROJECTBROKER = "projectbroker";

	/** CONF_TASK_PREVIEW configuration parameter key used for task-form. */
	public static final transient String CONF_TASK_PREVIEW = "task_preview";

	public static final transient String CONF_RETURNBOX_ENABLED = "returnbox_enabled";

	public static final transient String CONF_ACCOUNTMANAGER_GROUP_KEY = "config_accountmanager_group_id";

	public static final transient String CONF_PROJECTBROKER_KEY = "conf_projectbroker_id";

	public static final transient String CONF_NODE_SHORT_TITLE_KEY = "conf_node_short_title";

	// MUST BE NON TRANSIENT
	private Condition conditionDrop, conditionScoring, conditionReturnbox;
	private Condition conditionProjectBroker;

	public ProjectBrokerCourseNode() {
		this(null);
	}

	public ProjectBrokerCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		ProjectBrokerCourseEditorController childTabCntrllr = ProjectBrokerControllerFactory
				.createCourseEditController(ureq, wControl, course, this);
		CourseNode chosenNode = course.getEditorTreeModel()
				.getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController editController = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode,
				euce, childTabCntrllr);
		editController.addControllerListener(childTabCntrllr);
		return editController;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		// Do not allow guests to access tasks
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
			String title = trans.translate(NLS_GUESTNOACCESS_TITLE);
			String message = trans.translate(NLS_GUESTNOACCESS_MESSAGE);
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			// Add message id to business path if nodemcd is available
			if (nodecmd != null) {
				try {
					Long projectId = Long.valueOf(nodecmd);
					BusinessControlFactory bcf = BusinessControlFactory.getInstance();
					BusinessControl businessControl = bcf.createFromString("[Project:" + projectId + "]");
					wControl = bcf.createBusinessWindowControl(businessControl, wControl);
				} catch (NumberFormatException e) {
					// ups, nodecmd is not a message, what the heck is it then?
					log.warn("Could not create message ID from given nodemcd::" + nodecmd, e);
				}
			}
			controller = ProjectBrokerControllerFactory.createRunController(ureq, wControl, userCourseEnv, this);
		}
		Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_projectbroker_icon");
		return new NodeRunConstructionResult(wrapperCtrl);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		if (nodeSecCallback.isAccessible()) {
			return ProjectBrokerControllerFactory.createPeekViewRunController(ureq, wControl, userCourseEnv, this);
		}
		// use standard peekview
		return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback, small);
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
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		boolean isValid = true;
		Boolean hasScoring = (Boolean) getModuleConfiguration().get(CONF_SCORING_ENABLED);
		if (hasScoring.booleanValue()) {
			if (!MSEditFormController.isConfigValid(getModuleConfiguration()))
				isValid = false;
		}
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			String shortKey = NLS_ERROR_MISSINGSCORECONFIG_SHORT;
			String longKey = NLS_ERROR_MISSINGSCORECONFIG_SHORT;
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(MSEditFormController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition error
		// messages
		// check if group-manager is already initialized
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_PROJECTBROKER,
				getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		Translator trans = new PackageTranslator(PACKAGE_PROJECTBROKER, locale);
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		List<Property> list = cpm.listCourseNodeProperties(this, null, null, null);
		if (list.size() != 0)
			return trans.translate(NLS_WARN_NODEDELETE); // properties exist
		File fDropboxFolder = new File(FolderConfig.getCanonicalRoot()
				+ DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fDropboxFolder.exists() && fDropboxFolder.list().length > 0)
			return trans.translate(NLS_WARN_NODEDELETE); // Dropbox folder contains files
		File fReturnboxFolder = new File(FolderConfig.getCanonicalRoot()
				+ ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fReturnboxFolder.exists() && fReturnboxFolder.list().length > 0)
			return trans.translate(NLS_WARN_NODEDELETE); // Returnbox folder contains files

		return null; // no data yet.
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
		Long projectBrokerId = projectBrokerManager.getProjectBrokerId(cpm, this);
		File fDropBox = new File(FolderConfig.getCanonicalRoot()
				+ DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fDropBox.exists()) {
			FileUtils.deleteDirsAndFiles(fDropBox, true, true);
		}
		File fReturnBox = new File(FolderConfig.getCanonicalRoot()
				+ ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fReturnBox.exists()) {
			FileUtils.deleteDirsAndFiles(fReturnBox, true, true);
		}
		File attachmentDir = new File(FolderConfig.getCanonicalRoot()
				+ projectBrokerManager.getAttachmentBasePathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (attachmentDir.exists()) {
			FileUtils.deleteDirsAndFiles(attachmentDir, true, true);
		}
		// Delete project-broker, projects and project-groups
		if (projectBrokerId != null) {
			projectBrokerManager.deleteProjectBroker(projectBrokerId, course.getCourseEnvironment(), this);
		}
		// Delete all properties...
		cpm.deleteNodeProperties(this, null);

		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		CoreSpringFactory.getImpl(TaskExecutorManager.class).delete(resource, getIdent());
	}

	/**
	 * @return dropbox condition
	 */
	public Condition getConditionDrop() {
		if (conditionDrop == null) {
			conditionDrop = new Condition();
		}
		conditionDrop.setConditionId("drop");
		return conditionDrop;
	}

	/**
	 * @return scoring condition
	 */
	public Condition getConditionScoring() {
		if (conditionScoring == null) {
			conditionScoring = new Condition();
		}
		conditionScoring.setConditionId("scoring");
		return conditionScoring;
	}

	/**
	 * 
	 * @return Returnbox condition
	 */
	public Condition getConditionReturnbox() {
		if (conditionReturnbox == null) {
			conditionReturnbox = new Condition();
		}
		conditionReturnbox.setConditionId("returnbox");
		return conditionReturnbox;
	}

	/**
	 * @param conditionDrop
	 */
	public void setConditionDrop(Condition conditionDrop) {
		if (conditionDrop == null) {
			conditionDrop = getConditionDrop();
		}
		conditionDrop.setConditionId("drop");
		this.conditionDrop = conditionDrop;
	}

	/**
	 * @param conditionScoring
	 */
	public void setConditionScoring(Condition conditionScoring) {
		if (conditionScoring == null) {
			conditionScoring = getConditionScoring();
		}
		conditionScoring.setConditionId("scoring");
		this.conditionScoring = conditionScoring;
	}

	/**
	 * 
	 * @param condition
	 */
	public void setConditionReturnbox(Condition condition) {
		if (condition == null) {
			condition = getConditionReturnbox();
		}
		condition.setConditionId("returnbox");
		this.conditionReturnbox = condition;
	}

	public Condition getConditionProjectBroker() {
		if (conditionProjectBroker == null) {
			conditionProjectBroker = new Condition();
		}
		conditionProjectBroker.setConditionId("projectbroker");
		return conditionProjectBroker;
	}

	public void setConditionProjectBroker(Condition condition) {
		if (condition == null) {
			condition = getConditionProjectBroker();
		}
		condition.setConditionId("projectbroker");
		this.conditionProjectBroker = condition;
	}
	
	protected static XStream getXStream() {
		XStream xstream = XStreamHelper.createXStreamInstance();
		Class<?>[] types = new Class[] {
				ProjectBrokerConfig.class
			};
		xstream.addPermission(new ExplicitTypePermission(types));
		return xstream;
	}

	@Override
	public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper,
			Processing processType) {
		// initialize managers
		if (processType == Processing.editor && importDirectory != null) {
			ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
			CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
			// create a new projectBroker
			ProjectBroker projectBroker = projectBrokerManager.createAndSaveProjectBroker();
			projectBrokerManager.saveProjectBrokerId(projectBroker.getKey(), cpm, this);
			// get the node folder inside of the importDirectory
			File folderNodeData = new File(importDirectory, getIdent());

			// for the broker prefs
			File projectBrokerFile = new File(folderNodeData, "projectbroker.xml");
			if (projectBrokerFile.exists()) {
				XStream xstream = getXStream();
				ProjectGroupManager projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
				ProjectBrokerConfig brokerConfig = (ProjectBrokerConfig)XStreamHelper.readObject(xstream, projectBrokerFile);
				if (brokerConfig != null && brokerConfig.getAccountGroupKey() != null) {
					Long accountGroupKey = envMapper.toGroupKeyFromOriginalKey(brokerConfig.getAccountGroupKey());
					if (accountGroupKey != null) {
						projectGroupManager.saveAccountManagerGroupKey(accountGroupKey, cpm, this);
					}
				}
			}

			// loop through the project directories
			if (folderNodeData.exists()) {
				for (File projectDir : folderNodeData.listFiles(DirectoryFilter.DIRECTORY_FILTER)) {
					for (File projectFile : projectDir.listFiles(new FileNameSuffixFilter("xml"))) {
						importProject(projectDir, projectFile, projectBroker, course, envMapper);
					}
				}
			}
		}
		super.postImport(importDirectory, course, envMapper, processType);
	}

	private void importProject(File projectDir, File projectFile, ProjectBroker projectBroker, ICourse course,
			CourseEnvironmentMapper envMapper) {
		XStream xstream = getXStream();
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		ProjectGroupManager projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);

		// read the projectConfiguration from the importDirectory
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> projectConfig = (Map<String, Object>) XStreamHelper.readObject(xstream, projectFile);
			String projectTitle = (String) projectConfig.get("title");

			Long originalGroupKey = null;
			if (projectConfig.containsKey("businessGroupKey")) {
				originalGroupKey = (Long) projectConfig.get("businessGroupKey");
			} else {
				for (BusinessGroupReference ref : envMapper.getGroups()) {
					if (ref.getName().endsWith(projectTitle)) {
						originalGroupKey = ref.getOriginalKey();
					}
				}
			}

			BusinessGroup projectGroup = null;
			if (originalGroupKey != null) {
				Long groupKey = envMapper.toGroupKeyFromOriginalKey(originalGroupKey);
				projectGroup = bgs.loadBusinessGroup(groupKey);
			}
			if (projectGroup == null) {
				projectGroup = projectGroupManager.createProjectGroupFor(projectBroker.getKey(), envMapper.getAuthor(),
						projectTitle, (String) projectConfig.get("description"), course.getResourceableId());
			}
			if (envMapper.getAuthor() != null) {
				Identity author = envMapper.getAuthor();
				bgs.addOwners(author, null, Collections.singletonList(author), projectGroup, null);
			}

			Project project = projectBrokerManager.createAndSaveProjectFor(projectTitle,
					(String) projectConfig.get("description"), projectBrokerManager.getProjectBrokerId(cpm, this),
					projectGroup);
			projectGroupManager.setDeselectionAllowed(project, (boolean) projectConfig.get("allowDeselection"));
			project.setMailNotificationEnabled((boolean) projectConfig.get("mailNotificationEnabled"));
			project.setMaxMembers((int) projectConfig.get("maxMembers"));
			project.setAttachedFileName(projectConfig.get("attachmentFileName").toString());
			for (int i = 0; i < (int) projectConfig.get("customeFieldSize"); i++) {
				project.setCustomFieldValue(i, projectConfig.get("customFieldValue" + i).toString());
			}
			projectBrokerManager.updateProject(project);

			// get the attachment directory within the project directory
			File attachmentDir = new File(projectDir, "attachment");
			if (attachmentDir.exists()) {
				File[] attachment = attachmentDir.listFiles();
				if (attachment.length > 0) {
					VFSLeaf attachmentLeaf = new LocalFileImpl(attachment[0]);
					projectBrokerManager.saveAttachedFile(project, projectConfig.get("attachmentFileName").toString(),
							attachmentLeaf, course.getCourseEnvironment(), this, envMapper.getAuthor());
				}
			}
		} catch (Exception e) {
			// handle/log error in case of FileIO exception or cast
			// exception if import input is not correct
			log.error("Error while importing a project into projectbroker", e);
		}
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation,
			Locale locale, boolean withReferences) {
		super.importNode(importDirectory, course, owner, organisation, locale, withReferences);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		// initialize managers
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
		ProjectBroker pb = projectBrokerManager.getProjectBroker(projectBrokerManager.getProjectBrokerId(cpm, this));
		ProjectGroupManager projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		XStream xstream = getXStream();

		// folder for the pb node
		File pbNodeFolder = new File(exportDirectory, getIdent());
		pbNodeFolder.mkdirs();
		// for the broker prefs
		ProjectBrokerConfig brokerConfig = new ProjectBrokerConfig();
		brokerConfig.setAccountGroupKey(projectGroupManager.getAccountManagerGroupKey(cpm, this));
		File projectBrokerFile = new File(pbNodeFolder, "projectbroker.xml");
		XStreamHelper.writeObject(xstream, projectBrokerFile, brokerConfig);

		// get all the projects available in the pb
		List<Project> projects = projectBrokerManager.getProjectListBy(pb.getKey());
		for (Project project : projects) {
			File projectFolder = new File(pbNodeFolder, project.getKey().toString());
			projectFolder.mkdirs();
			// create a hashmap with the project configuration and insert the
			// project data
			File projectFile = new File(projectFolder, project.getKey() + ".xml");
			Map<String, Object> projectData = new HashMap<>();
			projectData.put("title", project.getTitle());
			projectData.put("description", project.getDescription());
			projectData.put("customFieldSize", project.getCustomFieldSize());
			projectData.put("maxMembers", project.getMaxMembers());
			projectData.put("mailNotificationEnabled", project.isMailNotificationEnabled());
			projectData.put("attachmentFileName", project.getAttachmentFileName());
			projectData.put("allowDeselection", projectGroupManager.isDeselectionAllowed(project));
			projectData.put("customeFieldSize", project.getCustomFieldSize());
			projectData.put("businessGroupKey", project.getProjectGroup().getKey());
			// iterate through the customFields
			for (int i = 0; i < project.getCustomFieldSize(); i++) {
				projectData.put("customFieldValue" + i, project.getCustomFieldValue(i));
			}
			// writeout the project data
			XStreamHelper.writeObject(xstream, projectFile, projectData);
			// add attachment file
			LocalFolderImpl rootFolder = VFSManager.olatRootContainer(
					projectBrokerManager.getAttamchmentRelativeRootPath(project, course.getCourseEnvironment(), this),
					null);
			VFSItem item = rootFolder.resolve(project.getAttachmentFileName());
			if (item instanceof VFSLeaf) {
				VFSLeaf itemLeaf = (VFSLeaf) item;
				File attachmentFolder = new File(projectFolder, "attachment");
				File attachment = new File(attachmentFolder,
						Base64.encodeBase64String(project.getAttachmentFileName().getBytes()));
				try {
					attachmentFolder.mkdirs();
					if(!attachment.createNewFile()) {
						log.error("Cannot create attachment file: {}", attachment);
					}
					FileOutputStream attachmentOutputStream = new FileOutputStream(attachment);
					InputStream leafInputStream = itemLeaf.getInputStream();
					FileUtils.copy(leafInputStream, attachmentOutputStream);
					attachmentOutputStream.close();
					leafInputStream.close();
				} catch (IOException e) {
					log.error("Error while exporting attachments for projectbroker " + project.getTitle(), e);
				}
			}
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream,
			String archivePath, String charset) {
		boolean dataFound = false;
		String dropboxPath = DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), this);
		VFSContainer dropboxDir = VFSManager.olatRootContainer(dropboxPath, null);
		String returnboxPath = ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), this);
		VFSContainer returnboxDir = VFSManager.olatRootContainer(returnboxPath, null);
		if (!dropboxDir.exists() && !returnboxDir.exists()) {
			return false;
		}

		String exportDirName = "projectbroker_" + Formatter.makeStringFilesystemSave(getShortName()) + "_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		exportDirName = ZipUtil.concat(archivePath, exportDirName);

		try {
			String projectBrokerTableExport = ProjectBrokerExportGenerator.createCourseResultsOverviewTable(this,
					course, locale);
			String tableExportFileName = ExportUtil
					.createFileNameWithTimeStamp(getShortTitle() + "-projectbroker_overview", "xls");
			exportStream.putNextEntry(new ZipEntry(exportDirName + "/" + tableExportFileName));
			IOUtils.write(projectBrokerTableExport, exportStream, "UTF-8");
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}

		// copy dropboxes to tmp dir
		if (dropboxDir.exists()) {
			// OLAT-6426 archive only dropboxes of users that handed in at least one file ->
			// prevent empty folders in archive
			for (VFSItem themaItem : dropboxDir.getItems(new VFSSystemItemFilter())) {
				if (!(themaItem instanceof VFSContainer))
					continue;
				List<VFSItem> userFolderArray = ((VFSContainer) themaItem).getItems(new VFSSystemItemFilter());
				for (VFSItem userFolder : userFolderArray) {
					if (!VFSManager.isDirectoryAndNotEmpty(userFolder))
						continue;
					String path = exportDirName + "/dropboxes/" + themaItem.getName();
					ZipUtil.addToZip(userFolder, path, exportStream, new VFSSystemItemFilter(), false);
				}
			}
		}

		// copy returnboxes to tmp dir
		if (returnboxDir.exists()) {
			for (VFSItem themaItem : returnboxDir.getItems(new VFSSystemItemFilter())) {
				if (!(themaItem instanceof VFSContainer))
					continue;
				List<VFSItem> userFolderArray = ((VFSContainer) themaItem).getItems(new VFSSystemItemFilter());
				for (VFSItem userFolder : userFolderArray) {
					if (!VFSManager.isDirectoryAndNotEmpty(userFolder))
						continue;
					String path = exportDirName + "/returnboxes/" + themaItem.getName();
					ZipUtil.addToZip(userFolder, path, exportStream, new VFSSystemItemFilter(), false);
				}
			}
		}

		return dataFound;
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			// dropbox defaults
			config.set(CONF_DROPBOX_ENABLED, Boolean.TRUE);
			config.set(CONF_DROPBOX_ENABLEMAIL, Boolean.FALSE);
			config.set(CONF_DROPBOX_CONFIRMATION, "");
			// scoring defaults
			config.set(CONF_SCORING_ENABLED, Boolean.FALSE);
			// returnbox defaults
			config.set(CONF_RETURNBOX_ENABLED, Boolean.TRUE);
			// New config parameter version 2
			config.setBooleanEntry(CONF_TASK_PREVIEW, false);
			MSCourseNode.initDefaultConfig(config);
			config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
		} else {
			int version = config.getConfigurationVersion();
			if (version < CURRENT_CONFIG_VERSION) {
				// Loaded config is older than current config version => migrate
				if (version == 1) {
					// migrate V1 => V2 (remove all condition
					this.setConditionDrop(null);
					this.setConditionReturnbox(null);
					version = 2;
				}
				if (version < 3) {
					setPreConditionAccess(conditionProjectBroker);
					conditionProjectBroker = null;
					conditionScoring = null;
				}
				config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
			}
		}
	}

	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(conditionDrop, envMapper);
		postImportCondition(conditionScoring, envMapper);
		postImportCondition(conditionReturnbox, envMapper);
		postImportCondition(conditionProjectBroker, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(conditionDrop, envMapper, backwardsCompatible);
		postExportCondition(conditionScoring, envMapper, backwardsCompatible);
		postExportCondition(conditionReturnbox, envMapper, backwardsCompatible);
		postExportCondition(conditionProjectBroker, envMapper, backwardsCompatible);
	}

	/**
	 * Do re-arrange the projects in a new project broker after the copy happened
	 */
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course,
			ICourse sourceCourse) {
		super.postCopy(envMapper, processType, course, null);
		if (processType.equals(Processing.runstructure)) {
			// initialize the managers and services
			ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
			ProjectGroupManager projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
			CoursePropertyManager oldCpm = sourceCourse.getCourseEnvironment().getCoursePropertyManager();
			BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			// create new Project broker and get the old one
			Long projectBrokerId = projectBrokerManager.createAndSaveProjectBroker().getKey();
			projectBrokerManager.saveProjectBrokerId(projectBrokerId,
					course.getCourseEnvironment().getCoursePropertyManager(), this);

			// find the group for account manager and remap the account group
			CourseNode sourceCourseNode = sourceCourse.getRunStructure().getNode(getIdent());
			Long sourceAccountGroupKey = projectGroupManager.getAccountManagerGroupKey(oldCpm, sourceCourseNode);
			if (sourceAccountGroupKey != null) {
				Long copiedGroupKey = envMapper.toGroupKeyFromOriginalKey(sourceAccountGroupKey);
				CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
				projectGroupManager.saveAccountManagerGroupKey(copiedGroupKey, cpm, this);
			}

			Long oldBrokerId = projectBrokerManager.getProjectBrokerId(oldCpm, this);
			List<Project> projectsFromGroup = projectBrokerManager.getProjectListBy(oldBrokerId);
			// loop create and configure the new Projects
			for (Project project : projectsFromGroup) {
				Long originalGroupKey = project.getProjectGroup().getKey();
				Long copiedGroupKey = envMapper.toGroupKeyFromOriginalKey(originalGroupKey);

				Identity author = envMapper.getAuthor();
				BusinessGroup projectGroup = bgs.loadBusinessGroup(copiedGroupKey);
				if (projectGroup == null) {
					projectGroup = projectGroupManager.createProjectGroupFor(projectBrokerId, author,
							project.getTitle(), project.getDescription(), course.getResourceableId());
				}
				if (author != null) {
					bgs.addOwners(author, null, Collections.singletonList(author), projectGroup, null);
				}

				Project newProject = projectBrokerManager.createAndSaveProjectFor(project.getTitle(),
						project.getDescription(), projectBrokerId, projectGroup);
				// copy all project configurations
				newProject.setMailNotificationEnabled(project.isMailNotificationEnabled());
				newProject.setMaxMembers(project.getMaxMembers());
				for (int i = 0; i < project.getCustomFieldSize(); i++) {
					newProject.setCustomFieldValue(i, project.getCustomFieldValue(i));
				}
				projectGroupManager.setDeselectionAllowed(newProject, project.getProjectGroup().isAllowToLeave());
				projectBrokerManager.updateProject(newProject);
				// attachment file
				VFSContainer rootFolder = VFSManager.olatRootContainer(projectBrokerManager
						.getAttamchmentRelativeRootPath(project, sourceCourse.getCourseEnvironment(), this), null);
				VFSItem item = rootFolder.resolve(project.getAttachmentFileName());
				if (item instanceof VFSLeaf) {
					projectBrokerManager.saveAttachedFile(newProject, project.getAttachmentFileName(), (VFSLeaf) item,
							course.getCourseEnvironment(), this, envMapper.getAuthor());
					newProject.setAttachedFileName(project.getAttachmentFileName());
					projectBrokerManager.updateProject(newProject);
				}
			}
		}
	}

	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		// create the instance for the copy
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle, course, author);
		// get all the different managers
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		ProjectGroupManager projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);

		// get the pbID from the source pb
		Long oldProjectBrokerId = projectBrokerManager.getProjectBrokerId(cpm, this);
		// create a new projectBroker for the copyInstance
		ProjectBroker newBroker = projectBrokerManager.createAndSaveProjectBroker();
		Long projectBrokerId = newBroker.getKey();
		projectBrokerManager.saveProjectBrokerId(projectBrokerId, cpm, copyInstance);

		// configure the new Project like the old one
		// copy the old accountManagergroup to preserve the
		// "persons in charge"
		Long originalAccountGroupKey = projectGroupManager.getAccountManagerGroupKey(cpm, this);
		if (originalAccountGroupKey != null) {
			BusinessGroup originalAccountGroup = projectGroupManager.getAccountManagerGroupFor(cpm, this, course,
					getShortTitle(), getShortTitle(), null);
			BusinessGroup newAccountManagerGroup = bgs.copyBusinessGroup(author, originalAccountGroup,
					originalAccountGroup.getName(), originalAccountGroup.getDescription(),
					originalAccountGroup.getMinParticipants(), originalAccountGroup.getMaxParticipants(), false, false,
					true, false, false, true, false, false, Boolean.FALSE);
			projectGroupManager.saveAccountManagerGroupKey(newAccountManagerGroup.getKey(), cpm, copyInstance);
			bgs.addResourceTo(newAccountManagerGroup,
					course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		}

		if (oldProjectBrokerId != null) {
			List<Project> projects = projectBrokerManager.getProjectListBy(oldProjectBrokerId);
			for (Project project : projects) {
				// create projectGroup
				BusinessGroup projectGroup = projectGroupManager.createProjectGroupFor(projectBrokerId, author,
						project.getTitle(), project.getDescription(), course.getResourceableId());
				Project newProject = projectBrokerManager.createAndSaveProjectFor(project.getTitle(),
						project.getDescription(), projectBrokerId, projectGroup);

				// copy all project configurations
				newProject.setMailNotificationEnabled(project.isMailNotificationEnabled());
				newProject.setMaxMembers(project.getMaxMembers());
				for (int i = 0; i < project.getCustomFieldSize(); i++) {
					newProject.setCustomFieldValue(i, project.getCustomFieldValue(i));
				}
				projectGroupManager.setDeselectionAllowed(newProject, project.getProjectGroup().isAllowToLeave());
				projectBrokerManager.updateProject(newProject);

				// attachment file
				VFSContainer rootFolder = VFSManager.olatRootContainer(projectBrokerManager
						.getAttamchmentRelativeRootPath(project, course.getCourseEnvironment(), this), null);
				VFSItem item = rootFolder.resolve(project.getAttachmentFileName());
				if (item instanceof VFSLeaf) {
					projectBrokerManager.saveAttachedFile(newProject, project.getAttachmentFileName(), (VFSLeaf) item,
							course.getCourseEnvironment(), copyInstance, author);
					newProject.setAttachedFileName(project.getAttachmentFileName());
					projectBrokerManager.updateProject(newProject);
				}
			}
		}
		return copyInstance;
	}

	public static class ProjectBrokerConfig implements Serializable {

		private static final long serialVersionUID = -1002067261836601966L;
		private Long accountGroupKey;

		public Long getAccountGroupKey() {
			return accountGroupKey;
		}

		public void setAccountGroupKey(Long accountGroupKey) {
			this.accountGroupKey = accountGroupKey;
		}
	}
}