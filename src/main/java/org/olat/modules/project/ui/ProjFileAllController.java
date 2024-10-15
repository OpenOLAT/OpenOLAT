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
package org.olat.modules.project.ui;

import java.util.Collections;
import java.util.Date;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ui.component.ProjAvatarComponent;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProjFileAllController extends ProjFileListController {

	private FileElement uploadEl;
	private FormLink createLink;
	private FormLink recordVideoLink;
	private FormLink recordAudioLink;
	private FormLink downloadAllLink;
	private FormLink addBrowserLink;

	private final String avatarUrl;

	@Autowired
	private AVModule avModule;
	
	public ProjFileAllController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, "file_all", bcFactory, project, secCallback, lastVisitDate, avatarMapperKey);
		ProjProjectImageMapper projectImageMapper = new ProjProjectImageMapper(projectService);
		String projectMapperUrl = registerCacheableMapper(ureq, ProjProjectImageMapper.DEFAULT_ID, projectImageMapper,
				ProjProjectImageMapper.DEFAULT_EXPIRATION_TIME);
		this.avatarUrl = projectImageMapper.getImageUrl(projectMapperUrl, project, ProjProjectImageType.avatar);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("avatar", new ComponentWrapperElement(new ProjAvatarComponent("avatar", project, avatarUrl, Size.medium, false)));


		uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file.upload", null, formLayout);
		uploadEl.addActionListener(FormEvent.ONCHANGE);
		uploadEl.setMultiFileUpload(false);
		uploadEl.setChooseButtonLabel(translate("file.upload"));
		uploadEl.setDragAndDropForm(true);

		DropdownItem uploadDropdown = uifactory.addDropdownMenu("upload.dropdown", null, null, formLayout, getTranslator());
		uploadDropdown.setOrientation(DropdownOrientation.right);

		addBrowserLink = uifactory.addFormLink("browser.add", formLayout, Link.LINK);
		addBrowserLink.setIconLeftCSS("o_icon o_icon-fw o_icon_filehub_add");
		addBrowserLink.setElementCssClass("o_sel_folder_add_browser");
		uploadDropdown.addElement(addBrowserLink);

		uploadDropdown.addElement(new Dropdown.SpacerItem("createSpace"));

		
		createLink = uifactory.addFormLink("file.create", formLayout, Link.LINK);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setVisible(secCallback.canCreateFiles());
		uploadDropdown.addElement(createLink);
		
		if (secCallback.canCreateFiles() && avModule.isRecordingEnabled()) {
			uploadDropdown.addElement(new Dropdown.SpacerItem("createSpace"));

			if (avModule.isVideoRecordingEnabled()) {
				recordVideoLink = uifactory.addFormLink("record.video", formLayout, Link.LINK);
				recordVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
				recordVideoLink.setTitle("record.video");
				uploadDropdown.addElement(recordVideoLink);
			}
			if (avModule.isAudioRecordingEnabled()) {
				recordAudioLink = uifactory.addFormLink("record.audio", formLayout, Link.LINK);
				recordAudioLink.setIconLeftCSS("o_icon o_icon-fw o_icon_audio_record");
				recordAudioLink.setTitle("record.audio");
				uploadDropdown.addElement(recordAudioLink);
			}
		}

		DropdownItem dropdown = uifactory.addDropdownMenuMore("cmds", flc, getTranslator());
		
		downloadAllLink = uifactory.addFormLink("file.download.all", formLayout, Link.LINK);
		downloadAllLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		dropdown.addElement(downloadAllLink);
		
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadEl) {
			doUploadFile(ureq, uploadEl, Collections.emptyList());
			selectFilterTab(ureq, tabAll);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == createLink) {
			doCreateFile(ureq);
		} else if (source == downloadAllLink) {
			doDownloadAll(ureq);
		} else if (source == recordVideoLink) {
			doRecordVideo(ureq);
		} else if (source == recordAudioLink) {
			doRecordAudio(ureq);
		} else if (source == addBrowserLink) {
			doAddFromBrowser(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean isFullTable() {
		return true;
	}
	
	@Override
	protected Integer getNumLastModified() {
		return null;
	}

	@Override
	protected void onModelLoaded() {
		//
	}

}
