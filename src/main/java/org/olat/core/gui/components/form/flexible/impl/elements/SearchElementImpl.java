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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.elements.SearchVariant;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;

/**
 * Composite FormItem for the unified search field. Owns the search text
 * input (a plain {@link TextElementImpl} or, when a {@link ListProvider} is
 * given, an {@link AutoCompleterImpl}), the search button and the reset
 * button, and encapsulates the behaviour that every hand-built search field
 * used to duplicate: reset-button visibility, dirty-flag suppression while
 * the term changes, and focus restore after a reset.
 *
 * Initial date: 2026-08-25<br>
 * @author uhensler, https://www.frentix.com
 */
public class SearchElementImpl extends FormItemImpl implements SearchElement, FormItemCollection, ComponentEventListener, Disposable {

	private final SearchVariant variant;
	private final AbstractTextElement searchInputEl;
	private final FormLinkImpl searchButtonEl;
	private final FormLinkImpl resetButtonEl;
	private final SearchElementComponent component;

	private boolean propagateDirtiness = false;
	private boolean searchButtonLabelVisible = false;
	private int minLength = 0;
	private BiConsumer<UserRequest, String> autoCompleteSelectListener;

	public SearchElementImpl(String id, String name, SearchVariant variant, Locale locale) {
		this(id, name, variant, locale, null, null);
	}

	public SearchElementImpl(String id, String name, Locale locale, ListProvider provider, UserSession usess) {
		this(id, name, SearchVariant.TYPEAHEAD, locale, provider, usess);
	}

