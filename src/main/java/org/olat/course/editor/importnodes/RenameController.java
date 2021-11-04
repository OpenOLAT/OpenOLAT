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
package org.olat.course.editor.importnodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;

/**
 * 
 * Initial date: 12 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RenameController extends FormBasicController {
	
	private TextElement filenameEl;
	private final ConfigurationFileRow fileRow;
	private final VFSContainer targetContainer;
	private final VFSContainer sourceContainer;

	public RenameController(UserRequest ureq, WindowControl wControl, ConfigurationFileRow fileRow,
			VFSContainer sourceContainer, VFSContainer targetContainer) {
		super(ureq, wControl);
		this.fileRow = fileRow;
		this.sourceContainer = sourceContainer;
		this.targetContainer = targetContainer;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String filename = fileRow.getItem().getName();
		if(fileRow.getRenamedFilename() != null) {
			filename = fileRow.getRenamedFilename();
		}
		filenameEl = uifactory.addTextElement("new.filename", "new.filename", 128, filename, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("rename", buttonsCont);
	}
	
	public ConfigurationFileRow getFileRow() {
		return fileRow;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		filenameEl.clearError();
		if(!StringHelper.containsNonWhitespace(filenameEl.getValue())) {
			filenameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			VFSContainer parent = fileRow.getItem().getParentContainer();
			String newRelPath = VFSManager.getRelativeItemPath(parent, sourceContainer, "/");
			newRelPath = newRelPath + "/" + filenameEl.getValue();
			if(targetContainer.resolve(newRelPath) != null) {
				filenameEl.setErrorKey("error.file.exists", null);
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fileRow.setRenamedFilename(filenameEl.getValue());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
