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

package org.olat.user;

import java.util.Hashtable;
import java.util.Map;

import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * The HomePageConfig class represents a configuration for a Homepage. It is
 * persisted as an xml file in the ???. Loading and saving of
 * configuration files is managed through the HomePageConfigManagerImpl.
 * <p>
 * A HomePageConfig has a class wide <code>CURRENTVERSION</code>, an integer
 * denoting the most actual code version of the HomePageConfig. The attribute
 * <code>version</code> is the version number of an instance.
 * <p>
 * <b><code>CURRENTVERSION vs. version</code> </b>
 * <ul>
 * <li><code>version == CURRENTVERSION</code><br>
 * an up to date HomePageConfig</li>
 * <li><code>version &lt; CURRENTVERSION</code><br>
 * demands for resolving version differences, as the loaded HomePageConfig may
 * contain outdated configuration information</li>
 * </ul>
 * <P>
 * 
 * Initial Date: July 15, 2005 <br>
 * @author Alexander Schneider
 */
public class HomePageConfig implements OLATResourceable{


	transient private final static int CURRENTVERSION = 4;
	
	transient private final static String RESOURCEABLETYPENAME = "HOMEPAGECONFIG";
	
	transient public final static String KEY_RESOURCEABLEID = "RESOURCEABLEID";
	
	transient public final static String KEY_USERNAME = "USERNAME";

	transient public static final String KEY_TEXTABOUTME = "TEXTABOUTME";
	
	// use the user property names defined in UserConstants instead!
	@Deprecated
	transient public final static String KEY_EMAIL = "EMAIL";
	@Deprecated
	transient public final static String KEY_GENDER = "GENDER";
	@Deprecated
	transient public final static String KEY_BIRTHDAY = "BIRTHDAY";
	@Deprecated
	transient public final static String KEY_TELMOBILE = "TELMOBILE";
	@Deprecated
	transient public static final String KEY_TELPRIVATE = "TELPRIVATE";
	@Deprecated
	transient public static final String KEY_TELOFFICE = "TELOFFICE";
	@Deprecated
	transient public static final String KEY_ADDRESS = "ADDRESS";
	@Deprecated
	transient public static final String KEY_INSTITIUTIONALNAME = "INSTITUTIONALNAME";
	@Deprecated
	transient public static final String KEY_INSTITUTIONEMAIL = "INSTITUTIONEMAIL";

	/**
	 * config file version from file
	 */
	private int version = -1;
	
	/**
	 * holds the configuration
	 */
	private Map<String,Object> configuration = new Hashtable<>();

	public HomePageConfig() {
	// empty, for XSTream
	}

	/**
	 * @return version of this loaded/created instance
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * initialize with default values
	 */

	public void initDefaults() {
		// version 1
		// removed, see version 4 comment
		
		// version 2
		configuration.put(KEY_USERNAME, "");
		
		// version 3
		configuration.put(KEY_RESOURCEABLEID, "");

		// version 4
		// no default values for flags, they are set in the user preferences
		// configuration. See olat_userconfig.xml in the spring config dir
		
		this.version = CURRENTVERSION;
	}

