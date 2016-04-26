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
package org.olat.core.util.openxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Initial date: 25.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLWorkbookSharedStrings implements Iterable<String> {

	private final List<String> sharedStrings = new ArrayList<>();
	
	public OpenXMLWorkbookSharedStrings() {
		sharedStrings.add("OpenOLAT");//prevent empty list
	}
	
	/**
	 * Add a shared string and return the index.
	 * @param string
	 * @return
	 */
	public int add(String string) {
		int index = sharedStrings.indexOf(string);
		if(index < 0) {
			sharedStrings.add(string);
			index = sharedStrings.size() - 1;
		}
		return index;
	}

	@Override
	public Iterator<String> iterator() {
		return sharedStrings.iterator();
	}
	
	public int size() {
		return sharedStrings.size();
	}
}
