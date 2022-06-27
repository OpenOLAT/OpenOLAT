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

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * First page of video preferences
 * 
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoMetaDataEditFormController extends FormBasicController {
	
	private final RepositoryEntry repoEntry;
	private final VideoMeta videoMetadata;

	@Autowired
	private VideoManager videoManager;

	public VideoMetaDataEditFormController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, VideoMeta videoMetadata) {
		super(ureq, wControl);
		this.repoEntry = repoEntry;
		this.videoMetadata = videoMetadata;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.video.metaDataConfig");
		setFormContextHelp("manual_user/authoring/Set_up_info_page/");
		
		OLATResource videoResource = repoEntry.getOlatResource();
		if(StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
			uifactory.addStaticTextElement("video.config.url", videoMetadata.getUrl(), formLayout);
		}

		uifactory.addStaticTextElement("video.config.duration", repoEntry.getExpenditureOfWork(), formLayout);

		uifactory.addStaticTextElement("video.config.width", String.valueOf(videoMetadata.getWidth()) + "px", formLayout);
		uifactory.addStaticTextElement("video.config.height", String.valueOf(videoMetadata.getHeight()) + "px", formLayout);

		String aspcectRatio = videoManager.getAspectRatio(videoMetadata.getWidth(), videoMetadata.getHeight());
		uifactory.addStaticTextElement("video.config.ratio", aspcectRatio, formLayout);

		uifactory.addStaticTextElement("video.config.creationDate", StringHelper.formatLocaleDateTime(videoResource.getCreationDate().getTime(), getLocale()), formLayout);
		
		long size;
		if(StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
			size = videoMetadata.getSize();
		} else {
			File videoFile = videoManager.getVideoFile(videoResource);
			if(videoFile != null) {
				size = videoFile.length();
			} else {
				size = videoMetadata.getSize();
			}
		}
		if(size > 0) {
			uifactory.addStaticTextElement("video.config.fileSize", Formatter.formatBytes(size), formLayout);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

}
