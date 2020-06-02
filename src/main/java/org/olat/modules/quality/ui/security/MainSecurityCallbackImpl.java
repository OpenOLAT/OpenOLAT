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
package org.olat.modules.quality.ui.security;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationStatus;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class MainSecurityCallbackImpl implements MainSecurityCallback {

	private final boolean canView;
	private final boolean canEdit;
	private final List<OrganisationRef> viewerOrganisationRefs;
	private final List<OrganisationRef> learnResourceManagerOrganisationRefs;
	private final QualityDataCollectionViewSearchParams reportAccessParams;
	private boolean canViewDataCollections;
	
	@Autowired
	private QualityModule qualityModule;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private BaseSecurityModule securityModule;

	public MainSecurityCallbackImpl(IdentityRef identityRef, boolean canView, boolean canEdit,
			List<OrganisationRef> viewerOrganisationRefs, List<OrganisationRef> learnResourceManagerOrganisationRefs) {
		this.canView = canView;
		this.canEdit = canEdit;
		this.viewerOrganisationRefs = viewerOrganisationRefs;
		this.learnResourceManagerOrganisationRefs = learnResourceManagerOrganisationRefs;
		CoreSpringFactory.autowireObject(this);
		
		reportAccessParams = new QualityDataCollectionViewSearchParams();
		reportAccessParams.setOrgansationRefs(viewerOrganisationRefs);
		reportAccessParams.setReportAccessIdentity(identityRef);
		reportAccessParams.setLearnResourceManagerOrganisationRefs(learnResourceManagerOrganisationRefs);
		reportAccessParams.setIgnoreReportAccessRelationRole(!securityModule.isRelationRoleEnabled());
	}

	@Override
	public boolean canCreateSuggestion() {
		return qualityModule.isSuggestionEnabled();
	}

	@Override
	public boolean canExecute(QualityExecutorParticipation participation) {
		QualityExecutorParticipationStatus status = participation.getExecutionStatus();
		return QualityExecutorParticipationStatus.READY.equals(status)
				|| QualityExecutorParticipationStatus.PARTICIPATING.equals(status);
	}

	@Override
	public boolean canViewDataCollections() {
		if (!canViewDataCollections) {
			// Check if the user has access gained in the meantime, e.g because he has now report access
			canViewDataCollections = canView || hasReportAccess();
		}
		return canViewDataCollections;
	}

	private boolean hasReportAccess() {
		return qualityService.getDataCollectionCount(reportAccessParams) > 0;
	}

	@Override
	public List<OrganisationRef> getViewDataCollectionOrganisationRefs() {
		return viewerOrganisationRefs;
	}

	@Override
	public List<OrganisationRef> getLearnResourceManagerOrganisationRefs() {
		return learnResourceManagerOrganisationRefs;
	}

	@Override
	public boolean canCreateDataCollections() {
		return canEdit;
	}
	
	@Override
	public boolean canViewGenerators() {
		return canCreateDataCollections();
	}

	@Override
	public List<OrganisationRef> getViewGeneratorOrganisationRefs() {
		return viewerOrganisationRefs;
	}

	@Override
	public boolean canCreateGenerators() {
		return canCreateDataCollections();
	}

	@Override
	public boolean canViewAnalysis() {
		return canView;
	}

	@Override
	public List<OrganisationRef> getViewAnalysisOrganisationRefs() {
		return viewerOrganisationRefs;
	}

	@Override
	public List<OrganisationRef> getViewPresentationOrganisationRefs() {
		return viewerOrganisationRefs;
	}

	@Override
	public boolean canEditPresentations() {
		return canEdit;
	}

	@Override
	public boolean canDeletePresentation(AnalysisPresentation presentation) {
		return canEditPresentations() && presentation.getKey() != null;
	}

}
