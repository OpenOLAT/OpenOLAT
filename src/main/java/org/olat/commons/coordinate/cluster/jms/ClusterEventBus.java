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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cluster.ClusterConfig;
import org.olat.core.util.event.AbstractEventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.event.businfo.BusListenerInfo;
import org.olat.core.util.event.businfo.BusListenerInfos;
import org.olat.core.util.resource.OresHelper;

/**
 * This class realizes a clustered (multiple java vm) system event bus. it uses JMS 
 * (per default, apache activeMQ 4.1.4 is configured using spring) as an implementation.
 * 
 * @author Felix Jost
 */
public class ClusterEventBus extends AbstractEventBus implements MessageListener, GenericEventListener {
	private static final Logger log = Tracing.createLoggerFor(ClusterEventBus.class);
	//ores helper is limited to 50 character, so truncate it
	static final OLATResourceable CLUSTER_CHANNEL = OresHelper.createOLATResourceableType(ClusterEventBus.class.getName().substring(0, 50));

	private ClusterConfig clusterConfig;

	// settings
	private long sendInterval = 5000; // 1000 miliseconds between each "ping/alive/info" message, can be set using spring
	private long jmsMsgDelayLimit = 10000;  // max duration of ClusterInfoEvent send-receive time in ms
	
	// counters
	private long latestSentMsgId = -1;
	private long numOfSentMessages = 0;
	
	// stats
	private List<String> msgsSent = new ArrayList<>(); 
	private List<String> msgsReceived = new ArrayList<>(); 
	private int msgsSentCount = 0;
	private int msgsReceivedCount = 0;
	
	// latest incoming info from other Nodes
	private Map<Integer, NodeInfo> nodeInfos = new HashMap<>();
	
	private int maxListSize = 10; // how many entries are kept in the outbound/inbound history. Just for administrative purposes
	
	// for bookkeeping how many resources have how many listeners
	private final BusListenerInfos busInfos = new BusListenerInfos();
	protected boolean isClusterInfoEventThreadRunning = true;
	private ConnectionFactory connectionFactory;
	private Topic destination;
	private Connection connection;
	private Session sessionConsumer;
	private MessageConsumer consumer;
	private Session sessionProducer;
	private MessageProducer producer;
	
	private long lastOnMessageFinishTime_ = -1;
	private final SimpleProbe mrtgProbeJMSLoad_ = new SimpleProbe();
	private final SimpleProbe mrtgProbeJMSDeliveryTime_ = new SimpleProbe();
	private final SimpleProbe mrtgProbeJMSProcessingTime_ = new SimpleProbe();
	
	private final SimpleProbe mrtgProbeJMSEnqueueTime_ = new SimpleProbe();
	
	private ExecutorService jmsExecutor;
	
	/**
	 * [used by spring]
	 * 
	 * @param jmsTemplate
	 */
	ClusterEventBus() {
		super();
	}

	public void springInit() throws JMSException {
		jmsExecutor = Executors.newSingleThreadExecutor();
		
		connection = connectionFactory.createConnection();
		sessionConsumer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = sessionConsumer.createConsumer(destination);
		consumer.setMessageListener(this);
		sessionProducer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = sessionProducer.createProducer(destination);

		connection.start();
		log.info("ClusterEventBus JMS started");

		final Integer nodeId = clusterConfig.getNodeId();
		Thread t = new Thread(new Runnable() {
			public void run() {
				// send an infopacket to all olat nodes at regular intervals.
				while(isClusterInfoEventThreadRunning) {
					try {
						ClusterInfoEvent cie = new ClusterInfoEvent(clusterConfig, createBusListenerInfo());
						fireEventToListenersOf(cie, CLUSTER_CHANNEL, false);
						if (log.isDebugEnabled()) log.debug("sent via jms clusterInfoEvent with timestamp:{} from node: {}", cie.getCreated(),nodeId);
					} catch (Exception e) {
						// log error, but do not throw exception, but retry.
						try {
							log.error("error while sending ClusterInfoEvent", e);
						} catch (NullPointerException nex) {
							// ignore, could happen when shutting down
							System.err.println("ClusterEventBus : error while sending ClusterInfoEvent, could happen in shutting down, Ex=" + e);
						}
					}
					try {
						Thread.sleep(sendInterval);
					} catch (InterruptedException e) {
						// ignore
					}
				}
				try {
					log.info("ClusterEventBus stopped, do no longer send ClusterInfoEvents");
				} catch (NullPointerException nex) {
					System.err.println("ClusterEventBus stopped, do no longer send ClusterInfoEvents");
				}
			}});
		t.setDaemon(true); // VM can shutdown even when this thread is still running
		t.start();
		// register to listen for other nodes' clusterinfoevents
		registerFor(this, null, CLUSTER_CHANNEL);
	}
	
