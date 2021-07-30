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
package org.olat.modules.video.ui;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.ui.EditLicenseTypeController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoTranscoding;

/**
 * Initial date: 01.07.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class VideoLicenseInterceptionController extends FormBasicController {

	private License license;
	private VFSLeaf masterFile;
	private VideoTranscoding transcoding;
	
	public VideoLicenseInterceptionController(UserRequest ureq, WindowControl wControl, License license, VFSLeaf masterFile, VideoTranscoding transcoding) {
		super(ureq, wControl, "video_license_interception");
		
		setTranslator(Util.createPackageTranslator(EditLicenseTypeController.class, getLocale(), getTranslator()));
		
		this.license = license;
		this.masterFile = masterFile;
		this.transcoding = transcoding;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer form = (FormLayoutContainer) formLayout;
		
		if (license.getLicenseType() != null) {
			if (StringHelper.containsNonWhitespace(license.getLicenseType().getCssClass())) {
				form.contextPut("licenseIcon", license.getLicenseType().getCssClass());
			}
			
			if (StringHelper.containsNonWhitespace(license.getLicenseType().getName())) {
				form.contextPut("licenseType", license.getLicenseType().getName());
			}
			
			if (StringHelper.containsNonWhitespace(license.getFreetext())) {
				form.contextPut("licenseText", license.getFreetext());
			} else {
				form.contextPut("licenseText", Formatter.formatURLsAsLinks(license.getLicenseType().getText(), true));
			}
			
			if (StringHelper.containsNonWhitespace(license.getLicensor())) {
				form.contextPut("licensor", license.getLicensor());
			}
			
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonLayout.setRootForm(mainForm);
			formLayout.add(buttonLayout);
			
			uifactory.addFormSubmitButton("submit", "accept.and.download", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new VideoLicenseAcceptEvent(VideoLicenseAcceptEvent.ACCEPT_LICENSE, masterFile, transcoding));
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, new VideoLicenseAcceptEvent(VideoLicenseAcceptEvent.DENY_LICENSE, masterFile, transcoding));
	}

	@Override
	protected void doDispose() {
		// Nothing to dispose
	}

}
