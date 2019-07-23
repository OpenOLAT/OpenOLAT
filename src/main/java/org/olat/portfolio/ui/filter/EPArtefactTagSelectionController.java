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
package org.olat.portfolio.ui.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.manager.EPFrontendManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * edit tag filter with all available tags of this user
 * 
 * <P>
 * Initial Date:  28.10.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactTagSelectionController extends FormBasicController {

	private List<String> selectedTagsList;
	@Autowired
	private EPFrontendManager ePFMgr;
	private MultipleSelectionElementImpl chkBox;

	public EPArtefactTagSelectionController(UserRequest ureq, WindowControl wControl, List<String> selectedTagsList) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.selectedTagsList = selectedTagsList;
		initForm(ureq);		
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("filter.tag.intro");
		
		Map<String, String> allUserTags = ePFMgr.getUsersMostUsedTags(getIdentity(), -1);
		LinkedList<Entry<String, String>> sortEntrySet = new LinkedList<>(allUserTags.entrySet());
		String[] keys = new String[sortEntrySet.size()];
		String[] values = new String[sortEntrySet.size()];
		int i=0;
		for (Entry<String, String> entry : sortEntrySet) {
			String tag = entry.getValue();
			keys[i] = tag;
			values[i] = tag; 
			i++;
		}
		chkBox = (MultipleSelectionElementImpl) uifactory.addCheckboxesVertical("tag", null, formLayout, keys, values, 2);
		
		if (selectedTagsList != null) {
			String[] selectedKeys = selectedTagsList.toArray(new String[0]);
			chkBox.setSelectedValues(selectedKeys);
		}
		chkBox.addActionListener(FormEvent.ONCHANGE);
		uifactory.addFormSubmitButton("filter.type.submit", formLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (selectedTagsList == null) selectedTagsList = new ArrayList<>();
		Collection<String> selectedKeys = chkBox.getSelectedKeys();
		Set<String> allKeys = chkBox.getKeys();
		for (String actTag : allKeys) {
			boolean selected = selectedKeys.contains(actTag);
			if (selected && !selectedTagsList.contains(actTag)) {
				selectedTagsList.add(actTag);
			} 
			if (!selected && selectedTagsList.contains(actTag)) {
				selectedTagsList.remove(actTag);
			}
		}
		if (selectedTagsList.size() == 0) selectedTagsList = null;
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//nothing
	}

}
