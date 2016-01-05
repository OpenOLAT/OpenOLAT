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
package org.olat.course;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.bc.BCCourseNodeEditController;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibleTreeFilter;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.resource.OLATResource;

/**
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MergedCourseContainer extends MergeSource {
	
	private static final OLog log = Tracing.createLoggerFor(MergedCourseContainer.class);
	
	private final Long courseId;
	private final IdentityEnvironment identityEnv;
	
	public MergedCourseContainer(Long courseId, String name) {
		this(courseId, name, null);
	}
	
	public MergedCourseContainer(Long courseId, String name, IdentityEnvironment identityEnv) {
		super(null, name);
		this.courseId = courseId;
		this.identityEnv = identityEnv;
	}
	
	@Override
	protected void init() {
		ICourse course = CourseFactory.loadCourse(courseId);
		if(course instanceof PersistingCourseImpl) {
			init((PersistingCourseImpl)course);
		}
	}
	
	protected void init(PersistingCourseImpl persistingCourse) {
		super.init();

		if(identityEnv == null || identityEnv.getRoles().isOLATAdmin()) {
			addContainersChildren(persistingCourse.getIsolatedCourseFolder(), true);
		} else {
			RepositoryEntry re = persistingCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			RepositoryEntrySecurity reSecurity = RepositoryManager.getInstance()
					.isAllowed(identityEnv.getIdentity(), identityEnv.getRoles(), re);
			if(reSecurity.isEntryAdmin()) {
				addContainersChildren(persistingCourse.getIsolatedCourseFolder(), true);
			}
		}
			
		// grab any shared folder that is configured
		OlatRootFolderImpl sharedFolder = null;
		String sfSoftkey = persistingCourse.getCourseConfig().getSharedFolderSoftkey();
		if (StringHelper.containsNonWhitespace(sfSoftkey) && !CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(sfSoftkey)) {
			OLATResource sharedResource = CoreSpringFactory.getImpl(RepositoryService.class)
					.loadRepositoryEntryResourceBySoftKey(sfSoftkey);
			if (sharedResource != null) {
				sharedFolder = SharedFolderManager.getInstance().getSharedFolder(sharedResource);
				if (sharedFolder != null) {
					sharedFolder.setLocalSecurityCallback(new ReadOnlyCallback());
					//add local course folder's children as read/write source and any sharedfolder as subfolder
					addContainer(new NamedContainerImpl("_sharedfolder", sharedFolder));
				}
			}
		}
			
		// add all course building blocks of type BC to a virtual folder
		MergeSource nodesContainer = new MergeSource(null, "_courseelementdata");
		if(identityEnv == null) {
			CourseNode rootNode = persistingCourse.getRunStructure().getRootNode();
			addFolderBuildingBlocks(persistingCourse, nodesContainer, rootNode);
		} else {
			TreeEvaluation treeEval = new TreeEvaluation();
			GenericTreeModel treeModel = new GenericTreeModel();
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, persistingCourse.getCourseEnvironment());
			CourseNode rootCn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
			NodeEvaluation rootNodeEval = rootCn.eval(userCourseEnv.getConditionInterpreter(), treeEval, new VisibleTreeFilter());
			TreeNode treeRoot = rootNodeEval.getTreeNode();
			treeModel.setRootNode(treeRoot);
			addFolderBuildingBlocks(persistingCourse, nodesContainer, treeRoot);
		}
		
		if (nodesContainer.getItems().size() > 0) {
			addContainer(nodesContainer);
		}
	}
	
	private void addFolderBuildingBlocks(PersistingCourseImpl course, MergeSource nodesContainer, TreeNode courseNode) {
		if(courseNode == null) return;
		
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			TreeNode child = (TreeNode)courseNode.getChildAt(i);
			
			NodeEvaluation nodeEval;
			if(child.getUserObject() instanceof NodeEvaluation) {
				nodeEval = (NodeEvaluation)child.getUserObject();
			} else {
				continue;
			}
			
			if(nodeEval != null && nodeEval.getCourseNode() != null) {
				CourseNode courseNodeChild = nodeEval.getCourseNode();
				String folderName = RequestUtil.normalizeFilename(courseNodeChild.getShortTitle());
				MergeSource courseNodeContainer;
				if (courseNodeChild instanceof BCCourseNode) {
					final BCCourseNode bcNode = (BCCourseNode) courseNodeChild;
					// add folder not to merge source. Use name and node id to have unique name
					VFSContainer rootFolder = null;
					String subpath = bcNode.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH);
					if(StringHelper.containsNonWhitespace(subpath)){
						if(bcNode.isSharedFolder()){
							// grab any shared folder that is configured
							OlatRootFolderImpl sharedFolder = null;
							String sfSoftkey = course.getCourseConfig().getSharedFolderSoftkey();
							if (StringHelper.containsNonWhitespace(sfSoftkey) && !CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(sfSoftkey)) {
								OLATResource sharedResource = CoreSpringFactory.getImpl(RepositoryService.class)
										.loadRepositoryEntryResourceBySoftKey(sfSoftkey);
								if (sharedResource != null) {
									sharedFolder = SharedFolderManager.getInstance().getSharedFolder(sharedResource);
									VFSContainer courseBase = sharedFolder;
									sharedFolder.setLocalSecurityCallback(new ReadOnlyCallback());
									subpath = subpath.replaceFirst("/_sharedfolder", "");
									rootFolder = (VFSContainer) courseBase.resolve(subpath);
								}
							}
						}else{
							VFSContainer courseBase = course.getCourseBaseContainer();
							rootFolder = (VFSContainer) courseBase.resolve("/coursefolder"+subpath);
						}
					}
					if(bcNode.getModuleConfiguration().getBooleanSafe(BCCourseNodeEditController.CONFIG_AUTO_FOLDER)){
						String path = BCCourseNode.getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), bcNode);
						rootFolder = new OlatRootFolderImpl(path, null);
					}

					boolean canDownload = nodeEval.isCapabilityAccessible("download");
					if(canDownload) {
						if(nodeEval.isCapabilityAccessible("upload")) {
							//inherit the security callback from the course as for author
						} else {
							rootFolder.setLocalSecurityCallback(new ReadOnlyCallback());
						}
						
						// add node ident if multiple files have same name
						if (nodesContainer.getItems(new VFSItemFilter() {
							@Override
							public boolean accept(VFSItem vfsItem) {
								return (bcNode.getShortTitle().equals(RequestUtil.normalizeFilename(bcNode.getShortTitle())));
							}
						}).size() > 0) {
							folderName = folderName + " (" + bcNode.getIdent() + ")";
						}
						
						// Create a container for this node content and wrap it with a merge source which is attached to tree
						VFSContainer nodeContentContainer = new NamedContainerImpl(folderName, rootFolder);
						courseNodeContainer = new MergeSource(nodesContainer, folderName);
						courseNodeContainer.addContainersChildren(nodeContentContainer, true);
						nodesContainer.addContainer(courseNodeContainer);	
						// Do recursion for all children
						addFolderBuildingBlocks(course, courseNodeContainer, child);
		
					} else {
						// For non-folder course nodes, add merge source (no files to show) ...
						courseNodeContainer = new MergeSource(null, folderName);
						// , then do recursion for all children ...
						addFolderBuildingBlocks(course, courseNodeContainer, child);
						// ... but only add this container if it contains any children with at least one BC course node
						if (courseNodeContainer.getItems().size() > 0) {
							nodesContainer.addContainer(courseNodeContainer);
						}
					}	
				} else {
					// For non-folder course nodes, add merge source (no files to show) ...
					courseNodeContainer = new MergeSource(null, folderName);
					// , then do recursion for all children ...
					addFolderBuildingBlocks(course, courseNodeContainer, child);
					// ... but only add this container if it contains any children with at least one BC course node
					if (courseNodeContainer.getItems().size() > 0) {
						nodesContainer.addContainer(courseNodeContainer);
					}
				}
			}
		}
	}

	
	/**
	 * internal method to recursively add all course building blocks of type
	 * BC to a given VFS container. This should only be used for an author view,
	 * it does not test for security.
	 * @param course
	 * @param nodesContainer
	 * @param courseNode
	 * @return container for the current course node
	 */
	private void addFolderBuildingBlocks(PersistingCourseImpl course, MergeSource nodesContainer, CourseNode courseNode) {
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			String folderName = RequestUtil.normalizeFilename(child.getShortTitle());
			MergeSource courseNodeContainer;
			if (child instanceof BCCourseNode) {
				final BCCourseNode bcNode = (BCCourseNode) child;
				// add folder not to merge source. Use name and node id to have unique name
				String path;
				VFSContainer rootFolder = null;

				String subpath = bcNode.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH);
				if(StringHelper.containsNonWhitespace(subpath)){
					if(bcNode.isSharedFolder()){
						// grab any shared folder that is configured
						OlatRootFolderImpl sharedFolder = null;
						String sfSoftkey = course.getCourseConfig().getSharedFolderSoftkey();
						if (StringHelper.containsNonWhitespace(sfSoftkey) && !CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(sfSoftkey)) {
							RepositoryManager rm = RepositoryManager.getInstance();
							RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(sfSoftkey, false);
							if (re != null) {
								sharedFolder = SharedFolderManager.getInstance().getSharedFolder(re.getOlatResource());
								VFSContainer courseBase = sharedFolder;
								subpath = subpath.replaceFirst("/_sharedfolder", "");
								rootFolder = (VFSContainer) courseBase.resolve(subpath);
								if(rootFolder != null){
									rootFolder.setLocalSecurityCallback(new ReadOnlyCallback());
								}
							}
						}
					}else{
						VFSContainer courseBase = course.getCourseBaseContainer();
						rootFolder = (VFSContainer) courseBase.resolve("/coursefolder"+subpath);
					}
				}
				if(bcNode.getModuleConfiguration().getBooleanSafe(BCCourseNodeEditController.CONFIG_AUTO_FOLDER)){
					path = BCCourseNode.getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), bcNode);
					rootFolder = new OlatRootFolderImpl(path, null);
				}


				// add node ident if multiple files have same name
				if (nodesContainer.getItems(new VFSItemFilter() {
					@Override
					public boolean accept(VFSItem vfsItem) {
						return (bcNode.getShortTitle().equals(RequestUtil.normalizeFilename(bcNode.getShortTitle())));
					}
				}).size() > 0) {
					folderName = folderName + " (" + bcNode.getIdent() + ")";
				}

 				if(rootFolder != null){
				// Create a container for this node content and wrap it with a merge source which is attached to tree
				VFSContainer nodeContentContainer = new NamedContainerImpl(folderName, rootFolder);
				courseNodeContainer = new MergeSource(nodesContainer, folderName);
				courseNodeContainer.addContainersChildren(nodeContentContainer, true);
				nodesContainer.addContainer(courseNodeContainer);	
				// Do recursion for all children
				addFolderBuildingBlocks(course, courseNodeContainer, child);
 				}
			} else {
				// For non-folder course nodes, add merge source (no files to show) ...
				courseNodeContainer = new MergeSource(null, folderName);
				// , then do recursion for all children ...
				addFolderBuildingBlocks(course, courseNodeContainer, child);
				// ... but only add this container if it contains any children with at least one BC course node
				if (courseNodeContainer.getItems().size() > 0) {
					nodesContainer.addContainer(courseNodeContainer);
				}
			}
		}
	}

	private Object readResolve() {
		try {
			init();
			return this;
		} catch (Exception e) {
			log.error("Cannot init the merged container of a course after deserialization", e);
			return null;
		}
	}
}
