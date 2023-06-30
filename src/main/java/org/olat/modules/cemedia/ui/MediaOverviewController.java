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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.CaretPosition;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.model.StandardMediaRenderingHints;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaHandlerVersion;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaHandler.CreateVersion;
import org.olat.modules.cemedia.manager.MetadataXStream;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.ui.component.MediaRelationsCellRenderer;
import org.olat.modules.cemedia.ui.medias.FileMediaController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaOverviewController extends FormBasicController implements Activateable2 {

	private FormLink createVersionButton;
	private FormLink uploadVersionButton;
	private DropdownItem versionDropdownItem;
	private Link gotoOriginalLink;
	private FormLayoutContainer metaCont;
	
	private Controller mediaCtrl;
	private Controller addVersionCtrl;
	private CloseableModalController cmc;
	
	private int counter;
	private Media media;
	private MediaVersion currentVersion;
	private boolean editable = true;
	private final MediaHandler handler;
	private final List<MediaUsage> usageList;

	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	
	public MediaOverviewController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion currentVersion, List<MediaUsage> usageList, boolean editable) {
		super(ureq, wControl, "media_overview", Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()));
		this.media = media;
		this.editable = editable;
		this.usageList = usageList;
		this.currentVersion = currentVersion;
		handler = mediaService.getMediaHandler(media.getType());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("title", StringHelper.escapeHtml(media.getTitle()));
			layoutCont.contextPut("description", StringHelper.xssScan(media.getDescription()));
			layoutCont.contextPut("iconCssClass", handler.getIconCssClass(currentVersion));
			updateVersion(ureq, currentVersion);
		}
		
		MediaHandlerVersion handlerVersion = handler.hasVersion();
		if(editable && handlerVersion.hasVersion()) {
			if(handlerVersion.canCreateVersion()) {
				createVersionButton = uifactory.addFormLink("create.version", "create.version." + handler.getType(), null, formLayout, Link.BUTTON);
				String createIconCssClass = handlerVersion.createIconCssClass();
				if(!StringHelper.containsNonWhitespace(handlerVersion.createIconCssClass())) {
					createIconCssClass = "o_icon_add";
				}
				createVersionButton.setIconLeftCSS("o_icon " + createIconCssClass);
			}
			if(handlerVersion.canUploadVersion()) {
				uploadVersionButton = uifactory.addFormLink("upload.version", "upload.version." + handler.getType(), null, formLayout, Link.BUTTON);
				String addIconCssClass = handlerVersion.uploadIconCssClass();
				if(!StringHelper.containsNonWhitespace(handlerVersion.uploadIconCssClass())) {
					addIconCssClass = "o_icon_refresh";
				}
				uploadVersionButton.setIconLeftCSS("o_icon " + addIconCssClass);
			}
			versionDropdownItem = uifactory.addDropdownMenu("versions.list", "versions.list", null, formLayout, getTranslator());
			versionDropdownItem.setIconCSS("o_icon o_icon_version");
			versionDropdownItem.setOrientation(DropdownOrientation.right);
			versionDropdownItem.setButton(true);
			loadVersions();
		}

		String metaPage = velocity_root + "/media_details_metadata.html";
		metaCont = uifactory.addCustomFormLayout("meta", null, metaPage, formLayout);
		loadModels(metaCont);
		
		int numOfReferences = usageList.size();
		List<FormLink> referencesLinks = new ArrayList<>(numOfReferences);
		for(int i=0; i<numOfReferences && i<3; i++) {
			FormLink link = forgeReferenceLink(usageList.get(i));
			referencesLinks.add(link);
		}
		metaCont.contextPut("referencesLinks", referencesLinks);
		if(numOfReferences > 3) {
			DropdownItem moreReferencesDropdown = uifactory.addDropdownMenu("reference.more", "reference.more", null, metaCont, getTranslator());
			moreReferencesDropdown.setButton(false);
			moreReferencesDropdown.setCaretPosition(CaretPosition.none);
			moreReferencesDropdown.setAriaLabel(translate("reference.more"));
			moreReferencesDropdown.setTranslatedLabel(translate("reference.more.number", Integer.toString(numOfReferences - 3)));
			
			for(int i=3; i<numOfReferences; i++) {
				FormLink link = forgeReferenceLink(usageList.get(i));
				moreReferencesDropdown.addElement(link);
			}
		}
	}
	
	private FormLink forgeReferenceLink(MediaUsage usedIn) {
		FormLink link = uifactory.addFormLink("ref_" + (++counter), "page", usedIn.pageTitle(), null, metaCont, Link.LINK | Link.NONTRANSLATED); 
		link.setIconLeftCSS("o_icon o_icon-fw o_page_icon");
		link.setUserObject(usedIn);
		
		if(usedIn.repositoryEntryKey() != null) {
			link.setTooltip(translate("reference.tooltip.repository", usedIn.repositoryEntryDisplayname()));
		} else if(usedIn.binderKey() != null) {
			link.setTooltip(translate("reference.tooltip.binder", usedIn.binderTitle()));
		}
		return link;
	}

	public void reload() {
		media = mediaService.getMediaByKey(media.getKey());
		if(media.getVersions() != null && !media.getVersions().isEmpty()) {
			currentVersion = media.getVersions().get(0);
		}
		loadModels(metaCont);
		loadVersions();
	}
	
	private void loadVersions() {
		if(!editable) return;
		
		versionDropdownItem.removeAllFormItems();
		
		List<MediaVersion> versions = media.getVersions();
		for(int i=0; i<versions.size(); i++) {
			MediaVersion version = versions.get(i);
			String versionName;
			if(i == 0) {
				versionName = translate("last.version." + handler.getType());
			} else {
				versionName = translate("version." + handler.getType(), version.getVersionName());
			}
			FormLink versionLink = uifactory.addFormLink("version." + version.getKey(), "version", versionName, null, flc, Link.LINK | Link.NONTRANSLATED); 
			versionLink.setUserObject(versions.get(i));
			versionDropdownItem.addElement(versionLink);
		}
		
		versionDropdownItem.setVisible(versionDropdownItem.size() > 1);
	}
	
	private void updateVersion(UserRequest ureq, MediaVersion version) {
		mediaCtrl = handler.getMediaController(ureq, getWindowControl(), version, new StandardMediaRenderingHints());
		if(mediaCtrl != null) {
			// Move this to the MediaHandler if even more Media types are editable inline.
			if (mediaCtrl instanceof FileMediaController fileMediaCtrl && editable) {
				fileMediaCtrl.setEditable(editable);
			}
			listenTo(mediaCtrl);
			flc.put("media", mediaCtrl.getInitialComponent());
		}
	}
	
	private void loadModels(FormLayoutContainer container) {
		metaCont.contextPut("media", media);
		String author = userManager.getUserDisplayName(media.getAuthor());
		metaCont.contextPut("author", author);
		
		if(media.getCollectionDate() != null) {
			String collectionDate = Formatter.getInstance(getLocale()).formatDate(media.getCollectionDate());
			metaCont.contextPut("collectionDate", collectionDate);
		}
		
		if (MediaUIHelper.showBusinessPath(media.getBusinessPath())) {
			gotoOriginalLink = LinkFactory.createLink("goto.original", metaCont.getFormItemComponent(), this);
		}
		
		if(StringHelper.containsNonWhitespace(media.getMetadataXml())) {
			Object metadata = MetadataXStream.get().fromXML(media.getMetadataXml());
			metaCont.contextPut("metadata", metadata);
		}
		
		List<TagInfo> tagInfos = mediaService.getTagInfos(media);
		if(tagInfos != null && !tagInfos.isEmpty()) {
			List<TextBoxItem> tagsMap = tagInfos.stream()
					.map(cat -> new TextBoxItemImpl(cat.getDisplayName(), cat.getDisplayName()))
					.collect(Collectors.toList());
			TextBoxListElement tagsEl = uifactory.addTextBoxListElement("tags", "tags", "categories.hint", tagsMap, container, getTranslator());
			tagsEl.setHelpText(translate("categories.hint"));
			tagsEl.setElementCssClass("o_sel_ep_tagsinput");
			tagsEl.setEnabled(false);
		}
		
		List<TaxonomyLevel> levels = mediaService.getTaxonomyLevels(media);
		if(levels != null && !levels.isEmpty()) {
			List<TextBoxItem> levelsMap = levels.stream()
					.map(level -> {
						String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), level);
						return new TextBoxItemImpl(displayName, displayName);
					})
					.collect(Collectors.toList());
			TextBoxListElement taxonomylevelsEl = uifactory.addTextBoxListElement("taxonomy.level", "taxonomy.level", null, levelsMap, container, getTranslator());
			taxonomylevelsEl.setEnabled(false);
		}
		
		List<MediaShare> shares = mediaService.getMediaShares(media);
		final MediaRelationsCellRenderer shareRenderer = new MediaRelationsCellRenderer(userManager);
		List<Share> sharesList = shares.stream()
				.map(share ->  new Share(shareRenderer.getIconCssClass(share), StringHelper.escapeHtml(shareRenderer.getDisplayName(share))))
				.toList();
		container.contextPut("sharesList", sharesList);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(gotoOriginalLink == source) {
			NewControllerFactory.getInstance().launch(media.getBusinessPath(), ureq, getWindowControl());	
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(addVersionCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doUpdateVersions(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addVersionCtrl);
		removeAsListenerAndDispose(cmc);
		addVersionCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(createVersionButton == source) {
			doCreateVersion(ureq);
		} else if(uploadVersionButton == source) {
			doUploadVersion(ureq);
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			Object uobject = link.getUserObject();
			if("page".equals(cmd) && uobject instanceof MediaUsage mediaUsage) {
				MediaUIHelper.open(ureq, getWindowControl(), mediaUsage);
			} else if("version".equals(cmd) && uobject instanceof MediaVersion version) {
				doChangeVersion(ureq, version);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doCreateVersion(UserRequest ureq) {
		String title = translate("create.version." + handler.getType());
		doAddVersion(ureq, CreateVersion.CREATE, title);
	}
	
	private void doUploadVersion(UserRequest ureq) {
		String title = translate("upload.version." + handler.getType());
		doAddVersion(ureq, CreateVersion.UPLOAD, title);
	}
	
	private void doAddVersion(UserRequest ureq, CreateVersion createType, String modalTitle) {
		addVersionCtrl = handler.getNewVersionController(ureq, getWindowControl(), media, createType);
		listenTo(addVersionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addVersionCtrl.getInitialComponent(), true, modalTitle, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChangeVersion(UserRequest ureq, MediaVersion version) {
		removeAsListenerAndDispose(mediaCtrl);
		updateVersion(ureq, version);
	}
	
	private void doUpdateVersions(UserRequest ureq) {
		reload();
		removeAsListenerAndDispose(mediaCtrl);
		updateVersion(ureq, currentVersion);
	}
	
	public record Share(String iconCssClass, String displayName) {
		//
	}
}