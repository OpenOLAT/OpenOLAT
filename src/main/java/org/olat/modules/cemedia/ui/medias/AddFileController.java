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
package org.olat.modules.cemedia.ui.medias;

import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocTemplate;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.handler.FileHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.AddMediaEvent;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.olat.modules.cemedia.ui.event.UploadMediaEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddFileController extends BasicController implements PageElementAddController {
	
	private final Link addFileButton;
	private final Link createFileButton;
	
	private Media mediaReference;
	private AddElementInfos userObject;
	private final DocTemplates docTemplates;
	
	private CloseableModalController cmc;
	private CreateFileMediaController createFileCtrl;
	private CollectFileMediaController uploadFileCtrl;
	private final MediaCenterController mediaCenterCtrl;
	
	@Autowired
	private DocEditorService docEditorService;
	
	public AddFileController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale()));
		
		docTemplates = FileHandler.getEditableTemplates(getIdentity(), ureq.getUserSession().getRoles(), getLocale());
		
		VelocityContainer mainVC = createVelocityContainer("add_file");
		
		mediaCenterCtrl = new MediaCenterController(ureq, wControl, mediaHandler, true);
		listenTo(mediaCenterCtrl);
		mainVC.put("mediaCenter", mediaCenterCtrl.getInitialComponent());
		
		addFileButton = LinkFactory.createButton("add.file", mainVC, this);
		addFileButton.setElementCssClass("o_sel_upload_file");
		addFileButton.setIconLeftCSS("o_icon o_icon_add");
		
		createFileButton = LinkFactory.createButton("create.file.title", mainVC, this);
		createFileButton.setIconLeftCSS("o_icon o_filetype_ico");
		createFileButton.setVisible(isCreateFilePossible(ureq.getUserSession().getRoles()));

		putInitialPanel(mainVC);
	}
	
	private boolean isCreateFilePossible(Roles roles) {
		List<DocTemplate> editableTemplates = docTemplates.getTemplates();
		for (DocTemplate docTemplate: editableTemplates) {
			if (docEditorService.hasEditor(getIdentity(), roles,  docTemplate.getSuffix(), Mode.EDIT, true, false)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public PageElement getPageElement() {
		return MediaPart.valueOf(mediaReference);
	}

	@Override
	public void setUserObject(AddElementInfos uobject) {
		this.userObject = uobject;
	}

	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addFileButton == source) {
			doAddFile(ureq);
		} else if(createFileButton == source) {
			doCreateFile(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(mediaCenterCtrl == source) {
			if(event instanceof MediaSelectionEvent se) {
				if(se.getMedia() != null) {
					mediaReference = se.getMedia();
					fireEvent(ureq, new AddMediaEvent(false));
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if(event instanceof UploadMediaEvent upme) {
				doUpload(ureq, upme.getUploadMedia());
			}
		} else if(uploadFileCtrl == source) {
			if(event == Event.DONE_EVENT) {
				mediaReference = uploadFileCtrl.getMediaReference();
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(createFileCtrl == source) {
			if(event == Event.DONE_EVENT) {
				mediaReference = createFileCtrl.getMediaReference();
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(uploadFileCtrl);
		removeAsListenerAndDispose(createFileCtrl);
		removeAsListenerAndDispose(cmc);
		uploadFileCtrl = null;
		createFileCtrl = null;
		cmc = null;
	}
	
	private void doUpload(UserRequest ureq, UploadMedia uploadMedia) {
		uploadFileCtrl = new CollectFileMediaController(ureq, getWindowControl(), uploadMedia);
		listenTo(uploadFileCtrl);
		
		String title = translate("add.file");
		cmc = new CloseableModalController(getWindowControl(), null, uploadFileCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddFile(UserRequest ureq) {
		if(guardModalController(uploadFileCtrl)) return;
		
		uploadFileCtrl = new CollectFileMediaController(ureq, getWindowControl());
		listenTo(uploadFileCtrl);
		
		String title = translate("add.file");
		cmc = new CloseableModalController(getWindowControl(), null, uploadFileCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateFile(UserRequest ureq) {
		if(guardModalController(createFileCtrl)) return;
		
		createFileCtrl = new CreateFileMediaController(ureq, getWindowControl(), docTemplates);
		listenTo(createFileCtrl);
		
		String title = translate("create.file.title");
		cmc = new CloseableModalController(getWindowControl(), null, createFileCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
