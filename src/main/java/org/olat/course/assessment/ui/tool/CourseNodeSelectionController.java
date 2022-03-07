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
package org.olat.course.assessment.ui.tool;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeSelectionController extends BasicController {
	
	private final MenuTree menuTree;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public CourseNodeSelectionController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry) {
		super(ureq, wControl);
	
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = AssessmentHelper.assessmentTreeModel(course, getLocale());
		menuTree.setTreeModel(tm);
		menuTree.setExpandSelectedNode(true);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);
		
		VelocityContainer mainVC = createVelocityContainer("course_node_chooser");
		mainVC.put("courseTree", menuTree);
		putInitialPanel(mainVC);
	}
	
	public void selectedCourseNode(CourseNode courseNode) {
		TreeNode selectedNode = TreeHelper.findNodeByUserObject(courseNode, menuTree.getTreeModel().getRootNode());
		if(selectedNode != null) {
			menuTree.setSelectedNode(selectedNode);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(TreeEvent.COMMAND_TREENODE_OPEN.equals(te.getSubCommand()) || TreeEvent.COMMAND_TREENODE_CLOSE.equals(te.getSubCommand())) {
					Object uo = menuTree.getSelectedNode().getUserObject();
					if(menuTree.getSelectedNode() == menuTree.getTreeModel().getRootNode()) {
						//do nothing
					} else {
						CourseNode selectedNode = (CourseNode)uo;
						AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(selectedNode);
						if (assessmentConfig.isAssessable() && assessmentConfig.isEditable()) {
							fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, selectedNode.getIdent()));
						}
					}
				} else if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
					Object uo = menuTree.getSelectedNode().getUserObject();
					if(menuTree.getSelectedNode() == menuTree.getTreeModel().getRootNode()) {
						CourseNode rootNode = (CourseNode)uo;
						fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, rootNode.getIdent()));
					} else {
						CourseNode selectedNode = (CourseNode)uo;
						AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(selectedNode);
						if (assessmentConfig.isAssessable() && assessmentConfig.isEditable()) {
							fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, selectedNode.getIdent()));
						}
					}
				}
			}
		}
	}
}