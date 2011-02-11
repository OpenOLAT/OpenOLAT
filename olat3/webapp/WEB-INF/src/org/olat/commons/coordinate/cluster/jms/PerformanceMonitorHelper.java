package org.olat.commons.coordinate.cluster.jms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.Probe;
import org.olat.testutils.codepoints.client.StatId;
import org.olat.testutils.codepoints.server.Codepoint;

public class PerformanceMonitorHelper {

	private static boolean isStarted_ = false;
	
	private static CodepointClient codepointClient = null;
	
	private static Map<String,Probe> probes_ = new HashMap<String,Probe>();
	
	public static synchronized boolean isStarted() {
		return isStarted_;
	}
	
	public static synchronized List<PerfItem> getPerfItems() {
		if (!isStarted_ || (codepointClient==null)) {
			return new LinkedList<PerfItem>();
		}
		List<PerfItem> ll = new LinkedList<PerfItem>();
		try {
			ll.add(new PerfItem(
					"DBImpl session initialize initialize", 1, -1, -1, -1, -1, -1, -1,  -1, -1, -1, -1, 
					codepointClient.getCodepoint("org.olat.core.commons.persistence.DBImpl.initializeSession").getHitCount()));
			ll.add(new PerfItem(
					"DBImpl session initialize close", 1, -1, -1, -1, -1, -1, -1,  -1, -1, -1, -1, 
					codepointClient.getCodepoint("org.olat.core.commons.persistence.DBImpl.closeSession").getHitCount()));
			ll.add(new PerfItem(
					"ClusterCacher received event", 1, -1, -1, -1, -1, -1, -1,  -1, -1, -1, -1, 
					codepointClient.getCodepoint("org.olat.core.util.cache.n.impl.cluster.ClusterCacher.event").getHitCount()));
			ll.add(new PerfItem(
					"ClusterCacher invalidate cache entry", 1, -1, -1, -1, -1, -1, -1,  -1, -1, -1, -1, 
					codepointClient.getCodepoint("org.olat.core.util.cache.n.impl.cluster.ClusterCacher.invalidateKeys").getHitCount()));
			ll.add(new PerfItem(
					"ClusterCacher send 'invalidate cache entry' event", 1, -1, -1, -1, -1, -1, -1,  -1, -1, -1, -1, 
					codepointClient.getCodepoint("org.olat.core.util.cache.n.impl.cluster.ClusterCacher.sendChangedKeys").getHitCount()));

			Set<Entry<String, Probe>> s = probes_.entrySet();
			for (Iterator<Entry<String, Probe>> it = s.iterator(); it.hasNext();) {
				Entry<String, Probe> entry = it.next();
				Probe p = entry.getValue();
				ll.add(
						new PerfItem(
								entry.getKey(), 
								p.getStatValue(StatId.MIN_TIME_ELAPSED),
								p.getStatValue(StatId.MAX_TIME_ELAPSED),
								p.getStatValue(StatId.LAST_TIME_ELAPSED),
								p.getStatValue(StatId.TOTAL_AVERAGE_TIME_ELAPSED),
								p.getStatValue(StatId.LAST_10MEASUREMENT_AVERAGE_TIME_ELAPSED),
								p.getStatValue(StatId.LAST_100MEASUREMENT_AVERAGE_TIME_ELAPSED),
								p.getStatValue(StatId.LAST_1000MEASUREMENT_AVERAGE_TIME_ELAPSED),
								p.getStatValue(StatId.TOTAL_FREQUENCY),
								p.getStatValue(StatId.LAST_10_FREQUENCY),
								p.getStatValue(StatId.LAST_100_FREQUENCY),
								p.getStatValue(StatId.LAST_1000_FREQUENCY),
								p.getStart().getHitCount()));
			}
		} catch (CommunicationException e) {
			ll.add(new PerfItem("Codepoint problem - no stats available there", -1,-1,-1,-1,-1, -1, -1, -1, -1, -1, -1, -1 ));
		}
		return ll;
	}

	public static synchronized boolean toggleStartStop() {
		if (isStarted_) {
			// then stop
			isStarted_ = false;
			for (Iterator<Probe> it = probes_.values().iterator(); it
					.hasNext();) {
				Probe p = it.next();
				try{
					p.close();
				} catch(CommunicationException e) {
					// ignore
				}
			}
			probes_.clear();
			codepointClient.close();
			codepointClient = null;
			return true;
		} else {
			// then start
			isStarted_ = true;
			try{
				codepointClient = Codepoint.getLocalLoopCodepointClient();
			} catch(RuntimeException re) {
				isStarted_ = false;
				return false;
			}
			try {
				codepointClient.setAllHitCounts(0);
				probes_.put("ClusterSyncer.doInSync", 
						codepointClient.startProbingBetween(
								"org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-before-sync.org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync", 
								"org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync"));
				probes_.put("DBImpl session initialize->close", 
						codepointClient.startProbingBetween(
								"org.olat.core.commons.persistence.DBImpl.initializeSession", 
								"org.olat.core.commons.persistence.DBImpl.closeSession"));
				probes_.put("DispatcherAction.execute", 
						codepointClient.startProbingBetween(
								"org.olat.core.dispatcher.DispatcherAction.execute-start", 
								"org.olat.core.dispatcher.DispatcherAction.execute-end"));
				probes_.put("DBQueryImpl.list", 
						codepointClient.startProbingBetween(
								"org.olat.core.commons.persistence.DBQueryImpl.list-entry", 
								"org.olat.core.commons.persistence.DBQueryImpl.list-exit"));
			} catch (CommunicationException e) {
				// ignore ?
			}
			return true;
		}
	}

	public static synchronized void resetStats() {
		if (!isStarted_) {
			return;
		}
		Set<Entry<String, Probe>> s = probes_.entrySet();
		for (Iterator<Entry<String, Probe>> it = s.iterator(); it.hasNext();) {
			Entry<String, Probe> entry = it.next();
			try {
				entry.getValue().clearStats();
				entry.getValue().getStart().setHitCount(0);
				entry.getValue().getEnd().setHitCount(0);
			} catch (CommunicationException e) {
				// well, ignore
			}
		}
		
	}
}
