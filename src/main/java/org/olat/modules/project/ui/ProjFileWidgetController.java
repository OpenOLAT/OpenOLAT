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

import java.util.Date;
import java.util.List;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectStatus;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProjFileWidgetController extends ProjFileListController {
	
	private static final Integer NUM_LAST_MODIFIED = 6;
	
	private FormLink titleLink;
	private FileElement uploadEl;
	private FormLink createLink;
	private FormLink recordVideoLink;
	private FormLink recordAudioLink;
	private FormLink showAllLink;

	@Autowired
	private AVModule avModule;

	public ProjFileWidgetController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory,
			ProjProject project, ProjProjectSecurityCallback secCallback, Date lastVisitDate,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, "file_widget", bcFactory, project, secCallback, lastVisitDate, avatarMapperKey);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleLink = uifactory.addFormLink("file.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		
		String url = bcFactory.getFilesUrl(project);
		titleLink.setUrl(url);

		uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file.upload", null, formLayout);
		uploadEl.addActionListener(FormEvent.ONCHANGE);
		uploadEl.setElementCssClass("btn btn-default o_button_ghost");
		uploadEl.setMultiFileUpload(false);
		uploadEl.setDragAndDropForm(true);
		uploadEl.setChooseButtonLabel("none");
		uploadEl.setVisible(secCallback.canCreateFiles());

		if (secCallback.canCreateFiles() && avModule.isRecordingEnabled()) {
			DropdownItem createDropdown = uifactory.addDropdownMenu("file.create.dropdown", null,
					null, formLayout, getTranslator());
			createDropdown.setCarretIconCSS("o_icon o_icon_lg o_icon_add");
			createDropdown.setAriaLabel(translate("file.widget.commands.add"));
			createDropdown.setOrientation(DropdownOrientation.right);
			createDropdown.setButton(false);
			createDropdown.setGhost(true);
			createDropdown.setEmbbeded(true);

			createLink = uifactory.addFormLink("file.create", formLayout, Link.LINK);
			createLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			createLink.setTitle("file.create");
			createDropdown.addElement(createLink);

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
		} else {
			createLink = uifactory.addFormLink("file.create", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
			createLink.setIconLeftCSS("o_icon o_icon_add");
			createLink.setTitle(translate("file.create"));
			createLink.setGhost(true);
			createLink.setVisible(secCallback.canCreateFiles());
		}

		showAllLink = uifactory.addFormLink("file.show.all", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		showAllLink.setUrl(url);
		
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == uploadEl) {
			doUploadFile(ureq, uploadEl);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == createLink) {
			doCreateFile(ureq);
		} else if (source == recordVideoLink) {
			doRecordVideo(ureq);
		} else if (source == recordAudioLink) {
			doRecordAudio(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean isFullTable() {
		return false;
	}

	@Override
	protected Integer getNumLastModified() {
		return NUM_LAST_MODIFIED;
	}

	@Override
	protected void onModelLoaded() {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		long count = projectService.getFilesCount(searchParams);
		
		showAllLink.setI18nKey(translate("file.show.all", String.valueOf(count)));
		showAllLink.setVisible(count > 0);
	}

}
