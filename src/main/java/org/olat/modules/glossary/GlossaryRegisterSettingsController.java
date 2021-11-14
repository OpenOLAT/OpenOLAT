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

import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * allows to set register/index on/off for repository typ glossary
 * 
 * <P>
 * Initial Date:  20.01.2009 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryRegisterSettingsController extends FormBasicController {

	private final boolean readOnly;
	private OLATResource olatresource;
	private MultipleSelectionElement regOnOff;
	private LocalFolderImpl glossaryFolder;
	
	@Autowired
	private GlossaryManager glossaryManager;
	@Autowired
	private GlossaryItemManager glossaryItemManager;

	public GlossaryRegisterSettingsController(UserRequest ureq, WindowControl control, OLATResource resource, boolean readOnly) {
		super(ureq, control);
		this.readOnly = readOnly;
		this.olatresource = resource;
		glossaryFolder = glossaryManager.getGlossaryRootFolder(olatresource);
	
		initForm(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source==regOnOff){
			boolean regOnChecked = regOnOff.isSelected(0);
			Properties glossProps = glossaryItemManager.getGlossaryConfig(glossaryFolder);
			glossProps.put(GlossaryItemManager.REGISTER_ONOFF, String.valueOf(regOnChecked));
			glossaryItemManager.setGlossaryConfig(glossaryFolder, glossProps);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// saved in innerEvent
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("register.title");
		setFormDescription("register.intro");
		String[] regKeys = {"true"};
		String[] regValues = {""};
		
		regOnOff = uifactory.addCheckboxesHorizontal("register.onoff", formLayout, regKeys, regValues);
		regOnOff.addActionListener(FormEvent.ONCLICK);
		regOnOff.setEnabled(!readOnly);
		
		Properties glossProps = glossaryItemManager.getGlossaryConfig(glossaryFolder);
		String configuredStatus = glossProps.getProperty(GlossaryItemManager.REGISTER_ONOFF);
		if (configuredStatus!=null){
			regOnOff.select(configuredStatus, true);
		}		
	}

}
