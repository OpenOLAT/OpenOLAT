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
package org.olat.commons.coordinate.cluster.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.commons.coordinate.cluster.ClusterCoordinator;
import org.olat.commons.coordinate.cluster.lock.ClusterLockManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * provides a control panel for the olat system administrator.
 * displays the status of all running olat cluster nodes and also displays the latest sent messages.
 *  
 * 
 * <P>
 * Initial Date:  29.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterAdminControllerCluster extends BasicController {
	private static final OLATResourceable ORES_TEST = OresHelper.createOLATResourceableInstanceWithoutCheck(ClusterAdminControllerCluster.class.getName(), new Long(123));
	
	ClusterEventBus clusBus;

	private VelocityContainer mainVc;
	boolean disposed = false;

	private VelocityContainer nodeInfoVc;

	private VelocityContainer perfInfoVc;
	private Link toggleStartStop;
	private Link resetStats;

	private Link syncLong;
	private Link syncShort;
	private Link testPerf;

	private Link testCachePut;
	private Link testCachePut2;
	
	private Link testSFUPerf;
	
	private Link releaseAllLocksFor;

	private VelocityContainer cachetest;
	
	private UserSearchController usc;
	
	@Autowired
	private ClusterLockManager clusterLockManager;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public ClusterAdminControllerCluster(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		CoordinatorManager clustercoord = CoreSpringFactory.getImpl(CoordinatorManager.class);
		Coordinator coordinator = clustercoord.getCoordinator();
		if(!(coordinator instanceof ClusterCoordinator)) {
			putInitialPanel(new Panel("empty"));
			return;
		}

		ClusterCoordinator cCord = (ClusterCoordinator)coordinator; 
		clusBus = cCord.getClusterEventBus();
		mainVc = createVelocityContainer("cluster");

		// information about the cluster nodes
		mainVc.contextPut("own_nodeid", "This node is node: '"+clusBus.getClusterConfig().getNodeId()+"'");
		
		nodeInfoVc = createVelocityContainer("nodeinfos");
		Formatter f = Formatter.getInstance(ureq.getLocale());
		nodeInfoVc.contextPut("f", f);
		mainVc.put("nodeinfos", nodeInfoVc);
		updateNodeInfos();
		
		toggleStartStop = LinkFactory.createButtonSmall("toggleStartStop", mainVc, this);
		resetStats = LinkFactory.createButtonSmall("resetStats", mainVc, this);

		perfInfoVc = createVelocityContainer("performanceinfos");
		Formatter f2 = Formatter.getInstance(ureq.getLocale());
		perfInfoVc.contextPut("f", f2);
		mainVc.put("performanceinfos", perfInfoVc);
		updatePerfInfos();
		
		// test for the distributed cache
		cachetest = createVelocityContainer("cachetest");
		testCachePut = LinkFactory.createButtonSmall("testCachePut", cachetest, this);
		testCachePut2= LinkFactory.createButtonSmall("testCachePut2", cachetest, this);
		mainVc.put("cachetest", cachetest);
		updateCacheInfo();
		
		final VelocityContainer busMsgs = createVelocityContainer("busmsgs");
		busMsgs.contextPut("time", Formatter.formatDatetime(new Date()));
		
		mainVc.put("busmsgs", busMsgs);
		// let a thread repeatively dump all messages
		Thread pollThread = new Thread(new Runnable(){
			public void run() {
				while (!disposed) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
					
					// simple reput the new lists into the velocity container.
					// the container is then dirty and automatically rerendered since polling has been turned on here.
					busMsgs.contextPut("time", Formatter.formatDatetime(new Date()));
					busMsgs.contextPut("recmsgs", clusBus.getListOfReceivedMsgs());
					busMsgs.contextPut("sentmsgs", clusBus.getListOfSentMsgs());
					// also let node infos refresh
					updateNodeInfos();
					// also let perf infos refresh
					updatePerfInfos();
					// update cache info
					updateCacheInfo();
				}
			}});
		pollThread.setDaemon(true);
		pollThread.start();
		
		// activate polling
		mainVc.put("updatecontrol", new JSAndCSSComponent("intervall", this.getClass(), 3000));
		
		// add a few buttons
		syncLong = LinkFactory.createButtonSmall("sync.long", mainVc, this);
		syncShort = LinkFactory.createButtonSmall("sync.short", mainVc, this);
		testPerf  = LinkFactory.createButtonSmall("testPerf", mainVc, this);
		testSFUPerf = LinkFactory.createButtonSmall("testSFUPerf", mainVc, this);
		releaseAllLocksFor = LinkFactory.createButtonSmall("releaseAllLocksFor", mainVc, this);
		
		mainVc.contextPut("eventBusListener", clusBus.toString());
		mainVc.contextPut("busListenerInfos", clusBus.getBusInfosAsString());
		
		putInitialPanel(mainVc);
	}
	
	void updateNodeInfos() {
		Map<Integer, NodeInfo> stats = clusBus.getNodeInfos();
		List<NodeInfo> li = new ArrayList<>(stats.values());
		Collections.sort(li, new Comparator<NodeInfo>(){
			public int compare(NodeInfo o1, NodeInfo o2) {
				return o1.getNodeId().compareTo(o2.getNodeId());
			}});
		nodeInfoVc.contextPut("stats",li);
		nodeInfoVc.contextPut("thisNodeId", clusBus.getClusterConfig().getNodeId());
		mainVc.contextPut("eventBusListener", clusBus.toString());
		mainVc.contextPut("busListenerInfos", clusBus.getBusInfosAsString());
	}
	
	void updatePerfInfos() {
		// collect performance information
		
		
		boolean started = true;
		perfInfoVc.contextPut("perfs", null);
		if (started) {
			perfInfoVc.contextPut("started", "started");
		} else {
			perfInfoVc.contextPut("started", "notstarted");
		}
	}
	
	@Override
	protected void doDispose() {
		disposed  = true;
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == syncLong) {
			// sync on a olatresourceable and hold the lock for 5 seconds.
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ORES_TEST, new SyncerExecutor(){
				public void execute() {
					sleep(5000);
				}});
			// the runnable is executed within the same thread->
			getWindowControl().setInfo("done syncing on the test olatresourceable for 5 seconds");
		} else if (source == syncShort) {
			// sync on a olatresourceable and hold the lock for 1 second.
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ORES_TEST, new SyncerExecutor(){
				public void execute() {
					sleep(1000);
				}});
			// the runnable is executed within the same thread->
			getWindowControl().setInfo("done syncing on the test olatresourceable for 1 second");
		} else if (source == testPerf) {
			// send 1000 (short) messages over the cluster bus
			int cnt = 1000;
			long start = System.nanoTime();
			for (int i = 0; i < cnt; i++) {
				clusBus.fireEventToListenersOf(new MultiUserEvent("jms-perf-test-"+i+" of "+cnt),ORES_TEST);
			}
			long stop = System.nanoTime();
			long dur = stop-start;
			double inmilis = dur / 1000000;
			double avg = dur / cnt;
			double avgmilis = avg / 1000000;
			getWindowControl().setInfo("sending "+cnt+" messages took "+inmilis+" ms, avg per messages was "+avg+" ns = "+avgmilis+" ms");
		} else if (source == testCachePut) {
			CacheWrapper<String,String> cw = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache(this.getClass().getSimpleName(), "cachetest");
			// we explicitly use put and not putSilent to show that a put invalidates (and thus removes) this key of this cache in all other cluster nodes. 
			cw.update("akey", "hello");
			updateCacheInfo();
		} else if (source == testCachePut2) {
			// we explicitly use put and not putSilent to show that a put invalidates (and thus removes) this key of this cache in all other cluster nodes.
			CacheWrapper<String,String> cw = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache(this.getClass().getSimpleName(), "cachetest");
			cw.update("akey", "world");
			updateCacheInfo();
		} else if (source == testSFUPerf) {
			// acquire a sync 1000x times (does internally a select-for-update on the database)
			int cnt = 1000;
			long start = System.nanoTime();
			for (int i = 0; i < cnt; i++) {
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ORES_TEST, new SyncerExecutor(){
					@Override
					public void execute() {
						// empty
					}});
			}
			long stop = System.nanoTime();
			long dur = stop-start;
			double inmilis = dur / 1000000;
			double avg = dur / cnt;
			double avgmilis = avg / 1000000;
			getWindowControl().setInfo("acquiring "+cnt+" locks for syncing (using db's \"select for update\") took "+inmilis+" ms, avg per messages was "+avg+" ns = "+avgmilis+" ms");
		} else if (source == releaseAllLocksFor) {
			// let a user search pop up
			usc = new UserSearchController(ureq, getWindowControl(), true);
			listenTo(usc);
			getWindowControl().pushAsModalDialog(usc.getInitialComponent());
		} else if (source == toggleStartStop) {
			clusBus.resetStats();
			updatePerfInfos();
		} else if (source == resetStats) {
			clusBus.resetStats();
			updatePerfInfos();
		}
	}
  
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == usc) {
			getWindowControl().pop();
			if (event != Event.CANCELLED_EVENT) {
				// we configured usc to either cancel or to only accept single user selection.
				SingleIdentityChosenEvent sce = (SingleIdentityChosenEvent)event;
				Identity ident = sce.getChosenIdentity();
				clusterLockManager.releaseAllLocksFor(ident.getKey());
				showInfo("locks.released", ident.getKey().toString());
			}
		}
	}
	
	private void sleep (int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	private void updateCacheInfo() {
		CacheWrapper<String,String> cw = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache(this.getClass().getSimpleName(), "cachetest");
		Object val = cw.get("akey");
		cachetest.contextPut("cacheval", val==null? "-null-": val);
		// org.olat.commons.coordinate.cluster.jms.ClusterAdminController:cachetest::0@subcachetypetest::123
	}

}
