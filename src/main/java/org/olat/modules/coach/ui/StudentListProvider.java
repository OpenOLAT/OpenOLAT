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
package org.olat.modules.coach.ui;

import java.util.Iterator;

import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.util.CSSHelper;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 02.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StudentListProvider implements ListProvider {
	
	private static final int MAX_ENTRIES = 10;
	
	private final StudentsTableDataModel model;
	private final UserManager userManager;
	
	public StudentListProvider(StudentsTableDataModel model, UserManager userManager) {
		this.model = model;
		this.userManager = userManager;
	}
	
	@Override
	public int getMaxEntries() {
		return MAX_ENTRIES;
	}
	
	@Override
	public void getResult(String searchValue, ListReceiver receiver) {
		int maxEntries = MAX_ENTRIES;
		boolean hasMore = false;
		searchValue = searchValue.toLowerCase();
		for (Iterator<StudentStatEntry> it_res = model.getObjects().iterator(); (hasMore=it_res.hasNext()) && maxEntries > 0;) {
			StudentStatEntry entry = it_res.next();
			if(contains(searchValue, entry)) {
				maxEntries--;
				String key = entry.getIdentityKey().toString();
				String displayKey = null;//TODO username was name
				String displayText = userManager.getUserDisplayName(entry.getIdentityKey());
				receiver.addEntry(key, null, displayText, CSSHelper.CSS_CLASS_USER);
			}
		}					
		
		if(hasMore){
			receiver.addEntry(".....",".....");
		}		
	}
	
	public static boolean contains(String searchValue, StudentStatEntry entry) {
		String[] userProperties = entry.getIdentityProps();
		for(int i=userProperties.length; i-->0; ) {
			String userProp = userProperties[i];
			if(userProp != null && userProp.toLowerCase().contains(searchValue)) {
				return true;
			}
		}
		return false;
	}
}
