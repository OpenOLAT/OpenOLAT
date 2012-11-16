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

package org.olat.admin.sysinfo;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.servlets.WebDAVManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;


/**
*  Description:<br>
*  all you wanted to know about your running OLAT system
*
* @author Felix Jost
*/
public class SysinfoController extends BasicController implements Activateable2 {

	private static final String ACTION_INFOMSG = "infomsg";
	private static final String ACTION_SYSINFO = "sysinfo";

	private VelocityContainer mySysinfo;

	private TabbedPane tabbedPane;
	private Link gcButton;
	private Controller clusterController;
	private Controller infoMsgCtrl;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public SysinfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		BaseSecurity mgr = BaseSecurityManager.getInstance();
		if (!mgr.isIdentityPermittedOnResourceable(
				ureq.getIdentity(), 
				Constants.PERMISSION_ACCESS, 
				OresHelper.lookupType(this.getClass())))
			throw new OLATSecurityException("Insufficient permissions to access SysinfoController");

		
		mySysinfo = createVelocityContainer("sysinfo");
		gcButton = LinkFactory.createButton("run.gc", mySysinfo, this);
		// add system startup time
		SimpleDateFormat startupTimeFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", ureq.getLocale());
		mySysinfo.contextPut("startupTime", startupTimeFormatter.format(new Date(WebappHelper.getTimeOfServerStartup())));		
		
		

		
		//info message controller has two implementations (SingleVM or cluster)
		InfoMessageManager InfoMgr = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
		infoMsgCtrl = InfoMgr.getInfoMessageController(ureq, getWindowControl());
			
		tabbedPane = new TabbedPane("tp", ureq.getLocale());
		tabbedPane.addTab(ACTION_INFOMSG,infoMsgCtrl.getInitialComponent());
		//fxdiff: FXOLAT-79 check fxadmin-rights
		tabbedPane.addTab(ACTION_SYSINFO, mySysinfo);
		
		//fxdiff: no cluster anyway:
//		AutoCreator controllerCreator = (AutoCreator)CoreSpringFactory.getBean("clusterAdminControllerCreator");
//		clusterController = controllerCreator.createController(ureq, wControl);
//		tabbedPane.addTab("Cluster", clusterController.getInitialComponent());
		
		VelocityContainer myBuildinfo = createVelocityContainer("buildinfo");
		fillBuildInfoTab(myBuildinfo);		
		tabbedPane.addTab("buildinfo", myBuildinfo);

