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
package org.olat.ims.qti21;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;

/**
 * 
 * Initial date: 20.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21Constants {
	
	public static final OLATResourceable ASSESSMENT_TEST_SESSION_PULLED = OresHelper.createOLATResourceableInstance("assessment-test-session-pulled", 0l);
	
	public static final String TOOLNAME = "OpenOLAT";
	
	public static final String TOOLVERSION = "v1.0";
	
	public static final String QTI_21_FORMAT = "IMS QTI 2.1";
	
	public static final String CHOICE_ALIGN_RIGHT = "choiceright";
	
	public static final String SCORE = "SCORE";
	
	public static final Identifier SCORE_IDENTIFIER = Identifier.assumedLegal(SCORE);
	
	public static final ComplexReferenceIdentifier SCORE_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(SCORE);
	
	public static final String MAXSCORE = "MAXSCORE";
	
	public static final Identifier MAXSCORE_IDENTIFIER = Identifier.assumedLegal(MAXSCORE);
	
	public static final ComplexReferenceIdentifier MAXSCORE_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(MAXSCORE);
	
	public static final String MINSCORE = "MINSCORE";
	
	public static final Identifier MINSCORE_IDENTIFIER = Identifier.assumedLegal(MINSCORE);
	
	public static final ComplexReferenceIdentifier MINSCORE_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(MINSCORE);
	
	public static final String PASS = "PASS";

	public static final Identifier PASS_IDENTIFIER = Identifier.assumedLegal(PASS);
	
	public static final ComplexReferenceIdentifier PASS_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(PASS);
	
	public static final String NUM_ATTEMPTS = "numAttempts";
	
	public static final Identifier NUM_ATTEMPTS_IDENTIFIER = Identifier.assumedLegal(NUM_ATTEMPTS);
	
	public static final ComplexReferenceIdentifier NUM_ATTEMPTS_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(NUM_ATTEMPTS);
	
	public static final String FEEDBACKBASIC = "FEEDBACKBASIC";
	
	public static final Identifier FEEDBACKBASIC_IDENTIFIER = Identifier.parseString(FEEDBACKBASIC);
	
	public static final ComplexReferenceIdentifier FEEDBACKBASIC_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(FEEDBACKBASIC);
	
	public static final String FEEDBACKMODAL = "FEEDBACKMODAL";
	
	public static final Identifier FEEDBACKMODAL_IDENTIFIER = Identifier.parseString(FEEDBACKMODAL);
	
	public static final String CORRECT = "correct";

	public static final Identifier CORRECT_IDENTIFIER = Identifier.parseString(CORRECT);
	
	public static final ComplexReferenceIdentifier CORRECT_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(CORRECT);
	
	public static final IdentifierValue CORRECT_IDENTIFIER_VALUE = new IdentifierValue(CORRECT);
	
	public static final String INCORRECT = "incorrect";
	
	public static final Identifier INCORRECT_IDENTIFIER = Identifier.parseString(INCORRECT);
	
	public static final ComplexReferenceIdentifier INCORRECT_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(INCORRECT);
	
	public static final IdentifierValue INCORRECT_IDENTIFIER_VALUE = new IdentifierValue(INCORRECT);

	public static final String WRONG = "wrong";
	
	public static final Identifier WRONG_IDENTIFIER = Identifier.parseString(WRONG);
	
	public static final IdentifierValue WRONG_IDENTIFIER_VALUE = new IdentifierValue(WRONG);
	
	public static final String EMPTY = "empty";
	
	public static final Identifier EMPTY_IDENTIFIER = Identifier.parseString(EMPTY);
	
	public static final ComplexReferenceIdentifier EMPTY_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(EMPTY);
	
	public static final IdentifierValue EMPTY_IDENTIFIER_VALUE = new IdentifierValue(EMPTY);
	
	public static final String ANSWERED = "answered";
	
	public static final Identifier ANSWERED_IDENTIFIER = Identifier.parseString(ANSWERED);
	
	public static final IdentifierValue ANSWERED_IDENTIFIER_VALUE = new IdentifierValue(ANSWERED);
	

	public static final String HINT = "HINT";
	
	public static final Identifier HINT_IDENTIFIER  = Identifier.parseString(HINT);
	
	/** This is the variable identifer */
	public static final String HINT_REQUEST = "HINTREQUEST";
	
	public static final Identifier HINT_REQUEST_IDENTIFIER = Identifier.parseString(HINT_REQUEST);
	
	public static final ComplexReferenceIdentifier HINT_REQUEST_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(HINT_REQUEST);
	
	/** This is the outcome declaration identifier */
	public static final String HINT_FEEDBACKMODAL = "HINTFEEDBACKMODAL";
	
	public static final Identifier HINT_FEEDBACKMODAL_IDENTIFIER = Identifier.parseString(HINT_FEEDBACKMODAL);
	
	public static final ComplexReferenceIdentifier HINT_FEEDBACKMODAL_CLX_IDENTIFIER = ComplexReferenceIdentifier.parseString(HINT_FEEDBACKMODAL);
	
	
	public static final String CORRECT_SOLUTION = "SOLUTIONMODAL";

	public static final Identifier CORRECT_SOLUTION_IDENTIFIER  = Identifier.parseString(CORRECT_SOLUTION);
	
	
	public static final String CSS_MATCH_MATRIX = "match_matrix";
	
	public static final String CSS_MATCH_DRAG_AND_DROP = "match_dnd";
	
	public static final String CSS_MATCH_TRUE_FALSE = "match_true_false";
	
	public static final String CSS_MATCH_KPRIM = "match_krpim";
	
	public static final String CSS_MATCH_SOURCE_TOP = "source-top";
	
	public static final String CSS_MATCH_SOURCE_LEFT = "source-left";
	
	public static final String CSS_MATCH_SOURCE_RIGHT = "source-right";
	
	public static final String CSS_MATCH_SOURCE_BOTTOM = "source-bottom";
	
	public static final String CSS_INTERACTION_RESPONSIVE = "interaction-responsive";
	
	public static final String CSS_HOTSPOT_DISABLE_SHADOW =  "hotspot-noshadow";
	
	public static final String CSS_ESSAY_DISABLE_COPYPASTE =  "essay-nocopypaste";
	

	public static final String QMD_ENTRY_SUMMARY_NONE = "summaryNone";
	
	public static final String QMD_ENTRY_SUMMARY_COMPACT = "summaryCompact";
	
	public static final String QMD_ENTRY_SUMMARY_DETAILED = "summaryDetailed";
	
	public static final String QMD_ENTRY_SUMMARY_SECTION = "summarySection";
	
	
	public static final String QMD_LABEL_SEQUENCE = "qmd_navigatorpagetype";
	
	public static final String QMD_ENTRY_SEQUENCE_SECTION = "sectionPage";
	
	public static final String QMD_ENTRY_SEQUENCE_ITEM = "itemPage";
	
	
	public static final String QMD_ENTRY_TYPE_ASSESS = "Assessment";
	
	public static final String QMD_ENTRY_TYPE_SELF = "Self-Assessment";
	
	public static final String QMD_ENTRY_TYPE_SURVEY = "Survey";
	
	public enum HotspotLayouts {
		
		standard(""),
		light("hotspot-light"),
		inverted("hotspot-inverted"),
		green("hotspot-green"),
		purple("hotspot-purple");
		
		private final String cssClass;
		
		private HotspotLayouts(String cssClass) {
			this.cssClass = cssClass;
		}
		
		public String cssClass() {
			return cssClass;
		}
	}

}