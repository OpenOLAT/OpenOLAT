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
package org.olat.ims.qti21.ui.assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.assessment.AssessmentEntry;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 17.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestCorrection {
	
	private final Set<AssessmentTestSession> testSessions = new HashSet<>();
	private final Map<Identity, List<AssessmentItemCorrection>> identityToCorrections = new HashMap<>();
	private final Map<AssessmentItemRef, List<AssessmentItemCorrection>> itemRefToCorrections = new HashMap<>();
	private final Map<Identity, AssessmentEntry> identityToAssessmentEntries;
	
	public AssessmentTestCorrection(Map<Identity, AssessmentEntry> identityToAssessmentEntries) {
		this.identityToAssessmentEntries = identityToAssessmentEntries;
	}
	
	public List<AssessmentItemCorrection> getCorrections(AssessmentItemRef itemRef) {
		return itemRefToCorrections.get(itemRef);
	}
	
	public List<AssessmentItemCorrection> getCorrections(Identity assessedIdentity) {
		return identityToCorrections.get(assessedIdentity);
	}
	
	public List<AssessmentTestSession> getTestSessions() {
		return new ArrayList<>(testSessions);
	}
	
	public AssessmentEntry getAssessmentEntry(Identity assessedIdentity) {
		return identityToAssessmentEntries.get(assessedIdentity);
	}
	
	public void addAssessmentEntry(AssessmentEntry assessmentEntry) {
		identityToAssessmentEntries.put(assessmentEntry.getIdentity(), assessmentEntry);
	}
	
	public void add(AssessmentItemCorrection correction) {
		testSessions.add(correction.getTestSession());
		
		Identity identity = correction.getAssessedIdentity();
		List<AssessmentItemCorrection> identityCorrections = identityToCorrections.get(identity);
		if(identityCorrections == null) {
			identityCorrections = new ArrayList<>();
			identityToCorrections.put(identity, identityCorrections);
		}
		identityCorrections.add(correction);
		
		AssessmentItemRef itemRef = correction.getItemRef();
		List<AssessmentItemCorrection> itemCorrections = itemRefToCorrections.get(itemRef);
		if(itemCorrections == null) {
			itemCorrections = new ArrayList<>();
			itemRefToCorrections.put(itemRef, itemCorrections);
		}
		itemCorrections.add(correction);
	}
}
