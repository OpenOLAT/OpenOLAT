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
import org.olat.modules.opencast.OpencastSeries;
import org.olat.modules.opencast.OpencastService;
import org.olat.modules.opencast.ui.SeriesDataModel.SeriesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SeriesListController extends FormBasicController {
	
	private static final String CMD_SELECT = "select";
	
	private FlexiTableElement tableEl;
	private SeriesDataModel dataModel;
	
	private final List<OpencastSeries> series;
	
	@Autowired
	private OpencastService opencastService;

	public SeriesListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		AuthDelegate authDelegate = opencastService.getAuthDelegate(getIdentity());
		series = opencastService.getSeries(authDelegate);
		
		initForm(ureq);
		loadModel(null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SeriesCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SeriesCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SeriesCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SeriesCols.contributors));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SeriesCols.subjects));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CMD_SELECT, translate(SeriesCols.select.i18nHeaderKey()), CMD_SELECT));
		
		dataModel = new SeriesDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "opencast-series");
		tableEl.setSearchEnabled(true);
	}

	private void loadModel(String search) {
		List<OpencastSeries> filteredSeries = StringHelper.containsNonWhitespace(search)
				? series.stream().filter(searchFilter(search)).collect(Collectors.toList())
				: series;
		dataModel.setObjects(filteredSeries);
		tableEl.reset(false, false, true);
	}

	private Predicate<? super OpencastSeries> searchFilter(String search) {
		return event -> {
			return contains(event.getTitle(), search)
					|| contains(event.getDescription(), search)
					|| contains(event.getContributors(), search)
					|| contains(event.getIdentifier(), search)
					|| contains(event.getSubjects(), search)
					;
			};
	}

	private boolean contains(String value, String search) {
		return StringHelper.containsNonWhitespace(value) && value.contains(search);
	}

	private boolean contains(List<String> values, String search) {
		for (String value : values) {
			if (contains(value, search)) {
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
					OpencastSeries opencastSeries = dataModel.getObject(se.getIndex());
					fireEvent(ureq, new OpencastSeriesSelectionEvent(opencastSeries));
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
	
	public static final class OpencastSeriesSelectionEvent extends Event {
		
		private static final long serialVersionUID = -4946550299535902114L;
		
		private final OpencastSeries series;
		
		public OpencastSeriesSelectionEvent(OpencastSeries series) {
			super("opencast-selection-series");
			this.series = series;
		}

		public OpencastSeries getSeries() {
			return series;
		}

	}

}
