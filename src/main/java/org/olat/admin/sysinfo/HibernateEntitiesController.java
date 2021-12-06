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

import org.hibernate.stat.EntityStatistics;
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
 * Initial date: 17.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HibernateEntitiesController extends FormBasicController {
	
	private FlexiTableElement table;
	private EntityStatisticsDataModel tableModel;
	
	@Autowired
	private DB dbInstance;

	public HibernateEntitiesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.entity.i18nKey(), EntityCols.entity.ordinal(),
				true, EntityCols.entity.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.load.i18nKey(), EntityCols.load.ordinal(),
				true, EntityCols.load.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.fetch.i18nKey(), EntityCols.fetch.ordinal(),
				true, EntityCols.fetch.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.insert.i18nKey(), EntityCols.insert.ordinal(),
				true, EntityCols.insert.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.update.i18nKey(), EntityCols.update.ordinal(),
				true, EntityCols.update.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.delete.i18nKey(), EntityCols.delete.ordinal(),
				true, EntityCols.delete.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntityCols.optimisticFailure.i18nKey(), EntityCols.optimisticFailure.ordinal(),
				true, EntityCols.optimisticFailure.name()));

		tableModel = new EntityStatisticsDataModel(columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "entities", tableModel, 50, true, getTranslator(), formLayout);
		table.setAndLoadPersistedPreferences(ureq, "HibernateEntityStatistics");
		table.setExportEnabled(true);
		table.setPageSize(250);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public void loadModel() {
		Statistics statistics = dbInstance.getStatistics();
		String[] entities = statistics.getEntityNames();
		List<QueryInfos> infos = new ArrayList<>(entities.length);
		for(String entity:entities) {
			EntityStatistics queryStats = statistics.getEntityStatistics(entity);
			infos.add(new QueryInfos(entity, queryStats));
		}
		tableModel.setObjects(infos);
		table.reset();
	}
	
	private static class QueryInfos {
		
		private final String entity;
		private final long load;
		private final long update;
		private final long delete;
		private final long fetch;
		private final long insert;
		private final long optimisticFailure;
		
		public QueryInfos(String entity, EntityStatistics statistics) {
			this.entity = entity;
			load = statistics.getLoadCount();
			fetch = statistics.getFetchCount();
			insert =statistics.getInsertCount();
			update = statistics.getUpdateCount();
			delete = statistics.getDeleteCount();
			optimisticFailure = statistics.getOptimisticFailureCount();
		}

		public String getEntity() {
			return entity;
		}

		public long getLoad() {
			return load;
		}

		public long getUpdate() {
			return update;
		}

		public long getDelete() {
			return delete;
		}

		public long getFetch() {
			return fetch;
		}

		public long getInsert() {
			return insert;
		}

		public long getOptimisticFailure() {
			return optimisticFailure;
		}
	}
	
	private static class EntityStatisticsDataModel extends DefaultFlexiTableDataModel<QueryInfos>
		implements SortableFlexiTableDataModel<QueryInfos> {
		
		public EntityStatisticsDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<QueryInfos> views = new EntityStatisticsModelSort(orderBy, this, null).sort();
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
			switch(EntityCols.values()[col]) {
				case load: return row.getLoad();
				case fetch: return row.getFetch();
				case insert: return row.getInsert();
				case update: return row.getUpdate();
				case delete: return row.getDelete();
				case optimisticFailure: return row.getOptimisticFailure();
				case entity: return row.getEntity();
				default: return null;
			}
		}
	}
	
	private static class EntityStatisticsModelSort extends SortableFlexiTableModelDelegate<QueryInfos> {
		
		public EntityStatisticsModelSort(SortKey orderBy, SortableFlexiTableDataModel<QueryInfos> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<QueryInfos> rows) {
			super.sort(rows);
		}
	}

	public enum EntityCols {
		load("hibernate.entity.load"),
		fetch("hibernate.entity.fetch"),
		insert("hibernate.entity.insert"),
		update("hibernate.entity.update"),
		delete("hibernate.entity.delete"),
		optimisticFailure("hibernate.entity.optimisticFailure"),
		entity("hibernate.entity.entity");
		
		private final String i18nKey;
		
		private EntityCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
