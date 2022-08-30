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
package org.olat.modules.ceditor.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.TitleElement;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TitleInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private SingleSelection headingEl;
	
	private TitleElement title;
	private final PageElementStore<TitleElement> store;
	
	@Autowired
	private DB dbInstance;
	
	public TitleInspectorController(UserRequest ureq, WindowControl wControl, TitleElement title, PageElementStore<TitleElement> store) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.title = title;
		this.store = store;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.title");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues headingValues = new SelectionValues();
		String content = title.getContent();
		TitleSettings settings = title.getTitleSettings();
		
		int selectedHeading = -1;
		for(int i=1; i<=6; i++) {
			String size = Integer.toString(i);
			String heading = "h".concat(size);
			headingValues.add(SelectionValues.entry(size, heading));
			String headingTag = "<" + heading;
			if(content != null && content.startsWith(headingTag)) {
				selectedHeading = i;
			}
		}
		
		if(settings != null && settings.getSize() > 0) {
			selectedHeading = settings.getSize();
		}
		
		headingEl = uifactory.addDropdownSingleselect("heading.size", "heading.size", formLayout,
				headingValues.keys(), headingValues.values());
		headingEl.addActionListener(FormEvent.ONCHANGE);
		if(selectedHeading > 0 && selectedHeading <= 6) {
			headingEl.select(Integer.toString(selectedHeading), true);
		}
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(headingEl == source) {
			doChangeHeading(ureq, headingEl.getSelectedKey());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doChangeHeading(UserRequest ureq, String heading) {
		TitleSettings settings = new TitleSettings();
		settings.setSize(Integer.parseInt(heading));
		String settingsXml = ContentEditorXStream.toXml(settings);
		title.setLayoutOptions(settingsXml);
		doSave(ureq);
	}
	
	private void doSave(UserRequest ureq) {
		title = store.savePageElement(title);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(title));
	}
}
