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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.manager.VideoManagerImpl;
import org.olat.modules.video.manager.VideoSubtitlesHelper;
import org.olat.modules.video.ui.event.TrackUploadEvent;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Uploadform to uplaod track files and save the corresponding language
 * 
 * Initial date: 01.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */

public class VideoTrackUploadForm extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(VideoTrackUploadForm.class);
	private FileElement fileEl;
	private SingleSelection langsItem;
	private VFSContainer mediaContainer;
	
	@Autowired
	private VideoManager videoManager;

	public VideoTrackUploadForm(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		mediaContainer = videoManager.getMasterContainer(videoResource);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues languageKV = new SelectionValues();
		Arrays.stream(DateFormat.getAvailableLocales())
				.filter(locale -> locale.hashCode() != 0)
				.distinct()
				.forEach(locale -> languageKV.add(entry(
						locale.getLanguage(), 
						locale.getDisplayLanguage(getTranslator().getLocale())
						)));
		languageKV.sort(SelectionValues.VALUE_ASC);
		langsItem = uifactory.addDropdownSingleselect("track.langs", formLayout, languageKV.keys(), languageKV.values(), null);
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "track.upload", formLayout);
		langsItem.setMandatory(true);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		buttonGroupLayout.setElementCssClass("o_sel_upload_buttons");
		
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("track.upload", buttonGroupLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if (fileEl.isUploadSuccess()) {
			String filename = fileEl.getUploadFileName();
			String suffix = FileUtils.getFileSuffix(filename);
			if (!VideoManager.FILETYPE_SRT.equals(suffix) && !VideoManager.FILETYPE_VTT.equals(suffix)) {
				fileEl.setErrorKey("track.upload.error.filetype", null);
				allOk &= false;
			} else if (!VideoSubtitlesHelper.isVtt(fileEl.getUploadFile()) &&
					!VideoSubtitlesHelper.isConvertibleSrt(fileEl.getUploadFile())) {
				fileEl.setErrorKey("track.upload.error.filetype", null);
				allOk &= false;
			}
		} else {
			fileEl.setErrorKey("track.upload.error.nofile", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (fileEl.isUploadSuccess()) {
			String newTrackName = VideoManagerImpl.TRACK + langsItem.getSelectedKey() + VideoManager.DOT + VideoManager.FILETYPE_VTT;
			if (VideoSubtitlesHelper.isVtt(fileEl.getUploadFile())) {
				fileEl.setUploadFileName(newTrackName);
				VFSItem oldFile = mediaContainer.resolve(newTrackName);
				if (oldFile instanceof VFSLeaf && oldFile.exists()) {
					oldFile.deleteSilently();
				}
				VFSLeaf track = fileEl.moveUploadFileTo(mediaContainer);
				fireEvent(ureq, new TrackUploadEvent(langsItem.getSelectedKey(), track));
			} else if (VideoSubtitlesHelper.isConvertibleSrt(fileEl.getUploadFile())) {
				log.info("User upload of a non-VTT file that is convertible to VTT");
				VFSItem existingItem = mediaContainer.resolve(newTrackName);
				if (existingItem != null) {
					existingItem.deleteSilently();
				}
				VFSLeaf newTrack = mediaContainer.createChildLeaf(newTrackName);
				try (FileInputStream fileInputStream = new FileInputStream(fileEl.getUploadFile())) {
					VideoSubtitlesHelper.convertSrtToVtt(fileInputStream, newTrack.getOutputStream(false));
					CoreSpringFactory.getImpl(VFSRepositoryService.class).itemSaved(newTrack, getIdentity());
					fireEvent(ureq, new TrackUploadEvent(langsItem.getSelectedKey(), newTrack));
				} catch (Exception e) {
					log.error("Cannot convert non-VTT to VTT", e);
					getWindowControl().setWarning(translate("track.upload.error.filetype"));
				}
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}