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
package org.olat.core.gui.components.form.flexible.impl.elements;

import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.expand.FormExpandButton;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.render.Renderer;
import org.olat.core.util.StringHelper;


/**
 * 
 * Initial date: Sep 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ObjectSelectionController extends FormBasicController {
	
	static final Event OPEN_BROWSER_EVENT = new Event("open-browser");
	private static final int MORE_SIZE = 50;
	
	private TextElement searchTermEl;
	private FormLink searchResetLink;
	private FormLink openBrowserLink;
	private FormLink selectAllLink;
	private FormLink selectionResetLink;
	private FormExpandButton selectionsExpandButton;
	private MultipleSelectionElement selectionEl;
	private MultipleSelectionElement optionsEl;
	private SingleSelection singleSelectionEl;
	private FormLink loadMoreLink;
	private StaticTextElement nothingFoundEl;
	private FormLink applyButton;
	
	private final boolean multiSelection;
	private final ObjectSelectionSource source;
	private Set<String> selectedKeys;
	private int optionLimit;

	public ObjectSelectionController(UserRequest ureq, WindowControl wControl, boolean multiSelection,
			ObjectSelectionSource source, Set<String> selectedKeys) {
		super(ureq, wControl, "object_selection");
		this.multiSelection = multiSelection;
		this.source = source;
		this.selectedKeys = new HashSet<>(selectedKeys);
		
		initForm(ureq);
		onSelectionChanged();
		doExpandSelection();
		doResetSearch();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchTermEl = uifactory.addTextElement("search.term", null, 100, "", formLayout);
		searchTermEl.setDomReplacementWrapperRequired(false);
		searchTermEl.setElementCssClass("o_search_term");
		searchTermEl.setAriaLabel(translate("search.term.aria"));
		searchTermEl.setAriaRole(TextElement.ARIA_ROLE_SEARCHBOX);
		searchTermEl.setAutocomplete("off");
		searchTermEl.addActionListener(FormEvent.ONKEYUP);
		
		searchResetLink = uifactory.addFormLink("search.reset", "", null, formLayout, Link.BUTTON_SMALL | Link.NONTRANSLATED);
		searchResetLink.setDomReplacementWrapperRequired(true);
		searchResetLink.setElementCssClass("o_reset_search");
		searchResetLink.setTitle(translate("search.reset"));
		searchResetLink.setIconLeftCSS("o_icon o_icon_remove_filters");
		
		openBrowserLink = uifactory.addFormLink("browser.open", "browser.open", null, formLayout, Link.BUTTON);
		openBrowserLink.setElementCssClass("o_open_browser");
		openBrowserLink.setIconLeftCSS("o_icon o_icon-fw o_icon_browse");
		openBrowserLink.setVisible(source.isBrowserAvailable());
		
		if (multiSelection) {
			selectAllLink = uifactory.addFormLink("select.all", formLayout);
			selectAllLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
			
			selectionResetLink = uifactory.addFormLink("selection.reset", formLayout);
			selectionResetLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
		
			selectionsExpandButton = uifactory.addExpandLink("selections.expand", formLayout);
			
			selectionEl = uifactory.addCheckboxesVertical("selection", null, formLayout, emptyStrings(), emptyStrings(), 1);
			selectionEl.setHorizontallyAlignedCheckboxes(true);
			selectionEl.setEscapeHtml(false);
			selectionEl.addActionListener(FormEvent.ONCHANGE);
			selectionsExpandButton.setAriaControls(Renderer.getComponentPrefix(selectionEl.getComponent()));
			
			optionsEl = uifactory.addCheckboxesVertical("options", null, formLayout, emptyStrings(), emptyStrings(), 1);
			optionsEl.setHorizontallyAlignedCheckboxes(true);
			optionsEl.setEscapeHtml(false);
			optionsEl.addActionListener(FormEvent.ONCHANGE);
			searchTermEl.setAriaControls(optionsEl.getFormDispatchId());
			
			if (formLayout instanceof FormLayoutContainer container) {
				String optionsLabel = source.getOptionsLabel(getLocale());
				container.contextPut("optionsLabel", StringHelper.containsNonWhitespace(optionsLabel)? optionsLabel: translate("options"));
			}
		} else {
			singleSelectionEl = uifactory.addRadiosVertical("single.selection", null, formLayout, emptyStrings(), emptyStrings());
			singleSelectionEl.setEscapeHtml(false);
			singleSelectionEl.addActionListener(FormEvent.ONCHANGE);
			searchTermEl.setAriaControls(singleSelectionEl.getFormDispatchId());
		}
		
		loadMoreLink = uifactory.addFormLink("selection.load.more", formLayout, Link.LINK);
		loadMoreLink.setIconLeftCSS("o_icon o_icon_load_more");
		loadMoreLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
		loadMoreLink.setDomReplacementWrapperRequired(true);
		
		nothingFoundEl = uifactory.addStaticTextElement("options.nothing.found", null, "", formLayout);
		
		applyButton = uifactory.addFormLink("apply", formLayout, Link.BUTTON);
		applyButton.setElementCssClass("o_selection_apply o_button_primary_light");
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if (searchTermEl == source) {
			doSearch(false);
		} else if (searchResetLink == source) {
			doResetSearch();
		} else if (selectAllLink == source) {
			doSelectAll();
		} else if (selectionResetLink == source) {
			doResetSelection();
		} else if (selectionsExpandButton == source) {
			doExpandSelection();
		} else if (selectionEl == source) {
			doUnselect();
		} else if (optionsEl == source) {
			doToggleOption();
		} else if (singleSelectionEl == source) {
			doSelectSingle();
		} else if (loadMoreLink == source) {
			doSearch(true);
		} else if (openBrowserLink == source) {
			fireEvent(ureq, OPEN_BROWSER_EVENT);
		} else if (source == applyButton) {
			doApplySelection(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source != searchTermEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doApplySelection(ureq);
	}

	private void doApplySelection(UserRequest ureq) {
		fireEvent(ureq, new SelectionEvent(selectedKeys));
	}

	private void doSearch(boolean more) {
		if (more) {
			optionLimit += MORE_SIZE;
		} else {
			optionLimit = MORE_SIZE;
		}
		
		String searchText = searchTermEl.getValue();
		
		Predicate<? super ObjectOption> searchFilter;
		if (StringHelper.containsNonWhitespace(searchText)) {
			Set<String> searchTerms = Arrays.stream(searchText.toLowerCase().split(" "))
				.filter(StringHelper::containsNonWhitespace)
				.map(String::trim)
				.collect(Collectors.toSet());
			searchFilter = createSearchFilter(searchTerms);
		} else {
			searchFilter = option -> true;
		}
		
		List<ObjectOption> matchedOptions = source.getOptions().stream()
			.filter(searchFilter)
			.collect(Collectors.toList());
		if (optionLimit < matchedOptions.size()) {
			matchedOptions = matchedOptions.subList(0, optionLimit);
			loadMoreLink.setVisible(true);
		} else {
			loadMoreLink.setVisible(false);
		}
		
		SelectionValues optionsSV = new SelectionValues();
		matchedOptions.forEach(option -> optionsSV.add(SelectionValues.entry(option.getKey(), toSelectionValue(option))));
		if (singleSelectionEl != null) {
			singleSelectionEl.setKeysAndValues(optionsSV.keys(), optionsSV.values(), null);
			singleSelectionEl.getComponent().setDirty(true);
			updateSingleSelection();
		} else {
			optionsEl.setKeysAndValues(optionsSV.keys(), optionsSV.values());
			optionsEl.getComponent().setDirty(true);
			updateOptionSelections();
		}
		
		searchTermEl.getComponent().setDirty(false);
		
		nothingFoundEl.setValue(matchedOptions.isEmpty()? "<i>" + translate("options.nothing.found") + "</i>": "");
		searchResetLink.setVisible(StringHelper.containsNonWhitespace(searchText));
	}

	private Predicate<? super ObjectOption> createSearchFilter(Set<String> searchTerms) {
		return option -> {
			for (String searchTerm : searchTerms) {
				if (!isMatch(searchTerm, option.getTitle()) && !isMatch(searchTerm, option.getSubTitleFull()) && !isMatch(searchTerm, option.getSubTitle())) {
					return false;
				}
			}
			return true;
		};
	}
	
	private boolean isMatch(String searchTerm,  String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			return value.toLowerCase().indexOf(searchTerm) > -1;
		}
		return false;
	}
	
	private String toSelectionValue(ObjectOption option) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"o_object_selection_option\">");
		if (StringHelper.containsNonWhitespace(option.getImageSrc())) {
			sb.append("<div class=\"o_object_selection_image\">");
			sb.append("<img src=\"").append(option.getImageSrc()).append("\" ");
			sb.append("alt =\"").append(StringHelper.blankIfNull(StringHelper.escapeForHtmlAttribute(option.getImageAlt()))).append("\"");
			sb.append(">");
			sb.append("</div>");
		}
		sb.append("<div class=\"o_object_selection_text\">");
		sb.append("<div class=\"o_nowrap\">");
		sb.append(StringHelper.escapeHtml(option.getTitle()));
		sb.append("</div>");
		sb.append("<div class=\"o_nowrap o_muted\"");
		if (StringHelper.containsNonWhitespace(option.getSubTitleFull()) && !Objects.equals(option.getSubTitle(), option.getSubTitleFull())) {
			sb.append("title=\"");
			sb.append(StringHelper.escapeForHtmlAttribute(option.getSubTitleFull()));
			sb.append("\"");
		}
		sb.append("><small>");
		if (StringHelper.containsNonWhitespace(option.getSubTitle())) {
			sb.append(StringHelper.escapeHtml(option.getSubTitle()));
		}
		sb.append("</small></div>");
		sb.append("</div>");
		sb.append("</div>");
		return sb.toString();
	}
	
	private void doResetSearch() {
		searchTermEl.setValue(null);
		doSearch(false);
		
		Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(), searchTermEl.getForId());
		mainForm.getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}

	private void doSelectAll() {
		selectedKeys.addAll(optionsEl.getKeys());
		onSelectionChanged();
	}

	private void doResetSelection() {
		selectedKeys = new HashSet<>(2);
		onSelectionChanged();
	}
	
	private void doUnselect() {
		selectedKeys = new HashSet<>(selectionEl.getSelectedKeys());
		onSelectionChanged();
	}
	
	public void addSelection(Collection<String> keys) {
		if (multiSelection) {
			for (String key : keys) {
				if (source.getOptions().stream().anyMatch(option -> key.equals(option.getKey()))) {
					selectedKeys.add(key);
				}
			}
			onSelectionChanged();
			return;
		}
		
		if (keys == null || keys.size() != 1) {
			return;
		}
		
		String key = List.copyOf(keys).get(0);
		if (singleSelectionEl.containsKey(key)) {
			singleSelectionEl.select(key, true);
			doSelectSingle();
		}
	}

	private void doToggleOption() {
		// Option selected
		for (String key : optionsEl.getSelectedKeys()) {
			if (!selectedKeys.contains(key)) {
				selectedKeys.add(key);
				onSelectionChanged();
				return;
			}
		}
		
		// Option unselected
		Set<String> optionKeys = optionsEl.getKeys();
		for (String key : selectedKeys) {
			if (optionKeys.contains(key) && !optionsEl.isKeySelected(key)) {
				selectedKeys.remove(key);
				onSelectionChanged();
				return;
			}
		}
	}

	private void doSelectSingle() {
		selectedKeys.clear();
		if (singleSelectionEl.isOneSelected()) {
			selectedKeys.add(singleSelectionEl.getSelectedKey());
		}
		
		onSelectionChanged();
	}
	
	private void doExpandSelection() {
		if (selectionEl == null) {
			return;
		}
		
		selectionEl.setVisible(selectionsExpandButton.isExpanded());
		selectionEl.getKeys().stream().forEach(key -> selectionEl.select(key, true));
		onSelectionChanged();
	}
	
	private void onSelectionChanged() {
		if (singleSelectionEl != null) {
			// Set the initially selected key
			updateSingleSelection();
			return;
		}
		
		SelectionValues selectionsSV = new SelectionValues();
		source.getOptions().stream()
			.filter(option -> selectedKeys.contains(option.getKey()))
			.forEach(option -> selectionsSV.add(SelectionValues.entry(option.getKey(), toSelectionValue(option))));

		selectionEl.setKeysAndValues(selectionsSV.keys(), selectionsSV.values());
		selectionEl.getKeys().stream().forEach(key -> selectionEl.select(key, true));
		
		updateOptionSelections();
		
		selectionsExpandButton.setText(translate("selection.num", String.valueOf(selectionsSV.size())));
		
		selectionResetLink.setEnabled(selectionsSV.size() > 0);
	}
	
	private void updateOptionSelections() {
		optionsEl.getKeys().forEach(key -> optionsEl.select(key, selectedKeys.contains(key)));
	}
	
	private void updateSingleSelection() {
		if (!selectedKeys.isEmpty() && !singleSelectionEl.isOneSelected()) {
			String selectedKey = List.copyOf(selectedKeys).get(0);
			if (Arrays.asList(singleSelectionEl.getKeys()).contains(selectedKey)) {
				singleSelectionEl.select(selectedKey, true);
			}
		}
	}
	
	public static final class SelectionEvent extends Event {
		
		private static final long serialVersionUID = 2195120197133429621L;
		
		private final Set<String> selectedKeys;
		
		public SelectionEvent(Set<String> selectedKeys) {
			super("keys-selected");
			this.selectedKeys = selectedKeys;
		}
		
		public Set<String> getSelectedKeys() {
			return selectedKeys;
		}
		
	}

}
