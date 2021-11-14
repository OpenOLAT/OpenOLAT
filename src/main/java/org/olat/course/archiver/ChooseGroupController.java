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
package org.olat.course.archiver;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseGroupController extends FormBasicController {
	
	private final List<CourseNode> courseNodes;
	private final List<BusinessGroup> relatedGroups;
	private SingleSelection selectGroupEl;
	
	public ChooseGroupController(UserRequest ureq, WindowControl wControl,
			List<CourseNode> courseNodes, List<BusinessGroup> relatedGroups) {
		super(ureq, wControl);
		this.courseNodes = courseNodes;
		this.relatedGroups = relatedGroups;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("select.group.desc");
		
		String[] theKeys = new String[1 + relatedGroups.size()];
		theKeys[0] = "all";
		String[] theValues = new String[1 + relatedGroups.size()];
		theValues[0] = translate("select.group.all");
		for(int i=0; i<relatedGroups.size(); i++) {
			theKeys[i+1] = Integer.toString(i);
			theValues[i+1] = StringHelper.escapeHtml(relatedGroups.get(i).getName());
		}
		selectGroupEl = uifactory.addDropdownSingleselect("select.group", formLayout, theKeys, theValues, null);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setRootForm(mainForm);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", buttonLayout);
	}
	
	public BusinessGroup getSelectedGroup() {
		if(!selectGroupEl.isOneSelected() || selectGroupEl.isSelected(0)) {
			return null;
		}
		String key = selectGroupEl.getSelectedKey();
		if(StringHelper.isLong(key)) {
			int pos = Integer.parseInt(key);
			if(pos >= 0 && pos <relatedGroups.size()) {
				return relatedGroups.get(pos);
			}
		}
		return null;
	}

	public List<CourseNode> getCourseNodes() {
		return courseNodes;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}