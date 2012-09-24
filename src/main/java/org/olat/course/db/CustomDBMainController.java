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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.course.db;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.properties.Property;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for CustomDBMainController
 * 
 * <P>
 * Initial Date:  7 avr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CustomDBMainController extends GenericMainController {
	
	public static final String CUSTOM_DB = "custom_db";
	
	private ICourse course;
	private ToolController toolC;
	
	private CustomDBController dbController;
	private CustomDBAddController addController;
	private CloseableModalController cmc;

	public CustomDBMainController(UserRequest ureq, WindowControl windowControl, ICourse course) {
		super(ureq, windowControl);
		this.course = course;
	
		// Tool and action box
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		toolC.addHeader(translate("tool.name"));
		toolC.addLink("cmd.new_db", translate("command.new_db"), null, "b_toolbox_link b_new");
		toolC.addLink("cmd.close", translate("command.closedb"), null, "b_toolbox_close");
		setToolController(toolC);
		
		//set main node
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle(translate("main.menu.title"));
		root.setAltText(translate("main.menu.title.alt"));
		root.setUserObject("dbs");
		addChildNodeToPrepend(root);

		init(ureq);
		
		getMenuTree().setRootVisible(false);
	}

	@Override
	protected void doDispose() {
		// controllers disposed by BasicController:
	}
	
	private void disposeAddController() {
		removeAsListenerAndDispose(addController);
		removeAsListenerAndDispose(cmc);
		addController = null;
		cmc = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == toolC) {
			if (event.getCommand().equals("cmd.close")) {
				doDispose();
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event.getCommand().equals("cmd.new_db")) {
				removeAsListenerAndDispose(addController);
				addController = new CustomDBAddController(ureq, getWindowControl());
				listenTo(addController);
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			}
		} else if (source == addController) {
			if(event == Event.DONE_EVENT) {
				String category = addController.getCategory();
				addCustomDb(category);
				dbController.updateUI();
			}
			cmc.deactivate();
			disposeAddController();
		}
	}
	
	@Override
	protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq) {
		if("dbs".equals(uobject)) {
			dbController = new CustomDBController(ureq, getWindowControl(), course.getResourceableId());
			return dbController;
		}
		return null;
	}
	
	private void addCustomDb(final String category) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(course, new SyncerExecutor() {
			@Override
			public void execute() {
				CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
				CourseNode rootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode()).getCourseNode();
				Property p = cpm.findCourseNodeProperty(rootNode, null, null, CUSTOM_DB);
				if(p == null) {
					p = cpm.createCourseNodePropertyInstance(rootNode, null, null, CUSTOM_DB, null, null, null, category);
					cpm.saveProperty(p);
				} else {
					String currentDbs = p.getTextValue();
					p.setTextValue(currentDbs + ":" + category);
					cpm.updateProperty(p);
				}
			}
		});
	}
}
