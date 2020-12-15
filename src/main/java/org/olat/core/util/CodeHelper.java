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
* <p>
*/ 

package org.olat.core.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


/**
 * enclosing_type Description:<br>
 *
 * @author Felix Jost
 * @author Guido Schnider
 */
public class CodeHelper {
  //o_clusterNOK: Review: Done by cg (add factor 1000000000000), What happens in clustermode, same ID on different VM ?
	// with factor 1000000000000 each node can use 0-999999999999 unique ram id's 
	private static AtomicLong ramid = new AtomicLong( 1000000000 );
	private static final AtomicLong timeuniqueId = new AtomicLong(System.currentTimeMillis() * 64);
	private static Integer nodeId;
	
	private CodeHelper(Integer nodeId) {
		CodeHelper.nodeId = nodeId;
		ramid = new AtomicLong( ((long)nodeId) * 1000000000 );
	}
	
	/**
	 * Generates a virtually global unique ID based on the
	 * forever unique ID (see getForeverUniqueID()) and a
	 * user defined namespace (see OLATContext.instanceId).
	 * This ID has a maximum length of 30 characters
	 * (10 chr instanceID + 20 chr timeuniqueID)
	 * 
	 * @return a virtually global unique ID
	 */
	public static String getGlobalForeverUniqueID() {
		return WebappHelper.getInstanceId()+ "_" + nodeId + "_" + getForeverUniqueID();
	}
	
	/**
	 * PLEASE use only if REALLY needed.<br>
	 * 
	 * Best effort is taken to make it "globally unique" without any persisting media by instantiating it by System.currentTimeMilis * 64, so that
	 * after a restart of the vm, the counter advances 64000 units per second which should be enough that that value is
	 * never exceeded by the usage of that ID (100 concurrent users which can consume 640 unique id per each second, and: even if exceeded, after a restart of a vm
	 * (assumed time at least 10secs), a loss of 10*64000 = 640000 can be caught up
	 * <br>
	 * <br>
	 * if you just need a counter which is unique within the virtual machine, 
	 * but does not need to be unique if 
	 * the sessions are persisted and the vm is restarted, then use @see getRAMUniqueID()
	 * 
	 * returns a unique id; even if the system is restarted.
	 * 
	 * @return long
	 */
	public static long getForeverUniqueID() {
		return timeuniqueId.incrementAndGet(); //o_clusterNOK synchronized check what of data generated with that long is persisted
	}
	
	public static long getUniqueIDFromString(String base){
		return Math.abs(base.hashCode());
	}
	
	/**
	 * a simple counter which is garanteed to be unique ONLY within one instance of a virtual machine.
	 * Best effort is taken to make it "globally unique" by instantiating it by System.currentTimeMilis * 64, so that
	 * after a restart of the vm, the counter advances 64000 units per second which should be enough that that value is
	 * never exceeded by the usage of that ID
	 * 
	 * @return RAM unique ID
	 */
	public static long getRAMUniqueID() {
		return ramid.incrementAndGet();
	}
	
	public static String getUniqueID() {
		return UUID.randomUUID().toString();
	}
	
	public static long nanoToMilliTime(long start) {
		long end = System.nanoTime();
		return (end - start) / 1000000l;
	}
	
	public static long nanoToSecond(long start) {
		long end = System.nanoTime();
		return (end - start) / 1000000000l;
	}
	
	public static void printMilliSecondTime(long nanoStart, String action) {
		long end = System.nanoTime();
		long takes = (end - nanoStart) / 1000000l;
		System.out.println(action + " takes (ms): " + takes);
	}
	
	public static void printMicroSecondTime(long nanoStart, String action) {
		long end = System.nanoTime();
		long takes = (end - nanoStart) / 1000l;
		System.out.println(action + " takes (\u00B5s): " + takes);
	}
}
