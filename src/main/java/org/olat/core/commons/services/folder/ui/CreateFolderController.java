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
package org.olat.core.commons.services.folder.ui;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CreateFolderController extends FormBasicController {

	private TextElement textElementEl;

	private final VFSContainer currentContainer;
	private VFSContainer createdItem;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	protected CreateFolderController(UserRequest ureq, WindowControl wControl, VFSContainer currentContainer) {
		super(ureq, wControl);
		// For validation messages
		setTranslator(Util.createPackageTranslator(FileUploadController.class, getLocale(), getTranslator()));
		this.currentContainer = currentContainer;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		textElementEl = uifactory.addTextElement("fileName", "folder.name", -1, "", formLayout);
		textElementEl.setDisplaySize(20);
		textElementEl.setMandatory(true);
		textElementEl.setFocus(true);
		textElementEl.setElementCssClass("o_sel_folder_new_folder_name");
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("folder.create.button", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
	}

	public VFSContainer getCreatedContainer() {
		return createdItem;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String name = textElementEl.getValue();
		if(!StringHelper.containsNonWhitespace(name)) {
			textElementEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			if (!FileUtils.validateFilename(name)) {
				textElementEl.setErrorKey("cf.name.notvalid");
				allOk &= false;
			} else {
				VFSItem item = currentContainer.resolve(name);
				if (item != null) {
					textElementEl.setErrorKey("cf.exists", new String[] {name});
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = textElementEl.getValue().trim();
		createdItem = currentContainer.createChildContainer(name);
		if (createdItem != null && createdItem.canMeta() == VFSStatus.YES) {
			VFSMetadata metaInfo = createdItem.getMetaInfo();
			if (metaInfo instanceof VFSMetadataImpl metadata) {
				metadata.setFileInitializedBy(getIdentity());
				vfsRepositoryService.updateMetadata(metaInfo);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
