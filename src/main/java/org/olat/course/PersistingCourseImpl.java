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

package org.olat.course;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import org.olat.admin.quota.QuotaConstants;
import org.olat.commons.fileutil.CourseConfigUtil;
import org.olat.commons.fileutil.CustomQuotaDetectedException;
import org.olat.commons.fileutil.FileSizeLimitExceededException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithLazyQuotaCallback;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManager;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNode.Processing;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.environment.CourseEnvironmentImpl;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * Implementation of the course data structure. The course is defined using a 
 * runStructure and the editorTreeModel. Additional things are available through
 * the courseEnvironment (e.g. access to managers (Factory methods) or the course
 * configuration)<br/>
 * It is allowed to save a course only if the course is in readAndWrite. 
 * <P>
 * Initial Date:  12.08.2005 <br>
 * @author Felix Jost
 */
public class PersistingCourseImpl implements ICourse, OLATResourceable, Serializable {

	private static final long serialVersionUID = -1022498371474445868L;

	public static final String COURSE_ROOT_DIR_NAME = "course";
	
	private static final String EDITORTREEMODEL_XML = "editortreemodel.xml";
	private static final String RUNSTRUCTURE_XML = "runstructure.xml";
	private static final String ORES_TYPE_NAME = CourseModule.getCourseTypeName();
	public static final String COURSEFOLDER = "coursefolder";

	private Long resourceableId;
	private Structure runStructure;
	private boolean hasAssessableNodes = false;
	private CourseEditorTreeModel editorTreeModel;
	private CourseConfig courseConfig;
	private final CourseEnvironmentImpl courseEnvironment;
	private LocalFolderImpl courseRootContainer;
	private String courseTitle = null;
	/** courseTitleSyncObj is a final Object only used for synchronizing the courseTitle getter - see OLAT-5654 */
	private final Object courseTitleSyncObj = new Object();
	private static final Logger log = Tracing.createLoggerFor(PersistingCourseImpl.class);
	
	//an PersistingCourseImpl instance could be readOnly if readAndWrite == false, or readAndWrite 
	private boolean readAndWrite = false; //default readOnly
	
	public boolean isReadAndWrite() {
		return readAndWrite;
	}

	public void setReadAndWrite(boolean readAndWrite) {
		this.readAndWrite = readAndWrite;
	}

	/**
	 * Creates a new Course instance and creates the course filesystem if it does
	 * not already exist. Editor and run structures are not yet set. Use load() to
	 * initialize the editor and run structure from persisted XML structure.
	 * 
	 * @param resource The OLAT resource
	 */
	PersistingCourseImpl(OLATResource resource) {
		this.resourceableId = resource.getResourceableId();
		// prepare filesystem and set course base path and course folder paths
		prepareFilesystem();
		courseConfig = CoreSpringFactory.getImpl(CourseConfigManager.class).loadConfigFor(this); // load or init defaults
		courseEnvironment = new CourseEnvironmentImpl(this, resource);
	}
	
	PersistingCourseImpl(RepositoryEntry courseEntry) {
		courseTitle = courseEntry.getDisplayname();
		resourceableId = courseEntry.getOlatResource().getResourceableId();
		// prepare filesystem and set course base path and course folder paths
		prepareFilesystem();
		courseConfig = CoreSpringFactory.getImpl(CourseConfigManager.class).loadConfigFor(this); // load or init defaults
		courseEnvironment = new CourseEnvironmentImpl(this, courseEntry);
	}
	
	@Override
	public Structure getRunStructure() {
		return runStructure;
	}

	@Override
	public CourseEditorTreeModel getEditorTreeModel() {
		return editorTreeModel;
	}

	@Override
	public LocalFolderImpl getCourseBaseContainer() {
		return courseRootContainer;
	}
	
