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

package org.olat.commons.calendar;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.calendar.model.ICalToken;
import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.group.BusinessGroupService;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;


/**
 * Description:<BR>
 * Constants and helper methods for the OLAT iCal feeds
 * 
 * <P>
 * Initial Date:  June 2, 2008
 *
 * @author Udit Sajjanhar
 */
public class ICalTokenGenerator {
	
	private static final OLog log = Tracing.createLoggerFor(ICalTokenGenerator.class);

	/** Authentication provider name for iCal authentication **/
	public static final String ICAL_AUTH_PROVIDER = "ICAL-OLAT";
	/** Key under which the users iCal token is beeing kept in the http session **/
	public static final String ICAL_AUTH_TOKEN_KEY = "icaltoken";
	
	/** OLAT server URL **/
	public static final String URI_SERVER = Settings.getServerContextPathURI() + "/";
	/** path prefix for personal iCal feed **/
	public static final String ICAL_PREFIX_PERSONAL = "/user/";
	/** path prefix for course iCal feed **/
	public static final String ICAL_PREFIX_COURSE = "/course/";
	/** path prefix for group iCal feed **/
	public static final String ICAL_PREFIX_GROUP = "/group/";

	public static final int ICAL_PATH_SHIFT = 1;
	/** Expected number of tokens in the course/group calendar link **/
	public static final int ICAL_PATH_TOKEN_LENGTH = 4;
	/** Expected number of tokens in the personal calendar link **/
	public static final int ICAL_PERSONAL_PATH_TOKEN_LENGTH = ICAL_PATH_TOKEN_LENGTH - 1;

	
  /** collection of iCal feed prefixs **/
  public static final String[] ICAL_PREFIX_COLLECTION = {ICAL_PREFIX_PERSONAL,
                                                         ICAL_PREFIX_COURSE,
                                                         ICAL_PREFIX_GROUP};
  
  /** category for the iCal AUTH_TOKEN property **/
  public static final String PROP_CAT_ICALTOKEN = "icalAuthToken";
  /** name for the iCal AUTH_TOKEN property **/
  public static final String PROP_NAME_ICALTOKEN = "authToken";
  
  private static String createIcalAuthToken(OLATResourceable resourceable, Identity identity) {
  	// generate the random alpha numeric token
  	String token = RandomStringUtils.randomAlphanumeric(6);
  	
  	// save token as a property of resourceable
  	NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
  	Property tokenProperty = npm.createPropertyInstance(identity, null,
  			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN, null, null, token, null );
  	npm.saveProperty(tokenProperty);
  	
  	//return the token generated
  	return token;
  }
  
  private static String createIcalAuthToken(Identity identity) {
  	// generate the random alpha numeric token
  	String token = RandomStringUtils.randomAlphabetic(6);
  	
   	// save token as a property of resourceable
  	PropertyManager pm = PropertyManager.getInstance();
  	Property tokenProperty = pm.createPropertyInstance(identity, null,
  			null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN, null, null, token, null );
  	pm.saveProperty(tokenProperty);
  	
  	//return the generated token
  	return token;
  }
  
