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
package org.olat.course.statistic.export;

import static java.util.Locale.ENGLISH;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;

/**
 * Converter class to take a LoggingObject and convert it into a (csv) line
 * <p>
 * Primarily a helper class for the ICourseLogExporter implementations.
 * <P>
 * Initial Date:  06.01.2010 <br>
 * @author Stefan
 */
public class LogLineConverter {
	
	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(LogLineConverter.class);
	
	/** spring property defining all properties - including the order in which they will be exported **/
	private List<String> orderedExportedProperties = new ArrayList<String>();
	
	/** spring property defining all properties which should be anonymized - they must also be in orderedExportedProperties **/
	private Set<String> anonymizedProperties = new HashSet<String>();

	/** internal property which contains (javax.bean) PropertyDescriptors of each of the above property -
	 * given the properties are available
	 */
	private List<PropertyDescriptor> orderedExportedPropertyDescriptors = new ArrayList<PropertyDescriptor>();
	
	/**
	 * spring property setter for orderedExportedProperties - which is the list of all properties to be extracted
	 * from the LoggingObject and exported in the csv format
	 * @param orderedExportedProperties
	 */
	public void setOrderedExportedProperties(List<String> orderedExportedProperties) {
		this.orderedExportedProperties = orderedExportedProperties;
		initPropertyDescriptor();
	}
	
	/**
	 * spring property setter for anonymizedProperties - all of these must also be in the orderedExportedProperties list
	 */
	public void setAnonymizedProperties(Set<String> anonymizedProperties) {
		this.anonymizedProperties = anonymizedProperties;
	}

  /**
   * Returns a String which capitalizes the first letter of the string.
   * (c) from java.beans.NameGenerator (which unfortunatelly is not a public class)
   */
  public static String capitalize(String name) { 
  	if (name == null || name.length() == 0) { 
  		return name; 
    }
  	return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
  }
  
	/**
	 * Initialize the (java.bean) PropertyDescriptors for the properties
	 */
	private void initPropertyDescriptor() {
		for (Iterator<String> it = orderedExportedProperties.iterator(); it.hasNext();) {
			String propertyName = it.next();
			try {
				// we're using this specialized constructor since we want to allow properties that are read-only (such as creationDate).
				// with the simpler constructor (String,Class) that would result in an Exception.
				// also note that we're using "is"+propertyName rather than get - that's the way the PropertyDescriptor itself
				// does it in the constructor (String,Class) - resulting in a lookup of an is Method first and then the get Method
				// this seems to be the correct standard here.
				PropertyDescriptor pd = new PropertyDescriptor(propertyName, LoggingObject.class, "is" + capitalize(propertyName), null);
				orderedExportedPropertyDescriptors.add(pd);
			} catch (IntrospectionException e) {
				log_.error("initPropertyDescriptor: Could not retrieve property "+propertyName+" from LoggingObject, configuration error?", e);
			}
		}
	}

	/**
	 * Returns the CSV Header line containing all property names in the exact same way as in the config file -
	 * excluding those properties which could not be retrieved, i.e. for which no PropertyDescriptor could be created
	 * @return the CSV Header line containing all property names in the exact same way as in the config file -
	 * excluding those properties which could not be retrieved, i.e. for which no PropertyDescriptor could be created
	 */
	public String getCSVHeader() {
		List<String> propertyNames = new ArrayList<String>();
		for (Iterator<PropertyDescriptor> it = orderedExportedPropertyDescriptors.iterator(); it.hasNext();) {
			PropertyDescriptor pd = it.next();
			propertyNames.add(pd.getName());
		}
		return StringHelper.formatAsEscapedCSVString(propertyNames);
	}

	/**
	 * Returns a CSV line for the given LoggingObject - containing all properties in the exact same way as in the 
	 * config file - excluding those which could not be retrieved, i.e. for which no PropertyDescriptor could be 
	 * created
	 * @param loggingObject the LoggingObject for which a CSV line should be created
	 * @return the CSV line representing the given LoggingObject
	 */
	public String getCSVRow(LoggingObject loggingObject, boolean anonymize, Long resourceableId) {
		List<String> loggingObjectList = new ArrayList<String>();
		for (Iterator<PropertyDescriptor> it = orderedExportedPropertyDescriptors.iterator(); it.hasNext();) {
			PropertyDescriptor pd = it.next();
			
			String strValue = "";
			try {
				Object value = pd.getReadMethod().invoke(loggingObject, (Object[])null);
				if (value!=null) {
					strValue = String.valueOf(value);
				}
				if (anonymize && anonymizedProperties.contains(pd.getName())) {
					// do anonymization
					strValue = makeAnonymous(String.valueOf(value), resourceableId);
				}
			} catch (IllegalArgumentException e) {
				// nothing to do
			} catch (IllegalAccessException e) {
				// nothing to do
			} catch (InvocationTargetException e) {
				// nothing to do
			}
			loggingObjectList.add(strValue);
		}
		
		return StringHelper.formatAsEscapedCSVString(loggingObjectList);
	}
	
	/**
	 * encode a string and course resourcable id with MD5
	 * @param s
	 * @param courseResId
	 * @return
	 */
	private String makeAnonymous(String s, Long courseResId) {
		String encodeValue = s + "-" + Long.toString(courseResId);
		// encode with MD5
		return Encoder.md5hash(encodeValue);
	}

}
