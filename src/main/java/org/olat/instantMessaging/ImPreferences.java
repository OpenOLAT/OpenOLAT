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

package org.olat.instantMessaging;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.instantMessaging.ui.ChangeIMSettingsController;
import org.olat.properties.Property;

/**
 * Description:<br />
 * The Im preferences object is used to store some settings about Instant Messaging.
 * The Im preferences are stored in the database as a user property
 * <P>
 * Initial Date:  08. august 2005 <br />
 *
 * @author Alexander Schneider
 */
public class ImPreferences {
	static final String USER_PROPERTY_KEY = "impreferences";
	/**	 */
	transient private final static String KEY_VISIBILITY = "VISIBILITY";
	/**	 */
	transient private final static String KEY_DISPLAYONLINETIME = "ONLINETIME";
	/**	 */
	transient private final static String KEY_DISPLAY_AWARENESS = "AWARENESS";
	/**	 */
	transient private final static String KEY_DEFAULTSTATUS = "DEFAULTSTATUS";
	
	// keys: prefs-keys; values: any Prefs-Objects
	private Map<String, Object> prefstore = new HashMap<String, Object>();
	// simply to indicate preferences version
	private int version = 1;
	
	transient Identity owner;
	//when read from the db the property object gets saved here for later reference
	transient Property dbProperty = null;
	/**
	 * @param owner
	 */
	ImPreferences(Identity owner) {
		this.owner = owner;
	}
	
	/**
	 * 
	 * @return boolean true if this user is visible on the onlinelist to other users
	 */
	public boolean isVisibleToOthers(){
    Boolean b = (Boolean) prefstore.get(ChangeIMSettingsController.class.getName() + "::"+KEY_VISIBILITY);
    return b.booleanValue();
	}
	/**
	 * @param isVisible
	 */
	public void setVisibleToOthers(boolean isVisible){
		prefstore.put(ChangeIMSettingsController.class.getName()+"::"+ImPreferences.KEY_VISIBILITY, isVisible ? Boolean.TRUE : Boolean.FALSE);
	}
	
	/**
	 * 
	 * @return boolean isOnlineVisible
	 */
	public boolean isOnlineTimeVisible(){
	    Boolean b = (Boolean) prefstore.get(ChangeIMSettingsController.class.getName() + "::"+KEY_DISPLAYONLINETIME);
	    return b.booleanValue();
	}
	/**
	 * 
	 * @param isVisible
	 */
	public void setOnlineTimeVisible(boolean isVisible){
		prefstore.put(ChangeIMSettingsController.class.getName()+"::"+KEY_DISPLAYONLINETIME,  isVisible ? Boolean.TRUE : Boolean.FALSE);
	}
	
	/**
	 * 
	 * @return boolean isCourseNameVisible
	 */
	public boolean isAwarenessVisible(){
	    Boolean b = (Boolean) prefstore.get(ChangeIMSettingsController.class.getName() + "::"+KEY_DISPLAY_AWARENESS);
	    return b.booleanValue();
	}
	/**
	 * 
	 * @param isVisible
	 */
	public void setAwarenessVisible(boolean isVisible){
		prefstore.put(ChangeIMSettingsController.class.getName()+"::"+KEY_DISPLAY_AWARENESS,isVisible ? Boolean.TRUE : Boolean.FALSE);
	}
	
	/**
	 * 
	 * @return the default status 
	 */
	public String getRosterDefaultStatus(){
		return (String)prefstore.get(ChangeIMSettingsController.class.getName() + "::"+KEY_DEFAULTSTATUS);
	}
	
	public void setRosterDefaultStatus(String defaultStatus){
		prefstore.put(ChangeIMSettingsController.class.getName()+"::"+KEY_DEFAULTSTATUS, defaultStatus);
	}

	protected Property getDbProperty() {
		return dbProperty;
	}
	
}

