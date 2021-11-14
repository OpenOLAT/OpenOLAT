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
package org.olat.ims.qti21.ui.statistics;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 13 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21PrintController extends BasicController {

	private int count = 0;
	private final VelocityContainer mainVC;
	
	public QTI21PrintController(UserRequest ureq, WindowControl wControl, QTI21StatisticResourceResult resourceResult) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("print");
		initView(ureq, resourceResult);
		
		MainPanel mainPanel = new MainPanel("statsPrintPanel");
		mainPanel.setContent(mainVC);
		mainPanel.setCssClass("o_qti_print");
		putInitialPanel(mainPanel);
	}
	
	private void initView(UserRequest ureq, QTI21StatisticResourceResult resourceResult) {
		TreeNode rootNode;
		if(resourceResult.getCourseEntry() != null) {
			rootNode = resourceResult.getSubTreeModel().getRootNode();
			ICourse course = CourseFactory.loadCourse(resourceResult.getCourseEntry());
			mainVC.contextPut("courseTitle", course.getCourseTitle());
		} else {
			rootNode = resourceResult.getTreeModel().getRootNode();
		}
		String testTitle = resourceResult.getTestEntry().getDisplayname();
		mainVC.contextPut("testTitle", testTitle);
		
		List<String> pageNames = new ArrayList<>();
		// append the root for informations
		Controller assessmentCtrl = resourceResult.getController(ureq, getWindowControl(), null, rootNode, true);
		String pageName = "page" + count++;
		mainVC.put(pageName, assessmentCtrl.getInitialComponent());
		pageNames.add(pageName);
		// append all assessment items
		appendNodes(ureq, rootNode, resourceResult, pageNames);

		mainVC.contextPut("pageNames", pageNames);
	}
	
	private void appendNodes(UserRequest ureq, TreeNode node, QTI21StatisticResourceResult resourceResult, List<String> pageNames) {
		if(node.getUserObject() instanceof AssessmentItemRef) {
			Controller itemCtrl = resourceResult.getController(ureq, getWindowControl(), null, node, true);
			if(itemCtrl != null) {
				String itemPageName = "page" + count++;
				mainVC.put(itemPageName, itemCtrl.getInitialComponent());
				pageNames.add(itemPageName);
			}
		}
		
		for(int i=0; i<node.getChildCount(); i++) {
			appendNodes(ureq, (TreeNode)node.getChildAt(i), resourceResult, pageNames);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
