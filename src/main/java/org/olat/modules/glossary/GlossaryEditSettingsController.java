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
package org.olat.modules.glossary;

import java.util.Properties;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.OLATResource;

/**
 * 
 * Description:<br>
 * Allow to change the edit permission on the glossary only owners / all users
 * 
 * <P>
 * Initial Date:  15 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GlossaryEditSettingsController extends FormBasicController {

	private OLATResource olatresource;
	private MultipleSelectionElement editByUserEnabled;
	private OlatRootFolderImpl glossaryFolder;

	public GlossaryEditSettingsController(UserRequest ureq, WindowControl control, OLATResource resource) {
		super(ureq, control);
		this.olatresource = resource;
		glossaryFolder = GlossaryManager.getInstance().getGlossaryRootFolder(olatresource);
	
		initForm(ureq);
	}	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormDescription("edit.intro");
		String[] regKeys = {"true"};
		String[] regValues = {""};
		
		editByUserEnabled = uifactory.addCheckboxesHorizontal("edit.onoff", formLayout, regKeys, regValues);
		editByUserEnabled.addActionListener(FormEvent.ONCLICK);
		
		Properties glossProps = GlossaryItemManager.getInstance().getGlossaryConfig(glossaryFolder);
		String configuredStatus = glossProps.getProperty(GlossaryItemManager.EDIT_USERS);
		if (configuredStatus != null){
			editByUserEnabled.select(configuredStatus, true);
		}		
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == editByUserEnabled){
			boolean editByUserChecked = editByUserEnabled.isSelected(0);
			GlossaryItemManager gIM = GlossaryItemManager.getInstance();
			Properties glossProps = gIM.getGlossaryConfig(glossaryFolder);
			glossProps.put(GlossaryItemManager.EDIT_USERS, String.valueOf(editByUserChecked));
			gIM.setGlossaryConfig(glossaryFolder, glossProps);
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// saved in innerEvent
	}
}