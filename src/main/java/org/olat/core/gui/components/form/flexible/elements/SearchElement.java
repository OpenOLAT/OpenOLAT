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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.function.BiConsumer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.elements.SearchFormEvent;

/**
 * A unified search input field. Encapsulates the search term input, the
 * search button, the reset button, dirty-flag suppression and focus restore
 * after a reset. See {@link SearchVariant} for the available layouts.
 *
 * Initial date: 2026-08-25<br>
 * @author uhensler, https://www.frentix.com
 */
public interface SearchElement extends FormItem {

	String getValue();

	void setValue(String value);

	void setPlaceholderText(String placeholder);

	void setPlaceholderKey(String i18nKey, String... args);

	void setAriaLabel(String ariaLabel);

	/**
	 * @param controlsId The DOM id of the component the search field controls,
	 *                   obtained from
	 *                   {@link org.olat.core.gui.render.Renderer#getComponentPrefix(org.olat.core.gui.components.Component)}
	 */
	void setAriaControls(String controlsId);

	void setSearchButtonVisible(boolean visible);

	/**
	 * {@link SearchVariant#LARGE} defaults to true, every other variant to
	 * false. There must be a good reason to deviate from that default.
	 */
	void setSearchButtonLabelVisible(boolean visible);

	void setMinLength(int minLength);

	void setMaxLength(int maxLength);

	/**
	 * Whether the search field marks the surrounding form dirty when the
	 * search term changes. Default is false: the search element handles its
	 * own request round trip and does not trigger the "unsaved changes"
	 * dialog of the surrounding form.
	 */
	void setPropagateDirtiness(boolean propagate);

	/**
	 * For composites that embed a SearchElement as a child and want to
	 * intercept search/reset themselves instead of receiving an
	 * automatically-fired SearchFormEvent on the root form (e.g. a FlexiTable,
	 * which must translate it into its own event and reload data). Applies
	 * the value/reset side effects and returns the resulting event, or null
	 * if this request doesn't target this element. The caller is responsible
	 * for firing it, or not.
	 */
	SearchFormEvent evalLocalDispatch(UserRequest ureq);

	/**
	 * For composites that embed a SearchElement and want to react to a
	 * typeahead suggestion being selected, when this element is backed by a
	 * {@link org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider}:
	 * called with the selected suggestion's key. Picking a suggestion is a
	 * component-level dispatch, not a form submit, so it never reaches
	 * {@link #evalLocalDispatch(UserRequest)}. No-op for a plain text field.
	 */
	void setAutoCompleteSelectListener(BiConsumer<UserRequest, String> listener);

	void setAutoCompleteShowDisplayKey(boolean showDisplayKey);

}
