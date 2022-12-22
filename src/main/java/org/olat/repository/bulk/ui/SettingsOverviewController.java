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
package org.olat.repository.bulk.ui;

import static org.olat.core.util.StringHelper.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.RepositoryBulkService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.model.SettingsContext.Replacement;
import org.olat.repository.bulk.model.SettingsSteps;
import org.olat.repository.bulk.model.SettingsSteps.Step;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SettingsOverviewController extends StepFormBasicController {

	private static final String CMD_RESOURCE = "resource";
	
	private FormLayoutContainer overviewCont;

	private CloseableCalloutWindowController calloutCtrl;
	
	private final SettingsSteps steps;
	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final int totalRepositoryEntries;
	private int counter = 0;
	
	@Autowired
	private RepositoryBulkService repositoryBulkService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;

	public SettingsOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, SettingsSteps steps) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.steps = steps;
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		this.editables = repositoryBulkService.getSettingsBulkEditables(context.getRepositoryEntries());
		this.totalRepositoryEntries = context.getRepositoryEntries().size();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("settings.bulk.overview.title");
		setFormInfo("noTransOnlyParam",
				new String[] {RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), null)});
		
		overviewCont = FormLayoutContainer.createCustomFormLayout("overview", getTranslator(), velocity_root + "/settings_bulk_overview.html");
		overviewCont.setRootForm(mainForm);
		formLayout.add(overviewCont);
		
		List<OverviewStep> overviewSteps = new ArrayList<>((int)steps.size());
		if (steps.contains(Step.metadata)) {
			List<OverviewField> fields = new ArrayList<>(4);
			if (context.isSelected(SettingsBulkEditable.authors)) {
				String text = translate("settings.bulk.overview.authors", context.getAuthors());
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.authors);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.educationalType)) {
				RepositoryEntryEducationalType educationalType = repositoryManager.getEducationalType(context.getEducationalTypeKey());
				String text = educationalType == null
						? translate("settings.bulk.overview.educational.type.none")
						: translate("settings.bulk.overview.educational.type", translate(RepositoyUIFactory.getI18nKey(educationalType)));
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.educationalType);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.mainLanguage)) {
				String text = translate("settings.bulk.overview.mainLanguage", context.getMainLanguage());
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.mainLanguage);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.expenditureOfWork)) {
				String text = translate("settings.bulk.overview.expenditureOfWork", context.getExpenditureOfWork());
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.expenditureOfWork);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.license)) {
				String licensor = Objects.requireNonNullElse(context.getLicensor(), "-");
				String licensorText = translate("settings.bulk.overview.licensor", licensor);
				LicenseType licenseType = licenseService.loadLicenseTypeByKey(context.getLicenseTypeKey());
				String text = null;
				if (licenseService.isFreetext(licenseType)) {
					text = translate("settings.bulk.overview.license.freetext", licenseType.getName(), Formatter.truncate(context.getFreetext(), 60), licensorText);
				} else if (licenseType != null) {
					text = translate("settings.bulk.overview.license.regular", LicenseUIFactory.translate(licenseType, getLocale()), licensorText);
				} else {
					text = translate("settings.bulk.overview.license.none");
				}
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.license);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (fields.isEmpty()) {
				fields.add(new OverviewField(translate("settings.bulk.overview.none"), null));
			}
			OverviewStep step = new OverviewStep(translate("settings.bulk.metadata.title"), fields);
			overviewSteps.add(step);
		}
		
		if (steps.contains(Step.taxonomy)) {
			List<OverviewField> fields = new ArrayList<>();
			if (context.isSelected(SettingsBulkEditable.taxonomyLevelsAdd)) {
				List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevelsByKeys(context.getTaxonomyLevelAddKeys());
				taxonomyLevels.sort((l1, l2) -> TaxonomyUIFactory.translateDisplayName(getTranslator(), l1, EMPTY)
						.compareToIgnoreCase(TaxonomyUIFactory.translateDisplayName(getTranslator(), l2, EMPTY)));
				for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
					String text = translate("settings.bulk.overview.taxonomy.add", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
					List<RepositoryEntry> changes = editables.getTaxonomyLevelAddChanges(taxonomyLevel.getKey());
					String resourceItemName = createResourceLink(changes);
					fields.add(new OverviewField(text, resourceItemName));
				}
			}
			if (context.isSelected(SettingsBulkEditable.taxonomyLevelsRemove)) {
				List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevelsByKeys(context.getTaxonomyLevelRemoveKeys());
				taxonomyLevels.sort((l1, l2) -> TaxonomyUIFactory.translateDisplayName(getTranslator(), l1, EMPTY)
						.compareToIgnoreCase(TaxonomyUIFactory.translateDisplayName(getTranslator(), l2, EMPTY)));
				for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
					String text = translate("settings.bulk.overview.taxonomy.remove", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
					List<RepositoryEntry> changes = editables.getTaxonomyLevelRemoveChanges(taxonomyLevel.getKey());
					String resourceItemName = createResourceLink(changes);
					fields.add(new OverviewField(text, resourceItemName));
				}
			}
			if (fields.isEmpty()) {
				fields.add(new OverviewField(translate("settings.bulk.overview.none"), null));
			}
			OverviewStep step = new OverviewStep(translate("settings.bulk.taxonomy.title"), fields);
			overviewSteps.add(step);
		}
		
		if (steps.contains(Step.organisation)) {
			List<OverviewField> fields = new ArrayList<>();
			if (context.isSelected(SettingsBulkEditable.organisationsAdd)) {
				Set<OrganisationRefImpl> organisationRefs = context.getOrganisationAddKeys().stream().map(OrganisationRefImpl::new).collect(Collectors.toSet());
				List<Organisation> organisations = organisationService.getOrganisation(organisationRefs);
				organisations.sort((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()));
				for (Organisation organisation : organisations) {
					String text = translate("settings.bulk.overview.organisation.add", organisation.getDisplayName());
					List<RepositoryEntry> changes = editables.getOrganisationAddChanges(organisation.getKey());
					String resourceItemName = createResourceLink(changes);
					fields.add(new OverviewField(text, resourceItemName));
				}
			}
			if (context.isSelected(SettingsBulkEditable.organisationsRemove)) {
				Set<OrganisationRefImpl> organisationRefs = context.getOrganisationRemoveKeys().stream().map(OrganisationRefImpl::new).collect(Collectors.toSet());
				List<Organisation> organisations = organisationService.getOrganisation(organisationRefs);
				organisations.sort((o1, o2) -> o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName()));
				for (Organisation organisation : organisations) {
					String text = translate("settings.bulk.overview.organisation.remove", organisation.getDisplayName());
					List<RepositoryEntry> changes = editables.getOrganisationRemoveChanges(organisation.getKey(), context.getOrganisationRemoveKeys());
					String resourceItemName = createResourceLink(changes);
					fields.add(new OverviewField(text, resourceItemName));
				}
			}
			if (fields.isEmpty()) {
				fields.add(new OverviewField(translate("settings.bulk.overview.none"), null));
			}
			OverviewStep step = new OverviewStep(translate("settings.bulk.organisation.title"), fields);
			overviewSteps.add(step);
		}
		
		if (steps.contains(Step.authorRights)) {
			List<OverviewField> fields = new ArrayList<>(3);
			if (context.isSelected(SettingsBulkEditable.authorRightReference)) {
				String text = context.isAuthorRightReference()
						? translate("settings.bulk.overview.author.rights.reference.add")
						: translate("settings.bulk.overview.author.rights.reference.remove");
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.authorRightReference);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.authorRightCopy)) {
				String text = context.isAuthorRightCopy()
						? translate("settings.bulk.overview.author.rights.copy.add")
						: translate("settings.bulk.overview.author.rights.copy.remove");
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.authorRightCopy);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.authorRightDownload)) {
				String text = context.isAuthorRightDownload()
						? translate("settings.bulk.overview.author.rights.download.add")
						: translate("settings.bulk.overview.author.rights.download.remove");
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.authorRightDownload);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (fields.isEmpty()) {
				fields.add(new OverviewField(translate("settings.bulk.overview.none"), null));
			}
			OverviewStep step = new OverviewStep(translate("settings.bulk.author.rights.title"), fields);
			overviewSteps.add(step);
		}
		
		if (steps.contains(Step.execution)) {
			List<OverviewField> fields = new ArrayList<>(3);
			if (context.isSelected(SettingsBulkEditable.lifecycleType)) {
				String text;
				switch (context.getLifecycleType()) {
				case none: text = translate("settings.bulk.execution.period.none");
					break;
				case publicCycle: 
					RepositoryEntryLifecycle lifecycle = lifecycleDao.loadById(context.getLifecyclePublicKey());
					String lifecycleName = lifecycle != null? lifecycle.getLabel(): "-";
					text = translate("settings.bulk.overview.execution.public", lifecycleName);
					break;
				case privateCycle: 
					String from = context.getLifecycleValidFrom() != null
							? Formatter.getInstance(getLocale()).formatDate(context.getLifecycleValidFrom())
							: "-";
					String to = context.getLifecycleValidTo() != null
							? Formatter.getInstance(getLocale()).formatDate(context.getLifecycleValidTo())
							: "-";
					text = translate("settings.bulk.overview.execution.private", from, to);
					break;
				default:
					text = "-";
					break;
				}
				text = translate("settings.bulk.overview.execution.period", text);
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.lifecycleType);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (context.isSelected(SettingsBulkEditable.location)) {
				String text = translate("settings.bulk.overview.location", context.getLocation());
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.location);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			if (fields.isEmpty()) {
				fields.add(new OverviewField(translate("settings.bulk.overview.none"), null));
			}
			OverviewStep step = new OverviewStep(translate("settings.bulk.execution.title"), fields);
			overviewSteps.add(step);
		}
		
		if (steps.contains(Step.toolbar)) {
			List<OverviewField> fields = new ArrayList<>(3);
			addToolbarField(fields, SettingsBulkEditable.toolSearch, context.isToolSearch(), "settings.bulk.toolbar.search");
			addToolbarField(fields, SettingsBulkEditable.toolCalendar, context.isToolCalendar(), "settings.bulk.toolbar.calendar");
			addToolbarField(fields, SettingsBulkEditable.toolParticipantList, context.isToolParticipantList(), "settings.bulk.toolbar.participant.list");
			addToolbarField(fields, SettingsBulkEditable.toolParticipantInfo, context.isToolParticipantInfo(), "settings.bulk.toolbar.participant.info");
			addToolbarField(fields, SettingsBulkEditable.toolEmail, context.isToolEmail(), "settings.bulk.toolbar.email");
			addToolbarField(fields, SettingsBulkEditable.toolTeams, context.isToolTeams(), "settings.bulk.toolbar.teams");
			if (context.isSelected(SettingsBulkEditable.toolBigBlueButton)) {
				String text = null;
				if (context.isToolBigBlueButton()) {
					text = translate("settings.bulk.overview.toolbar.on", translate("settings.bulk.toolbar.bigbluebutton"));
					String bbbModeratorText = context.isToolBigBlueButton()
							? translate("settings.bulk.overview.toolbar.on", translate("settings.bulk.toolbar.bigbluebutton.moderator"))
							: translate("settings.bulk.overview.toolbar.off", translate("settings.bulk.toolbar.bigbluebutton.moderator"));
					text = translate("settings.bulk.overview.toolbar.bigbluebutton", text, bbbModeratorText);
				} else {
					text = translate("settings.bulk.overview.toolbar.off", translate("settings.bulk.toolbar.bigbluebutton"));
				}
				List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.toolBigBlueButton);
				String resourceItemName = createResourceLink(changes);
				fields.add(new OverviewField(text, resourceItemName));
			}
			addToolbarField(fields, SettingsBulkEditable.toolZoom, context.isToolZoom(), "settings.bulk.toolbar.zoom");
			if (context.isSelected(SettingsBulkEditable.toolBlog)) {
				String text = null;
				Replacement replacement = context.getToolBlog();
				if (Replacement.remove == replacement) {
					text = translate("settings.bulk.overview.toolbar.blog.remove");
				} else {
					RepositoryEntry blogEntry = repositoryManager.lookupRepositoryEntryBySoftkey(context.getToolBlogKey(), false);
					if (blogEntry != null) {
						if (Replacement.add == replacement) {
							text = translate("settings.bulk.overview.toolbar.blog.add", blogEntry.getDisplayname());
						} else if (Replacement.change == replacement) {
							text = translate("settings.bulk.overview.toolbar.blog.change", blogEntry.getDisplayname());
						} else if (Replacement.addChange == replacement) {
							text = translate("settings.bulk.overview.toolbar.blog.add.change", blogEntry.getDisplayname());
						}
					}
				}
				if (StringHelper.containsNonWhitespace(text)) {
					List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.toolBlog);
					String resourceItemName = createResourceLink(changes);
					fields.add(new OverviewField(text, resourceItemName));
				}
			}
			if (context.isSelected(SettingsBulkEditable.toolWiki)) {
				String text = null;
				Replacement replacement = context.getToolWiki();
				if (Replacement.remove == replacement) {
					text = translate("settings.bulk.overview.toolbar.wiki.remove");
				} else {
					RepositoryEntry wikiEntry = repositoryManager.lookupRepositoryEntryBySoftkey(context.getToolWikiKey(), false);
					if (wikiEntry != null) {
						if (Replacement.add == replacement) {
							text = translate("settings.bulk.overview.toolbar.wiki.add", wikiEntry.getDisplayname());
						} else if (Replacement.change == replacement) {
							text = translate("settings.bulk.overview.toolbar.wiki.change", wikiEntry.getDisplayname());
						} else if (Replacement.addChange == replacement) {
							text = translate("settings.bulk.overview.toolbar.wiki.add.change", wikiEntry.getDisplayname());
						}
					}
				}
				if (StringHelper.containsNonWhitespace(text)) {
					List<RepositoryEntry> changes = editables.getChanges(context, SettingsBulkEditable.toolWiki);
					String resourceItemName = createResourceLink(changes);
					fields.add(new OverviewField(text, resourceItemName));
				}
			}
			addToolbarField(fields, SettingsBulkEditable.toolForum, context.isToolForum(), "settings.bulk.toolbar.forum");
			addToolbarField(fields, SettingsBulkEditable.toolDocuments, context.isToolDocuments(), "settings.bulk.toolbar.documents");
			addToolbarField(fields, SettingsBulkEditable.toolChat, context.isToolChat(), "settings.bulk.toolbar.chat");
			if (fields.isEmpty()) {
				fields.add(new OverviewField(translate("settings.bulk.overview.none"), null));
			}
			OverviewStep step = new OverviewStep(translate("settings.bulk.toolbar.title"), fields);
			overviewSteps.add(step);
		}
		
		if (overviewSteps.isEmpty()) {
			overviewSteps.add(new OverviewStep(null, List.of(new OverviewField(translate("settings.bulk.overview.none"), null))));
		}
		
		overviewCont.contextPut("steps", overviewSteps);
	}

	private String createResourceLink(List<RepositoryEntry> repositoryEntries) {
		String linkText = null;
		if (repositoryEntries.isEmpty()) {
			linkText = translate("settings.bulk.overview.resources.none");
		} else if (repositoryEntries.size() == totalRepositoryEntries) {
			linkText = translate("settings.bulk.overview.resources.all");
		} else if (repositoryEntries.size() == 1) {
			linkText = translate("settings.bulk.overview.resources.one");
		} else {
			linkText = translate("settings.bulk.overview.resources.multi", String.valueOf(repositoryEntries.size()));
		}
		
		String linkName = "usage-" + counter++;
		FormLink link = uifactory.addFormLink(linkName, CMD_RESOURCE, "", null, overviewCont, Link.LINK | Link.NONTRANSLATED);
		link.setI18nKey(linkText);
		link.setEnabled(!repositoryEntries.isEmpty());
		link.setUserObject(repositoryEntries);
		return linkName;
	}
	
	private void addToolbarField(List<OverviewField> fields, SettingsBulkEditable editable, boolean on, String i18nKey) {
		if (context.isSelected(editable)) {
			String text = on
					? translate("settings.bulk.overview.toolbar.on", translate(i18nKey))
					: translate("settings.bulk.overview.toolbar.off", translate(i18nKey));
			List<RepositoryEntry> changes = editables.getChanges(context, editable);
			String resourceItemName = createResourceLink(changes);
			fields.add(new OverviewField(text, resourceItemName));
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_RESOURCE.equals(cmd)) {
				@SuppressWarnings("unchecked")
				List<RepositoryEntry> repositoryEntries = (List<RepositoryEntry>)link.getUserObject();
				doOpenChanges(ureq, repositoryEntries, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doOpenChanges(UserRequest ureq, List<RepositoryEntry> changes, FormLink link) {
		removeAsListenerAndDispose(calloutCtrl);
		
		VelocityContainer usageVC = createVelocityContainer("repository_entry_name");
		
		List<String> entryNames = changes.stream().map(RepositoryEntry::getDisplayname).sorted().collect(Collectors.toList());
		usageVC.contextPut("entryNames", entryNames);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), usageVC, link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	public static final class OverviewStep {
		
		private final String name;
		private final List<OverviewField> fields;
		
		public OverviewStep(String name, List<OverviewField> fields) {
			this.name = name;
			this.fields = fields;
		
		}
		public String getName() {
			return name;
		}
		
		public List<OverviewField> getFields() {
			return fields;
		}
		
	}
	
	public static final class OverviewField {
		
		private final String text;
		private final String resourceItemName;
		
		public OverviewField(String text, String resourceItemName) {
			this.text = text;
			this.resourceItemName = resourceItemName;
		}
		
		public String getText() {
			return text;
		}
		
		public String getResourceItemName() {
			return resourceItemName;
		}
		
	}

}
