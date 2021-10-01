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
package org.olat.course.assessment.ui.tool;

import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 20.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityListProvider implements ListProvider {
	
	private static final int MAX_ENTRIES = 15;
	
	private final Identity coach;
	private final String subIdent;
	private final RepositoryEntry courseEntry;
	private final RepositoryEntry referenceEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private final UserManager userManager;
	private AssessmentToolManager assessmentToolManager;
	
	public AssessedIdentityListProvider(Identity coach, RepositoryEntry courseEntry,
			RepositoryEntry referenceEntry, String subIdent,
			AssessmentToolSecurityCallback assessmentCallback) {
		this.coach = coach;
		this.subIdent = subIdent;
		this.courseEntry = courseEntry;
		this.referenceEntry = referenceEntry;
		this.assessmentCallback = assessmentCallback;
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		assessmentToolManager = CoreSpringFactory.getImpl(AssessmentToolManager.class);
	}
	
	@Override
	public int getMaxEntries() {
		return MAX_ENTRIES;
	}

	@Override
	public void getResult(String searchValue, ListReceiver receiver) {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, subIdent, referenceEntry, assessmentCallback);
		params.setSearchString(searchValue);

		int maxEntries = MAX_ENTRIES;
		List<IdentityShort> res = assessmentToolManager.getShortAssessedIdentities(coach, params, maxEntries);
		List<Long> entryIdentityKey = assessmentToolManager.getIdentityKeys(coach, params, null);

		boolean hasMore = false;
		for (Iterator<IdentityShort> it_res = res.iterator(); (hasMore=it_res.hasNext()) && maxEntries > 0;) {
			IdentityShort ident = it_res.next();
			if (entryIdentityKey.contains(ident.getKey())) {
				maxEntries--;
				String key = ident.getKey().toString();
				String displayKey = ident.getNickName();
				String displayText = userManager.getUserDisplayName(ident);
				receiver.addEntry(key, displayKey, displayText, CSSHelper.CSS_CLASS_USER);
			}
		}					
		if(hasMore){
			receiver.addEntry(".....",".....");
		}
	}
}