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

import java.util.Date;
import java.util.Locale;

import org.olat.ims.qti21.model.DigitalSignatureOptions;

/**
 * 
 * Initial date: 20.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OutcomesListener {
	
	/**
	 * Add more useful informations to the signature as a mail bundle to send the signature per email.
	 * 
	 * @param candidateSession
	 * @param options
	 * @param locale
	 */
	public void decorateConfirmation(AssessmentTestSession candidateSession, DigitalSignatureOptions options, Date timestamp, Locale locale);
	
	/**
	 * Update the outcomes.
	 * 
	 * @param score The current score
	 * @param pass Currently passed or failed (null is possible)
	 * @param compeltion The number of questions answered measured against the number of questions in the test
	 */
	public void updateOutcomes(Float score, Boolean pass, Date start, Double completion);
	
	/**
	 * The test or item is submitted, it's finished.
	 * 
	 * @param score The score (automatically calculated)
	 * @param pass Passed, failed or null
	 * @param completion The completion grade of the test
	 * @param assessmentId The ID of the session (primary of the test or item session)
	 */
	public void submit(Float score, Boolean pass, Date start, Double completion, Long assessmentId);

}
