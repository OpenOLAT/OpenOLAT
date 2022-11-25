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

package org.olat.core.commons.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
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
	
	private static final Logger log = Tracing.createLoggerFor(PersistenceHelper.class);
	private static boolean charCountNativeUTF8 = true;
	private static final int DEFAULT_CHUNK_SIZE = 16384;
	
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
	
	public static boolean appendAnd(StringBuilder sb, boolean where) {
		if(where) {
			sb.append(" and ");
		} else {
			sb.append(" where ");
		}
		return true;
	}
	
	public static boolean appendAnd(NativeQueryBuilder sb, boolean where) {
		if(where) {
			sb.append(" and ");
		} else {
			sb.append(" where ");
		}
		return true;
	}
	
	public static final void appendFuzzyLike(Appendable sb, String field, String key, String dbVendor) {
		try {
			if(dbVendor.equals("mysql")) {
				sb.append(" ").append(field).append(" like :").append(key);
			} else {
				sb.append(" lower(").append(field).append(") like :").append(key);
			}
			if(dbVendor.equals("oracle")) {
				sb.append(" escape '\\'");
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public static final void appendFuzzyNotLike(Appendable sb, String field, String key, String dbVendor) {
		try {
			sb.append("(").append(field).append(" is null or ");
			if(dbVendor.equals("mysql")) {
				sb.append(" ").append(field).append(" not like :").append(key);
			} else {
				sb.append(" lower(").append(field).append(") not like :").append(key);
			}
			if(dbVendor.equals("oracle")) {
				sb.append(" escape '\\'");
			}
			sb.append(")");
		} catch (IOException e) {
			log.error("", e);
		}
	}

	
	/**
	 * Helper method that replaces * with % and appends and
	 * prepends % to the string to make fuzzy SQL match when using like.
	 * Use "" to disable this feature and use exact match
	 * @param email
	 * @return fuzzized string
	 */
	public static final String makeFuzzyQueryString(String string) {
		// By default only fuzzy at the end. Usually it makes no sense to do a
		// fuzzy search with % at the beginning, but it makes the query very very
		// slow since it can not use any index and must perform a fulltext search.
		// User can always use * to make it a really fuzzy search query
		if (string.length() > 1 && string.startsWith("\"") && string.endsWith("\"")) {			
			string = string.substring(1, string.length()-1);
		} else {
			if(!string.startsWith("*") && !string.endsWith("*")) {
				string = "%" + string + "%";
			}
			string = string.replace('*', '%');
		}
		// with 'LIKE' the character '_' is a wildcard which matches exactly one character.
		// To test for literal instances of '_', we have to escape it.
		string = string.replace("_", "\\_");
		return string.toLowerCase();
	}
	
	public static String makeEndFuzzyQueryString(String string) {
		// By default only fuzzy at the end. Usually it makes no sense to do a
		// fuzzy search with % at the beginning, but it makes the query very very
		// slow since it can not use any index and must perform a fulltext search.
		// User can always use * to make it a really fuzzy search query
		string = string.replace('*', '%');
		string = string + "%";
		// with 'LIKE' the character '_' is a wildcard which matches exactly one character.
		// To test for literal instances of '_', we have to escape it.
		string = string.replace("_", "\\_");
		return string.toLowerCase();
	}
	

	public static String getOrderByRandom(DB dbInstance) {
		if (dbInstance.isPostgreSQL()){
			return "random()";
		} else if (dbInstance.isMySQL()){
			return "rand()";
		} else if (dbInstance.isOracle()) {
			return "DBMS_RANDOM.VALUE()";
		}
		return "";
	}

	/**
	 * 
	 * Checks if the given persistable is in the given iterator (database identity). 
	 * If so, the persistable from the iterator is returned. If not, null is returned. 
	 * @param iter Iterator of persistable objects
	 * @param persistable The persistable object that is looked for in the iterator
	 * @return null if not found or the persistable object that has the same key as the given 
	 * persistable object. The object might also have object identity, but this is not guaranteed. 
	 */
	public static Persistable getPersistableByPersistableKey(Iterator<? extends Persistable> iter, Persistable persistable) {
		while (iter.hasNext()) {
			Persistable persistableFromIterator = iter.next();
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
	public static boolean listContainsObjectByKey(List<? extends Persistable> objects, Persistable persistable) {
		return listContainsObjectByKey(objects, persistable.getKey());
	}

	/**
	 * Iterates over a list to see if there is an object in the list with the given persistable
	 * key that is used by the hibernate layer. 
	 * @param objects List of persistable objects
	 * @param persistable Persistable object
	 * @return boolean
	 */
	public static boolean listContainsObjectByKey(List<? extends Persistable> objects, Long key) {
		for (Iterator<? extends Persistable> iter = objects.iterator(); iter.hasNext();) {
			try {
				Persistable listObject = iter.next();
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
	public static int indexOf(List<? extends Persistable> objects, Persistable persistable){
		return indexOf(objects, persistable.getKey());
	}
	
	/**
	 * Returns the position of the object in the list. An object is found when it has
	 * the same hibernate key as the given object (!= java object identity)
	 * @param objects
	 * @param key
	 * @return int position of object in list
	 */
	public static int indexOf(List<? extends Persistable> objects, Long key) {
		for (Iterator<? extends Persistable> iter = objects.iterator(); iter.hasNext();) {
			try {
				Persistable listObject = iter.next();
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
	public static boolean replaceObjectInListByKey(List<Persistable> objects, Persistable toBeReplacedObject) {
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
	public static int removeObjectsFromList(List<? extends Persistable> originalList, List<? extends Persistable> toBeRemovedObjects) {	
		int counter = 0;
		Iterator<? extends Persistable> removeIter = toBeRemovedObjects.iterator();
		while (removeIter.hasNext()) {
			Persistable toBeRemoved = removeIter.next();
			Iterator<? extends Persistable> originalIter = originalList.iterator();
			while (originalIter.hasNext()) {
				Persistable fromOriginal = originalIter.next();
				if (fromOriginal.getKey().equals(toBeRemoved.getKey())) {
					originalList.remove(fromOriginal);
					counter++;
					break;
				}
			}
		}
		return counter;
	}
	
	public static Persistable findInListByKey(List<? extends Persistable> persistables, Persistable persistable) {
		Long key = persistable.getKey();
		for (Iterator<? extends Persistable> iter = persistables.iterator(); iter.hasNext();) {
			Persistable ppit  = iter.next();
			if (ppit.getKey().equals(key)) {
				return ppit;
			}
		}
		return null;
	}
	
	/**
	 * @param listOfPersistables
	 * @param persistable
	 * @return True if listOfPersistable contains persistable
	 */
	public static boolean containsPersistable(List<? extends Persistable> listOfPersistables, Persistable persistable) {
		Long key = persistable.getKey();
		for (Iterator<? extends Persistable> iter = listOfPersistables.iterator(); iter.hasNext();) {
			Persistable entry = iter.next();
			if (entry.getKey().equals(key)) {
				return true;
			} 
		}
		return false;
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<Long> toKeys(Collection<? extends Persistable> list) {
		List<Long> keys = new ArrayList<>();
		for(Persistable obj:list) {
			keys.add(obj.getKey());
		}
		return keys;
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<Long> toKeys(Persistable... list) {
		List<Long> keys = new ArrayList<>();
		if(list != null && list.length > 0) {
			for(Persistable obj:list) {
				keys.add(obj.getKey());
			}
		}
		return keys;
	}
	
	public static List<String> toList(String... strings) {
		List<String> stringList = new ArrayList<>();
		if(strings != null && strings.length > 0) {
			for(String string:strings) {
				if(StringHelper.containsNonWhitespace(string)) {
					stringList.add(string);
				}
			}
		}
		return stringList;
	}
	
	public static Long extractLong(Object[] results, int pos) {
		if(results == null || pos >= results.length) return null;
		Object obj = results[pos];
		return obj == null ? null : ((Number)obj).longValue();
	}
	
	public static Double extractDouble(Object[] results, int pos) {
		if(results == null || pos >= results.length) return null;
		Object obj = results[pos];
		return obj == null ? null : ((Number)obj).doubleValue();
	}
	
	public static long extractPrimitiveLong(Object[] results, int pos) {
		if(results == null || pos >= results.length) return 0l;
		Object obj = results[pos];
		return obj == null ? 0l : ((Number)obj).longValue();
	}
	
	public static int extractPrimitiveInt(Object[] results, int pos) {
		if(results == null || pos >= results.length) return 0;
		Object obj = results[pos];
		return obj == null ? 0 : ((Number)obj).intValue();
	}
	
	
	public static float extractPrimitiveFloat(Object[] results, int pos) {
		if(results == null || pos >= results.length) return 0l;
		Object obj = results[pos];
		return obj == null ? 0l : ((Number)obj).longValue();
	}
	
	public static String extractString(Object[] results, int pos) {
		if(results == null || pos >= results.length ) return null;
		Object obj = results[pos];
		return obj == null ? null : (obj instanceof String ? (String)obj : obj.toString());
	}
	
	public static boolean extractBoolean(Object[] results, int pos, boolean def) {
		if(results == null || pos >= results.length) return def;
		Object obj = results[pos];
		return obj == null ? def : ((Boolean)obj).booleanValue();
	}
	
	public static final String convert(String content) {
		if(StringHelper.containsNonWhitespace(content)) {
			StringBuilder sb = new StringBuilder(content.length() + 100);
			for(char ch : content.toCharArray()) {
				if(ch < 55000) {
					sb.append(ch);
				}
			}
			return sb.toString();
		}
		return content;
	}
	
	/*
	 * Big lists must be partitioned because PostgreSQL can not handle so many elements in IN operator,
	 * see https://makk.es/blog/postgresql-parameter-limitation/.
	 * Solution taken from https://e.printstacktrace.blog/divide-a-list-to-lists-of-n-size-in-Java-8/.
	 */
	public static <T> Collection<List<T>> collectionOfChunks(List<T> list) {
		return collectionOfChunks(list, 1);
	}
	
	public static <T> Collection<List<T>> collectionOfChunks(List<T> list, int numOfParameters) {
		final int chunkSize = DEFAULT_CHUNK_SIZE / numOfParameters;
		AtomicInteger counter = new AtomicInteger();
		return list.stream()
			.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
			.values();
	}
	
	public static boolean isConstraintViolationException(Throwable e) {
		int count = 0;// prevent some infinite loop
		do {
			if(e instanceof ConstraintViolationException) {
				return true;
			}
			e = e.getCause();
			count++;
		} while (e != null && count < 10);
		return false;
	}
}
