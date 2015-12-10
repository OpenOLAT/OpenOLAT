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
	
	public static final String TOOLNAME = "OpenOLAT";
	
	public static final String TOOLVERSION = "v1.0";
	
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
	
	public static final String FEEDBACKBASIC = "FEEDBACKBASIC";
	
	public static final Identifier FEEDBACKBASIC_IDENTIFIER = Identifier.parseString(FEEDBACKBASIC);
	
	public static final String FEEDBACKMODAL = "FEEDBACKMODAL";
	
	public static final Identifier FEEDBACKMODAL_IDENTIFIER = Identifier.parseString(FEEDBACKMODAL);
	
	public static final String CORRECT = "correct";

	public static final Identifier CORRECT_IDENTIFIER = Identifier.parseString(CORRECT);
	
	public static final IdentifierValue CORRECT_IDENTIFIER_VALUE = new IdentifierValue(CORRECT);
	
	public static final String INCORRECT = "incorrect";
	
	public static final Identifier INCORRECT_IDENTIFIER = Identifier.parseString(INCORRECT);
	
	public static final IdentifierValue INCORRECT_IDENTIFIER_VALUE = new IdentifierValue(INCORRECT);

}
