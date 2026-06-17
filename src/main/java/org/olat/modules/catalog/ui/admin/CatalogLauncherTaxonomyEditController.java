/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.catalog.ui.admin;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler.Config;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionSource;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.RepositoryHandlerFactory.OrderedRepositoryHandler;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jun 3, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogLauncherTaxonomyEditController extends CatalogLauncherEditController {
	
	private static final String MODE_TAXONOMY = "taxonomy";
	private static final String MODE_TAXONOMY_LEVEL = "taxonomy.level";

	private SingleSelection modeEl;
	private SingleSelection taxonomyEl;
	private ObjectSelectionElement taxonomyLevelObjEl;
	private MultipleSelectionElement educationalTypeEl;
	private MultipleSelectionElement resourceTypeEl;

	private final TaxonomyLevelLauncherHandler handler;

	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	public CatalogLauncherTaxonomyEditController(UserRequest ureq, WindowControl wControl,
			TaxonomyLevelLauncherHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, handler, catalogLauncher);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.handler = handler;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Config config = null;
		if (getCatalogLauncher() != null) {
			config = handler.fromXML(getCatalogLauncher().getConfig());
		}
		
		initTitle(formLayout);
		initTaxonomy(formLayout, config);
		initEnabled(formLayout);
		initFormOrganisations(formLayout);
		
		FormLayoutContainer typesCont = FormLayoutContainer.createTwoColsFormLayout("types", getTranslator());
		typesCont.setRootForm(mainForm);
		formLayout.add(typesCont);
		
		initEducationalType(typesCont, config);
		initResourceType(typesCont, config);
		
		initButtons(ureq, formLayout);
	}

	private void initTaxonomy(FormItemContainer formLayout, Config config) {
		SelectionValues modeKV = new SelectionValues();
		modeKV.add(new SelectionValue(MODE_TAXONOMY_LEVEL, translate("admin.taxonomy.mode.level"),
				translate("admin.taxonomy.mode.level.desc"), null, null, true));
		modeKV.add(new SelectionValue(MODE_TAXONOMY, translate("admin.taxonomy.mode.taxonomy"),
				translate("admin.taxonomy.mode.taxonomy.desc"), null, null, true));
		modeEl = uifactory.addCardSingleSelectHorizontal("admin.taxonomy.mode", "admin.taxonomy.mode",
				formLayout, modeKV);
		modeEl.addActionListener(FormEvent.ONCHANGE);

		List<TaxonomyRef> taxonomyRefs = repositoryModule.getTaxonomyRefs();
		List<TaxonomyLevel> allLevels = taxonomyRefs.isEmpty()
				? List.of()
				: taxonomyService.getTaxonomyLevels(taxonomyRefs);
		SelectionValues taxonomyKV = new SelectionValues();
		allLevels.stream()
				.map(TaxonomyLevel::getTaxonomy)
				.distinct()
				.forEach(t -> taxonomyKV.add(entry(t.getKey().toString(), t.getDisplayName())));
		taxonomyKV.sort(VALUE_ASC);
		taxonomyEl = uifactory.addDropdownSingleselect("admin.taxonomy", "admin.taxonomy",
				formLayout, taxonomyKV.keys(), taxonomyKV.values());
		taxonomyEl.setMandatory(true);

		TaxonomyLevel preselectedLevel = null;
		if (config != null && config.getTaxonomyLevelKey() != null) {
			Long levelKey = config.getTaxonomyLevelKey();
			preselectedLevel = taxonomyService.getTaxonomyLevel(() -> levelKey);
		}
		Collection<TaxonomyLevel> selectedLevels = preselectedLevel != null ? List.of(preselectedLevel) : List.of();
		TaxonomyLevelSelectionSource source = new TaxonomyLevelSelectionSource(
				getLocale(),
				selectedLevels,
				() -> taxonomyService.getTaxonomyLevels(taxonomyRefs),
				translate("admin.taxonomy.levels"));
		taxonomyLevelObjEl = uifactory.addObjectSelectionElement("admin.taxonomy.levels", "admin.taxonomy.levels",
				formLayout, getWindowControl(), false, source);
		taxonomyLevelObjEl.setMandatory(true);

		String mode = MODE_TAXONOMY_LEVEL;
		if (config != null && config.getTaxonomyKey() != null) {
			mode = MODE_TAXONOMY;
			String key = config.getTaxonomyKey().toString();
			if (taxonomyEl.containsKey(key)) {
				taxonomyEl.select(key, true);
			}
		}
		modeEl.select(mode, true);

		updateTaxonomyUI();
	}

	private void updateTaxonomyUI() {
		boolean taxonomyMode = modeEl.isKeySelected(MODE_TAXONOMY);
		taxonomyEl.setVisible(taxonomyMode);
		taxonomyLevelObjEl.setVisible(!taxonomyMode);
	}

	private void initEducationalType(FormItemContainer formLayout, Config config) {
		SelectionValues educationalTypeKV = new SelectionValues();
		repositoryManager.getAllEducationalTypes()
				.forEach(type -> educationalTypeKV.add(entry(type.getKey().toString(), translate(RepositoyUIFactory.getI18nKey(type)))));
		educationalTypeKV.sort(SelectionValues.VALUE_ASC);
		educationalTypeEl = uifactory.addCheckboxesDropdown("educationalType", "admin.educational.types",
				formLayout, educationalTypeKV.keys(), educationalTypeKV.values());
		if (config != null && config.getEducationalTypeKeys() != null && !config.getEducationalTypeKeys().isEmpty()) {
			for (Long key : config.getEducationalTypeKeys()) {
				if (educationalTypeEl.getKeys().contains(key.toString())) {
					educationalTypeEl.select(key.toString(), true);
				}
			}
		}
		
	}

	private void initResourceType(FormItemContainer formLayout, Config config) {
		SelectionValues resourceTypeSV = new SelectionValues();
		List<OrderedRepositoryHandler> supportedHandlers = repositoryHandlerFactory.getMainOrderRepositoryHandlers();
		for (OrderedRepositoryHandler handler:supportedHandlers) {
			String type = handler.getHandler().getSupportedType();
			String iconLeftCss = RepositoyUIFactory.getIconCssClass(type);
			resourceTypeSV.add(new SelectionValue(type, translate(type), null, "o_icon o_icon-fw ".concat(iconLeftCss), null, true));
		}
		resourceTypeEl = uifactory.addCheckboxesDropdown("resourceType", "admin.resource.types",
				formLayout, resourceTypeSV.keys(), resourceTypeSV.values(), null, resourceTypeSV.icons());
		if (config != null && config.getResourceTypes()!= null && !config.getResourceTypes().isEmpty()) {
			for (String type : config.getResourceTypes()) {
				if (resourceTypeEl.getKeys().contains(type)) {
					resourceTypeEl.select(type, true);
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == modeEl) {
			updateTaxonomyUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		taxonomyEl.clearError();
		taxonomyLevelObjEl.clearError();

		if (modeEl.isKeySelected(MODE_TAXONOMY)) {
			if (!taxonomyEl.isOneSelected()) {
				taxonomyEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		} else if (modeEl.isKeySelected(MODE_TAXONOMY_LEVEL)) {
			if (taxonomyLevelObjEl.getSelectedKeys().isEmpty()) {
				taxonomyLevelObjEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected String getConfig() {
		Config config = new Config();
		
		if (modeEl.isKeySelected(MODE_TAXONOMY)) {
			config.setTaxonomyKey(Long.valueOf(taxonomyEl.getSelectedKey()));
		} else {
			config.setTaxonomyLevelKey(Long.valueOf(taxonomyLevelObjEl.getSelectedKey()));
		}
		
		if (educationalTypeEl.isAtLeastSelected(1)) {
			Collection<Long> educationalTypeKeys = educationalTypeEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.collect(Collectors.toSet());
			config.setEducationalTypeKeys(educationalTypeKeys);
		} else {
			config.setEducationalTypeKeys(null);
		}
		
		if (resourceTypeEl.isAtLeastSelected(1)) {
			config.setResourceTypes(new HashSet<>(resourceTypeEl.getSelectedKeys()));
		} else {
			config.setResourceTypes(null);
		}
		
		return handler.toXML(config);
	}

}
