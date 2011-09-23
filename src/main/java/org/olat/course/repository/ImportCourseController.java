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

package org.olat.course.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.commons.file.filechooser.FileChooserController;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemFileTypeFilter;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManagerImpl;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Initial Date:  13.05.2005
 *
 * @author Mike Stock
 */
public class ImportCourseController extends BasicController implements IAddController {

	private OLATResource newCourseResource;
	private ICourse course;//o_clusterOK: creation process
	private File fCourseImportZIP;
	private RepositoryAddCallback callback;
	
	private FileChooserController cfc;
	private Controller activeImportController;
	private ImportSharedfolderReferencesController sharedFolderImportController;
	private ImportGlossaryReferencesController glossaryImportController;
	private List nodeList = new ArrayList();
	private int nodeListPos = 0;
	private Panel myPanel;
	private static final VFSItemFileTypeFilter zipTypeFilter = new VFSItemFileTypeFilter(new String[] { "zip" });

	/**
	 * Import a course from a previous export.
	 * 
	 * @param callback
	 * @param ureq
	 * @param wControl 
	 */
	public ImportCourseController(RepositoryAddCallback callback, UserRequest ureq, WindowControl wControl) { 
		super(ureq,wControl);
		this.callback = callback;
		myPanel = new Panel("importPanel");
		myPanel.addListener(this);
		
		// prepare generic filechoser for add file
		removeAsListenerAndDispose(cfc);
		cfc = new FileChooserController(ureq, getWindowControl(),(int)QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO).getUlLimitKB().longValue(), false);
		listenTo(cfc);
		