	@Override
	public LocalFolderImpl getCourseExportDataDir() {
		LocalFolderImpl vfsExportDir = (LocalFolderImpl)getCourseBaseContainer().resolve(ICourse.EXPORTED_DATA_FOLDERNAME);
		if (vfsExportDir == null) {
			vfsExportDir = (LocalFolderImpl)getCourseBaseContainer().createChildContainer(ICourse.EXPORTED_DATA_FOLDERNAME);
		}
		return vfsExportDir;
	}

	@Override
	public VFSContainer getCourseFolderContainer() {
		// add local course folder's children as read/write source and any sharedfolder as subfolder
		MergedCourseContainer courseFolderContainer = new MergedCourseContainer(resourceableId, getCourseTitle());
		courseFolderContainer.init(this);
		return courseFolderContainer;
	}

	@Override
	public VFSContainer getCourseFolderContainer(CourseContainerOptions options) {
		// add local course folder's children as read/write source and any sharedfolder as subfolder
		MergedCourseContainer courseFolderContainer = new MergedCourseContainer(resourceableId, getCourseTitle(), null,
				options, false);
		courseFolderContainer.init(this);
		return courseFolderContainer;
	}

	@Override
	public VFSContainer getCourseFolderContainer(boolean overrideReadOnly) {
		// add local course folder's children as read/write source and any sharedfolder as subfolder
		MergedCourseContainer courseFolderContainer = new MergedCourseContainer(resourceableId, getCourseTitle(), null,
				CourseContainerOptions.all(), overrideReadOnly);
		courseFolderContainer.init(this);
		return courseFolderContainer;
	}

	@Override
	public VFSContainer getCourseFolderContainer(IdentityEnvironment identityEnv) {
		// add local course folder's children as read/write source and any sharedfolder as subfolder
		MergedCourseContainer courseFolderContainer = new MergedCourseContainer(resourceableId, getCourseTitle(), identityEnv);
		courseFolderContainer.init(this);
		return courseFolderContainer;
	}
	
	/**
	 * @see org.olat.course.ICourse#getCourseEnvironment()
	 */
	@Override
	public CourseEnvironment getCourseEnvironment() {
		return courseEnvironment;
	}

