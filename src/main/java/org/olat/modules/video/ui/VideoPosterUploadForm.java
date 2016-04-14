package org.olat.modules.video.ui;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.resource.OLATResource;
/**
 * Videoposter upload form for create a 
 * @author dfakae
 *
 */

public class VideoPosterUploadForm extends FormBasicController {
	private OLATResource videoResource;
	long remainingSpace;
	private VFSContainer videoResourceFileroot;
	private VFSContainer metaDataFolder;
	private FileElement posterField;

	private static final int picUploadlimitKB = 51200;


	private static final Set<String> imageMimeTypes = new HashSet<String>();
	static {
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
	}

	public VideoPosterUploadForm(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		remainingSpace = Quota.UNLIMITED;
		videoResourceFileroot = new LocalFolderImpl(FileResourceManager.getInstance().getFileResourceRootImpl(videoResource).getBasefile());
		metaDataFolder = VFSManager.getOrCreateContainer(videoResourceFileroot, "media");

		posterField = uifactory.addFileElement(getWindowControl(), "poster", "video.config.poster", formLayout);
		posterField.limitToMimeType(imageMimeTypes, null, null);
		posterField.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		posterField.setPreview(ureq.getUserSession(), true);
		posterField.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		buttonGroupLayout.setElementCssClass("o_sel_upload_buttons");
		uifactory.addFormSubmitButton("track.upload", buttonGroupLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if ( posterField.isUploadSuccess()) {
			if (remainingSpace != -1) {
				if (posterField.getUploadFile().length() / 1024 > remainingSpace) {
					posterField.setErrorKey("QuotaExceeded", null);
					posterField.getUploadFile().delete();
					return;
				}
			}else{
				fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, posterField.moveUploadFileTo(metaDataFolder)));
			}
		}
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
	}
}