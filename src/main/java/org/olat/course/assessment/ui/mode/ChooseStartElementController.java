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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.INodeFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;

/**
 * 
 * Initial date: 19.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseStartElementController extends FormBasicController {

	private MenuTreeItem selectTree;
	private GenericTreeModel treeModel;

	private final OLATResourceable ores;
	private final String preSelectedKey;
	private final List<String> elementKeys;

	public ChooseStartElementController(UserRequest ureq, WindowControl wControl,
			String selectedKey, List<String> elementKeys, OLATResourceable ores) {
		super(ureq, wControl, "course_element");
		this.ores = OresHelper.clone(ores);
		preSelectedKey = selectedKey;
		this.elementKeys = elementKeys;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		treeModel = new CourseNodeSelectionTreeModel(course);
		selectTree = uifactory.addTreeMultiselect("elements", null, formLayout, treeModel, this);
		selectTree.setSelectedNodeId(preSelectedKey);
		selectTree.setNoDirtyCheckOnClick(true);
		if(elementKeys != null) {
			selectTree.setFilter(new RestionctionsFilter(elementKeys, course));
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", formLayout);
	}
	
	public String getSelectedKey() {
		return selectTree.getSelectedNodeId();
	}
	
	public String getSelectedName() {
		String selectedKey = getSelectedKey();
		String name = null;

		TreeNode node = treeModel.getNodeById(selectedKey);
		if(node == null) {
			//not published??
		} else {
			name = node.getTitle();
		}
		return name;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private class RestionctionsFilter implements INodeFilter {
		
		private final List<String> keysAndParents = new ArrayList<>();
		
		public RestionctionsFilter(List<String> keys, ICourse course) {
			Structure runstructure = course.getRunStructure();
			for(String nodeId:keys) {
				//allow the parent line
				for(INode courseNode = runstructure.getNode(nodeId); courseNode != null; courseNode = courseNode.getParent()) {
					keysAndParents.add(courseNode.getIdent());
				}
			}
		}

		@Override
		public boolean isVisible(INode node) {
			return keysAndParents.contains(node.getIdent());
		}	
	}
}