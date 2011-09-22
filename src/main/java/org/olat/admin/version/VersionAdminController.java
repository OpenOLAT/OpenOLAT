/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.admin.version;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.version.SimpleVersionConfig;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public class VersionAdminController extends FormBasicController {
	
	private SingleSelection numOfVersions;
	
	private String[] keys = new String[] {
			"0","2","3","4","5","10","25","50","-1"
	};
	
	private String[] values = new String[] {
			"0","2","3","4","5","10","25","50","-1"
	};

	public VersionAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// use combined translator from system admin main
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
		
		initForm(this.flc, this, ureq);
		
		values[0] = getTranslator().translate("version.off");
		values[values.length - 1] = getTranslator().translate("version.unlimited");
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// First add title and context help
		setFormTitle("version.title");
		setFormDescription("version.intro");
		setFormContextHelp(VersionAdminController.class.getPackage().getName(), "version.html", "help.hover.version");
		
		numOfVersions = uifactory.addDropdownSingleselect("version.numOfVersions", formLayout, keys, values, null);
		Long maxNumber = getNumOfVersions();
		if(maxNumber == null) {
			numOfVersions.select("0", true);
		} else if (maxNumber.longValue() == -1l) {
			numOfVersions.select("-1", true);
		} else {
			String str = maxNumber.toString();
			boolean found = false;
			for(String value:values) {
				if(value.equals(str)) {
					found = true;
					break;
				}
			}
			
			if(found) {
				numOfVersions.select(str, true);
			} else {
				//set a default value if the saved number is not in the list,
				//normally not possible but...
				numOfVersions.select("10", true);
			}
		}
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String num = numOfVersions.getSelectedKey();
		if(num == null || num.length() == 0) return;
		
		try {
			int maxNumber = Integer.parseInt(num);
			setNumOfVersions(maxNumber);
			getWindowControl().setInfo("saved");
		} catch (NumberFormatException e) {
			showError("version.notANumber");
		}
	}
	
	public Long getNumOfVersions() {
		SimpleVersionConfig config = (SimpleVersionConfig) CoreSpringFactory.getBean(SimpleVersionConfig.class);
		return config.getMaxNumberOfVersionsProperty();
	}
	
	public void setNumOfVersions(int maxNumber) {
		SimpleVersionConfig config = (SimpleVersionConfig) CoreSpringFactory.getBean(SimpleVersionConfig.class);
		config.setMaxNumberOfVersionsProperty(new Long(maxNumber));
	}
}
