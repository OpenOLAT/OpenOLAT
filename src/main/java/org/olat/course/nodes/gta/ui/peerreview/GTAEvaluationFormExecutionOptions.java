/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

/**
 * 
 * Initial date: 18 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEvaluationFormExecutionOptions {
	
	private boolean withAssessedIdentityHeader;
	private boolean withReviewerHeader;
	private boolean anonym;
	private String placeHolderName;
	private boolean withDocuments;
	private boolean withBackButton;
	private boolean withRating;

	public static GTAEvaluationFormExecutionOptions valueOf(boolean withAssessedIdentityHeader, boolean withReviewerHeader,
			boolean anonym,String placeHolderName, boolean withDocuments, boolean withBackButton, boolean withRating) {
		GTAEvaluationFormExecutionOptions options = new GTAEvaluationFormExecutionOptions();
		options.withAssessedIdentityHeader = withAssessedIdentityHeader;
		options.withReviewerHeader = withReviewerHeader;
		options.anonym = anonym;
		options.placeHolderName = placeHolderName;
		options.withDocuments = withDocuments;
		options.withBackButton = withBackButton;
		options.withRating = withRating;
		return options;
	}

	public boolean withAssessedIdentityHeader() {
		return withAssessedIdentityHeader;
	}

	public boolean withReviewerHeader() {
		return withReviewerHeader;
	}

	public boolean isAnonym() {
		return anonym;
	}

	public String getPlaceHolderName() {
		return placeHolderName;
	}

	public boolean withDocuments() {
		return withDocuments;
	}

	public boolean withBackButton() {
		return withBackButton;
	}
	
	public boolean withRating() {
		return withRating;
	}
}
