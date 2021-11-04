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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.editor.importnodes;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.editor.NodeConfigController;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCourseNodesFinishStepCallback implements StepRunnerCallback {
	
	private static final Logger log = Tracing.createLoggerFor(ImportCourseNodesFinishStepCallback.class);
	
	private final Long targetCourseId;
	private final RepositoryEntry targetEntry;
	private final ImportCourseNodesContext importCourseContext;
	
	public ImportCourseNodesFinishStepCallback(RepositoryEntry targetCourse, ImportCourseNodesContext importCourseContext) {
		this.importCourseContext = importCourseContext;
		this.targetEntry = targetCourse;
		targetCourseId = targetEntry.getOlatResource().getResourceableId();
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper(targetEntry, importCourseContext.getEntry());
		envMapper.setAuthor(ureq.getIdentity());
		
		importCourseFiles(envMapper);
		importCourseNodes(ureq, envMapper);
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void importCourseFiles(CourseEnvironmentMapper envMapper) {
		List<ImportCourseFile> courseFolderFiles = importCourseContext.getCourseFolderFiles();
		ICourse targetCourse = CourseFactory.loadCourse(importCourseContext.getTargetEntry());
		VFSContainer targetCourseFolderCont = targetCourse.getCourseFolderContainer(CourseContainerOptions.courseFolder());
		for(ImportCourseFile file:courseFolderFiles) {
			if(file.getParent() == null) {
				recursiveCopyCourseFolderFiles(file, targetCourseFolderCont, targetCourseFolderCont, envMapper);
			}
		}
	}
	
	private void recursiveCopyCourseFolderFiles(ImportCourseFile file, VFSContainer targetParent, VFSContainer targetCourseFolderCont, CourseEnvironmentMapper envMapper) {
		VFSItem item = file.getOriginalItem();
		String targetFilename = item.getName();
		if(file.isRenamed()) {
			targetFilename = file.getRenamedFilename();
		}
		VFSItem targetItem = targetParent.resolve(targetFilename);
		
		if(item instanceof VFSContainer) {
			VFSContainer targetContainer;
			if(targetItem == null) {
				targetContainer = targetParent.createChildContainer(targetFilename);
			} else if(targetItem instanceof VFSContainer) {
				targetContainer = (VFSContainer)targetItem;
			} else {
				return; // problem
			}
			file.setTargetItem(targetContainer);
			handleRenamimgCourseFolderFile(file, targetContainer, targetCourseFolderCont, envMapper); 
			for(ImportCourseFile child:file.getChildren()) {
				recursiveCopyCourseFolderFiles(child, targetContainer, targetCourseFolderCont, envMapper);
			}
		} else if(item instanceof VFSLeaf && targetItem == null) {
			VFSLeaf targetLeaf = targetParent.createChildLeaf(targetFilename);
			VFSManager.copyContent((VFSLeaf)item, targetLeaf, true, null);
			file.setTargetItem(targetLeaf);
			handleRenamimgCourseFolderFile(file, targetLeaf, targetCourseFolderCont, envMapper); 
		}
	}
	
	private void handleRenamimgCourseFolderFile(ImportCourseFile file, VFSItem targetItem, VFSContainer targetCourseFolderCont,
			CourseEnvironmentMapper envMapper) {
		boolean renamed = file.isRenamed();
		for(ImportCourseFile parent=file.getParent(); !renamed && parent != null; parent=parent.getParent()) {
			renamed |= parent.isRenamed();
		}
		if(renamed && file.getCourseFolderSubPath() != null) {
			String newVal = VFSManager.getRelativeItemPath(targetItem, targetCourseFolderCont, "");
			envMapper.addRenamedPath(file.getCourseFolderSubPath(), newVal);
		}
	}
	
	private void importCourseNodes(UserRequest ureq, CourseEnvironmentMapper envMapper) {
		CourseEditorTreeNode targetNode = importCourseContext.getCopyTo();
		try {
			List<ImportCourseNode> nodes = importCourseContext.getSelectedNodes();
			int pos = 0;
			CourseEditorTreeNode selectedNode;
			if(targetNode.getParent() == null) {
				//root, add as last child
				pos = targetNode.getChildCount();
				selectedNode = targetNode;
			} else {
				selectedNode = (CourseEditorTreeNode)targetNode.getParent();
				pos = targetNode.getPosition() + 1;
			}
			
			// create the course nodes and their ident's
			for(ImportCourseNode node:nodes) {
				if(node.getParent() == null) {
					recursiveCopy(node, selectedNode.getCourseNode(), pos++, ureq.getIdentity(), envMapper);
				}
			}
			// configure them
			for(ImportCourseNode node:nodes) {
				if(node.getParent() == null) {
					recursiveConfigure(node, envMapper);
				}
			}

			CourseFactory.saveCourseEditorTreeModel(targetCourseId);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void recursiveConfigure(ImportCourseNode node, CourseEnvironmentMapper envMapper) {
		if(!node.isExcludeFromImport()) {
			PersistingCourseImpl targetCourse = CourseFactory.getCourseEditSession(targetCourseId);
			ICourse sourceCourse = CourseFactory.loadCourse(importCourseContext.getEntry());
			
			CourseNode srcCourseNode = node.getCourseNode();
			CourseNode targetCourseNode = targetCourse.getEditorTreeModel()
					.getCourseNode(node.getTargetCourseNodeIdent());
	
			ImportSettings settings = new ImportSettings();
			settings.setCopyType(node.getImportSetting());
			
			targetCourseNode.postImportCourseNodes(targetCourse, srcCourseNode, sourceCourse, settings, envMapper);
		}
		
		int numOfChildren = node.getChildren().size();
		for (int i = 0; i<numOfChildren; i++) {
			ImportCourseNode child = node.getChildren().get(i);
			recursiveConfigure(child, envMapper);
		}
	}
	
	private void recursiveCopy(ImportCourseNode node, CourseNode parentNode, int pos, Identity identity, CourseEnvironmentMapper envMapper) {
		if(!node.isExcludeFromImport()) {
			PersistingCourseImpl targetCourse = CourseFactory.getCourseEditSession(targetCourseId);
			
			CourseEditorTreeNode sourceEditorTreeNode = node.getEditorTreeNode();
			
			// create copy of course node
			CourseNode targetCourseNode = createCopy(sourceEditorTreeNode.getCourseNode());
			envMapper.addNodeIdentKeyPair(sourceEditorTreeNode.getCourseNode().getIdent(), targetCourseNode.getIdent());
			
			// Remove the parent and the children references as the new node ends up in the editortreemodel,
			// where the tree structure is not in the Course Nodes but in the EditorTreeNodes.
			targetCourseNode.setParent(null);
			targetCourseNode.removeAllChildren();
			
			// Insert at desired position		
			CourseEditorTreeNode targetEditorCourseNode = targetCourse.getEditorTreeModel()
					.insertCourseNodeAt(targetCourseNode, parentNode, pos);
			targetEditorCourseNode.setNewnode(true);
			node.setTargetCourseNodeIdent(targetCourseNode.getIdent());
			if(importCourseContext.getFirstNode() == null) {
				importCourseContext.setFirstNode(targetEditorCourseNode);
			}
			
			parentNode = targetCourseNode;	
		}
		
		int numOfChildren = node.getChildren().size();
		for (int i = 0; i<numOfChildren; i++) {
			ImportCourseNode child = node.getChildren().get(i);
			recursiveCopy(child, parentNode, i, identity, envMapper);
		}
	}
	
	private CourseNode createCopy(CourseNode courseNode) {
		CourseNode copyInstance = (CourseNode) XStreamHelper.xstreamClone(courseNode);
		copyInstance.setIdent(String.valueOf(CodeHelper.getForeverUniqueID()));
		copyInstance.setPreConditionVisibility(null);
		if (CourseNodeHelper.isCustomShortTitle(courseNode.getLongTitle(), courseNode.getShortTitle())) {
			copyInstance.setShortTitle(Formatter.truncateOnly(courseNode.getShortTitle(), NodeConfigController.SHORT_TITLE_MAX_LENGTH));
		} else {
			copyInstance.setShortTitle(null);
		}
		copyInstance.setLongTitle(courseNode.getLongTitle());
		return copyInstance;
	}
}
