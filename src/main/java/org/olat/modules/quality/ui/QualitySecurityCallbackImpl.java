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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationStatus;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.generator.QualityGenerator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualitySecurityCallbackImpl implements QualitySecurityCallback {

	private final Roles roles;
	private final boolean hasReportAccess;
	
	@Autowired
	private QualityModule qualityModule;
	@Autowired
	private QualityService qualityService;

	public QualitySecurityCallbackImpl(IdentityRef identityRef, Roles roles) {
		this.roles = roles;
		CoreSpringFactory.autowireObject(this);
		
		QualityDataCollectionViewSearchParams searchParams = new QualityDataCollectionViewSearchParams();
		searchParams.setReportAccessIdentity(identityRef);
		this.hasReportAccess = qualityService.getDataCollectionCount(searchParams) > 0;
	}

	@Override
	public boolean canCreateSuggestion() {
		return qualityModule.isSuggestionEnabled();
	}

	@Override
	public boolean canViewDataCollections() {
		return canEditDataCollections() || roles.isPrincipal() || hasReportAccess;
	}

	@Override
	public OrganisationRoles[] getViewDataCollectionRoles() {
		return new OrganisationRoles[] { 
				OrganisationRoles.qualitymanager,
				OrganisationRoles.administrator,
				OrganisationRoles.principal 
			};
	}

	@Override
	public boolean canEditDataCollections() {
		return roles.isQualityManager() || roles.isAdministrator();
	}

	@Override
	public boolean canCreateDataCollections() {
		return canEditDataCollections();
	}

	@Override
	public boolean canViewDataCollectionConfigurations() {
		return canEditDataCollections() || roles.isPrincipal();
	}
	
	@Override
	public boolean canUpdateBaseConfiguration(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && PREPARATION.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canSetPreparation(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && READY.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canSetReady(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && PREPARATION.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canSetRunning(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && isNotRunning(dataCollection);
	}

	@Override
	public boolean canSetFinished(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && isNotFinished(dataCollection);
	}

	@Override
	public boolean canDeleteDataCollections() {
		return canEditDataCollections();
	}

	@Override
	public boolean canDeleteDataCollection(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && isNotRunning(dataCollection);
	}

	@Override
	public boolean canAddParticipants(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && isNotFinished(dataCollection);
	}

	@Override
	public boolean canRevomeParticipation(QualityDataCollectionLight dataCollection) {
		return canEditDataCollections() && isNotRunning(dataCollection);
	}

	@Override
	public boolean canEditReminders() {
		return canEditDataCollections();
	}

	@Override
	public boolean canEditReminder(QualityDataCollectionLight dataCollection, QualityReminder reminder) {
		return canEditReminders() && isNotSent(reminder) && isNotFinished(dataCollection);
	}

	private boolean isNotSent(QualityReminder reminder) {
		return reminder == null || !reminder.isSent();
	}

	@Override
	public boolean canEditReportAccessOnline(QualityDataCollection dataCollection) {
		return canEditDataCollections();
	}
	
	@Override
	public boolean canEditReportAccessEmail(QualityDataCollection dataCollection) {
		return canEditDataCollections() && isNotFinished(dataCollection);
	}

	@Override
	public boolean canEditReportAccessMembers(QualityDataCollection dataCollection) {
		return canEditDataCollections();
	}

	@Override
	public boolean canViewReports() {
		return canViewDataCollections() ;
	}

	@Override
	public boolean canViewReport(QualityDataCollectionLight dataCollection) {
		return canViewReports() && isRunning(dataCollection);
	}

	@Override
	public boolean canExecute(QualityExecutorParticipation participation) {
		QualityExecutorParticipationStatus status = participation.getExecutionStatus();
		return QualityExecutorParticipationStatus.READY.equals(status)
				|| QualityExecutorParticipationStatus.PARTICIPATING.equals(status);
	}

	private boolean isRunning(QualityDataCollectionLight dataCollection) {
		return !isNotRunning(dataCollection);
	}
	
	private boolean isNotRunning(QualityDataCollectionLight dataCollection) {
		QualityDataCollectionStatus status = dataCollection.getStatus();
		return PREPARATION.equals(status) || READY.equals(status);
	}

	private boolean isNotFinished(QualityDataCollectionLight dataCollection) {
		return !FINISHED.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canViewGenerators() {
		return roles.isQualityManager() || roles.isAdministrator() || roles.isPrincipal();
	}

	@Override
	public boolean canCreateGenerators() {
		return canEditGenerators();
	}

	@Override
	public boolean canEditGenerators() {
		return canEditDataCollections();
	}

	@Override
	public boolean canEditGenerator(QualityGenerator generator) {
		return canEditGenerators() && !generator.isEnabled();
	}

	@Override
	public boolean canEditGeneratorForm(QualityGenerator generator, Long numOfDataCollections) {
		return canEditGenerator(generator) && numOfDataCollections < 1;
	}

	@Override
	public boolean canEditReportAccessOnline(QualityGenerator generator) {
		return canEditGenerator(generator);
	}

	@Override
	public boolean canEditReportAccessEmail(QualityGenerator generator) {
		return canEditGenerator(generator);
	}

	@Override
	public boolean canEditReportAccessMembers(QualityGenerator generator) {
		return canEditGenerator(generator);
	}

	@Override
	public boolean canActivateGenerators() {
		return canEditGenerators();
	}

	@Override
	public boolean canDeleteGenerators() {
		return canEditGenerators();
	}

	@Override
	public boolean canDeleteGenerator(long numberDataCollections) {
		return canDeleteGenerators() && numberDataCollections < 1;
	}

	@Override
	public boolean canViewAnalysis() {
		return roles.isQualityManager() || roles.isAdministrator() || roles.isPrincipal();
	}

	@Override
	public OrganisationRoles[] getViewAnalysisRoles() {
		return new OrganisationRoles[] { 
				OrganisationRoles.qualitymanager,
				OrganisationRoles.administrator,
				OrganisationRoles.principal 
			};
	}

	@Override
	public OrganisationRoles[] getViewPresentationRoles() {
		return new OrganisationRoles[] { 
				OrganisationRoles.qualitymanager,
				OrganisationRoles.administrator,
				OrganisationRoles.principal 
			};
	}

	@Override
	public boolean canEditPresentations() {
		return roles.isQualityManager() || roles.isAdministrator();
	}

	@Override
	public boolean canDeletePresentation(AnalysisPresentation presentation) {
		return canEditPresentations() && presentation.getKey() != null;
	}

}
