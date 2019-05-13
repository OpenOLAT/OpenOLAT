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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.Stats;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.logging.Tracing;
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
public class AllCachesController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(AllCachesController.class);
	
	private VelocityContainer myContent;
	private TableController tableCtr;
	private TableDataModel<CacheInfos> tdm;
	private DialogBoxController dc;
	
	/**
	 * @param ureq
	 * @param wControl
	 * 
	 */
	public AllCachesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		//create page
		myContent = createVelocityContainer("index");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setResultsPerPage(200);
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.name", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.disk", 1, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.hitcnt", 2, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.mcexp", 3, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.quickcount", 5, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.tti", 6, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.ttl", 7, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.maxElements", 8, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cache.clustered", 9, null, getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("empty", "cache.empty", translate("action.choose")));
		listenTo(tableCtr);
		myContent.contextPut("title", translate("caches.title"));
		myContent.put("cachetable", tableCtr.getInitialComponent());
		loadModel();
				
		//returned panel is not needed here, because this controller only shows content with the index.html velocity page
		putInitialPanel(myContent);
	}
	

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// 
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals("empty")) {
					Object cacheInfos = tableCtr.getTableDataModel().getObject(te.getRowId());
					dc = activateYesNoDialog(ureq, null, translate("confirm.emptycache"), dc);
					dc.setUserObject(cacheInfos);
				}
			}
		} else if (source == dc) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				String cacheName = null;
				try {
					CacheInfos cacheInfos = (CacheInfos)dc.getUserObject();
					cacheInfos.clear();
					loadModel();
				} catch (IllegalStateException e) {
					log.error("Cannot remove Cache:"+cacheName, e);
				}	
			}
		}
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
			log.error("", e);
		}
		
		try {
			loadModel(infos, names, ((DBImpl)DBFactory.getInstance()).getCacheContainer());
		} catch (Exception e) {
			log.error("", e);
		}

		tdm = new AllCachesTableDataModel(infos);
		tableCtr.setTableDataModel(tdm);
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
	
	private static class CacheInfos {
		private final String cname;
		private final boolean binary;
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
			Stats stats = cache.getAdvancedCache().getStats();
			
			this.cname = cname;
			
			binary = cache.getCacheConfiguration().storeAsBinary().enabled();
			hits = stats.getHits();
			misses = stats.getMisses();
			size = cache.getAdvancedCache().size();
			maxIdle = cache.getCacheConfiguration().expiration().maxIdle();
			lifespan = cache.getCacheConfiguration().expiration().lifespan();
			maxEntries = cache.getCacheConfiguration().eviction().maxEntries();
			cacheMode = cache.getCacheConfiguration().clustering().cacheModeString();
		}
		
		public String getCname() {
			return cname;
		}
		
		public boolean isBinary() {
			return binary;
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

	private static class AllCachesTableDataModel implements TableDataModel<CacheInfos> {
		private List<CacheInfos> cacheInfos;
	  
		protected AllCachesTableDataModel(List<CacheInfos> cacheInfos) {
			this.cacheInfos = cacheInfos;
		}
		
		@Override
		public CacheInfos getObject(int row) {
			return cacheInfos.get(row);
		}
	
		@Override
		public void setObjects(List<CacheInfos> objects) {
			this.cacheInfos = objects;
		}
	
		@Override
		public AllCachesTableDataModel createCopyWithEmptyList() {
			return new AllCachesTableDataModel(Collections.<CacheInfos>emptyList());
		}
	
		public int getColumnCount() {
			return 9;
		}
	
		public int getRowCount() {
			return cacheInfos.size();
		}
	
		public Object getValueAt(int row, int col) {
			CacheInfos c = getObject(row);
			switch(col) {
				case 0: return c.getCname();
				case 1: return c.isBinary();
				case 2: return c.getHits();
				case 3: return c.getMisses();
				case 5: return c.getSize();
				case 6: return c.getMaxIdle();
				case 7: return c.getLifespan();
				case 8: return c.getMaxEntries();
				case 9: return c.getCacheMode();
				default: return "";
			}
		}
	}
}