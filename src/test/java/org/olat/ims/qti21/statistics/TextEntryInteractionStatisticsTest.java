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
package org.olat.ims.qti21.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.ims.qti21.model.statistics.TextEntryInteractionStatistics;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntryAlternative;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Test the OpenOLAT implementation of match() / or Mapping (in QtiWorks)
 * for textEntryInteraction.
 * 
 * Initial date: 15 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class TextEntryInteractionStatisticsTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { true, "+bremsen()", Collections.singletonList("+ bremsen()"), "+bremsen()", true },
            { true, "+bremsen()", Collections.singletonList("+ bremsen()"), " +bremsen()", true },
            { true, "+bremsen()", Collections.singletonList("+ bremsen()"), " +bremsen ()", false },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), " +Bremsen ()", false },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), " +breMSen()", true },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), " +BREMSEN() ", true },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), " + BREMSEN() ", true },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), "bremsen", false },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), "bremsen()", false },
            { false, "+bremsen()", Collections.singletonList("+ bremsen()"), "-bremsen()", false },
            { false, null, List.of(), "-bremsen()", false }// special case where FIB has no correct response
        });
    }
	
	private boolean caseSensitive;
	private String correctResponse;
	private List<String> alternatives;
	private String response;
	private boolean match;
	
	public TextEntryInteractionStatisticsTest(boolean caseSensitive, String correctResponse, List<String> alternatives, String response, boolean match) {
		this.caseSensitive = caseSensitive;
		this.correctResponse = correctResponse;
		this.alternatives = alternatives;
		this.response = response;
		this.match = match;	
	}
	
	@Test
	public void matchStatistics() {
		TextEntryInteractionStatistics textEntryStats = new TextEntryInteractionStatistics(null,
				caseSensitive, correctResponse, alternatives, 1.0d);
		boolean hasMatched = textEntryStats.matchResponse(response);
		Assert.assertEquals(match, hasMatched);
	}
	
	@Test
	public void matchBuilder() {
		TextEntry textEntry = new TextEntry(Identifier.assumedLegal("textentry"));
		textEntry.setSolution(correctResponse);
		
		List<TextEntryAlternative> textEntryAlternatives = new ArrayList<>();
		for(String alt:alternatives) {
			TextEntryAlternative alternative = new TextEntryAlternative();
			alternative.setAlternative(alt);
			textEntryAlternatives.add(alternative);
		}
		
		textEntry.setAlternatives(textEntryAlternatives);
		textEntry.setCaseSensitive(caseSensitive);
		boolean hasMatched = textEntry.match(response);
		Assert.assertEquals(match, hasMatched);
	}

}
