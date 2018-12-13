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
package org.olat.ims.qti21.model;

import java.util.Date;
import java.util.Locale;

import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesAssessmentItemListener;
import org.olat.ims.qti21.OutcomesListener;

/**
 * 
 * Initial date: 23.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InMemoryOutcomeListener implements OutcomesListener, OutcomesAssessmentItemListener {

	@Override
	public void outcomes(AssessmentTestSession candidateSession, Float score, Boolean pass) {
		//
	}

	@Override
	public void decorateConfirmation(AssessmentTestSession candidateSession, DigitalSignatureOptions options, Date timestamp, Locale locale) {
		//do nothing
	}

	@Override
	public void updateOutcomes(Float score, Boolean pass, Double completion) {
		//
	}

	@Override
	public void submit(Float score, Boolean pass, Double completion, Long assessmentId) {
		//
	}
}
