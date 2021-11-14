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
package org.olat.admin.sysinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HibernateQueriesController extends FormBasicController {
	
	private FlexiTableElement table;
	private QueryStatisticsDataModel tableModel;
	
	@Autowired
	private DB dbInstance;

	public HibernateQueriesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QueryCols.average.i18nKey(), QueryCols.average.ordinal(),
				true, QueryCols.average.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QueryCols.max.i18nKey(), QueryCols.max.ordinal(),
				true, QueryCols.max.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QueryCols.min.i18nKey(), QueryCols.min.ordinal(),
				true, QueryCols.min.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, QueryCols.row.i18nKey(), QueryCols.row.ordinal(),
				true, QueryCols.row.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QueryCols.count.i18nKey(), QueryCols.count.ordinal(),
				true, QueryCols.count.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, QueryCols.cacheHits.i18nKey(), QueryCols.cacheHits.ordinal(),
				true, QueryCols.cacheHits.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, QueryCols.cacheMisses.i18nKey(), QueryCols.cacheMisses.ordinal(),
				true, QueryCols.cacheMisses.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, QueryCols.cachePuts.i18nKey(), QueryCols.cachePuts.ordinal(),
				true, QueryCols.cachePuts.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(QueryCols.query.i18nKey(), QueryCols.query.ordinal(),
				true, QueryCols.query.name()));
		
		tableModel = new QueryStatisticsDataModel(columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "queries", tableModel, 50, true, getTranslator(), formLayout);
		table.setAndLoadPersistedPreferences(ureq, "HibernateQueriesStatistics");
		table.setExportEnabled(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public void loadModel() {
		Statistics statistics = dbInstance.getStatistics();
		String[] queries = statistics.getQueries();
		List<QueryInfos> infos = new ArrayList<>(queries.length);
		for(String query:queries) {
			QueryStatistics queryStats = statistics.getQueryStatistics(query);
			infos.add(new QueryInfos(query, queryStats));
		}
		tableModel.setObjects(infos);
		table.reset();
	}
	
	private static class QueryInfos {
		
		private final String query;
		private final long averageTime;
		private final long maxTime;
		private final long minTime;
		private final long row;
		private final long count;
		private final long cacheHits;
		private final long cacheMisses;
		private final long cachePuts;
		
		public QueryInfos(String query, QueryStatistics statistics) {
			this.query = query;
			averageTime = statistics.getExecutionAvgTime();
			maxTime = statistics.getExecutionMaxTime();
			minTime = statistics.getExecutionMinTime();
			count = statistics.getExecutionCount();
			row =statistics.getExecutionRowCount();
			cacheHits = statistics.getCacheHitCount();
			cacheMisses = statistics.getCacheMissCount();
			cachePuts = statistics.getCachePutCount();
		}

		public String getQuery() {
			return query;
		}

		public long getAverageTime() {
			return averageTime;
		}

		public long getMaxTime() {
			return maxTime;
		}

		public long getMinTime() {
			return minTime;
		}

		public long getRow() {
			return row;
		}

		public long getCount() {
			return count;
		}

		public long getCacheHits() {
			return cacheHits;
		}

		public long getCacheMisses() {
			return cacheMisses;
		}

		public long getCachePuts() {
			return cachePuts;
		}
	}
	
	private static class QueryStatisticsDataModel extends DefaultFlexiTableDataModel<QueryInfos>
		implements SortableFlexiTableDataModel<QueryInfos> {
		
		public QueryStatisticsDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<QueryInfos> views = new QueryStatisticsModelSort(orderBy, this, null).sort();
				super.setObjects(views);
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			QueryInfos infos = getObject(row);
			return getValueAt(infos, col);
		}

		@Override
		public Object getValueAt(QueryInfos row, int col) {
			switch(QueryCols.values()[col]) {
				case average: return row.getAverageTime();
				case max: return row.getMaxTime();
				case min: return row.getMinTime();
				case query: return row.getQuery();
				case row: return row.getRow();
				case count: return row.getCount();
				case cacheHits: return row.getCacheHits();
				case cacheMisses: return row.getCacheMisses();
				case cachePuts: return row.getCachePuts();
				default: return null;
			}
		}

		@Override
		public DefaultFlexiTableDataModel<QueryInfos> createCopyWithEmptyList() {
			return new QueryStatisticsDataModel(getTableColumnModel());
		}
	}
	
	private static class QueryStatisticsModelSort extends SortableFlexiTableModelDelegate<QueryInfos> {
		
		public QueryStatisticsModelSort(SortKey orderBy, SortableFlexiTableDataModel<QueryInfos> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<QueryInfos> rows) {
			super.sort(rows);
		}
	}

	public enum QueryCols {
		average("hibernate.query.average"),
		max("hibernate.query.max"),
		min("hibernate.query.min"),
		row("hibernate.query.row"),
		count("hibernate.query.count"),
		cacheHits("hibernate.query.hits"),
		cacheMisses("hibernate.query.miss"),
		cachePuts("hibernate.query.puts"),
		query("hibernate.query.query");
		
		private final String i18nKey;
		
		private QueryCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
