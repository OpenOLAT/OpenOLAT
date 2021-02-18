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

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Formcontroller for editform of the video poster
 * 
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoPosterEditController extends FormBasicController {

	private FormLink deleteImage;
	private FormLink uploadImage;
	private FormLink replaceImage;
	private FormLayoutContainer displayContainer;
	
	private CloseableModalController cmc;
	private VideoPosterUploadForm posterUploadForm;
	private VideoPosterSelectionForm posterSelectionForm;
	
	private VideoMeta videoMetadata;
	private OLATResource videoResource;
	
	@Autowired
	private VideoManager videoManager;

	public VideoPosterEditController(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		this.videoResource = videoResource;
		videoMetadata = videoManager.getVideoMetadata(videoResource);
		if(!StringHelper.containsNonWhitespace(videoMetadata.getUrl()) && videoMetadata.getVideoFormat() == null) {
			videoMetadata = videoManager.checkUnkownVideoFormat(videoMetadata);
		}
		initForm(ureq);
		updatePosterImage(ureq, videoResource);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.video.posterConfig");

		String posterPage = velocity_root + "/poster_config.html";
		displayContainer = FormLayoutContainer.createCustomFormLayout("tasks", getTranslator(), posterPage);

		displayContainer.contextPut("hint", translate("video.config.poster.hint"));

		displayContainer.setLabel("video.config.poster", null);
		formLayout.add(displayContainer);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);

		replaceImage = uifactory.addFormLink("replaceimg", "video.config.poster.replace", null, buttonGroupLayout, Link.BUTTON);
		replaceImage.setIconLeftCSS("o_icon o_icon_browse o_icon-fw");
		VideoFormat format = videoMetadata.getVideoFormat();
		replaceImage.setVisible(format == VideoFormat.mp4 || format == VideoFormat.panopto);

		uploadImage = uifactory.addFormLink("uploadImage", "video.config.poster.upload", null, buttonGroupLayout, Link.BUTTON);
		uploadImage.setIconLeftCSS("o_icon o_icon_upload o_icon-f");
		
		deleteImage = uifactory.addFormLink("deleteImage", "delete", null, buttonGroupLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == replaceImage) {
			doReplacePoster(ureq);
		} else if (source == uploadImage) {
			doUploadPoster(ureq);
		} else if (source == deleteImage) {
			doDeletePoster(ureq);
		}
	}
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == posterUploadForm || source == posterSelectionForm){
			if(event instanceof FolderEvent){
				VFSLeaf posterFile = (VFSLeaf) ((FolderEvent) event).getItem();
				if(source == posterUploadForm){
					videoManager.setPosterframeResizeUploadfile(videoResource, posterFile, getIdentity());
					posterFile.delete();
				} else {					
					videoManager.setPosterframe(videoResource, posterFile, getIdentity());
				}
				updatePosterImage(ureq, videoResource);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(posterSelectionForm);
		removeAsListenerAndDispose(posterUploadForm);
		removeAsListenerAndDispose(cmc);
		posterSelectionForm = null;
		posterUploadForm = null;
		cmc = null;
	}

	private void doReplacePoster(UserRequest ureq){
		posterSelectionForm = new VideoPosterSelectionForm(ureq, getWindowControl(), videoResource, videoMetadata);
		listenTo(posterSelectionForm);
		
		if(posterSelectionForm.hasProposals()) {
			String title = translate("video.config.poster.replace");
			cmc = new CloseableModalController(getWindowControl(), "close", posterSelectionForm.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		} else {
			showWarning("warning.no.poster.proposals");
			cleanUp();
		}
	}

	private void doUploadPoster(UserRequest ureq){
		posterUploadForm = new VideoPosterUploadForm(ureq, getWindowControl(), videoResource);
		listenTo(posterUploadForm);
		
		String title = translate("video.config.poster.upload");
		cmc = new CloseableModalController(getWindowControl(), "close", posterUploadForm.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeletePoster(UserRequest ureq){
		videoManager.deletePosterframe(videoResource);
		updatePosterImage(ureq, videoResource);
	}

	private boolean updatePosterImage(UserRequest ureq, OLATResource video){
		VFSLeaf posterFile = videoManager.getPosterframe(video);
		if(posterFile != null) {
			VFSContainer masterContainer = posterFile.getParentContainer();
			VideoMediaMapper mediaMapper = new VideoMediaMapper(masterContainer);
			String mediaUrl = registerMapper(ureq, mediaMapper);
			displayContainer.contextPut("mediaUrl", mediaUrl);
		} else {
			displayContainer.contextRemove("mediaUrl");
		}
		displayContainer.setDirty(true);
		deleteImage.setVisible(posterFile != null);
		return posterFile != null;
	}
}