	public SimpleProbe getMrtgProbeJMSDeliveryTime() {
		return mrtgProbeJMSDeliveryTime_;
	}
	
	public SimpleProbe getMrtgProbeJMSProcessingTime() {
		return mrtgProbeJMSProcessingTime_;
	}
	
	public SimpleProbe getMrtgProbeJMSLoad() {
		return mrtgProbeJMSLoad_;
	}
	
	public SimpleProbe getMrtgProbeJMSEnqueueTime() {
		return mrtgProbeJMSEnqueueTime_;
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		// we listen only on our own channel, the event must be a clusterInfoEvent.
		ClusterInfoEvent cie = (ClusterInfoEvent)event;
		Integer nodeId = cie.getConfig().getNodeId();
		NodeInfo ni = getNodeInfoFor(nodeId);
		ni.update(cie);
		// check duration of send-receive ClusterInfoEvent
		long now = System.currentTimeMillis();
		if ((now - cie.getCreated()) > jmsMsgDelayLimit) {
			log.warn("JMS-Performance problem: JMS-Message delay is too big, send-receive take:" + (now - cie.getCreated()) + "ms. event="+event);
		}
		
		// update the eventBusInfo from the node
		BusListenerInfo busInfo = cie.getBusListenerInfo();
		busInfos.updateInfoFor(nodeId, busInfo);
	}
	
	/**
	 * this implementation must sum up all counts from all cluster nodes to return the correct number. 
	 */
	@Override
	public int getListeningIdentityCntFor(OLATResourceable ores) {
		return busInfos.getListenerCountFor(ores);
	}

	/**
	 * 
	 * @see org.olat.core.util.event.AbstractOLATSystemBus#fireEventToListenersOf(org.olat.core.util.event.MultiUserEvent,
	 *      org.olat.core.id.OLATResourceable)
	 */
	@Override
	public void fireEventToListenersOf(final MultiUserEvent event, final OLATResourceable ores) {
		fireEventToListenersOf(event, ores, true);
	}
	