		tabbedPane.addListener(this);
		putInitialPanel(tabbedPane);
	}

	private void fillBuildInfoTab(VelocityContainer myBuildinfo) {
		List<Map> properties = new LinkedList<Map>();
		Map<String, String> m = new HashMap<String, String>();
		m.put("key", "Version");
		m.put("value", Settings.getFullVersionInfo());
		properties.add(m);
		
		m = new HashMap<String, String>();
		m.put("key", "HG changeset on build");
		m.put("value", Settings.getRepoRevision());
		properties.add(m);
		
		m = new HashMap<String, String>();
		m.put("key", "isClusterMode");
		m.put("value", Settings.getClusterMode().equals("Cluster") ? "true"  : "false" );
		properties.add(m);
		
		m = new HashMap<String, String>();
		m.put("key", "nodeId");
		m.put("value", Settings.getNodeInfo().equals("") ? "N1" : Settings.getNodeInfo());
		properties.add(m);
		
		m = new HashMap<String, String>();
		m.put("key", "serverStartTime");
		final Date timeOfServerStartup = new Date(WebappHelper.getTimeOfServerStartup());
		m.put("value", String.valueOf(timeOfServerStartup));
		properties.add(m);
		
		m = new HashMap<String, String>();
		m.put("key", "Build date");
		m.put("value", String.valueOf(Settings.getBuildDate()));
		properties.add(m);
		
		File baseDir = new File(WebappHelper.getContextRoot(), "..");
		m = new HashMap<String, String>();
		try {
			m.put("key", "baseDir");
			m.put("value", baseDir.getCanonicalPath());
		} catch (IOException e1) {
			// then fall back to unresolved path
			m.put("key", "baseDir");
			m.put("value", baseDir.getAbsolutePath());
		}
		properties.add(m);
				
		m = new HashMap<String, String>();
		m.put("key", "jsMathEnabled");
		boolean jsMathEnabled = BaseChiefController.isJsMathEnabled();
		m.put("value", Boolean.toString(jsMathEnabled));
		properties.add(m);
		
		m = new HashMap<String, String>();
		m.put("key", "WebDAVEnabled");
		boolean webDavEnabled = WebDAVManager.getInstance().isEnabled();
		m.put("value", Boolean.toString(webDavEnabled));
		properties.add(m);
		
		myBuildinfo.contextPut("properties", properties);
	}



	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && entries.isEmpty()) return;
		tabbedPane.activate(ureq, entries, state);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == tabbedPane) { // those must be links
			TabbedPaneChangedEvent tbcEvent = (TabbedPaneChangedEvent)event;
			Component newComponent = tbcEvent.getNewComponent();
			//fxdiff BAKS-7 Resume function
			tabbedPane.addToHistory(ureq, getWindowControl());
			if (newComponent == infoMsgCtrl.getInitialComponent()) {
				
			}
			
			else if (newComponent == mySysinfo) {
				Runtime r = Runtime.getRuntime();
				StringBuilder sb = new StringBuilder();
				appendFormattedKeyValue(sb, "Processors", new Integer(r.availableProcessors()));
				appendFormattedKeyValue(sb, "Total Memory", StringHelper.formatMemory(r.totalMemory()));
				appendFormattedKeyValue(sb, "Free Memory", StringHelper.formatMemory(r.freeMemory()));
				appendFormattedKeyValue(sb, "Max Memory", StringHelper.formatMemory(r.maxMemory()));
				
				sb.append("<br />Detailed Memory Information (Init/Used/Max)<br/> ");
				Iterator<MemoryPoolMXBean> iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
				while (iter.hasNext()) {
				    MemoryPoolMXBean item = iter.next();
				    String name = item.getName();
				    MemoryType type = item.getType();
				    appendFormattedKeyValue(sb, name, " Type: " + type);
				    MemoryUsage usage = item.getUsage();
				    appendFormattedKeyValue(sb, "Usage", StringHelper.formatMemory(usage.getInit()) + "/" + StringHelper.formatMemory(usage.getUsed()) + "/" + StringHelper.formatMemory(usage.getMax()));
				    MemoryUsage peak = item.getPeakUsage();
				    appendFormattedKeyValue(sb, "Peak", StringHelper.formatMemory(peak.getInit()) + "/" + StringHelper.formatMemory(peak.getUsed()) + "/" + StringHelper.formatMemory(peak.getMax()));
				    MemoryUsage collections = item.getCollectionUsage();
				    if (collections!= null){
				    	appendFormattedKeyValue(sb, "Collections", StringHelper.formatMemory(collections.getInit()) + "/" + StringHelper.formatMemory(collections.getUsed()) + "/" + StringHelper.formatMemory(collections.getMax()));
				    }
				    sb.append("<hr/>");
				}
				
				int controllerCnt = DefaultController.getControllerCount();
				sb.append("<br />Controller Count (active and not disposed):"+controllerCnt);
				sb.append("<br />Concurrent Dispatching Threads: "+DispatcherAction.getConcurrentCounter());
				mySysinfo.contextPut("memory", sb.toString());
				mySysinfo.contextPut("threads",getThreadsInfo());
				mySysinfo.contextPut("javaenv", getJavaenv());
				
			}
		} 
		else if (source == gcButton){
			Runtime.getRuntime().gc();
			getWindowControl().setInfo("Garbage collection done.");
			event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, mySysinfo));
		}
		
		
	}

	private String getJavaenv() {
		Properties p = System.getProperties();
		Iterator it = p.keySet().iterator();
		StringBuilder props = new StringBuilder();
		int lineCut = 100;
		while (it.hasNext()) {
			String key = (String) it.next();
			props.append("<b>" + key + "</b>&nbsp;=&nbsp;");
			String value = p.getProperty(key);
			if (value.length() <= lineCut)
				props.append(value);
			else {
				props.append(value.substring(0, lineCut - key.length()));
				while (value.length() > lineCut) {
					value = "<br />" + value.substring(lineCut);
					props.append(value.substring(0,	value.length() > lineCut ? lineCut : value.length()));
				}
			}
			props.append("<br />");
		}
		return props.toString();
	}


	
	private void appendFormattedKeyValue(StringBuilder sb, String key, Object value) {
		sb.append("&nbsp;&nbsp;&nbsp;<b>");
		sb.append(key);
		sb.append(":</b>&nbsp;");
		sb.append(value);
		sb.append("<br />");
	}
	
	private String getThreadsInfo() {
		StringBuilder sb = new StringBuilder("<pre>threads:<br />");
		try { // to be sure
			ThreadGroup tg = Thread.currentThread().getThreadGroup();
			int actCnt = tg.activeCount();
			int grpCnt = tg.activeGroupCount();
			sb.append("about "+actCnt +" threads, "+grpCnt+" groups<br /><br />");
			Thread[] threads = new Thread[actCnt];
			tg.enumerate(threads, true);
			for (int i = 0; i < actCnt; i++) {
				Thread tr = threads[i];
				if (tr != null) { // thread may have finished in the meantime 
					String name = tr.getName();
					boolean alive = tr.isAlive();
					boolean interrupted = tr.isInterrupted();
					sb.append("Thread: (alive = "+alive+", interrupted: "+interrupted+", group:"+tr.getThreadGroup().getName()+") "+name+"<br />");
				}
			}
		}
		catch (Exception e) {
			sb.append("exception occured:"+e.getMessage());
		}
		return sb.toString()+"</pre>";
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (clusterController != null) {
			clusterController.dispose();
		}
	}
}
