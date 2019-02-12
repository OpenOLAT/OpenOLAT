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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
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
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * The Class VideoResourceEditController. 
 * @autor fkiefer fabian.kiefer@frentix.com
 * this class replaces an an existing video resource with another,
 * deletes existing transcodings and recreates them considering the new resolution
 */
public class VideoResourceEditController extends FormBasicController {

	private static final Set<String> videoMimeTypes = new HashSet<>();
	static {
		videoMimeTypes.add("video/quicktime");
		videoMimeTypes.add("video/mp4");
	}
	private static final String VIDEO_RESOURCE = "video.mp4";
	
	private VFSContainer vfsContainer;
	private OLATResource videoResource;
	private RepositoryEntry entry;
	
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;
	@Autowired
	private MovieService movieService;
	
	private StaticTextElement typeEl;
	private FileElement uploadFileEl;

	
	public VideoResourceEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry){
		super(ureq, wControl);
		this.entry = entry;
		this.videoResource = entry.getOlatResource();
		vfsContainer = videoManager.getMasterContainer(videoResource);

		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.video.exchange");
		setFormDescription("video.replace.desc");
		setFormContextHelp("ok");
		
		uploadFileEl = uifactory.addFileElement(getWindowControl(), "upload", "video.replace.upload", formLayout);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);
		uploadFileEl.limitToMimeType(videoMimeTypes, "video.mime.type.error", null);
		
		typeEl = uifactory.addStaticTextElement("video.mime.type", "video.mime.type", "", formLayout);
		typeEl.setVisible(false);		
	
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", "tab.video.exchange", buttonGroupLayout);

	}

	private int doReplaceFileAndUpdateMetadata() {
		VFSLeaf video = (VFSLeaf) vfsContainer.resolve(VIDEO_RESOURCE);		
		File uploadFile = uploadFileEl.getUploadFile();
		VideoMeta meta = videoManager.getVideoMetadata(videoResource);
		if (uploadFileEl.getUploadSize() > 0 && uploadFile.exists()){
			video.delete();
			VFSLeaf uploadVideo = vfsContainer.createChildLeaf(VIDEO_RESOURCE);
			VFSManager.copyContent(uploadFile, uploadVideo);
			//update video dimensions
			Size dimensions = movieService.getSize(uploadVideo, VideoManagerImpl.FILETYPE_MP4);
			// update video duration
			long duration = movieService.getDuration(uploadVideo, VideoTranscoding.FORMAT_MP4);
			// exchange poster
			videoManager.exchangePoster(videoResource);
			if (duration != -1) {
				String length = Formatter.formatTimecode(duration);
				entry.setExpenditureOfWork(length);
				meta.setSize(uploadFile.length());
				meta.setWidth(dimensions.getWidth());
				meta.setHeight(dimensions.getHeight());
				meta.setVideoFormat(VideoFormat.valueOfFilename(uploadVideo.getName()));
				meta.setLength(length);
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
	protected void formOK(UserRequest ureq) {
		if (uploadFileEl.getUploadFile() != null && uploadFileEl.isUploadSuccess()) {
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
	}
}
