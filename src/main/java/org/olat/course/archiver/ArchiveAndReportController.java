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
package org.olat.course.archiver;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.FOCourseNode;
import org.olat.instantMessaging.ui.IMArchiverController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchiveAndReportController extends MainLayoutBasicController implements Activateable2 {
	
	private MenuTree menuTree;
	private GenericTreeNode forums;
	private GenericTreeNode reports;
	private GenericTreeNode logFiles;
	private GenericTreeNode courseChat;
	private GenericTreeNode courseArchive;
	private GenericTreeNode courseResults;

	private Controller archiveCtrl;
	private final LayoutMain3ColsController columnLayoutCtr;

	private final OLATResourceable ores;
	private final RepositoryEntry courseEntry;
	
	public ArchiveAndReportController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.ores = OresHelper.clone(courseEntry.getOlatResource());
		
		menuTree = new MenuTree("menuTree");
		TreeModel tm = buildTreeModel(); 
		menuTree.setTreeModel(tm);
		menuTree.setRootVisible(false);
		menuTree.setOpenNodeIds(List.of("Reports"));
		menuTree.addListener(this);
		
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, new Panel("empty"), "course" + ores.getResourceableId());
		listenTo(columnLayoutCtr); // cleanup on dispose
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		menuTree.setSelectedNode(courseArchive);
		doOpenNode(ureq, menuTree.getSelectedNode());
	}

	/**
	 * Generates the archiver menu
	 * @return The generated menu tree model
	 */
	private TreeModel buildTreeModel() {
		GenericTreeModel overviewTreeModel = new GenericTreeModel();
		GenericTreeNode overviewRootNode = new GenericTreeNode("Root");
		overviewTreeModel.setRootNode(overviewRootNode);
		
		courseArchive = new GenericTreeNode("CourseArchive");
		courseArchive.setTitle(translate("menu.course.archive"));
		courseArchive.setIconCssClass("o_icon_coursearchive");
		overviewRootNode.addChild(courseArchive);
		
		logFiles = new GenericTreeNode("Orders");
		logFiles.setTitle(translate("menu.archivelogfiles"));
		logFiles.setIconCssClass("o_icon_courselog");
		overviewRootNode.addChild(logFiles);
		
		reports = new GenericTreeNode("Reports");
		reports.setTitle(translate("menu.reports"));
		reports.setDelegate(overviewRootNode);
		overviewRootNode.addChild(reports);
		
		courseResults = new GenericTreeNode("CourseResults");
		courseResults.setTitle(translate("menu.scoreaccounting"));
		courseResults.setIconCssClass("o_icon_score");
		reports.addChild(courseResults);
		courseResults.setDelegate(reports);
		
		courseChat = new GenericTreeNode("CourseChat");
		courseChat.setTitle(translate("menu.chat"));
		courseChat.setIconCssClass("o_icon_chat");
		reports.addChild(courseChat);
		
		forums = new GenericTreeNode("Forums");
		forums.setTitle(translate("menu.forums"));
		forums.setIconCssClass("o_fo_icon");
		reports.addChild(forums);
		
		return overviewTreeModel;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(menuTree == source) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selectedTreeNode = menuTree.getSelectedNode();
				doOpenNode(ureq, selectedTreeNode);
			}
		}
	}
	
	private void doOpenNode(UserRequest ureq, TreeNode treeNode) {
		if(treeNode == courseArchive) {
			doOpenController(new CourseArchiveListController(ureq, getWindowControl(), courseEntry, true));
		} else if(treeNode == logFiles) {
			doOpenController(new CourseLogsArchiveController(ureq, getWindowControl(), ores));
		} else if(treeNode == reports) {
			doOpenController(new ScoreAccountingArchiveController(ureq, getWindowControl(), ores));
			menuTree.setSelectedNode(courseResults);
		} else if(treeNode == courseResults) {
			doOpenController(new ScoreAccountingArchiveController(ureq, getWindowControl(), ores));
		} else if(treeNode == courseChat) {
			doOpenController(new IMArchiverController(ureq, getWindowControl(), ores));
		} else if(treeNode == forums) {
			doOpenController(new ForumArchiveController(ureq, getWindowControl(), ores, false, new FOCourseNode()));
		}
	}
	
	private void doOpenController(Controller ctrl) {
		removeAsListenerAndDispose(archiveCtrl);
		archiveCtrl = ctrl;
		listenTo(archiveCtrl);
		columnLayoutCtr.setCol3(archiveCtrl.getInitialComponent());
	}
}
