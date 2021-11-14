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
package org.olat.modules.docpool.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolAdminConfigurationController extends  FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableEl, templatesDirectoryEl;
	private TextElement webDAVMountPointEl;
	private SingleSelection taxonomyTreeEl;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private DocumentPoolModule docPoolModule;
	
	public DocumentPoolAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateProperties();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormDescription("admin.description");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("document.pool.admin.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(docPoolModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		String mountPoint = docPoolModule.getWebDAVMountPoint();
		webDAVMountPointEl = uifactory.addTextElement("mount.point", "document.pool.webdav.mount.point", 32, mountPoint, formLayout);
		webDAVMountPointEl.setMandatory(true);
		
		String selectedTaxonomyTreeKey = docPoolModule.getTaxonomyTreeKey();
		List<Taxonomy> taxonomyList = taxonomyService.getTaxonomyList();
		String[] taxonomyKeys = new String[taxonomyList.size() + 1];
		String[] taxonomyValues = new String[taxonomyList.size() + 1];
		taxonomyKeys[0] = "";
		taxonomyValues[0] = "-";
		for(int i=taxonomyList.size(); i-->0; ) {
			Taxonomy taxonomy = taxonomyList.get(i);
			taxonomyKeys[i + 1] = taxonomy.getKey().toString();
			taxonomyValues[i + 1] = taxonomy.getDisplayName();
		}
		taxonomyTreeEl = uifactory.addDropdownSingleselect("selected.taxonomy.tree", formLayout, taxonomyKeys, taxonomyValues, null);
		taxonomyTreeEl.setMandatory(true);
		boolean found = false;
		if(StringHelper.containsNonWhitespace(selectedTaxonomyTreeKey)) {
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyTreeKey)) {
					taxonomyTreeEl.select(taxonomyKey, true);
					found = true;
				}
			}
		}
		if(!found && taxonomyKeys.length > 0) {
			taxonomyTreeEl.select(taxonomyKeys[0], true);
		}
		
		templatesDirectoryEl = uifactory.addCheckboxesHorizontal("document.pool.templates.directory", formLayout,
				onKeys, onValues);
		if(docPoolModule.isTemplatesDirectoryEnabled()) {
			templatesDirectoryEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateProperties() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		webDAVMountPointEl.setVisible(enabled);
		taxonomyTreeEl.setVisible(enabled);
		templatesDirectoryEl.setVisible(enabled);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		taxonomyTreeEl.clearError();
		if(enableEl.isAtLeastSelected(1) && (!taxonomyTreeEl.isOneSelected() || !StringHelper.isLong(taxonomyTreeEl.getSelectedKey()))) {
			taxonomyTreeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		webDAVMountPointEl.clearError();
		if(enableEl.isAtLeastSelected(1)) {
			if(!StringHelper.containsNonWhitespace(webDAVMountPointEl.getValue())) {
				webDAVMountPointEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableEl) {
			updateProperties();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		docPoolModule.setEnabled(enabled);
		
		if(enabled) {
			String selectedTaxonomyTreeKey = taxonomyTreeEl.getSelectedKey();
			docPoolModule.setTaxonomyTreeKey(selectedTaxonomyTreeKey);
			String mountPoint = webDAVMountPointEl.getValue();
			docPoolModule.setWebDAVMountPoint(mountPoint);
			docPoolModule.setTemplatesDirectoryEnabled(templatesDirectoryEl.isAtLeastSelected(1));
		} else {
			docPoolModule.setTaxonomyTreeKey("");
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
