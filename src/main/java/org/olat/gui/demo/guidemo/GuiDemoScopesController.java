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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.DateScope;
import org.olat.core.gui.components.scope.DateScopeEvent;
import org.olat.core.gui.components.scope.DateScopeSelection;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 24 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GuiDemoScopesController extends BasicController {

	private final Formatter formatter;
	private final ScopeSelection simpleScope;
	private final DateScopeSelection dateScope;

	public GuiDemoScopesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		formatter = Formatter.getInstance(getLocale());
		
		VelocityContainer mainVC = createVelocityContainer("guidemo-scopes");
		putInitialPanel(mainVC);
		
		// Simple scopes
		List<Scope> simpleScopes = new ArrayList<>(8);
		for (int i = 1; i < 9; i++) {
			String identifier = "select." + i;
			simpleScopes.add(ScopeFactory.createScope(identifier, translate(identifier), null));
		}
//		simpleScopes.add(ScopeFactory.createScope("d", "Das ist ein langer Text auf mehreren Zeilen, der muss noch länger sein", null));
		simpleScope = ScopeFactory.createScopeSelection("scope.simple", mainVC, this, simpleScopes);
		simpleScope.setHintsEnabled(false);
		
		// Scopes with hints
		List<Scope> hintScopes = new ArrayList<>(8);
		for (int i = 1; i < 9; i++) {
			String identifier = "select." + i;
			hintScopes.add(ScopeFactory.createScope(identifier, translate(identifier), translate("scope.hints.hint", String.valueOf(i))));
		}
//		hintScopes.add(ScopeFactory.createScope("d", "Das ist ein langer Text auf mehreren Zeilen, der muss noch länger sein", "hint"));
		ScopeFactory.createScopeSelection("scope.hints", mainVC, this, hintScopes);
		
		// Date scopes
		List<DateScope> dateScopes = ScopeFactory.dateScopesBuilder(getLocale())
				.nextMonths(1)
				.nextMonths(3)
				.build();
		dateScope = ScopeFactory.createDateScopeSelection(wControl, "scope.dates", mainVC, this, dateScopes, getLocale());
		dateScope.setCustomScopeLimit(new DateRange(
				DateUtils.getStartOfDay(new Date()),
				DateUtils.getEndOfDay(DateUtils.addYears(new Date(), 1))));
		dateScope.setAdditionalDateScopes(ScopeFactory.dateScopesBuilder(getLocale())
				.nextWeeks(1)
				.nextWeeks(2)
				.nextMonths(6)
				.toEndOfMonth()
				.nextMonths(12)
				.toEndOfYear()
				.build());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == simpleScope) {
			if (event instanceof ScopeEvent scopeEvent) {
				showInfo("scope.simple.selected", new String[] {
						scopeEvent.getDeselectedKey() != null? translate(scopeEvent.getDeselectedKey()): "-",
						scopeEvent.getSelectedKey() != null? translate(scopeEvent.getSelectedKey()): "-"
					});
			}
		} else if (source == dateScope) {
			if (event instanceof DateScopeEvent dateScopeEvent) {
				if (dateScopeEvent.getDateRange() == null) {
					showInfo("scope.date.selected.nothing");
				} else {
					showInfo("scope.date.selected", new String[] {
							formatter.formatDate(dateScopeEvent.getDateRange().getFrom()),
							formatter.formatDate(dateScopeEvent.getDateRange().getTo())
					});
				}
			}
		}
	}

}
