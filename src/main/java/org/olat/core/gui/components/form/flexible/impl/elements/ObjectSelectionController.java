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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
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
	
	private static final int MORE_SIZE = 100;

	private TextElement searchTermEl;
	private FormLink searchResetLink;
	private FormLink selectAllLink;
	private FormLink selectionResetLink;
	private FormExpandButton selectionsExpandButton;
	private MultipleSelectionElement selectionEl;
	private StaticTextElement nothingFoundEl;

	private final List<GroupBinding> bindings = new ArrayList<>();
	private final boolean multiSelection;
	private final ObjectSelectionSource source;
	private final String popupCssClass;
	private Set<String> selectedKeys;

	public ObjectSelectionController(UserRequest ureq, WindowControl wControl, boolean multiSelection,
			ObjectSelectionSource source, Set<String> selectedKeys, String popupCssClass) {
		super(ureq, wControl, "object_selection");
		this.multiSelection = multiSelection;
		this.source = source;
		this.popupCssClass = popupCssClass;
		this.selectedKeys = new HashSet<>(selectedKeys);
		initForm(ureq);
		onSelectionChanged();
		doExpandSelection();
		doResetSearch();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("popupCssClass", StringHelper.containsNonWhitespace(popupCssClass) ? popupCssClass : "");
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
		
		if (multiSelection) {
			selectAllLink = uifactory.addFormLink("select.all", formLayout);
			selectAllLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
			
			selectionResetLink = uifactory.addFormLink("selection.reset", formLayout);
			selectionResetLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
		
			selectionsExpandButton = uifactory.addExpandLink("selections.expand", formLayout);
			
			selectionEl = uifactory.addCheckboxesVertical("selection", null, formLayout, emptyStrings(), emptyStrings(), 1);
			selectionEl.setEscapeHtml(false);
			selectionEl.addActionListener(FormEvent.ONCHANGE);
			selectionsExpandButton.setAriaControls(Renderer.getComponentPrefix(selectionEl.getComponent()));
		}

		List<ObjectOptionGroup> groups = source.getOptionGroups(getLocale());
		boolean multiGroup = groups.size() > 1;
		List<String> groupContainerNames = new ArrayList<>(groups.size());
		for (int i = 0; i < groups.size(); i++) {
			ObjectOptionGroup group = groups.get(i);

			FormLayoutContainer groupContainer = FormLayoutContainer.createCustomFormLayout(
				"group_" + i, getTranslator(), velocity_root + "/object_selection_group.html");
			groupContainer.setElementCssClass("o_object_selection_group");
			formLayout.add(groupContainer);
			groupContainerNames.add(groupContainer.getName());

			FormExpandButton expandButton = null;
			if (multiGroup) {
				expandButton = uifactory.addExpandLink("selection.expand_g" + i, groupContainer);
				if (StringHelper.containsNonWhitespace(group.getSubLabel())) {
					expandButton.setEscapeMode(EscapeMode.none);
					expandButton.setText(StringHelper.escapeHtml(Objects.requireNonNullElse(group.getLabel(), translate("options")))
							+ " - <small>" + StringHelper.escapeHtml(group.getSubLabel()) + "</small>");
				} else {
					expandButton.setText(Objects.requireNonNullElse(group.getLabel(), translate("options")));
				}
				expandButton.setExpanded(true);
			}

			FormLink loadMoreLink = uifactory.addFormLink("selection.load.more_g" + i, "selection.load.more", null, groupContainer, Link.LINK);
			loadMoreLink.setIconLeftCSS("o_icon o_icon_load_more");
			loadMoreLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
			loadMoreLink.setDomReplacementWrapperRequired(true);

			GroupBinding binding;
			if (multiSelection) {
				MultipleSelectionElement multiEl = uifactory.addCheckboxesVertical("options_g" + i, null, groupContainer, emptyStrings(), emptyStrings(), 1);
				multiEl.setEscapeHtml(false);
				multiEl.addActionListener(FormEvent.ONCHANGE);
				binding = new GroupBinding(multiEl, null, loadMoreLink, expandButton, groupContainer);
			} else {
				SingleSelection singleEl = uifactory.addRadiosVertical("single.selection_g" + i, null, groupContainer, emptyStrings(), emptyStrings());
				singleEl.setEscapeHtml(false);
				singleEl.addActionListener(FormEvent.ONCHANGE);
				binding = new GroupBinding(null, singleEl, loadMoreLink, expandButton, groupContainer);
			}

			groupContainer.contextPut("groupLabel", Objects.requireNonNullElse(group.getLabel(), translate("options")));
			groupContainer.contextPut("groupSubLabel", StringHelper.containsNonWhitespace(group.getSubLabel()) ? group.getSubLabel() : null);
			groupContainer.contextPut("expandButtonName", expandButton != null ? expandButton.getName() : "");
			groupContainer.contextPut("elementName", binding.multiEl != null ? binding.multiEl.getName() : binding.singleEl.getName());
			groupContainer.contextPut("loadMoreName", loadMoreLink.getName());

			bindings.add(binding);
		}
		flc.contextPut("groupContainerNames", groupContainerNames);

		if (!bindings.isEmpty()) {
			if (multiSelection) {
				searchTermEl.setAriaControls(bindings.get(0).multiEl.getFormDispatchId());
			} else {
				searchTermEl.setAriaControls(bindings.get(0).singleEl.getFormDispatchId());
			}
		}

		nothingFoundEl = uifactory.addStaticTextElement("options.nothing.found", null, "", formLayout);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (searchTermEl == source) {
			bindings.forEach(b -> b.optionLimit = MORE_SIZE);
			doSearch();
		} else if (searchResetLink == source) {
			doResetSearch();
		} else if (selectAllLink == source) {
			doSelectAll();
			fireEvent(ureq, new SelectionEvent(selectedKeys));
		} else if (selectionResetLink == source) {
			doResetSelection();
			fireEvent(ureq, new SelectionEvent(selectedKeys));
		} else if (selectionsExpandButton == source) {
			doExpandSelection();
		} else if (selectionEl == source) {
			doUnselect();
			fireEvent(ureq, new SelectionEvent(selectedKeys));
		} else {
			for (GroupBinding b : bindings) {
				if (multiSelection && b.multiEl == source) {
					doToggleOption(b);
					fireEvent(ureq, new SelectionEvent(selectedKeys));
					break;
				} else if (!multiSelection && b.singleEl == source) {
					doSelectSingle(b);
					fireEvent(ureq, new SelectionEvent(selectedKeys));
					break;
				} else if (b.loadMoreLink == source) {
					b.optionLimit += MORE_SIZE;
					doSearch();
					break;
				} else if (b.expandButton == source) {
					b.expanded = b.expandButton.isExpanded();
					doSearch();
					break;
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == searchTermEl) {
			return;
		}
		for (GroupBinding b : bindings) {
			if (multiSelection && b.multiEl == source) return;
			if (!multiSelection && b.singleEl == source) return;
		}
		super.propagateDirtinessToContainer(source, fe);
	}

	private void doSearch() {
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

		List<ObjectOptionGroup> groups = source.getOptionGroups(getLocale());
		boolean anyVisible = false;
		for (int i = 0; i < bindings.size() && i < groups.size(); i++) {
			GroupBinding b = bindings.get(i);
			ObjectOptionGroup group = groups.get(i);

			List<? extends ObjectOption> matched = group.getOptions().stream()
				.filter(searchFilter)
				.collect(Collectors.toList());

			boolean truncated = b.optionLimit < matched.size();
			if (truncated) {
				matched = matched.subList(0, b.optionLimit);
			}

			SelectionValues sv = new SelectionValues();
			matched.forEach(option -> sv.add(SelectionValues.entry(option.getKey(), toSelectionValue(option))));

			boolean groupHasResults = !matched.isEmpty();
			boolean groupElementVisible = groupHasResults && b.expanded;
			boolean newLoadMoreVisible = truncated && groupElementVisible;
			boolean newExpandButtonVisible = b.expandButton != null && groupHasResults;

			boolean prevContainerVisible = b.container.isVisible();
			boolean prevElementVisible = multiSelection ? b.multiEl.isVisible() : b.singleEl.isVisible();
			boolean prevLoadMoreVisible = b.loadMoreLink.isVisible();
			boolean prevExpandButtonVisible = b.expandButton != null && b.expandButton.isVisible();

			if (multiSelection) {
				b.multiEl.setKeysAndValues(sv.keys(), sv.values());
				b.multiEl.getComponent().setDirty(true);
				b.multiEl.setVisible(groupElementVisible);
				updateOptionSelections(b);
			} else {
				b.singleEl.setKeysAndValues(sv.keys(), sv.values(), null);
				b.singleEl.getComponent().setDirty(true);
				b.singleEl.setVisible(groupElementVisible);
				updateSingleSelection(b);
			}
			b.loadMoreLink.setVisible(newLoadMoreVisible);
			if (b.expandButton != null) {
				b.expandButton.setVisible(newExpandButtonVisible);
			}
			b.container.setVisible(groupHasResults);

			// Only dirty the container when its template structure changes; an unconditional
			// dirty wipes the scroll anchor inside o_object_selection_main on load more.
			boolean templateStateChanged = prevContainerVisible != groupHasResults
					|| prevElementVisible != groupElementVisible
					|| prevLoadMoreVisible != newLoadMoreVisible
					|| prevExpandButtonVisible != newExpandButtonVisible;
			if (templateStateChanged) {
				b.container.getComponent().setDirty(true);
			}

			if (groupHasResults) {
				anyVisible = true;
			}
		}

		searchTermEl.getComponent().setDirty(false);
		nothingFoundEl.setValue(anyVisible ? "" : "<i>" + translate("options.nothing.found") + "</i>");
		searchResetLink.setVisible(StringHelper.containsNonWhitespace(searchText));
	}

	private Predicate<? super ObjectOption> createSearchFilter(Set<String> searchTerms) {
		return option -> {
			for (String searchTerm : searchTerms) {
				if (!isMatch(searchTerm, option.getTitle()) && !isMatch(searchTerm, option.getSubTitle())) {
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
		sb.append("<div class=\"o_object_selection_option ");
		sb.append(StringHelper.blankIfNull(option.getOptionCss()));
		sb.append("\">");
		if (StringHelper.containsNonWhitespace(option.getImageHtml())) {
			sb.append("<div class=\"o_object_selection_image\">");
			sb.append(option.getImageHtml());
			sb.append("</div>");
		} else if (StringHelper.containsNonWhitespace(option.getImageSrc())) {
			sb.append("<div class=\"o_object_selection_image\">");
			sb.append("<img src=\"").append(option.getImageSrc()).append("\" ");
			sb.append("alt =\"").append(StringHelper.blankIfNull(StringHelper.escapeForHtmlAttribute(option.getImageAlt()))).append("\"");
			sb.append(">");
			sb.append("</div>");
		}
		sb.append("<div class=\"o_nowrap o_object_selection_title\">");
		sb.append(StringHelper.escapeHtml(option.getTitle()));
		sb.append("</div>");
		if (StringHelper.containsNonWhitespace(option.getSubTitle())) {
			sb.append("<div class=\"o_nowrap o_muted o_object_selection_subtitle o_fit_path\"><small>");
			sb.append(StringHelper.escapeHtml(option.getSubTitle()));
			sb.append("</small></div>");
		}
		sb.append("</div>");
		return sb.toString();
	}
	
	private void doResetSearch() {
		searchTermEl.setValue(null);
		bindings.forEach(b -> b.optionLimit = MORE_SIZE);
		doSearch();

		Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(), searchTermEl.getForId());
		mainForm.getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}

	private void doSelectAll() {
		for (GroupBinding b : bindings) {
			selectedKeys.addAll(b.multiEl.getKeys());
		}
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
			Set<String> allOptionKeys = source.getOptionGroups(getLocale()).stream()
				.flatMap(g -> g.getOptions().stream())
				.map(ObjectOption::getKey)
				.collect(Collectors.toSet());
			for (String key : keys) {
				if (allOptionKeys.contains(key)) {
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
		for (GroupBinding b : bindings) {
			if (b.singleEl.containsKey(key)) {
				b.singleEl.select(key, true);
				doSelectSingle(b);
				return;
			}
		}
	}

	private void doToggleOption(GroupBinding binding) {
		MultipleSelectionElement el = binding.multiEl;
		for (String key : el.getSelectedKeys()) {
			if (!selectedKeys.contains(key)) {
				selectedKeys.add(key);
				onSelectionChanged();
				return;
			}
		}

		Set<String> optionKeys = el.getKeys();
		for (String key : selectedKeys) {
			if (optionKeys.contains(key) && !el.isKeySelected(key)) {
				selectedKeys.remove(key);
				onSelectionChanged();
				return;
			}
		}
	}

	private void doSelectSingle(GroupBinding binding) {
		selectedKeys.clear();
		if (binding.singleEl.isOneSelected()) {
			selectedKeys.add(binding.singleEl.getSelectedKey());
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
		if (!multiSelection) {
			for (GroupBinding b : bindings) {
				updateSingleSelection(b);
			}
			return;
		}
		
		SelectionValues selectionsSV = new SelectionValues();
		source.getOptionGroups(getLocale()).stream()
			.flatMap(g -> g.getOptions().stream())
			.filter(option -> selectedKeys.contains(option.getKey()))
			.forEach(option -> selectionsSV.add(SelectionValues.entry(option.getKey(), toSelectionValue(option))));

		selectionEl.setKeysAndValues(selectionsSV.keys(), selectionsSV.values());
		selectionEl.getKeys().stream().forEach(key -> selectionEl.select(key, true));

		for (GroupBinding b : bindings) {
			updateOptionSelections(b);
		}

		selectionsExpandButton.setText(translate("selection.num", String.valueOf(selectionsSV.size())));
		selectionsExpandButton.setVisible(true);
		selectionResetLink.setEnabled(selectionsSV.size() > 0);
	}
	
	private void updateOptionSelections(GroupBinding b) {
		b.multiEl.getKeys().forEach(key -> b.multiEl.select(key, selectedKeys.contains(key)));
	}

	private void updateSingleSelection(GroupBinding b) {
		if (b.singleEl.isOneSelected() && !selectedKeys.contains(b.singleEl.getSelectedKey())) {
			b.singleEl.select(b.singleEl.getSelectedKey(), false);
		}
		if (!selectedKeys.isEmpty() && !b.singleEl.isOneSelected()) {
			String selectedKey = List.copyOf(selectedKeys).get(0);
			if (Arrays.asList(b.singleEl.getKeys()).contains(selectedKey)) {
				b.singleEl.select(selectedKey, true);
			}
		}
	}

	private static final class GroupBinding {

		final MultipleSelectionElement multiEl;
		final SingleSelection singleEl;
		final FormLink loadMoreLink;
		final FormExpandButton expandButton;
		final FormLayoutContainer container;
		int optionLimit = MORE_SIZE;
		boolean expanded = true;

		GroupBinding(MultipleSelectionElement multiEl, SingleSelection singleEl, FormLink loadMoreLink, FormExpandButton expandButton, FormLayoutContainer container) {
			this.multiEl = multiEl;
			this.singleEl = singleEl;
			this.loadMoreLink = loadMoreLink;
			this.expandButton = expandButton;
			this.container = container;
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
