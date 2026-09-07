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

import java.util.Map;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.repository.RepositoryEntry;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyRow implements Persistable {

	private static final long serialVersionUID = 1L;

	private final EvaluationFormSurvey survey;
	private final boolean used;
	private long openCount;
	private long completedCount;
	private long canceledCount;

	private FormLink stepNameLink;
	private FormLink toolsLink;
	private OfferSurveyParticipationListController detailsController;
	private Map<Long, Integer> positionByOfferKey = Map.of();

	public OfferSurveyRow(EvaluationFormSurvey survey, boolean used) {
		this.survey = survey;
		this.used = used;
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

	public boolean isUsed() {
		return used;
	}

	public long getOpenCount() {
		return openCount;
	}

	public void setOpenCount(long openCount) {
		this.openCount = openCount;
	}

	public long getCompletedCount() {
		return completedCount;
	}

	public void setCompletedCount(long completedCount) {
		this.completedCount = completedCount;
	}

	public long getCanceledCount() {
		return canceledCount;
	}

	public void setCanceledCount(long canceledCount) {
		this.canceledCount = canceledCount;
	}

	public FormLink getStepNameLink() {
		return stepNameLink;
	}

	public void setStepNameLink(FormLink stepNameLink) {
		this.stepNameLink = stepNameLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public OfferSurveyParticipationListController getDetailsController() {
		return detailsController;
	}

	public void setDetailsController(OfferSurveyParticipationListController detailsController) {
		this.detailsController = detailsController;
	}

	public boolean isDetailsControllerAvailable() {
		return detailsController != null;
	}

	public String getDetailsControllerName() {
		return detailsController.getInitialFormItem().getComponent().getComponentName();
	}

	public void setPositionByOfferKey(Map<Long, Integer> positionByOfferKey) {
		this.positionByOfferKey = positionByOfferKey;
	}

	public Integer getPosition(Long offerKey) {
		return positionByOfferKey.get(offerKey);
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
		if (obj instanceof OfferSurveyRow row) {
			return getKey() != null && getKey().equals(row.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
