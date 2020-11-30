/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required  applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted <br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodes.document.DocumentSecurityCallback;
import org.olat.course.nodes.document.DocumentSecurityCallbackFactory;
import org.olat.course.nodes.document.DocumentSource;
import org.olat.course.nodes.document.ui.DocumentEditController;
import org.olat.course.nodes.document.ui.DocumentPeekviewController;
import org.olat.course.nodes.document.ui.DocumentRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 13 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 6299893052294231498L;
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(DocumentRunController.class);

	public static final String TYPE = "document";
	public static final String ICON_CSS = "o_filetype_file";

	// Configs
	private static final int CURRENT_VERSION = 2;
	public static final String CONFIG_DOC_COURSE_REL_PATH = "doc.course.folder";
	public static final String CONFIG_DOC_REPO_SOFT_KEY = "doc.repo";
	public static final String CONFIG_HEIGHT_AUTO = "auto";
	public static final String CONFIG_KEY_HEIGHT = "height";
	public static final String CONFIG_KEY_EDIT_OWNER = "edit.owner";
	public static final String CONFIG_KEY_EDIT_COACH = "edit.coach";
	public static final String CONFIG_KEY_EDIT_PARTICIPANT = "edit.participant";
	public static final String CONFIG_KEY_EDIT_GUEST = "edit.guest";
	public static final String CONFIG_KEY_DOWNLOAD_OWNER = "download.owner";
	public static final String CONFIG_KEY_DOWNLOAD_COACH = "download.coach";
	public static final String CONFIG_KEY_DOWNLOAD_PARTICIPANT = "download.participant";
	public static final String CONFIG_KEY_DOWNLOAD_GUEST = "download.guest";
	
	public DocumentCourseNode() {
		this(null);
	}
		
	public DocumentCourseNode(INode parent) {
		super(TYPE, parent);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		VFSContainer courseFolderCont = course.getCourseFolderContainer(CourseContainerOptions.withoutElements());
		DocumentEditController editCtrl = new DocumentEditController(ureq, wControl, stackPanel, this, courseFolderCont);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, course, chosenNode, userCourseEnv, editCtrl);
		nodeEditCtr.addControllerListener(editCtrl);
		return nodeEditCtr;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		VFSContainer courseFolderCont = userCourseEnv.getCourseEnvironment().getCourseFolderContainer(CourseContainerOptions.withoutElements());
		VFSLeaf vfsLeaf = getDocumentSource(courseFolderCont).getVfsLeaf();
		
		DocumentSecurityCallback securityCallback = DocumentSecurityCallbackFactory.createSecurityCallback(this, userCourseEnv);
		Controller controller = new DocumentRunController(ureq, wControl, this, securityCallback, courseFolderCont, "o_cnd_run");
		
		String iconCssClass = vfsLeaf != null? CSSHelper.createFiletypeIconCssClassFor(vfsLeaf.getName()): ICON_CSS;
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, iconCssClass);
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		if (nodeSecCallback.isAccessible()) {
			Long courseRepoKey = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
			VFSContainer courseFolderCont = userCourseEnv.getCourseEnvironment().getCourseFolderContainer(CourseContainerOptions.withoutElements());
			return new DocumentPeekviewController(ureq, wControl, this, courseRepoKey, courseFolderCont);
		}
		return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		StatusDescription sd = StatusDescription.NOERROR;
		boolean documentSelected = getModuleConfiguration().has(CONFIG_DOC_COURSE_REL_PATH)
				|| getModuleConfiguration().has(CONFIG_DOC_REPO_SOFT_KEY);
		if (!documentSelected) {
			String shortKey = "error.no.document.short";
			String longKey = "error.no.document";
			String[] params = new String[] { this.getShortTitle() };
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, TRANSLATOR_PACKAGE);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(DocumentEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE,
				getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		if (getModuleConfiguration().has(CONFIG_DOC_REPO_SOFT_KEY)) {
			String softKey =  getModuleConfiguration().getStringValue(DocumentCourseNode.CONFIG_DOC_REPO_SOFT_KEY);
			RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
			return repositoryManager.lookupRepositoryEntryBySoftkey(softKey, false);
		}
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return getModuleConfiguration().has(CONFIG_DOC_REPO_SOFT_KEY);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			config.setBooleanEntry(CONFIG_KEY_EDIT_OWNER, true);
			config.setBooleanEntry(CONFIG_KEY_EDIT_COACH, false);
			config.setBooleanEntry(CONFIG_KEY_EDIT_PARTICIPANT, false);
			config.setBooleanEntry(CONFIG_KEY_EDIT_GUEST, false);
			config.setBooleanEntry(CONFIG_KEY_DOWNLOAD_OWNER, true);
			config.setBooleanEntry(CONFIG_KEY_DOWNLOAD_COACH, true);
			config.setBooleanEntry(CONFIG_KEY_DOWNLOAD_PARTICIPANT, true);
			config.setBooleanEntry(CONFIG_KEY_DOWNLOAD_GUEST, true);
		}
		if (config.getConfigurationVersion() < 2) {
			config.set(CONFIG_KEY_HEIGHT, CONFIG_HEIGHT_AUTO);
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	public void setDocumentFromCourseFolder(String selectedRelativeItemPath) {
		getModuleConfiguration().setStringValue(CONFIG_DOC_COURSE_REL_PATH, selectedRelativeItemPath);
		getModuleConfiguration().remove(CONFIG_DOC_REPO_SOFT_KEY);
	}

	public void setDocumentFromRepository(RepositoryEntry entry) {
		getModuleConfiguration().setStringValue(CONFIG_DOC_REPO_SOFT_KEY, entry.getSoftkey());
		getModuleConfiguration().remove(CONFIG_DOC_COURSE_REL_PATH);
	}
	
	public DocumentSource getDocumentSource(VFSContainer courseFolderCont) {
		VFSLeaf vfsLeaf = null;
		RepositoryEntry entry = null;
		ModuleConfiguration config = getModuleConfiguration();
		if (config.has(DocumentCourseNode.CONFIG_DOC_COURSE_REL_PATH)) {
			String relPath = config.getStringValue(DocumentCourseNode.CONFIG_DOC_COURSE_REL_PATH);
			VFSItem vfsItem = courseFolderCont.resolve(relPath);
			if (vfsItem instanceof VFSLeaf) {
				vfsLeaf = (VFSLeaf)vfsItem;
			}
		} else if (config.has(DocumentCourseNode.CONFIG_DOC_REPO_SOFT_KEY)) {
			String softKey =  config.getStringValue(DocumentCourseNode.CONFIG_DOC_REPO_SOFT_KEY);
			RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
			entry = repositoryManager.lookupRepositoryEntryBySoftkey(softKey, false);
			if (entry != null) {
				OLATResource resource = entry.getOlatResource();
				VFSContainer fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
				for (VFSItem item : fResourceFileroot.getItems(new VFSSystemItemFilter())) {
					if (item instanceof VFSLeaf) {
						vfsLeaf = (VFSLeaf)item;
					}
				}
			}
		}
		return new DocumentSource(vfsLeaf, entry);
	}
	
}
