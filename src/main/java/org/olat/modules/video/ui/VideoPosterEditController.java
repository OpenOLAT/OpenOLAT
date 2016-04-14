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
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.video.manager.MediaMapper;
import org.olat.modules.video.manager.VideoManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == posterUploadForm|| source == posterSelectionForm){
			if(event instanceof FolderEvent){
				posterFile = (VFSLeaf) ((FolderEvent) event).getItem();
				flc.setDirty(true);
				cmc.deactivate();
				VFSLeaf newPosterFile = posterFile;
				videoManager.setPosterframe(videoResource, newPosterFile);
				if(source == posterUploadForm){
					posterFile.delete();
				}
				updatePosterImage(ureq, videoResource);
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
		VFSContainer mediaBase = FileResourceManager.getInstance().getFileResourceMedia(video);
		MediaMapper mediaMapper = new MediaMapper(mediaBase);
		String mediaUrl = registerMapper(ureq, mediaMapper);
		String serverUrl = Settings.createServerURI();
		displayContainer.contextPut("serverUrl", serverUrl);
		displayContainer.contextPut("mediaUrl", mediaUrl);
	}
}