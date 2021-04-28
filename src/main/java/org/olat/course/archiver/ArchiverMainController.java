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

package org.olat.course.archiver;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ActionType;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.WikiCourseNode;

/**
 * Initial Date:  May 26, 2004
 * @author gnaegi
 * 
 */
public class ArchiverMainController extends MainLayoutBasicController implements Activateable2 {

	private static final String CMD_INDEX = "index";
	private static final String CMD_QTISURVRESULTS = "qtisurvresults";
	private static final String CMD_QTITESTRESULTS = "qtitestresults";
	private static final String CMD_SCOREACCOUNTING = "scoreaccounting";
	private static final String CMD_ARCHIVELOGFILES = "archivelogfiles";
	private static final String CMD_HANDEDINTASKS = "handedintasks";
	private static final String CMD_GROUPTASKS = "grouptask";
	private static final String CMD_PROJECTBROKER = "projectbroker";
	private static final String CMD_FORUMS = "forums";
	private static final String CMD_DIALOGS = "dialogs";
	private static final String CMD_WIKIS = "wikis";
	private static final String CMD_SCORM = "scorm";
	private static final String CMD_CHECKLIST = "checklist";
	private static final String CMD_PARTICIPANTFOLDER = "participantfolder";
	private static final String CMD_SURVEY = "survey";
	private static final String CMD_FORM = "form";
	
	
	private IArchiverCallback archiverCallback;
	private MenuTree menuTree;
	private VelocityContainer intro;
	private Panel main;
	private Controller resC;
	private Controller contentCtr;
	
	private OLATResourceable ores;
	
	private Locale locale;
	private LayoutMain3ColsController columnLayoutCtr;
	
