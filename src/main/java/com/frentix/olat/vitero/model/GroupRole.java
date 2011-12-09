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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.model;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public enum GroupRole {
	//position is important! 
	participant(0),
	assistant(1),
	teamleader(2),
	audience(3);
	
	private final int vmsValue;
	
	private GroupRole(int val) {
		this.vmsValue = val;
	}

	public int getVmsValue() {
		return vmsValue;
	}
	
	public static GroupRole valueOf(int role) {
		switch(role) {
			case 0: return participant;
			case 1: return assistant;
			case 2: return teamleader;
			case 3: return audience;
			default : return null;
		}
	}
}
