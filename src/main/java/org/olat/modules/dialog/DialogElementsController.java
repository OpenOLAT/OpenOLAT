/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.title.TitleInfo;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogConfigForm;
import org.olat.course.nodes.dialog.DialogNodeForumCallback;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumUIFactory;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * TODO: guido Class Description for DialogController
 * <P>
 * Initial Date: 03.11.2005 <br>
 * 
 * @author guido
 */
public class DialogElementsController extends BasicController {
	
	protected static final String ACTION_START_FORUM = "startforum";
	protected static final String ACTION_SHOW_FILE = "showfile";
	protected static final String ACTION_DELETE_ELEMENT = "delete";
	private static final int TABLE_RESULTS_PER_PAGE = 5;

	private DialogElementsPropertyManager dialogElmsMgr;
	private VelocityContainer content;
	private TableController tableCtr;
	private CourseNode courseNode;
	private FileUploadController fileUplCtr;
	private LinkChooserController fileCopyCtr;
	private StackedPanel dialogPanel;
	private ForumManager forumMgr;
	private DialogElement recentDialogElement, selectedElement;
	private DialogElementsTableModel tableModel;
	private DialogBoxController confirmDeletionCtr;
	private ContextualSubscriptionController csCtr;
	private DialogNodeForumCallback forumCallback;
	private SubscriptionContext subsContext;
	private CoursePropertyManager coursePropMgr;
	private boolean isOlatAdmin;
	private boolean isGuestOnly;
	private NodeEvaluation nodeEvaluation;
	private UserCourseEnvironment userCourseEnv;
	private TableGuiConfiguration tableConf;
	private Link uploadButton;
	private Link copyButton;
	private Controller forumCtr;
	private CloseableModalController cmc;

	public DialogElementsController(UserRequest ureq, WindowControl wControl, CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			NodeEvaluation nodeEvaluation) {
		super(ureq, wControl);
		this.nodeEvaluation = nodeEvaluation;
		this.userCourseEnv = userCourseEnv;
		this.coursePropMgr = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
		this.courseNode = courseNode;
		forumMgr = ForumManager.getInstance();
		dialogElmsMgr = DialogElementsPropertyManager.getInstance();

		content = createVelocityContainer("dialog");		
		uploadButton = LinkFactory.createButton("dialog.upload.file", content, this);
		uploadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
		
		isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();

		// fxdiff VCRP-12: copy files from course folder
		// add copy from course folder if user has course editor rights (course owner and users in a right group with the author right)
		Identity identity = ureq.getIdentity();
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry entry = rm.lookupRepositoryEntry(course, true);
		if (isOlatAdmin || rm.isOwnerOfRepositoryEntry(identity, entry)
				|| userCourseEnv.getCourseEnvironment().getCourseGroupManager().hasRight(identity, CourseRights.RIGHT_COURSEEDITOR)) {
			copyButton = LinkFactory.createButton("dialog.copy.file", content, this);
		}
		
		forumCallback = new DialogNodeForumCallback(nodeEvaluation, isOlatAdmin, isGuestOnly, subsContext);
		content.contextPut("security", forumCallback);
		
		if(isGuestOnly){
			//guests cannot subscribe (OLAT-2019)
			subsContext = null;
		}else{
			subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode);
		}
		
