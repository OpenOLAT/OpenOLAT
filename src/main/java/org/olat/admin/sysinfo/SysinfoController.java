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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.olat.admin.cache.AllCachesController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.LogFileParser;
import org.olat.core.logging.LogRealTimeViewerController;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.Tracing;
import org.olat.core.servlets.WebDAVManager;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;


/**
*  Description:<br>
*  all you wanted to know about your running OLAT system
*
* @author Felix Jost
*/
public class SysinfoController extends BasicController implements Activateable2 {

	private static final String ACTION_SNOOP = "snoop";
	private static final String ACTION_ERRORS = "errors";
	private static final String ACTION_INFOMSG = "infomsg";
	private static final String ACTION_SETLEVEL = "setlevel";
	private static final String ACTION_LOGLEVELS = "loglevels";
	private static final String ACTION_VIEWLOG	 = "viewlog";
	private static final String ACTION_VIEWLOG_PACKAGE = "p";
	private static final String ACTION_SESSIONS = "sessions";
	private static final String ACTION_SYSINFO = "sysinfo";
	private static final String ACTION_HIBERNATEINFO = "hibernate";
	private static final String ACTION_LOCKS = "locks";

	private VelocityContainer mySessions, mySnoop, myErrors, myLoglevels, mySysinfo, myMultiUserEvents,myHibernateInfo;
	private Panel cachePanel;
	private LockController lockController;
	private TabbedPane tabbedPane;
	
	private RequestLoglevelController requestLoglevelController;

	private String err_nr;
	private String err_dd;
	private String err_mm;
	private String err_yyyy;
	private AllCachesController cacheController;
	
	private Link resetloglevelsButton;
	private Link gcButton;
	private Controller clusterController;
	private Link enableHibernateStatisticsButton;
	private Link disableHibernateStatisticsButton;
	private Link clearHibernateStatisticsButton;
	private CloseableModalController cmc;	
	private LogRealTimeViewerController logViewerCtr;
	private Controller infoMsgCtrl;
	// 22.09.2009/cg De-activate FileSystemTest 
	//private Controller fileSystemTestController;
	
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

		lockController = new LockController(ureq, getWindowControl());
		myErrors = createVelocityContainer("errors");
		myLoglevels = createVelocityContainer("loglevels");
		resetloglevelsButton = LinkFactory.createButton("resetloglevels", myLoglevels, this);
		
		mySysinfo = createVelocityContainer("sysinfo");
		gcButton = LinkFactory.createButton("run.gc", mySysinfo, this);
		// add system startup time
		SimpleDateFormat startupTimeFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", ureq.getLocale());
		mySysinfo.contextPut("startupTime", startupTimeFormatter.format(new Date(WebappHelper.getTimeOfServerStartup())));		
		
		mySnoop = createVelocityContainer("snoop");

		myHibernateInfo = createVelocityContainer("hibernateinfo");
		enableHibernateStatisticsButton = LinkFactory.createButton("enable.hibernate.statistics", myHibernateInfo, this);
		disableHibernateStatisticsButton = LinkFactory.createButton("disable.hibernate.statistics", myHibernateInfo, this);
		clearHibernateStatisticsButton = LinkFactory.createButton("clear.hibernate.statistics", myHibernateInfo, this);
		
		requestLoglevelController = new RequestLoglevelController(ureq, getWindowControl());
		myMultiUserEvents = createVelocityContainer("multiuserevents");
		
		//info message controller has two implementations (SingleVM or cluster)
		InfoMessageManager InfoMgr = (InfoMessageManager)CoreSpringFactory.getBean(InfoMessageManager.class);
		infoMsgCtrl = InfoMgr.getInfoMessageController(ureq, getWindowControl());
			
		tabbedPane = new TabbedPane("tp", ureq.getLocale());
		tabbedPane.addTab(ACTION_INFOMSG,infoMsgCtrl.getInitialComponent());
		tabbedPane.addTab(ACTION_ERRORS, myErrors);
		//fxdiff: FXOLAT-79 check fxadmin-rights
		tabbedPane.addTab(ACTION_LOGLEVELS, myLoglevels);		
		tabbedPane.addTab(ACTION_SYSINFO, mySysinfo);
		tabbedPane.addTab(ACTION_SNOOP, mySnoop);
		tabbedPane.addTab("requestloglevel", requestLoglevelController.getInitialComponent());
		tabbedPane.addTab(ACTION_LOCKS, lockController.getInitialComponent());
		// fxdiff: not usable:	tabbedPane.addTab(getTranslator().translate("sess.multiuserevents"), myMultiUserEvents);
		tabbedPane.addTab(ACTION_HIBERNATEINFO, myHibernateInfo);
		
