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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.CourseNodeGroup;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseNodeController extends BasicController {
	
	private CourseNode createdNode;
	private final OLATResourceable courseOres;
	private final CourseEditorTreeNode currentNode;
	
	private Link multiSpsLink, multiCheckListLink;
	
	@Autowired
	private NodeAccessService nodeAccessService;

	public ChooseNodeController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CourseEditorTreeNode currentNode) {
		super(ureq, wControl);
		
		this.currentNode = currentNode;
		this.courseOres = courseOres;
		
		multiSpsLink = LinkFactory.createToolLink(EditorMainController.CMD_MULTI_SP,
				translate("command.multi.sps"), this, "o_icon_wizard");
		multiCheckListLink = LinkFactory.createToolLink(EditorMainController.CMD_MULTI_CHECKLIST,
				translate("command.multi.checklist"), this, "o_icon_wizard");
		
		VelocityContainer mainVC = createVelocityContainer("create_node");
		
		Map<String, CourseNodeTypesGroup> linkNames = new HashMap<>();
		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseNodeFactory cnf = CourseNodeFactory.getInstance();
		for (String courseNodeAlias : cnf.getRegisteredCourseNodeAliases()) {
			CourseNodeConfiguration cnConfig = cnf.getCourseNodeConfiguration(courseNodeAlias);
			boolean supportedNodeAccessType = nodeAccessService.isSupported(NodeAccessType.of(course), courseNodeAlias);
			if(cnConfig.isDeprecated() || !supportedNodeAccessType) {
				continue;
			}
			
			try {
				String group = cnConfig.getGroup();
				CourseNodeTypesGroup typesGroup = linkNames.get(group);
				if(typesGroup == null) {
					typesGroup = new CourseNodeTypesGroup(group);
					linkNames.put(group, typesGroup);
				}
				Link l = LinkFactory.createToolLink(courseNodeAlias, cnConfig.getLinkText(getLocale()), this, cnConfig.getIconCSSClass());
				l.setElementCssClass("o_sel_course_editor_node-" + courseNodeAlias);
				typesGroup.getNodeTypes().add(l.getComponentName());
				mainVC.put(l.getComponentName(), l);
				
				if("sp".equals(courseNodeAlias)) {
					typesGroup.getNodeTypes().add(multiSpsLink.getComponentName());
					mainVC.put(multiSpsLink.getComponentName(), multiSpsLink);
				} else if("checklist".equals(courseNodeAlias)) {
					typesGroup.getNodeTypes().add(multiCheckListLink.getComponentName());
					mainVC.put(multiCheckListLink.getComponentName(), multiCheckListLink);
				}
			} catch (Exception e) {
				logError("Error while trying to add a course buildingblock of type \""+courseNodeAlias +"\" to the editor", e);
			}
		}
		
		mainVC.contextPut("groupNames", CourseNodeGroup.values());
		mainVC.contextPut("linkNames", linkNames);
		putInitialPanel(mainVC);
	}
	
	public CourseNode getCreatedNode() {
		return createdNode;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(multiSpsLink == source) {
			fireEvent(ureq, event);
		} else if(multiCheckListLink == source) {
			fireEvent(ureq, event);
		} else if(source instanceof Link) {
			doCreateNode(event.getCommand());
			String cmd = EditorMainController.TB_ACTION + event.getCommand();
			fireEvent(ureq, new Event(cmd));
		}
	}
	
	private void doCreateNode(String type) {
		ICourse course = CourseFactory.getCourseEditSession(courseOres.getResourceableId());
		CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
		CourseEditorTreeNode selectedNode = null;
		int pos = 0;
		if(editorTreeModel.getRootNode().equals(currentNode)) {
			//root, add as last child
			pos = currentNode.getChildCount();
			selectedNode = currentNode;
		} else {
			selectedNode = (CourseEditorTreeNode)currentNode.getParent();
			pos = currentNode.getPosition() + 1;
		}
		
		// user chose a position to insert a new node
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(type);
		createdNode = newNodeConfig.getInstance();
		createdNode.updateModuleConfigDefaults(true, selectedNode, NodeAccessType.of(course));

		// Set some default values
		String title = newNodeConfig.getLinkText(getLocale());
		createdNode.setLongTitle(title);
		createdNode.setNoAccessExplanation(translate("form.noAccessExplanation.default"));
		
		// Add node
		editorTreeModel.insertCourseNodeAt(createdNode, selectedNode.getCourseNode(), pos);
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
	}
	
	public static class CourseNodeTypesGroup {
		private final String name;
		private final List<String> nodeTypes = new ArrayList<>();
		
		public CourseNodeTypesGroup(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public List<String> getNodeTypes() {
			return nodeTypes;
		}
	}
}
