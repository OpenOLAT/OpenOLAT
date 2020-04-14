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
package org.olat.core.commons.controllers.impressum;

import java.io.File;

import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/* 
 * Initial date: 12 Apr 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class PrivacyPolicyExtension extends GenericActionExtension {

	
	private final ImpressumModule impressumModule;
	
	public PrivacyPolicyExtension(ImpressumModule impressumModule) {
		this.impressumModule = impressumModule;
	}
	

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return new PrivacyPolicyController(ureq, wControl);
	}

	@Override
	public ExtensionElement getExtensionFor(String extensionPoint, UserRequest ureq) {		
		boolean enabled = false;
		
		if (impressumModule.isEnabled()) {
			enabled = true;
			
			File baseFolder = impressumModule.getPrivacyPolicyDirectory();
			if (new File(baseFolder, "index_" + ureq.getLocale().getLanguage() + ".html").exists()) {
				enabled &= true;
			} else if(new File (baseFolder, "index_de.html").exists()) {
					enabled &= true;
			} else if(new File (baseFolder, "index_en.html").exists()) {
					enabled &= true;
			} else {
				enabled &= false;
			}
		}
		
		return enabled ? super.getExtensionFor(extensionPoint, ureq) : null;
	}
}
