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
package org.olat.modules.project.ui;

import java.util.Date;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
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
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileAllController extends ProjFileListController {
	
	private FormLink uploadLink;
	private FormLink createLink;
	private FormLink recordVideoLink;
	private FormLink recordAudioLink;
	private FormLink downlaodAllLink;
	
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
		
		uploadLink = uifactory.addFormLink("file.upload", formLayout, Link.BUTTON);
		uploadLink.setIconLeftCSS("o_icon o_icon_upload");
		uploadLink.setVisible(secCallback.canCreateFiles());
		
		createLink = uifactory.addFormLink("file.create", formLayout, Link.BUTTON);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setVisible(secCallback.canCreateFiles());
		
		if (secCallback.canCreateFiles() && avModule.isRecordingEnabled()) {
			DropdownItem createDropdown = uifactory.addDropdownMenu("file.create.dropdown", null,
					null, formLayout, getTranslator());
			createDropdown.setOrientation(DropdownOrientation.right);

			if (avModule.isVideoRecordingEnabled()) {
				recordVideoLink = uifactory.addFormLink("record.video", formLayout, Link.LINK);
				recordVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
				recordVideoLink.setTitle("record.video");
				createDropdown.addElement(recordVideoLink);
			}
			if (avModule.isAudioRecordingEnabled()) {
				recordAudioLink = uifactory.addFormLink("record.audio", formLayout, Link.LINK);
				recordAudioLink.setIconLeftCSS("o_icon o_icon-fw o_icon_audio_record");
				recordAudioLink.setTitle("record.audio");
				createDropdown.addElement(recordAudioLink);
			}
		}

		DropdownItem dropdown = uifactory.addDropdownMenu("cmds", null, null, flc, getTranslator());
		dropdown.setCarretIconCSS("o_icon o_icon_commands");
		dropdown.setOrientation(DropdownOrientation.right);
		
		downlaodAllLink = uifactory.addFormLink("file.download.all", formLayout, Link.LINK);
		downlaodAllLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		dropdown.addElement(downlaodAllLink);
		
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadLink) {
			doUploadFile(ureq);
		} else if (source == createLink) {
			doCreateFile(ureq);
		} else if (source == downlaodAllLink) {
			doDownloadAll(ureq);
		} else if (source == recordVideoLink) {
			doRecordVideo(ureq);
		} else if (source == recordAudioLink) {
			doRecordAudio(ureq);
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
