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
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * The Class VideoResourceEditController. 
 * this class replaces an an existing video resource with another,
 * deletes existing transcodings and recreates them considering the new resolution
 * 
 * @autor fkiefer fabian.kiefer@frentix.com
 */
public class VideoResourceEditController extends FormBasicController {

	private static final Set<String> videoMimeTypes = Set.of("video/quicktime", "video/mp4");
	private static final String VIDEO_RESOURCE = "video.mp4";
	
	private VideoMeta meta;
	private VFSContainer vfsContainer;
	private OLATResource videoResource;
	private RepositoryEntry entry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;
	@Autowired
	private MovieService movieService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	private TextElement urlEl;
	private StaticTextElement typeEl;
	private FileElement uploadFileEl;
	
	public VideoResourceEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry){
		super(ureq, wControl);
		this.entry = entry;
		this.videoResource = entry.getOlatResource();
		vfsContainer = videoManager.getMasterContainer(videoResource);
		meta = videoManager.getVideoMetadata(videoResource);

		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.video.exchange");
		setFormDescription("video.replace.desc");
		setFormContextHelp("ok");
		
		if(StringHelper.containsNonWhitespace(meta.getUrl())) {
			urlEl = uifactory.addTextElement("video.config.url", 512, meta.getUrl(), formLayout);
		} else {
			uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", "video.replace.upload", formLayout);
			uploadFileEl.addActionListener(FormEvent.ONCHANGE);
			uploadFileEl.limitToMimeType(videoMimeTypes, "video.mime.type.error", null);
		}
		typeEl = uifactory.addStaticTextElement("video.mime.type", "video.mime.type", "", formLayout);
		typeEl.setVisible(false);
	
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", "tab.video.exchange", buttonGroupLayout);
	}
	
	private void doReplaceURLAndUpdateMetadata() {
		String url = urlEl.getValue();
		VideoFormat format = VideoFormat.valueOfUrl(url);
		if(format == null) {
			return;// cannot understand the URL
		}
		if(format == VideoFormat.panopto) {
			url = videoManager.toPodcastVideoUrl(url);
		}

		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(videoResource, true);
		videoManager.updateVideoMetadata(repoEntry, url, format, getIdentity());
		dbInstance.commit();
		meta = videoManager.getVideoMetadata(videoResource);
	}

	private int doReplaceFileAndUpdateMetadata() {
		VFSLeaf video = (VFSLeaf) vfsContainer.resolve(VIDEO_RESOURCE);		
		File uploadFile = uploadFileEl.getUploadFile();
		meta = videoManager.getVideoMetadata(videoResource);
		if (uploadFileEl.getUploadSize() > 0 && uploadFile.exists()) {
			video.delete();
			VFSLeaf uploadVideo = vfsContainer.createChildLeaf(VIDEO_RESOURCE);
			VFSManager.copyContent(uploadFile, uploadVideo, getIdentity());
			//update video dimensions
			Size dimensions = movieService.getSize(uploadVideo, VideoManagerImpl.FILETYPE_MP4);
			// update video duration
			long duration = movieService.getDuration(uploadVideo, VideoTranscoding.FORMAT_MP4);
			// exchange poster
			videoManager.exchangePoster(videoResource, getIdentity());

			meta.setSize(uploadFile.length());
			meta.setVideoFormat(VideoFormat.valueOfFilename(uploadVideo.getName()));
			String length = null;
			if (duration != -1) {
				length = Formatter.formatTimecode(duration);
				meta.setLength(length);
			}
			if(dimensions != null && dimensions.getWidth() > 0) {
				meta.setWidth(dimensions.getWidth());
				meta.setHeight(dimensions.getHeight());
			}
			meta = videoManager.updateVideoMetadata(meta);
			if(length != null) {
				repositoryManager.setExpenditureOfWork(entry, length);
			}
		} 
		return meta.getHeight();
	}

	private void queueDeleteTranscoding() {
		List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoResource);
		for (VideoTranscoding videoTranscoding : videoTranscodings) {
			videoManager.deleteVideoTranscoding(videoTranscoding);
		}
	}
	
	private void queueCreateTranscoding(int height) {
		List<Integer> missingResolutions = videoManager.getMissingTranscodings(videoResource);

		if (videoModule.isTranscodingEnabled()) {
			// 1) setup transcoding job for original file size
			videoManager.createTranscoding(videoResource, height, VideoTranscoding.FORMAT_MP4);
			// 2) setup transcoding jobs for all configured sizes below the original size
			for (Integer missingRes : missingResolutions) {
				if(height > missingRes){
					videoManager.createTranscoding(videoResource, missingRes, VideoTranscoding.FORMAT_MP4);					
				}
			}
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(urlEl != null) {
			urlEl.clearError();
			if(!StringHelper.containsNonWhitespace(urlEl.getValue())) {
				urlEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(VideoFormat.valueOfUrl(urlEl.getValue()) == null) {
				urlEl.setErrorKey("error.format.not.supported", null);
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(urlEl != null) {
			doReplaceURLAndUpdateMetadata();
		} else if (uploadFileEl != null && uploadFileEl.getUploadFile() != null && uploadFileEl.isUploadSuccess()) {
			queueDeleteTranscoding();
			int height = doReplaceFileAndUpdateMetadata();
			queueCreateTranscoding(height);
			typeEl.setValue(translate("admin.menu.title"));
			typeEl.setVisible(true);
			showInfo("video.replaced");
		} else {
			typeEl.setVisible(false);
			showWarning("video.not.replaced");
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