		// if sc is null, then no subscription is desired
		if (subsContext != null) {
			// FIXME:fj: implement subscription callback for group forums
			String businessPath = wControl.getBusinessControl().getAsString();
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(DialogElement.class), "", businessPath);
			csCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
			listenTo(csCtr);
			content.put("subscription", csCtr.getInitialComponent());
		}
		//configure and display table
		tableConf = new TableGuiConfiguration();
		tableConf.setResultsPerPage(TABLE_RESULTS_PER_PAGE);
		tableConf.setPreferencesOffered(true, "FileDialogElementsTable");
		tableConf.setDownloadOffered(true);
		dialogPanel = putInitialPanel(content);
		showOverviewTable(ureq, forumCallback);
	}

	private void showOverviewTable(UserRequest ureq, DialogNodeForumCallback callback) {
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConf, ureq, getWindowControl(), getTranslator());
		DialogPropertyElements elements = dialogElmsMgr.findDialogElements(coursePropMgr, courseNode);
		List<DialogElement> list = new ArrayList<DialogElement>();
		tableModel = new DialogElementsTableModel(getTranslator(), callback, courseNode.getModuleConfiguration());
		if (elements != null) list = elements.getDialogPropertyElements();
		for (Iterator<DialogElement> iter = list.iterator(); iter.hasNext();) {
			DialogElement element = iter.next();
			Integer msgCount = forumMgr.countMessagesByForumID(element.getForumKey());
			element.setMessagesCount(msgCount);
			element.setNewMessages(new Integer(msgCount.intValue()
					- forumMgr.countReadMessagesByUserAndForum(ureq.getIdentity(), element.getForumKey())));
		}
		tableModel.setEntries(list);
		tableModel.addColumnDescriptors(tableCtr);
		tableCtr.setTableDataModel(tableModel);
		tableCtr.modelChanged();
	    tableCtr.setSortColumn(3, false);
	    listenTo(tableCtr);
		content.put("dialogElementsTable", tableCtr.getInitialComponent());
		dialogPanel.setContent(content);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		DialogElement entry = null;
		// process table events
		if (source == tableCtr) {
			TableEvent te = (TableEvent) event;
			String command = te.getActionId();
			int row = te.getRowId();
			entry = tableModel.getEntryAt(tableCtr.getSortedRow(row));
			if (command.equals(ACTION_START_FORUM)) {
				selectedElement = dialogElmsMgr.findDialogElement(coursePropMgr, courseNode, entry.getForumKey());
				if(selectedElement == null){
					showInfo("element.already.deleted");
					return;
				}
				Forum forum = null;
				forum = forumMgr.loadForum(entry.getForumKey());
				content.contextPut("hasSelectedElement", Boolean.TRUE);
				content.contextPut("selectedElement", selectedElement);

				// display forum either inline or as popup
				String integration = (String) courseNode.getModuleConfiguration().get(DialogConfigForm.DIALOG_CONFIG_INTEGRATION);
				
				subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode, forum.getKey().toString());
				forumCallback = new DialogNodeForumCallback(nodeEvaluation, isOlatAdmin, isGuestOnly, subsContext);
				content.contextPut("security", forumCallback);
				
				if (integration.equals(DialogConfigForm.CONFIG_INTEGRATION_VALUE_INLINE)) {
					removeAsListenerAndDispose(forumCtr);
					forumCtr = ForumUIFactory.getStandardForumController(ureq, getWindowControl(), forum, forumCallback);
					listenTo(forumCtr);
					content.contextPut("hasInlineForum", Boolean.TRUE);
					content.put("forum", forumCtr.getInitialComponent());
				} else {
					content.contextPut("hasInlineForum", Boolean.FALSE);					
					TitleInfo titleInfo = new TitleInfo(translate("dialog.selected.element"), selectedElement.getFilename());
					PopupBrowserWindow pbw = ForumUIFactory.getPopupableForumController(ureq, getWindowControl(), forum, forumCallback, titleInfo);
					pbw.open(ureq);
				}

			} else if (command.equals(ACTION_SHOW_FILE)) {
				doFileDelivery(ureq, entry.getForumKey());
			} else if (command.equals(ACTION_DELETE_ELEMENT)) {
				selectedElement = entry;
				confirmDeletionCtr = activateYesNoDialog(ureq, translate("delete"), translate("element.delete", entry.getFilename()), confirmDeletionCtr);
				return;
			}
			// process file upload events
		} else if (source == cmc) {
			// reset recent element
			recentDialogElement = null;
			showOverviewTable(ureq, forumCallback);
			// cleanup
			removeAsListenerAndDispose(cmc);
			cmc = null;
			removeAsListenerAndDispose(fileUplCtr);
			fileUplCtr = null;
			
		} else if (source == fileUplCtr) {
			// clear dialog
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
			// event.
			if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				String filename = null;
				try {
					// get size of file
					OlatRootFolderImpl forumContainer = getForumContainer(recentDialogElement.getForumKey());
					VFSLeaf vl = (VFSLeaf) forumContainer.getItems().get(0);
					String fileSize = Formatter.formatBytes(vl.getSize());

					// new dialog element
					filename = ((FolderEvent) event).getFilename();
					DialogElement element = new DialogElement();
					element.setAuthor(recentDialogElement.getAuthor());
					element.setDate(new Date());
					element.setFilename(filename);
					element.setForumKey(recentDialogElement.getForumKey());
					element.setFileSize(fileSize);

					// do logging
					//ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(), LoggingResourceable.wrapUploadFile(filename));


					// inform subscription manager about new element
					if (subsContext != null) {
						NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
					}
					//everything when well so save the property
					dialogElmsMgr.addDialogElement(coursePropMgr, courseNode, element);
				} catch (Exception e) {
					//
					throw new OLATRuntimeException(DialogElementsController.class, "Error while adding new 'file discussion' element with filename: "+filename, e);
				}
			}
			// cleanup file upload controller
			removeAsListenerAndDispose(fileUplCtr);
			fileUplCtr = null;
			// reset recent element
			recentDialogElement = null;
			showOverviewTable(ureq, forumCallback);

		} else if (source == fileCopyCtr) {
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				// reset recent element
				recentDialogElement = null;
				showOverviewTable(ureq, forumCallback);
			} else if (event instanceof URLChoosenEvent) {
				URLChoosenEvent choosenEvent = (URLChoosenEvent)event;
				String fileUrl = choosenEvent.getURL();
				if(fileUrl.indexOf("://") < 0) {
					//copy file
					VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
					VFSLeaf vl = (VFSLeaf) courseContainer.resolve(fileUrl);
					OlatRootFolderImpl forumContainer = getForumContainer(recentDialogElement.getForumKey());
					VFSLeaf copyVl = forumContainer.createChildLeaf(vl.getName());
					if(copyVl == null) {
						copyVl = (VFSLeaf)forumContainer.resolve(vl.getName());
					}
					VFSManager.copyContent(vl, copyVl);
					
					// get size of file
					String fileSize = Formatter.formatBytes(copyVl.getSize());

					DialogElement element = new DialogElement();
					element.setAuthor(recentDialogElement.getAuthor());
					element.setDate(new Date());
					element.setFilename(vl.getName());
					element.setForumKey(recentDialogElement.getForumKey());
					element.setFileSize(fileSize);

					// do logging
					//ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(), LoggingResourceable.wrapUploadFile(filename));

					// inform subscription manager about new element
					if (subsContext != null) {
						NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
					}
					//everything when well so save the property
					dialogElmsMgr.addDialogElement(coursePropMgr, courseNode, element);
				}
				
				//not supported
				recentDialogElement = null;
				showOverviewTable(ureq, forumCallback);
			}
		} else if (source == confirmDeletionCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				DialogCourseNode node = (DialogCourseNode) courseNode;
				// archive data to personal folder
				node.doArchiveElement(selectedElement, CourseFactory.getOrCreateDataExportDirectory(getIdentity(), node.getShortTitle()), getLocale());
				// delete element
				dialogElmsMgr.deleteDialogElement(coursePropMgr, courseNode, selectedElement.getForumKey());
				forumMgr.deleteForum(selectedElement.getForumKey());
				showOverviewTable(ureq, forumCallback);
				content.contextPut("hasSelectedElement", Boolean.FALSE);
				//do logging
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DELETED, getClass(),
						LoggingResourceable.wrapUploadFile(selectedElement.getFilename()));
			}
		}
	}

	/**
	 * deliver the selected file and show in a popup
	 * 
	 * @param ureq
	 * @param command
	 */
	private void doFileDelivery(UserRequest ureq, Long forumKey) {
		OlatRootFolderImpl forumContainer = getForumContainer(forumKey);
		List<VFSItem> items = forumContainer.getItems(new VFSLeafFilter());
		if(items.size() > 0 && items.get(0) instanceof VFSLeaf) {
			VFSLeaf vl = (VFSLeaf)items.get(0);
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(vl));
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DOWNLOADED, getClass(),
					LoggingResourceable.wrapBCFile(vl.getName()));
		} else {
			logError("No file to discuss: " + forumContainer, null);
		}
	}

	public void event(UserRequest ureq, Component source, Event event) {
		// process my content events
		if (source == content) {
			String command = event.getCommand();
			if (command.equals(ACTION_SHOW_FILE)) {
				doFileDelivery(ureq, selectedElement.getForumKey());
			}
		} else if (source == uploadButton){
			Forum forum = forumMgr.addAForum();
			OlatRootFolderImpl forumContainer = getForumContainer(forum.getKey());
			
			removeAsListenerAndDispose(fileUplCtr);
			fileUplCtr = new FileUploadController(getWindowControl(),forumContainer, ureq, (int)FolderConfig.getLimitULKB(), Quota.UNLIMITED, null, false, false, false, true, false);
			listenTo(fileUplCtr);
			
			recentDialogElement = new DialogElement();
			recentDialogElement.setForumKey(forum.getKey());
			recentDialogElement.setAuthor(ureq.getIdentity().getName());
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", fileUplCtr.getInitialComponent(), true, translate("dialog.upload.file"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == copyButton) {
			Forum forum = forumMgr.addAForum();
			VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
			fileCopyCtr = new MyLinkChooserController(ureq, getWindowControl(), courseContainer, null);
			listenTo(fileCopyCtr);
			
			recentDialogElement = new DialogElement();
			recentDialogElement.setForumKey(forum.getKey());
			recentDialogElement.setAuthor(ureq.getIdentity().getName());
			dialogPanel.setContent(fileCopyCtr.getInitialComponent());
		}

	}

	/**
	 * to save content
	 * 
	 * @param forumKey
	 * @return
	 */
	public static OlatRootFolderImpl getForumContainer(Long forumKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("/forum/");
		sb.append(forumKey);
		sb.append("/");
		String pathToForumDir = sb.toString();
		OlatRootFolderImpl forumContainer = new OlatRootFolderImpl(pathToForumDir, null);
		File baseFile = forumContainer.getBasefile();
		baseFile.mkdirs();
		return forumContainer;
	}
	
	public static String getFileSize(Long forumKey){
		OlatRootFolderImpl forumContainer = getForumContainer(forumKey);
		VFSLeaf vl = (VFSLeaf) forumContainer.getItems().get(0);
		return Formatter.formatBytes(vl.getSize());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
	
	private class MyLinkChooserController extends LinkChooserController {
		public MyLinkChooserController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir, String uploadRelPath) {
			super(ureq, wControl, rootDir, uploadRelPath, null, null, "", null, true);
		}
		
		@Override
		//this is a hack to overwrite the package used by the BasicController
		protected VelocityContainer createVelocityContainer(String page) {
			setTranslator(Util.createPackageTranslator(LinkChooserController.class, getLocale()));
			velocity_root = Util.getPackageVelocityRoot(LinkChooserController.class);
			return super.createVelocityContainer(page);
		}

		@Override
		public void event(UserRequest ureq, Controller source, Event event) {
			fireEvent(ureq, event);
		}
	}
}
