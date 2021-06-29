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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.ui.marker.VideoMarkerEditController;
import org.olat.modules.video.ui.question.VideoQuestionEditController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.olat.repository.ui.settings.RepositoryEntryInfoController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 28.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class VideoSettingsController extends RepositoryEntrySettingsController {
	
	private RepositoryEntry entry;
	private VideoMeta videoMetadata;

	private RepositoryEntryInfoController infoCtrl;
	private VideoMetaDataWrapperController videoMetadataController;
	private VideoPosterEditController posterEditController;
	private VideoChapterEditController chapterEditController;
	private VideoTrackEditController trackEditController;
	private VideoQualityTableFormController qualityEditController;
	private VideoMarkerEditController markerEditController;
	private VideoQuestionEditController questionEditController;
	private VideoDownloadSettingsController downloadSettingsController;

	private Link infoLink;
	private Link metaDataLink;
	private Link posterEditLink;
	private Link chapterEditLink;
	private Link trackEditLink;
	private Link markerEditLink;
	private Link qualityConfigLink;
	private Link questionEditLink;
	private Link downloadConfigLink;
	
	@Autowired
	private VideoManager videoManager;
	
	public VideoSettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
		
		this.entry = entry;
		videoMetadata = videoManager.getVideoMetadata(entry.getOlatResource());		
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenInfos(ureq);
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Info".equalsIgnoreCase(type)) {
				doOpenInfos(ureq);
			} else if("Metadata".equalsIgnoreCase(type)) {
				doOpenMetadata(ureq);
			} else if("Poster".equalsIgnoreCase(type)) {
				doOpenPosterConfig(ureq);
			} else if("Chapters".equalsIgnoreCase(type)) {
				doOpenChapters(ureq);
			} else if("Comments".equalsIgnoreCase(type)) {
				doOpenComments(ureq);
			} else if("Quiz".equalsIgnoreCase(type)) {
				doOpenQuestions(ureq);
			} else if("Subtitles".equalsIgnoreCase(type)) {
				doOpenSubtitles(ureq);
			} else if("Qualities".equalsIgnoreCase(type)) {
				doOpenQualities(ureq);
			} else if("Download".equalsIgnoreCase(type)) {
				doOpenDownload(ureq);
			} else {
				super.activate(ureq, entries, state);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(infoLink == source) {
			cleanUp();
			doOpenInfos(ureq);
		} else if(metaDataLink == source) {
			cleanUp();
			doOpenMetadata(ureq);
		} else if(posterEditLink == source) {
			cleanUp();
			doOpenPosterConfig(ureq);
		} else if(chapterEditLink == source) {
			cleanUp();
			doOpenChapters(ureq);
		} else if(trackEditLink == source) {
			doOpenSubtitles(ureq);
		} else if(markerEditLink == source) {
			doOpenComments(ureq);
		} else if(qualityConfigLink == source) {
			doOpenQualities(ureq);
		} else if(questionEditLink == source) {
			doOpenQuestions(ureq);
		} else if(downloadConfigLink == source) {
			doOpenDownload(ureq);
		}
		
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(downloadSettingsController);
		removeAsListenerAndDispose(videoMetadataController);
		removeAsListenerAndDispose(questionEditController);
		removeAsListenerAndDispose(qualityEditController);
		removeAsListenerAndDispose(markerEditController);
		removeAsListenerAndDispose(trackEditController);
		removeAsListenerAndDispose(chapterEditController);
		removeAsListenerAndDispose(posterEditController);
		removeAsListenerAndDispose(infoCtrl);
		
		downloadSettingsController = null;
		videoMetadataController = null;
		questionEditController = null;
		qualityEditController = null;
		markerEditController = null;
		trackEditController = null;
		chapterEditController = null;
		posterEditController = null;
		infoCtrl = null;
		
		super.cleanUp();
	}
	
	@Override
	protected void initSegments() {
		initInfos();
		initAccessAndBooking();
		initOptions();
		initPoster();
		initChapters();
		initComments();
		initQuiz();
		initSubtitles();
		initQualities();
		initDownload();
	}
	
	@Override
	protected void initInfos() {
		infoLink = LinkFactory.createLink("details.info", getTranslator(), this);
		infoLink.setElementCssClass("o_sel_infos");
		buttonsGroup.addButton(infoLink, false);
		
		metaDataLink = LinkFactory.createLink("details.metadata", getTranslator(), this);
		metaDataLink.setElementCssClass("o_sel_metadata");
		buttonsGroup.addButton(metaDataLink, false);
	}
	
	private void initPoster() {
		posterEditLink = LinkFactory.createLink("tab.video.posterConfig", getTranslator(), this);
		posterEditLink.setElementCssClass("o_sel_poster");
		buttonsGroup.addButton(posterEditLink, false);
	}
	
	private void initChapters() {
		chapterEditLink = LinkFactory.createLink("tab.video.chapterConfig", getTranslator(), this);
		chapterEditLink.setElementCssClass("o_sel_chapters");
		buttonsGroup.addButton(chapterEditLink, false);
	}
	
	private void initComments() {
		markerEditLink = LinkFactory.createLink("tab.video.markerConfig", getTranslator(), this);
		markerEditLink.setElementCssClass("o_sel_comments");
		buttonsGroup.addButton(markerEditLink, false);
	}
	
	private void initQuiz() {
		questionEditLink = LinkFactory.createLink("tab.video.questionConfig", getTranslator(), this);
		questionEditLink.setElementCssClass("o_sel_quiz");
		buttonsGroup.addButton(questionEditLink, false);
	}
	
	private void initSubtitles() {
		trackEditLink = LinkFactory.createLink("tab.video.trackConfig", getTranslator(), this);
		trackEditLink.setElementCssClass("o_sel_subtitles");
		buttonsGroup.addButton(trackEditLink, false);
	}
	
	private void initQualities() {
		if(!StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
			qualityConfigLink = LinkFactory.createLink("tab.video.qualityConfig", getTranslator(), this);
			qualityConfigLink.setElementCssClass("o_sel_qualities");
			buttonsGroup.addButton(qualityConfigLink, false);
		}
	}
	
	private void initDownload() {
		if (videoMetadata.getVideoFormat().equals(VideoFormat.mp4)) {
			downloadConfigLink = LinkFactory.createLink("tab.video.downloadConfig", getTranslator(), this);
			downloadConfigLink.setElementCssClass("o_sel_download");
			buttonsGroup.addButton(downloadConfigLink, false);
		}
	}
	
	@Override
	protected void doOpenInfos(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Info"), null);
		infoCtrl = new RepositoryEntryInfoController(ureq, swControl, entry, readOnly);
		listenTo(infoCtrl);
		mainPanel.setContent(infoCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(infoLink);
	}
	
	@Override
	protected void doOpenMetadata(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Metadata"), null);
		videoMetadataController = new VideoMetaDataWrapperController(ureq, swControl, entry, videoMetadata);
		listenTo(videoMetadataController);
		mainPanel.setContent(videoMetadataController.getInitialComponent());
		buttonsGroup.setSelectedButton(metaDataLink);
	}
	
	private void doOpenPosterConfig(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Poster"), null);
		posterEditController = new VideoPosterEditController(ureq, swControl, entry.getOlatResource());
		listenTo(posterEditController);
		mainPanel.setContent(posterEditController.getInitialComponent());
		buttonsGroup.setSelectedButton(posterEditLink);
	}
	
	private void doOpenChapters(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Chapters"), null);
		chapterEditController = new VideoChapterEditController(ureq, swControl, entry);
		listenTo(chapterEditController);
		mainPanel.setContent(chapterEditController.getInitialComponent());
		buttonsGroup.setSelectedButton(chapterEditLink);
	}
	
	private void doOpenSubtitles(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Subtitles"), null);
		trackEditController = new VideoTrackEditController(ureq, swControl, entry.getOlatResource());
		listenTo(trackEditController);
		mainPanel.setContent(trackEditController.getInitialComponent());
		buttonsGroup.setSelectedButton(trackEditLink);
	}
	
	private void doOpenComments(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Comments"), null);
		markerEditController = new VideoMarkerEditController(ureq, swControl, entry);
		listenTo(markerEditController);
		mainPanel.setContent(markerEditController.getInitialComponent());
		buttonsGroup.setSelectedButton(markerEditLink);
	}
	
	private void doOpenQualities(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Qualities"), null);
		qualityEditController = new VideoQualityTableFormController(ureq, swControl, entry);
		listenTo(qualityEditController);
		mainPanel.setContent(qualityEditController.getInitialComponent());
		buttonsGroup.setSelectedButton(qualityConfigLink);
	}	
	
	private void doOpenQuestions(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Quiz"), null);
		questionEditController = new VideoQuestionEditController(ureq, swControl, entry);
		listenTo(questionEditController);
		mainPanel.setContent(questionEditController.getInitialComponent());
		buttonsGroup.setSelectedButton(questionEditLink);
	}
	
	private void doOpenDownload(UserRequest ureq) {
		if (videoMetadata.getVideoFormat().equals(VideoFormat.mp4)) {
			entry = repositoryService.loadByKey(entry.getKey());
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Download"), null);
			downloadSettingsController = new VideoDownloadSettingsController(ureq, swControl, videoMetadata);
			listenTo(downloadSettingsController);
			mainPanel.setContent(downloadSettingsController.getInitialComponent());
			buttonsGroup.setSelectedButton(downloadConfigLink);
		} else {
			doOpenInfos(ureq);
		}
	}
}
