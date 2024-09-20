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
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSSuccess;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RenameController extends FormBasicController {

	private TextElement titleEl;
	private TextElement filenameEl;
	
	private final VFSItem vfsItem;
	private final VFSMetadata vfsMetadata;
	private final String initialFilename;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager vfsLockManager;

	protected RenameController(UserRequest ureq, WindowControl wControl, VFSItem vfsItem, VFSMetadata vfsMetadata) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetaInfoFormController.class, getLocale(), getTranslator()));
		this.vfsItem = vfsItem;
		this.vfsMetadata = vfsMetadata;
		initialFilename = vfsItem.getName();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (vfsMetadata != null) {
			titleEl = uifactory.addTextElement("title", "mf.title", -1, vfsMetadata.getTitle(), formLayout);
		}
		
		filenameEl = uifactory.addTextElement("filename", "mf.filename", -1, initialFilename, formLayout);
		filenameEl.setMandatory(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		filenameEl.clearError();
		if (isFileRenamed()) {
			String filename = filenameEl.getValue();
			if (!StringHelper.containsNonWhitespace(filename)) {
				filenameEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (!FileUtils.validateFilename(filename)) {
				if (vfsItem instanceof VFSContainer) {
					filenameEl.setErrorKey("folder.name.notvalid");
				} else {	
					filenameEl.setErrorKey("file.name.notvalid");
				}
				allOk = false;
			}
			if (allOk) {
				VFSContainer container = vfsItem.getParentContainer();
				if (container.resolve(filename) != null) {
					if (vfsItem instanceof VFSContainer) {
						filenameEl.setErrorKey("error.exists.container");
					} else {
						filenameEl.setErrorKey("error.exists.leaf");
					}
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
	
	public boolean isFileRenamed() {
		String filename = filenameEl.getValue();
		if (initialFilename == null || filename == null) {
			return false;
		}
		return (!initialFilename.equals(filename));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateMetadata();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void updateMetadata() {
		if (vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null)) {
			showError("error.metadata.not.saved");
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
		
		if (vfsMetadata != null) {
			vfsMetadata.setTitle(titleEl.getValue());
			vfsRepositoryService.updateMetadata(vfsMetadata);
		}
		
		if (isFileRenamed()) {
			VFSContainer container = vfsItem.getParentContainer();
			String filename = filenameEl.getValue();
			if (container.resolve(filename) != null) {
				showError("error.metadata.not.saved");
				return;
			}
			
			VFSSuccess rename = vfsItem.rename(filename);
			if (VFSSuccess.SUCCESS != rename) {
				showError("error.metadata.not.saved");
				return;
			}
		}
	}

}
