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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMetadata;
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

	@Autowired
	private VideoManager videoManager;

	public VideoQualityTableFormController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.videoResource = videoEntry.getOlatResource();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("tab.video.qualityConfig"));
		generalCont.setRootForm(mainForm);
		generalCont.setFormContextHelp("Learning resource: Video#_video_resolution");
		formLayout.add(generalCont);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.resolution.i18nKey(), QualityTableCols.resolution.ordinal(), true, QualityTableCols.resolution.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.dimension.i18nKey(), QualityTableCols.dimension.ordinal(), true, QualityTableCols.dimension.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.size.i18nKey(), QualityTableCols.size.ordinal(), true, QualityTableCols.size.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.format.i18nKey(), QualityTableCols.format.ordinal(), true, QualityTableCols.format.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.view.i18nKey(), QualityTableCols.view.ordinal(), true, QualityTableCols.view.name()));
		// TODO: delete/recode link
		tableModel = new VideoQualityTableModel(columnsModel, getTranslator());

		List<QualityTableRow> rows = new ArrayList<QualityTableRow>();
		VideoMetadata videoMetadata = videoManager.readVideoMetadataFile(videoResource);

		// Add master video file
		FormLink previewMasterLink = uifactory.addFormLink("view", "viewQuality", "quality.view", "qulaity.view", null, Link.LINK);
		rows.add(new QualityTableRow(translate("quality.master"), videoMetadata.getWidth() +"x"+ videoMetadata.getHeight(),  Formatter.formatBytes(videoManager.getVideoFile(videoResource).length()), "mp4",previewMasterLink));
		// Add all the transcoded versions
		List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoResource);
		for(VideoTranscoding videoTranscoding:videoTranscodings){
			FormLink previewVersionLink = uifactory.addFormLink(Integer.toString(videoTranscoding.getResolution()), "viewQuality", "quality.view", "qulaity.view", null, Link.LINK);
			previewVersionLink.setUserObject(videoTranscoding);
			if (videoTranscoding.getStatus() < VideoTranscoding.TRANSCODING_STATUS_DONE) {
				previewVersionLink.setEnabled(false);
			}
			int width = videoTranscoding.getWidth();
			int height = videoTranscoding.getHeight();
			String dimension = width +"x"+ height;
			String fileSize = "";
			if (videoTranscoding.getSize() != 0) {
				fileSize = Formatter.formatBytes(videoTranscoding.getSize());
			} else if (videoTranscoding.getStatus() == VideoTranscoding.TRANSCODING_STATUS_WAITING) {
				fileSize = translate("transcoding.waiting");
			} else if (videoTranscoding.getStatus() <= VideoTranscoding.TRANSCODING_STATUS_DONE){
				fileSize = translate("transcoding.processing") + ": " + videoTranscoding.getStatus() + "%";					
			}
			// Set title for version - standard version or original size
			String title = videoManager.getDisplayTitleForResolution(videoTranscoding.getResolution(), getTranslator());
			rows.add(new QualityTableRow(title, dimension,  fileSize, videoTranscoding.getFormat(),previewVersionLink));
		}
		
		tableModel.setObjects(rows);
		tableEl = uifactory.addTableElement(getWindowControl(), "qualities", tableModel, getTranslator(), generalCont);
		tableEl.setCustomizeColumns(false);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
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
				VideoMetadata videoMetadata = videoManager.readVideoMetadataFile(videoResource);
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
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do, events cached in formInnerEvent
	}

	@Override
	protected void doDispose() {
		// controller auto disposed
	}

}
