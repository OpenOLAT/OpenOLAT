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
package org.olat.ims.qti.statistics.ui;

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
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.statistic.StatisticResourceNode;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12PrintController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	public QTI12PrintController(UserRequest ureq, WindowControl wControl, QTIStatisticResourceResult resourceResult) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("print");
		initView(ureq, resourceResult);
		
		MainPanel mainPanel = new MainPanel("statsPrintPanel");
		mainPanel.setContent(mainVC);
		mainPanel.setCssClass("o_qti_print");
		putInitialPanel(mainPanel);
	}
	
	private void initView(UserRequest ureq, QTIStatisticResourceResult resourceResult) {
		StatisticResourceNode rootNode = (StatisticResourceNode)resourceResult.getSubTreeModel().getRootNode();
		
		ICourse course = CourseFactory.loadCourse(resourceResult.getCourseOres());
		mainVC.contextPut("courseTitle", course.getCourseTitle());
		String testTitle = resourceResult.getQTIRepositoryEntry().getDisplayname();
		mainVC.contextPut("testTitle", testTitle);
		
		int count = 0;
		List<String> pageNames = new ArrayList<>();

		Controller assessmentCtrl = resourceResult.getController(ureq, getWindowControl(), null, rootNode, true);
		
		String pageName = "page" + count++;
		mainVC.put(pageName, assessmentCtrl.getInitialComponent());
		pageNames.add(pageName);

		for(int i=0; i<rootNode.getChildCount(); i++) {
			INode sectionNode = rootNode.getChildAt(i);
			for(int j=0; j<sectionNode.getChildCount(); j++) {
				TreeNode itemNode = (TreeNode)sectionNode.getChildAt(j);
				Controller itemCtrl = resourceResult.getController(ureq, getWindowControl(), null, itemNode, true);
				
				String itemPageName = "page" + count++;
				mainVC.put(itemPageName, itemCtrl.getInitialComponent());
				pageNames.add(itemPageName);
			}
		}

		mainVC.contextPut("pageNames", pageNames);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
