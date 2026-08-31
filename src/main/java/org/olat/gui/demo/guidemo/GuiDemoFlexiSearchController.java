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
package org.olat.gui.demo.guidemo;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SearchElement;
import org.olat.core.gui.components.form.flexible.elements.SearchVariant;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.SearchFormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.util.StringHelper;

/**
 * Demonstrates the unified {@link SearchElement}: the three variants and the
 * search / reset event contract.
 *
 * Initial date: 2026-08-25<br>
 * @author uhensler, https://www.frentix.com
 */
public class GuiDemoFlexiSearchController extends FormBasicController {

	private SearchElement typeaheadEl;
	private SearchElement largeEl;
	private SearchElement fixedWidthEl;
	private SearchElement defaultNoButtonEl;
	private StaticTextElement resultsEl;
	private StaticTextElement eventLogEl;

	public GuiDemoFlexiSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer defaultContCont = uifactory.addDefaultFormLayout("defaultCont", null, formLayout);
		defaultContCont.setFormTitle(translate("search.demo.default"));
		uifactory.addSearchElement("search.default", SearchVariant.DEFAULT, defaultContCont);

		FormLayoutContainer largeCont = uifactory.addDefaultFormLayout("largeCont", null, formLayout);
		largeCont.setFormTitle(translate("search.demo.large"));
		largeEl = uifactory.addSearchElement("search.large", SearchVariant.LARGE, largeCont);
		largeEl.setPlaceholderKey("search.demo.large.placeholder");

		FormLayoutContainer fixedWidthCont = uifactory.addDefaultFormLayout("fixedWidthCont", null, formLayout);
		fixedWidthCont.setFormTitle(translate("search.demo.fixed.width"));
		fixedWidthEl = uifactory.addSearchElement("search.fixed.width", SearchVariant.DEFAULT, fixedWidthCont);
		fixedWidthEl.setElementCssClass("o_search_demo_fixed_width");

		FormLayoutContainer noButtonCont = uifactory.addDefaultFormLayout("noButtonCont", null, formLayout);
		noButtonCont.setFormTitle(translate("search.demo.default.no.button"));
		defaultNoButtonEl = uifactory.addSearchElement("search.default.no.button", SearchVariant.DEFAULT, noButtonCont);
		defaultNoButtonEl.setSearchButtonVisible(false);

		FormLayoutContainer typeaheadCont = uifactory.addDefaultFormLayout("typeaheadCont", null, formLayout);
		typeaheadCont.setFormTitle(translate("search.demo.typeahead"));
		typeaheadEl = uifactory.addSearchElement("search.typeahead", SearchVariant.TYPEAHEAD, typeaheadCont);
		typeaheadEl.setPlaceholderKey("search.demo.typeahead.placeholder");
		resultsEl = uifactory.addStaticTextElement("search.results", "search.demo.results", "", typeaheadCont);

		FormLayoutContainer providerCont = uifactory.addDefaultFormLayout("providerCont", null, formLayout);
		providerCont.setFormTitle(translate("search.demo.typeahead.provider"));
		uifactory.addSearchElement("search.typeahead.provider", createNameListProvider(),
				ureq.getUserSession(), providerCont);

		FormLayoutContainer logCont = uifactory.addDefaultFormLayout("logCont", null, formLayout);
		logCont.setFormTitle(translate("search.demo.event.log"));
		eventLogEl = uifactory.addStaticTextElement("search.event.log", "search.demo.event.log.label", "", logCont);
	}

	private ListProvider createNameListProvider() {
		return new ListProvider() {
			@Override
			public int getMaxEntries() {
				return 10;
			}

			@Override
			public void getResult(String searchValue, ListReceiver receiver) {
				for (String name : NameSource.ALL) {
					if (name.toLowerCase().contains(searchValue.toLowerCase())) {
						receiver.addEntry(name, name);
					}
				}
			}
		};
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (event instanceof SearchFormEvent searchEvent) {
			if (source == typeaheadEl) {
				updateResults(searchEvent.getSearchText());
			}
			eventLogEl.setValue(searchEvent.getCommand() + ": \"" + searchEvent.getSearchText() + "\"");
		}
	}

	private void updateResults(String searchText) {
		List<String> matches = NameSource.ALL.stream()
				.filter(name -> !StringHelper.containsNonWhitespace(searchText)
						|| name.toLowerCase().contains(searchText.toLowerCase()))
				.limit(10)
				.collect(Collectors.toList());
		resultsEl.setValue(String.join(", ", matches));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
