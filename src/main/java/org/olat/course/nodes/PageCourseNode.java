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
package org.olat.course.nodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.page.CoursePageRunController;
import org.olat.course.nodes.page.PageEditController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.manager.PageImportExportHelper;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * 
 * Initial date: 4 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageCourseNode extends AbstractAccessableCourseNode {

	private static final Logger log = Tracing.createLoggerFor(PageCourseNode.class);
	private static final long serialVersionUID = -4565145351110778757L;
	private static final int CURRENT_VERSION = 1;
	public static final String TYPE = "cepage";
	
	private static final NodeRightType EDIT_PAGE = NodeRightTypeBuilder.ofIdentifier("edit_page")
			.setLabel(PageEditController.class, "edit.page")
			.addRole(NodeRightRole.coach, false)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(EDIT_PAGE);

	public PageCourseNode() {
		super(TYPE);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			String pageTitle = StringHelper.containsNonWhitespace(getLongTitle()) ? getLongTitle() : getShortTitle();
			Page page = portfolioService.appendNewPage(null, pageTitle, null, null, null, null);
			
			PageService pageService = CoreSpringFactory.getImpl(PageService.class);
			pageService.createLog(page, doer);
			
			CoreSpringFactory.getImpl(DB.class).commit();
			setPageReferenceKey(page.getKey());
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		PageEditController childTabCntrllr = new PageEditController(ureq, wControl, euce, course, this);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController nodeEditController = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
		// special case: listen to sp edit controller, must be informed when the short title is being modified
		nodeEditController.addControllerListener(childTabCntrllr); 
		return nodeEditController;
	}
	
	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd,
			VisibilityFilter visibilityFilter) {
		boolean canEdit = canEdit(userCourseEnv);
		CoursePageRunController runController = new CoursePageRunController(ureq, wControl, userCourseEnv, this, canEdit);
		return new NodeRunConstructionResult(runController);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}
	
	private boolean canEdit(UserCourseEnvironment userCourseEnv) {
		return userCourseEnv.isAdmin() ||
				CoreSpringFactory.getImpl(NodeRightService.class).isGranted(getModuleConfiguration(), userCourseEnv, EDIT_PAGE);
	}
	
	@Override
	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}
	
	public Long getPageReferenceKey() {
		Object key = getModuleConfiguration().get(PageEditController.CONFIG_KEY_PAGE);
		return key instanceof Long longKey ? longKey : null;
	}
	
	public void setPageReferenceKey(Long key) {
		getModuleConfiguration().set(PageEditController.CONFIG_KEY_PAGE, key);
	}
	
	public void removePageReferenceKey() {
		getModuleConfiguration().remove(PageEditController.CONFIG_KEY_PAGE);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public StatusDescription isConfigValid() {
		return StatusDescription.NOERROR;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		return new StatusDescription[] { StatusDescription.NOERROR };
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		PageService pageService = CoreSpringFactory.getImpl(PageService.class);
		pageService.deletePage(getPageReferenceKey());
		DBFactory.getInstance().commit();
		super.cleanupOnDelete(course);
	}
	
	@Override
	public void exportNode(File fExportDirectory, ICourse course) {
		File fNodeExportDir = new File(fExportDirectory, getIdent());
		fNodeExportDir.mkdirs();
		
		PageService pageService = CoreSpringFactory.getImpl(PageService.class);
		PageImportExportHelper exportHelper = CoreSpringFactory.getImpl(PageImportExportHelper.class);
		Page page = pageService.getFullPageByKey(getPageReferenceKey());
		
		File exportPage = new File(fNodeExportDir, "page.zip");
		try(OutputStream out = new FileOutputStream(exportPage);
				ZipOutputStream zout= new ZipOutputStream(out)) {
			zout.setLevel(9);
			exportHelper.export(page, zout);
		} catch(IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		File fNodeImportDir = new File(importDirectory, getIdent());
		File importPage = new File(fNodeImportDir, "page.zip");
		
		PageImportExportHelper importHelper = CoreSpringFactory.getImpl(PageImportExportHelper.class);
		try(ZipFile zfile= new ZipFile(importPage)) {	
			Page page = importHelper.importPage(zfile, owner);
			if(page != null) {
				setPageReferenceKey(page.getKey());
			}
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		PageCourseNode pageNode = (PageCourseNode)super.createInstanceForCopy(isNewTitle, course, author);
		copyPage(this, pageNode, author);
		return pageNode;
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		copyPage(this, this, envMapper.getAuthor());
	}
	
	@Override // Import course elements wizard
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		copyPage((PageCourseNode)sourceCourseNode, this, envMapper.getAuthor());
	}
	
	private void copyPage(PageCourseNode sourceCourseNode, PageCourseNode targetCourseNode, Identity owner) {
		Long sourcePageKey = sourceCourseNode.getPageReferenceKey();
		PageService pageService = CoreSpringFactory.getImpl(PageService.class);
		Page sourcePage = pageService.getFullPageByKey(sourcePageKey);
		if(sourcePage != null) {
			Page targetPage = pageService.copyPage(owner, sourcePage);
			targetCourseNode.setPageReferenceKey(targetPage.getKey());
		} else {
			targetCourseNode.removePageReferenceKey();
		}
	}

}
