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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;

/**
 * Initial Date:  03.12.2007 <br>
 * @author patrickb
 */
public class RepositoyUIFactory {
	
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
			keyValues.add(entry(key, value));
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
					el.setErrorKey("form.legende.mandatory", null);
					return false;
				} else if (StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
					el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
					return false;
				}
			}
		}
		return true;
	}
}