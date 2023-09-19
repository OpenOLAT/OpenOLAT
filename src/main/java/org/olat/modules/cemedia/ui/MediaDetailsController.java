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

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
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
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.ui.event.MediaEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDetailsController extends BasicController implements Activateable2 {
	
	private final Link deleteLink;
	private final Link downloadLink;
	private final TabbedPane tabbedPane;
	private final VelocityContainer mainVC;
	
	private Controller metadataCtrl;
	private CloseableModalController cmc;
	private MediaUsageController usageCtrl;
	private MediaRelationsController relationsCtrl;
	private final MediaOverviewController overviewCtrl;
	private ConfirmDeleteMediaController confirmDeleteMediaCtrl;

	private Media media;
	private MediaVersion version;
	private VFSMetadata versionMetadata;
	private final boolean editable;
	private final MediaHandler handler;
	private final List<MediaUsage> usageList;

	@Autowired
	private MediaService mediaService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public MediaDetailsController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion currentVersion) {
		super(ureq, wControl);
		
		this.media = media;
		this.version = currentVersion;
		versionMetadata = currentVersion == null ? null : currentVersion.getMetadata();
		handler = mediaService.getMediaHandler(media.getType());
		
		usageList = mediaService.getMediaUsage(media);
		editable = mediaService.isMediaEditable(getIdentity(), media);
		
		mainVC = createVelocityContainer("media_details");
		loadTitle();
		
		Dropdown commandsDropdown = new Dropdown("commands", null, false, getTranslator());
		commandsDropdown.setDomReplaceable(false);
		commandsDropdown.setCarretIconCSS("o_icon o_icon_commands");
		commandsDropdown.setButton(true);
		commandsDropdown.setEmbbeded(true);
		commandsDropdown.setOrientation(DropdownOrientation.right);
		commandsDropdown.setVisible(editable);
		mainVC.put("commands", commandsDropdown);
		
		downloadLink = LinkFactory.createToolLink("download", translate("download"), this, "o_icon o_icon-lg o_icon_download");
		downloadLink.setVisible(editable && currentVersion != null);
		commandsDropdown.addComponent(downloadLink);
		
		deleteLink = LinkFactory.createToolLink("delete", translate("delete"), this, "o_icon o_icon-lg o_icon_delete_item");
		deleteLink.setVisible(editable);
		commandsDropdown.addComponent(deleteLink);
		
		tabbedPane = new TabbedPane("pane", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabs", tabbedPane);
		
		overviewCtrl = new MediaOverviewController(ureq, getWindowControl(), media, currentVersion, usageList, editable);
		listenTo(overviewCtrl);
		tabbedPane.addTab(translate("tab.overview"), "o_sel_media_overview", overviewCtrl);
		
		if(editable) {
			tabbedPane.addTabControllerCreator(ureq, translate("tab.metadata"), "o_sel_media_metadata", uureq -> {
				removeAsListenerAndDispose(metadataCtrl);
				metadataCtrl = handler.getEditMetadataController(uureq, getWindowControl(), media);
				listenTo(metadataCtrl);
				return metadataCtrl;
			}, false);
		}
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.usage"), "o_sel_media_metadata", uureq -> {
			removeAsListenerAndDispose(usageCtrl);
			usageCtrl = new MediaUsageController(uureq, getWindowControl(), media);
			listenTo(usageCtrl);
			return usageCtrl;
		}, false);
		
		tabbedPane.addTabControllerCreator(ureq, translate("tab.relations"), "o_sel_media_relations", uureq -> {
			removeAsListenerAndDispose(relationsCtrl);
			relationsCtrl = new MediaRelationsController(uureq, getWindowControl(), media, editable);
			listenTo(relationsCtrl);
			return relationsCtrl;
		}, false);
		
		
		putInitialPanel(mainVC);
	}
	
	public Media getMedia() {
		return media;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteMediaCtrl == source) {
			cleanup();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, new MediaEvent(MediaEvent.DELETED));
			}	
		} else if(relationsCtrl == source || overviewCtrl == source || metadataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reload();
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanup() {
		removeAsListenerAndDispose(confirmDeleteMediaCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteMediaCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(downloadLink == source) {
			doDownload(ureq);
		}
	}
	
	private void reload() {
		MediaWithVersion mediaWithVersion = overviewCtrl.reload();
		media = mediaWithVersion.media();
		version = mediaWithVersion.version();
		if(usageCtrl != null) {
			usageCtrl.reload();
		}
		loadTitle();
	}
	
	private void loadTitle() {
		mainVC.contextPut("title", StringHelper.escapeHtml(media.getTitle()));
		if(version != null) {
			mainVC.contextPut("iconCssClass", handler.getIconCssClass(version));
		}
	}
	
	private void doDownload(UserRequest ureq) {
		VFSItem item = vfsRepositoryService.getItemFor(versionMetadata);
		if(item instanceof VFSLeaf leaf) {
			VFSMediaResource vmr = new VFSMediaResource(leaf);
			vmr.setDownloadable(true);
			ureq.getDispatchResult().setResultingMediaResource(vmr);
		}
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		final List<Long> rowsKeysToDelete = mediaService.filterOwnedDeletableMedias(getIdentity(), List.of(media.getKey()));
		if(rowsKeysToDelete.isEmpty()) {
			long usages = mediaService.countMediaUsage(List.of(media));
			String i18nKey = usages == 1 ? "warning.not.deletable.singular" : "warning.not.deletable.plural";
			showWarning(i18nKey, new String[]{ media.getTitle(), Long.toString(usages) });
		} else {
			confirmDeleteMediaCtrl = new ConfirmDeleteMediaController(ureq, getWindowControl(), List.of(media));
			listenTo(confirmDeleteMediaCtrl);
			
			String title = translate("delete");
			cmc = new CloseableModalController(getWindowControl(), null, confirmDeleteMediaCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
}
