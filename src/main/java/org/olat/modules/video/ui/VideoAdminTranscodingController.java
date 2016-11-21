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
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMetadata;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.ui.TranscodingTableModel.TranscodingCols;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class VideoAdminTranscodingController. 
 * Initial Date: 25.10.2016
 * @autor fkiefer fabian.kiefer@frentix.com
 * this class controls the transcondings of a kind, either delete all,
 * transcode all or only the missing
 */
public class VideoAdminTranscodingController extends FormBasicController {
	
	private Map<Integer,Set<OLATResource>> availableTranscodings;
	private List<OLATResource> olatresources;
	private TranscodingTableModel tableModel;
	private FlexiTableElement transcodingTable;
	
	private List<TranscodingRow> resolutions;
	
	@Autowired
	private OLATResourceManager olatresourceManager;
	@Autowired 
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;

	public VideoAdminTranscodingController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "transcoding_admin");
		resolutions = new ArrayList<>();
		generateStatusOfTranscodings();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("manage.transcodings.title");
		setFormDescription("manage.transcodings.description");
		setFormContextHelp("Portfolio template: Administration and editing#configuration");		
		
		FlexiTableColumnModel transcodingModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.resolutions));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.sumVideos));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.numberTranscodings));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.missingTranscodings));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.transcode, "quality.transcode", 
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("quality.transcode"), "quality.transcode"), null)));
		transcodingModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingCols.delete, "quality.delete", 
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("quality.delete"), "quality.delete"), null)));
		tableModel = new TranscodingTableModel(transcodingModel, getTranslator());
		
		transcodingTable = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		transcodingTable.setCustomizeColumns(false);
		transcodingTable.setNumOfRowsEnabled(false);
				
		setChecks();
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
	
	private void loadTable(){
		//Hardcoded same as VideoAdminSetController
		int[] resolution = {2160, 1080, 720, 480, 360, 240};
		//FIXME:FK fetch using one single SQL query
		for (int i = 0; i < resolution.length; i++) {
			int sizeOfTranscodings = availableTranscodings.get(resolution[i]).size();
			int counter = 0;
			for (OLATResource videoResource : olatresources) {
				VideoMetadata videoMetadata = videoManager.readVideoMetadataFile(videoResource);
				if (videoMetadata != null && videoMetadata.getHeight() >= resolution[i]) counter++;
			}
			resolutions.add(new TranscodingRow(resolution[i], sizeOfTranscodings, counter, mayTranscode(resolution[i])));
		}		
		if (resolutions != null) tableModel.setObjects(resolutions);
		transcodingTable.reset(true, true, true);	
	}
	
	/**
	 * Update Table Content of all available Transcodings
	 */
	public void setChecks(){	
		generateStatusOfTranscodings();
		resolutions.clear();
		loadTable();
	}


	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == transcodingTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				TranscodingRow currentObject = (TranscodingRow) tableModel.getObject(se.getIndex());
				if ("quality.delete".equals(se.getCommand())){
					queueDeleteTranscoding(currentObject);
					showInfo("delete.transcodings");
				} else if ("quality.transcode".equals(se.getCommand())){
					queueCreateTranscoding(currentObject);
					showInfo("info.transcoding");
				} 
			}
		}
		
		//refresh checks
		setChecks();
	}
	
	private void generateStatusOfTranscodings() {
		//FIXME:FK fetch using one single SQL query
		availableTranscodings = new HashMap<>();
		availableTranscodings.put(240, new HashSet<OLATResource>());
		availableTranscodings.put(360, new HashSet<OLATResource>());
		availableTranscodings.put(480, new HashSet<OLATResource>());
		availableTranscodings.put(720, new HashSet<OLATResource>());
		availableTranscodings.put(1080, new HashSet<OLATResource>());
		availableTranscodings.put(2160, new HashSet<OLATResource>());
		//determine resource type of interest
		List<String> types = new ArrayList<>();
		types.add("FileResource.VIDEO");
		//retrieve all resources of type video
		olatresources = olatresourceManager.findResourceByTypes(types);
		//go through all video resources
		for (OLATResource videoResource : olatresources) {
			//retrieve all transcodings for each video resource
			List<VideoTranscoding> transcodings = videoManager.getVideoTranscodings(videoResource);
			//map resource IDs to resolution
			for (VideoTranscoding videoTranscoding : transcodings) {
				if (videoTranscoding != null) {
					Set<OLATResource> oneResolution = availableTranscodings.get(videoTranscoding.getResolution());
					if (oneResolution != null) {
						oneResolution.add(videoTranscoding.getVideoResource());						
					}
				}
			}
		}
	}
	
	
	//state orders for inexistent transcodings
	private void queueCreateTranscoding(TranscodingRow source){
		for (OLATResource videoResource : olatresources) {
			if (!availableTranscodings.get(source.getResolution()).contains(videoResource)){
				VideoMetadata videoMetadata = videoManager.readVideoMetadataFile(videoResource);
				if (videoMetadata != null && videoMetadata.getHeight() >= source.getResolution()) {					
					videoManager.createTranscoding(videoResource, source.getResolution(), "mp4");				
				}
			}
		}
	}
	
	//go through all and delete selection
	private void queueDeleteTranscoding(TranscodingRow source) {
		for (OLATResource videoResource : olatresources) {
			if (availableTranscodings.get(source.getResolution()).contains(videoResource)) {
				List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoResource);

				for (VideoTranscoding videoTranscoding : videoTranscodings) {
					if (videoTranscoding.getResolution() == source.getResolution()) {
						videoManager.deleteVideoTranscoding(videoTranscoding);
					}
				}
			}
		}
	}
	


	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

	@Override
	protected void doDispose() {
		// no controllers to clean up
	}
}
