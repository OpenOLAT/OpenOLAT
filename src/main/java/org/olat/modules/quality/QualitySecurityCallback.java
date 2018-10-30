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
package org.olat.modules.quality;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.generator.QualityGenerator;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualitySecurityCallback {

	public boolean canCreateSuggestion();

	public boolean canViewDataCollections();
	
	public OrganisationRoles[] getViewDataCollectionRoles();
	
	public boolean canCreateDataCollections();
	
	public boolean canEditDataCollections();
	
	public boolean canViewDataCollectionConfigurations();
	
	public boolean canUpdateBaseConfiguration(QualityDataCollectionLight dataCollection);
	
	public boolean canSetPreparation(QualityDataCollectionLight dataCollection);
	
	public boolean canSetReady(QualityDataCollectionLight dataCollection);
	
	public boolean canSetRunning(QualityDataCollectionLight dataCollection);
	
	public boolean canSetFinished(QualityDataCollectionLight dataCollection);
	
	public boolean canDeleteDataCollections();
	
	public boolean canDeleteDataCollection(QualityDataCollectionLight dataCollection);
	
	public boolean canAddParticipants(QualityDataCollectionLight dataCollection);
	
	public boolean canRevomeParticipation(QualityDataCollectionLight dataCollection);

	public boolean canEditReminders();

	public boolean canEditReminder(QualityDataCollectionLight dataCollection, QualityReminder reminder);
	
	public boolean canViewReportAccesses();
	
	public boolean canEditReportAccesses();

	public boolean canViewReports();

	public boolean canViewReport(QualityDataCollectionLight dataCollection);

	public boolean canExecute(QualityExecutorParticipation participation);

	public boolean canViewGenerators();
	
	public boolean canCreateGenerators();

	public boolean canEditGenerators();

	public boolean canEditGenerator(QualityGenerator generator);

	public boolean canEditGeneratorForm(QualityGenerator generator, Long numOfDataCollections);

	public boolean canActivateGenerators();

	public boolean canDeleteGenerators();

	public boolean canDeleteGenerator(long numberDataCollections);

	public boolean canViewAnalysis();
	
	public OrganisationRoles[] getViewAnalysisRoles();
	
	public OrganisationRoles[] getViewPresentationRoles();
	
	public boolean canEditPresentations();

	public boolean canDeletePresentation(AnalysisPresentation presentation);

}
