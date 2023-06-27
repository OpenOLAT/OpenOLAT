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
package org.olat.modules.cemedia.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageStatus;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.ui.event.MediaEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDetailsController extends BasicController implements Activateable2 {
	
	private final Link editLink;
	private final Link deleteLink;
	private final TabbedPane tabbedPane;
	
	private Controller metadataCtrl;
	private Controller mediaEditCtrl;
	private CloseableModalController cmc;
	private MediaUsageController usageCtrl;
	private MediaRelationsController relationsCtrl;
	private DialogBoxController confirmDeleteMediaCtrl;
	private final MediaOverviewController overviewCtrl;

	private Media media;
	private boolean editable;
	private final MediaHandler handler;
	private final List<MediaUsage> usageList;
	
	@Autowired
	private MediaService mediaService;
	
	public MediaDetailsController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion currentVersion) {
		super(ureq, wControl);
		
		this.media = media;
		handler = mediaService.getMediaHandler(media.getType());
		
		usageList = mediaService.getMediaUsage(media);
		for(MediaUsage mediaUsage:usageList) {
			if(mediaUsage.getPageStatus() == PageStatus.closed || mediaUsage.getPageStatus() == PageStatus.published) {
				editable = false;
			}
		}
		
		VelocityContainer mainVC = createVelocityContainer("media_details");
		
		mainVC.contextPut("title", StringHelper.escapeHtml(media.getTitle()));
		mainVC.contextPut("description", StringHelper.xssScan(media.getDescription()));
		mainVC.contextPut("iconCssClass", handler.getIconCssClass(currentVersion));
		
		Dropdown commandsDropdown = new Dropdown("commands", null, false, getTranslator());
		commandsDropdown.setDomReplaceable(false);
		commandsDropdown.setCarretIconCSS("o_icon o_icon_commands");
		commandsDropdown.setButton(true);
		commandsDropdown.setEmbbeded(true);
		commandsDropdown.setOrientation(DropdownOrientation.right);
		mainVC.put("commands", commandsDropdown);
		
		editLink = LinkFactory.createToolLink("edit", translate("edit"), this, "o_icon o_icon-lg o_icon_edit");
		commandsDropdown.addComponent(editLink);
		
		deleteLink = LinkFactory.createToolLink("delete", translate("delete"), this, "o_icon o_icon-lg o_icon_delete_item");
		commandsDropdown.addComponent(deleteLink);
		
		tabbedPane = new TabbedPane("pane", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabs", tabbedPane);
		
		overviewCtrl = new MediaOverviewController(ureq, getWindowControl(), media, currentVersion, usageList, editable);
		listenTo(overviewCtrl);
		tabbedPane.addTab(translate("tab.overview"), "o_sel_media_overview", overviewCtrl);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.metadata"), "o_sel_media_metadata", uureq -> {
			removeAsListenerAndDispose(metadataCtrl);
			metadataCtrl = handler.getEditMetadataController(uureq, getWindowControl(), media);
			listenTo(metadataCtrl);
			return metadataCtrl;
		}, false);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.usage"), "o_sel_media_metadata", uureq -> {
			removeAsListenerAndDispose(usageCtrl);
			usageCtrl = new MediaUsageController(uureq, getWindowControl(), media);
			listenTo(usageCtrl);
			return usageCtrl;
		}, false);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.relations"), "o_sel_media_relations", uureq -> {
			removeAsListenerAndDispose(relationsCtrl);
			relationsCtrl = new MediaRelationsController(uureq, getWindowControl(), media);
			listenTo(relationsCtrl);
			return relationsCtrl;
		}, false);
		
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteMediaCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq);
			}	
		} else if(mediaEditCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reload();
			}
			cmc.deactivate();
			cleanUp();
		} else if(relationsCtrl == source || overviewCtrl == source || metadataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reload();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(mediaEditCtrl);
		removeAsListenerAndDispose(cmc);
		mediaEditCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(editLink == source) {
			doEdit(ureq);
		} 
	}
	
	private void reload() {
		overviewCtrl.reload();
		if(this.usageCtrl != null) {
			usageCtrl.reload();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		String title = translate("delete.media.confirm.title");
		String text = translate("delete.media.confirm.descr", StringHelper.escapeHtml(media.getTitle()));
		confirmDeleteMediaCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteMediaCtrl);
		confirmDeleteMediaCtrl.setUserObject(media);
	}
	
	private void doDelete(UserRequest ureq) {
		mediaService.deleteMedia(media);
		fireEvent(ureq, new MediaEvent(MediaEvent.DELETED));
	}
	
	private void doEdit(UserRequest ureq) {
		if(guardModalController(mediaEditCtrl)) return;
		
		mediaEditCtrl = handler.getEditMediaController(ureq, getWindowControl(), media);
		listenTo(mediaEditCtrl);
		
		String title = translate("edit");
		cmc = new CloseableModalController(getWindowControl(), null, mediaEditCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