	/**
	 * @see org.olat.course.ICourse#getCourseTitle()
	 */
	@Override
	public String getCourseTitle() {	
		if (courseTitle == null) {
			synchronized (courseTitleSyncObj) { //o_clusterOK by:ld/se
				if (courseTitle == null) {
					// load repository entry for this course and get title from it
					courseTitle = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resourceableId);
				}
			}
		}
		return courseTitle;
	}
	
	public void updateCourseEntry(RepositoryEntry courseEntry) {
		courseTitle = courseEntry.getDisplayname();
		courseEnvironment.updateCourseEntry(courseEntry);
		synchronizeLongCourseTitle();
	}

	@Override
	public boolean exceedsSizeLimit() {
		File exportDirectory = getCourseBaseContainer().getBasefile();
		// LMSUZH-45: just detect custom quotas on course elements instead of calculating the folder size
		try {
			CourseConfigUtil.checkAgainstCustomQuotas(exportDirectory);
		} catch (CustomQuotaDetectedException e) {
			log.error("Custom quota detected for " + exportDirectory.getPath() + " or its nodes");
			return true;
		}
// LMSUZH-45 Keeping the old code based on calculation of folder size, in case we want to build more complex solution
//		try {
//			CourseConfigUtil.checkAgainstConfiguredMaxSize(getCourseBaseContainer().getBasefile());
//		} catch (FileSizeLimitExceededException e) {
//			return true;
//		}

		return false;
	}

	/**
	 * Prepares the filesystem for this course.
	 */
	private void prepareFilesystem() {
		// generate course base path
		String relPath = File.separator + COURSE_ROOT_DIR_NAME + File.separator + getResourceableId().longValue();
		courseRootContainer = VFSManager.olatRootContainer(relPath, null);
		File fBasePath = courseRootContainer.getBasefile();
		if (!fBasePath.exists() && !fBasePath.mkdirs())
			throw new OLATRuntimeException(this.getClass(), "Could not create course base path:" + courseRootContainer, null);
	}

	/**
	 * @return The directory "coursefolder" or storage folder of the course
	 */
	protected LocalFolderImpl getIsolatedCourseFolder() {
		// create local course folder
		LocalFolderImpl isolatedCourseFolder = VFSManager.olatRootContainer(courseRootContainer.getRelPath() + File.separator + COURSEFOLDER, null);
		// generate course folder
		File fCourseFolder = isolatedCourseFolder.getBasefile();
		if (!fCourseFolder.exists() && !fCourseFolder.mkdirs()) {
			throw new OLATRuntimeException(this.getClass(),
					"could not create course's coursefolder path:" + fCourseFolder.getAbsolutePath(), null);
		}
		
		FullAccessWithQuotaCallback secCallback = new FullAccessWithLazyQuotaCallback(isolatedCourseFolder.getRelPath(), QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
		isolatedCourseFolder.setLocalSecurityCallback(secCallback);
		return isolatedCourseFolder;
	}
	
	/**
	 * @return The directory "coursefolder" or storage folder of the course
	 */
	protected File getIsolatedCourseBaseFolder() {
		// create local course folder
		return VFSManager.olatRootDirectory(courseRootContainer.getRelPath() + File.separator + COURSEFOLDER);
	}
	
	/**
	 * @return The directory "coursefolder" or storage folder of the course
	 */
	protected VFSContainer getIsolatedCourseBaseContainer() {
		// create local course folder
		return VFSManager.olatRootContainer(courseRootContainer.getRelPath() + File.separator + COURSEFOLDER, null);
	}
	
	/**
	 * Save the run structure to disk, persist to the xml file
	 */
	void saveRunStructure() {
		writeObject(RUNSTRUCTURE_XML, getRunStructure());
		log.debug("saveRunStructure");
	}

	/**
	 * Save the editor tree model to disk, persist to the xml file
	 */
	void saveEditorTreeModel() {
		writeObject(EDITORTREEMODEL_XML, getEditorTreeModel());
		log.debug("saveEditorTreeModel");
	}

	/**
	 * @see org.olat.course.ICourse#exportToFilesystem(java.io.File)
	 * <p>
	 * See OLAT-5368: Course Export can take longer than say 2min.
	 * <p>
	 */
	@Override
	public void exportToFilesystem(OLATResource originalCourseResource, File exportDirectory,
			boolean runtimeDatas, boolean backwardsCompatible) throws FileSizeLimitExceededException {
		long s = System.currentTimeMillis();
		log.info("exportToFilesystem: exporting course "+this+" to "+exportDirectory+"...");
		File fCourseBase = getCourseBaseContainer().getBasefile();
		//make the folder structure
		File fExportedDataDir = new File(exportDirectory, EXPORTED_DATA_FOLDERNAME);
		fExportedDataDir.mkdirs();

		//export course config
		FileUtils.copyFileToDir(new File(fCourseBase, CourseConfigManager.COURSECONFIG_XML), exportDirectory, "course export courseconfig");
		
		//export business groups
		CourseEnvironmentMapper envMapper = getCourseEnvironment().getCourseGroupManager().getBusinessGroupEnvironment();

		getCourseEnvironment().getCourseGroupManager().exportCourseBusinessGroups(fExportedDataDir, envMapper, runtimeDatas);
		// export editor structure
		FileUtils.copyFileToDir(new File(fCourseBase, EDITORTREEMODEL_XML), exportDirectory, "course export exitortreemodel");
		// export run structure
		FileUtils.copyFileToDir(new File(fCourseBase, RUNSTRUCTURE_XML), exportDirectory, "course export runstructure");

		// export layout and media folder
		FileUtils.copyDirToDir(new File(fCourseBase, "layout"), exportDirectory, "course export layout folder");
		FileUtils.copyDirToDir(new File(fCourseBase, "media"), exportDirectory, "course export media folder");
		// export course folder
		File fExportedCoursefolderZip = new File(exportDirectory, "oocoursefolder.zip");
		File courseFolder = getIsolatedCourseBaseFolder();
		ZipUtil.zipAll(courseFolder, fExportedCoursefolderZip);

		// export any node data
		log.info("exportToFilesystem: exporting course "+this+": exporting all nodes...");
		Visitor visitor = new NodeExportVisitor(fExportedDataDir, this);
		TreeVisitor tv = new TreeVisitor(visitor, getEditorTreeModel().getRootNode(), true);
		tv.visitAll();
		log.info("exportToFilesystem: exporting course "+this+": exporting all nodes...done.");
		
		// Do intermediate commit to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();

		// export shared folder
		CourseConfig config = getCourseConfig();
		if (config.hasCustomSharedFolder()) {
			log.info("exportToFilesystem: exporting course "+this+": shared folder...");
			if (!SharedFolderManager.getInstance().exportSharedFolder(
					config.getSharedFolderSoftkey(), fExportedDataDir)) {
				// export failed, delete reference to shared folder in the course config
				log.info("exportToFilesystem: exporting course "+this+": export of shared folder failed.");
				config.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
				CoreSpringFactory.getImpl(CourseConfigManager.class).saveConfigTo(this, config);
			}
			log.info("exportToFilesystem: exporting course "+this+": shared folder...done.");
		}
		
		// Do intermediate commit to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();

		// export glossary
		if (config.hasGlossary()) {
			log.info("exportToFilesystem: exporting course "+this+": glossary...");
			final GlossaryManager glossaryManager = CoreSpringFactory.getImpl(GlossaryManager.class);
			final CourseConfigManager courseConfigManager = CoreSpringFactory.getImpl(CourseConfigManager.class);
			if (!glossaryManager.exportGlossary(
					config.getGlossarySoftKey(), fExportedDataDir)) {
				// export failed, delete reference to glossary in the course config
				log.info("exportToFilesystem: exporting course "+this+": export of glossary failed.");
				config.setGlossarySoftKey(null);
				courseConfigManager.saveConfigTo(this, config);
			}
			log.info("exportToFilesystem: exporting course "+this+": glossary...done.");
		}
		
		// Do intermediate commit to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();

		log.info("exportToFilesystem: exporting course "+this+": configuration and repo data...");
		// export configuration file
		FileUtils.copyFileToDir(new File(fCourseBase, CourseConfigManager.COURSECONFIG_XML), exportDirectory, "course export configuration and repo info");
		
		// export repo metadata
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry myRE = rm.lookupRepositoryEntry(this, true);
		RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(myRE, fExportedDataDir);
		importExport.exportDoExportProperties();
		
		// Do intermediate commit to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();
		
		//export reminders
		CoreSpringFactory.getImpl(ReminderService.class)
			.exportReminders(myRE, fExportedDataDir);

		log.info("exportToFilesystem: exporting course "+this+" to "+exportDirectory+" done.");
		log.info("finished export course '"+getCourseTitle()+"' in t="+Long.toString(System.currentTimeMillis()-s));
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, ICourse sourceCourse) {
		Structure importedStructure = getRunStructure();
		visit(new NodePostCopyVisitor(envMapper, Processing.runstructure, this, sourceCourse), importedStructure.getRootNode());
		saveRunStructure();
		
		CourseEditorTreeModel importedEditorModel = getEditorTreeModel();
		visit(new NodePostCopyVisitor(envMapper, Processing.editor, this, sourceCourse), importedEditorModel.getRootNode());
		saveEditorTreeModel();
	}
	
	@Override
	public void postImport(File importDirectory, CourseEnvironmentMapper envMapper) {
		Structure importedStructure = getRunStructure();
		visit(new NodePostImportVisitor(importDirectory, this, envMapper, Processing.runstructure), importedStructure.getRootNode());
		saveRunStructure();
		
		CourseEditorTreeModel importedEditorModel = getEditorTreeModel();
		visit(new NodePostImportVisitor(importDirectory, this, envMapper, Processing.editor), importedEditorModel.getRootNode());
		saveEditorTreeModel();
	}
	
	private void visit(Visitor visitor, INode node) {
		visitor.visit(node);
		for(int i=node.getChildCount(); i-->0; ) {
			INode subNode = node.getChildAt(i);
			visit(visitor, subNode);
		}
	}

	/**
	 * Load the course from disk/database, load the run structure from xml file etc.
	 */
	void load() {
		/*
		 * remember that loading of the courseConfiguration is already done within
		 * the constructor !
		 */
		Object obj;
		obj = readObject(RUNSTRUCTURE_XML);
		if (!(obj instanceof Structure)) throw new AssertException("Error reading course run structure.");
		runStructure = (Structure) obj;
		initHasAssessableNodes();
		
		obj = readObject(EDITORTREEMODEL_XML);
		if (!(obj instanceof CourseEditorTreeModel)) throw new AssertException("Error reading course editor tree model.");
		editorTreeModel = (CourseEditorTreeModel) obj;

		synchronizeLongCourseTitle();
	}

	private void synchronizeLongCourseTitle() {
		runStructure.getRootNode().setLongTitle(getCourseTitle());
		((CourseEditorTreeNode) editorTreeModel.getRootNode()).setLongTitle(getCourseTitle());
	}

	/**
	 * Write a structure to an XML file in the course base path folder.
	 * 
	 * @param fileName
	 * @param obj
	 */
	private void writeObject(String fileName, Object obj) {
		VFSLeaf vfsItem =(VFSLeaf)getCourseBaseContainer().resolve(fileName);
		if (vfsItem == null) {
			vfsItem = getCourseBaseContainer().createChildLeaf(fileName);
		} else if(vfsItem.exists()) {
			try(InputStream in=vfsItem.getInputStream()) {
				CoreSpringFactory.getImpl(VFSRepositoryService.class).addVersion(vfsItem, null, "", in);
			} catch (Exception e) {
				log.error("Cannot versioned " + fileName, e);
			}
		}
		XStream xstream = CourseXStreamAliases.getWriteCourseXStream();
		XStreamHelper.writeObject(xstream, vfsItem, obj);
	}

	/**
	 * Read a structure from XML file within the course base path folder.
	 * 
	 * @param fileName
	 * @return de-serialized object
	 * @throws OLATRuntimeException if de-serialization fails.
	 */
	private Object readObject(String fileName) {
		VFSItem vfsItem = getCourseBaseContainer().resolve(fileName);
		if (!(vfsItem instanceof VFSLeaf)) {
			throw new CorruptedCourseException("Cannot resolve file: " + fileName + " course=" + toString());
		}
		try {
			XStream xstream = CourseXStreamAliases.getReadCourseXStream();
			return XStreamHelper.readObject(xstream, (VFSLeaf)vfsItem);
		} catch (Exception e) {
			log.error("Cannot read course tree file: " + fileName, e);
			throw new CorruptedCourseException("Cannot resolve file: " + fileName + " course=" + toString(), e);
		}
	}

	@Override
	public String getResourceableTypeName() {
		return ORES_TYPE_NAME;
	}

	@Override
	public Long getResourceableId() {
		return resourceableId;
	}

	/**
	 * Package private. Only used by CourseFactory.
	 * 
	 * @param editorTreeModel
	 */
	void setEditorTreeModel(CourseEditorTreeModel editorTreeModel) {
		this.editorTreeModel = editorTreeModel;
	}

	/**
	 * Package private. Only used by CourseFactory.
	 * 
	 * @param runStructure
	 */
	void setRunStructure(Structure runStructure) {
		this.runStructure = runStructure;
		initHasAssessableNodes();
	}

	/**
	 * This should only be called via the CourseFactory, since it has to update the course cache. <p>
	 * Sets the course configuration.
	 * @param courseConfig
	 */
	protected void setCourseConfig(CourseConfig courseConfig) {
		this.courseConfig = courseConfig;
		CoreSpringFactory.getImpl(CourseConfigManager.class).saveConfigTo(this, courseConfig);
	}
	
	@Override
	public CourseConfig getCourseConfig() {
		return courseConfig;
	}
		
	/**
	 * Sets information about there are assessable nodes or structure course nodes
	 * (subtype of assessable node), which 'hasPassedConfigured' or 'hasScoreConfigured'
	 * is true in the structure.
	 */
	void initHasAssessableNodes() {
		this.hasAssessableNodes = AssessmentHelper.checkForAssessableNodes(runStructure.getRootNode());
	}

	@Override
	public boolean hasAssessableNodes() {
		return hasAssessableNodes;
	}

	@Override
	public String toString() {
		return "Course:[" + getResourceableId() + "," + getCourseTitle() + "], " + super.toString();
	}

}

