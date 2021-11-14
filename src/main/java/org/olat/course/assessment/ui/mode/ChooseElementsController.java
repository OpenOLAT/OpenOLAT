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
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;

/**
 * 
 * Initial date: 15.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseElementsController extends FormBasicController {

	private MenuTreeItem selectTree;
	private TreeModel treeModel;
	private FormLink selectAll, deselectAll;

	private final OLATResourceable ores;
	private final List<String> preSelectedKeys;

	public ChooseElementsController(UserRequest ureq, WindowControl wControl, List<String> selectedKeys, OLATResourceable ores) {
		super(ureq, wControl, "course_elements");
		this.ores = OresHelper.clone(ores);
		preSelectedKeys = selectedKeys;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		treeModel = new CourseNodeSelectionTreeModel(course);
		selectTree = uifactory.addTreeMultiselect("elements", null, formLayout, treeModel, this);
		selectTree.setMultiSelect(true);
		selectTree.setSelectedKeys(preSelectedKeys);
		
		selectAll = uifactory.addFormLink("checkall", "form.checkall", null, formLayout, Link.LINK);
		deselectAll = uifactory.addFormLink("uncheckall", "form.uncheckall", null, formLayout, Link.LINK);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", formLayout);
	}
	
	public List<String> getSelectedKeys() {
		Set<String> selectedKeys = selectTree.getSelectedKeys();
		return new ArrayList<>(selectedKeys);
	}
	
	public List<String> getSelectedNames() {
		Set<String> selectedKeys = selectTree.getSelectedKeys();
		List<String> names = new ArrayList<>(selectedKeys.size());
		for(String selectedKey:selectedKeys) {
			
			TreeNode node = treeModel.getNodeById(selectedKey);
			if(node == null) {
				//not published??
			} else {
				names.add(node.getTitle());
			}
		}
		
		return names;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectAll == source) {
			selectTree.selectAll();
		} else if(deselectAll == source) {
			selectTree.deselectAll();
		}
		super.formInnerEvent(ureq, source, event);
	}
}