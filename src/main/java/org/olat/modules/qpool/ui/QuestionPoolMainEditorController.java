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
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionPoolService;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolMainEditorController extends BasicController implements Activateable2, StackedControllerAware {

	private final MenuTree menuTree;
	private final Panel content;
	private StackedController stackPanel;

	private QuestionsController myQuestionsCtrl;
	private QuestionsController markedQuestionsCtrl;
	private QuestionsController selectedPoolCtrl;
	private LayoutMain3ColsController columnLayoutCtr;
	
	private final QuestionPoolService qpoolService;
	
	public QuestionPoolMainEditorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		
		menuTree = new MenuTree("qpoolTree");
		menuTree.setTreeModel(buildTreeModel());
		menuTree.setSelectedNode(menuTree.getTreeModel().getRootNode());
		menuTree.addListener(this);
		menuTree.setRootVisible(false);
		
		content = new Panel("list");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, null, content, "qpool");				
		
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
		if(myQuestionsCtrl != null) myQuestionsCtrl.setStackedController(stackPanel);
		if(markedQuestionsCtrl != null) markedQuestionsCtrl.setStackedController(stackPanel);
		if(selectedPoolCtrl != null) selectedPoolCtrl.setStackedController(stackPanel);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(menuTree == source) {
			TreeNode node = menuTree.getSelectedNode();
			Object uNode = node.getUserObject();
			if("menu.database.my".equals(uNode)) {
				doSelectMyQuestions(ureq);
			} else if("menu.database.favorit".equals(uNode)) {
				doSelectMarkedQuestions(ureq);
			} else if(uNode instanceof Pool) {
				Pool pool = (Pool)uNode;
				doSelect(ureq, pool);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	private void doSelectMyQuestions(UserRequest ureq) {
		if(myQuestionsCtrl == null) {
			myQuestionsCtrl = new QuestionsController(ureq, getWindowControl(), new MyQuestionItemsSource(getIdentity()));
			myQuestionsCtrl.setStackedController(stackPanel);
			listenTo(myQuestionsCtrl);
		}
		content.setContent(myQuestionsCtrl.getInitialComponent());
	}
	
	private void doSelectMarkedQuestions(UserRequest ureq) {
		if(markedQuestionsCtrl == null) {
			markedQuestionsCtrl = new QuestionsController(ureq, getWindowControl(), new MarkedItemsSource(getIdentity()));
			markedQuestionsCtrl.setStackedController(stackPanel);
			listenTo(markedQuestionsCtrl);
		}
		content.setContent(markedQuestionsCtrl.getInitialComponent());
	}
	
	private void doSelect(UserRequest ureq, Pool pool) {
		if(selectedPoolCtrl == null) {
			selectedPoolCtrl = new QuestionsController(ureq, getWindowControl(), new PooledItemsSource(pool));
			selectedPoolCtrl.setStackedController(stackPanel);
			listenTo(selectedPoolCtrl);
		}
		content.setContent(selectedPoolCtrl.getInitialComponent());
	}
	
	private TreeModel buildTreeModel() {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode rootNode = new GenericTreeNode(translate("topnav.qpool"), "topnav.qpool.alt");
		rootNode.setCssClass("o_sel_qpool_home");
		gtm.setRootNode(rootNode);
		
		//question database
		GenericTreeNode databaseNode = new GenericTreeNode(translate("menu.database"), "menu.database");
		databaseNode.setCssClass("o_sel_qpool_database");
		rootNode.addChild(databaseNode);
		
		GenericTreeNode node = new GenericTreeNode(translate("menu.database.my"), "menu.database.my");
		node.setIconCssClass("o_sel_qpool_my_items");
		databaseNode.addChild(node);
		
		node = new GenericTreeNode(translate("menu.database.favorit"), "menu.database.favorit");
		node.setIconCssClass("o_sel_qpool_favorits");
		databaseNode.addChild(node);

		//pools
		GenericTreeNode poolNode = new GenericTreeNode(translate("menu.pools"), "menu.pools.alt");
		poolNode.setCssClass("o_sel_qpool_pools");
		rootNode.addChild(poolNode);
		
		List<Pool> pools = qpoolService.getPools(getIdentity());
		for(Pool pool:pools) {
			node = new GenericTreeNode(pool.getName(), pool);
			node.setIconCssClass("o_sel_qpool_pool");
			poolNode.addChild(node);
		}
		return gtm;
	}
}
