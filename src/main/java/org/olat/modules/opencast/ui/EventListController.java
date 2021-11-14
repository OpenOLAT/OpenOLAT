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
package org.olat.modules.opencast.ui;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.opencast.AuthDelegate;
import org.olat.modules.opencast.OpencastEvent;
import org.olat.modules.opencast.OpencastService;
import org.olat.modules.opencast.ui.EventDataModel.EventCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EventListController extends FormBasicController {
	
	private static final String CMD_SELECT = "select";
	
	private FlexiTableElement tableEl;
	private EventDataModel dataModel;
	
	private final List<OpencastEvent> events;
	
	@Autowired
	private OpencastService opencastService;

	public EventListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		AuthDelegate authDelegate = opencastService.getAuthDelegate(getIdentity());
		events = opencastService.getEvents(authDelegate);
		
		initForm(ureq);
		loadModel(null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EventCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EventCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EventCols.presenters));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EventCols.series));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CMD_SELECT, translate(EventCols.select.i18nHeaderKey()), CMD_SELECT));
		
		dataModel = new EventDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "opencast-event");
		tableEl.setSearchEnabled(true);
	}

	private void loadModel(String search) {
		List<OpencastEvent> filteredEvents = StringHelper.containsNonWhitespace(search)
				? events.stream().filter(searchFilter(search)).collect(Collectors.toList())
				: events;
		dataModel.setObjects(filteredEvents);
		tableEl.reset(false, false, true);
	}

	private Predicate<? super OpencastEvent> searchFilter(String search) {
		return event -> {
			return contains(event.getTitle(), search)
					|| contains(event.getDescription(), search)
					|| contains(event.getSeries(), search)
					|| contains(event.getIdentifier(), search)
					|| contains(event.getPresenters(), search)
					;
			};
	}

	private boolean contains(String value, String search) {
		return StringHelper.containsNonWhitespace(value) && value.contains(search);
	}

	private boolean contains(List<String> presenters, String search) {
		for (String presenter : presenters) {
			if (contains(presenter, search)) {
				return true;
			}
		}
		return false;
	}
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if(CMD_SELECT.equals(se.getCommand())) {
					OpencastEvent opencastEvent = dataModel.getObject(se.getIndex());
					fireEvent(ureq, new OpencastEventSelectionEvent(opencastEvent));
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				String search = ((FlexiTableSearchEvent)event).getSearch();
				loadModel(search);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static final class OpencastEventSelectionEvent extends Event {

		private static final long serialVersionUID = 6885558823218325842L;
		
		private final OpencastEvent event;
		
		public OpencastEventSelectionEvent(OpencastEvent event) {
			super("opencast-selection-event");
			this.event = event;
		}

		public OpencastEvent getEvent() {
			return event;
		}

	}

}
