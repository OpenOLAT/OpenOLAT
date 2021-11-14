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
package org.olat.modules.bigbluebutton.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SlideUploadController extends FormBasicController {

	private FileElement fileEl;
	
	private BigBlueButtonMeeting meeting;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;

	public SlideUploadController(UserRequest ureq, WindowControl wControl, BigBlueButtonMeeting meeting) {
		super(ureq, wControl);
		this.meeting = meeting;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_form");

		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "meeting.slides", formLayout);
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.limitToMimeType(BigBlueButtonModule.SLIDES_MIME_TYPES, "error.slides.type", null);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		List<ValidationStatus> validationResults = new ArrayList<>();
		fileEl.validate(validationResults);
		if(validationResults.isEmpty()) {
			Integer maxSizeInMb = bigBlueButtonModule.getMaxUploadSize();
			if(maxSizeInMb != null && maxSizeInMb.intValue() > 0) {
				long total = fileEl.getUploadSize();
				VFSContainer slidesContainer = bigBlueButtonManager.getSlidesContainer(meeting);
				List<VFSItem>  documents = slidesContainer.getItems(new VFSSystemItemFilter());
				for(VFSItem doc:documents) {
					if(doc instanceof VFSLeaf) {
						total += ((VFSLeaf)doc).getSize();
					}
				}
				
				if(total > (maxSizeInMb.intValue() * 1000 * 1000)) {
					fileEl.setErrorKey("error.slides.size", new String[] { maxSizeInMb.toString() });
					allOk &= false;
				}
			}
		} else {
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		VFSContainer slidesContainer = bigBlueButtonManager.getSlidesContainer(meeting);
		fileEl.moveUploadFileTo(slidesContainer);
		meeting = bigBlueButtonManager.updateMeeting(meeting);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}