	/**
	 * resolve issues of changed configuration version. Such as:
	 * <ul>
	 * <li>add new default values <br>
	 * <b>&gt;&gt;add the them </b></li>
	 * <li>no longer exisiting key value pairs <br>
	 * <b>&gt;&gt;remove from configuration </b></li>
	 * <li>changing of value meanings/types <br>
	 * <b>&gt;&gt;convert the existing entries </b></li>
	 * </ul>
	 */
	public void resolveVersionIssues() {
		if (version < CURRENTVERSION) {
			// from version 1 -> 2
			if (version == 1) {
			    configuration.put(KEY_USERNAME, "");
				this.version = 2;
			}
			if (version == 2){
				configuration.put(KEY_RESOURCEABLEID, "");
			    this.version = 3;
			}
			if (version == 3){
				configuration.put(KEY_RESOURCEABLEID, "");
			    this.version = 4;
			    // migrate values to new style
			    setEnabled(UserConstants.EMAIL, isEnabled("EMAIL"));
			    configuration.remove("EMAIL");
			    setEnabled(UserConstants.GENDER, isEnabled("GENDER"));
			    configuration.remove("GENDER");
			    setEnabled(UserConstants.BIRTHDAY, isEnabled("BIRTHDAY"));
			    configuration.remove("BIRTHDAY");
			    setEnabled(UserConstants.TELMOBILE, isEnabled("TELMOBILE"));
			    configuration.remove("TELMOBILE");
			    setEnabled(UserConstants.TELOFFICE, isEnabled("TELOFFICE"));
			    configuration.remove("TELOFFICE");
			    setEnabled(UserConstants.TELPRIVATE, isEnabled("TELPRIVATE"));
			    configuration.remove("TELPRIVATE");
			    setEnabled(UserConstants.INSTITUTIONALNAME, isEnabled("INSTITUTIONALNAME"));
			    configuration.remove("INSTITUTIONALNAME");
			    setEnabled(UserConstants.INSTITUTIONALEMAIL, isEnabled("INSTITUTIONALEMAIL"));
			    configuration.remove("INSTITUTIONALEMAIL");
			    boolean addressEnabled = isEnabled("ADDRESS");
			    setEnabled(UserConstants.STREET, addressEnabled);
			    setEnabled(UserConstants.EXTENDEDADDRESS, addressEnabled);
			    setEnabled(UserConstants.POBOX, addressEnabled);
			    setEnabled(UserConstants.ZIPCODE, addressEnabled);
			    setEnabled(UserConstants.CITY, addressEnabled);
			    setEnabled(UserConstants.REGION, addressEnabled);
			    setEnabled(UserConstants.COUNTRY, addressEnabled);
			    configuration.remove("ADDRESS");
			}

			/*
			 * after resolving the issues, the version number is merged to the
			 * CURRENTVERSION !! leave this!
			 */
			this.version = CURRENTVERSION;
		} else if (version > CURRENTVERSION) {
			// this is an error
		}
	}
	
	/**
	 * Enable or disable the visibility of a user property
	 * @param propertyName The name of the user property
	 * @param enabled true: enable visibility; false, disable
	 */
	public void setEnabled(String propertyName, boolean enabled) {
		configuration.put(propertyName, new Boolean(enabled));
	}
	
	/**
	 * Check if the visibility of user property is enabled
	 * @param propertyName The name of the user property
	 * @return true: enable visibility; false, disable
	 */
	public boolean isEnabled(String propertyName) {
		Boolean bool = (Boolean) configuration.get(propertyName);
		if (bool == null) return false;
		else return bool.booleanValue();
	}

	/**
	 * 
	 * @param id
	 */
	public void setResourceableId(Long id){
	    configuration.put(KEY_RESOURCEABLEID, id);
	}
	
    /**
     * @return id
     * 
     */
    public Long getResourceableId() {
        Long id = (Long) configuration.get(KEY_RESOURCEABLEID);
        return id;
    }
    
    /**
     * @return typeName
     */
    public String getResourceableTypeName() {
        return RESOURCEABLETYPENAME;
    }
	
	/**
	 * 
	 * @param userName
	 */
	public void setUserName(String userName){
	   configuration.put(KEY_USERNAME, userName); 
	}
	
	/**
	 * @return text about me
	 */
	public String getUserName() {
		String userName = (String) configuration.get(KEY_USERNAME);
		if (userName.equals("")) throw new AssertException("No username defined in " + HomePageConfigManager.HOMEPAGECONFIG_XML);
		return userName;
	}	
	
	/**
	 * 
	 * @param textAboutMe
	 */
	public void setTextAboutMe(String textAboutMe) {
		configuration.put(KEY_TEXTABOUTME, textAboutMe);
	}

	/**
	 * @return text about me
	 */
	public String getTextAboutMe() {
		return (String) configuration.get(KEY_TEXTABOUTME);
	}

    /**
     * @return boolean
     */
    public boolean hasResourceableId() {
        return !"".equals(configuration.get(KEY_RESOURCEABLEID));
    }
	
	
}