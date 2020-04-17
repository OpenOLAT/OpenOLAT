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
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/* 
 * Initial date: 12 Apr 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class TermsOfUseExtension extends GenericActionExtension {

	
	private final ImpressumModule impressumModule;
	
	public TermsOfUseExtension(ImpressumModule impressumModule) {
		this.impressumModule = impressumModule;
	}
	

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return new TermsOfUseController(ureq, wControl);
	}

	@Override
	public ExtensionElement getExtensionFor(String extensionPoint, UserRequest ureq) {
		boolean enabled = false;
		
		if (impressumModule.isEnabled()) {
			VFSContainer impressumDir = new LocalFolderImpl(impressumModule.getTermsOfUseDirectory());
			enabled = true;
			
			if (checkContent(impressumDir.resolve("index_" + ureq.getLocale().getLanguage() + ".html"))) {
				// Nothing to do here
			} else if (checkContent(impressumDir.resolve("index_en.html"))) {
				// Nothing to do here
			} else if (checkContent(impressumDir.resolve("index_de.html"))) {
				// Nothing to do here
			} else {
				// Nothing found
				enabled &= false;
			}
		}
		
		return enabled ? super.getExtensionFor(extensionPoint, ureq) : null;
	}
	
	private boolean checkContent(VFSItem file) {
		boolean check = false;
		if(file instanceof VFSLeaf && file.exists() ) {
			if(file instanceof LocalFileImpl) {
				File f = ((LocalFileImpl)file).getBasefile();
				try {
					String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
					content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
					if(content.length() > 0) {
						content = content.trim();
					}
					if(content.length() > 0) {
						check = true;
					}
				} catch (IOException e) {
					// Nothing to to here
				}
			} else {
				check = true;
			}
		}
		return check;
	}
}
