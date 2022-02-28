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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoMetadataSearchParams;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.model.TranscodingCount;
import org.olat.modules.video.ui.TranscodingTableModel.TranscodingCols;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class VideoAdminTranscodingController. 
 * Initial Date: 25.10.2016
 * @autor fkiefer fabian.kiefer@frentix.com
 * this class controls the transcondings of a kind, either delete all,
 * transcode all or only the missing
 */
public class VideoAdminTranscodingController extends FormBasicController {
	
	// Hardcoded same as VideoAdminSetController
	private static final List<Integer> RESOLUTIONS = List.of(2160, 1080, 720, 480);

	private TranscodingTableModel tableModel;
	private FlexiTableElement transcodingTable;
	
	@Autowired 
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;

	public VideoAdminTranscodingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "transcoding_admin");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("manage.transcodings.title");
		setFormDescription("manage.transcodings.description");
		setFormContextHelp("manual_user/portfolio/Portfolio_template_Administration_and_editing/");		
		
		FlexiTableColumnModel transcodingModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.resolutions));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.sumVideos));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.extern));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.numberTranscodings));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.failedTranscodings));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.missingTranscodings));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.transcode, "quality.transcode", 
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("quality.transcode"), "quality.transcode",
						"", "o_icon o_icon_refresh o_icon-fw"), null)));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.delete, "quality.delete", 
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("quality.delete"), "quality.delete",
						"", "o_icon o_icon_delete_item o_icon-fw"), null)));
		tableModel = new TranscodingTableModel(transcodingModel, getTranslator());
		
		transcodingTable = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		transcodingTable.setCustomizeColumns(false);
		transcodingTable.setNumOfRowsEnabled(false);
				
		loadTable();
	}
	
	private boolean mayTranscode(int resolution){
		if (!videoModule.isTranscodingEnabled()) {
			return false;
		}
		int[] transcodingRes = videoModule.getTranscodingResolutions();
		for (int i = 0; i < transcodingRes.length; i++) {
			if (resolution == transcodingRes[i]){
				return true;
			}
		}
		return false;
	}

	private void loadTable() {
		List<TranscodingRow> resolutions = new ArrayList<>(6);
		Map<Integer, Integer> successCount = new HashMap<>();
		int beginErrorCode = VideoTranscoding.TRANSCODING_STATUS_INEFFICIENT;
		for (TranscodingCount transcodingCount : videoManager.getAllVideoTranscodingsCountSuccess(beginErrorCode)) {
			successCount.put(transcodingCount.getResolution(), transcodingCount.getCount());
		}
		Map<Integer, Integer> failCount = new HashMap<>();
		for (TranscodingCount transcodingCount : videoManager.getAllVideoTranscodingsCountFails(beginErrorCode)) {
			failCount.put(transcodingCount.getResolution(), transcodingCount.getCount());
		}
		
		List<VideoMeta> videoMetadatas = videoManager.getVideoMetadata(new VideoMetadataSearchParams());
		for (Integer resolution: RESOLUTIONS) {
			int sumVideos = 0;
			int extern = 0;
			for (VideoMeta videoMetadata : videoMetadatas) {
				if (videoMetadata.getHeight() >= resolution) {
					sumVideos++;
					if (StringHelper.containsNonWhitespace(videoMetadata.getUrl())) {
						extern++;
					}
				}
			}
			
			int success = successCount.get(resolution) != null ? successCount.get(resolution) : 0;
			int fails = failCount.get(resolution) != null ? failCount.get(resolution) : 0;
			TranscodingRow transcodingRow = new TranscodingRow(resolution.intValue(), success, fails, extern, sumVideos,
					mayTranscode(resolution.intValue()));
			resolutions.add(transcodingRow);
		}
		tableModel.setObjects(resolutions);
		transcodingTable.reset(true, true, true);
	}
	
	
	/**
	 * Update Table Content of all available Transcodings
	 */
	public void reloadTable(){	
		loadTable();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == transcodingTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				TranscodingRow currentObject = tableModel.getObject(se.getIndex());
				if ("quality.delete".equals(se.getCommand())){
					queueDeleteTranscoding(currentObject);
					showInfo("delete.transcodings");
				} else if ("quality.transcode".equals(se.getCommand())){
					queueCreateTranscoding(currentObject);
					showInfo("info.transcoding");
				} 
			}
		}
		reloadTable();
	}
	
	private void queueCreateTranscoding(TranscodingRow source) {
		List<VideoTranscoding> allVideoTranscodings = videoManager.getOneVideoResolution(source.getResolution());
		Map<OLATResource, Set<Integer>> availableTranscodings = new HashMap<>();
		for (VideoTranscoding videoTranscoding : allVideoTranscodings) {
			if (availableTranscodings.containsKey(videoTranscoding.getVideoResource())) {
				availableTranscodings.get(videoTranscoding.getVideoResource()).add(videoTranscoding.getResolution());
			} else {
				Set<Integer> availableresolutions = new HashSet<>();
				availableresolutions.add(videoTranscoding.getResolution());
				availableTranscodings.put(videoTranscoding.getVideoResource(), availableresolutions);
			}
		}
		
		VideoMetadataSearchParams searchParams = new VideoMetadataSearchParams();
		searchParams.setUrlNull(Boolean.TRUE);
		searchParams.setMinHeight(source.getResolution());
		List<VideoMeta> videoMetadatas = videoManager.getVideoMetadata(searchParams);
		for (VideoMeta videoMetadata : videoMetadatas) {
			OLATResource videoResource = videoMetadata.getVideoResource();
			if (!availableTranscodings.containsKey(videoResource) || !availableTranscodings.get(videoResource).contains(source.getResolution())) {
				videoManager.createTranscoding(videoResource, source.getResolution(), "mp4");
			}
		}
	}
	
	//go through all and delete selection
	private void queueDeleteTranscoding(TranscodingRow source) {
		List<VideoTranscoding> allVideoTranscodings = videoManager.getOneVideoResolution(source.getResolution());
		for (VideoTranscoding videoTranscoding : allVideoTranscodings) {
			if (videoTranscoding.getResolution() == source.getResolution()) {
				videoManager.deleteVideoTranscoding(videoTranscoding);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}
}
