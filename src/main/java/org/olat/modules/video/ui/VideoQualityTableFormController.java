/**
 * OpenOLAT - Online Learning and Training</a><br>
 * <a href="http://www.openolat.org">
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.modules.video.ui.VideoQualityTableModel.QualityTableCols;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * table to show the different available transcoded video versions of a video resource 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoQualityTableFormController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private VideoQualityTableModel tableModel;
	private CloseableModalController cmc;
	private VelocityContainer previewVC;
	private OLATResource videoResource;
	private FormLink refreshbtn;

	private int count = 0;
	
	private static final Logger log = Tracing.createLoggerFor(VideoQualityTableFormController.class);

	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;

	public VideoQualityTableFormController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry) {
		super(ureq, wControl, "video_quality");
		this.videoResource = videoEntry.getOlatResource();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QualityTableCols.resolution));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QualityTableCols.dimension));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QualityTableCols.size, new TranscodingErrorIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QualityTableCols.format));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QualityTableCols.delete));

		tableModel = new VideoQualityTableModel(columnsModel, getTranslator());
		
		initTable();	
	}
	
	private void initTable(){
		List<QualityTableRow> rows = new ArrayList<>();
		VideoMeta videoMetadata = videoManager.getVideoMetadata(videoResource);
		// Add master video file
		FormLink previewMasterLink = uifactory.addFormLink("view", "viewQuality", "quality.master", "quality.master", flc, Link.LINK);
		Object[] statusMaster = new Object[]{100, Formatter.formatBytes(videoManager.getVideoFile(videoResource).length())};
		rows.add(new QualityTableRow(previewMasterLink, videoMetadata.getWidth() +"x"+ videoMetadata.getHeight(), statusMaster, "mp4",null));
		// Add all the transcoded versions
		List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoResource);
		for(VideoTranscoding videoTranscoding:videoTranscodings){
			String title = videoManager.getDisplayTitleForResolution(videoTranscoding.getResolution(), getTranslator());
			FormLink previewVersionLink = uifactory.addFormLink("res_" + count++, "viewQuality", title, title, flc, Link.LINK + Link.NONTRANSLATED);
			FormLink deleteLink = uifactory.addFormLink("del_" + count++, "deleteQuality", "quality.delete", "quality.delete", flc, Link.LINK);
			deleteLink.setUserObject(videoTranscoding);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item o_icon-fw");
			
			previewVersionLink.setUserObject(videoTranscoding);
			if (videoTranscoding.getStatus() < VideoTranscoding.TRANSCODING_STATUS_DONE) {
				previewVersionLink.setEnabled(false);
			}
			int width = videoTranscoding.getWidth();
			int height = videoTranscoding.getHeight();
			String dimension = width +"x"+ height;
			String fileSize = "";
			int status = videoTranscoding.getStatus();
			if (videoTranscoding.getSize() != 0 && status > -1) {
				fileSize = Formatter.formatBytes(videoTranscoding.getSize());
			} else if (status == VideoTranscoding.TRANSCODING_STATUS_WAITING) {
				fileSize = translate("transcoding.waiting");
			} else if (status <= VideoTranscoding.TRANSCODING_STATUS_DONE && status > -1){
				fileSize = translate("transcoding.processing") + ": " + videoTranscoding.getStatus() + "%";					
			} else if (status == VideoTranscoding.TRANSCODING_STATUS_INEFFICIENT) {
				fileSize = translate("transcoding.inefficient");
			} else if (status == VideoTranscoding.TRANSCODING_STATUS_ERROR) {
				fileSize = translate("transcoding.error");
			} else if (status == VideoTranscoding.TRANSCODING_STATUS_TIMEOUT) {
				fileSize = translate("transcoding.timeout");
			} 
			Object[] statusTranscoding = new Object[]{status, fileSize};
			rows.add(new QualityTableRow(previewVersionLink, dimension,statusTranscoding, videoTranscoding.getFormat(), deleteLink));
		}
		List<Integer> missingResolutions = videoManager.getMissingTranscodings(videoResource);
		if (videoModule.isTranscodingEnabled()) {
		 	for(Integer missingRes : missingResolutions){
				if (missingRes <= videoMetadata.getHeight()) {
					String title = videoManager.getDisplayTitleForResolution(missingRes, getTranslator());
					FormLink transcodeLink = uifactory.addFormLink("res_" + count++, "startTranscoding", "quality.transcode", "quality.transcode", flc, Link.LINK);
					transcodeLink.setUserObject(missingRes);
					transcodeLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
					
					FormLink previewMissingLink= uifactory.addFormLink("res_" + count++, "viewQuality", title, title, flc, Link.LINK + Link.NONTRANSLATED);
					previewMissingLink.setEnabled(false);
					Object[] status = new Object[]{-1, "-"};
					rows.add(new QualityTableRow(previewMissingLink, missingRes.toString(),status, "mp4", transcodeLink));
				}
			}
		}
	 	rows.sort(new VideoComparator());
		tableModel.setObjects(rows);
		
		if (flc.hasFormComponent(tableEl)){
			flc.remove(tableEl);
		}
		if (flc.hasFormComponent(refreshbtn)){
			flc.remove(refreshbtn);
		}
						
		tableEl = uifactory.addTableElement(getWindowControl(), "qualityTable", tableModel, getTranslator(), flc);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
				
		refreshbtn = uifactory.addFormLink("button.refresh", flc, Link.BUTTON);
		refreshbtn.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, org.olat.core.gui.control.Event event) {
		if (source instanceof FormLink && ((FormLink) source).getCmd().equals("viewQuality")) {
			if (cmc == null) {
				// initialize preview controller only once
				previewVC = createVelocityContainer("video_preview");
				cmc = new CloseableModalController(getWindowControl(), "close", previewVC);
				listenTo(cmc);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink && ((FormLink) source).getCmd().equals("viewQuality")) {
			if (cmc == null) {
				// initialize preview controller only once
				previewVC = createVelocityContainer("video_preview");
				cmc = new CloseableModalController(getWindowControl(), "close", previewVC);
				listenTo(cmc);
			}
			// Get the user object from the link to access version object
			FormLink link = (FormLink) source;
			VideoTranscoding videoTranscoding = (VideoTranscoding) link.getUserObject();
			if (videoTranscoding == null) {
				// this is the master video
				VideoMeta videoMetadata = videoManager.getVideoMetadata(videoResource);
				previewVC.contextPut("width", videoMetadata.getWidth());
				previewVC.contextPut("height", videoMetadata.getHeight());
				previewVC.contextPut("filename", "video.mp4");
				VFSContainer container = videoManager.getMasterContainer(videoResource);
				String transcodedUrl = registerMapper(ureq, new VideoMediaMapper(container));
				previewVC.contextPut("mediaUrl", transcodedUrl);
			} else {
				// this is a version
				previewVC.contextPut("width", videoTranscoding.getWidth());
				previewVC.contextPut("height", videoTranscoding.getHeight());
				previewVC.contextPut("filename", videoTranscoding.getResolution() + "video.mp4");
				VFSContainer container = videoManager.getTranscodingContainer(videoResource);
				String transcodedUrl = registerMapper(ureq, new VideoMediaMapper(container));
				previewVC.contextPut("mediaUrl", transcodedUrl);
			}
			// activate dialog to bring it in front
			cmc.activate();
		} else if (source instanceof FormLink && ((FormLink) source).getCmd().equals("deleteQuality")) {
			FormLink link = (FormLink) source;
			VideoTranscoding videoTranscoding = (VideoTranscoding) link.getUserObject();
			videoManager.deleteVideoTranscoding(videoTranscoding);
		} else if (source instanceof FormLink && ((FormLink) source).getCmd().equals("startTranscoding")) {
			videoManager.createTranscoding(videoResource, (int) source.getUserObject(), "mp4");
		}
		initTable();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do, events cached in formInnerEvent
	}

	private class VideoComparator implements Comparator<QualityTableRow> {

		@Override
		public int compare(QualityTableRow row1, QualityTableRow row2) {
			
			if (row1 == null || row1.getResolution() == null) return -1;
			if (row2 == null || row2.getResolution() == null) return -1;
			
			String s1 = translate(row1.getResolution().getI18nKey());
			String s2 = translate(row2.getResolution().getI18nKey());
			
			if (s1 == null || s1.length() == 0) return -1;	
			if (s2 == null || s2.length() == 0) return 1;	
			
			if ("Master video".equals(s1)) return -1;
			else if ("Master video".equals(s2)) return 1;
			else {
				try {
					int comp = Integer.parseInt(s2.substring(0, s2.length() < 30 ? s2.length() : 30).replaceAll("[^0-9]", ""))
							- Integer.parseInt(s1.substring(0, s1.length() < 30 ? s1.length() : 30).replaceAll("[^0-9]", ""));
					return comp;
				} catch (Exception e) {
					log.error("No valid transcoding resolution available", e);
					return 0;
				}
			}
		}
	}
}
