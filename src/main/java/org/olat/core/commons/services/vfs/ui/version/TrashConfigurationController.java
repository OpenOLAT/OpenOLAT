/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.vfs.ui.version;

import org.olat.admin.sysinfo.FilesAndFoldersController;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TrashConfigurationController extends FormBasicController {
	
	private TextElement retentionEl;
	
	@Autowired
	private VFSRepositoryModule vfsRepositoryModule;

	protected TrashConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(FilesAndFoldersController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("filesfolders.menu.trash");
		
		retentionEl = uifactory.addTextElement("trash.delete.days", 3, String.valueOf(vfsRepositoryModule.getTrashRetentionDays()), formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		retentionEl.clearError();
		if (!StringHelper.containsNonWhitespace(retentionEl.getValue())) {
			retentionEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			try {
				Integer days = Integer.parseInt(retentionEl.getValue());
				if (days.intValue() < 0) {
					retentionEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				retentionEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		vfsRepositoryModule.setTrashRetentionDays(Integer.valueOf(retentionEl.getValue()));
	}

}
