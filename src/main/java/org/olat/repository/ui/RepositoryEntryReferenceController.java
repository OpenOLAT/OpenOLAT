/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.IconPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.Confirm;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.SettingsContentProvider;
import org.olat.repository.ui.author.CreateEntryController;
import org.olat.repository.ui.author.ImportRepositoryEntryController;
import org.olat.repository.ui.author.ImportURLRepositoryEntryController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryReferenceController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryReferenceController.class);
	
	public static final Event PREVIEW_EVENT = new Event("preview");
	public static final Event SELECTION_EVENT = new Event("selection");
	
	private final VelocityContainer mainVC;
	private final IconPanel referencePanel;
	private final Link previewLink;
	private final Link selectLink;
	private final Link replaceSelectLink;
	private Component createCmp;
	private Link importLink;
	private Link replaceImportLink;
	private Link importUrlLink;
	private Link replaceImportUrlLink;
	private Link referencesHistoryLink;
	private final Link editLink;
	private final Link editSettingsLink;
	private final Dropdown commandsDropdown;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private CreateEntryController createCtrl;
	private ImportRepositoryEntryController importCtrl;
	private ImportURLRepositoryEntryController importUrlCtrl;
	private Controller referencesHistoryCtrl;
	private Controller editSettingsCtrl;
	
	private RepositoryEntry repositoryEntry;
	private final RepositoryEntryReferenceProvider referenceProvider;
	
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	public RepositoryEntryReferenceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, RepositoryEntryReferenceProvider guiConfig) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.repositoryEntry = repositoryEntry;
		this.referenceProvider = guiConfig;
		
		mainVC = createVelocityContainer("repository_entry_reference");
		putInitialPanel(mainVC);
		
		EmptyStateFactory.create("empty.state", mainVC, this, guiConfig.getEmptyStateConfig());
		
		referencePanel = new IconPanel("reference");
		referencePanel.setElementCssClass("o_block_bottom");
		mainVC.put("reference", referencePanel);

		commandsDropdown = new Dropdown("commands", null, true, getTranslator());
		commandsDropdown.setDomReplaceable(false);
		commandsDropdown.setButton(true);
		commandsDropdown.setOrientation(DropdownOrientation.right);
		mainVC.put(commandsDropdown.getComponentName(), commandsDropdown);
		
		previewLink = LinkFactory.createButton("preview", mainVC, this);
		previewLink.setElementCssClass("o_sel_re_reference_preview");
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		selectLink = LinkFactory.createButton("select", mainVC, this);
		selectLink.setElementCssClass("o_sel_re_reference_select");
		selectLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		selectLink.setPrimary(true);
		
		replaceSelectLink = LinkFactory.createButton("replace", mainVC, this);
		replaceSelectLink.setElementCssClass("o_sel_re_reference_replace_select");
		replaceSelectLink.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		
		String warningMessage = referenceProvider.getWarningMessage();
		if(StringHelper.containsNonWhitespace(warningMessage)) {
			referencePanel.setMessage(warningMessage);
			referencePanel.setMesssageIconCssClass("o_warning_with_icon");
		}
		
		if (guiConfig.canCreate()) {
			List<String> creatorTypes = new ArrayList<>( guiConfig.getResourceTypes().size());
			for (String limitType : guiConfig.getResourceTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(limitType);
				if (handler.supportCreate(getIdentity(), ureq.getUserSession().getRoles())) {
					creatorTypes.add(limitType);
				}
			}
			
			if (creatorTypes.size() == 1) {
				Link createLink = LinkFactory.createButton("create", mainVC, this);
				createLink.setElementCssClass("o_sel_re_reference_create");
				createLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				createLink.setSuppressDirtyFormWarning(true);
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(creatorTypes.get(0));
				createLink.setUserObject(handler);
				createCmp = createLink;
				
				Link replaceCreateLink = LinkFactory.createCustomLink("replace.create", "create", "create", Link.LINK, mainVC, this);
				replaceCreateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				replaceCreateLink.setUserObject(handler);
				commandsDropdown.addComponent(replaceCreateLink);
				
			} else if (creatorTypes.size() > 1) {
				Dropdown dropdown = new Dropdown("create", "cmd.create.ressource", false, getTranslator());
				dropdown.setElementCssClass("o_sel_repo_popup_create_resources");
				dropdown.setIconCSS("o_icon o_icon-fw o_icon_add");
				dropdown.setButton(true);
				dropdown.setOrientation(DropdownOrientation.right);
				dropdown.setDomReplaceable(false);
				dropdown.setEmbbeded(true);
				dropdown.setExpandContentHeight(true);
				mainVC.put(dropdown.getComponentName(), dropdown);
				
				for (String limitType : creatorTypes) {
					RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(limitType);
					Link createLink = LinkFactory.createLink(handler.getSupportedType(), getTranslator(), this);
					createLink.setUserObject(handler);
					dropdown.addComponent(createLink);
					
					Link replaceCreateLink = LinkFactory.createCustomLink("replace.create", "create", null, Link.LINK + Link.NONTRANSLATED, mainVC, this);
					String customDisplayText = translate("create.resource", translate(handler.getSupportedType()));
					replaceCreateLink.setCustomDisplayText(customDisplayText);
					replaceCreateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
					commandsDropdown.addComponent(replaceCreateLink);
				}
				createCmp = dropdown;
			}
		}
		
		if (guiConfig.canImport()) {
			if (isImportUrlLinkVisible()) {
				importUrlLink = LinkFactory.createButton("import.url", "cmd.import.url.ressource", mainVC, this);
				importUrlLink.setElementCssClass("o_sel_re_reference_import_url");
				importUrlLink.setIconLeftCSS("o_icon o_icon-fw o_icon_link");

				replaceImportLink = LinkFactory.createCustomLink("replace.import", "import", "import", Link.LINK, mainVC, this);
				replaceImportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
				commandsDropdown.addComponent(replaceImportLink);

				Dropdown importDropdown = new Dropdown("import.dropdown", null, true, getTranslator());
				importDropdown.setElementCssClass("o_sel_repo_import_url");
				importDropdown.setDomReplaceable(false);
				importDropdown.setButton(true);
				importDropdown.setOrientation(DropdownOrientation.right);
				mainVC.put(importDropdown.getComponentName(), importDropdown);

				importLink = LinkFactory.createCustomLink("import", "import", "import", Link.LINK, mainVC, this);
				importLink.setElementCssClass("o_sel_re_reference_import");
				importLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
				importDropdown.addComponent(importLink);

				replaceImportUrlLink = LinkFactory.createCustomLink("replace.import.url", "replace.import.url", "cmd.import.url.ressource", Link.LINK, mainVC, this);
				replaceImportUrlLink.setIconLeftCSS("o_icon o_icon-fw o_icon_link");
				commandsDropdown.addComponent(replaceImportUrlLink);
			} else if (isImportLinkVisible()) {
				importLink = LinkFactory.createButton("import", mainVC, this);
				importLink.setElementCssClass("o_sel_re_reference_import");
				importLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");

				replaceImportLink = LinkFactory.createCustomLink("replace.import", "import", "import", Link.LINK, mainVC, this);
				replaceImportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
				commandsDropdown.addComponent(replaceImportLink);
			}
		}
		
		referencesHistoryLink = LinkFactory.createCustomLink("references.history", "references.history", "references.history", Link.LINK, mainVC, this);
		referencesHistoryLink.setIconLeftCSS("o_icon o_icon-fw o_icon_history");
		commandsDropdown.addComponent(new Spacer("historySpace"));
		commandsDropdown.addComponent(referencesHistoryLink);
		
		editLink = LinkFactory.createButton("edit.resource", mainVC, this);
		editLink.setElementCssClass("o_sel_re_reference_edit");
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editLink.setGhost(true);
		
		editSettingsLink = LinkFactory.createButton("edit.settings", mainVC, this);
		editSettingsLink.setElementCssClass("o_sel_re_settings_edit");
		editSettingsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		editSettingsLink.setGhost(true);
		
		updateUI(ureq);
	}

	private void updateUI(UserRequest ureq) {
		mainVC.contextPut("repositoryEntryAvailable", Boolean.valueOf(repositoryEntry != null));
		
		referencePanel.removeAllLinks();
		
		String warningMessage = referenceProvider.getWarningMessage();
		if(StringHelper.containsNonWhitespace(warningMessage)) {
			referencePanel.setMessage(warningMessage);
			referencePanel.setMesssageIconCssClass("o_warning_with_icon");
		}
		
		boolean hasHistory = referenceProvider.hasReferencesHistory();
		referencesHistoryLink.setVisible(hasHistory);
		referencesHistoryLink.setEnabled(hasHistory);
		
		if (repositoryEntry != null) {
			
			referencePanel.setTitle(repositoryEntry.getDisplayname());
			referencePanel.setTagline(repositoryEntry.getExternalRef());
			referencePanel.setIconCssClass(referenceProvider.getIconCssClass(repositoryEntry));
			referencePanel.setContent(referenceProvider.getReferenceContentProvider().getContent(repositoryEntry));
			
			SettingsContentProvider settingsProvider = referenceProvider.getSettingsContentProvider();
			if(referenceProvider.hasSettings() && settingsProvider != null) {
				referencePanel.setAdditionalContent(settingsProvider.getSettingsContent(repositoryEntry));
			}
			
			replaceSelectLink.setEnabled(referenceProvider.isReplaceable(repositoryEntry));
			
			RepositoryHandler typeToEdit = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);
			if (typeToEdit.supportsEdit(repositoryEntry.getOlatResource(), getIdentity(), ureq.getUserSession().getRoles()) != EditionSupport.no) {
				if (referenceProvider.isEditable(repositoryEntry, getIdentity())) {
					referencePanel.addLink(editLink);
				}
				
				if(referenceProvider.hasSettings() && settingsProvider != null
						&& referenceProvider.isSettingsEditable(repositoryEntry, getIdentity())) {
					referencePanel.addAdditionalLink(editSettingsLink);
				}
			}	
		}
		
		boolean enabled = false;
		for(Component command:commandsDropdown.getComponents()) {
			enabled |= command.isEnabled();
		}
		commandsDropdown.setEnabled(enabled);
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(UserRequest ureq, RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
		updateUI(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == previewLink) {
			fireEvent(ureq, PREVIEW_EVENT);
		} else if (source == selectLink) {
			doSelectRepositoryEntry(ureq);
		} else if (source == replaceSelectLink) {
			doSelectRepositoryEntry(ureq);
		} else if (source == importLink) {
			doImportRepositoryEntry(ureq);
		} else if (source == replaceImportLink) {
			doImportRepositoryEntry(ureq);
		} else if (source == importUrlLink) {
			doImportUrlRepositoryEntry(ureq);
		} else if (source == replaceImportUrlLink) {
			doImportUrlRepositoryEntry(ureq);
		} else if (source == editLink) {
			doEditRepositoryEntry(ureq);
		} else if(source == createCmp || (source instanceof Link link && link.getUserObject() instanceof RepositoryHandler)) {
			doCreateRepositoryEntry(ureq, (RepositoryHandler)((Link)source).getUserObject());
		} else if (source == editSettingsLink) {
			doEditSettings(ureq);
		} else if(source == referencesHistoryLink) {
			doShowReferencesHistory(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				repositoryEntry = searchCtrl.getSelectedEntry();
				updateUI(ureq);
				cmc.deactivate();
				cleanUp();
				fireEvent(ureq, SELECTION_EVENT);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == createCtrl) {
			if (event.equals(Event.DONE_EVENT)) {
				repositoryEntry = createCtrl.getAddedEntry();
				updateUI(ureq);
				cmc.deactivate();
				cleanUp();
				fireEvent(ureq, SELECTION_EVENT);
			} else if (event.equals(Event.FAILED_EVENT)) {
				cmc.deactivate();
				cleanUp();
				showError("add.failed");
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == importCtrl) {
			if (event.equals(Event.DONE_EVENT)) {
				repositoryEntry = importCtrl.getImportedEntry();
				updateUI(ureq);
				cmc.deactivate();
				cleanUp();
				fireEvent(ureq, SELECTION_EVENT);
			} else if (event.equals(Event.FAILED_EVENT)) {
				cmc.deactivate();
				cleanUp();
				showError("add.failed");
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == importUrlCtrl) {
			if (event.equals(Event.DONE_EVENT)) {
				repositoryEntry = importUrlCtrl.getImportedEntry();
				updateUI(ureq);
				cmc.deactivate();
				cleanUp();
				fireEvent(ureq, SELECTION_EVENT);
			} else if (event.equals(Event.FAILED_EVENT)) {
				cmc.deactivate();
				cleanUp();
				showError("add.failed");
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(source == editSettingsCtrl) {
			if (event.equals(Event.DONE_EVENT)) {

				cmc.deactivate();
				cleanUp();
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(source == referencesHistoryCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(referencesHistoryCtrl);
		removeAsListenerAndDispose(importUrlCtrl);
		removeAsListenerAndDispose(importCtrl);
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(searchCtrl);
		removeAsListenerAndDispose(cmc);
		referencesHistoryCtrl = null;
		importUrlCtrl = null;
		importCtrl = null;
		createCtrl = null;
		searchCtrl = null;
		cmc = null;
	}
	
	private boolean isImportLinkVisible() {
		if(!referenceProvider.canImport()) return false;

		boolean importAllowed = false;
		Set<String> supportedTypes = repositoryHandlerFactory.getSupportedTypes();
		for(String supportedType:supportedTypes) {
			if(repositoryHandlerFactory.getRepositoryHandler(supportedType).supportImport() && referenceProvider.getResourceTypes().contains(supportedType)) {
				importAllowed = true;
				break;
			}
		}
		return importAllowed;
	}
	
	private boolean isImportUrlLinkVisible() {
		if(!referenceProvider.canImport()) return false;

		boolean importAllowed = false;
		Set<String> supportedTypes = repositoryHandlerFactory.getSupportedTypes();
		for(String supportedType:supportedTypes) {
			if(repositoryHandlerFactory.getRepositoryHandler(supportedType).supportImportUrl() && referenceProvider.getResourceTypes().contains(supportedType)) {
				importAllowed = true;
				break;
			}
		}
		return importAllowed;
	}

	private void doSelectRepositoryEntry(UserRequest ureq) {
		Confirm confirmation = referenceProvider.confirmCanReplace(ureq);
		if(confirmation.canReplace()) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(searchCtrl);
			
			// The commandLabel is used to get the stored prefs, but it is never displayed in the gui?!
			searchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
					referenceProvider.getResourceTypes().stream().toArray(String[]::new), "keep.prefs");
			listenTo(searchCtrl);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), searchCtrl.getInitialComponent(),
					true, referenceProvider.getSelectionTitle());
			listenTo(cmc);
			cmc.activate();
		} else {
			getWindowControl().setWarning(confirmation.errorMessage());
		}
	}
	
	private void doCreateRepositoryEntry(UserRequest ureq, RepositoryHandler handler) {
		Confirm confirmation = referenceProvider.confirmCanReplace(ureq);
		if(confirmation.canReplace()) {
			removeAsListenerAndDispose(createCtrl);
			createCtrl = handler.createCreateRepositoryEntryController(ureq, getWindowControl(), false);
			listenTo(createCtrl);
			
			String title = translate(handler.getCreateLabelI18nKey());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(), true, title);
			cmc.setCustomWindowCSS("o_sel_author_create_popup");
			listenTo(cmc);
			cmc.activate();
		} else {
			getWindowControl().setWarning(confirmation.errorMessage());
		}
	}
	
	private void doImportRepositoryEntry(UserRequest ureq) {
		Confirm confirmation = referenceProvider.confirmCanReplace(ureq);
		if(confirmation.canReplace()) {
			removeAsListenerAndDispose(importCtrl);
			importCtrl = new ImportRepositoryEntryController(ureq, getWindowControl(), referenceProvider.getResourceTypes().stream().toArray(String[]::new));
			listenTo(importCtrl);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), importCtrl.getInitialComponent(), true, "");
			listenTo(cmc);
			cmc.activate();
		} else {
			getWindowControl().setWarning(confirmation.errorMessage());
		}
	}
	
	private void doImportUrlRepositoryEntry(UserRequest ureq) {
		Confirm confirmation = referenceProvider.confirmCanReplace(ureq);
		if(confirmation.canReplace()) {
			removeAsListenerAndDispose(importCtrl);
			
			String[] resourceTypes = null;
			List<String> resourceTypesList = referenceProvider.getResourceTypes();
			if(resourceTypesList != null && !resourceTypesList.isEmpty()) {
				resourceTypes = resourceTypesList.toArray(new String[resourceTypesList.size()]);
			}
			importUrlCtrl = new ImportURLRepositoryEntryController(ureq, getWindowControl(), resourceTypes);
			listenTo(importUrlCtrl);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), importUrlCtrl.getInitialComponent(), true, "");
			listenTo(cmc);
			cmc.activate();
		} else {
			getWindowControl().setWarning(confirmation.errorMessage());
		}
	}
	
	private void doShowReferencesHistory(UserRequest ureq) {
		removeAsListenerAndDispose(referencesHistoryCtrl);
		removeAsListenerAndDispose(cmc);
		
		referencesHistoryCtrl = referenceProvider.getReferencesHistoryController(ureq, getWindowControl());
		listenTo(referencesHistoryCtrl);
		
		String title = null;
		if(referencesHistoryCtrl instanceof FormBasicController formCtrl) {
			title = formCtrl.getAndRemoveFormTitle();
		}
		if(title == null) {	
			title = translate("references.history.title");
		}
		cmc = new CloseableModalController(getWindowControl(), translate("close"), referencesHistoryCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditRepositoryEntry(UserRequest ureq) {
		if (repositoryEntry == null) {
			return;
		}
		
		try {
			String businessPath = "[RepositoryEntry:" + repositoryEntry.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void doEditSettings(UserRequest ureq) {
		editSettingsCtrl = referenceProvider.getSettingsContentProvider()
				.getEditSettingsController(ureq, getWindowControl(), repositoryEntry);
		listenTo(editSettingsCtrl);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editSettingsCtrl.getInitialComponent(), true, "");
		listenTo(cmc);
		cmc.activate();
	}
}
