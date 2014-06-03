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

import java.util.List;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.Stats;
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.logging.OLog;
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
	
	private static final OLog log = Tracing.createLoggerFor(AllCachesController.class);
	
	private VelocityContainer myContent;
	private TableController tableCtr;
	private TableDataModel<String> tdm;
	private EmbeddedCacheManager cm;
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
		
		// eh cache
		String[] cnames;
		try {
			CoordinatorManager coordinator = CoreSpringFactory.getImpl(CoordinatorManager.class);
			Cacher cacher = coordinator.getCoordinator().getCacher();
			
			cm = cacher.getCacheContainer();
			Set<String> cacheNameSet = cm.getCacheNames();	
			cnames = cacheNameSet.toArray(new String[cacheNameSet.size()]);		
		} catch (Exception e) {
			log.error("", e);
			cnames = new String[0];
		}
		
		tdm = new AllCachesTableDataModel(cnames, cm);
		tableCtr.setTableDataModel(tdm);
		myContent.put("cachetable", tableCtr.getInitialComponent());
				
		//returned panel is not needed here, because this controller only shows content with the index.html velocity page
		putInitialPanel(myContent);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// 
	}
	
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals("empty")) {
					Object cname = tableCtr.getTableDataModel().getObject(te.getRowId());
					dc = activateYesNoDialog(ureq, null, translate("confirm.emptycache"), dc);
					dc.setUserObject(cname);
				}
			}
		} else if (source == dc) {
			//the dialogbox is already removed from the gui stack - do not use getWindowControl().pop(); to remove dialogbox
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok
				String cacheName = null;
				try {
					// delete cache
					cacheName = (String)dc.getUserObject();
					cm.getCache(cacheName).clear();
				} catch (IllegalStateException e) {
					// ignore
					log.error("Cannot remove Cache:"+cacheName, e);
				}
				
			}//else no was clicked or dialog box was cancelled (close icon clicked)
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

	private static class AllCachesTableDataModel implements TableDataModel<String> {
	  private String[] cnames;
		private EmbeddedCacheManager cacheManager;
	  
	  protected AllCachesTableDataModel(String[] cnames, EmbeddedCacheManager cacheManager) {
	  	this.cnames = cnames;
	  	this.cacheManager = cacheManager;
	  }
		
		@Override
		public String getObject(int row) {
			return cnames[row];
		}
	
		@Override
		public void setObjects(List<String> objects) {
			cnames = objects.toArray(cnames);
		}
	
		@Override
		public AllCachesTableDataModel createCopyWithEmptyList() {
			return new AllCachesTableDataModel(new String[0], cacheManager);
		}
	
		public int getColumnCount() {
			return 9;
		}
	
		public int getRowCount() {
			return cnames.length;
		}
	
		public Object getValueAt(int row, int col) {
			String cname = cnames[row];
			Cache<?,?> c = cacheManager.getCache(cname);
			Stats stats = c.getAdvancedCache().getStats();
			switch(col) {
				case 0: return cname;
				case 1: return c.getCacheConfiguration().storeAsBinary().enabled() ? Boolean.TRUE:Boolean.FALSE;
				case 2: return new Long(stats.getHits());
				case 3: return new Long(stats.getMisses());
				case 5: return new Long(stats.getTotalNumberOfEntries());
				case 6: return new Long(c.getCacheConfiguration().expiration().maxIdle());
				case 7: return new Long(c.getCacheConfiguration().expiration().lifespan());
				case 8: return new Integer(c.getCacheConfiguration().eviction().maxEntries());
				case 9: return c.getCacheConfiguration().clustering().cacheModeString();
				default: return "";
			}
		}
	}
}