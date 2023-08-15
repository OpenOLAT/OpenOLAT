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
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocumentSavedEvent;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditor;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.ModalInspectorController;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.AbstractMediaHandler.Storage;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DrawioMediaController extends BasicController implements PageRunElement, GenericEventListener {
	
	private VelocityContainer mainVC;
	private Link editLink;
	
	private ImageMediaController imageMediaCtrl;

	private StoredData storedData;
	private VFSLeaf vfsLeaf;
	
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private DrawioEditor drawioEditor;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private DocEditorService docEditorService;

	public DrawioMediaController(UserRequest ureq, WindowControl wControl, Storage dataStorage, MediaPart mediaPart,
			RenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		this.storedData = mediaPart.getStoredData();
		
		initVC(hints);
		
		if (this.vfsLeaf != null) {
			imageMediaCtrl = new ImageMediaController(ureq, wControl, dataStorage, mediaPart, hints);
			imageMediaCtrl.setPreventBrowserCaching(hints.isEditable());
			listenTo(imageMediaCtrl);
			mainVC.put("image", imageMediaCtrl.getInitialComponent());
		}
	}
	
	public DrawioMediaController(UserRequest ureq, WindowControl wControl, Storage dataStorage, MediaVersion mediaVersion,
			RenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		this.storedData = mediaVersion;
		
		initVC(hints);
		
		if (this.vfsLeaf != null) {
			imageMediaCtrl = new ImageMediaController(ureq, wControl, dataStorage, mediaVersion, hints);
			imageMediaCtrl.setPreventBrowserCaching(hints.isEditable());
			listenTo(imageMediaCtrl);
			mainVC.put("image", imageMediaCtrl.getInitialComponent());
		}
	}

	private void initVC(RenderingHints hints) {
		mainVC = createVelocityContainer("media_drawio");
		putInitialPanel(mainVC);
		
		VFSContainer container = fileStorage.getMediaContainer(storedData);
		VFSItem item = container.resolve(storedData.getRootFilename());
		if (item instanceof VFSLeaf vfsLeaf) {
			this.vfsLeaf = vfsLeaf;
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, DocEditorService.DOCUMENT_SAVED_EVENT_CHANNEL);
			
			editLink = LinkFactory.createCustomLink("edit", "edit", "edit", Link.LINK, mainVC, this);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			editLink.setElementCssClass("btn btn-default btn-xs o_button_ghost");
			editLink.setNewWindow(true, true);
			editLink.setVisible(drawioModule.isEnabled() && hints.isEditable() && !hints.isToPdf() && !hints.isOnePage());
			if(storedData instanceof MediaVersion mediaVersion) {
				editLink.setVisible(editLink.isVisible() && mediaService.isMediaEditable(getIdentity(), mediaVersion.getMedia()));
				
			}
		}
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return true;
	}

	@Override
	public void event(Event event) {
		if (event instanceof DocumentSavedEvent dccEvent) {
			if (imageMediaCtrl != null && vfsLeaf != null && vfsLeaf.getMetaInfo().getKey().equals(dccEvent.getVfsMetadatKey())) {
				imageMediaCtrl.updateImage(storedData);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEdit(ureq);
		}
	}
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (imageMediaCtrl != null && source instanceof ModalInspectorController && event instanceof ChangePartEvent) {
			imageMediaCtrl.dispatchEvent(ureq, source, event);
		}
	}

	@Override
	public void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, DocEditorService.DOCUMENT_SAVED_EVENT_CHANNEL);
		super.doDispose();
	}
	
	private void doEdit(UserRequest ureq) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(Mode.EDIT)
				.withFireSavedEvent(true)
				.build(vfsLeaf);
		// Use explicitly the draw.io editor because the common image editor is read only
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), drawioEditor, configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
}
