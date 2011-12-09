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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.coordinate.cluster.jms.ClusterEventBus;
import org.olat.commons.coordinate.cluster.jms.SimpleProbe;
import org.olat.core.commons.persistence.DBQueryImpl;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.CourseModule;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.Probe;
import org.olat.testutils.codepoints.client.StatId;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<br>
 * Wraps the request to get MRTGStatistics into a dispatcher.
 * 
 * <P>
 * Initial Date:  13.06.2006 <br>
 * @author patrickb
 */
public class MRTGStatsDispatcher implements Dispatcher {
	// default allows monitoring only from localhost
	// "*" means allow from any host (not recommended in real world setups)
	private String monitoringHost = "127.0.0.1"; 
	private long lastErrorCount = 0;
	private String instanceId;
	
	private CodepointClient codepointClient_;
	private Probe dispatchProbe_;
	private Probe doInSyncEnterProbe_;
	private Probe doInSyncInsideProbe_;
	private Probe dbQueryListProbe_;
	
	private CoordinatorManager coordinatorManager;
	
	/**
	 * [spring only]
	 */
	private MRTGStatsDispatcher(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
		try{
			codepointClient_ = Codepoint.getLocalLoopCodepointClient();
			dispatchProbe_ = codepointClient_.startProbingBetween(
					"org.olat.core.dispatcher.DispatcherAction.execute-start", 
					"org.olat.core.dispatcher.DispatcherAction.execute-end");
			doInSyncEnterProbe_ = codepointClient_.startProbingBetween(
					"org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-before-sync.org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync", 
					"org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync");
			doInSyncInsideProbe_ = codepointClient_.startProbingBetween(
					"org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync", 
					"org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-after-sync.org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync");
			dbQueryListProbe_ = codepointClient_.startProbingBetween(
					"org.olat.core.commons.persistence.DBQueryImpl.list-entry", 
					"org.olat.core.commons.persistence.DBQueryImpl.list-exit");
			dispatchProbe_.logifSlowerThan(8000, Level.WARNING);
			doInSyncEnterProbe_.logifSlowerThan(200, Level.WARNING);
			doInSyncInsideProbe_.logifSlowerThan(1000, Level.WARNING);
			dbQueryListProbe_.logifSlowerThan(1300, Level.WARNING);
		} catch(RuntimeException re) {
			Tracing.logInfo("Certain MRTG Statistics will not be available since Codepoints are disabled", getClass());
		} catch (CommunicationException e) {
			Tracing.logInfo("Certain MRTG Statistics will not be available since Codepoints are disabled", getClass());
		}
	}
	
	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		if(Tracing.isDebugEnabled(MRTGStatsDispatcher.class)){
			Tracing.logDebug("serving MRTGStats on uriPrefix [[["+uriPrefix+"]]]", MRTGStatsDispatcher.class);
		}
		returnMRTGStats(request, response);
	}

	private String roundedStatValueOf(Probe probe, StatId statId) {
		try{
			final int value = (int)Math.round(probe.getStatValue(statId)*100)/100;
			if (value>=0) {
				return String.valueOf(value);
			} else {
				return "0";
			}
		} catch (CommunicationException e) {
			e.printStackTrace(System.out);
			return "0";
		}
	}
		
	private String roundedValueOf(long value) {
		if (value>=0) {
			return String.valueOf(value);
		} else {
			return "0";
		}
	}
	
	/**
	 * @param request
	 * @param response
	 */
	private void returnMRTGStats(HttpServletRequest request, HttpServletResponse response) {
		if (!request.getRemoteAddr().equals(monitoringHost) && !monitoringHost.equals("*")) { 
			// limit to allowed hosts
			Tracing.logAudit("Trying to access stats from other host than configured (" + monitoringHost + ") : " + request.getRemoteAddr(), SysinfoController.class);
			DispatcherAction.sendForbidden(request.getPathInfo(), response);
		}
		String command = request.getParameter("cmd");
		if (command == null) command = "users";
		StringBuilder result = new StringBuilder();
		int httpsCount = 0;
		int activeSessionCnt = 0;
		if (command.equals("users")) { // get user stats of (authenticated) usersessions
			Set userSessions = UserSession.getAuthenticatedUserSessions();
			for (Iterator it_usess = userSessions.iterator(); it_usess.hasNext();) {
				UserSession usess = (UserSession) it_usess.next();
				activeSessionCnt++;
				SessionInfo sessInfo = usess.getSessionInfo();
				if (sessInfo.isSecure()) httpsCount++;
			}
			result.append(activeSessionCnt); // active authenticated sessions
			result.append("\n");
			result.append(httpsCount); // ,,, of which are secure
			result.append("\n0\n");
			result.append(instanceId);
		} else if (command.equals("webdav")) { // get webdav stats of (authenticated) usersessions
			Set userSessions = UserSession.getAuthenticatedUserSessions();
			int webdavcount = 0;
			int securewebdavcount = 0;
			for (Iterator it_usess = userSessions.iterator(); it_usess.hasNext();) {
				UserSession usess = (UserSession) it_usess.next();
				SessionInfo sessInfo = usess.getSessionInfo();
				if (sessInfo.isWebDAV()) {
					webdavcount++;
					if (sessInfo.isSecure()) securewebdavcount++;
				}
			}
			result.append(webdavcount); // webdav sessions
			result.append("\n");
			result.append(securewebdavcount); // ,,, of which are secure
			result.append("\n0\n");
			result.append(instanceId);
		} else if (command.equals("imstats")) { // get Jabber info
			if (InstantMessagingModule.isEnabled()) {
				result.append(InstantMessagingModule.getAdapter().countConnectedUsers());
				result.append("\n");
				//result.append(InstantMessagingModule.getAdapter().countUsersRunningFlashClient());
				result.append(0);
				result.append("\n0\n");
				result.append(instanceId);
			} else {
				result.append("0\n0\n0\n");
				result.append(instanceId);
			}
		} else if (command.equals("debug")) { // get debug stats
			// IMPORTANT: do not call too often, since .size() of a weakhashmap may be an expensive operation.
			// our mrtg default is: once every five minutes.
			int controllerCnt = DefaultController.getControllerCount();
			result.append(controllerCnt); // active and not yet disposed
			result.append("\n0\n0\n");
			result.append(instanceId);
			
		} else if (command.equals("mem")) { // get VM memory stats
			Runtime r = Runtime.getRuntime();
			long totalMem = r.totalMemory();
			// Total used memory in megabyptes
			result.append((totalMem - r.freeMemory())/1000000).append("\n"); 
			// Max available memory in VM in megabytes
			result.append(r.maxMemory()/1000000).append("\n"); 
			result.append("0\n");
			result.append(instanceId);

		} else if (command.equals("proc")) { // get VM process stats
			// Number of concurrent dispatching OLAT threads (concurrent user requests)
			result.append(DispatcherAction.getConcurrentCounter()).append("\n"); 
			// Number of active threads
			ThreadGroup group = Thread.currentThread().getThreadGroup();
			Thread[] threads = new Thread[ group.activeCount() ]; 
		    group.enumerate( threads, false ); 
	    	int counter = 0;
	    	for ( Thread t : threads ) {
	    		if (t == null) continue;
	    		// http-8080-Processor and TP-Processor
	    		// not precise, but good enouth
	      		if ( t.getName().indexOf("-Processor") != -1) {
	      		counter++;
	      		}
			}
			result.append(counter).append("\n"); 
			result.append("0\n");
			result.append(instanceId);

		} else if (command.equals("err")) { // get error stats
				// Average number of errors per minute since last call
			long currentErrorCount = Tracing.getErrorCount();
			long errorDifference = currentErrorCount - lastErrorCount;

			lastErrorCount = currentErrorCount;
			result.append(errorDifference).append("\n"); 
			result.append("0\n0\n");
			result.append(instanceId);
			
		} else if (command.startsWith("dispatch")) {
			
			if (dispatchProbe_==null) {
				result.append("0\n0\n0\n");
			} else {
				if (command.equals("dispatchAvg")) {
					result.append(roundedStatValueOf(dispatchProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
				} else if (command.equals("dispatchMax")) {
					result.append(roundedStatValueOf(dispatchProbe_, StatId.MAX_TIME_ELAPSED));
				} else if (command.equals("dispatchCnt")) {
					result.append(roundedStatValueOf(dispatchProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
				} else if (command.equals("dispatchReset")) {
					try {
						dispatchProbe_.clearStats();
					} catch (CommunicationException e) {
						e.printStackTrace(System.out);
						// ignore otherwise
					}
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
			
		} else if (command.startsWith("dbQueryList")) {
			
			if (dbQueryListProbe_==null) {
				result.append("0\n0\n0\n");
			} else {
				if (command.equals("dbQueryListAvg")) {
					result.append(roundedStatValueOf(dbQueryListProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
				} else if (command.equals("dbQueryListMax")) {
					result.append(roundedStatValueOf(dbQueryListProbe_, StatId.MAX_TIME_ELAPSED));
				} else if (command.equals("dbQueryListCnt")) {
					result.append(roundedStatValueOf(dbQueryListProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
				} else if (command.equals("dbQueryListReset")) {
					try {
						dbQueryListProbe_.clearStats();
					} catch (CommunicationException e) {
						e.printStackTrace(System.out);
						// ignore otherwise
					}
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
			
		} else if (command.startsWith("doInSyncEnter")) {
			
			if (doInSyncEnterProbe_==null) {
				result.append("0\n0\n0\n");
			} else {
				if (command.equals("doInSyncEnterAvg")) {
					result.append(roundedStatValueOf(doInSyncEnterProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
				} else if (command.equals("doInSyncEnterMax")) {
					result.append(roundedStatValueOf(doInSyncEnterProbe_, StatId.MAX_TIME_ELAPSED));
				} else if (command.equals("doInSyncEnterCnt")) {
					result.append(roundedStatValueOf(doInSyncEnterProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
				} else if (command.equals("doInSyncEnterReset")) {
					try {
						doInSyncEnterProbe_.clearStats();
					} catch (CommunicationException e) {
						e.printStackTrace(System.out);
						// ignore otherwise
					}
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);

		} else if (command.startsWith("doInSyncInside")) {
			
			if (doInSyncInsideProbe_==null) {
				result.append("0\n0\n0\n");
			} else {
				if (command.equals("doInSyncInsideAvg")) {
					result.append(roundedStatValueOf(doInSyncInsideProbe_, StatId.TOTAL_AVERAGE_TIME_ELAPSED));
				} else if (command.equals("doInSyncInsideMax")) {
					result.append(roundedStatValueOf(doInSyncInsideProbe_, StatId.MAX_TIME_ELAPSED));
				} else if (command.equals("doInSyncInsideCnt")) {
					result.append(roundedStatValueOf(doInSyncInsideProbe_, StatId.TOTAL_MEASUREMENTS_COUNT));
				} else if (command.equals("doInSyncInsideReset")) {
					try {
						doInSyncInsideProbe_.clearStats();
					} catch (CommunicationException e) {
						e.printStackTrace(System.out);
						// ignore otherwise
					}
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.startsWith("jmsDelivery")) {
			
			final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
			if (clusterEventBus==null) {
				result.append("0\n0\n0\n");
			} else {
				SimpleProbe probe = clusterEventBus.getMrtgProbeJMSDeliveryTime();
				if (command.equals("jmsDeliveryAvg")) {
					result.append(roundedValueOf(probe.getAvg()));
				} else if (command.equals("jmsDeliveryMax")) {
					result.append(roundedValueOf(probe.getMax()));
				} else if (command.equals("jmsDeliveryCnt")) {
					result.append(roundedValueOf(probe.getNum()));
				} else if (command.equals("jmsDeliveryReset")) {
					probe.reset();
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.startsWith("jmsProcessing")) {
			
			final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
			if (clusterEventBus==null) {
				result.append("0\n0\n0\n");
			} else {
				SimpleProbe probe = clusterEventBus.getMrtgProbeJMSProcessingTime();
				if (command.equals("jmsProcessingAvg")) {
					result.append(roundedValueOf(probe.getAvg()));
				} else if (command.equals("jmsProcessingMax")) {
					result.append(roundedValueOf(probe.getMax()));
				} else if (command.equals("jmsProcessingCnt")) {
					result.append(roundedValueOf(probe.getNum()));
				} else if (command.equals("jmsProcessingReset")) {
					probe.reset();
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.startsWith("jmsWaiting")) {
			
			final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
			if (clusterEventBus==null) {
				result.append("0\n0\n0\n");
			} else {
				SimpleProbe probe = clusterEventBus.getMrtgProbeJMSLoad();
				if (command.equals("jmsWaitingAvg")) {
					result.append(roundedValueOf(probe.getAvg()));
				} else if (command.equals("jmsWaitingMax")) {
					result.append(roundedValueOf(probe.getMax()));
				} else if (command.equals("jmsWaitingCnt")) {
					result.append(roundedValueOf(probe.getNum()));
				} else if (command.equals("jmsWaitingReset")) {
					probe.reset();
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.startsWith("jmsQueued")) {
			
			final ClusterEventBus clusterEventBus = (ClusterEventBus) coordinatorManager.getCoordinator().getEventBus();
			if (clusterEventBus==null) {
				result.append("0\n0\n0\n");
			} else {
				SimpleProbe probe = clusterEventBus.getMrtgProbeJMSEnqueueTime();
				if (command.equals("jmsQueuedAvg")) {
					result.append(roundedValueOf(probe.getAvg()));
				} else if (command.equals("jmsQueuedMax")) {
					result.append(roundedValueOf(probe.getMax()));
				} else if (command.equals("jmsQueuedCnt")) {
					result.append(roundedValueOf(probe.getNum()));
				} else if (command.equals("jmsQueuedReset")) {
					probe.reset();
					result.append("0");
				}
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("SecurityGroupMembershipImpl")) { // SecurityGroupMembershipImpl
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("org.olat.basesecurity.SecurityGroupMembershipImpl");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("BGAreaImpl")) { // BGAreaImpl
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("org.olat.group.area.BGAreaImpl");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("BusinessGroupImpl")) { // BusinessGroupImpl
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("org.olat.group.BusinessGroupImpl");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("OLATResourceImpl")) { // OLATResourceImpl
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("org.olat.resource.OLATResourceImpl");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("TheRest")) { // PolicyImpl
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("THEREST");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("PolicyImpl")) { // PolicyImpl
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("THEREST");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
			
			// dump details about all the non registered ones
			Set<Entry<String, org.olat.core.commons.persistence.SimpleProbe>> entries = DBQueryImpl.listTableStatsMap_.entrySet();
			Set<Entry<String, org.olat.core.commons.persistence.SimpleProbe>> nonRegisteredEntries = new HashSet<Entry<String,org.olat.core.commons.persistence.SimpleProbe>>();
			long sum = 0;
			for (Iterator<Entry<String, org.olat.core.commons.persistence.SimpleProbe>> it = entries.iterator(); it.hasNext();) {
				Entry<String, org.olat.core.commons.persistence.SimpleProbe> entry = it.next();
				if (!DBQueryImpl.registeredTables_.contains(entry.getKey())) {
					nonRegisteredEntries.add(entry);
					sum+=entry.getValue().getSum();
				}
			}
			List<Entry<String, org.olat.core.commons.persistence.SimpleProbe>> list = new LinkedList<Entry<String, org.olat.core.commons.persistence.SimpleProbe>>(nonRegisteredEntries);
			Collections.sort(
					list, new Comparator<Entry<String, org.olat.core.commons.persistence.SimpleProbe>>() {

				public int compare(
						Entry<String, org.olat.core.commons.persistence.SimpleProbe> o1,
						Entry<String, org.olat.core.commons.persistence.SimpleProbe> o2) {
					if (o1.getValue().getSum()>o2.getValue().getSum()) {
						return 1;
					} else if (o1.getValue().getSum()==o2.getValue().getSum()) {
						return 0;
					} else {
						return -1;
					}
				}
				
			});
			for (Iterator<Entry<String, org.olat.core.commons.persistence.SimpleProbe>> it = list.iterator(); it.hasNext();) {
				Entry<String, org.olat.core.commons.persistence.SimpleProbe> entry = it.next();
				Tracing.logInfo("MRTGStats: table '"+entry.getKey()+"' uses up "+entry.getValue().getSum()+"ms of a total of "+sum+"ms, which is "+Math.round(1000.0*entry.getValue().getSum()/sum)/10+"%", getClass());
				entry.getValue().reset();
			}
		} else if (command.equals("LifeCycleEntry")) { // LifeCycleEntry
			org.olat.core.commons.persistence.SimpleProbe probe = DBQueryImpl.listTableStatsMap_.get("org.olat.commons.lifecycle.LifeCycleEntry");
			if (probe==null) {
				result.append("0\n0\n0\n");
			} else {
				result.append(roundedValueOf(probe.getSum()));
				probe.reset();
				result.append("\n0\n0\n");
			}
			result.append(instanceId);
		} else if (command.equals("usercount")) { // get number of useraccounts counter
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			SecurityGroup olatuserGroup = secMgr.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
			int users = secMgr.countIdentitiesOfSecurityGroup(olatuserGroup);
			long disabled = secMgr.countIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_LOGIN_DENIED);			
			result.append(users-disabled).append("\n"); // number of active users
			result.append(disabled).append("\n"); // number of disabled users
			result.append("0\n");
			result.append(instanceId);
			
		} else if (command.equals("usercountmonthly")) { // get number of different users logged in during last month and last half year
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			Calendar lastLoginLimit = Calendar.getInstance();
			lastLoginLimit.add(Calendar.MONTH, -1);
			result.append(secMgr.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
			lastLoginLimit.add(Calendar.MONTH, -5); // -1 -5 = -6 for half a year
			result.append(secMgr.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
			result.append("0\n");
			result.append(instanceId);
		
		} else if (command.equals("usercountdaily")) { // get number of different users logged in during last day and last week
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			Calendar lastLoginLimit = Calendar.getInstance();
			lastLoginLimit.add(Calendar.DAY_OF_YEAR, -1);
			result.append(secMgr.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
			lastLoginLimit.add(Calendar.DAY_OF_YEAR, -6); // -1 - 6 = -7 for last week
			result.append(secMgr.countUniqueUserLoginsSince(lastLoginLimit.getTime())).append("\n");
			result.append("0\n");
			result.append(instanceId);			
				
		} else if (command.equals("usercountsince")) { // get number of different users logged in since a period which is specified by parameter date
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			String dateParam = request.getParameter("date");
			if (dateParam==null) {
				result.append("date parameter missing. add date=yyyy-MM-dd\n");
			}
			else {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date mydate = df.parse(dateParam);
					result.append(secMgr.countUniqueUserLoginsSince(mydate)).append("\n");
				} catch (ParseException e) {
					result.append("date parameter format error. expected: yyyy-MM-dd\n");
				}
				
				result.append("0\n0\n");
				result.append(instanceId);
			}
				
		}	else if (command.equals("coursecount")) { // get number of activated courses
			RepositoryManager repoMgr = RepositoryManager.getInstance();
			int allCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_OWNERS);
			int publishedCourses = repoMgr.countByTypeLimitAccess(CourseModule.ORES_TYPE_COURSE, RepositoryEntry.ACC_USERS);
			result.append(allCourses).append("\n"); // number of all courses
			result.append(publishedCourses).append("\n"); // number of published courses
			result.append("0\n");
			result.append(instanceId);
		}
		
		ServletUtil.serveStringResource(request, response, result.toString());
	}

	/**
	 * Setter for Spring configuration
	 * @param monitoringHost
	 */
	public void setMonitoringHost(String monitoringHost) {
		this.monitoringHost = monitoringHost;
	}
	
	/**
	 * Spring getter
	 * @return
	 */
	public String getMonitoringHost() {
		return monitoringHost;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}


	
	
}
