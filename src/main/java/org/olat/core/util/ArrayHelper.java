/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.olat.core.logging.AssertException;

/**
 * <h3>Description:</h3>
 * Some helper method to deal with arrays
 * <p>
 * Initial Date: 22.05.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class ArrayHelper {
	
	private static final String[] EMPTY_STRING_ARRAY = {};
	
	public static String[] emptyStrings() {
		return EMPTY_STRING_ARRAY;
	}

	/**
	 * Helper to sort two string arrays. The arrays must have the same length and
	 * correspond to each other. Use sortAscending to indicate that sorting should
	 * be done on the values of the first array or the second one. Use
	 * sortAscending to indicate the sort order. <br />
	 * Internally, when comparing the two stings, the string.compareTo() method is
	 * used. <br />
	 * After sorting both arrays have a new order based on the given criterias.
	 * 
	 * @param first The first array to be sorted
	 * @param second The second array to be sorted
	 * @param afterFirstArray true: use first array to define sorting; false: use
	 *          second one
	 * @param sortAscending true: sort ascending; false: sort descending
	 * @param caseSensitive true: sort case sensitive; false: sort case insensitive
	 */
	public static void sort(final String[] first, final String[] second, final boolean afterFirstArray, final boolean sortAscending, final boolean caseSensitive) {
		if (first.length != second.length) { throw new AssertException("Can not sort arrays that have different length! first::" + first.length
				+ " second::" + second.length); }

		Object[] pairs = new Object[first.length];
		// create pair array that contains the first and the second array
		for (int i = 0; i < first.length; i++) {
			pairs[i] = new Object[] { first[i], second[i] };
		}
		// sort the pairs
		Arrays.sort(pairs, new Comparator<>() {
			@Override
			public int compare(Object o1, Object o2) {
				Object[] pair1 = (Object[]) o1;
				Object[] pair2 = (Object[]) o2;
				// compare the key-value pair on the value, not the key
				if (afterFirstArray) {
					String val1 = (String) pair1[0];
					String val2 = (String) pair2[0];
					if (!caseSensitive) {
						val1 = val1.toLowerCase();
						val2 = val2.toLowerCase();
					}
					if (sortAscending) return val1.compareTo(val2);
					else return val2.compareTo(val1);
				} else {
					String val1 = (String) pair1[1];
					String val2 = (String) pair2[1];
					if (!caseSensitive) {
						val1 = val1.toLowerCase();
						val2 = val2.toLowerCase();
					}
					if (sortAscending) return val1.compareTo(val2);
					else return val2.compareTo(val1);
				}
			}
		});

		// extract keys and values again from the key-value-pair array to separate
		// arrays
		for (int i = 0; i < pairs.length; i++) {
			Object[] pair = (Object[]) pairs[i];
			first[i] = (String) pair[0];
			second[i] = (String) pair[1];
		}
	}

	/**
	 * Shortcut to create a string array from a collection of strings
	 * @param list
	 * @return
	 */
	public static String[] toArray(Collection<String> list) {
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Helper method to add a value to an existing string array. Note that the
	 * result will be a reference to a new array
	 * 
	 * @param oldArray
	 * @param doBeAddedValue
	 * @param addAtTheEnd true: add new value at last possition of array; false:
	 *          add new value at first position of array.
	 * @return The new array containing the values from the old array and the new
	 *         array
	 */
	public static String[] addToArray(String[] oldArray, String doBeAddedValue, boolean addAtTheEnd) {
		String[] newArray = new String[oldArray.length + 1];
		int targetPos = (addAtTheEnd ? 0 : 1);
		System.arraycopy(oldArray, 0, newArray, targetPos, oldArray.length);
		int newValuePos = (addAtTheEnd ? newArray.length : 1);
		newArray[newValuePos - 1] = doBeAddedValue;
		return newArray;
	}
	
}