	/**
	 * Constructor for the archiver main controller. This main controller has several subcontrollers which 
	 * implement different aspects of data that can be archived in an OLAT course
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param archiverCallback
	 */
	public ArchiverMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, IArchiverCallback archiverCallback) {
		super(ureq, wControl);		
		getUserActivityLogger().setStickyActionType(ActionType.admin);

		this.ores = ores;
		this.archiverCallback = archiverCallback;
		this.locale = ureq.getLocale();
		
		main = new Panel("archivermain");
		
		// Intro page, static
		intro = createVelocityContainer("archiver_index");
		main.setContent(intro);

		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = buildTreeModel(ureq); 
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);
				
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, main, "course" + ores.getResourceableId());
		listenTo(columnLayoutCtr); // cleanup on dispose
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // goto node in edit mode
				TreeNode selTreeNode = menuTree.getSelectedNode();
				Object cmd = selTreeNode.getUserObject();
				if(cmd instanceof ActionExtension) {
					launchExtensionController(ureq, (ActionExtension)cmd);
				} else {
					launchArchiveControllers(ureq, (String)cmd);
				}
			}
		}
	}

	/**
	 * 
	 * @param ureq
	 * @param cmd
	 */
	private void launchExtensionController(UserRequest ureq, ActionExtension ae) {
		removeAsListenerAndDispose(resC);
		ICourse course = CourseFactory.loadCourse(ores);
		resC = ae.createController(ureq, getWindowControl(), course);
		listenTo(resC);
		main.setContent(resC.getInitialComponent());
	}
	
	/**
	 * Generates the archiver menu
	 * @return The generated menu tree model
	 */
	private TreeModel buildTreeModel(UserRequest ureq) {
		GenericTreeNode root, gtn;
		
		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject(CMD_INDEX);
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		if (archiverCallback.mayArchiveQtiResults()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.qtiresults"));
			gtn.setUserObject(CMD_QTISURVRESULTS);
			gtn.setAltText(translate("menu.qtiresults.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveSurveys()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.surveys"));
			gtn.setUserObject(CMD_SURVEY);
			gtn.setAltText(translate("menu.surveys.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveQtiTestResults()) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.qtitestresults"));
			gtn.setUserObject(CMD_QTITESTRESULTS);
			gtn.setAltText("menu.qtitestresults.alt");
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveProperties()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.scoreaccounting"));
			gtn.setUserObject(CMD_SCOREACCOUNTING);
			gtn.setAltText(translate("menu.scoreaccounting.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveHandedInTasks()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.handedintasks"));
			gtn.setUserObject(CMD_HANDEDINTASKS);
			gtn.setAltText(translate("menu.handedintasks.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveHandedInTasks()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.grouptasks"));
			gtn.setUserObject(CMD_GROUPTASKS);
			gtn.setAltText(translate("menu.grouptasks.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveProjectBroker()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.projectbroker"));
			gtn.setUserObject(CMD_PROJECTBROKER);
			gtn.setAltText(translate("menu.projectbroker.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveLogfiles()) {
				gtn = new GenericTreeNode();		
				gtn.setTitle(translate("menu.archivelogfiles"));
				gtn.setUserObject(CMD_ARCHIVELOGFILES);
				gtn.setAltText(translate("menu.archivelogfiles.alt"));
				root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveForums()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.forums"));
			gtn.setUserObject(CMD_FORUMS);
			gtn.setAltText(translate("menu.forums.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveDialogs()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.dialogs"));
			gtn.setUserObject(CMD_DIALOGS);
			gtn.setAltText(translate("menu.dialogs.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveParticipantFolder()) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.participantfolder"));
			gtn.setUserObject(CMD_PARTICIPANTFOLDER);
			gtn.setAltText("menu.participantfolder.alt");
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveWikis()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.wikis"));
			gtn.setUserObject(CMD_WIKIS);
			gtn.setAltText(translate("menu.wikis.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveScorm()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.scorm"));
			gtn.setUserObject(CMD_SCORM);
			gtn.setAltText(translate("menu.scorm.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveChecklist()) {
			gtn = new GenericTreeNode();		
			gtn.setTitle(translate("menu.checklist"));
			gtn.setUserObject(CMD_CHECKLIST);
			gtn.setAltText(translate("menu.checklist.alt"));
			root.addChild(gtn);
		}
		if (archiverCallback.mayArchiveForms()) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.forms"));
			gtn.setUserObject(CMD_FORM);
			gtn.setAltText(translate("menu.forms.alt"));
			root.addChild(gtn);
		}
		
		
		//add extension menues
		ExtManager extm = ExtManager.getInstance();
		Class<?> extensionPointMenu = getClass();
		int cnt = extm.getExtensionCnt();
		for (int i = 0; i < cnt; i++) {
			Extension anExt = extm.getExtension(i);
			// check for sites
			ActionExtension ae = (ActionExtension) anExt.getExtensionFor(extensionPointMenu.getName(), ureq);
			if (ae != null && anExt.isEnabled()) {
				gtn = new GenericTreeNode();
				String menuText = ae.getActionText(locale);
				gtn.setTitle(menuText);
				gtn.setUserObject(ae);
				gtn.setAltText(ae.getDescription(locale));
				root.addChild(gtn);
			}
		}

		return gtm;
	}

	private void launchArchiveControllers(UserRequest ureq, String menuCommand) {
		if (menuCommand.equals(CMD_INDEX)) {
			main.setContent(intro);
		} else {
			removeAsListenerAndDispose(contentCtr);
			if (menuCommand.equals(CMD_QTISURVRESULTS)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new IQSURVCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_SURVEY)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new SurveyCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_QTITESTRESULTS)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, true, new IQTESTCourseNode(), new IQSELFCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_SCOREACCOUNTING)) {
					contentCtr = new ScoreAccountingArchiveController(ureq, getWindowControl(), ores);
					main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_ARCHIVELOGFILES)) {
				contentCtr = new CourseLogsArchiveController(ureq, getWindowControl(), ores);
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_HANDEDINTASKS)) { //TACourseNode
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new TACourseNode());
				main.setContent(contentCtr.getInitialComponent());
			}  else if (menuCommand.equals(CMD_GROUPTASKS)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new GTACourseNode(), new GTACourseNode(GTACourseNode.TYPE_INDIVIDUAL, null));
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_PROJECTBROKER)) { 
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new ProjectBrokerCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_FORUMS)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new FOCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_DIALOGS)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new DialogCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_WIKIS)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new WikiCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_SCORM)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new ScormCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_CHECKLIST)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new CheckListCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_PARTICIPANTFOLDER)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new PFCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			} else if (menuCommand.equals(CMD_FORM)) {
				contentCtr = new GenericArchiveController(ureq, getWindowControl(), ores, false, new FormCourseNode());
				main.setContent(contentCtr.getInitialComponent());
			}
			
			listenTo(contentCtr);
		}		
	}
	
	@Override
	protected void doDispose() {
		// controllers disposed by BasicController:
		columnLayoutCtr = null;
		resC = null;
		contentCtr = null;		
	}	
}
