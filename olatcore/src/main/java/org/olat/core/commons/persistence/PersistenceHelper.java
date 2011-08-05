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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.persistence;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.olat.core.id.Persistable;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;

/**
 * Description:<BR>
 * Helper methods to work with persistable objects
 * <P>
 * Initial Date:  Nov 30, 2004
 *
 * @author gnaegi 
 */
public class PersistenceHelper {
	private static boolean charCountNativeUTF8 = true;
	
	/**
	 * Spring only constructor. Use static methods to access the helper
	 * @param firstKeyValues init properties from spring
	 */
	public PersistenceHelper(Properties firstKeyValues) {
		// OLAT-6439 Init flag to know if the database understands native UTF-8 
		// Only necessary for old MySQL database of UZH
		if (firstKeyValues != null) {
			String dbvendor = firstKeyValues.getProperty("dbvendor");
			String dboptions = firstKeyValues.getProperty("mysqloptions");
			if ("mysql".equals(dbvendor) && dboptions != null && dboptions.contains("characterEncoding=UTF-8") && dboptions.contains("useOldUTF8Behavior=true")) {
				charCountNativeUTF8 = false; 
			}		
		}
	}

	/**
	 * Truncate the given original string to the defined max length. The method
	 * does also check if some legacy UTF-8 conversion is done by the database
	 * driver and shortens the allowed length to two third to be on the save
	 * side
	 * 
	 * @param original
	 *            The original String
	 * @param maxLength
	 *            The max length allowed by the database schema
	 * @param showThreeDots
	 *            Replace the last three characters with ... to indicate that
	 *            the string has been truncated
	 * @return The truncated string
	 */
	public static String truncateStringDbSave(final String original, int maxLength, final boolean showThreeDots) {
		if (original == null) {
			return null;
		}  
		if (!charCountNativeUTF8) {
			// When using legacy UTF-8 conversion we have actually no idea how
			// long the string can be since this is a hidden information to
			// MySQL. In that case we subtract 1/3 and cross our fingers
			maxLength = maxLength - (maxLength/3);
		}
		// Check if too long
		int length = original.length();
		if (length <= maxLength) {
			return original;
		}
		// 1) Remove all HTML markup first as truncating could lead to invalid HTML code. 
		String result = FilterFactory.getHtmlTagAndDescapingFilter().filter(original);
		if (length <= maxLength) {
			return original;
		}
		// 2) Truncate to maxLength
		if (showThreeDots) {
			result = Formatter.truncate(result, maxLength);			
		} else {
			result = Formatter.truncateOnly(result, maxLength);
		}
		return result;
	}

	/**
	 * 
	 * FIXME:fj:c cleanup up these methods here and class SyncHelper
	 * Checks if the given persistable is in the given iterator (database identity). 
	 * If so, the persistable from the iterator is returned. If not, null is returned. 
	 * @param iter Iterator of persistable objects
	 * @param persistable The persistable object that is looked for in the iterator
	 * @return null if not found or the persistable object that has the same key as the given 
	 * persistable object. The object might also have object identity, but this is not guaranteed. 
	 */
	// TODO:cg: not used => Remove it, ask fj
	public static Persistable getPersistableByPersistableKey(Iterator iter, Persistable persistable) {
		while (iter.hasNext()) {
			Persistable persistableFromIterator = (Persistable) iter.next();
			if (persistable.equalsByPersistableKey(persistableFromIterator)) 
				return persistableFromIterator;
		}
		return null;
	}

	/**
	 * Iterates over a list to see if the given persistable object is already in this list.
	 * This differs from list.contains() in the way that it does not check object identity
	 * but hibernate identity. The object list contains the object if it contains any 
	 * object with object.getKey() equals persistable.getKey()
	 * @param objects List of persistable objects
	 * @param persistable Persistable object
	 * @return boolean
	 */
	public static boolean listContainsObjectByKey(List objects, Persistable persistable) {
		return listContainsObjectByKey(objects, persistable.getKey());
	}

	/**
	 * Iterates over a list to see if there is an object in the list with the given persistable
	 * key that is used by the hibernate layer. 
	 * @param objects List of persistable objects
	 * @param persistable Persistable object
	 * @return boolean
	 */
	public static boolean listContainsObjectByKey(List objects, Long key) {
		for (Iterator iter = objects.iterator(); iter.hasNext();) {
			try {
				Persistable listObject = (Persistable) iter.next();
				if (listObject.getKey().equals(key))  {
					return true;
				}
			} catch (ClassCastException e) {
				throw new AssertionError("Class cast exception: objects list must contain object only of type persistable!");
			}
		}
		return false;
	}

	/**
	 * Returns the position of the object in the list. An object is found when it has
	 * the same hibernate key as the given object (!= java object identity)
	 * @param objects
	 * @param persistable
	 * @return int position of object in list
	 */
	public static int indexOf(List objects, Persistable persistable){
		return indexOf(objects, persistable.getKey());
	}
	
	/**
	 * Returns the position of the object in the list. An object is found when it has
	 * the same hibernate key as the given object (!= java object identity)
	 * @param objects
	 * @param key
	 * @return int position of object in list
	 */
	public static int indexOf(List objects, Long key) {
		for (Iterator iter = objects.iterator(); iter.hasNext();) {
			try {
				Persistable listObject = (Persistable) iter.next();
				if (listObject.getKey().equals(key))  {
					return objects.indexOf(listObject);
				}
			} catch (ClassCastException e) {
				throw new AssertionError("Class cast exception: objects list must contain object only of type persistable!");
			}
		}
		return -1;
	}
	
	/**
	 * Replace an object in the given list that has the same persistance key.
	 * @param objects List ob original objects
	 * @param toBeReplacedObject The object that should be searched for in the list and the 
	 * replace value
	 * @return boolean true: object replaced; false: object was not found in list
	 */
	public static boolean replaceObjectInListByKey(List objects, Persistable toBeReplacedObject) {
		int i = indexOf(objects, toBeReplacedObject);
		// return false when object was not found in list
		if (i < 0) return false;
		// otherwhise replace the object and return true
		objects.remove(i);
		objects.add(i, toBeReplacedObject);
		return true;
	}

	/**
	 * Removes a list of persistable objects from another list with
	 * persistable objects by comparing the hibernate key instead of the
	 * object identity.
	 * @param originalList
	 * @param toBeRemovedObjects
	 * @return int number of objects removed from the originalList
	 * After calling this operation the originalList will contain less or the same amount of
	 * objects
	 */
	public static int removeObjectsFromList(List originalList, List toBeRemovedObjects) {	
		int counter = 0;
		Iterator removeIter = toBeRemovedObjects.iterator();
		while (removeIter.hasNext()) {
			Persistable toBeRemoved = (Persistable) removeIter.next();
			Iterator originalIter = originalList.iterator();
			while (originalIter.hasNext()) {
				Persistable fromOriginal = (Persistable) originalIter.next();
				if (fromOriginal.getKey().equals(toBeRemoved.getKey())) {
					originalList.remove(fromOriginal);
					counter++;
					break;
				}
			}
		}
		return counter;
	}
}
