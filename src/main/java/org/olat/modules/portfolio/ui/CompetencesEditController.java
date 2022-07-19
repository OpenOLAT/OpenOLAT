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
package org.olat.modules.portfolio.ui;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable controller to show the list of competences and edit them with
 * a small edit button. When edited, an Event.CHANGED is fired
 * 
 * Initial date: 28.07.2017<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class CompetencesEditController extends FormBasicController {
	
	private TaxonomyLevelSelection competencesEl;
	private FormLink editLink;
	private FormSubmit saveButton;
	
	private final Page page;

	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public CompetencesEditController(UserRequest ureq, WindowControl wControl, Page page) {
		super(ureq, wControl, "competences_edit");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.page = page;
		
		initForm(ureq);
		/* we add domID to competences_edit.html to reduce DIV count */
		this.flc.getFormItemComponent().setDomReplacementWrapperRequired(false);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_tags_form");
		
		Set<TaxonomyLevel>  existingCompetences = page != null
				? portfolioService.getRelatedCompetences(page, true)
						.stream()
						.map(TaxonomyCompetence::getTaxonomyLevel)
						.collect(Collectors.toSet())
				: Collections.emptySet();
		Set<TaxonomyLevel> availableTaxonomyLevels = taxonomyService.getTaxonomyLevels(portfolioModule.getLinkedTaxonomies())
				.stream()
				.filter(taxonomyLevel -> taxonomyLevel.getType() == null || taxonomyLevel.getType().isAllowedAsCompetence())
				.collect(Collectors.toSet());
		
		competencesEl = uifactory.addTaxonomyLevelSelection("competences", "competences", formLayout,
				getWindowControl(), availableTaxonomyLevels);
		competencesEl.setDisplayNameHeader(translate("table.header.competence"));
		competencesEl.setSelection(existingCompetences);

		editLink = uifactory.addFormLink("edit", "edit", "edit", null, formLayout, Link.LINK);
		editLink.setCustomEnabledLinkCSS("o_competences_edit o_button_textstyle");
		
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.setElementCssClass("o_competences_save");
		saveButton.setVisible(false);
		
		// on init set to read-only
		initFormEditableState(false);
	}

	/**
	 * Internal helper to hide and disable elements of the form depending on the
	 * editable state of the form
	 * 
	 * @param editable
	 */
	private void initFormEditableState(boolean editable) {
		competencesEl.setEnabled(editable);
		editLink.setVisible(!editable);
		saveButton.setVisible(editable);
		// Special label when no categories are there
		if (competencesEl.getSelection().isEmpty()) {
			editLink.setI18nKey("add");			
		} else {
			editLink.setI18nKey("edit");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == editLink) {
			initFormEditableState(true);
		} else if(source == competencesEl) {
			fireEvent(ureq, Event.CHANGED_EVENT);
			initFormEditableState(false);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
		initFormEditableState(false);
	}

	/**
	 * @return The list of competences as visually configured in the box
	 */
	public Set<TaxonomyLevelRef> getUpdatedCompetences() {
		return competencesEl.getSelection();
	}
}