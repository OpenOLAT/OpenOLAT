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
package org.olat.modules.taxonomy.ui.component;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.util.ArrayHelper.emptyStrings;
import static org.olat.core.util.StringHelper.EMPTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: 05 Jan 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelSelectionController extends FormBasicController {
	
	private static final int MAX_RESULTS = 50;

	private FormLink browserButton;
	private FormLink selectButton;
	private FormLink expandButton;
	private FormLink quickSearchButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;

	private StaticTextElement selectionNoneEl;
	private MultipleSelectionElement selectionEl;
	private StaticTextElement resultsNoneEl;
	private MultipleSelectionElement resultsEl;
	private StaticTextElement resultsMoreEl;
	
	private final Set<TaxonomyLevel> allTaxonomyLevels;
	private final Set<Long> currentSelectionKeys;
	private Set<String> selectionKeys;
	private Boolean expand = Boolean.TRUE;

	public TaxonomyLevelSelectionController(UserRequest ureq, WindowControl wControl,
			Set<TaxonomyLevel> allTaxonomyLevels, Set<Long> currentSelectionKeys) {
		super(ureq, wControl, "level_selection", Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()));
		this.allTaxonomyLevels = allTaxonomyLevels;
		this.currentSelectionKeys = currentSelectionKeys;
		initForm(ureq);
		updateSelectionUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setElementCssClass("o_indicate_search");
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setEnabled(false);
		quickSearchButton.setDomReplacementWrapperRequired(false);

		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setElementCssClass("o_quick_search");
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);

		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("o_reset_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		
		Set<TaxonomyLevel> currentTaxonomyLevels = allTaxonomyLevels.stream()
				.filter(level -> currentSelectionKeys.contains(level.getKey()))
				.collect(Collectors.toSet());
		SelectionValues selectedSV = createTaxonomyLevelSV(currentTaxonomyLevels);
		selectionEl = uifactory.addCheckboxesVertical("taxonomy.level.selection.selection", formLayout,
				selectedSV.keys(), selectedSV.values(), null, 1);
		selectionEl.setHorizontallyAlignedCheckboxes(true);
		selectionEl.setEscapeHtml(false);
		selectionKeys = new HashSet<>(selectionEl.getKeys());
		selectionEl.setVisible(!selectionEl.getKeys().isEmpty());
		if (selectionEl.isVisible()) {
			selectionKeys.forEach(key -> selectionEl.select(key, true));
		}

		selectionNoneEl = uifactory.addStaticTextElement("taxonomy.level.selection.selection.none",
				"taxonomy.level.selection.selection", translate("taxonomy.level.selection.selection.none"), formLayout);
		selectionNoneEl.setVisible(selectionEl.getKeys().isEmpty());

		resultsNoneEl = uifactory.addStaticTextElement("taxonomy.level.selection.results.none",
				"taxonomy.level.selection.results", translate("taxonomy.level.selection.results.none"), formLayout);
		resultsNoneEl.setVisible(false);
		
		if (selectionEl.getKeys().size() > 4) {
			expandButton = uifactory.addFormLink("expandButton", null, "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		}
		expand = selectionEl.getKeys().size() <= 4;
		flc.contextPut("expand", expand);
		
		resultsEl = uifactory.addCheckboxesVertical("taxonomy.level.selection.results", formLayout, emptyStrings(),
				emptyStrings(), 1);
		resultsEl.setHorizontallyAlignedCheckboxes(true);
		resultsEl.setEscapeHtml(false);
		resultsEl.setVisible(false);

		resultsMoreEl = uifactory.addStaticTextElement("taxonomy.level.selection.results.more", null,
				translate("taxonomy.level.selection.results.more", String.valueOf(MAX_RESULTS)), formLayout);
		resultsMoreEl.setVisible(false);

		browserButton = uifactory.addFormLink("taxonomy.level.selection.browser", formLayout, Link.BUTTON_SMALL);
		selectButton = uifactory.addFormLink("taxonomy.level.selection.select", formLayout, Link.BUTTON_SMALL);
		selectButton.setPrimary(true);

		Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(),
				quickSearchEl.getFormDispatchId());
		getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}

	private SelectionValues createTaxonomyLevelSV(Collection<TaxonomyLevel> taxonomyLevels) {
		SelectionValues selectedSV = new SelectionValues();
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class=\"o_tax_ls_option\">");
			sb.append("<div class=\"o_nowrap o_muted\"><small>");
			sb.append(getLevelPath(taxonomyLevel));
			sb.append("</small></div>");
			sb.append("<div class=\"o_nowrap\">");
			sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
			sb.append("</div>");
			sb.append("</div>");
			selectedSV.add(entry(taxonomyLevel.getKey().toString(), sb.toString()));
		}
		selectedSV.sort(SelectionValues.VALUE_ASC);
		return selectedSV;
	}

	private String getLevelPath(TaxonomyLevel taxonomyLevel) {
		List<TaxonomyLevel> taxonomyLevels = new ArrayList<>();
		addParent(taxonomyLevels, taxonomyLevel);
		Collections.reverse(taxonomyLevels);

		StringBuilder sb = new StringBuilder();
		sb.append("/ ");
		if (taxonomyLevels.size() == 1) {
			sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevels.get(0)));
			sb.append(" /");
		} else if (taxonomyLevels.size() == 2) {
			sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevels.get(0)));
			sb.append(" / ");
			sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevels.get(1)));
			sb.append(" /");
		} else if (taxonomyLevels.size() > 2) {
			sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevels.get(0)));
			sb.append(" / ... / ");
			sb.append(TaxonomyUIFactory.translateDisplayName(getTranslator(),
					taxonomyLevels.get(taxonomyLevels.size() - 1)));
			sb.append(" /");
		}
		return sb.toString();
	}

	private void addParent(List<TaxonomyLevel> taxonomyLevels, TaxonomyLevel taxonomyLevel) {
		TaxonomyLevel parent = taxonomyLevel.getParent();
		if (parent != null) {
			taxonomyLevels.add(parent);
			addParent(taxonomyLevels, parent);
		}
	}
	
	public void addTaxonomyLevelKeys(Collection<Long> keys) {
		// Add new levels to the selection list
		Set<Long> allKeys = new HashSet<>(keys);
		allKeys.addAll(selectionEl.getKeys().stream().map(Long::valueOf).collect(Collectors.toList()));
		Set<TaxonomyLevel> currentTaxonomyLevels = allTaxonomyLevels.stream()
				.filter(level -> allKeys.contains(level.getKey()))
				.collect(Collectors.toSet());
		SelectionValues selectedSV = createTaxonomyLevelSV(currentTaxonomyLevels);
		selectionEl.setKeysAndValues(selectedSV.keys(), selectedSV.values());
		
		// Select the currently selected levels and the added levels
		selectionKeys.addAll(keys.stream().map(String::valueOf).collect(Collectors.toSet()));
		selectionKeys.forEach(key -> selectionEl.select(key, true));
		updateSelectionNumUI();
	}

	private void updateSelectionUI() {
		setSelectedKeys();
		updateSelectionNumUI();
		selectionKeys.forEach(key -> selectionEl.select(key, true));
		
		if (expandButton != null) {
			String expandIcon = expand.booleanValue()? "o_icon_details_collaps": "o_icon_details_expand";
			expandButton.setIconLeftCSS("o_icon o_icon_lg " + expandIcon);
			expandButton.setElementCssClass("o_tax_ls_expand_button");
		}
	}

	private void setSelectedKeys() {
		if (expand.booleanValue()) {
			selectionKeys = new HashSet<>(selectionEl.getSelectedKeys());
		}
	}

	private void updateSelectionNumUI() {
		if (selectionKeys.isEmpty()) {
			selectionEl.setLabel("taxonomy.level.selection.selection", null);
		} else {
			selectionEl.setLabel("taxonomy.level.selection.selection.num", new String[] { String.valueOf(selectionKeys.size()) });
		}
	}
	
	private void doToggleExpand() {
		expand = Boolean.valueOf(!expand.booleanValue());
		flc.contextPut("expand", expand);
		selectionKeys.forEach(key -> selectionEl.select(key, true));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (browserButton == source) {
			setSelectedKeys();
			fireEvent(ureq, BROWSE_EVENT);
		} else if (selectButton == source) {
			doSelect(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch();
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch();
		} else if (source == expandButton) {
			doToggleExpand();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == browserButton || source == selectionEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSelect(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doSelect(UserRequest ureq) {
		Set<Long> keys = new HashSet<>();
		
		Collection<String> selectedKeys = expand.booleanValue()? selectionEl.getSelectedKeys(): selectionKeys;
		for (String key : selectedKeys) {
			keys.add(Long.valueOf(key));
		}
		
		if (resultsEl.isVisible()) {
			for (String key : resultsEl.getSelectedKeys()) {
				keys.add(Long.valueOf(key));
			}
		}
		
		fireEvent(ureq, new SelectionEvent(keys));
	}

	private void doQuickSearch() {
		resultsNoneEl.setVisible(false);
		resultsMoreEl.setVisible(false);
		
		String searchText = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);
		
		if (StringHelper.containsNonWhitespace(searchText)) {
			resultsEl.setLabel("taxonomy.level.selection.results", new String[] { searchText });
			Set<TaxonomyLevel> taxonomyLevels = searchTaxonomyLevels(searchText);
			if (!taxonomyLevels.isEmpty()) {
				SelectionValues resultSV = createTaxonomyLevelSV(taxonomyLevels);
				boolean croped = false;
				if (resultSV.size() > MAX_RESULTS) {
					resultSV.cropEnd(MAX_RESULTS - 1);
					croped = true;
				}
				resultsEl.setKeysAndValues(resultSV.keys(), resultSV.values());
				resultsEl.setVisible(true);
				if (croped) {
					resultsMoreEl.setVisible(true);
				}
			} else {
				resultsEl.setKeysAndValues(emptyStrings(), emptyStrings());
				resultsEl.setVisible(false);
			}
		} else {
			resultsEl.setKeysAndValues(emptyStrings(), emptyStrings());
			resultsEl.setVisible(false);
		}
		
		resultsNoneEl.getComponent().setDirty(true);
		resultsEl.getComponent().setDirty(true);
		resultsMoreEl.getComponent().setDirty(true);
	}
	
	private Set<TaxonomyLevel> searchTaxonomyLevels(String searchText) {
		String lowerSearchText = searchText.toLowerCase();
		return allTaxonomyLevels.stream()
				.filter(level -> searchTaxonomyLevel(level, lowerSearchText))
				.collect(Collectors.toSet());
	}

	private boolean searchTaxonomyLevel(TaxonomyLevel taxonomylevel, String searchText) {
		String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomylevel, EMPTY);
		return displayName.toLowerCase().indexOf(searchText) > -1;
	}

	private void doResetQuickSearch() {
		quickSearchEl.setValue("");
		resultsNoneEl.setVisible(false);
		resultsEl.setVisible(false);
		resultsMoreEl.setVisible(false);
	}

	public static final Event BROWSE_EVENT = new Event("tax-selection-browse");
	public static class SelectionEvent extends Event {

		private static final long serialVersionUID = 785343886939702394L;

		private final Set<Long> keys;

		public SelectionEvent(Set<Long> keys) {
			super("tax-selection");
			this.keys = keys;
		}

		public Set<Long> getKeys() {
			return keys;
		}

	}
	
}
