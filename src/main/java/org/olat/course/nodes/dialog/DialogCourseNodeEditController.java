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

package org.olat.course.nodes.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.Quota;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.dialog.DialogElement;
import org.olat.modules.dialog.DialogElementsController;
import org.olat.modules.dialog.DialogElementsPropertyManager;
import org.olat.modules.dialog.DialogElementsTableModel;
import org.olat.modules.dialog.DialogPropertyElements;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * controller for the tabbed pane inside the course editor for the course node 'dialog elements'
 * <P>
 * Initial Date: 02.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_DIALOGCONFIG = "pane.tab.dialogconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	
	private static final String[] paneKeys = { PANE_TAB_DIALOGCONFIG, PANE_TAB_ACCESSIBILITY };
	
	private VelocityContainer content, accessContent;	
	private DialogCourseNode courseNode;
	private ConditionEditController readerCondContr, posterCondContr, moderatorCondContr;
	private TabbedPane myTabbedPane;
	private BCCourseNode bcNode = new BCCourseNode();
	private ICourse course;
	private DialogConfigForm configForumLaunch;
	private TableController tableCtr;
	private Translator resourceTrans;
	private FileUploadController fileUplCtr;
	private DialogElement recentElement;
	private TableGuiConfiguration tableConf;
	private Link uploadButton;
	
	public DialogCourseNodeEditController(UserRequest ureq, WindowControl wControl, DialogCourseNode node,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		super(ureq,wControl);
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.course = course;
		this.courseNode = node;
		
		resourceTrans = Util.createPackageTranslator(DialogElementsTableModel.class, ureq.getLocale(), getTranslator());
		// set name of the folder we use
		bcNode.setShortTitle(translate("dialog.folder.name"));

		// dialog specific config tab		
		content = this.createVelocityContainer("edit");
		uploadButton = LinkFactory.createButton("dialog.upload.file", content, this);
		
		//configure table
		tableConf = new TableGuiConfiguration();
		tableConf.setResultsPerPage(10);
		showOverviewTable(ureq);

		initConfigForm(ureq);

		// accessability config tab		
		accessContent = this.createVelocityContainer("edit_access");

		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		// Reader precondition
		Condition readerCondition = courseNode.getPreConditionReader();
		// TODO:gs:a getAssessableNodes ist der dialog node assessable oder nicht?
		readerCondContr = new ConditionEditController(ureq, getWindowControl(), userCourseEnv, readerCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));		
		listenTo(readerCondContr);
		accessContent.put("readerCondition", readerCondContr.getInitialComponent());

		// Poster precondition
		Condition posterCondition = courseNode.getPreConditionPoster();
		posterCondContr = new ConditionEditController(ureq, getWindowControl(), userCourseEnv, posterCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));		
		this.listenTo(posterCondContr);
		accessContent.put("posterCondition", posterCondContr.getInitialComponent());

		// Moderator precondition
		Condition moderatorCondition = courseNode.getPreConditionModerator();
		moderatorCondContr = new ConditionEditController(ureq, getWindowControl(), userCourseEnv, moderatorCondition,
				AssessmentHelper.getAssessableNodes(editorModel, courseNode));
		//FIXME:gs: why is firing needed here?
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);		
		this.listenTo(moderatorCondContr);
		accessContent.put("moderatorCondition", moderatorCondContr.getInitialComponent());
	}

	private void initConfigForm(UserRequest ureq) {
		
		removeAsListenerAndDispose(configForumLaunch);
		configForumLaunch = new DialogConfigForm(ureq, getWindowControl(), courseNode.getModuleConfiguration());
		listenTo(configForumLaunch);
		
		content.put("showForumAsPopupConfigForm", configForumLaunch.getInitialComponent());
	}


	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getPaneKeys()
	 */
	public String[] getPaneKeys() {
		return paneKeys;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getTabbedPane()
	 */
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == uploadButton) {
			Forum forum = ForumManager.getInstance().addAForum();
			OlatRootFolderImpl forumContainer = DialogElementsController.getForumContainer(forum.getKey());
			
			removeAsListenerAndDispose(fileUplCtr);
			fileUplCtr = new FileUploadController(getWindowControl(),forumContainer, ureq, (int)FolderConfig.getLimitULKB(), Quota.UNLIMITED, null, false);			
			listenTo(fileUplCtr);
			
			recentElement = new DialogElement();
			recentElement.setForumKey(forum.getKey());
			recentElement.setAuthor(ureq.getIdentity().getName());
			content.contextPut("overview", Boolean.FALSE);
			content.put("upload", fileUplCtr.getInitialComponent());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == configForumLaunch) {
			if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == readerCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = readerCondContr.getCondition();
				courseNode.setPreConditionReader(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == posterCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = posterCondContr.getCondition();
				courseNode.setPreConditionPoster(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == moderatorCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = moderatorCondContr.getCondition();
				courseNode.setPreConditionModerator(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == tableCtr) {
			// process table events
		} else if (source == fileUplCtr) {
			// event.
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				// reset recent element
				recentElement = null;
				showOverviewTable(ureq);
			} else if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {

				// new dialog element
				DialogElement element = new DialogElement();
				element.setAuthor(recentElement.getAuthor());
				element.setDate(new Date());
				final String filename = ((FolderEvent) event).getFilename();
				element.setFilename(filename);
				element.setForumKey(recentElement.getForumKey());
				element.setFileSize(DialogElementsController.getFileSize(recentElement.getForumKey()));

				// save property
				DialogElementsPropertyManager.getInstance().addDialogElement(course.getCourseEnvironment().getCoursePropertyManager(), courseNode, element);

				// do logging
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(),
						LoggingResourceable.wrapUploadFile(filename));
			}
		}
	}
/**
 * update table with latest elements
 * @param ureq
 */
	private void showOverviewTable(UserRequest ureq) {
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConf, ureq, getWindowControl(), resourceTrans);
		listenTo(tableCtr);
		
		DialogPropertyElements elements = DialogElementsPropertyManager.getInstance().findDialogElements(this.course.getCourseEnvironment().getCoursePropertyManager(), courseNode);
		List<DialogElement> list = new ArrayList<>();
		DialogElementsTableModel tableModel = new DialogElementsTableModel(getTranslator(), null, null);
		if (elements != null) list = elements.getDialogPropertyElements();
		tableModel.setEntries(list);
		tableModel.addColumnDescriptors(tableCtr);
		tableCtr.setTableDataModel(tableModel);
		tableCtr.modelChanged();
		tableCtr.setSortColumn(1, true);
		content.contextPut("overview", Boolean.TRUE);
		content.put("dialogElementsTable", tableCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
    //child controllers registered with listenTo() get disposed in BasicController
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.tabbedpane.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessContent);
		tabbedPane.addTab(translate(PANE_TAB_DIALOGCONFIG), content);
	}

}
