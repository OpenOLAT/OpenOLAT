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
import org.olat.core.helpers.Settings;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoManager;
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

	@Autowired
	private VideoManager videoManager;
	private VFSLeaf posterFile;
	private OLATResource videoResource;
	private FormLayoutContainer displayContainer;
	private FormLink replaceImage;
	private FormLink uploadImage;
	private VideoPosterSelectionForm posterSelectionForm;
	private CloseableModalController cmc;
	private VideoPosterUploadForm posterUploadForm;

	public VideoPosterEditController(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.video.posterConfig");

		String posterPage = velocity_root + "/poster_config.html";
		displayContainer = FormLayoutContainer.createCustomFormLayout("tasks", getTranslator(), posterPage);

		displayContainer.contextPut("hint", translate("video.config.poster.hint"));

		posterFile = videoManager.getPosterframe(videoResource);
		updatePosterImage(ureq, videoResource);
		displayContainer.setLabel("video.config.poster", null);
		formLayout.add(displayContainer);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);

		replaceImage = uifactory.addFormLink("replaceimg", "video.config.poster.replace", null, buttonGroupLayout, Link.BUTTON);
		replaceImage.setIconLeftCSS("o_icon o_icon_browse o_icon-fw");
		replaceImage.setVisible(true);
		uploadImage = uifactory.addFormLink("uploadImage", "video.config.poster.upload", null, buttonGroupLayout, Link.BUTTON);
		uploadImage.setIconLeftCSS("o_icon o_icon_upload o_icon-f");
		uploadImage.setVisible(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == replaceImage) {
			doReplaceVideo(ureq);
		} else if (source == uploadImage) {
			doUploadVideo(ureq);
		}
	}
	@Override
	protected void doDispose() {

	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == posterUploadForm || source == posterSelectionForm){
			if(event instanceof FolderEvent){
				posterFile = (VFSLeaf) ((FolderEvent) event).getItem();
				flc.setDirty(true);
				cmc.deactivate();
				VFSLeaf newPosterFile = posterFile;
				
				if(source == posterUploadForm){
					videoManager.setPosterframeResizeUploadfile(videoResource, newPosterFile);						
					posterFile.delete();
				} else {					
					videoManager.setPosterframe(videoResource, newPosterFile);
				}
				updatePosterImage(ureq, videoResource);
				// cleanup controllers
				if (posterSelectionForm != null) {
					removeAsListenerAndDispose(posterSelectionForm);
					posterSelectionForm = null;
				}
				if (posterUploadForm != null) {
					removeAsListenerAndDispose(posterUploadForm);
					posterUploadForm = null;
				}
				if (cmc != null) {
					removeAsListenerAndDispose(cmc);
					cmc = null;
				}
				
			}
		}
	}

	private void doReplaceVideo(UserRequest ureq){
		posterSelectionForm = new VideoPosterSelectionForm(ureq, getWindowControl(), videoResource);
		listenTo(posterSelectionForm);
		cmc = new CloseableModalController(getWindowControl(), "close", posterSelectionForm.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}

	private void doUploadVideo(UserRequest ureq){
		posterUploadForm = new VideoPosterUploadForm(ureq, getWindowControl(), videoResource);
		listenTo(posterUploadForm);
		cmc = new CloseableModalController(getWindowControl(), "close", posterUploadForm.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}

	private void updatePosterImage(UserRequest ureq, OLATResource video){
		posterFile = videoManager.getPosterframe(video);
		VFSContainer masterContainer = posterFile.getParentContainer();
		VideoMediaMapper mediaMapper = new VideoMediaMapper(masterContainer);
		String mediaUrl = registerMapper(ureq, mediaMapper);
		String serverUrl = Settings.createServerURI();
		displayContainer.contextPut("serverUrl", serverUrl);
		displayContainer.contextPut("mediaUrl", mediaUrl);
	}
}