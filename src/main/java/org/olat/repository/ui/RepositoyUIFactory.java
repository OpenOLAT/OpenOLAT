/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.repository.ui;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.CreateRepositoryEntryController;
import org.olat.user.ui.organisation.OrganisationSelectionSource;

/**
 * Initial Date:  03.12.2007 <br>
 * @author patrickb
 */
public class RepositoyUIFactory {
	
	private static final String GUIPREF_KEY_REPOSITORY_ENTRY_ORGS = "repository.entry.orgs";
	
	public static String getIconCssClass(String type) {
		String iconCSSClass = "o_" + type.replace(".", "-");
		iconCSSClass = iconCSSClass.concat("_icon");
		return iconCSSClass;
	}
	
	public static String getIconCssClass(RepositoryEntryShort re) {
		if(re == null) return "";
		
		String iconCSSClass = "o_" + re.getResourceType().replace(".", "-");
		if (re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			iconCSSClass = iconCSSClass.concat("_icon_closed");
		} else {
			iconCSSClass = iconCSSClass.concat("_icon");
		}
		return iconCSSClass;
	}
	
	public static String getIconCssClass(RepositoryEntry re) {
		if(re == null) return "";
		
		String iconCSSClass = "o_" + re.getOlatResource().getResourceableTypeName().replace(".", "-");
		if (re.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			iconCSSClass = iconCSSClass.concat("_icon_closed");
		} else {
			iconCSSClass = iconCSSClass.concat("_icon");
		}
		return iconCSSClass;
	}
	
