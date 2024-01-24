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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.MediaSettings;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.ModalInspectorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ChangeVersionPartEvent;
import org.olat.modules.cemedia.Citation;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MetadataXStream;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaMetadataController;
import org.olat.modules.cemedia.ui.component.CitationComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationMediaController extends BasicController {
	
	private final VelocityContainer mainVC;
	private Link editLink;

	private MediaVersion mediaVersion;

	private CloseableModalController cmc;
	private UpdateTextVersionController citationCtrl;
	
	@Autowired
	private MediaService mediaService;
	
	public CitationMediaController(UserRequest ureq, WindowControl wControl, MediaPart mediaPart, MediaVersion mediaVersion, RenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		
		this.mediaVersion = mediaVersion;
		mainVC = createVelocityContainer("media_citation");
		setBlockLayoutClass(mediaPart.getMediaSettings());

		if(mediaVersion != null) {
			Media media = mediaVersion.getMedia();
			
			String desc = media.getDescription();
			mainVC.contextPut("description", StringHelper.containsNonWhitespace(desc) ? desc : null);
			String title = media.getTitle();
			mainVC.contextPut("title", StringHelper.containsNonWhitespace(title) ? title : null);
			mainVC.contextPut("citation", mediaVersion.getContent());
			
			editLink = LinkFactory.createCustomLink("edit", "edit", "edit", Link.LINK, mainVC, this);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			editLink.setElementCssClass("btn btn-default btn-xs o_button_ghost");
			editLink.setVisible(hints.isEditable() && !hints.isToPdf() && !hints.isOnePage() 
					&& mediaService.isMediaEditable(getIdentity(), media));

			String citationXml = media.getMetadataXml();
			if(StringHelper.containsNonWhitespace(citationXml)) {
				Citation citation = (Citation)MetadataXStream.get().fromXML(citationXml);
				CitationComponent citationCmp = new CitationComponent("cit");
				citationCmp.setCitation(citation);
				citationCmp.setDublinCoreMetadata(media);
				mainVC.put("cit", citationCmp);	
			}
			
			if(hints.isExtendedMetadata()) {
				MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
				listenTo(metaCtrl);
				mainVC.put("meta", metaCtrl.getInitialComponent());
			}
		}
		putInitialPanel(mainVC);
	}

	private void setBlockLayoutClass(MediaSettings mediaSettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(mediaSettings, false));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEdit(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof ModalInspectorController && event instanceof ChangeVersionPartEvent cvpe) {
			PageElement element = cvpe.getElement();
			if(element instanceof MediaPart mediaPart) {
				mainVC.contextPut("citation", mediaPart.getMediaVersion().getContent());
			}
		} else if(citationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				mediaVersion = citationCtrl.getMediaVersion();
				mainVC.contextPut("citation", mediaVersion.getContent());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (source instanceof ModalInspectorController && event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof MediaPart mediaPart) {
				setBlockLayoutClass(mediaPart.getMediaSettings());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(citationCtrl);
		removeAsListenerAndDispose(cmc);
		citationCtrl = null;
		cmc = null;
	}
	
	private void doEdit(UserRequest ureq) {
		citationCtrl = new UpdateTextVersionController(ureq, getWindowControl(), mediaVersion);
		listenTo(citationCtrl);

		String modalTitle = translate("artefact.citation");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), citationCtrl.getInitialComponent(), true, modalTitle, true);
		listenTo(cmc);
		cmc.activate();
	}
}
