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
package org.olat.modules.quality.analysis.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.quality.analysis.EvaluationFormView;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisRow implements EvaluationFormView {
	
	private final EvaluationFormView formView;
	private final FormLink openLink;

	public AnalysisRow(EvaluationFormView formView, FormLink openLink) {
		this.formView = formView;
		this.openLink = openLink;
	}

	@Override
	public String getResourceableTypeName() {
		return formView.getResourceableTypeName();
	}

	@Override
	public Long getResourceableId() {
		return formView.getResourceableId();
	}
	
	@Override
	public Long getFormEntryKey() {
		return formView.getFormEntryKey();
	}
	
	@Override
	public Date getFormCreatedDate() {
		return formView.getFormCreatedDate();
	}

	@Override
	public String getFormTitle() {
		return formView.getFormTitle();
	}

	@Override
	public Long getNumberDataCollections() {
		return formView.getNumberDataCollections();
	}

	@Override
	public Date getSoonestDataCollectionDate() {
		return formView.getSoonestDataCollectionDate();
	}

	@Override
	public Date getLatestDataCollectionDate() {
		return formView.getLatestDataCollectionDate();
	}

	@Override
	public Long getNumberParticipationsDone() {
		return formView.getNumberParticipationsDone();
	}

	public FormLink getOpenLink() {
		return openLink;
	}
	
}