class NodePostExportVisitor implements Visitor {
	private final CourseEnvironmentMapper envMapper;
	private final boolean backwardsCompatible;
	
	public NodePostExportVisitor(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		this.envMapper = envMapper;
		this.backwardsCompatible = backwardsCompatible;
	}
	
	@Override
	public void visit(INode node) {
		if(node instanceof CourseEditorTreeNode) {
			node = ((CourseEditorTreeNode)node).getCourseNode();
		}
		if(node instanceof CourseNode) {
			((CourseNode)node).postExport(envMapper, backwardsCompatible);
		}
	}
}

class NodePostImportVisitor implements Visitor {
	private final ICourse course;
	private final File importDirectory;
	private final Processing processType;
	private final CourseEnvironmentMapper envMapper;
	
	public NodePostImportVisitor(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
		this.course = course;
		this.envMapper = envMapper;
		this.processType = processType;
		this.importDirectory = importDirectory;
	}
	
	@Override
	public void visit(INode node) {
		if(node instanceof CourseEditorTreeNode) {
			node = ((CourseEditorTreeNode)node).getCourseNode();
		}
		if(node instanceof CourseNode) {
			((CourseNode)node).postImport(importDirectory, course, envMapper, processType);
		}
	}
}

class NodePostCopyVisitor implements Visitor {
	
