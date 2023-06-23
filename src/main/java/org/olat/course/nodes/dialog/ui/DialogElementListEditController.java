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
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.ui.DialogElementsTableModel.DialogCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The list of files to discuss use in the course element editor.
 * <p>
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DialogElementListEditController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = DialogElementsTableModel.class.getCanonicalName();

	private FlexiTableElement tableEl;
	private DialogElementsTableModel tableModel;
	
	private int counter = 0;
	private final RepositoryEntry entry;
	private final DialogCourseNode courseNode;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserCourseEnvironment userCourseEnv;

	private CloseableModalController cmc;
	private DialogBoxController confirmDeletionCtr;
	private DialogFileUploadController dialogFileUploadCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private DialogElementsManager dialogElementsManager;
	@Autowired
	private NotificationsManager notificationsManager;
	
	public DialogElementListEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
										   UserCourseEnvironment userCourseEnv, DialogCourseNode courseNode) {
		super(ureq, wControl, "element_list");
		setTranslator(Util.createPackageTranslator(DialogCourseNodeRunController.class, getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.entry = entry;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.downloadLink));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.publishedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.authoredBy));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.creationDate));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DialogCols.toolsLink));

		tableModel = new DialogElementsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setPageSize(25);
		tableEl.setEmptyTableSettings("table.empty.message", null, "o_dialog_icon", "dialog.upload.file", "o_icon_add", false);
	}
	
	protected void loadModel() {
		List<DialogElement> elements = dialogElementsManager.getDialogElements(entry, courseNode.getIdent());
		List<DialogElementRow> rows = new ArrayList<>(elements.size());

		for (DialogElement element : elements) {
			DialogElementRow row = new DialogElementRow(element, userPropertyHandlers, getLocale());
			VFSLeaf item = dialogElementsManager.getDialogLeaf(element);
			if(item != null) {
				DownloadLink downloadLink = uifactory.addDownloadLink("file_" + counter, row.getFilename(), null, item, flc);
				row.setDownloadLink(downloadLink);
			}
			FormLink toolsLink = uifactory.addFormLink("tools_" + row.getDialogElementKey(), "tools", translate("table.header.action"), null, null, Link.NONTRANSLATED);
			row.setToolsLink(toolsLink);
			counter++;
			rows.add(row);
		}
		counter = 0;
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void doUploadFile(UserRequest ureq) {
		removeAsListenerAndDispose(dialogFileUploadCtrl);

		dialogFileUploadCtrl = new DialogFileUploadController(ureq, getWindowControl(), userCourseEnv, true);
		listenTo(dialogFileUploadCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), dialogFileUploadCtrl.getInitialComponent(),
				true, translate("dialog.upload.file"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doFinalizeUploadFile(VFSLeaf file) {
		//everything when well so save the property
		DialogElement element = dialogElementsManager.createDialogElement(entry, getIdentity(), file.getName(),
				file.getSize(), courseNode.getIdent(), dialogFileUploadCtrl.getAuthoredByElValue());
		VFSContainer dialogContainer = dialogElementsManager.getDialogContainer(element);
		VFSManager.copyContent(file.getParentContainer(), dialogContainer);

		markPublisherNews();
		loadModel();
	}

	/**
	 * Inform subscription manager about new element.
	 */
	private void markPublisherNews() {
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode);
		notificationsManager.markPublisherNews(subsContext, getIdentity(), true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDeletionCtr) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doDelete((DialogElementRow)confirmDeletionCtr.getUserObject());
				loadModel();
			}
		} else if (source == dialogFileUploadCtrl) {
			if(event == Event.DONE_EVENT) {
				VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
				if (dialogFileUploadCtrl.getActionSelectedKey().equals(DialogFileUploadController.DIALOG_ACTION_UPLOAD)) {
					VFSLeaf newFile = dialogElementsManager.doUpload(
							dialogFileUploadCtrl.getFileUploadEl().getUploadFile(),
							dialogFileUploadCtrl.getFileNameElValue(),
							courseContainer,
							getIdentity());

					if (newFile != null) {
						doFinalizeUploadFile(newFile);
						fireEvent(ureq, Event.DONE_EVENT);
					} else {
						showError("failed");
						fireEvent(ureq, Event.FAILED_EVENT);
					}
				} else {
					String filename = dialogFileUploadCtrl.getFileNameElValue();
					String chosenFile = dialogFileUploadCtrl.getFileChooserElValue();
					if(!chosenFile.contains("://")) {
						DialogElement element = dialogElementsManager.doCopySelectedFile(chosenFile, filename, courseContainer,
								getIdentity(), entry, courseNode.getIdent(),
								dialogFileUploadCtrl.getAuthoredByElValue());
						if (element != null) {
							markPublisherNews();
							loadModel();
						}
					}
				}
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(dialogFileUploadCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		dialogFileUploadCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				DialogElementRow row = tableModel.getObject(se.getIndex());
				if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, row);
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				loadModel();
				doUploadFile(ureq);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("tools".equals(cmd)) {
				doOpenTools(ureq, link);
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
	
	private void doConfirmDelete(UserRequest ureq, DialogElementRow row) {
		String msg = translate("element.delete", row.getFilename());
		confirmDeletionCtr = activateYesNoDialog(ureq, translate("delete"), msg, confirmDeletionCtr);
		confirmDeletionCtr.setUserObject(row);
	}
	
	private void doDelete(DialogElementRow rowToDelete) {
		DialogElement elementToDelete = dialogElementsManager.getDialogElementByKey(rowToDelete.getDialogElementKey());
		// archive data to personal folder
		File exportDir = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), courseNode.getShortTitle());
		courseNode.doArchiveElement(elementToDelete, exportDir, getLocale(), getIdentity());

		dialogElementsManager.deleteDialogElement(elementToDelete);
		//do logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DELETED, getClass(),
				LoggingResourceable.wrapUploadFile(elementToDelete.getFilename()));
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link deleteLink;
		private final DialogElementRow dialogElementRow;

		public ToolsController(UserRequest ureq, WindowControl wControl, String rowToDelete) {
			super(ureq, wControl);
			DialogElement element = dialogElementsManager.getDialogElementByKey(Long.valueOf(rowToDelete));
			this.dialogElementRow = new DialogElementRow(element, userPropertyHandlers, getLocale());

			mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>(2);

			deleteLink = addLink("delete", "o_icon_delete_item", links);
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
				doConfirmDelete(ureq, dialogElementRow);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