	private void fireEventToListenersOf(final MultiUserEvent event, final OLATResourceable ores, boolean strict) {
		// send the event wrapped over jms to all nodes 
		// (the receiver will detect whether messages are from itself and thus can be ignored, since they were already sent directly.
		final long msgId = ++latestSentMsgId;
		final Integer nodeId = clusterConfig.getNodeId();
		
		jmsExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					ObjectMessage message = sessionProducer.createObjectMessage();
					message.setObject(new JMSWrapper(nodeId, msgId, ores, event));
					if(strict) {
						producer.send(message);
					} else {
						producer.send(message, DeliveryMode.NON_PERSISTENT, 3, 5000);
					}
				} catch (Exception e) {
					log.error("Cannot send JMS message", e);
					// cluster:::: what shall we do here: the JMS bus is broken! and we thus cannot know if other nodes are alive.
					// if we are the only node running, then we could continue.
					// a) either throw an exception - meaning olat doesn't really run at all and produces redscreens all the time and logging in is not possible.
					// b) or warn in the log/jmx - but surveillance is critical here!!
					// -> do the more fail-fast option a) at the moment for correctness reasons.
					System.err.println("###############################################################################################");
					System.err.println("### ClusterEventBus: communication error with JMS - cannot send messages!!!" + e);
					System.err.println("###############################################################################################");
					
					throw new OLATRuntimeException("communication error with JMS - cannot send messages!!!", e);
				}
				numOfSentMessages++;
			}
		});

		// store it for later access by the admin controller
		String sentMsg = "sent msg: from node:" + nodeId + ", olat-id:" + msgId + ", ores:"	+ ores.getResourceableTypeName() + ":" + ores.getResourceableId()+", event:"+event;
		addToSentScreen(sentMsg);
		if (log.isDebugEnabled()) log.debug(sentMsg);
	}

	/**
	 * called by springs org.springframework.jms.listener.DefaultMessageListenerContainer, see coredefaultconfig.xml
	 * we receive a message here on the topic reserved for olat system bus messages. 
	 */
	@Override
	public void onMessage(Message message) {
		try{
			serveMessage(message, -1);
		} catch(RuntimeException re) {
			log.error("RuntimeException enountered by serve-thread:", re);
		} catch(Error er) {
			log.error("Error enountered by serve-thread:", er);
		} finally {
			try {
				DBFactory.getInstance().commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	void serveMessage(Message message, long receiveEnqueueTime) {
		// stats
		final long receiveTime = System.currentTimeMillis();
		if (receiveEnqueueTime>0) {
			final long diff = receiveTime - receiveEnqueueTime;
			mrtgProbeJMSEnqueueTime_.addMeasurement(diff);
		}
		if (lastOnMessageFinishTime_!=-1) {
			final long waitingTime = receiveTime - lastOnMessageFinishTime_;
			// the waiting time is inverted to represent more like a frequency
			// the values it translates to are the following:
			// 0ms -> 100
			// 1ms ->  66
			// 2ms ->  50
			// 4ms ->  33
			// 6ms ->  25
			// 8ms ->  20
			//18ms ->  10
			//20ms ->   9
			//23ms ->   8
			//26.5ms -> 7
			//31ms ->   6
			//38ms ->   5
			mrtgProbeJMSLoad_.addMeasurement((long) (100.0/((waitingTime/2.0)+1.0)));
			lastOnMessageFinishTime_ = -1;
		}

		ObjectMessage om = (ObjectMessage) message;
		try {
			// unpack
			JMSWrapper jmsWrapper = (JMSWrapper) om.getObject();
			Integer nodeId = jmsWrapper.getNodeId();			
			MultiUserEvent event = jmsWrapper.getMultiUserEvent();
			OLATResourceable ores = jmsWrapper.getOres();
			boolean fromSameNode = clusterConfig.getNodeId().equals(nodeId);

			String recMsg = "received msg: "+(fromSameNode? "[same node]":"")+" from node:" + 
			nodeId + ", olat-id:" + jmsWrapper.getMsgId() + ", ores:" + ores.getResourceableTypeName() + ":" + ores.getResourceableId() +
			", event:"+event+"}";

			// stats
			final long jmsTimestamp = om.getJMSTimestamp();
			if (jmsTimestamp!=0) {
				final long deliveryTime = receiveTime - jmsTimestamp;
				if (deliveryTime>1500) {
					// then issue a log statement
					log.warn("message received with long delivery time (longer than 1500ms: {}): {}", deliveryTime, recMsg);
				}
				mrtgProbeJMSDeliveryTime_.addMeasurement(deliveryTime);
			}
			
			addToReceivedScreen(recMsg);
			if (log.isDebugEnabled()) log.debug(recMsg);
			
			// message with destination and source both having this vm are ignored here, since they were already 
			// "inline routed" when having been sent (direct call within the vm).
			// distribute the unmarshalled event to all JVM wide listeners for this channel.
			doFire(event, ores);
			
			// stats
			final long doneTime = System.currentTimeMillis();
			final long processingTime = doneTime - receiveTime;
			if (processingTime>500) {
				// then issue a log statement
				log.warn("message received with long processing time (longer than 500ms: {}): {}", processingTime, recMsg);
			}
			mrtgProbeJMSProcessingTime_.addMeasurement(processingTime);
		} catch (Error er) {
			log.error("Uncaught Error in ClusterEventBus.onMessage!", er);
			throw er;
		} catch (RuntimeException re) {
			log.error("Uncaught RuntimeException in ClusterEventBus.onMessage!", re);
			throw re;
		} catch (JMSException e) {
			log.warn("JMSException in ClusterEventBus.onMessage", e);
			throw new OLATRuntimeException("error when receiving jms messages", e);
		} catch(Throwable th) {
			log.error("Uncaught Throwable in ClusterEventBus.onMessage!", th);
		} finally {
			lastOnMessageFinishTime_ = System.currentTimeMillis();
		}
	}

	private NodeInfo getNodeInfoFor(Integer nodeId) {
		synchronized (nodeInfos) {//cluster_ok node info is per vm only
			NodeInfo f = nodeInfos.get(nodeId);
			if (f == null) {
				f = new NodeInfo(nodeId);
				nodeInfos.put(nodeId, f);
			}
			return f;
		}
	}

	/**
	 * [used by spring]
	 */
	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}

	/**
	 * [used by spring to auto export mbean data]
	 * 
	 * @return the number of sent cluster event bus message since startup of
	 *         this java vm
	 */
	public long getNumOfSentMessages() {
		return numOfSentMessages;
	}

	/**
	 * [used by spring to auto export mbean data]
	 * 
	 * @return the id of the latest msg sent from this cluster
	 */
	public long getLatestSentMsgId() {
		return latestSentMsgId;
	}
	
	Map<Integer, NodeInfo> getNodeInfos() {
		return nodeInfos;
	}
	
	List<PerfItem> getPerfItems() {
		List<PerfItem> l = new ArrayList<>(2);
		l.add(new PerfItem("Cluster Events Sent", -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, msgsSentCount));
		l.add(new PerfItem("Cluster Events Received",  -1, -1, 1, -1, -1, -1, -1,  -1, -1, -1, -1, msgsReceivedCount));
		return l;
	}
	
	void resetStats() {
		msgsSentCount = 0;
		msgsReceivedCount = 0;
	}
	
	private void addToSentScreen(String msg) {
		synchronized (msgsSent) {//cluster_ok is per vm only
			msgsSentCount++;
			msgsSent.add(msg);
			if (msgsSent.size() > maxListSize) {
				msgsSent.remove(0);
			}
		}
	}

	private void addToReceivedScreen(String msg) {
		synchronized (msgsReceived) {//cluster_ok is per vm only
			msgsReceivedCount++;
			msgsReceived.add(msg);
			if (msgsReceived.size() > maxListSize) {
				msgsReceived.remove(0);
			}
		}
	}
	
	public String getBusInfosAsString() {
		return busInfos.getAsString();
	}
	
	/**
	 * @return the copied list of the latest "maxListSize" received messages (copied so that iterating is failsafe)
	 */
	List<String> getListOfReceivedMsgs() {
		synchronized(msgsReceived) {//cluster_ok is per vm only
			return new ArrayList<>(msgsReceived);
		}
	}
	
	/**
	 * @return the copied list of the latest "maxListSize" sent messages (copied so that iterating is failsafe)
	 */
	List<String> getListOfSentMsgs() {
		synchronized(msgsSent) {//cluster_ok is per vm only
			return new ArrayList<>(msgsSent);
		}
	}

	/**
	 * [used by spring]
	 */
	public void stop() {
		log.info("ClusterEventBus: Set stop flag for ClusterInfoEvent-Thread.");
		isClusterInfoEventThreadRunning = false;
		try {
			jmsExecutor.shutdownNow();
			sessionProducer.close();
			sessionConsumer.close();
			connection.close();
			log.info("ClusterEventBus stopped");
		} catch (JMSException e) {
			log.warn("Exception in stop ClusteredSearchProvider, ",e);
		}
	}

	public ClusterConfig getClusterConfig() {
		return clusterConfig;
	}

	/**
	 * [used by spring]
	 */
	public void setSendInterval(long sendInterval) {
		this.sendInterval = sendInterval;
	}

	/**
	 * [used by spring]
	 */
	public void setJmsMsgDelayLimit(long jmsMsgDelayLimit) {
		this.jmsMsgDelayLimit = jmsMsgDelayLimit;
	}

	/**
	 * [used by spring]
	 */
	public void setConnectionFactory(ConnectionFactory conFac) {
		this.connectionFactory = conFac;
	}

	public void setDestination(Topic destination) {
		this.destination = destination;
	}

}
