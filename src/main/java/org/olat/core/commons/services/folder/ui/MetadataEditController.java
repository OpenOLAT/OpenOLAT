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

import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MetadataEditController extends FormBasicController {

	private MetaInfoFormController metadataCtrl;
	
	private VFSItem vfsItem;
	private String resourceUrl;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager vfsLockManager;

	public MetadataEditController(UserRequest ureq, WindowControl wControl, VFSItem vfsItem, String resourceUrl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.vfsItem = vfsItem;
		this.resourceUrl = resourceUrl;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		metadataCtrl = new MetaInfoFormController(ureq, getWindowControl(), mainForm, vfsItem, resourceUrl, true, true, true);
		formLayout.add("metadata", metadataCtrl.getFormItem());
		listenTo(metadataCtrl);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (metadataCtrl.isFileRenamed() && !metadataCtrl.getFilenameEl().hasError()) {
			String filename = metadataCtrl.getFilename();
			VFSContainer container = vfsItem.getParentContainer();
			if (container.resolve(filename) != null) {
				if (vfsItem instanceof VFSContainer) {
					metadataCtrl.getFilenameEl().setErrorKey("error.exists.container");
				} else {
					metadataCtrl.getFilenameEl().setErrorKey("error.exists.leaf");
				}
				allOk &= false;
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
		updateMetadata(metadataCtrl.getMetaInfo(), metadataCtrl.getFilename(), metadataCtrl.isFileRenamed());
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void updateMetadata(VFSMetadata metadata, String fileName, boolean fileRenamed) {
		if (metadata == null) {
			return;
		}
			
		if (!vfsItem.exists()) {
			if (vfsItem instanceof VFSContainer) {
				showError("error.deleted.container");
			} else {
				showError("error.deleted.leaf");
			}
			return;
		}
		
		if (vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null)) {
			showError("error.metadata.not.saved");
			return;
		}
		
		vfsRepositoryService.updateMetadata(metadata);
		if (fileRenamed) {
			VFSContainer container = vfsItem.getParentContainer();
			if (container.resolve(fileName) != null) {
				showError("error.metadata.not.saved");
				return;
			}
			
			VFSStatus rename = vfsItem.rename(fileName);
			if (VFSStatus.NO.equals(rename)) {
				showError("error.metadata.not.saved");
				return;
			}
		}
	}

}
