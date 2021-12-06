/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.Stats;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBImpl;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.CoordinatorManager;

/**
 * Description:<BR/>
 * 
 * 
 * <P/>
 * Initial Date:  Jul 13, 2005
 *
 * @author Felix Jost 
 */
public class AllCachesController extends FormBasicController {

	private DialogBoxController dc;
	
	private CachesDataModel tableModel;
	private FlexiTableElement tableEl;
	
	/**
	 * @param ureq
	 * @param wControl
	 * 
	 */
	public AllCachesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "index");
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.offHeap));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.hit));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.miss));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.size));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.maxIdle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.lifespan));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.maxEntries));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CacheCols.cacheMode));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("cache.empty", translate("action.choose"), "empty"));
		
		tableModel = new CachesDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 250, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "all-caches");
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(250);
	}
	
	private void loadModel() {
		Set<String> names = new HashSet<>();
		List<CacheInfos> infos = new ArrayList<>();
		
		//our cache first
		try {
			CoordinatorManager coordinator = CoreSpringFactory.getImpl(CoordinatorManager.class);
			Cacher cacher = coordinator.getCoordinator().getCacher();
			loadModel(infos, names, cacher.getCacheContainer());
		} catch (Exception e) {
			logError("", e);
		}
		
		try {
			loadModel(infos, names, ((DBImpl)DBFactory.getInstance()).getCacheContainer());
		} catch (Exception e) {
			logError("", e);
		}

		tableModel.setObjects(infos);
		tableEl.reset(true, true, true);
	}
	
	private void loadModel(List<CacheInfos> infos, Set<String> names, EmbeddedCacheManager cm) {
		Set<String> cacheNameSet = cm.getCacheNames();
		for(String cacheName:cacheNameSet) {
			if(names.contains(cacheName)) continue;
			
			Cache<?,?> cache = cm.getCache(cacheName);
			CacheInfos cacheInfos = new CacheInfos(cacheName, cache);
			infos.add(cacheInfos);
		}
		names.addAll(cacheNameSet);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dc) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doClearCache((CacheInfos)dc.getUserObject());
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent) event;
				if ("empty".equals(se.getCommand())) {
					CacheInfos cacheInfos = tableModel.getObject(se.getIndex());
					dc = activateYesNoDialog(ureq, null, translate("confirm.emptycache"), dc);
					dc.setUserObject(cacheInfos);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doClearCache(CacheInfos cacheInfos) {
		try {
			cacheInfos.clear();
			loadModel();
		} catch (IllegalStateException e) {
			logError("Cannot clear cache", e);
		}
	}
	
	private static class CacheInfos {
		private final String cname;
		private final boolean offHeap;
		private final long hits;
		private final long misses;
		private final long size;
		private final long maxIdle;
		private final long lifespan;
		private final long maxEntries;
		private final String cacheMode;
		
		private final Cache<?,?> cache;

		public CacheInfos(String cname, Cache<?,?> cache) {
			this.cache = cache;
			this.cname = cname;
			
			Stats stats = cache.getAdvancedCache().getStats();
			
			Configuration configuration = cache.getCacheConfiguration();
			
			offHeap = configuration.memory().storage() == StorageType.OFF_HEAP;
			hits = stats.getHits();
			misses = stats.getMisses();
			size = cache.getAdvancedCache().size();
			maxIdle = cache.getCacheConfiguration().expiration().maxIdle();
			lifespan = cache.getCacheConfiguration().expiration().lifespan();
			maxEntries = configuration.memory().maxCount();
			cacheMode = cache.getCacheConfiguration().clustering().cacheModeString();
		}
		
		public String getCname() {
			return cname;
		}
		
		public boolean isOffHeap() {
			return offHeap;
		}
		
		public long getHits() {
			return hits;
		}
		
		public long getMisses() {
			return misses;
		}
		
		public long getSize() {
			return size;
		}
		
		public long getMaxIdle() {
			return maxIdle;
		}
		
		public long getLifespan() {
			return lifespan;
		}
		
		public long getMaxEntries() {
			return maxEntries;
		}
		
		public String getCacheMode() {
			return cacheMode;
		}
		
		public void clear() {
			cache.clear();
		}
	}
	
	private static class CachesDataModel extends DefaultFlexiTableDataModel<CacheInfos>
	implements SortableFlexiTableDataModel<CacheInfos> {
		private static final CacheCols[] COLS = CacheCols.values();
		private final Locale locale;
		
		public CachesDataModel(FlexiTableColumnModel columnModel, Locale locale) {
			super(columnModel);
			this.locale = locale;
		}	

		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<CacheInfos> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
				super.setObjects(views);
			}
		}

		@Override
		public Object getValueAt(int row, int col) {
			CacheInfos infos = getObject(row);
			return getValueAt(infos, col);
		}

		@Override
		public Object getValueAt(CacheInfos c, int col) {
			switch(COLS[col]) {
				case name: return c.getCname();
				case offHeap: return c.isOffHeap();
				case hit: return c.getHits();
				case miss: return c.getMisses();
				case size: return c.getSize();
				case maxIdle: return c.getMaxIdle();
				case lifespan: return c.getLifespan();
				case maxEntries: return c.getMaxEntries();
				case cacheMode: return c.getCacheMode();
				default: return "ERROR";
			}
		}
	}

	public enum CacheCols implements FlexiSortableColumnDef {
		name("cache.name"),
		offHeap("cache.off.heap"),
		hit("cache.hitcnt"),
		miss("cache.mcexp"),
		size("cache.quickcount"),
		maxIdle("cache.tti"),
		lifespan("cache.ttl"),
		maxEntries("cache.maxElements"),
		cacheMode("cache.clustered");
		
		private final String i18nKey;
		
		private CacheCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}