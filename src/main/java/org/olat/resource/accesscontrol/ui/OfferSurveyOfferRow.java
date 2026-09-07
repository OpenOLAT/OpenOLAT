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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.OfferToSurvey;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyOfferRow implements Persistable {

	private static final long serialVersionUID = 1L;

	private final EvaluationFormSurvey survey;
	private final OfferToSurvey offerToSurvey;
	private boolean pendingUsed;

	private FormToggle useEl;
	private UpDown upDown;

	public OfferSurveyOfferRow(EvaluationFormSurvey survey, OfferToSurvey offerToSurvey) {
		this.survey = survey;
		this.offerToSurvey = offerToSurvey;
		this.pendingUsed = offerToSurvey != null;
	}

	@Override
	public Long getKey() {
		return survey.getKey();
	}

	public EvaluationFormSurvey getSurvey() {
		return survey;
	}

	public RepositoryEntry getFormEntry() {
		return survey.getFormEntry();
	}

	public String getTitle() {
		return survey.getFormEntry().getDisplayname();
	}

	public String getReference() {
		return survey.getFormEntry().getExternalRef();
	}

	public String getStepName() {
		return survey.getDisplayName();
	}

	public OfferToSurvey getOfferToSurvey() {
		return offerToSurvey;
	}

	public boolean isUsed() {
		return pendingUsed;
	}

	public void setPendingUsed(boolean pendingUsed) {
		this.pendingUsed = pendingUsed;
	}

	public FormToggle getUseEl() {
		return useEl;
	}

	public void setUseEl(FormToggle useEl) {
		this.useEl = useEl;
	}

	public UpDown getUpDown() {
		return upDown;
	}

	public void setUpDown(UpDown upDown) {
		this.upDown = upDown;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 1 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof OfferSurveyOfferRow row) {
			return getKey() != null && getKey().equals(row.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