		//fxdiff: no cluster anyway:
//		AutoCreator controllerCreator = (AutoCreator)CoreSpringFactory.getBean("clusterAdminControllerCreator");
//		clusterController = controllerCreator.createController(ureq, wControl);
//		tabbedPane.addTab("Cluster", clusterController.getInitialComponent());

		cachePanel = new Panel("cachepanel");
		tabbedPane.addTab("caches", cachePanel);
		
		VelocityContainer myBuildinfo = createVelocityContainer("buildinfo");
		fillBuildInfoTab(myBuildinfo);		
		tabbedPane.addTab("buildinfo", myBuildinfo);
		
		// 22.09.2009/cg De-activate FileSystemTest 
		//fileSystemTestController = new FileSystemTestController(ureq, getWindowControl() );
		//tabbedPane.addTab("fstest", fileSystemTestController.getInitialComponent());
		
		tabbedPane.addListener(this);
		putInitialPanel(tabbedPane);
		
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String sNow = sdf.format(now);
		err_dd = sNow.substring(0, 2);
		err_mm = sNow.substring(3, 5);
		err_yyyy = sNow.substring(6, 10);
		myErrors.contextPut("highestError", Tracing.getErrorCount());
		myErrors.contextPut("mydd", err_dd);
		myErrors.contextPut("mymm", err_mm);
		myErrors.contextPut("myyyyy", err_yyyy);
		myErrors.contextPut("olat_formatter", Formatter.getInstance(ureq.getLocale()));
		myErrors.contextPut("example_error", Settings.getNodeInfo()  + "-E12 "+ Settings.getNodeInfo()  + "-E64..." );
		//FIXME:fj:b do not use this call
		event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, mySessions));
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
			if (newComponent == cachePanel) {
				if (cacheController != null) {
					cacheController.dispose();
				}
				cacheController = new AllCachesController(ureq, getWindowControl());
				cachePanel.setContent(cacheController.getInitialComponent());
			}
			else if (newComponent == infoMsgCtrl.getInitialComponent()) {
				
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
			else if (newComponent == lockController.getInitialComponent()) {
				lockController.resetTableModel();
			}
			else if (newComponent == myMultiUserEvents) {
				StringBuilder sb = new StringBuilder();
				Map infocenter = CoordinatorManager.getInstance().getCoordinator().getEventBus().getUnmodifiableInfoCenter();
				int cntRes = infocenter.size();
				// cluster::: sort the entries (table?): sort by count and name
				// REVIEW:2008-12-11:pb access ea.getListenerCount -> possible dead lock
				// -> look for a different way to show info
				// see also OLAT-3681
				//
				/*
				sb.append("Total (possible weak-referenced) Resources: "+cntRes+" (showing only those with listeners, 'null' for a listener value meaning the OLAT system), count is cluster-wide, identities only vm-wide<br /><br />");
				for (Iterator it_ores = infocenter.entrySet().iterator(); it_ores.hasNext();) {
					Map.Entry entry = (Map.Entry) it_ores.next();
					String oresDerivedString = (String) entry.getKey();
					EventAgency ea = (EventAgency) entry.getValue();
					Set listenIdentNames = ea.getListeningIdentityNames();
					if (listenIdentNames.size() > 0) {
						sb.append("<b>Resource:</b> [").append(ea.getListenerCount()).append("] on ").append(oresDerivedString).append("<br />Listeners: ");
						for (Iterator it_id = listenIdentNames.iterator(); it_id.hasNext();) {
							String login = (String) it_id.next();
							sb.append(login).append("; ");
						}
						sb.append("<br /><br />");
					}
				}
				*/
				sb.append(" <a href=\"http://bugs.olat.org/jira/browse/OLAT-3681\">OLAT-3681</a> ");
				myMultiUserEvents.contextPut("info", sb.toString());
			}
			else if (newComponent == myHibernateInfo) {
				myHibernateInfo.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
				myHibernateInfo.contextPut("hibernateStatistics", DBFactory.getInstance(false).getStatistics());
			} 			 
			else if (newComponent == myLoglevels) {
				List loggers = Tracing.getLoggersSortedByName(); // put it in a list in case of a reload (enum can only be used once)
				myLoglevels.contextPut("loggers", loggers);
			
			} 
			else if (newComponent == mySnoop) {
				mySnoop.contextPut("snoop", getSnoop(ureq));
			}
		} 
		else if (source == myLoglevels) {
			if (event.getCommand().equals(ACTION_SETLEVEL)) {
				String level = ureq.getHttpReq().getParameter("level");
				String logger = ureq.getHttpReq().getParameter("logger");
				if (logger.equals(org.olat.core.logging.Tracing.class.getName())) {
					getWindowControl().setError("log level of "+org.olat.core.logging.Tracing.class.getName()+" must not be changed!");
					return;
				}
				Level l;
				if (level.equals("debug")) l = Level.DEBUG;
				else if (level.equals("info")) l = Level.INFO;
				else if (level.equals("warn")) l = Level.WARN;
				else l = Level.ERROR;
				
				Tracing.setLevelForLogger(l, logger);
				getWindowControl().setInfo("Set logger " + logger + " to level " + level);
				
			} else if (event.getCommand().equals(ACTION_VIEWLOG)) {
				String toBeViewed = ureq.getParameter(ACTION_VIEWLOG_PACKAGE);
				if (toBeViewed == null) return; // should not happen
				if (logViewerCtr != null)	logViewerCtr.dispose();
				logViewerCtr = new LogRealTimeViewerController(ureq, getWindowControl(), toBeViewed, Level.ALL, true);
				if (cmc != null)	cmc.dispose();
				cmc = new CloseableModalController(getWindowControl(), getTranslator().translate("close"), logViewerCtr.getInitialComponent());
				cmc.addControllerListener(this);
				cmc.activate();
			}
			// push loglevel list again
			event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myLoglevels));
		} 
		else if (source == resetloglevelsButton){
			Tracing.setLevelForAllLoggers(Level.INFO);
			getWindowControl().setInfo("All loglevels set to INFO");
		}
		else if (source == gcButton){
			Runtime.getRuntime().gc();
			getWindowControl().setInfo("Garbage collection done.");
			event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, mySysinfo));
		}
		else if (source == myErrors) {
			HttpServletRequest hreq = ureq.getHttpReq();
			err_nr = hreq.getParameter("mynr");
			if (hreq.getParameter("mydd") != null)
				err_dd = hreq.getParameter("mydd");
			if (hreq.getParameter("mymm") != null)
				err_mm = hreq.getParameter("mymm");
			if (hreq.getParameter("myyyyy") != null)
				err_yyyy = hreq.getParameter("myyyyy");
			if (err_nr != null) {
				myErrors.contextPut("mynr", err_nr);
				myErrors.contextPut("errormsgs", LogFileParser.getError(err_nr, err_dd, err_mm, err_yyyy, true));
			}

			myErrors.contextPut("highestError", Tracing.getErrorCount());
			myErrors.contextPut("mydd", err_dd);
			myErrors.contextPut("mymm", err_mm);
			myErrors.contextPut("myyyyy", err_yyyy);
			myErrors.contextPut("olat_formatter", Formatter.getInstance(ureq.getLocale()));

		} 
		else if (source == enableHibernateStatisticsButton){
			DBFactory.getInstance(false).getStatistics().setStatisticsEnabled(true);
			myHibernateInfo.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
			getWindowControl().setInfo("Hibernate statistics enabled.");
			event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myHibernateInfo));
		}
		else if (source == disableHibernateStatisticsButton){
			DBFactory.getInstance(false).getStatistics().setStatisticsEnabled(false);
			myHibernateInfo.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
			getWindowControl().setInfo("Hibernate statistics disabled.");
			event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myHibernateInfo));
		}
		else if (source == clearHibernateStatisticsButton){
			DBFactory.getInstance(false).getStatistics().clear();
			getWindowControl().setInfo("Hibernate statistics clear done.");
			event(ureq, tabbedPane, new TabbedPaneChangedEvent(null, myHibernateInfo));
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cmc.dispose();
			cmc = null;
			if (logViewerCtr != null) {
				logViewerCtr.dispose();
				logViewerCtr = null;
			}
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

	/**
	 * @param ureq
	 * @return Formatted HTML
	 */
	private String getSnoop(UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		HttpServletRequest hreq = ureq.getHttpReq();
		sb.append("<h4>Request attributes:</h4>");
		Enumeration e = hreq.getAttributeNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			Object value = hreq.getAttribute(key);
			appendFormattedKeyValue(sb, key, value);
		}

		appendFormattedKeyValue(sb, "Protocol", hreq.getProtocol());
		appendFormattedKeyValue(sb, "Scheme", hreq.getScheme());
		appendFormattedKeyValue(sb, "Server Name", hreq.getServerName());
		appendFormattedKeyValue(sb, "Server Port", new Integer(hreq.getServerPort()));
		appendFormattedKeyValue(sb, "Remote Addr", hreq.getRemoteAddr());
		appendFormattedKeyValue(sb, "Remote Host", hreq.getRemoteHost());
		appendFormattedKeyValue(sb, "Character Encoding", hreq.getCharacterEncoding());
		appendFormattedKeyValue(sb, "Content Length", new Integer(hreq.getContentLength()));
		appendFormattedKeyValue(sb, "Content Type", hreq.getContentType());
		appendFormattedKeyValue(sb, "Locale", hreq.getLocale());

		sb.append("<h4>Parameter names in this hreq:</h4>");
		e = hreq.getParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String[] values = hreq.getParameterValues(key);
			String value = "";
			for (int i = 0; i < values.length; i++) {
				value = value + " " + values[i];
			}
			appendFormattedKeyValue(sb, key, value);
		}
		
		sb.append("<h4>Headers in this hreq:</h4>");
		e = hreq.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = hreq.getHeader(key);
			appendFormattedKeyValue(sb, key, value);
		}
		sb.append("<h4>Cookies in this hreq:</h4>");
		Cookie[] cookies = hreq.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				appendFormattedKeyValue(sb, cookie.getName(), cookie.getValue());
			}
		}

		sb.append("<h4>Hreq parameters:</h4>");
		appendFormattedKeyValue(sb, "Request Is Secure", new Boolean(hreq.isSecure()));
		appendFormattedKeyValue(sb, "Auth Type", hreq.getAuthType());
		appendFormattedKeyValue(sb, "HTTP Method", hreq.getMethod());
		appendFormattedKeyValue(sb, "Remote User", hreq.getRemoteUser());
		appendFormattedKeyValue(sb, "Request URI", hreq.getRequestURI());
		appendFormattedKeyValue(sb, "Context Path", hreq.getContextPath());
		appendFormattedKeyValue(sb, "Servlet Path", hreq.getServletPath());
		appendFormattedKeyValue(sb, "Path Info", hreq.getPathInfo());
		appendFormattedKeyValue(sb, "Path Trans", hreq.getPathTranslated());
		appendFormattedKeyValue(sb, "Query String", hreq.getQueryString());

		HttpSession hsession = hreq.getSession();
		appendFormattedKeyValue(sb, "Requested Session Id", hreq.getRequestedSessionId());
		appendFormattedKeyValue(sb, "Current Session Id", hsession.getId());
		appendFormattedKeyValue(sb, "Session Created Time", new Long(hsession.getCreationTime()));
		appendFormattedKeyValue(sb, "Session Last Accessed Time", new Long(hsession.getLastAccessedTime()));
		appendFormattedKeyValue(sb, "Session Max Inactive Interval Seconds",	new Long(hsession.getMaxInactiveInterval()));
		
		sb.append("<h4>Session values:</h4> ");
		Enumeration names = hsession.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			appendFormattedKeyValue(sb, name, hsession.getAttribute(name));
		}
		return sb.toString();
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
		if (cacheController != null) {
			cacheController.dispose();
			cacheController = null;
		}
		if (clusterController != null) clusterController.dispose();
		if (cmc != null) {
			cmc.dispose();
			cmc = null;
		}
		if (logViewerCtr != null) {
			logViewerCtr.dispose();
			logViewerCtr = null;
		}
	}
}
