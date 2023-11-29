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
package org.olat.core.gui.components.scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.FormToggleComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateRange;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 27 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DateScopeSelection extends ScopeSelection implements ControllerEventListener {
	
	final static String ADDITIONAL_IDENTIFIER = "date.scope.additional";
	
	private final WindowControl wControl;
	private final Translator dateScopeTranslator;
	private CloseableCalloutWindowController calloutCtrl;
	private CustomDateScopeController customScopeCtrl;
	
	private DateRange customScopeLimit;
	private List<DateScope> additionalDateScopes;
	private DateScope customDateScope;

	DateScopeSelection(WindowControl wControl, String name, Locale locale) {
		super(name);
		this.wControl = wControl;
		this.dateScopeTranslator = Util.createPackageTranslator(DateScopeSelection.class, locale);
	}
	
	void setDateScopes(List<DateScope> dateScopes) {
		List<Scope> scopes = new ArrayList<>(dateScopes.size() + 1);
		dateScopes.forEach(dateScope -> scopes.add(dateScope));
		customDateScope = ScopeFactory.createDateScope(ADDITIONAL_IDENTIFIER,
				dateScopeTranslator.translate("date.scope.additional.title"),
				dateScopeTranslator.translate("date.scope.additional.hint"),
				null);
		scopes.add(customDateScope);
		super.setScopes(scopes);
	}
	
	/**
	 * Initialize the scopes of the additional date scope selection panel.
	 * 
	 * @param additionalDateScopes
	 */
	public void setAdditionalDateScopes(List<DateScope> additionalDateScopes) {
		this.additionalDateScopes = additionalDateScopes;
	}
	
	/**
	 *  Limits the user to set the custom date range in the editable date fields.
	 *
	 * @param range
	 */
	public void setCustomScopeLimit(DateRange customScopeLimit) {
		this.customScopeLimit = customScopeLimit;
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (customScopeCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doSetCustomScope(ureq, customScopeCtrl.getDateRange());
			}
			if (event == CustomDateScopeController.RESET_EVENT) {
				doResetCustomScope(ureq);
			}
			calloutCtrl.deactivate();
			cleanUp();
		} else if (calloutCtrl == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		calloutCtrl = cleanUp(calloutCtrl);
		customScopeCtrl = cleanUp(customScopeCtrl);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}

	@Override
	protected void doToggleItem(UserRequest ureq, ScopeItem scopeItem) {
		if (ADDITIONAL_IDENTIFIER.equals(scopeItem.getKey())) {
			doOpenCustomScope(ureq, scopeItem.getToggle());
		} else {
			DateRange dateRange = null;
			String deselectedKey = doSetSelectedKey(scopeItem);
			toggleOff(deselectedKey);
			if (scopeItem.getToggle().isOn()) {
				dateRange = ((DateScope)scopeItem.getScope()).getDateRange();
			}
			fireEvent(ureq, new DateScopeEvent(deselectedKey, selectedKey, dateRange));
		}
	}
	
	private void doOpenCustomScope(UserRequest ureq, FormToggleComponent formToggle) {
		formToggle.setOn(ADDITIONAL_IDENTIFIER.equals(selectedKey));
		
		DateRange dateRange = customDateScope != null? customDateScope.getDateRange(): null;
		customScopeCtrl = new CustomDateScopeController(ureq, wControl, additionalDateScopes, dateRange, customScopeLimit);
		customScopeCtrl.addControllerListener(this);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, customScopeCtrl.getInitialComponent(),
				formToggle.getFormDispatchId(), "", true, "",
				new CalloutSettings(false, CalloutOrientation.bottom, true, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}
	
	private void doSetCustomScope(UserRequest ureq, DateRange dateRange) {
		String deselectedKey = selectedKey;
		selectedKey = ADDITIONAL_IDENTIFIER;
		
		List<ScopeItem> scopeItems = new ArrayList<>(getScopeItems());
		for (int i = 0; i < scopeItems.size(); i++) {
			ScopeItem scopeItem = scopeItems.get(i);
			if (scopeItem.getToggle().isOn()) {
				deselectedKey = scopeItem.getKey();
				scopeItem.getToggle().toggleOff();
			}
		}
		
		customDateScope = ScopeFactory.createDateScope(ADDITIONAL_IDENTIFIER,
				dateScopeTranslator.translate("date.scope.additional.title"),
				ScopeFactory.formatDateRange(dateScopeTranslator, Formatter.getInstance(dateScopeTranslator.getLocale()), dateRange),
				dateRange);
		ScopeItem scopeItem = createScopeItem(customDateScope);
		scopeItem.getToggle().toggleOn();
		
		scopeItems.set(scopeItems.size() - 1, scopeItem);
		setScopeItems(scopeItems);
		setDirty(true);
		
		fireEvent(ureq, new DateScopeEvent(deselectedKey, ADDITIONAL_IDENTIFIER, dateRange));
	}
	
	private void doResetCustomScope(UserRequest ureq) {
		if (ADDITIONAL_IDENTIFIER.equals(selectedKey)) {
			String deselectedKey = selectedKey;
			selectedKey = null;
			customDateScope = ScopeFactory.createDateScope(ADDITIONAL_IDENTIFIER,
					dateScopeTranslator.translate("date.scope.additional.title"),
					dateScopeTranslator.translate("date.scope.additional.hint"),
					null);
			ScopeItem scopeItem = createScopeItem(customDateScope);
			List<ScopeItem> scopeItems = new ArrayList<>(getScopeItems());
			scopeItems.set(scopeItems.size() - 1, scopeItem);
			setScopeItems(scopeItems);
			setDirty(true);
			
			fireEvent(ureq, new DateScopeEvent(deselectedKey, selectedKey, null));
		}
		
		// else nothing to reset. Maybe even another scope selected at the end.
	}

}
