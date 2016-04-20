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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.resource.OLATResource;

/**
 * Uploadform to uplaod track files and save the corresponding language
 * 
 * Initial date: 01.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */

public class VideoTrackUploadForm extends FormBasicController {
	private OLATResource videoResource;
	private FileElement fileEl;
	SingleSelection langsItem;
	long remainingSpace;
	private VFSContainer videoResourceFileroot;
	private VFSContainer metaDataFolder;

	private static final Set<String> trackMimeTypes = new HashSet<String>();
	static {
		trackMimeTypes.add("text/plain");
	}

	public VideoTrackUploadForm(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		remainingSpace = Quota.UNLIMITED;
		videoResourceFileroot = new LocalFolderImpl(FileResourceManager.getInstance().getFileResourceRootImpl(videoResource).getBasefile());
		metaDataFolder = VFSManager.getOrCreateContainer(videoResourceFileroot, "media");
		List<String> langs = new ArrayList<String>();
		List<String> dispLangs = new ArrayList<String>();


		for(Locale locale : SimpleDateFormat.getAvailableLocales()){
			if(locale.hashCode()!=0){
				langs.add(locale.getLanguage());
				dispLangs.add(locale.getDisplayLanguage(getTranslator().getLocale()));
			}
		}

		List<String> langsWithoutDup = langs.parallelStream().distinct().collect(Collectors.toList());
		List<String> dispLangsWithoutDup = dispLangs.parallelStream().distinct().collect(Collectors.toList());

		langsItem = uifactory.addDropdownSingleselect("track.langs", formLayout, langsWithoutDup.toArray(new String[langsWithoutDup.size()]), dispLangsWithoutDup.toArray(new String[dispLangsWithoutDup.size()]), null);
		fileEl = uifactory.addFileElement(getWindowControl(), "track.upload", formLayout);
//		fileEl.limitToMimeType(trackMimeTypes, "video.config.track.error.type", null);
		langsItem.setMandatory(true);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		buttonGroupLayout.setElementCssClass("o_sel_upload_buttons");
		uifactory.addFormSubmitButton("track.upload", buttonGroupLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if ( fileEl.isUploadSuccess()) {
			if (remainingSpace != -1) {
				if (fileEl.getUploadFile().length() / 1024 > remainingSpace) {
					fileEl.setErrorKey("QuotaExceeded", null);
					fileEl.getUploadFile().delete();
					return;
				}
			}else{
				fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, fileEl.moveUploadFileTo(metaDataFolder)));
			}
		}else{
			fileEl.setErrorKey("track.upload.error.nofile", null);
		}

	}


	protected String getLang(){
		return langsItem.getSelectedKey();
	}


	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}
}