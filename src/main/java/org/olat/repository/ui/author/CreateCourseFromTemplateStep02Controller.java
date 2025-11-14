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
package org.olat.repository.ui.author;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionSource;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-11-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateCourseFromTemplateStep02Controller extends StepFormBasicController {

	private CreateCourseFromTemplateContext context;

	private TextElement titleEl;
	private TextElement extRefEl;
	private SingleSelection runtimeTypeEl;
	private SingleSelection executionPeriodEl;
	private SingleSelection publicDatesEl;
	private FormLayoutContainer privateDatesCont;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private ObjectSelectionElement subjectsEl;
	private SingleSelection implementationFormatEl;
	private ObjectSelectionElement administrativeAccessEl;
	private final List<Organisation> manageableOrganisations;

	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private LifecycleModule lifecycleModule;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryService repositoryService;

	public CreateCourseFromTemplateStep02Controller(UserRequest ureq, WindowControl wControl, Form rootForm,
													StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		if (runContext.get(CreateCourseFromTemplateContext.KEY) instanceof CreateCourseFromTemplateContext ctx) {
			this.context = ctx;
		}

		manageableOrganisations = organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("cif.title", 100, "", formLayout);
		titleEl.setDisplaySize(30);
		titleEl.setMandatory(true);
		titleEl.setInlineValidationOn(true);

		extRefEl = uifactory.addTextElement("cif.externalref", 255, "", formLayout);
		extRefEl.setHelpText(translate("cif.externalref.hover"));
		extRefEl.setInlineValidationOn(true);

		initRuntimeType(formLayout);
		initExecutionPeriod(formLayout);
		initSubjects(formLayout);
		initImplementationFormat(formLayout);
		initAdministrativeAccess(ureq, formLayout);
	}

	private void initRuntimeType(FormItemContainer formLayout) {
		if (curriculumModule.isEnabled()) {
			SelectionValues runtimeTypeKV = new SelectionValues();
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.standalone.name(), translate("runtime.type.standalone")));
			runtimeTypeKV.add(SelectionValues.entry(RepositoryEntryRuntimeType.curricular.name(), translate("runtime.type.curricular")));
			runtimeTypeEl = uifactory.addDropdownSingleselect("cif.runtime.type", "cif.runtime.type", formLayout,
					runtimeTypeKV.keys(), runtimeTypeKV.values());

			String defaultType = curriculumModule.getDefaultCourseRuntimeType().name();
			if (runtimeTypeKV.containsKey(defaultType)) {
				runtimeTypeEl.select(defaultType, true);
			}

			if (!runtimeTypeEl.isOneSelected()) {
				runtimeTypeEl.select(RepositoryEntryRuntimeType.standalone.name(), true);
			}
		}
	}

	private void initExecutionPeriod(FormItemContainer formLayout) {
		SelectionValues executionPeriodKV =  new SelectionValues();
		
		executionPeriodKV.add(SelectionValues.entry("none", translate("cif.dates.none")));
		executionPeriodKV.add(SelectionValues.entry("private", translate("cif.dates.private")));
		if (lifecycleModule.isEnabled()) {
			executionPeriodKV.add(SelectionValues.entry("public", translate("cif.dates.public")));
		}

		executionPeriodEl = uifactory.addRadiosVertical("cif.dates", formLayout, executionPeriodKV.keys(),
				executionPeriodKV.values());
		executionPeriodEl.setHelpText(translate("cif.dates.help"));
		executionPeriodEl.select(courseModule.getCourseExecutionDefault(), true);
		executionPeriodEl.addActionListener(FormEvent.ONCHANGE);

		List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
		List<RepositoryEntryLifecycle> filteredCycles = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		for (RepositoryEntryLifecycle cycle : cycles) {
			if (cycle.getValidTo() == null
					|| now.isBefore(LocalDateTime.ofInstant(cycle.getValidTo().toInstant(), ZoneId.systemDefault()))) {
				filteredCycles.add(cycle);
			}
		}

		SelectionValues publicDatesKV = new SelectionValues();

		for (RepositoryEntryLifecycle cycle : filteredCycles) {
			StringBuilder sb = new StringBuilder(32);
			boolean labelAvailable = StringHelper.containsNonWhitespace(cycle.getLabel());
			if (labelAvailable) {
				sb.append(cycle.getLabel());
			}
			if (StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
				if(labelAvailable) sb.append(" - ");
				sb.append(cycle.getSoftKey());
			}
			publicDatesKV.add(SelectionValues.entry(cycle.getKey().toString(), sb.toString()));
		}
		publicDatesEl = uifactory.addDropdownSingleselect("cif.public.dates", formLayout, publicDatesKV.keys(), 
				publicDatesKV.values(), null);
		
		cycles.stream()
				.filter(RepositoryEntryLifecycle::isDefaultPublicCycle)
				.findFirst()
				.ifPresent(cycle -> {
					if (publicDatesEl.containsKey(cycle.getKey().toString())) {
						publicDatesEl.select(cycle.getKey().toString(), true);
					}
				});

		String privateDatePage = Util.getPackageVelocityRoot(RepositoryService.class) + "/cycle_dates.html";
		privateDatesCont = FormLayoutContainer.createCustomFormLayout("private.date", getTranslator(), privateDatePage);
		privateDatesCont.setRootForm(mainForm);
		privateDatesCont.setLabel("cif.private.dates", null);
		formLayout.add("private.date", privateDatesCont);

		startDateEl = uifactory.addDateChooser("date.start", "cif.date.from", null, privateDatesCont);
		endDateEl = uifactory.addDateChooser("date.end", "cif.date.to", null, privateDatesCont);

		if (startDateEl != null && endDateEl != null) {
			startDateEl.addActionListener(FormEvent.ONCHANGE);
			endDateEl.addActionListener(FormEvent.ONCHANGE);
		}

		updateExecutionPeriodVisibility();
	}

	private void initSubjects(FormItemContainer formLayout) {
		List<TaxonomyRef> taxonomyRefs = repositoryModule.getTaxonomyRefs();
		if (taxonomyModule.isEnabled() && !taxonomyRefs.isEmpty()) {
			String labelI18nKey = catalogModule.isEnabled()? "cif.taxonomy.levels.catalog": "cif.taxonomy.levels";
			ObjectSelectionSource source = new TaxonomyLevelSelectionSource(getLocale(),
					repositoryService.getTaxonomy(context.getTemplateRepositoryEntry()),
					() -> taxonomyService.getTaxonomyLevels(taxonomyRefs),
					translate("cif.taxonomy.options.label"), translate(labelI18nKey));
			subjectsEl = uifactory.addObjectSelectionElement("taxonomy", labelI18nKey, formLayout, 
					getWindowControl(), true, source);
			if (catalogModule.isEnabled()) {
				subjectsEl.setHelpTextKey("cif.taxonomy.levels.help.catalog", null);
			}
		}
	}

	private void initImplementationFormat(FormItemContainer formLayout) {
		SelectionValues implementationFormatKV = new SelectionValues();
		repositoryManager.getAllEducationalTypes()
				.forEach(type -> implementationFormatKV.add(entry(type.getIdentifier(), 
						translate(RepositoyUIFactory.getI18nKey(type)))));
		implementationFormatKV.sort(SelectionValues.VALUE_ASC);
		implementationFormatEl = uifactory.addDropdownSingleselect("cif.educational.type", formLayout, 
				implementationFormatKV.keys(), implementationFormatKV.values());
		implementationFormatEl.enableNoneSelection();
		RepositoryEntryEducationalType implementationFormat = context.getTemplateRepositoryEntry().getEducationalType();
		if (implementationFormat != null && Arrays.asList(implementationFormatEl.getKeys()).contains(implementationFormat.getIdentifier())) {
			implementationFormatEl.select(implementationFormat.getIdentifier(), true);
		}
	}

	private void initAdministrativeAccess(UserRequest ureq, FormItemContainer formLayout) {
		administrativeAccessEl = RepositoyUIFactory.createOrganisationsEl(ureq, getWindowControl(), formLayout, uifactory,
				organisationModule, manageableOrganisations);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == executionPeriodEl) {
			updateExecutionPeriodVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateExecutionPeriodVisibility() {
		if (executionPeriodEl.isOneSelected()) {
			String type =  executionPeriodEl.getSelectedKey();
			if ("none".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(false);
			} else if("public".equals(type)) {
				publicDatesEl.setVisible(true);
				privateDatesCont.setVisible(false);
			} else if("private".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(true);
			}
		}
	}

	@Override
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean allOk = super.validateFormItem(ureq, item);
		
		if (item == titleEl) {
			validateTitleUnique(ureq);
		} else if (item == extRefEl) {
			validateExtRefUnique(ureq);
		}
		
		return allOk;
	}

	private void validateTitleUnique(UserRequest ureq) {
		titleEl.clearWarning();
		if (StringHelper.containsNonWhitespace(titleEl.getValue()) && 
				!StringHelper.containsNonWhitespace(extRefEl.getValue())) {
			SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
			params.setExactSearch(true);
			params.setDisplayname(titleEl.getValue().toLowerCase());
			if (repositoryService.countAuthorView(params) > 0) {
				titleEl.setWarningKey("error.exists.displayname");
			}
		}
	}

	private void validateExtRefUnique(UserRequest ureq) {
		extRefEl.clearWarning();
		if (StringHelper.containsNonWhitespace(extRefEl.getValue())) {
			SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
			params.setExactSearch(true);
			params.setReference(extRefEl.getValue().toLowerCase());
			if (repositoryService.countAuthorView(params) > 0) {
				extRefEl.setWarningKey("error.exists.ext.ref");
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		boolean titleOk = RepositoyUIFactory.validateTextElement(titleEl, true, 100);
		if (titleOk) {
			validateTitleUnique(ureq);
		} else {
			allOk &= false;
		}

		boolean extRefOk = RepositoyUIFactory.validateTextElement(extRefEl, false, 255);
		if (extRefOk) {
			validateExtRefUnique(ureq);
		} else {
			allOk &= false;
		}

		if (publicDatesEl != null) {
			publicDatesEl.clearError();
			if (publicDatesEl.isEnabled() && publicDatesEl.isVisible() && !publicDatesEl.isOneSelected()) {
				publicDatesEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		allOk &= RepositoyUIFactory.validateOrganisationEl(administrativeAccessEl);

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		instantiateCourse(ureq);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private void instantiateCourse(UserRequest ureq) {
		String title = titleEl.getValue();
		String extRef = extRefEl.getValue();
		RepositoryEntry entry = repositoryService.copy(context.getTemplateRepositoryEntry(), getIdentity(), title, extRef);

		RepositoryEntryRuntimeType runtimeType = RepositoryEntryRuntimeType.standalone;
		if (runtimeTypeEl != null && runtimeTypeEl.isOneSelected()) {
			runtimeType = RepositoryEntryRuntimeType.valueOf(runtimeTypeEl.getSelectedKey());
		}

		RepositoryEntryLifecycle lifecycle = null;
		if (executionPeriodEl.isOneSelected()) {
			String type =  executionPeriodEl.getSelectedKey();
			if ("public".equals(type)) {
				if (publicDatesEl.isOneSelected()) {
					lifecycle = lifecycleDao.loadById(Long.parseLong(publicDatesEl.getSelectedKey()));
				}
			} else if ("private".equals(type)) {
				String softKey = "lf_" + entry.getSoftkey();
				Date startDate = startDateEl.getDate();
				Date endDate = endDateEl.getDate();
				lifecycle = lifecycleDao.create(title, softKey, true, startDate, endDate);
			}
		}

		Set<TaxonomyLevel> subjects = null;
		if (subjectsEl != null && !subjectsEl.getSelectedKeys().isEmpty()) {
			subjects = Set.copyOf(taxonomyService.getTaxonomyLevelsByRefs(
					TaxonomyLevelSelectionSource.toRefs(subjectsEl.getSelectedKeys())
			));
		}

		String educationalTypeKey = implementationFormatEl.isOneSelected() ? implementationFormatEl.getSelectedKey() : null;
		RepositoryEntryEducationalType educationalType = repositoryManager.getEducationalType(educationalTypeKey);

		List<Organisation> orgs = organisationService.getOrganisation(
				OrganisationSelectionSource.toRefs(administrativeAccessEl.getSelectedKeys()));
		
		RepositoryEntry reloadedEntry = repositoryManager.setDescriptionAndName(entry, title, extRef,
				entry.getAuthors(), entry.getDescription(), entry.getTeaser(), entry.getObjectives(),
				entry.getRequirements(), entry.getCredits(), entry.getMainLanguage(), entry.getLocation(),
				entry.getExpenditureOfWork(), lifecycle, orgs, subjects, educationalType);
		
		repositoryManager.setRuntimeType(reloadedEntry, runtimeType);

		RepositoyUIFactory.setDefaultOrganisationKeys(ureq, administrativeAccessEl.getSelectedKeys());
		
		context.setCreatedRepositoryEntry(reloadedEntry);
	}
}
