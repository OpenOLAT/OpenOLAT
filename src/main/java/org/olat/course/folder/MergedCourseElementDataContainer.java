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
package org.olat.course.folder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.bc.FolderNodeCallback;
import org.olat.course.nodes.bc.FolderNodeReadOnlyCallback;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 28 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MergedCourseElementDataContainer extends MergeSource {
	
	private static final Logger log = Tracing.createLoggerFor(MergedCourseElementDataContainer.class);
	
	private final Long courseId;
	private boolean initialized = false;
	private final boolean courseReadOnly;
	private final IdentityEnvironment identityEnv;
	
	public MergedCourseElementDataContainer(Long courseId, IdentityEnvironment identityEnv, boolean courseReadOnly) {
		super(null, "_courseelementdata");
		this.courseId = courseId;
		this.identityEnv = identityEnv;
		this.courseReadOnly = courseReadOnly;
	}
	
	public boolean isEmpty() {
		if(initialized) {
			return getItems().isEmpty();
		}
		
		ICourse course = CourseFactory.loadCourse(courseId);
		AtomicInteger count = new AtomicInteger(0);
		if(identityEnv == null) {
			new TreeVisitor(node -> {
				if(node instanceof PFCourseNode || node instanceof BCCourseNode) {
					count.incrementAndGet();
				}
			}, course.getRunStructure().getRootNode(), true).visitAll();
		} else if(course instanceof PersistingCourseImpl) {
			PersistingCourseImpl persistingCourse = (PersistingCourseImpl)course;
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, persistingCourse.getCourseEnvironment());
			NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
			CourseTreeNode rootTreeNode = (CourseTreeNode)nodeAccessService.getCourseTreeModelBuilder(userCourseEnv)
					.withFilter(AccessibleFilter.create())
					.build()
					.getRootNode();
	
			new TreeVisitor(node -> {
				if(node instanceof CourseTreeNode) {
					CourseTreeNode courseTreeNode = (CourseTreeNode)node;
					if (courseTreeNode.getCourseNode() instanceof PFCourseNode || courseTreeNode.getCourseNode() instanceof BCCourseNode) {
						count.incrementAndGet();
					}
				}
			}, rootTreeNode, true).visitAll();
			
		}
		return count.get() == 0;
	}
	
	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			init();
		}
		return super.getItems(filter);
	}

	@Override
	public VFSItem resolve(String path) {
		if(!initialized) {
			init();
		}
		return super.resolve(path);
	}

	@Override
	protected void init() {
		if(initialized) return;
		
		ICourse course = CourseFactory.loadCourse(courseId);
		if(course instanceof PersistingCourseImpl) {
			initialized = true;
			init((PersistingCourseImpl)course);
		}
	}
	
	protected void init(PersistingCourseImpl persistingCourse) {
		if(identityEnv == null) {
			CourseNode rootNode = persistingCourse.getRunStructure().getRootNode();
			addFoldersForAdmin(persistingCourse, this, rootNode);
		} else {
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, persistingCourse.getCourseEnvironment());
			NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
			CourseTreeNode treeRoot = (CourseTreeNode)nodeAccessService.getCourseTreeModelBuilder(userCourseEnv)
					.withFilter(AccessibleFilter.create())
					.build()
					.getRootNode();
			addFolders(persistingCourse, this, treeRoot, userCourseEnv);
		}
	}
	
	private void addFolders(PersistingCourseImpl course, MergeSource nodesContainer, CourseTreeNode courseTreeNode,
			UserCourseEnvironment userCourseEnv) {
		if(courseTreeNode == null) return;
		
		for (int i = 0; i < courseTreeNode.getChildCount(); i++) {
			INode childNode = courseTreeNode.getChildAt(i);
			if (childNode instanceof CourseTreeNode) {
				CourseTreeNode childTreeNode = (CourseTreeNode)childNode;
				if(childTreeNode.getCourseNode() != null) {
					CourseNode courseNodeChild = childTreeNode.getCourseNode();
					String folderName = RequestUtil.normalizeFilename(courseNodeChild.getShortTitle());
					 
					if (courseNodeChild instanceof BCCourseNode) {
						final BCCourseNode bcNode = (BCCourseNode) courseNodeChild;
						NodeEvaluation nodeEval = childTreeNode.getNodeEvaluation();
						// add folder not to merge source. Use name and node id to have unique name
						VFSContainer rootFolder = getBCContainer(course, bcNode, nodeEval, userCourseEnv, false);

						boolean canDownload = bcNode.canDownload(nodeEval);
						if(canDownload && rootFolder != null) {
							if(courseReadOnly) {
								rootFolder.setLocalSecurityCallback(new ReadOnlyCallback());
							} else if(bcNode.canUpload(userCourseEnv, nodeEval)) {
								//inherit the security callback from the course as for author
							} else {
								rootFolder.setLocalSecurityCallback(new ReadOnlyCallback());
							}
							
							folderName = getFolderName(nodesContainer, bcNode, folderName);
							
							// Create a container for this node content and wrap it with a merge source which is attached to tree
							VFSContainer nodeContentContainer = new NamedContainerImpl(folderName, rootFolder);
							MergeSource courseNodeContainer = new MergeSource(nodesContainer, folderName);
							courseNodeContainer.addContainersChildren(nodeContentContainer, true);
							nodesContainer.addContainer(courseNodeContainer);	
							// Do recursion for all children
							addFolders(course, courseNodeContainer, childTreeNode, userCourseEnv);
			
						} else {
							// For non-folder course nodes, add merge source (no files to show) ...
							MergeSource courseNodeContainer = new MergeSource(null, folderName);
							// , then do recursion for all children ...
							addFolders(course, courseNodeContainer, childTreeNode, userCourseEnv);
							// ... but only add this container if it contains any children with at least one BC course node
							if (courseNodeContainer.getItems().size() > 0) {
								nodesContainer.addContainer(courseNodeContainer);
							}
						}	
					} else if (courseNodeChild instanceof PFCourseNode) {
						PFCourseNode pfNode = (PFCourseNode) courseNodeChild;					
						// add folder not to merge source. Use name and node id to have unique name
						folderName = getFolderName(nodesContainer, pfNode, folderName);
						MergedPFCourseNodeContainer courseNodeContainer = new MergedPFCourseNodeContainer(nodesContainer, folderName,
								courseId, pfNode, identityEnv, courseReadOnly, false);					
						addFolders(course, courseNodeContainer, childTreeNode, userCourseEnv);
						nodesContainer.addContainer(courseNodeContainer);
						
					} else {
						// For non-folder course nodes, add merge source (no files to show) ...
						MergeSource courseNodeContainer = new MergeSource(null, folderName);
						// , then do recursion for all children ...
						addFolders(course, courseNodeContainer, childTreeNode, userCourseEnv);
						// ... but only add this container if it contains any children with at least one BC course node
						if (courseNodeContainer.getItems().size() > 0) {
							nodesContainer.addContainer(courseNodeContainer);
						}
					}
				}
			
			}
		}
	}

	
	/**
	 * Internal method to recursively add all course building blocks of type
	 * BC to a given VFS container. This should only be used for an author view,
	 * it does not test for security.
	 * 
	 * @param course
	 * @param nodesContainer
	 * @param courseNode
	 * @return container for the current course node
	 */
	private void addFoldersForAdmin(PersistingCourseImpl course, MergeSource nodesContainer, CourseNode courseNode) {
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			String folderName = RequestUtil.normalizeFilename(child.getShortTitle());
			
			if (child instanceof BCCourseNode) {
				final BCCourseNode bcNode = (BCCourseNode) child;
				// add folder not to merge source. Use name and node id to have unique name
				VFSContainer rootFolder = getBCContainer(course, bcNode, null, null, true);
				if(courseReadOnly) {
					rootFolder.setLocalSecurityCallback(new ReadOnlyCallback());	
				}
				folderName = getFolderName(nodesContainer, bcNode, folderName);

 				if(rootFolder != null) {
 					// Create a container for this node content and wrap it with a merge source which is attached to tree
 					VFSContainer nodeContentContainer = new NamedContainerImpl(folderName, rootFolder);
 					MergeSource courseNodeContainer = new MergeSource(nodesContainer, folderName);
 					courseNodeContainer.addContainersChildren(nodeContentContainer, true);
 					nodesContainer.addContainer(courseNodeContainer);	
 					// Do recursion for all children
 					addFoldersForAdmin(course, courseNodeContainer, child);
 				}
			} else if (child instanceof PFCourseNode) {
				final PFCourseNode pfNode = (PFCourseNode) child;					
				// add folder not to merge source. Use name and node id to have unique name
				folderName = getFolderName(nodesContainer, pfNode, folderName);
				MergedPFCourseNodeContainer courseNodeContainer = new MergedPFCourseNodeContainer(nodesContainer, folderName,
						courseId, pfNode, identityEnv, courseReadOnly, true);
				nodesContainer.addContainer(courseNodeContainer);
				// Do recursion for all children
				addFoldersForAdmin(course, courseNodeContainer, child);
			} else {
				// For non-folder course nodes, add merge source (no files to show) ...
				MergeSource courseNodeContainer = new MergeSource(null, folderName);
				// , then do recursion for all children ...
				addFoldersForAdmin(course, courseNodeContainer, child);
				// ... but only add this container if it contains any children with at least one BC course node
				if (!courseNodeContainer.getItems().isEmpty()) {
					nodesContainer.addContainer(courseNodeContainer);
				}
			}
		}
	}
	
	/**
	 * Add node ident if multiple files have same name
	 * 
	 * @param nodesContainer
	 * @param bcNode
	 * @param folderName
	 * @return
	 */
	private String getFolderName(MergeSource nodesContainer, CourseNode bcNode, String folderName) {
		// add node ident if multiple files have same name
		if (!nodesContainer.getItems(vfsItem -> vfsItem.getName().equals(RequestUtil.normalizeFilename(bcNode.getShortTitle()))).isEmpty()) {
			folderName = folderName + " (" + bcNode.getIdent() + ")";
		}
		return folderName;
	}
	
	private VFSContainer getBCContainer(ICourse course, BCCourseNode bcNode, NodeEvaluation nodeEval,
			UserCourseEnvironment userCourseEnv, boolean isOlatAdmin) {
		CourseNode parent = bcNode.getParent() instanceof CourseNode? (CourseNode)bcNode.getParent(): null;
		bcNode.updateModuleConfigDefaults(false, parent, NodeAccessType.of(course));
		// add folder not to merge source. Use name and node id to have unique name
		VFSContainer rootFolder = null;
		String subpath = bcNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);
		if(StringHelper.containsNonWhitespace(subpath)){
			if(bcNode.isSharedFolder()){
				// grab any shared folder that is configured
				VFSContainer sharedFolder = null;
				String sfSoftkey = course.getCourseConfig().getSharedFolderSoftkey();
				if (StringHelper.containsNonWhitespace(sfSoftkey) && !CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(sfSoftkey)) {
					RepositoryManager rm = RepositoryManager.getInstance();
					RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(sfSoftkey, false);
					if (re != null) {
						sharedFolder = SharedFolderManager.getInstance().getSharedFolder(re.getOlatResource());
						VFSContainer courseBase = sharedFolder;
						subpath = subpath.replaceFirst("/_sharedfolder", "");
						rootFolder = (VFSContainer) courseBase.resolve(subpath);
						if(rootFolder != null) {
							if(course.getCourseConfig().isSharedFolderReadOnlyMount() || courseReadOnly) {
								rootFolder.setLocalSecurityCallback(new ReadOnlyCallback());
							} else if(rootFolder.getLocalSecurityCallback() != null) {
								SubscriptionContext subContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), bcNode);
								rootFolder.setLocalSecurityCallback(new OverrideSubscriptionSecurityCallback(rootFolder.getLocalSecurityCallback(), subContext));
							}
						}
					}
				}
			} else {
				VFSContainer courseBase = course.getCourseBaseContainer();
				rootFolder = (VFSContainer) courseBase.resolve("/coursefolder" + subpath);
				if(rootFolder != null && rootFolder.getLocalSecurityCallback() != null) {
					SubscriptionContext subContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), bcNode);
					rootFolder.setLocalSecurityCallback(new OverrideSubscriptionSecurityCallback(rootFolder.getLocalSecurityCallback(), subContext));
				}
			}
		}
		
		if(bcNode.getModuleConfiguration().getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER)){
			String path = BCCourseNode.getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), bcNode);
			rootFolder = VFSManager.olatRootContainer(path, null);
			if(nodeEval != null) {
				SubscriptionContext subContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), bcNode);
				if(courseReadOnly) {
					rootFolder.setLocalSecurityCallback(new FolderNodeReadOnlyCallback(subContext));	
				} else {
					boolean canDownload = bcNode.canDownload(nodeEval);
					boolean canUpload = userCourseEnv != null
							? bcNode.canUpload(userCourseEnv, nodeEval)
							: true; // is for admin
					rootFolder.setLocalSecurityCallback(new FolderNodeCallback(path, canDownload, canUpload, isOlatAdmin, false, subContext));
				}
			} else {
				VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(this);
				if(secCallback != null) {
					SubscriptionContext subContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), bcNode);
					rootFolder.setLocalSecurityCallback(new OverrideQuotaSecurityCallback(path, secCallback, subContext));
				}
			}
		}
		return rootFolder;
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
	
	private static class OverrideQuotaSecurityCallback implements VFSSecurityCallback {
		
		private final String relPath;
		private Quota overridenQuota;
		private final SubscriptionContext subContext;
		private final VFSSecurityCallback secCallback;
		
		public OverrideQuotaSecurityCallback(String relPath, VFSSecurityCallback secCallback, SubscriptionContext subContext) {
			this.relPath = relPath;
			this.subContext = subContext;
			this.secCallback = secCallback;
		}

		@Override
		public boolean canRead() {
			return secCallback.canRead();
		}

		@Override
		public boolean canWrite() {
			return secCallback.canWrite();
		}

		@Override
		public boolean canCreateFolder() {
			return secCallback.canCreateFolder();
		}

		@Override
		public boolean canDelete() {
			return secCallback.canDelete();
		}

		@Override
		public boolean canList() {
			return secCallback.canList();
		}

		@Override
		public boolean canCopy() {
			return secCallback.canCopy();
		}

		@Override
		public boolean canDeleteRevisionsPermanently() {
			return secCallback.canDeleteRevisionsPermanently();
		}

		@Override
		public Quota getQuota() {
			if(overridenQuota == null) {
				QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
				overridenQuota = qm.getCustomQuota(relPath);
				if (overridenQuota == null) {
					Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
					overridenQuota = qm.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
				}
			}
			return overridenQuota;
		}

		@Override
		public void setQuota(Quota quota) {
			//
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return subContext == null ? secCallback.getSubscriptionContext() : subContext;
		}	
	}
	

	private static class OverrideSubscriptionSecurityCallback implements VFSSecurityCallback {

		private final SubscriptionContext subContext;
		private final VFSSecurityCallback secCallback;
		
		public OverrideSubscriptionSecurityCallback(VFSSecurityCallback secCallback, SubscriptionContext subContext) {
			this.subContext = subContext;
			this.secCallback = secCallback;
		}

		@Override
		public boolean canRead() {
			return secCallback.canRead();
		}

		@Override
		public boolean canWrite() {
			return secCallback.canWrite();
		}

		@Override
		public boolean canCreateFolder() {
			return secCallback.canCreateFolder();
		}

		@Override
		public boolean canDelete() {
			return secCallback.canDelete();
		}

		@Override
		public boolean canList() {
			return secCallback.canList();
		}

		@Override
		public boolean canCopy() {
			return secCallback.canCopy();
		}

		@Override
		public boolean canDeleteRevisionsPermanently() {
			return secCallback.canDeleteRevisionsPermanently();
		}

		@Override
		public Quota getQuota() {
			return secCallback.getQuota();
		}

		@Override
		public void setQuota(Quota quota) {
			//
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return subContext == null ? secCallback.getSubscriptionContext() : subContext;
		}
	}
}