	private final Processing processType;
	private final CourseEnvironmentMapper envMapper;
	private final ICourse course;
	private final ICourse sourceCourse;
	
	public NodePostCopyVisitor(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse) {
		this.envMapper = envMapper;
		this.processType = processType;
		this.course = course;
		this.sourceCourse = sourceCourse;
	}
	
	@Override
	public void visit(INode node) {
		if(node instanceof CourseEditorTreeNode) {
			node = ((CourseEditorTreeNode)node).getCourseNode();
		}
		if(node instanceof CourseNode) {
			((CourseNode)node).postCopy(envMapper, processType, course, sourceCourse);
		}
	}
}

class NodeExportVisitor implements Visitor {

	private File exportDirectory;
	private ICourse course;

	/**
	 * Constructor of the node deletion visitor
	 * 
	 * @param exportDirectory
	 * @param course
	 */
	public NodeExportVisitor(File exportDirectory, ICourse course) {
		this.exportDirectory = exportDirectory;
		this.course = course;
	}

	/**
	 * Visitor pattern to delete the course nodes
	 * 
	 * @see org.olat.core.util.tree.Visitor#visit(org.olat.core.util.nodes.INode)
	 */
	@Override
	public void visit(INode node) {
		CourseEditorTreeNode cNode = (CourseEditorTreeNode) node;
		cNode.getCourseNode().exportNode(exportDirectory, course);
		// Do frequent intermediate commits to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();
	}

}
