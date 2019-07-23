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

package org.olat.core.util.memento;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * Initial Date: Nov 25, 2005 <br>
 * 
 * @author patrick
 */
public abstract class Memento implements Serializable {
	/**
	 * holds the mementos state in a weakly coupled fashion. This helps if
	 * mementos are persisted using xmlstream and then changing the class
	 * structure of a memento implementor.
	 */
	protected Map mementoState = new Hashtable();
	/**
	 * creation time of this memento
	 */
	protected transient static String TIMESTAMP = "timestamp";
	/**
	 * the version of the memento used for migration issues
	 */
	protected transient static String VERSION = "version";
	/**
	 * key for the state
	 */
	protected transient static String STATE = "state";

	/**
	 *
	 */
	public Memento() {
	//
	}

	/**
	 * @return creation time of the memento
	 */
	public long getTimestamp() {
		//
		return ((Long) mementoState.get(TIMESTAMP)).longValue();
	}

	/**
	 * @return
	 */
	protected Object getState() {
		resolveVersionIssues();
		/*
		 * PostCondition: getVersion() ==
		 * ((Integer)mementoState.get(VERSION)).intValue()
		 */
		return mementoState.get(STATE);

	}

	/**
	 * @param state
	 */
	protected void setState(Object state) {
		Long time = new Long(System.currentTimeMillis());
		Integer version = new Integer(getVersion());
		mementoState.put(TIMESTAMP, time);
		mementoState.put(VERSION, version);
		mementoState.put(STATE, state);
	}

	/**
	 * default implemenation
	 * @return
	 */
	abstract protected int getVersion();

	/**
	 * default implementation
	 */
	abstract protected void resolveVersionIssues(); 
}
