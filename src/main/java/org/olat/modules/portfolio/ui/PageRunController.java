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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageProvider;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.ceditor.ui.FullEditorSecurityCallback;
import org.olat.modules.ceditor.ui.PageController;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.ceditor.ui.event.ImportEvent;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.handler.ContainerHandler;
import org.olat.modules.portfolio.handler.CreateFileHandler;
import org.olat.modules.portfolio.handler.EvaluationFormHandler;
import org.olat.modules.portfolio.handler.HTMLRawPageElementHandler;
import org.olat.modules.portfolio.handler.MathPageElementHandler;
import org.olat.modules.portfolio.handler.ParagraphPageElementHandler;
import org.olat.modules.portfolio.handler.SpacerElementHandler;
import org.olat.modules.portfolio.handler.TablePageElementHandler;
import org.olat.modules.portfolio.handler.TitlePageElementHandler;
import org.olat.modules.portfolio.model.ExtendedMediaRenderingHints;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.model.StandardMediaRenderingHints;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.DonePageEvent;
import org.olat.modules.portfolio.ui.event.EditPageMetadataEvent;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.olat.modules.portfolio.ui.event.PageChangedEvent;
import org.olat.modules.portfolio.ui.event.PageDeletedEvent;
import org.olat.modules.portfolio.ui.event.PageRemovedEvent;
import org.olat.modules.portfolio.ui.event.PageSelectionEvent;
import org.olat.modules.portfolio.ui.event.PublishEvent;
import org.olat.modules.portfolio.ui.event.ReopenPageEvent;
import org.olat.modules.portfolio.ui.event.RevisionEvent;
import org.olat.modules.portfolio.ui.event.SelectPageEvent;
import org.olat.modules.portfolio.ui.event.ToggleEditPageEvent;
import org.olat.modules.portfolio.ui.export.ExportBinderAsPDFResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageRunController extends BasicController implements TooledController, GenericEventListener, Activateable2  {

	private VelocityContainer mainVC;
	private Link previousPageLink;
	private Link nextPageLink;
	private Link allPagesLink;
	private Link editLink;
	private Link editMetadataLink, moveToTrashLink, restoreLink, deleteLink,
		printLink, exportPageAsPdfLink;
	protected final TooledStackedPanel stackPanel;
	
	private CloseableModalController cmc;
	private PageMetadataController pageMetaCtrl;
	private PageController pageCtrl;
	private PageEditorV2Controller pageEditCtrl;
	private RestorePageController restorePageCtrl;
	private ConfirmClosePageController confirmDonePageCtrl;
	private DialogBoxController confirmPublishCtrl, confirmRevisionCtrl, confirmCloseCtrl,
		confirmReopenCtrl, confirmMoveToTrashCtrl, confirmDeleteCtrl;
	private PageMetadataEditController editMetadataCtrl;
	private UserCommentsAndRatingsController commentsCtrl;
	private SelectPageListController selectPageController;
	
	private Page page;
	private LockResult lockEntry;
	private OLATResourceable lockOres;
	private List<Assignment> assignments;
	private final UserSession userSession;
	private boolean dirtyMarker = false;
	private final boolean openInEditMode;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;
	
	public PageRunController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Page page, boolean openEditMode) {
		super(ureq, wControl);
		this.page = page;
		this.stackPanel = stackPanel;
		
		this.secCallback = secCallback;
		lockOres = OresHelper.createOLATResourceableInstance("Page", page.getKey());
		userSession = ureq.getUserSession();
		
		if(openEditMode && page.isEditable()) {
			lockEntry = coordinator.getCoordinator().getLocker().acquireLock(lockOres, getIdentity(), "", getWindow());
		}
		this.openInEditMode = openEditMode && page.isEditable() && (lockEntry != null && lockEntry.isSuccess());
		coordinator.getCoordinator().getEventBus().registerFor(this, getIdentity(), lockOres);
		
		assignments = portfolioService.getSectionsAssignments(page, null);
		
		mainVC = createVelocityContainer("page_content");
		mainVC.contextPut("pageTitle", page.getTitle());
		loadMeta(ureq);
		
		pageCtrl = new PageController(ureq, getWindowControl(), new PortfolioPageProvider(), new StandardMediaRenderingHints());
		listenTo(pageCtrl);
		mainVC.put("page", pageCtrl.getInitialComponent());
		loadModel(ureq, false);
		stackPanel.addListener(this);
		
		previousPageLink = LinkFactory.createButton("page.paging.previous", mainVC, this);
		previousPageLink.setVisible(false);
		previousPageLink.setIconLeftCSS("o_icon o_icon_move_left");
		nextPageLink = LinkFactory.createButton("page.paging.next", mainVC, this);
		nextPageLink.setVisible(false);
		nextPageLink.setIconRightCSS("o_icon o_icon_move_right");
		allPagesLink = LinkFactory.createButton("page.paging.all", mainVC, this);
		allPagesLink.setVisible(false);

		putInitialPanel(mainVC);
		
		if(openInEditMode) {
			pageEditCtrl = new PageEditorV2Controller(ureq, getWindowControl(),
					new PortfolioPageEditorProvider(ureq.getUserSession().getRoles()), new FullEditorSecurityCallback(),
					getTranslator());
			listenTo(pageEditCtrl);
			if (page != null && page.getBody() != null && page.getBody().getUsage() > 1) {
				showWarning("page.is.referenced");
				mainVC.contextPut("pageIsReferenced", true);
			}
			mainVC.contextPut("isPersonalBinder", (!secCallback.canNewAssignment() && secCallback.canEditMetadataBinder()));
			mainVC.put("page", pageEditCtrl.getInitialComponent());
			// Remove comments controller in edit mode, save button confuses user
			if(commentsCtrl != null && commentsCtrl.getCommentsCount() == 0) {
				mainVC.remove(commentsCtrl.getInitialComponent());
			}
		}
	}
	
	public void initPaging(boolean hasPrevious, boolean hasNext) {
		previousPageLink.setVisible(true);
		previousPageLink.setEnabled(hasPrevious);
		nextPageLink.setVisible(true);
		nextPageLink.setEnabled(hasNext);
		allPagesLink.setVisible(true);
	}

	@Override
	public void initTools() {
		editLink(!openInEditMode);
		stackPanel.addTool(editLink, Align.left);

		editMetadataLink = LinkFactory.createToolLink("edit.page.metadata", translate("edit.page.metadata"), this);
		editMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit_metadata");
		editMetadataLink.setVisible(secCallback.canEditPageMetadata(page, assignments) && page == null);
		stackPanel.addTool(editMetadataLink, Align.left);
		
		if(secCallback.canExportBinder()) {
			Dropdown exportTools = new Dropdown("export.page", "export.page", false, getTranslator());
			exportTools.setElementCssClass("o_sel_pf_export_tools");
			exportTools.setIconCSS("o_icon o_icon_download");
			stackPanel.addTool(exportTools, Align.left);
			
			if(pdfModule.isEnabled()) {
				exportPageAsPdfLink = LinkFactory.createToolLink("export.page.pdf", translate("export.page.pdf"), this);
				exportPageAsPdfLink.setIconLeftCSS("o_icon o_filetype_pdf");
				exportTools.addComponent(exportPageAsPdfLink);
			}
			
			printLink = LinkFactory.createToolLink("export.page.onepage", translate("export.page.onepage"), this);
			printLink.setIconLeftCSS("o_icon o_icon_print");
			printLink.setPopup(new LinkPopupSettings(950, 750, "binder"));
			exportTools.addComponent(printLink);
		}
		
		moveToTrashLink = LinkFactory.createToolLink("delete.page", translate("delete.page"), this);
		moveToTrashLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
		moveToTrashLink.setElementCssClass("o_sel_pf_move_page_to_trash");
		moveToTrashLink.setVisible(secCallback.canDeletePage(page));
		stackPanel.addTool(moveToTrashLink, Align.right);
		
		if(secCallback.canRestorePage(page)) {
			restoreLink = LinkFactory.createToolLink("restore.page", translate("restore.page"), this);
			restoreLink.setIconLeftCSS("o_icon o_icon-lg o_icon_restore");
			stackPanel.addTool(restoreLink, Align.left);
			
			deleteLink = LinkFactory.createToolLink("delete.def.page", translate("delete.def.page"), this);
			deleteLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
			deleteLink.setElementCssClass("o_sel_pf_delete_page");
			stackPanel.addTool(deleteLink, Align.left);
		}
	}
	
	/**
	 * @param edit The wanted state of the links
	 * @return The edit link
	 */
	private Link editLink(boolean edit) {
		if(page.isEditable()) {
			if(editLink == null) {
				editLink = LinkFactory.createToolLink("edit.page", translate("edit.page"), this);
				editLink.setElementCssClass("o_sel_pf_edit_page");
			}
			if(edit) {
				editLink.setCustomDisplayText(translate("edit.page"));
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_off");
			} else {
				editLink.setCustomDisplayText(translate("edit.page.close"));
				editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_toggle_on");
			}
			editLink.setVisible(secCallback.canEditPage(page) && page == null);
			editLink.setUserObject(edit);
			
			if(pageMetaCtrl != null) {
				pageMetaCtrl.editLink(edit);
			}
		}
		return editLink;
	}
	
	private void loadModel(UserRequest ureq, boolean reloadComments) {
		mainVC.contextPut("pageTitle", page.getTitle());
		pageCtrl.loadElements(ureq);
		dirtyMarker = false;
		
		if(secCallback.canComment(page)) {
			if(reloadComments && commentsCtrl != null) {
				mainVC.remove(commentsCtrl.getInitialComponent());
				removeAsListenerAndDispose(commentsCtrl);
				commentsCtrl = null;
			}
			if(commentsCtrl == null) {
				CommentAndRatingSecurityCallback commentSecCallback;
				if(PageStatus.isClosed(page)) {
					commentSecCallback = new ReadOnlyCommentsSecurityCallback();
				} else {
					commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
				}
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
				commentsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), ores, null, commentSecCallback, null, true, false, true);
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
			editLink.setVisible(secCallback.canEditPage(page) && page == null);
		}
		if(editMetadataLink != null) {
			editMetadataLink.setVisible(secCallback.canEditMetadataBinder() && page == null);
		}
		if(moveToTrashLink != null) {
			moveToTrashLink.setVisible(secCallback.canDeletePage(page));
		}
	}
	
	private void loadMeta(UserRequest ureq) {
		removeAsListenerAndDispose(pageMetaCtrl);
		
		mainVC.contextPut("pageTitle", page.getTitle());
		pageMetaCtrl = new PageMetadataController(ureq, getWindowControl(), secCallback, page, openInEditMode);
		listenTo(pageMetaCtrl);
		mainVC.put("meta", pageMetaCtrl.getInitialComponent());
	}
	
	public Page getPage() {
		return page;
	}
	
	public Section getSection() {
		return page.getSection();
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		if (lockEntry != null && lockEntry.isSuccess()) {
			// release lock
			coordinator.getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
		coordinator.getCoordinator().getEventBus().deregisterFor(this, lockOres);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof PageChangedEvent) {
			PageChangedEvent pce = (PageChangedEvent)event;
			if(!pce.isMe(getIdentity()) && page.getKey().equals(pce.getPageKey())) {
				dirtyMarker = false;
				pageCtrl.loadElements(new SyntheticUserRequest(getIdentity(), getLocale(), userSession));
			}
		}
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
				coordinator.getCoordinator().getEventBus()
					.fireEventToListenersOf(new PageChangedEvent(getIdentity().getKey(), page.getKey()), lockOres);
			} else if(event instanceof PublishEvent) {
				doConfirmPublish(ureq);
			} else if(event instanceof ImportEvent) {
				openImportPageSelection(ureq);
			}
		} else if(editMetadataCtrl == source || restorePageCtrl == source) {
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
			} else if(event instanceof DonePageEvent) {
				doConfirmDone(ureq);
			} else if(event instanceof ReopenPageEvent) {
				doConfirmReopen(ureq);
			} else if(event == Event.CHANGED_EVENT) {
				// categories modified, just propagate
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof ToggleEditPageEvent) {
				doEditPage(ureq);
			} else if(event instanceof EditPageMetadataEvent) {
				doEditMetadata(ureq);
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
				fireEvent(ureq, new ClosePageEvent());
			}
		} else if(confirmReopenCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doReopen(ureq);
			}
		} else if(confirmMoveToTrashCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doMoveToTrash(ureq);
			}
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq);
			}
		} else if(confirmDonePageCtrl == source) {
			if(event instanceof ClosePageEvent) {
				doClose(ureq);
			} else {
				doDone(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(selectPageController == source) {
			importSelectedContents(ureq, event);
		}  else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDonePageCtrl);
		removeAsListenerAndDispose(editMetadataCtrl);
		removeAsListenerAndDispose(restorePageCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDonePageCtrl = null;
		editMetadataCtrl = null;
		restorePageCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEditPage(ureq);
		} else if(editMetadataLink == source) {
			doEditMetadata(ureq);
		} else if(moveToTrashLink == source) {
			doConfirmMoveToTrash(ureq);
		} else if(restoreLink == source) {
			doRestorePage(ureq);
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(printLink == source) {
			doPrint(ureq);
		} else if(exportPageAsPdfLink == source) {
			doExportBinderAsPdf(ureq);
		} else if(previousPageLink == source) {
			fireEvent(ureq, new SelectPageEvent(SelectPageEvent.PREVIOUS_PAGE));
		} else if(nextPageLink == source) {
			fireEvent(ureq, new SelectPageEvent(SelectPageEvent.NEXT_PAGE));
		} else if(allPagesLink == source) {
			fireEvent(ureq, new SelectPageEvent(SelectPageEvent.ALL_PAGES));
		}
	}
	
	private void doConfirmMoveToTrash(UserRequest ureq) {
		String title = translate("delete.page.confirm.title");
		String text = translate("delete.page.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmMoveToTrashCtrl = activateYesNoDialog(ureq, title, text, confirmMoveToTrashCtrl);
	}
	
	private void doMoveToTrash(UserRequest ureq) {
		Page floatingPage = portfolioService.removePage(page);
		fireEvent(ureq, new PageRemovedEvent(floatingPage));
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		String title = translate("delete.def.page.confirm.title");
		String text = translate("delete.def.page.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
	}
	
	private void doDelete(UserRequest ureq) {
		portfolioService.deletePage(page);
		fireEvent(ureq, new PageDeletedEvent());
	}
	
	private void doConfirmPublish(UserRequest ureq) {
		List<ValidationMessage> messages = new ArrayList<>();
		pageCtrl.validateElements(ureq, messages);
		
		String title = translate("publish.confirm.title");
		String text = translate("publish.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		
		if(!messages.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("<p>").append(text).append("</p>");
			for(ValidationMessage message:messages) {
				sb.append("<p class='o_warning'>").append(message.getMessage()).append("</p>");
			}
			text = sb.toString();
		}

		confirmPublishCtrl = activateYesNoDialog(ureq, title, text, confirmPublishCtrl);
	}
	
	private void doPublish(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.published, getIdentity(), secCallback.getRole());
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq, false);
		doRunPage(ureq);
		mainVC.contextPut("isPersonalBinder", false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmRevision(UserRequest ureq) {
		String title = translate("revision.confirm.title");
		String text = translate("revision.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmRevisionCtrl = activateYesNoDialog(ureq, title, text, confirmRevisionCtrl);
	}
	
	private void doRevision(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.inRevision, getIdentity(), secCallback.getRole());
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq, false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmClose(UserRequest ureq) {
		String title = translate("close.confirm.title");
		String text = translate("close.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmCloseCtrl = activateYesNoDialog(ureq, title, text, confirmCloseCtrl);
	}
	
	private void doClose(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.closed, getIdentity(), secCallback.getRole());
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq, true);
		fireEvent(ureq, new ClosePageEvent());
	}
	
	private void doDone(UserRequest ureq) {
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq, true);
		fireEvent(ureq, new DonePageEvent());
	}
	
	private void doConfirmDone(UserRequest ureq) {
		if(secCallback.canClose(page)) {
			confirmDonePageCtrl = new ConfirmClosePageController(ureq, getWindowControl(), page);
			listenTo(confirmDonePageCtrl);
			
			String title = translate("close.page");
			cmc = new CloseableModalController(getWindowControl(), null, confirmDonePageCtrl.getInitialComponent(), true, title, false);
			listenTo(cmc);
			cmc.activate();
		} else {
			doDone(ureq);
		}
	}
	
	private void doConfirmReopen(UserRequest ureq) {
		String title = translate("reopen.confirm.title");
		String text = translate("reopen.confirm.descr", new String[]{ StringHelper.escapeHtml(page.getTitle()) });
		confirmReopenCtrl = activateYesNoDialog(ureq, title, text, confirmReopenCtrl);
	}
	
	private void doReopen(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.published, getIdentity(), secCallback.getRole());
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		loadModel(ureq, true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doEditMetadata(UserRequest ureq) {
		if(guardModalController(editMetadataCtrl)) return;
		
		removeAsListenerAndDispose(editMetadataCtrl);
		
		Binder binder = null;
		Section section = null;
		if(page.getSection() != null) {
			section = page.getSection();
			binder = portfolioService.getBinderBySection(section);
		}
		
		boolean editMetadata = secCallback.canEditPageMetadata(page, assignments);
		editMetadataCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback,
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
			if(lockEntry != null && lockEntry.isSuccess()) {
				coordinator.getCoordinator().getLocker().releaseLock(lockEntry);
			}
			
			doRunPage(ureq);
			editLink(true);
			// Add comments controller again in run mode, maybe removed by
			// previous edit mode entering
			if(commentsCtrl != null) {
				mainVC.put("comments", commentsCtrl.getInitialComponent());
			}
		} else {
			lockEntry = coordinator.getCoordinator().getLocker().acquireLock(lockOres, getIdentity(), "", getWindow());
			if(lockEntry.isSuccess()) {
				pageEditCtrl = new PageEditorV2Controller(ureq, getWindowControl(),
						new PortfolioPageEditorProvider(ureq.getUserSession().getRoles()),
						new FullEditorSecurityCallback(), getTranslator());
				if (page != null && page.getBody() != null && page.getBody().getUsage() > 1) {
					showWarning("page.is.referenced");
				}
				listenTo(pageEditCtrl);
				mainVC.contextPut("isPersonalBinder", (!secCallback.canNewAssignment() && secCallback.canEditMetadataBinder()));
				mainVC.put("page", pageEditCtrl.getInitialComponent());
				editLink(false);
				// Remove comments controller in edit mode, save button confuses user
				if(commentsCtrl != null && commentsCtrl.getCommentsCount() == 0) {
					mainVC.remove(commentsCtrl.getInitialComponent());
				}
			} else {
				String i18nMsg = lockEntry.isDifferentWindows() ? "warning.page.locked.same.user" : "warning.page.locked";
				String[] i18nParams = new String[] { StringHelper.escapeHtml(userManager.getUserDisplayName(lockEntry.getOwner())), Formatter.getInstance(getLocale()).formatTime(new Date(lockEntry.getLockAquiredTime())) };
				showWarning(i18nMsg, i18nParams);
			}
		}
	}
	
	private void doRunPage(UserRequest ureq) {
		if(dirtyMarker) {
			loadModel(ureq, false);
		}
		mainVC.put("page", pageCtrl.getInitialComponent());
		editLink(true);
	}
	
	private void doRestorePage(UserRequest ureq) {
		restorePageCtrl = new RestorePageController(ureq, getWindowControl(), page);
		listenTo(restorePageCtrl);
		
		String title = translate("restore.page");
		cmc = new CloseableModalController(getWindowControl(), null, restorePageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doExportBinderAsPdf(UserRequest ureq) {
		MediaResource resource = new ExportBinderAsPDFResource(page, ureq, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doPrint(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			BinderOnePageController printCtrl = new BinderOnePageController(lureq, lwControl, page, ExtendedMediaRenderingHints.toPrint(), true);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, printCtrl);
			layoutCtr.addDisposableChildController(printCtrl); // dispose controller on layout dispose
			return layoutCtr;
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(ctrlCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr, true);
	}
	
	private void openImportPageSelection(UserRequest ureq) {
		selectPageController = new SelectPageListController(ureq, getWindowControl(), BinderSecurityCallbackFactory.getCallbackForImportPages(), page != null ? Collections.singletonList(page) : null);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectPageController.getInitialComponent(), true, translate("import.content"), true);
		listenTo(selectPageController);
		listenTo(cmc);
		
		cmc.activate();		
	}
	
	private void importSelectedContents(UserRequest ureq, Event event) {
		if (selectPageController == null || event == null || !(event instanceof PageSelectionEvent)) {
			return;
		}
		
		Page selectedPage = ((PageSelectionEvent) event).getPage();
		page = portfolioService.linkPageBody(page, selectedPage);
		dirtyMarker = true;
		
		cmc.deactivate();
		cleanUp();
		
		pageEditCtrl.loadModel(ureq);
		mainVC.contextPut("pageIsReferenced", page != null && page.getBody() != null && page.getBody().getUsage() > 1);
		mainVC.setDirty(true);
	}

	private class PortfolioPageProvider implements PageProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		
		public PortfolioPageProvider() {
			//handler for title
			TitlePageElementHandler titleRawHandler = new TitlePageElementHandler();
			handlers.add(titleRawHandler);
			//handler simple HTML
			ParagraphPageElementHandler paragraphHandler = new ParagraphPageElementHandler();
			handlers.add(paragraphHandler);
			//handler for spacer code
			SpacerElementHandler hrHandler = new SpacerElementHandler();
			handlers.add(hrHandler);
			//handler for container
			ContainerHandler containerHandler = new ContainerHandler();
			handlers.add(containerHandler);
			//handler for form
			EvaluationFormHandler formHandler = new EvaluationFormHandler();
			handlers.add(formHandler);
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			//handler for table
			TablePageElementHandler tableHandler = new TablePageElementHandler();
			handlers.add(tableHandler);
			//handler for table
			MathPageElementHandler mathHandler = new MathPageElementHandler();
			handlers.add(mathHandler);
			
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
		
		public PortfolioPageEditorProvider(Roles roles) {
			//handler for title
			TitlePageElementHandler titleRawHandler = new TitlePageElementHandler();
			handlers.add(titleRawHandler);
			creationHandlers.add(titleRawHandler);
			//handler simple HTML
			ParagraphPageElementHandler paragraphHandler = new ParagraphPageElementHandler();
			handlers.add(paragraphHandler);
			creationHandlers.add(paragraphHandler);
			// handler for table
			TablePageElementHandler tableHandler = new TablePageElementHandler();
			handlers.add(tableHandler);
			creationHandlers.add(tableHandler);
			//handler for LaTeX code
			MathPageElementHandler mathHandler = new MathPageElementHandler();
			handlers.add(mathHandler);
			creationHandlers.add(mathHandler);

			// Handler only to create files
			if (isCreateFilePossible(roles)) {
				CreateFileHandler createFileHandler = new CreateFileHandler();
				creationHandlers.add(createFileHandler);
			}
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
			//handler for form
			EvaluationFormHandler formHandler = new EvaluationFormHandler();
			handlers.add(formHandler);
			
			//add the hook to pick media from the media center
			creationHandlers.add(new OtherArtefactsHandler());
			
			//handler for container
			ContainerHandler containerHandler = new ContainerHandler();
			handlers.add(containerHandler);
			creationHandlers.add(containerHandler);
			//handler for HR code
			SpacerElementHandler hrHandler = new SpacerElementHandler();
			handlers.add(hrHandler);
			creationHandlers.add(hrHandler);
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			creationHandlers.add(htlmRawHandler);// at the end, legacy	
		}

		private boolean isCreateFilePossible(Roles roles) {
			List<DocTemplate> editableTemplates = CreateFileHandler.getEditableTemplates(getIdentity(), roles, getLocale()).getTemplates();
			for (DocTemplate docTemplate: editableTemplates) {
				if (docEditorService.hasEditor(getIdentity(), roles,  docTemplate.getSuffix(), Mode.EDIT, true, false)) {
					return true;
				}
			}
			return false;
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
		public int indexOf(PageElement element) {
			List<PagePart> elements = portfolioService.getPageParts(page);
			return elements.indexOf(element);
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
		public boolean isRemoveConfirmation(PageElement element) {
			return false;
		}

		@Override
		public String getRemoveConfirmationI18nKey() {
			return null;
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

		@Override
		public void movePageElement(PageElement elementToMove, PageElement sibling, boolean after) {
			if(elementToMove instanceof PagePart && (sibling == null || sibling instanceof PagePart)) {
				portfolioService.movePagePart(page, (PagePart)elementToMove, (PagePart)sibling, after);
			}
		}
		
		@Override
		public String getImportButtonKey() {
			return page.getBody().getUsage() <= 1 ? "import.content" : null;
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
		public PageElementCategory getCategory() {
			return PageElementCategory.embed;
		}

		@Override
		public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
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
	}
}
