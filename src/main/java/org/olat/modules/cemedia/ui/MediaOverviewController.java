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

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.CaretPosition;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.modules.cemedia.MediaCenterLicenseHandler;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaHandler.CreateVersion;
import org.olat.modules.cemedia.MediaHandlerUISettings;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MetadataXStream;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.ui.component.MediaRelationsCellRenderer;
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

	private FormLink setVersionButton;
	private FormLink createVersionButton;
	private FormLink uploadVersionButton;
	private FormLink restoreVersionButton;
	private DropdownItem versionDropdownItem;
	private Link gotoOriginalLink;
	private FormLayoutContainer metaCont;
	
	private Controller mediaCtrl;
	private Controller addVersionCtrl;
	private MediaLogController logCtrl;
	private CloseableModalController cmc;
	
	private int counter;
	private boolean logsOpen = true;
	private Media media;
	private MediaVersion currentVersion;
	private MediaVersion selectedVersion;
	private boolean editable = true;
	private final MediaHandler handler;
	private final List<MediaUsage> usageList;
	private final MediaHandlerUISettings uiSettings;

	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private MediaCenterLicenseHandler licenseHandler;
	
	public MediaOverviewController(UserRequest ureq, WindowControl wControl, Media media, MediaVersion currentVersion, List<MediaUsage> usageList, boolean editable) {
		super(ureq, wControl, "media_overview", Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()));
		this.media = media;
		this.editable = editable;
		this.usageList = usageList;
		this.currentVersion = currentVersion;
		this.selectedVersion = currentVersion;
		handler = mediaService.getMediaHandler(media.getType());
		uiSettings = handler.getUISettings();
		
		logCtrl = new MediaLogController(ureq, getWindowControl(), mainForm, media);
		listenTo(logCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		versionDropdownItem = uifactory.addDropdownMenu("versions.list", "versions.current", null, formLayout, getTranslator());
		versionDropdownItem.setIconCSS("o_icon o_icon_version");
		versionDropdownItem.setOrientation(DropdownOrientation.right);
		versionDropdownItem.setButton(true);
		versionDropdownItem.setVisible(uiSettings.hasVersion());
		loadVersions();
		
		if(editable && uiSettings.hasVersion()) {
			setVersionButton = uifactory.addFormLink("set.version", "set.version", null, formLayout, Link.BUTTON);
			setVersionButton.setIconLeftCSS("o_icon o_icon_add");
			restoreVersionButton = uifactory.addFormLink("restore.version", "restore.version", null, formLayout, Link.BUTTON);
			restoreVersionButton.setIconLeftCSS("o_icon o_icon_refresh");
			
			if(uiSettings.canCreateVersion()) {
				createVersionButton = uifactory.addFormLink("create.version", "create.version." + handler.getType(), null, formLayout, Link.BUTTON);
				String createIconCssClass = uiSettings.createIconCssClass();
				if(!StringHelper.containsNonWhitespace(uiSettings.createIconCssClass())) {
					createIconCssClass = "o_icon_add";
				}
				createVersionButton.setIconLeftCSS("o_icon " + createIconCssClass);
			}
			if(uiSettings.canUploadVersion()) {
				uploadVersionButton = uifactory.addFormLink("upload.version", "upload.version." + handler.getType(), null, formLayout, Link.BUTTON);
				String addIconCssClass = uiSettings.uploadIconCssClass();
				if(!StringHelper.containsNonWhitespace(uiSettings.uploadIconCssClass())) {
					addIconCssClass = "o_icon_refresh";
				}
				uploadVersionButton.setIconLeftCSS("o_icon " + addIconCssClass);
			}
		}
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("logsOpen", Boolean.valueOf(logsOpen));
		}
		
		formLayout.add("logs", logCtrl.getInitialFormItem());
		loadLogs();
		
		String metaPage = velocity_root + "/media_details_metadata.html";
		metaCont = uifactory.addCustomFormLayout("meta", null, metaPage, formLayout);
		
		loadModels(metaCont);
		updateVersion(ureq, selectedVersion);
		
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

	public MediaWithVersion reload() {
		media = mediaService.getMediaByKey(media.getKey());
		if(media.getVersions() != null && !media.getVersions().isEmpty()) {
			currentVersion = media.getVersions().get(0);
		}
		loadModels(metaCont);
		loadVersions();
		loadLogs();
		return new MediaWithVersion(media, currentVersion, null, -1l);
	}
	
	private void loadVersions() {
		versionDropdownItem.removeAllFormItems();
		
		List<MediaVersion> versions = media.getVersions();
		for(int i=0; i<versions.size(); i++) {
			MediaVersion version = versions.get(i);
			String versionName;
			if(i == 0) {
				versionName = translate("versions.current");
			} else if(version.getCollectionDate() == null) {
				versionName = translate("versions.selected.nodate", version.getVersionName());
			} else {
				String collectionDate = Formatter.getInstance(getLocale()).formatDate(version.getCollectionDate());
				versionName = translate("versions.selected", version.getVersionName(), collectionDate);
			}
			FormLink versionLink = uifactory.addFormLink("version." + version.getKey(), "version", versionName, null, flc, Link.LINK | Link.NONTRANSLATED); 
			versionLink.setUserObject(versions.get(i));
			versionDropdownItem.addElement(versionLink);
		}
		
		versionDropdownItem.setVisible(versionDropdownItem.size() > 1);
	}
	
	private void loadLogs() {
		logCtrl.loadModel();
		logCtrl.getInitialFormItem().setVisible(uiSettings.viewLogs() && versionDropdownItem.size() > 1);
	}
	
	private void updateVersion(UserRequest ureq, MediaVersion version) {
		if(version == null) return;
		
		selectedVersion = version;

		boolean lastVersion = selectedVersion.equals(currentVersion);
		boolean mediaEditable = editable && lastVersion;
		mediaCtrl = handler.getMediaController(ureq, getWindowControl(), selectedVersion, new StandardMediaRenderingHints(mediaEditable));
		
		if(setVersionButton != null) {
			setVersionButton.setVisible(editable && lastVersion);
		}
		if(restoreVersionButton != null) {
			restoreVersionButton.setVisible(editable && !lastVersion);
		}
		if(createVersionButton != null) {
			createVersionButton.setVisible(editable && lastVersion);
		}
		if(uploadVersionButton != null) {
			uploadVersionButton.setVisible(editable && lastVersion);
		}
		
		if(mediaCtrl != null) {
			if(version.getMetadata() != null && version.getMetadata().getFileSize() >= 0) {
				metaCont.contextPut("fileSize", Formatter.formatBytes(version.getMetadata().getFileSize()));
			} else {
				metaCont.contextRemove("fileSize");
			}
			
			if(version.getCollectionDate() != null) {
				String collectionDate = Formatter.getInstance(getLocale()).formatDate(version.getCollectionDate());
				metaCont.contextPut("collectionDate", collectionDate);
				if(lastVersion) {
					versionDropdownItem.setTranslatedLabel(translate("versions.current"));
				} else {
					versionDropdownItem.setTranslatedLabel(translate("versions.selected", version.getVersionName(), collectionDate));
				}
			} else if(lastVersion) {
				versionDropdownItem.setTranslatedLabel(translate("versions.current"));
			} else {
				versionDropdownItem.setTranslatedLabel(translate("versions.selected.nodate", version.getVersionName()));
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
		
		// License
		if (licenseModule.isEnabled(licenseHandler)) {
			License license = licenseService.loadOrCreateLicense(media);
			LicenseType licenseType = license.getLicenseType();
			if (!licenseService.isNoLicense(licenseType)) {
				metaCont.contextPut("license", LicenseUIFactory.translate(licenseType, getLocale()));
				metaCont.contextPut("licenseIconCss", LicenseUIFactory.getCssOrDefault(licenseType));
				String licensor = StringHelper.containsNonWhitespace(license.getLicensor())? license.getLicensor(): "";
				metaCont.contextPut("licensor", licensor);
				metaCont.contextPut("licenseText", LicenseUIFactory.getFormattedLicenseText(license));	
			}
		} 
		
		List<TagInfo> tagInfos = mediaService.getTagInfos(media, getIdentity(), true);
		List<String> tags = tagInfos.stream()
				.map(TagInfo::getDisplayName)
				.toList();
		metaCont.contextPut("tags", tags);
		
		List<TaxonomyLevel> levels = mediaService.getTaxonomyLevels(media);
		List<String> levelsNames = levels.stream()
				.map(level ->  TaxonomyUIFactory.translateDisplayName(getTranslator(), level))
				.toList();
		metaCont.contextPut("taxonomyLevels", levelsNames);
		
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
		} else if ("ONCLICK".equals(event.getCommand())) {
			String logsOpenVal = ureq.getParameter("logsOpen");
			if (StringHelper.containsNonWhitespace(logsOpenVal)) {
				logsOpen = Boolean.parseBoolean(logsOpenVal);
				flc.getContext().put("logsOpen", logsOpen);// No dirty, action done by JS
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(mediaCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadLogs();
			}
		} else if(addVersionCtrl == source) {
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
		} else if(setVersionButton == source) {
			doSetVersion(ureq);
		} else if(restoreVersionButton == source) {
			doRestoreVersion(ureq);
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
	
	private void doSetVersion(UserRequest ureq) {
		media = mediaService.getMediaByKey(media.getKey());
		media = mediaService.setVersion(media, getIdentity());
		reload();
		updateVersion(ureq, currentVersion);
	}
	
	private void doRestoreVersion(UserRequest ureq) {
		media = mediaService.getMediaByKey(media.getKey());
		media = mediaService.restoreVersion(media, selectedVersion);
		reload();
		updateVersion(ureq, currentVersion);
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