  	public static List<ICalToken> getICalAuthTokens(Identity identity) {
  		PropertyManager pm = PropertyManager.getInstance();
  		List<Property> tokenProperties = pm.findAllUserProperties(identity, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  		List<ICalToken> tokens = new ArrayList<>();
  		for(Property tokenProperty:tokenProperties) {
  			Long resourceId = tokenProperty.getResourceTypeId();
  			String resourceName = tokenProperty.getResourceTypeName();
  			String value = tokenProperty.getStringValue();
  			
  			String type;
  			if(resourceId == null) {
  				type = CalendarManager.TYPE_USER;
  				resourceId = identity.getKey();
  			} else if("CourseModule".equals(resourceName)) {
  				type = CalendarManager.TYPE_COURSE;
  			} else if("BusinessGroup".equals(resourceName)) {
  				type = CalendarManager.TYPE_GROUP;
  			} else {
  				continue;
  			}
  			tokens.add(new ICalToken(type, value, resourceId));
  		}
  		return tokens;
	}
  
  private static String getIcalAuthToken(OLATResourceable resourceable, Identity identity, boolean create) {
  	// find the property for the resourceable
  	NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
  	Property tokenProperty = npm.findProperty(identity, null, 
  			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  	
  	String token;	
  	if (tokenProperty == null && create) {
  		token = createIcalAuthToken(resourceable, identity);
  	} else {
  		token = tokenProperty.getStringValue();
  	}
  	
  	// return the string value for the property 
  	return token;
  }
  
  private static String getIcalAuthToken(Identity identity, boolean create) {
  	// find the property for the identity
  	PropertyManager pm = PropertyManager.getInstance();
  	Property tokenProperty = pm.findProperty(identity, null, null, 
  			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  	
  	String token;
  	if (tokenProperty == null && create) {
  		token = createIcalAuthToken(identity);
  	} else {
  		token = tokenProperty.getStringValue();
  	}
  	
  	// return the string value for the property 
  	return token;
  }
  
  private static String regenerateIcalAuthToken(OLATResourceable resourceable, Identity identity) {
  	// find the property for the resourceable
  	NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
  	Property tokenProperty = npm.findProperty(identity, null, 
  			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  	
  	//genearate the new token
  	String authToken = RandomStringUtils.randomAlphanumeric(6);
  	
  	//set new auth token as the string value of the property
  	tokenProperty.setStringValue(authToken);
  	
  	// update the property
  	npm.updateProperty(tokenProperty);
  	
  	//return the new token
  	return authToken;
  }
  
  private static String regenerateIcalAuthToken(Identity identity) {
  	// find the property for the identity
  	PropertyManager pm = PropertyManager.getInstance();
  	Property tokenProperty = pm.findProperty(identity, null, null, 
  			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  	
  	//genearate the new token
  	String authToken = RandomStringUtils.randomAlphanumeric(6);
  	
  	//set new auth token as the string value of the property
  	tokenProperty.setStringValue(authToken);
  	
  	// update the property
  	pm.updateProperty(tokenProperty);
  	
  	//return the new token
  	return authToken;
  }
  
  public static void destroyIcalAuthToken(String calendarType, String calendarID, Identity identity) {
  	if (!calendarType.equals(ICalFileCalendarManager.TYPE_USER)) {
	  	// find the property for the resourceable
	  	OLATResourceable resourceable = getResourceable(calendarType, calendarID);
	  	NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
	  	Property tokenProperty = npm.findProperty(identity, null, 
	  			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
	  	if (tokenProperty != null) {
	  		npm.deleteProperty(tokenProperty);
	  	}
  	} else {
    	PropertyManager pm = PropertyManager.getInstance();
    	Property tokenProperty = pm.findProperty(identity, null, null, 
    			PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
    	if (tokenProperty != null) {
    		pm.deleteProperty(tokenProperty);
    	}
  	}
  }
  
  private static Identity getIdentity (String userName) {
		Identity identity = BaseSecurityManager.getInstance().findIdentityByName(userName);
		if (identity == null) {
			// error - abort
			log.error("Identity not found for the username: " + userName);
		} 
		return identity;
  }
  
  private static OLATResourceable getResourceable(String calendarType, String calendarID) {
  	OLATResourceable resourceable ;
  	

		if (calendarType.equals(ICalFileCalendarManager.TYPE_GROUP)) {
		 	// get the group
		 	resourceable = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(new Long(calendarID));
		 	if (resourceable == null) {
		 		// error
		 		log.error("Group not found for the Resourceableid: " + calendarID);
		 		return null;
		 	}
		} else if ((calendarType.equals(ICalFileCalendarManager.TYPE_COURSE))) {
			try {
		 		// get the course
		 		resourceable = CourseFactory.loadCourse(new Long(Long.parseLong(calendarID)));
		 	} catch (Exception e) {
		 		log.error("Course not found for the Resourceableid: " + calendarID);
		 		return null;
		 	}
		} else {
			// error - abort
			log.error("Unmatching Calendar Type: " + calendarType);
			return null;
		} 
  
  	return resourceable;
  }
  
  private static String constructIcalFeedPath(String calendarType, String userName, String authToken, String calendarID) {
  	if (calendarType.equals(ICalFileCalendarManager.TYPE_USER)) {
  		return URI_SERVER + "ical" + "/" + calendarType + "/" + userName + "/" + authToken + ".ics"; 		
  	} else {
  		return URI_SERVER + "ical" + "/" + calendarType + "/" + userName + "/" + authToken + "/" + calendarID + ".ics";
  	}  
  }
  
  /**
   * returns the authentication token for the calendar type and calendar id.
   * authentication token is stored as a property.
   * @param calendarType
   * @param calendarID
   * @param userName
   * @param createToken create a new token if it doesn't exist
   * @return authentication token
   */
  public static String getIcalAuthToken(String calendarType, String calendarID, String userName, boolean createToken) {
		
  	// get the identity of the user
  	Identity identity = getIdentity(userName);
  	if (identity == null) {
  		return null;
  	}
  	
  	return getIcalAuthToken(calendarType, calendarID, identity, createToken);
  }
  
  /**
   * returns the authentication token for the calendar type and calendar id.
   * authentication token is stored as a property.
   * @param calendarType
   * @param calendarID
   * @param identity
   * @param createToken createToken create a new token if it doesn't exist
   * @return authentication token
   */
  public static String getIcalAuthToken(String calendarType, String calendarID, Identity identity, boolean createToken) {
  	
  	if (!calendarType.equals(ICalFileCalendarManager.TYPE_USER)) {
  		// get the resourceable
  		OLATResourceable resourceable = getResourceable(calendarType, calendarID);
  		if (resourceable == null) {
  			return null;
  		}
  		return getIcalAuthToken(resourceable, identity, createToken);
  	} else {
  		return getIcalAuthToken(identity, createToken);
  	}
  }
  
  /**
   * regenerates the authentication token for the calendar type and calendar id.
   * returns the generated token
   * @param calendarType
   * @param calendarID
   * @param identity
   * @return authentication token
   */
	public static FeedLink regenerateIcalAuthToken(String calendarType, String calendarID, Identity identity) {
		String authToken;
	  	if (!calendarType.equals(ICalFileCalendarManager.TYPE_USER)) {
	  		// get the resourceable
	  		OLATResourceable resourceable = getResourceable(calendarType, calendarID);
	  		if (resourceable == null) {
	  			return null;
	  		}
	  		authToken = regenerateIcalAuthToken(resourceable, identity);
	  	} else {
	  		authToken = regenerateIcalAuthToken(identity);
	  	}
	  	String path = constructIcalFeedPath(calendarType, identity.getName(), authToken, calendarID);
	  	return new FeedLink(authToken, path);
	}
  
  /**
   * return the ical feed link for the calendar. 
   * authentication token is created if it doesn't exist already.
   * @param calendarType
   * @param calendarID
   * @param identity
   * @return
   */
	public static FeedLink getIcalFeedLink(String calendarType, String calendarID, Identity identity) {
	  	String authToken = getIcalAuthToken(calendarType, calendarID, identity, true);
	  	String path = constructIcalFeedPath(calendarType, identity.getName(), authToken, calendarID);
	  	return new FeedLink(authToken, path);
  }
  
  /**
   * Check if iCalFeedLink exist 
   * @param calendarType
   * @param calendarID
   * @param identity
   * @return
   */
	public static boolean existIcalFeedLink(String calendarType, String calendarID, Identity identity) {
  	Property tokenProperty = null;
  	if (!calendarType.equals(ICalFileCalendarManager.TYPE_USER)) {
	  	// find the property for the resourceable
	  	OLATResourceable resourceable = getResourceable(calendarType, calendarID);
	  	NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(resourceable);
	  	tokenProperty = npm.findProperty(identity, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  	} else {
    	PropertyManager pm = PropertyManager.getInstance();
    	tokenProperty = pm.findProperty(identity, null, null, PROP_CAT_ICALTOKEN, PROP_NAME_ICALTOKEN);
  	}
  	return tokenProperty != null;
	}
	
	public static class FeedLink {
		
		private final String token;
		private final String link;
		
		public FeedLink(String token, String link) {
			this.token = token;
			this.link = link;
		}
		
		public String getToken() {
			return token;
		}
		
		public String getLink() {
			return link;
		}
	}
}