	public SearchElementImpl(String id, String name, SearchVariant variant, Locale locale,
			ListProvider provider, UserSession usess) {
		super(name);
		this.variant = variant;

		String baseId = StringHelper.containsNonWhitespace(id) ? id : String.valueOf(CodeHelper.getRAMUniqueID());

		if (provider != null) {
			AutoCompleterImpl autoCompleterEl = new AutoCompleterImpl(baseId + "_input", name, locale);
			autoCompleterEl.setListProvider(provider, usess);
			autoCompleterEl.getComponent().addListener(this);
			searchInputEl = autoCompleterEl;
		} else {
			searchInputEl = new TextElementImpl(baseId + "_input", name, "");
		}
		searchInputEl.setDomReplacementWrapperRequired(false);
		searchInputEl.showLabel(false);
		searchInputEl.setElementCssClass("o_search_input");
		searchInputEl.setAriaRole(TextElement.ARIA_ROLE_SEARCHBOX);
		searchInputEl.setAutocomplete("off");
		searchInputEl.setDirtyMarkingEnabled(false);
		if (variant == SearchVariant.TYPEAHEAD) {
			searchInputEl.addActionListener(FormEvent.ONKEYUP);
		}

		searchButtonEl = new FormLinkImpl(baseId + "_search", "rSearchB", "", Link.BUTTON | Link.NONTRANSLATED);
		searchButtonEl.setDomReplacementWrapperRequired(false);
		searchButtonEl.setElementCssClass("o_search_button");
		searchButtonEl.setIconLeftCSS("o_icon o_icon-fw o_icon_search");
		searchButtonEl.setVisible(variant != SearchVariant.TYPEAHEAD);
		searchButtonLabelVisible = variant == SearchVariant.LARGE;

		resetButtonEl = new FormLinkImpl(baseId + "_reset", "rSearchReset", "", Link.BUTTON_SMALL | Link.NONTRANSLATED);
		resetButtonEl.setDomReplacementWrapperRequired(true);
		resetButtonEl.setElementCssClass("o_search_reset");
		resetButtonEl.setIconLeftCSS("o_icon o_icon-fw o_icon_close");
		resetButtonEl.setAriaRole(Link.ARIA_ROLE_BUTTON);
		resetButtonEl.setGhost(true);

		component = new SearchElementComponent(this, baseId + "_cmp");
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	SearchVariant getVariant() {
		return variant;
	}

	@Override
	public String getValue() {
		return searchInputEl.getValue();
	}

	@Override
	public void setValue(String value) {
		if (searchInputEl instanceof AutoCompleterImpl autoCompleterEl) {
			autoCompleterEl.setKey(null);
		}
		searchInputEl.setValue(value);
	}

	@Override
	public void setPlaceholderText(String placeholder) {
		searchInputEl.setPlaceholderText(placeholder);
	}

	@Override
	public void setPlaceholderKey(String i18nKey, String... args) {
		searchInputEl.setPlaceholderKey(i18nKey, args);
	}

	@Override
	public void setAriaLabel(String ariaLabel) {
		searchInputEl.setAriaLabel(ariaLabel);
	}

	@Override
	public void setAriaControls(String controlsId) {
		searchInputEl.setAriaControls(controlsId);
	}

	@Override
	public void setSearchButtonVisible(boolean visible) {
		searchButtonEl.setVisible(visible);
	}

	boolean isSearchButtonVisible() {
		return searchButtonEl.isVisible();
	}

	@Override
	public void setSearchButtonLabelVisible(boolean visible) {
		this.searchButtonLabelVisible = visible;
		updateSearchButtonLabel();
	}

	@Override
	public void setMinLength(int minLength) {
		this.minLength = minLength;
		if (searchInputEl instanceof AutoCompleterImpl autoCompleterEl) {
			autoCompleterEl.setMinLength(minLength);
		}
	}

	@Override
	public void setMaxLength(int maxLength) {
		searchInputEl.setMaxLength(maxLength);
	}

	@Override
	public void setAutoCompleteSelectListener(BiConsumer<UserRequest, String> listener) {
		this.autoCompleteSelectListener = listener;
	}

	@Override
	public void setAutoCompleteShowDisplayKey(boolean showDisplayKey) {
		if (searchInputEl instanceof AutoCompleterImpl autoCompleterEl) {
			autoCompleterEl.setShowDisplayKey(showDisplayKey);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (autoCompleteSelectListener != null && searchInputEl.getComponent() == source
				&& event instanceof AutoCompleteEvent ace) {
			autoCompleteSelectListener.accept(ureq, ace.getKey());
		}
	}

	@Override
	public void setFocus(boolean hasFocus) {
		super.setFocus(hasFocus);
		searchInputEl.setFocus(hasFocus);
	}

	@Override
	public void setPropagateDirtiness(boolean propagate) {
		this.propagateDirtiness = propagate;
	}

	public boolean isPropagateDirtiness() {
		return propagateDirtiness;
	}

	@Override
	public void reset() {
		if (getRootForm() != null) {
			// The traversal that dispatches this reset also calls evalFormRequest()
			// on searchInputEl separately afterwards; without removing its raw
			// request parameter first, that call re-applies the stale submitted
			// value and undoes the reset.
			getRootForm().removeRequestParameter(searchInputEl.getFormDispatchId());
		}
		searchInputEl.setValue(null);
		if (getRootForm() != null) {
			Command focusCommand = FormJSHelper.getFormFocusCommand(getRootForm().getFormName(), searchInputEl.getForId());
			getRootForm().getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
		}
	}

	TextElement getSearchInput() {
		return searchInputEl;
	}

	FormLink getSearchButton() {
		return searchButtonEl;
	}

	FormLink getResetButton() {
		return resetButtonEl;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return List.of(searchInputEl, searchButtonEl, resetButtonEl);
	}

	@Override
	public FormItem getFormComponent(String name) {
		for (FormItem item : getFormItems()) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public void setTranslator(Translator translator) {
		Translator elmTranslator = Util.createPackageTranslator(SearchElementImpl.class, translator.getLocale(), translator);
		super.setTranslator(elmTranslator);
		searchInputEl.setTranslator(elmTranslator);
		searchButtonEl.setTranslator(elmTranslator);
		resetButtonEl.setTranslator(elmTranslator);

		if (!StringHelper.containsNonWhitespace(searchInputEl.getAriaLabel())) {
			searchInputEl.setAriaLabel(elmTranslator.translate("search.term.aria"));
		}
		if (!searchInputEl.hasPlaceholder()) {
			searchInputEl.setPlaceholderKey("enter.search.term", null);
		}
		searchButtonEl.setTitle(elmTranslator.translate("search.button.aria"));
		searchButtonEl.setAriaLabel(elmTranslator.translate("search.button.aria"));
		updateSearchButtonLabel();
		resetButtonEl.setTitle(elmTranslator.translate("search.reset"));
		resetButtonEl.setAriaLabel(elmTranslator.translate("search.reset"));
	}

	private void updateSearchButtonLabel() {
		if (getTranslator() != null && searchButtonLabelVisible) {
			searchButtonEl.setI18nKey(getTranslator().translate("search.button.aria"));
		}
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		searchInputEl.setEnabled(isEnabled);
		searchButtonEl.setEnabled(isEnabled);
		resetButtonEl.setEnabled(isEnabled);
	}

	@Override
	protected void rootFormAvailable() {
		if (searchInputEl.getRootForm() != getRootForm()) {
			searchInputEl.setRootForm(getRootForm());
		}
		if (searchButtonEl.getRootForm() != getRootForm()) {
			searchButtonEl.setRootForm(getRootForm());
		}
		if (resetButtonEl.getRootForm() != getRootForm()) {
			resetButtonEl.setRootForm(getRootForm());
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		SearchFormEvent event = evalLocalDispatch(ureq);
		if (event != null && !getRootForm().hasAlreadyFired()) {
			getRootForm().fireFormEvent(ureq, event);
		}
	}

	@Override
	public SearchFormEvent evalLocalDispatch(UserRequest ureq) {
		searchInputEl.evalFormRequest(ureq);
		// setValue() marked the input dirty; clear it now so this request's AJAX
		// response does not replace the input DOM node and drop the next keystroke.
		searchInputEl.getComponent().setDirty(false);

		if (getRootForm().hasAlreadyFired()) {
			return null;
		}

		String dispatchuri = getRootForm().getRequestParameter("dispatchuri");
		if (dispatchuri == null) {
			return null;
		}

		if (dispatchuri.equals(resetButtonEl.getFormDispatchId())) {
			reset();
			return new SearchFormEvent(SearchFormEvent.RESET, this, null);
		} else if (dispatchuri.equals(searchButtonEl.getFormDispatchId())) {
			return new SearchFormEvent(SearchFormEvent.SEARCH, this, getValue());
		} else if (dispatchuri.equals(searchInputEl.getFormDispatchId())) {
			if (searchInputEl instanceof AutoCompleterImpl autoCompleterEl) {
				autoCompleterEl.dispatchFormRequest(ureq);
			}
			if (variant == SearchVariant.TYPEAHEAD) {
				return searchIfAllowed();
			}
		}
		return null;
	}

	private SearchFormEvent searchIfAllowed() {
		String searchText = getValue();
		boolean allowed = minLength <= 0
				|| !StringHelper.containsNonWhitespace(searchText)
				|| searchText.length() >= minLength;
		return allowed ? new SearchFormEvent(SearchFormEvent.SEARCH, this, searchText) : null;
	}

	@Override
	public void dispose() {
		if (searchInputEl instanceof AutoCompleterImpl autoCompleterEl) {
			autoCompleterEl.dispose();
		}
	}

}