	/**
	 * Create main controller that does nothing but displaying a message that
	 * this resource is disabled due to security constraints
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static GenericMainController createRepoEntryDisabledDueToSecurityMessageController(UserRequest ureq, WindowControl wControl) {
		//wrap simple message into mainLayout
		GenericMainController glc = new GenericMainController(ureq, wControl) {
			@Override
			public void init(UserRequest uureq) {
				Panel empty = new Panel("empty");			
				setTranslator(Util.createPackageTranslator(RepositoryModule.class, uureq.getLocale())); 
				MessageController contentCtr = MessageUIFactory.createInfoMessage(uureq, getWindowControl(), translate("security.disabled.title"), translate("security.disabled.info"));
				listenTo(contentCtr); // auto dispose later
				Component resComp = contentCtr.getInitialComponent();
				LayoutMain3ColsController columnLayoutCtr = new LayoutMain3ColsController(uureq, getWindowControl(), empty, resComp, /*do not save no prefs*/null);
				listenTo(columnLayoutCtr); // auto dispose later
				putInitialPanel(columnLayoutCtr.getInitialComponent());
			}
		
			@Override
			protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest uureq) {
				//no menutree means no menu events.
				return null;
			}
		
		};
		glc.init(ureq);
		return glc;
	}

	
	public static Controller createLifecycleAdminController(UserRequest ureq, WindowControl wControl) {
		return new LifecycleAdminController(ureq, wControl);
	}
	
	public static List<String> getDefaultOrganisationKeys(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			Object pref = guiPrefs.get(CreateRepositoryEntryController.class, GUIPREF_KEY_REPOSITORY_ENTRY_ORGS);
			if (pref instanceof String preList) {
				return Arrays.stream(preList.split("::")).toList();
			}
		}
		return List.of();
	}
	
	public static void setDefaultOrganisationKeys(UserRequest ureq, Collection<String> keys) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			String joindeKeys = keys.stream().collect(Collectors.joining("::"));
			guiPrefs.putAndSave(CreateRepositoryEntryController.class, GUIPREF_KEY_REPOSITORY_ENTRY_ORGS, joindeKeys);
		}
	}
	
	public static ObjectSelectionElement createOrganisationsEl(UserRequest ureq, WindowControl windowControl,
			FormItemContainer formLayout, FormUIFactory uifactory, OrganisationModule organisationModule,
			List<Organisation> manageableOrganisations) {
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				List.of(),
				() -> manageableOrganisations);
		ObjectSelectionElement organisationEl = uifactory.addObjectSelectionElement("organisations",
				"cif.organisations", formLayout, windowControl, true, organisationSource);
		organisationEl.setMandatory(true);
		organisationEl.setVisible(manageableOrganisations.size() > 1 && organisationModule.isEnabled());
		
		if (organisationEl.isVisible()) {
			RepositoyUIFactory.getDefaultOrganisationKeys(ureq).forEach(key -> organisationEl.select(key));
			if (organisationEl.getSelectedKeys().isEmpty() && !manageableOrganisations.isEmpty()) {
				organisationEl.select(manageableOrganisations.get(0).getKey().toString());
			}
		}
		
		return organisationEl;
	}
	
	public static boolean validateOrganisationEl(ObjectSelectionElement organisationEl) {
		organisationEl.clearError();
		if (organisationEl.isVisible() && organisationEl.getSelectedKeys().isEmpty()) {
			organisationEl.setErrorKey("form.legende.mandatory");
			return false;
		}
		return true;
	}
	
	public static Organisation getResourceOrganisation(OrganisationService organisationService,
			ObjectSelectionElement organisationEl, List<Organisation> manageableOrganisations) {
		Organisation organisation;
		if(organisationEl.isVisible() && !organisationEl.getSelectedKeys().isEmpty()) {
			OrganisationRef organisationRef = organisationEl.getSelectedKeys().stream()
					.limit(1)
					.map(OrganisationSelectionSource::toRef)
					.findFirst().get();
			organisation = organisationService.getOrganisation(organisationRef);
		} else if(manageableOrganisations.size() == 1) {
			organisation = organisationService.getOrganisation(manageableOrganisations.get(0));
		} else {
			organisation = organisationService.getDefaultOrganisation();
		}
		return organisation;
	}
	
	public static void addOrganisations(UserRequest ureq, OrganisationService organisationService, RepositoryService repositoryService,
			ObjectSelectionElement organisationEl, RepositoryEntry repositoryEntry, Organisation resourceOrganisation) {
		if(organisationEl.isVisible() && !organisationEl.getSelectedKeys().isEmpty()) {
			List<OrganisationRef> additionalOrganisationRefs = organisationEl.getSelectedKeys().stream()
					.filter(key -> !key.equals(resourceOrganisation.getKey().toString()))
					.map(OrganisationSelectionSource::toRef)
					.toList();
			organisationService.getOrganisation(additionalOrganisationRefs)
					.forEach(org -> repositoryService.addOrganisation(repositoryEntry, org));
			RepositoyUIFactory.setDefaultOrganisationKeys(ureq, organisationEl.getSelectedKeys());
		}
	}
	
	public static SelectionValues createTaxonomyLevelKV(Translator translator, List<TaxonomyLevel> allTaxonomyLevels) {
		boolean multiTaxonomy = allTaxonomyLevels.stream().map(TaxonomyLevel::getTaxonomy).distinct().count() > 1;
		SelectionValues keyValues = new SelectionValues();
		for (TaxonomyLevel level:allTaxonomyLevels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			addParentNames(translator, names, level);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			if (multiTaxonomy) {
				value = level.getTaxonomy().getDisplayName() + ": " + value;
			}
			keyValues.add(entry(key, StringHelper.escapeHtml(value)));
		}
		keyValues.sort(VALUE_ASC);
		return keyValues;
	}
	
	private static void addParentNames(Translator translator, List<String> names, TaxonomyLevel level) {
		names.add(TaxonomyUIFactory.translateDisplayName(translator, level));
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentNames(translator, names, parent);
		}
	}

	public static void selectTaxonomyLevels(MultipleSelectionElement taxonomyLevelEl, Collection<? extends TaxonomyLevelRef> levels) {
		for (TaxonomyLevelRef level : levels) {
			String key = level.getKey().toString();
			if (taxonomyLevelEl.getKeys().contains(key)) {
				taxonomyLevelEl.select(key, true);
			}
		}
	}
	
	public static String getI18nKey(RepositoryEntryEducationalType type) {
		return "educational.type.id." + type.getIdentifier();
	}
	
	public static String getPresetI18nKey(RepositoryEntryEducationalType type) {
		return "educational.type.id.preset." + type.getIdentifier();
	}
	
	public static boolean validateTextElement(TextElement el, boolean mandatory, int maxLength) {
		if (el != null) {
			el.clearError();
			if(el.isVisible() && el.isEnabled()) {
				String val = el.getValue();
				if (mandatory && !StringHelper.containsNonWhitespace(val)) {
					el.setErrorKey("form.legende.mandatory");
					return false;
				} else if (StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
					el.setErrorKey("input.toolong", Integer.toString(maxLength));
					return false;
				}
			}
		}
		return true;
	}
}