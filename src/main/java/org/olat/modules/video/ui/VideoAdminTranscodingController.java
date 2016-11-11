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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoTranscoding;
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
	
	private MultipleSelectionElement enable2160SelectionEl;
	private MultipleSelectionElement enable1080SelectionEl;
	private MultipleSelectionElement enable720SelectionEl;
	private MultipleSelectionElement enable480SelectionEl;
	private MultipleSelectionElement enable360SelectionEl;
	private MultipleSelectionElement enable240SelectionEl;
	
	private Map<Integer,Set<Long>> availableTranscodings;
	private List<OLATResource> olatresources;
	
	
	@Autowired
	private OLATResourceManager olatresourceManager;
	@Autowired 
	private VideoManager videoManager;

	public VideoAdminTranscodingController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);

		generateStatusOfTranscodings();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("manage.transcodings.title");
		setFormDescription("manage.transcodings.description");
		setFormContextHelp("Portfolio template: Administration and editing#configuration");		
		
		String[] enableKeys = new String[]{ "on" };
		String[] enableValues = new String[]{ translate("on") };

		enable2160SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.2160", formLayout, enableKeys, enableValues);
		enable2160SelectionEl.addActionListener(FormEvent.ONCHANGE);
		enable2160SelectionEl.setUserObject(2160);
		
		enable1080SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.1080", formLayout, enableKeys, enableValues);
		enable1080SelectionEl.addActionListener(FormEvent.ONCHANGE);
		enable1080SelectionEl.setUserObject(1080);
		
		enable720SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.720", formLayout, enableKeys, enableValues);
		enable720SelectionEl.addActionListener(FormEvent.ONCHANGE);
		enable720SelectionEl.setUserObject(720);
		
		enable480SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.480", formLayout, enableKeys, enableValues);
		enable480SelectionEl.addActionListener(FormEvent.ONCHANGE);
		enable480SelectionEl.setUserObject(480);
		
		enable360SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.360", formLayout, enableKeys, enableValues);
		enable360SelectionEl.addActionListener(FormEvent.ONCHANGE);
		enable360SelectionEl.setUserObject(360);
		
		enable240SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.240", formLayout, enableKeys, enableValues);
		enable240SelectionEl.addActionListener(FormEvent.ONCHANGE);
		enable240SelectionEl.setUserObject(240);
		
		setChecks();
	}
	
	public void setChecks(){	
		
		generateStatusOfTranscodings();
		
		MultipleSelectionElement[] resolutionSelectionEls = {enable240SelectionEl,enable360SelectionEl,
				enable480SelectionEl, enable720SelectionEl,enable1080SelectionEl,enable2160SelectionEl};
		
		//iterate all MultiSelectionElements and decide if checked or not
		for (MultipleSelectionElement mse : resolutionSelectionEls) {
			int sizeOfTranscodings = availableTranscodings.get((int) mse.getUserObject()).size();
			if (sizeOfTranscodings == olatresources.size()){
				mse.select("on",true);
			} else {
				mse.select("on",false);
			}
			String transcoded = " " + translate("number.transcodings");
			mse.setKeysAndValues(new String[]{ "on" }, new String[]{ sizeOfTranscodings + "/" + olatresources.size() + transcoded});
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enable2160SelectionEl){
			queueCreateOrDeleteTranscoding(source);
		} else if (source == enable1080SelectionEl){
			queueCreateOrDeleteTranscoding(source);
		} else if (source == enable720SelectionEl){
			queueCreateOrDeleteTranscoding(source);
		} else if (source == enable480SelectionEl){
			queueCreateOrDeleteTranscoding(source);
		} else if (source == enable360SelectionEl){
			queueCreateOrDeleteTranscoding(source);
		} else if (source == enable240SelectionEl){	
			queueCreateOrDeleteTranscoding(source);	
		}
		//refresh checks
		setChecks();
	}
	
	private void generateStatusOfTranscodings() {
		availableTranscodings = new HashMap<>();
		availableTranscodings.put(240, new HashSet<Long>());
		availableTranscodings.put(360, new HashSet<Long>());
		availableTranscodings.put(480, new HashSet<Long>());
		availableTranscodings.put(720, new HashSet<Long>());
		availableTranscodings.put(1080, new HashSet<Long>());
		availableTranscodings.put(2160, new HashSet<Long>());
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
				if (videoTranscoding.getStatus() != -1 || true) {
					availableTranscodings.get(videoTranscoding.getResolution())
							.add(videoTranscoding.getVideoResource().getKey());
				}
			}
		}
	}
	
	//create of delete resources, depended on MultiSelectionElement status
	private void queueCreateOrDeleteTranscoding(FormItem source){
		if (source instanceof MultipleSelectionElement && ((MultipleSelectionElement)source).isSelected(0)){
			queueCreateTranscoding(source);
		} else {
			queueDeleteTranscoding(source);
		}	
	}
	
	//state orders for inexistent transcodings
	private void queueCreateTranscoding(FormItem source){
		for (OLATResource videoResource : olatresources) {
			if (!availableTranscodings.get((int) source.getUserObject()).contains(videoResource.getKey())){
				videoManager.createTranscoding(videoResource, (int) source.getUserObject(), "mp4");				
			}
		}
	}
	
	//go through all and delete selection
	private void queueDeleteTranscoding(FormItem source) {
		for (OLATResource videoResource : olatresources) {
			if (availableTranscodings.get((int) source.getUserObject()).contains(videoResource.getKey())) {
				List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoResource);

				for (VideoTranscoding videoTranscoding : videoTranscodings) {
					if (videoTranscoding.getResolution() == (int) source.getUserObject()) {
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
