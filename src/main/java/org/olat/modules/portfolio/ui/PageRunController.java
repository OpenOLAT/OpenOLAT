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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.editor.AddElementInfos;
import org.olat.modules.portfolio.ui.editor.InteractiveAddPageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageController;
import org.olat.modules.portfolio.ui.editor.PageEditorController;
import org.olat.modules.portfolio.ui.editor.PageEditorProvider;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementAddController;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageProvider;
import org.olat.modules.portfolio.ui.editor.SimpleAddPageElementHandler;
import org.olat.modules.portfolio.ui.editor.handler.HTMLRawPageElementHandler;
import org.olat.modules.portfolio.ui.editor.handler.SpacerElementHandler;
import org.olat.modules.portfolio.ui.editor.handler.TitlePageElementHandler;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.olat.modules.portfolio.ui.event.PageRemoved;
import org.olat.modules.portfolio.ui.event.PublishEvent;
import org.olat.modules.portfolio.ui.event.ReopenPageEvent;
import org.olat.modules.portfolio.ui.event.RevisionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageRunController extends BasicController implements TooledController, Activateable2  {

	private VelocityContainer mainVC;
	private Link editLink, editMetadataLink, deleteLink;
	protected final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private PageMetadataController pageMetaCtrl;
	private PageController pageCtrl;
	private PageEditorController pageEditCtrl;
	private DialogBoxController confirmPublishCtrl, confirmRevisionCtrl, confirmCloseCtrl,
		confirmReopenCtrl, confirmDeleteCtrl;
	private PageMetadataEditController editMetadataCtrl;
	private UserCommentsAndRatingsController commentsCtrl;
	
	private Page page;
	private List<Assignment> assignments;
	private boolean dirtyMarker = false;
	private final boolean openInEditMode;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageRunController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Page page, boolean openInEditMode) {
		super(ureq, wControl);
		this.page = page;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.openInEditMode = openInEditMode;
		
		assignments = portfolioService.getAssignments(page);
		
		mainVC = createVelocityContainer("page_content");
		mainVC.contextPut("pageTitle", page.getTitle());
		loadMeta(ureq);
		
		pageCtrl = new PageController(ureq, getWindowControl(), new PortfolioPageProvider());
		listenTo(pageCtrl);
		mainVC.put("page", pageCtrl.getInitialComponent());
		loadModel(ureq);
		stackPanel.addListener(this);

		putInitialPanel(mainVC);
		
		if(openInEditMode) {
			pageEditCtrl = new PageEditorController(ureq, getWindowControl(), new PortfolioPageEditorProvider());
			listenTo(pageEditCtrl);
			mainVC.put("page", pageEditCtrl.getInitialComponent());
		}
	}

	@Override
	public void initTools() {
		editLink(!openInEditMode);
		stackPanel.addTool(editLink, Align.left);

		editMetadataLink = LinkFactory.createToolLink("edit.page.metadata", translate("edit.page.metadata"), this);
		editMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit_metadata");
		editMetadataLink.setVisible(secCallback.canEditMetadataBinder());
		stackPanel.addTool(editMetadataLink, Align.left);
		
		deleteLink = LinkFactory.createToolLink("delete.page", translate("delete.page"), this);
		deleteLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
		deleteLink.setVisible(secCallback.canDeletePage(page));
		stackPanel.addTool(deleteLink, Align.right);
	}
	
	private Link editLink(boolean edit) {
		if(editLink == null) {
			editLink = LinkFactory.createToolLink("edit.page", translate("edit.page"), this);
		}
		if(edit) {
			editLink.setCustomDisplayText(translate("edit.page"));
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
		} else {
			editLink.setCustomDisplayText(translate("edit.page.close"));
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
		}
		editLink.setVisible(secCallback.canEditPage(page));
		editLink.setUserObject(edit);
		return editLink;
	}
	
	private void loadModel(UserRequest ureq) {
		mainVC.contextPut("pageTitle", page.getTitle());
		pageCtrl.loadElements(ureq);
		dirtyMarker = false;
		
		if(secCallback.canComment(page)) {
			if(commentsCtrl == null) {
				CommentAndRatingSecurityCallback commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
				commentsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), ores, null, commentSecCallback, true, false, true);
				commentsCtrl.expandComments(ureq);
				listenTo(commentsCtrl);
			}
			mainVC.put("comments", commentsCtrl.getInitialComponent());
		} else if(commentsCtrl != null) {
			mainVC.remove(commentsCtrl.getInitialComponent());
			removeAsListenerAndDispose(commentsCtrl);
			commentsCtrl = null;
		}
		
		if(editLink != null) {
			editLink.setVisible(secCallback.canEditPage(page));
		}
		if(editMetadataLink != null) {
			editMetadataLink.setVisible(secCallback.canEditMetadataBinder());
		}
		if(deleteLink != null) {
			deleteLink.setVisible(secCallback.canDeletePage(page));
		}
	}
	
	private void loadMeta(UserRequest ureq) {
		removeAsListenerAndDispose(pageMetaCtrl);
		
		mainVC.contextPut("pageTitle", page.getTitle());
		pageMetaCtrl = new PageMetadataController(ureq, getWindowControl(), secCallback, page);
		listenTo(pageMetaCtrl);
		mainVC.put("meta", pageMetaCtrl.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Comment".equalsIgnoreCase(resName)) {
			Long commentId = entries.get(0).getOLATResourceable().getResourceableId();
			commentsCtrl.expandCommentsAt(ureq, commentId);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(pageEditCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				dirtyMarker = true;
			} else if(event instanceof PublishEvent) {
				doConfirmPublish(ureq);
			}
		} else if(editMetadataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadMeta(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(pageMetaCtrl == source) {
			if(event instanceof PublishEvent) {
				doConfirmPublish(ureq);
			} else if(event instanceof RevisionEvent) {
				doConfirmRevision(ureq);
			} else if(event instanceof ClosePageEvent) {
				doConfirmClose(ureq);
			} else if(event instanceof ReopenPageEvent) {
				doConfirmReopen(ureq);
			}	
		} else if(commentsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				commentsCtrl.collapseComments();
			}
		} else if(confirmPublishCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doPublish(ureq);
			}
		} else if(confirmRevisionCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doRevision(ureq);
			}
		} else if(confirmCloseCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doClose(ureq);
			}
		} else if(confirmReopenCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doReopen(ureq);
			}
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editMetadataCtrl);
		removeAsListenerAndDispose(cmc);
		editMetadataCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEditPage(ureq);
		} else if(editMetadataLink == source) {
			doEditMetadata(ureq);
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		}
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		String title = translate("delete.page.confirm.title");
		String text = translate("delete.page.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
	}
	
	private void doDelete(UserRequest ureq) {
		Page floatingPage = portfolioService.removePage(page);
		fireEvent(ureq, new PageRemoved(floatingPage));
	}
	
	private void doConfirmPublish(UserRequest ureq) {
		String title = translate("publish.confirm.title");
		String text = translate("publish.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmPublishCtrl = activateYesNoDialog(ureq, title, text, confirmPublishCtrl);
	}
	
	private void doPublish(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.published);
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq);
		doRunPage(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmRevision(UserRequest ureq) {
		String title = translate("revision.confirm.title");
		String text = translate("revision.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmRevisionCtrl = activateYesNoDialog(ureq, title, text, confirmRevisionCtrl);
	}
	
	private void doRevision(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.inRevision);
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmClose(UserRequest ureq) {
		String title = translate("close.confirm.title");
		String text = translate("close.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmCloseCtrl = activateYesNoDialog(ureq, title, text, confirmCloseCtrl);
	}
	
	private void doClose(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.closed);
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmReopen(UserRequest ureq) {
		String title = translate("reopen.confirm.title");
		String text = translate("reopen.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmReopenCtrl = activateYesNoDialog(ureq, title, text, confirmReopenCtrl);
	}
	
	private void doReopen(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.published);
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doEditMetadata(UserRequest ureq) {
		if(editMetadataCtrl != null) return;
		
		removeAsListenerAndDispose(editMetadataCtrl);
		
		Binder binder = null;
		Section section = null;
		if(page.getSection() != null) {
			section = page.getSection();
			binder = portfolioService.getBinderBySection(section);
		}
		
		boolean editMetadata = secCallback.canEditPageMetadata(page, assignments);
		editMetadataCtrl = new PageMetadataEditController(ureq, getWindowControl(),
				binder, editMetadata, section, editMetadata, page, editMetadata);
		listenTo(editMetadataCtrl);
		
		String title = translate("edit.page.metadata");
		cmc = new CloseableModalController(getWindowControl(), null, editMetadataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditPage(UserRequest ureq) {
		removeAsListenerAndDispose(pageEditCtrl);
		if(Boolean.FALSE.equals(editLink.getUserObject())) {
			doRunPage(ureq);
		} else {
			pageEditCtrl = new PageEditorController(ureq, getWindowControl(), new PortfolioPageEditorProvider());
			listenTo(pageEditCtrl);
			mainVC.put("page", pageEditCtrl.getInitialComponent());
			editLink(false);
		}
	}
	
	private void doRunPage(UserRequest ureq) {
		if(dirtyMarker) {
			loadModel(ureq);
		}
		mainVC.put("page", pageCtrl.getInitialComponent());
		editLink(true);
	}

	private class PortfolioPageProvider implements PageProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		
		public PortfolioPageProvider() {
			//handler for title
			TitlePageElementHandler titleRawHandler = new TitlePageElementHandler();
			handlers.add(titleRawHandler);
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			//handler for HTML code
			SpacerElementHandler hrHandler = new SpacerElementHandler();
			handlers.add(hrHandler);
			
			List<MediaHandler> mediaHandlers = portfolioService.getMediaHandlers();
			for(MediaHandler mediaHandler:mediaHandlers) {
				if(mediaHandler instanceof PageElementHandler) {
					handlers.add((PageElementHandler)mediaHandler);
				}
			}
		}

		@Override
		public List<? extends PageElement> getElements() {
			return portfolioService.getPageParts(page);
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}
	}

	private class PortfolioPageEditorProvider implements PageEditorProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		private final List<PageElementHandler> creationHandlers = new ArrayList<>();
		
		public PortfolioPageEditorProvider() {
			//handler for title
			TitlePageElementHandler titleRawHandler = new TitlePageElementHandler();
			handlers.add(titleRawHandler);
			creationHandlers.add(titleRawHandler);
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			creationHandlers.add(htlmRawHandler);
			//handler for HR code
			SpacerElementHandler hrHandler = new SpacerElementHandler();
			handlers.add(hrHandler);
			creationHandlers.add(hrHandler);
			
			
			List<MediaHandler> mediaHandlers = portfolioService.getMediaHandlers();
			for(MediaHandler mediaHandler:mediaHandlers) {
				if(mediaHandler instanceof PageElementHandler) {
					handlers.add((PageElementHandler)mediaHandler);
					if(mediaHandler instanceof InteractiveAddPageElementHandler
							|| mediaHandler instanceof SimpleAddPageElementHandler) {
						creationHandlers.add((PageElementHandler)mediaHandler);
					}
				}
			}
			
			//add the hook to pick media from the media center
			creationHandlers.add(new OtherArtefactsHandler());
		}

		@Override
		public List<? extends PageElement> getElements() {
			return portfolioService.getPageParts(page);
		}

		@Override
		public List<PageElementHandler> getCreateHandlers() {
			return creationHandlers;
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}

		@Override
		public PageElement appendPageElement(PageElement element) {
			PagePart part = null;
			if(element instanceof PagePart) {
				part = portfolioService.appendNewPagePart(page, (PagePart)element);
			}
			return part;
		}

		@Override
		public PageElement appendPageElementAt(PageElement element, int index) {
			PagePart part = null;
			if(element instanceof PagePart) {
				part = portfolioService.appendNewPagePartAt(page, (PagePart)element, index);
			}
			return part;
		}

		@Override
		public void removePageElement(PageElement element) {
			if(element instanceof PagePart) {
				portfolioService.removePagePart(page, (PagePart)element);
			}
		}

		@Override
		public void moveUpPageElement(PageElement element) {
			if(element instanceof PagePart) {
				portfolioService.moveUpPagePart(page, (PagePart)element);
			}
		}

		@Override
		public void moveDownPageElement(PageElement element) {
			if(element instanceof PagePart) {
				portfolioService.moveDownPagePart(page, (PagePart)element);
			}
		}
	}
	
	public static class OtherArtefactsHandler implements PageElementHandler, InteractiveAddPageElementHandler {

		@Override
		public String getType() {
			return "others";
		}

		@Override
		public String getIconCssClass() {
			return "o_icon_mediacenter";
		}

		@Override
		public Component getContent(UserRequest ureq, WindowControl wControl, PageElement element) {
			return null;
		}

		@Override
		public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
			return null;
		}
		
		@Override
		public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
			return new OtherArtfectsChooserController(ureq, wControl);
		}
	}
	
	public static class OtherArtfectsChooserController extends BasicController implements PageElementAddController {
		
		private MediaPart mediaPart;
		private AddElementInfos userObject;
		private final MediaCenterController mediaListCtrl;
		
		public OtherArtfectsChooserController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			mediaListCtrl = new MediaCenterController(ureq, getWindowControl());
			listenTo(mediaListCtrl);
			putInitialPanel(mediaListCtrl.getInitialComponent());
		}

		@Override
		public PageElement getPageElement() {
			return mediaPart;
		}

		@Override
		public AddElementInfos getUserObject() {
			return userObject;
		}

		@Override
		public void setUserObject(AddElementInfos userObject) {
			this.userObject = userObject;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(event instanceof MediaSelectionEvent) {
				MediaSelectionEvent mse = (MediaSelectionEvent)event;
				if(mse.getMedia() != null) {
					doAddMedia(mse.getMedia());
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
			super.event(ureq, source, event);
		}
		
		private void doAddMedia(Media media) {
			mediaPart = new MediaPart();
			mediaPart.setMedia(media);
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}
