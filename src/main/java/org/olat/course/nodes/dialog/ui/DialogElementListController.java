/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.nodes.dialog.ui.DialogElementsTableModel.DialogCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The list of files to discuss.
 * <p>
 * Initial date: 3 janv. 2018<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class DialogElementListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = DialogElementsTableModel.class.getCanonicalName();

	private static final String DIALOG_UPLOAD_FILE = "dialog.upload.file";
	private static final String ACTION_TOOLS_LINK = "tools";
	private static final String CMD_SELECT = "select";
	private static final String FORUM = "forum";

	private FlexiTableElement tableEl;
	private DialogElementsTableModel tableModel;

	private final boolean showForum;
	private final DialogCourseNode courseNode;
	private final RepositoryEntry entry;
	private final DialogSecurityCallback secCallback;
	private final UserCourseEnvironment userCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private CloseableModalController cmc;
	private DialogBoxController confirmDeletionCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private DialogFileUploadController dialogFileUploadCtrl;

	private DialogElement element;

	@Autowired
	private UserManager userManager;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private DialogElementsManager dialogElementsManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public DialogElementListController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
									   DialogCourseNode courseNode, DialogSecurityCallback secCallback, boolean showForum) {
		super(ureq, wControl, "element_list");
		setTranslator(Util.createPackageTranslator(DialogCourseNodeRunController.class, getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.showForum = showForum;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.secCallback = secCallback;
		this.entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (showForum) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.filename, FORUM));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.filename));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.publishedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.authoredBy));

		if (showForum) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.threads));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.newThreads));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.messages));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.newMessages));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.lastActivityDate));

		if (!userCourseEnv.isCourseReadOnly() && secCallback != null && secCallback.mayDeleteMessageAsModerator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.toolsLink));
		}

		tableModel = new DialogElementsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setPageSize(25);
		tableEl.setAndLoadPersistedPreferences(ureq, "dialog.elements.v3");
		// for compact card view of dialog files
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("dialog_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(DialogElementListCssDelegate.DELEGATE);

		if (secCallback != null && secCallback.mayOpenNewThread()) {
			FormLink uploadButton = uifactory.addFormLink(DIALOG_UPLOAD_FILE, "uploadFile", null, null, flc, Link.BUTTON);
			uploadButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			tableEl.setEmptyTableSettings("table.empty.message", null, "o_dialog_icon", DIALOG_UPLOAD_FILE, "o_icon_add", false);
		} else {
			tableEl.setEmptyTableSettings("table.empty.message", null, "o_dialog_icon");
		}
	}

	private void doUploadFile(UserRequest ureq) {
		removeAsListenerAndDispose(dialogFileUploadCtrl);

		dialogFileUploadCtrl = new DialogFileUploadController(ureq, getWindowControl(), userCourseEnv, secCallback.canCopyFile());
		listenTo(dialogFileUploadCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), dialogFileUploadCtrl.getInitialComponent(),
				true, translate(DIALOG_UPLOAD_FILE));
		listenTo(cmc);
		cmc.activate();
	}

	private void doFinalizeUploadFile(VFSLeaf file) {
		//everything when well so save the property
		element = dialogElementsManager.createDialogElement(entry, getIdentity(), file.getName(),
				file.getSize(), courseNode.getIdent(), dialogFileUploadCtrl.getAuthoredByElValue());
		VFSContainer dialogContainer = dialogElementsManager.getDialogContainer(element);
		VFSManager.copyContent(file.getParentContainer(), dialogContainer);

		markPublisherNews();
		loadModel();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(dialogFileUploadCtrl);
		removeAsListenerAndDispose(confirmDeletionCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		dialogFileUploadCtrl = null;
		confirmDeletionCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}


	protected void loadModel() {
		List<DialogElement> elements = dialogElementsManager.getDialogElements(entry, courseNode.getIdent());
		List<DialogElementRow> rows = new ArrayList<>(elements.size());
		List<Long> forumKeys = elements.stream().map(el -> el.getForum().getKey()).toList();
		// retrieve Maps with informations of forum messages, so db call is only once
		Map<Long, Long> forumKeyToCountForThreads = forumManager.countThreadsByForums(forumKeys);
		Map<Long, Long> forumKeyToCountForReadThreads = forumManager.countReadThreadsByUserAndForums(getIdentity(), forumKeys);
		Map<Long, Long> forumKeyToCountForMessages = forumManager.countMessagesByForums(forumKeys);
		Map<Long, Long> forumKeyToCountForReadMessages = forumManager.countReadMessagesByUserAndForums(getIdentity(), forumKeys);

		for (DialogElement dialogElement : elements) {
			DialogElementRow row = new DialogElementRow(dialogElement, userPropertyHandlers, getLocale());

			// get number of threads and messages (all and unread)
			Long threadCount = forumKeyToCountForThreads.getOrDefault(dialogElement.getForum().getKey(), 0L);
			Long newThreads = threadCount - forumKeyToCountForReadThreads.getOrDefault(dialogElement.getForum().getKey(), 0L);
			Long messagesCount = forumKeyToCountForMessages.getOrDefault(dialogElement.getForum().getKey(), 0L);
			Long newMessages = messagesCount - forumKeyToCountForReadMessages.getOrDefault(dialogElement.getForum().getKey(), 0L);

			// set number of threads and messages (all and unread)
			row.setNumOfThreads(threadCount);
			row.setNumOfUnreadThreads(newThreads);
			row.setNumOfMessages(messagesCount);
			row.setNumOfUnreadMessages(newMessages);

			rows.add(forgeRow(row, dialogElement));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	/**
	 * forge selectLink for card view
	 * @param row
	 */
	private void forgeSelectLink(DialogElementRow row) {
		FormLink link = uifactory.addFormLink("select_" + row.getDialogElementKey(), CMD_SELECT, "", null, flc, Link.LINK + Link.NONTRANSLATED);
		link.setI18nKey(row.getFilename());
		link.setElementCssClass("o_link_plain");

		String iconCSS = CSSHelper.createFiletypeIconCssClassFor(row.getFilename());
		link.setIconLeftCSS("o_icon " + iconCSS);

		link.setUserObject(row);
		row.setSelectLink(link);
	}

	private DialogElementRow forgeRow(DialogElementRow row, DialogElement element) {
		Formatter formatter = Formatter.getInstance(getLocale());

		// set last activity info
		List<Date> lastActivities = forumManager.getMessagesByForum(element.getForum()).stream().map(MessageLight::getLastModified).toList();
		if (lastActivities.isEmpty()) {
			row.setLastActivityDate(element.getCreationDate());
		} else if (Collections.max(lastActivities).before(element.getLastModified())) {
			row.setLastActivityDate(element.getLastModified());
		} else {
			row.setLastActivityDate(Collections.max(lastActivities));
		}

		// set createdBy/authoredby for card view
		if (StringHelper.containsNonWhitespace(row.getPublishedBy())) {
			row.setPublishedByCardView(translate("dialog.metadata.published.by") + " " + row.getPublishedBy());
		}
		if (StringHelper.containsNonWhitespace(row.getAuthoredBy())) {
			row.setAuthoredByCardView(translate("dialog.metadata.authored.by") + " " + row.getAuthoredBy());
		}

		// set modified for card view
		String modifiedDate = formatter.formatDateAndTime(row.getLastActivityDate());
		String modified = translate("last.activity", modifiedDate);
		row.setModified(modified);

		// Thumbnail
		VFSLeaf vfsItem = dialogElementsManager.getDialogLeaf(element);
		if (vfsItem != null) {
			boolean thumbnailAvailable = vfsRepositoryService.isThumbnailAvailable(vfsItem);
			if (thumbnailAvailable) {
				VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(vfsItem, 650, 1000, false);
				if (thumbnail != null) {
					row.setThumbnailAvailable(true);
					VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
					String thumbnailUrl = registerCacheableMapper(null, null, thumbnailMapper);
					row.setThumbnailUrl(thumbnailUrl);
				}
			}
		}

		forgeSelectLink(row);

		// set individual toolsLink for each row
		FormLink toolsLink = uifactory.addFormLink("tools_" + row.getDialogElementKey(), ACTION_TOOLS_LINK, translate("table.header.action"), null, null, Link.NONTRANSLATED);
		row.setToolsLink(toolsLink);

		return row;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDeletionCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				DialogElement elementToDelete = (DialogElement) confirmDeletionCtrl.getUserObject();
				doDelete(elementToDelete);
				// fire event for going back after deletion
				fireEvent(ureq, Event.BACK_EVENT);
				loadModel();
			}
		} else if (source == dialogFileUploadCtrl) {
			if (event == Event.DONE_EVENT) {
				if (dialogFileUploadCtrl.getActionSelectedKey().equals(DialogFileUploadController.DIALOG_ACTION_UPLOAD)) {
					// upload new file
					VFSLeaf newFile = dialogElementsManager.doUpload(
							dialogFileUploadCtrl.getFileUploadEl().getUploadFile(),
							dialogFileUploadCtrl.getFileNameElValue());

					if (newFile != null) {
						doFinalizeUploadFile(newFile);
						fireEvent(ureq, Event.DONE_EVENT);
					} else {
						showError("failed");
						fireEvent(ureq, Event.FAILED_EVENT);
					}
				} else {
					// copy file
					String filename = dialogFileUploadCtrl.getFileNameElValue();
					String chosenFile = dialogFileUploadCtrl.getFileChooserElValue();
					if (!chosenFile.contains("://")) {
						VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
						element = dialogElementsManager.doCopySelectedFile(chosenFile, filename, courseContainer,
								getIdentity(), entry, courseNode.getIdent(),
								dialogFileUploadCtrl.getAuthoredByElValue());
						markPublisherNews();
						loadModel();
					}
				}
				// after successful upload fireEvent which gets caught in NodeRunCtrl, so dialog gets opened
				fireEvent(ureq, new DialogRunEvent(element));
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				DialogElementRow row = tableModel.getObject(se.getIndex());
				if (FORUM.equals(se.getCommand())) {
					// case when table filename was selected
					fireEvent(ureq, new SelectRowEvent(row));
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				// empty table button
				loadModel();
				doUploadFile(ureq);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("uploadFile".equals(cmd)) {
				// upload file button top right
				loadModel();
				doUploadFile(ureq);
			} else if (ACTION_TOOLS_LINK.equals(cmd)) {
				doOpenTools(ureq, link);
			} else if (CMD_SELECT.equals(cmd)) {
				// card view selection by filename
				String fileKey = link.getName().replaceAll(".+?_", "");
				if (StringHelper.isLong(fileKey)) {
					element = dialogElementsManager.getDialogElementByKey(Long.valueOf(fileKey));
					DialogElementRow row = new DialogElementRow(element, userPropertyHandlers, getLocale());
					fireEvent(ureq, new SelectRowEvent(row));
				}
			}
		} else if ("ONCLICK".equals(event.getCommand())) {
			// case when card view element was selected
			String fileKey = ureq.getParameter("select_file");
			if (StringHelper.isLong(fileKey)) {
				element = dialogElementsManager.getDialogElementByKey(Long.valueOf(fileKey));
				DialogElementRow row = new DialogElementRow(element, userPropertyHandlers, getLocale());
				fireEvent(ureq, new SelectRowEvent(row));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenTools(UserRequest ureq, FormLink link) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), link.getName().replaceAll(".+?_", ""));
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	protected void doConfirmDelete(UserRequest ureq, DialogElement element) {
		String msg = translate("element.delete", element.getFilename());
		confirmDeletionCtrl = activateYesNoDialog(ureq, translate("delete"), msg, confirmDeletionCtrl);
		confirmDeletionCtrl.setUserObject(element);
	}

	private void doDelete(DialogElement elementToDelete) {
		if (elementToDelete == null) {
			loadModel();
		} else {
			// archive data to personal folder
			File exportDir = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), courseNode.getShortTitle());
			courseNode.doArchiveElement(elementToDelete, exportDir, getLocale(), getIdentity());

			dialogElementsManager.deleteDialogElement(elementToDelete);
			//do logging
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DELETED, getClass(),
					LoggingResourceable.wrapUploadFile(elementToDelete.getFilename()));
		}
	}

	/**
	 * deliver the selected file and show in a popup
	 *
	 * @param ureq
	 * @param command
	 */
	private void doFileDelivery(UserRequest ureq) {
		VFSContainer forumContainer = dialogElementsManager.getDialogContainer(element);
		List<VFSItem> items = forumContainer.getItems(new VFSLeafFilter());
		if (!items.isEmpty() && items.get(0) instanceof VFSLeaf vl) {
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(vl));
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DOWNLOADED, getClass(),
					LoggingResourceable.wrapBCFile(vl.getName()));
		} else {
			logError("No file to discuss: {}" + forumContainer, null);
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> comps = new ArrayList<>(2);
		if (rowObject instanceof DialogElementRow dialogElementRow && (dialogElementRow.getSelectLink() != null)) {
				comps.add(dialogElementRow.getSelectLink().getComponent());
		}
		return comps;
	}

	/**
	 * Inform subscription manager about new element.
	 */
	private void markPublisherNews() {
		if (secCallback.getSubscriptionContext() != null) {
			notificationsManager.markPublisherNews(secCallback.getSubscriptionContext(), getIdentity(), true);
		}
	}

	private static final class DialogElementListCssDelegate extends DefaultFlexiTableCssDelegate {

		private static final DialogElementListCssDelegate DELEGATE = new DialogElementListCssDelegate();

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_dialog_file_list";
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return FlexiTableRendererType.custom == type
					? "o_dialog_file_rows o_block_top o_dialog_cards"
					: "o_dialog_file_rows o_block_top";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_dialog_file_row";
		}
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link deleteLink;
		private final Link downloadLink;

		public ToolsController(UserRequest ureq, WindowControl wControl, String rowToDeleteKey) {
			super(ureq, wControl);
			element = dialogElementsManager.getDialogElementByKey(Long.valueOf(rowToDeleteKey));

			mainVC = createVelocityContainer(ACTION_TOOLS_LINK);

			List<String> links = new ArrayList<>(2);

			deleteLink = addLink("delete", "o_icon_delete_item", links);
			downloadLink = addLink("download", "o_icon_download", links);
			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}

		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (deleteLink == source) {
				close();
				doConfirmDelete(ureq, element);
			} else if (source == downloadLink) {
				close();
				doFileDelivery(ureq);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
