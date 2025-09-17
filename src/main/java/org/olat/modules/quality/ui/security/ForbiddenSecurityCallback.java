/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.ui.security;

import java.util.List;

import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.analysis.AnalysisPresentation;

/**
 * 
 * Initial date: Sep 16, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ForbiddenSecurityCallback implements MainSecurityCallback {

	@Override
	public boolean canCreateSuggestion() {
		return false;
	}

	@Override
	public boolean canExecute(QualityExecutorParticipation participation) {
		return false;
	}

	@Override
	public boolean canViewDataCollections() {
		return false;
	}

	@Override
	public List<OrganisationRef> getViewDataCollectionOrganisationRefs() {
		return null;
	}

	@Override
	public List<OrganisationRef> getViewOnlyDataCollectionOrganisationRefs() {
		return null;
	}

	@Override
	public List<OrganisationRef> getEditDataCollectionOrganisationRefs() {
		return null;
	}

	@Override
	public List<OrganisationRef> getLearnResourceManagerOrganisationRefs() {
		return null;
	}

	@Override
	public boolean canCreateDataCollections() {
		return false;
	}

	@Override
	public boolean canViewGenerators() {
		return false;
	}

	@Override
	public List<OrganisationRef> getViewGeneratorOrganisationRefs() {
		return null;
	}

	@Override
	public boolean canCreateGenerators() {
		return false;
	}

	@Override
	public boolean canViewPreviews() {
		return false;
	}

	@Override
	public boolean canViewAnalysis() {
		return false;
	}

	@Override
	public List<OrganisationRef> getViewAnalysisOrganisationRefs() {
		return null;
	}

	@Override
	public List<OrganisationRef> getViewPresentationOrganisationRefs() {
		return null;
	}

	@Override
	public boolean canEditPresentations() {
		return false;
	}

	@Override
	public boolean canDeletePresentation(AnalysisPresentation presentation) {
		return false;
	}

	@Override
	public boolean canCreateToDoTasks() {
		return false;
	}

}
