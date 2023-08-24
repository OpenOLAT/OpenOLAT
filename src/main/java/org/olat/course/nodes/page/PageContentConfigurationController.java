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
package org.olat.course.nodes.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.IconPanel;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent.LabelText;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.PageCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.tools.CourseToolLinkTreeModel;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.ui.PageRunController;
import org.olat.modules.portfolio.ui.PageSettings;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageContentConfigurationController extends BasicController {

	private final Link editLink;
	private final Link previewLink;
	private final IconPanel itemCmp;
	private final IconPanelLabelTextContent contentCmp;
	private final VelocityContainer contentVC;
	private final EmptyState fileNotAvailableCmp;
	
	private Page page;
	private final PageCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	
	private NodeRightsController nodeRightCtrl;
	private PageRunController pageCtrl;
	private LayoutMain3ColsBackController previewLayoutCtr;

	@Autowired
	private PageService pageService;
	@Autowired
	private CourseNodeFactory courseNodeFactory;
	
	public PageContentConfigurationController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, ICourse course, PageCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		
		if(courseNode.getPageReferenceKey() != null) {
			page = pageService.getPageByKey(courseNode.getPageReferenceKey());
			
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			if(page != null && !pageService.hasReference(page, courseEntry, courseNode.getIdent())) {
				pageService.addReference(page, courseEntry, courseNode.getIdent());
			}
		}

		// Main container for everything
		contentVC = createVelocityContainer("config_content");
		contentVC.contextPut("itemAvailable", Boolean.valueOf(page != null));
		putInitialPanel(contentVC);
		
		fileNotAvailableCmp = EmptyStateFactory.create("item.not.available", contentVC, this);
		fileNotAvailableCmp.setIconCss("o_icon o_filetype_html");
		fileNotAvailableCmp.setMessageI18nKey("page.not.available.message");
		
		itemCmp = new IconPanel("item.available");
		itemCmp.setElementCssClass("o_block_bottom");
		itemCmp.setIconCssClass("o_icon o_icon-fw " + courseNodeFactory.getCourseNodeConfiguration(courseNode.getType()).getIconCSSClass());
		contentVC.put(itemCmp.getComponentName(), itemCmp);
		
		contentCmp = new IconPanelLabelTextContent("content");
		itemCmp.setContent(contentCmp);
		
		editLink = LinkFactory.createButton("edit.page", contentVC, this);
		editLink.setElementCssClass("o_sel_filechooser_edit");
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editLink.setGhost(true);
		itemCmp.addLink(editLink);
		
		previewLink = LinkFactory.createButton("preview", contentVC, this);
		previewLink.setElementCssClass("o_sel_filechooser_preview");
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
		nodeRightCtrl = new NodeRightsController(ureq, getWindowControl(), courseGroupManager,
				PageCourseNode.NODE_RIGHT_TYPES, courseNode.getModuleConfiguration(), null);
		listenTo(nodeRightCtrl);
		contentVC.put("rights", nodeRightCtrl.getInitialComponent());
		
		updateUI();
	}
	
	protected void updateTitle() {
		String title = courseNode.getLongTitle();
		if(!StringHelper.containsNonWhitespace(title)) {
			title = courseNode.getShortTitle();
		}
		
		if(StringHelper.containsNonWhitespace(title) && !Objects.equals(title, page.getTitle())) {
			page.setTitle(title);
			page = pageService.updatePage(page);
			updateUI();
		}
	}
	
	private void updateUI() {
		List<LabelText> labelTexts = new ArrayList<>(2);
		if(page != null) {
			itemCmp.setTitle(page.getTitle());
			String lastModified = Formatter.getInstance(getLocale()).formatDateAndTime(page.getLastModified());
			labelTexts.add(new LabelText(translate("cep.last.modified"), lastModified));
		}
		contentCmp.setLabelTexts(labelTexts);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(nodeRightCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == previewLayoutCtr && event == Event.BACK_EVENT) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(previewLayoutCtr);
		removeAsListenerAndDispose(pageCtrl);
		previewLayoutCtr = null;
		pageCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(previewLink == source) {
			doShowPreview(ureq);
		} else if(editLink == source) {
			doEdit(ureq);
		}
	}
	
	private void doShowPreview(UserRequest ureq) {
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getReadOnlyCallback();
		PageSettings pageSettings = PageSettings.noHeader(null);

		page = pageService.getFullPageByKey(page.getKey());
		pageCtrl = new PageRunController(ureq, getWindowControl(), null,
				 secCallback, page, pageSettings, false);
		listenTo(pageCtrl);
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), pageCtrl, userCourseEnv, courseNode, "o_page_icon");
		pageCtrl.initTools();
		
		previewLayoutCtr = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, ctrl.getInitialComponent(), null);
		previewLayoutCtr.addDisposableChildController(pageCtrl);
		previewLayoutCtr.activate();
		listenTo(previewLayoutCtr);
	}
	
	private void doEdit(UserRequest ureq) {
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForMyPageList();
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		CustomLinkTreeModel linkTreeModel = new CourseInternalLinkTreeModel(courseEnv.getRunStructure().getRootNode());
		CustomLinkTreeModel toolLinkTreeModel = new CourseToolLinkTreeModel(courseEnv.getCourseConfig(), courseEnv.getCourseGroupManager().getCourseEntry(), getLocale());
		PageSettings pageSettings = PageSettings.reduced(null, linkTreeModel, toolLinkTreeModel, true, false);

		page = pageService.getFullPageByKey(page.getKey());
		pageCtrl = new PageRunController(ureq, getWindowControl(), null,
				 secCallback, page, pageSettings, true);
		listenTo(pageCtrl);
		pageCtrl.initTools();
		
		previewLayoutCtr = new LayoutMain3ColsBackController(ureq, getWindowControl(), null, pageCtrl.getInitialComponent(), null);
		previewLayoutCtr.addDisposableChildController(pageCtrl);
		previewLayoutCtr.activate();
		listenTo(previewLayoutCtr);
	}
}
