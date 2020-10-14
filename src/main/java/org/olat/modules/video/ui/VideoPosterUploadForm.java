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

import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.resource.OLATResource;

/**
 * Videoposter upload form for create a 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoPosterUploadForm extends FormBasicController {
	private OLATResource videoResource;
	private long remainingSpace;
	private VFSContainer metaDataFolder;
	private FileElement posterField;

	private static final int picUploadlimitKB = 51200;


	private static final Set<String> imageMimeTypes = new HashSet<>();
	static {
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
	}

	public VideoPosterUploadForm(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		remainingSpace = Quota.UNLIMITED;
		VFSContainer videoResourceFileroot = new LocalFolderImpl(FileResourceManager.getInstance().getFileResourceRootImpl(videoResource).getBasefile());
		metaDataFolder = VFSManager.getOrCreateContainer(videoResourceFileroot, "media");

		posterField = uifactory.addFileElement(getWindowControl(), "poster", "video.config.poster", formLayout);
		posterField.limitToMimeType(imageMimeTypes, "poster.error.filetype", null);
		posterField.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		posterField.setPreview(ureq.getUserSession(), true);
		posterField.addActionListener(FormEvent.ONCHANGE);
		posterField.setHelpTextKey("poster.help", null);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		buttonGroupLayout.setElementCssClass("o_sel_upload_buttons");
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("track.upload", buttonGroupLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (posterField.isUploadSuccess()) {
			if (remainingSpace != -1) {
				if (posterField.getUploadFile().length() / 1024 > remainingSpace) {
					posterField.setErrorKey("QuotaExceeded", null);
					FileUtils.deleteFile(posterField.getUploadFile());
				}
			} else {
				fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, posterField.moveUploadFileTo(metaDataFolder)));
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
}