		cfc.setSuffixFilter(zipTypeFilter);
		myPanel.setContent(cfc.getInitialComponent());
		this.putInitialPanel(myPanel);
	}
	
	/**
	 * @see org.olat.repository.controllers.IAddController#getTransactionComponent()
	 */
	public Component getTransactionComponent() {
		return getInitialComponent();
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionFinishBeforeCreate()
	 */
	public boolean transactionFinishBeforeCreate() {
		// create group management
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		cgm.createCourseGroupmanagement(course.getResourceableId().toString());
		// import groups
		cgm.importCourseLearningGroups(getExportDataDir(course));
		cgm.importCourseRightGroups(getExportDataDir(course));
		return true;
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {
		// Create course admin policy for owner group of repository entry
		// -> All owners of repository entries are course admins
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		secMgr.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());
		// set root node title
						
		course = CourseFactory.getCourseEditSession(re.getOlatResource().getResourceableId());
		String displayName = re.getDisplayname();
		course.getRunStructure().getRootNode().setShortTitle(Formatter.truncateOnly(displayName, 25)); //do not use truncate!
		course.getRunStructure().getRootNode().setLongTitle(displayName);
		//course.saveRunStructure();
		CourseEditorTreeNode editorRootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode());
		editorRootNode.getCourseNode().setShortTitle(Formatter.truncateOnly(displayName, 25)); //do not use truncate!
		editorRootNode.getCourseNode().setLongTitle(displayName);
		// mark entire structure as dirty/new so the user can re-publish
		markDirtyNewRecursively(editorRootNode);
		// root has already been created during export. Unmark it.
		editorRootNode.setNewnode(false);		
		
		CourseFactory.saveCourse(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(),true);
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		cleanupExportDataDir();
		if (course != null){
			CourseFactory.deleteCourse(newCourseResource);
			course = null;
		}
	}

	/**
	 * Mark whole tree (incl. root node) "dirty" and "new" recursively.
	 * 
	 * @param editorRootNode
	 */
	private void markDirtyNewRecursively(CourseEditorTreeNode editorRootNode) {
		editorRootNode.setDirty(true);
		editorRootNode.setNewnode(true);
		if (editorRootNode.getChildCount() > 0) {
			for (int i = 0; i < editorRootNode.getChildCount(); i++) {
				markDirtyNewRecursively((CourseEditorTreeNode)editorRootNode.getChildAt(i));
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//nothing to do
/*		if (source == finishedMessage) {
			getWindowControl().pop();
			// save the editor tree model, to persist any changes made during import.
			course.saveEditorTreeModel();
			callback.finished(ureq);
		}*/
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == cfc) {
			if (event.equals(Event.DONE_EVENT)) {
				// create new repository entry
				if (cfc.isFileFromFolder()) {
					VFSLeaf vfsLeaf = cfc.getFileSelection();
					if (!(vfsLeaf instanceof LocalFileImpl)) {
						callback.failed(ureq);
						return;
					}
					fCourseImportZIP = ((LocalFileImpl)vfsLeaf).getBasefile();
				}	else {
					fCourseImportZIP = cfc.getUploadedFile();
				}
				newCourseResource = OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
				course = CourseFactory.importCourseFromZip(newCourseResource, fCourseImportZIP);
				// cfc.release();
				if (course == null) {
					callback.failed(ureq);
					return;
				}
				// create empty run structure
				course = CourseFactory.openCourseEditSession(course.getResourceableId());
				Structure runStructure = course.getRunStructure();
				runStructure.getRootNode().removeAllChildren();			
				
				CourseFactory.saveCourse(course.getResourceableId());
				//CourseFactory.closeCourseEditSession(course.getResourceableId());
				
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				callback.canceled(ureq);
				return;
			}
			callback.setResourceable(newCourseResource);
			callback.setResourceName(fCourseImportZIP.getName());
			
			final File exportDir = new File(course.getCourseBaseContainer().getBasefile(), "/"+ICourse.EXPORTED_DATA_FOLDERNAME);
			if(exportDir.exists() && exportDir.canRead()){
				final RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(exportDir);
				callback.setDisplayName(importExport.getDisplayName());
				callback.setDescription(importExport.getDescription());
			}else{
				logError("Directory "+exportDir.getAbsolutePath()+" not found", new FileNotFoundException());
			}
			// collect all nodes
			collectNodesAsList((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode(), nodeList);
			nodeListPos = 0;
			boolean finished = processNodeList(ureq);
			if (finished) {
				// no node wanted to provide a controller to import its stuff. We're finished processing the nodes.
				// now process any shared folder reference...
				CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
				if (courseConfig.hasCustomSharedFolder()) {
					processSharedFolder(ureq);
				} 
				else if (courseConfig.hasGlossary()) {
					processGlossary(ureq);
				} 
				else {
					// only when no sharedFolder and no glossary 
					//getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
					// save the editor tree model, to persist any changes made during import.					
					CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
					callback.finished(ureq);
				}
			}
		} else if (source == activeImportController) {
			if (event == Event.DONE_EVENT) {
				// continues to search through the list of nodes
				boolean finished = processNodeList(ureq);
				if (finished) {
					CourseConfig courseConfig = CourseConfigManagerImpl.getInstance().loadConfigFor(course);
					if (courseConfig.hasCustomSharedFolder()) {
						processSharedFolder(ureq);
					} else if (courseConfig.hasGlossary()) {
						processGlossary(ureq);
					} else {
						//getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
						// save the editor tree model, to persist any changes made during import.						
						CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
						callback.finished(ureq);
					}
				}
			} else if (event == Event.CANCELLED_EVENT) {
				callback.canceled(ureq);
				return;
			} else if (event == Event.FAILED_EVENT) {
				callback.canceled(ureq);
				showError("add.failed");
				return;
			}
		} else if (source == sharedFolderImportController) {
			if (event == Event.DONE_EVENT) {
				CourseConfig courseConfig = CourseConfigManagerImpl.getInstance().loadConfigFor(course);
				if (courseConfig.hasGlossary()) {
					processGlossary(ureq);
				} else {
					//getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
					// save the editor tree model, to persist any changes made during import.					
					CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
					callback.finished(ureq);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				callback.canceled(ureq);
				//FIXME: this does not remove all data from the database, see repositoryManger
				if (course != null) CourseFactory.deleteCourse(newCourseResource);
				return;
			} else if (event == Event.FAILED_EVENT) {
				callback.canceled(ureq);
				showError("add.failed");
				return;
			}
		} else if (source == glossaryImportController) {
				if (event == Event.DONE_EVENT) {
					//getWindowControl().pushAsModalDialog(translator.translate("import.suc.title"), finishedMessage);
					// save the editor tree model, to persist any changes made during import.					
					CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
					callback.finished(ureq);
				} else if (event == Event.CANCELLED_EVENT) {
					callback.canceled(ureq);
					//FIXME: this does not remove all data from the database, see repositoryManger
					if (course != null) CourseFactory.deleteCourse(newCourseResource);
					return;
				} else if (event == Event.FAILED_EVENT) {
					callback.canceled(ureq);
					showError("add.failed");
					return;
				}		
			}
	}

	private void processSharedFolder(UserRequest ureq) {
		// if shared folder controller exists we did already import this one.
		if (sharedFolderImportController == null) {
			RepositoryEntryImportExport sfImportExport = SharedFolderManager.getInstance().getRepositoryImportExport(getExportDataDir(course));
			
			removeAsListenerAndDispose(sharedFolderImportController);
			sharedFolderImportController = new ImportSharedfolderReferencesController(sfImportExport, course, ureq, getWindowControl());
			listenTo(sharedFolderImportController);
			
			myPanel.setContent(sharedFolderImportController.getInitialComponent());
		}
	}

	private void processGlossary(UserRequest ureq) {
		// if glossary controller exists we did already import this one.
		if (glossaryImportController == null) {
			RepositoryEntryImportExport sfImportExport = GlossaryManager.getInstance().getRepositoryImportExport(getExportDataDir(course));
			
			removeAsListenerAndDispose(glossaryImportController);
			glossaryImportController = new ImportGlossaryReferencesController(sfImportExport, course, ureq, getWindowControl());
			listenTo(glossaryImportController);
			
			myPanel.setContent(glossaryImportController.getInitialComponent());
		}
	}
	
	private void cleanupExportDataDir() {
		if (course == null) return;
		File fExportedDataDir = getExportDataDir(course);
		if (fExportedDataDir.exists())
			FileUtils.deleteDirsAndFiles(fExportedDataDir, true, true);
	}
	
	/**
	 * Collect all nodes as list.
	 * 
	 * @param rootNode
	 * @param nl
	 */
	public static void collectNodesAsList(CourseEditorTreeNode rootNode, List nl) {
		nl.add(rootNode);
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			collectNodesAsList((CourseEditorTreeNode)rootNode.getChildAt(i), nl);
		}
	}
	
	/**
	 * Process the list of nodes to import. Call importNode on each node, starting at currentPos
	 * in the list of nodes. If a node provides a Controller, set the activeImportController to
	 * the Controller returned by the importNode(), active this controller and return false.
	 * The calling method should then just exit its event() method and yield control to the
	 * activeImportController. When the activeImportController is finished, it sends a Event.DONE_EVENT
	 * and this controller continues to process the nodes in the list.
	 * 
	 * @param ureq
	 * @return True if the whole list is processed, false otherwise.
	 */
	private boolean processNodeList(UserRequest ureq) {
		while (nodeListPos < nodeList.size()) {
			CourseEditorTreeNode nextNode = (CourseEditorTreeNode)nodeList.get(nodeListPos);
			nodeListPos++;
			Controller ctrl = nextNode.getCourseNode().importNode(getExportDataDir(course), course, false, ureq, getWindowControl());
			if (ctrl != null) {
				// this node needs a controller to do its import job.
				removeAsListenerAndDispose(activeImportController);
				activeImportController = ctrl;
				listenTo(activeImportController);
				
				myPanel.setContent(activeImportController.getInitialComponent());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * The folder where nodes export their data to.
	 * 
	 * @param theCourse
	 * @return File
	 */
	public static File getExportDataDir(ICourse theCourse) {
		OlatRootFolderImpl vfsExportDir = (OlatRootFolderImpl)theCourse.getCourseBaseContainer().resolve(ICourse.EXPORTED_DATA_FOLDERNAME);
		if (vfsExportDir == null)
			vfsExportDir = (OlatRootFolderImpl)theCourse.getCourseBaseContainer().createChildContainer(ICourse.EXPORTED_DATA_FOLDERNAME);
		return vfsExportDir.getBasefile();
	}
	
	protected void doDispose() {
		if (course != null) CourseFactory.closeCourseEditSession(course.getResourceableId(), false);
	}

}
