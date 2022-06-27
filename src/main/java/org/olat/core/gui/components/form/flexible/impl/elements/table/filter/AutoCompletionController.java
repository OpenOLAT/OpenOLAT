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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import static org.olat.core.util.ArrayHelper.emptyStrings;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection;
import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection.AutoCompletionSource;
import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection.AutoCompletionSource.SearchResult;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 05 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AutoCompletionController extends FormBasicController {
	
	private FormLink clearButton;
	private FormLink updateButton;
	private FormLink quickSearchButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;
	
	private StaticTextElement selectionNoneEl;
	private MultipleSelectionElement selectionEl;
	private StaticTextElement resultsNoneEl;
	private MultipleSelectionElement resultsEl;
	private StaticTextElement resultsMoreEl;
	
	private final String searchPlaceholder;
	private final AutoCompletionMultiSelection.AutoCompletionSource source; 
	private SelectionValues selectedValues;
	
	public AutoCompletionController(UserRequest ureq, WindowControl wControl, String searchPlaceholder,
			AutoCompletionSource source, SelectionValues selection) {
		super(ureq, wControl, "field_autocompletion", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.searchPlaceholder = searchPlaceholder;
		this.source = source;
		this.selectedValues = selection;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setElementCssClass("o_indicate_search");
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setEnabled(false);
		quickSearchButton.setDomReplacementWrapperRequired(false);
		
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setElementCssClass("o_quick_search");
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.setPlaceholderText(searchPlaceholder);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		
		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("o_reset_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		
		selectionEl = uifactory.addCheckboxesVertical("autocompletion.selection", formLayout, selectedValues.keys(),
				selectedValues.values(), selectedValues.icons(), 1);
		selectionEl.setEscapeHtml(false);
		selectionEl.getKeys().forEach(key -> selectionEl.select(key, true));
		selectionEl.setVisible(!selectionEl.getSelectedKeys().isEmpty());
		
		selectionNoneEl = uifactory.addStaticTextElement("autocompletion.selection.none", "autocompletion.selection",
				translate("autocompletion.selection.none"), formLayout);
		selectionNoneEl.setVisible(selectionEl.getSelectedKeys().isEmpty());
		
		resultsNoneEl = uifactory.addStaticTextElement("autocompletion.results.none", "autocompletion.results",
				translate("autocompletion.results.none"), formLayout);
		resultsNoneEl.setVisible(false);
		
		resultsEl = uifactory.addCheckboxesVertical("autocompletion.results", formLayout, emptyStrings(), emptyStrings(), 1);
		resultsEl.setEscapeHtml(false);
		resultsEl.setVisible(false);
		
		resultsMoreEl = uifactory.addStaticTextElement("autocompletion.results.more", null, "", formLayout);
		resultsMoreEl.setVisible(false);
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
		
		Command focusCommand = FormJSHelper.getFormFocusCommand(flc.getRootForm().getFormName(), quickSearchEl.getFormDispatchId());
		getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(clearButton == source) {
			doClear(ureq);
		} else if(updateButton == source) {
			doUpdate(ureq);
		} else if(quickSearchEl == source) {
			doQuickSearch();
		} else if(resetQuickSearchButton == source) {
			doResetQuickSearch();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if(source == clearButton || source == selectionEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doUpdate(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doUpdate(UserRequest ureq) {
		SelectionValues selectionKV = new SelectionValues();
		
		for (String key : selectionEl.getSelectedKeys()) {
			selectionKV.add(SelectionValues.entry(key, selectionEl.getValue(key)));
		}
		
		if (resultsEl.isVisible()) {
			for (String key : resultsEl.getSelectedKeys()) {
				if (!selectionKV.containsKey(key)) {
					selectionKV.add(SelectionValues.entry(key, resultsEl.getValue(key)));
				}
			}
		}
		fireEvent(ureq, new AutoCompletionSelectionEvent(selectionKV));
	}
	
	private void doClear(UserRequest ureq) {
		fireEvent(ureq, new AutoCompletionSelectionEvent(null));
	}
	
	private void doQuickSearch() {
		resultsNoneEl.setVisible(false);
		resultsMoreEl.setVisible(false);
		
		String searchText = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);
		
		if (StringHelper.containsNonWhitespace(searchText)) {
			SearchResult searchResult = source.getSearchResult(searchText);
			if (searchResult.getCountCurrent() > 0) {
				SelectionValues results = searchResult.getSelectionValues();
				resultsEl.setKeysAndValues(results.keys(), results.values(), null, results.icons());
				resultsEl.setLabel("autocompletion.results", new String[] {searchText});
				resultsEl.setVisible(true);
				if (searchResult.getCountTotal() > searchResult.getCountCurrent()) {
					int numOfMoreResults = searchResult.getCountTotal() - searchResult.getCountCurrent();
					String moreResults = translate("autocompletion.results.more", new String[] {String.valueOf(numOfMoreResults)});
					resultsMoreEl.setValue(String.valueOf(moreResults));
					resultsMoreEl.setVisible(true);
				}
			} else {
				resultsNoneEl.setLabel("autocompletion.results", new String[] {searchText});
				resultsNoneEl.setVisible(true);
				resultsEl.setKeysAndValues(emptyStrings(), emptyStrings(), null, emptyStrings());
				resultsEl.setVisible(false);
			}
		} else {
			resultsEl.setKeysAndValues(emptyStrings(), emptyStrings(), null, emptyStrings());
			resultsEl.setVisible(false);
		}
		
		resultsNoneEl.getComponent().setDirty(true);
		resultsEl.getComponent().setDirty(true);
		resultsMoreEl.getComponent().setDirty(true);
	}
	
	private void doResetQuickSearch() {
		quickSearchEl.setValue("");
		resultsNoneEl.setVisible(false);
		resultsEl.setVisible(false);
		resultsMoreEl.setVisible(false);
	}
	
	public static class AutoCompletionSelectionEvent extends Event {
		
		private static final long serialVersionUID = 3584489308662154946L;
		
		private final SelectionValues selection;

		public AutoCompletionSelectionEvent(SelectionValues selection) {
			super("autocompletionselection");
			this.selection = selection;
		}

		public SelectionValues getSelection() {
			return selection;
		}

	}
}
