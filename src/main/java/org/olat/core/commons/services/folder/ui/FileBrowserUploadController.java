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

import java.util.List;

import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 19 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserUploadController extends FormBasicController {

	private FileElement fileEl;

	private final FileBrowserSelectionMode selectionMode;
	private final FolderQuota folderQuota;
	private final String submitButtonText;

	public FileBrowserUploadController(UserRequest ureq, WindowControl wControl,
			FileBrowserSelectionMode selectionMode, FolderQuota folderQuota, String submitButtonText) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.selectionMode = selectionMode;
		this.folderQuota = folderQuota;
		this.submitButtonText = submitButtonText;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", null, formLayout);
		fileEl.setMaxUploadSizeKB(folderQuota.getUploadLimitKB(), null, null);
		if (FileBrowserSelectionMode.sourceMulti == selectionMode) {
			fileEl.setMultiFileUpload(true);
			fileEl.setMaxNumberOfFiles(10, null);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("submit", "submit", "noTransOnlyParam", new String[] {submitButtonText}, buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<VFSItem> items = fileEl.getUploadFilesInfos().stream()
				.map(infos -> (VFSItem)new NamedLeaf(infos.fileName(), new LocalFileImpl(infos.file())))
				.toList();
		fireEvent(ureq, new FileBrowserSelectionEvent(items, fileEl));
	}

}
