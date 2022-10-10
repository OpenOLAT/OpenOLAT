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
package org.olat.course.nodes.pf.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.modules.ModuleConfiguration;

/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFFileUploadController extends FormBasicController {

	private static final String INTENDING = "\u00a0"; // &nbsp; non-breaking space

	private String uploadFileName;
	private FileElement uploadFileEl;
	private SingleSelection fileDestinationEl;
	private File uploadFile;
	private final PFCourseNode pfNode;
	private final boolean uploadToAll;

	public PFFileUploadController(UserRequest ureq, WindowControl wControl, boolean uploadToall, PFCourseNode pfNode) {
		super(ureq, wControl);
		this.uploadToAll = uploadToall;
		this.pfNode = pfNode;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues templateFolderValues = new SelectionValues();
		List<String> folderElements = new ArrayList<>();

		ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();

		if (moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE) != null) {
			folderElements = new ArrayList<>(Arrays.asList(moduleConfiguration
					.get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString()
					.split(",")));
			folderElements.removeIf(String::isBlank);
			folderElements.removeIf(fel -> fel.contains(PFManager.FILENAME_DROPBOX));

			templateFolderValues.add(SelectionValues.entry(PFManager.FILENAME_RETURNBOX, translate(PFCourseNode.FOLDER_RETURN_BOX)));
		} else {
			moduleConfiguration.setStringValue(PFCourseNode.CONFIG_KEY_TEMPLATE, "");
			// Persists config
			fireEvent(ureq, Event.CHANGED_EVENT);
		}


		for (String folder : folderElements) {
			String normalizedElement =
					folder
							.replaceAll(".+?/", INTENDING + INTENDING + INTENDING + INTENDING);
			templateFolderValues.add(SelectionValues.entry(folder, normalizedElement));
		}

		templateFolderValues.sort(Comparator.comparing(SelectionValues.SelectionValue::getKey));

		fileDestinationEl = uifactory.addDropdownSingleselect(
				"pf.upload.destination",
				formLayout,
				templateFolderValues.keys(),
				templateFolderValues.values());

		uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", "textfield.upload", formLayout);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", "upload.link", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (uploadFileEl.isUploadSuccess()) {
			uploadFile = uploadFileEl.getUploadFile();
			uploadFileName = uploadFileEl.getUploadFileName();
			fireEvent(ureq, Event.DONE_EVENT);	
		}
	}
	
	protected File getUpLoadFile () {
		return uploadFile;
	}
	
	protected String getUploadFileName () {
		return uploadFileName;
	}

	protected SingleSelection getFileDestinationEl() {
		return fileDestinationEl;
	}

	protected boolean isUploadToAll () {
		return uploadToAll;
	}
}
