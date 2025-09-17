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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.generator.QualityGenerator;

/**
 * 
 * Initial date: 14 Nov 2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualitySecurityCallbackFactory {

	private static final ForbiddenSecurityCallback FORBIDDEN_SECURITY_CALLBACK = new ForbiddenSecurityCallback();
	private static final DataCollectionReadOnlySecurityCallback DATA_COLLECTION_READ_ONLY_SECURITY_CALLBACK = new DataCollectionReadOnlySecurityCallback();
	private static final GeneratorReadOnlySecurityCallback GENERATOR_READ_ONLY_SECURITY_CALLBACK = new GeneratorReadOnlySecurityCallback();
	private static final OrganisationRoles[] QUALITY_MANAGER_ROLES = new OrganisationRoles[] {
			OrganisationRoles.qualitymanager, OrganisationRoles.administrator };
	private static final OrganisationRoles[] QUALITY_VIEWER_ROLES = new OrganisationRoles[] {
			OrganisationRoles.qualitymanager, OrganisationRoles.administrator, OrganisationRoles.principal };
	private static final OrganisationRoles[] QUALITY_VIEWER_ONLY_ROLES = new OrganisationRoles[] {
			OrganisationRoles.principal };

	public static MainSecurityCallback createMainSecurityCallback(Roles roles, IdentityRef identityRef) {
		boolean canEdit = roles.isAdministrator() || roles.isQualityManager();
		boolean canView = canEdit || roles.isPrincipal();
		List<OrganisationRef> viewOrganisations = getViewOrganisations(roles, canView);
		List<OrganisationRef> viewOnlyOrganisations = getViewOnlyOrganisations(roles, canView);
		List<OrganisationRef> editOrganisations = getEditOrganisations(roles, canEdit);
		List<OrganisationRef> learnResourceManagerOrganisations = getLearnResourceManagerOrganisations(roles);
		
		return new MainSecurityCallbackImpl(identityRef, canView, canEdit, viewOrganisations, viewOnlyOrganisations,
				editOrganisations, learnResourceManagerOrganisations);
	}
	
	private static List<OrganisationRef> getViewOrganisations(Roles roles, boolean canView) {
		if (canView) {
			OrganisationModule organisationModule = CoreSpringFactory.getImpl(OrganisationModule.class);
			if (organisationModule.isEnabled()) {
				return roles.getOrganisationsWithRoles(QUALITY_VIEWER_ROLES);
			}
			return null; // null = all organisations
		}
		return Collections.emptyList(); // empty list = no organisations
	}
	
	private static List<OrganisationRef> getViewOnlyOrganisations(Roles roles, boolean canView) {
		if (canView) {
			OrganisationModule organisationModule = CoreSpringFactory.getImpl(OrganisationModule.class);
			if (organisationModule.isEnabled()) {
				return roles.getOrganisationsWithRoles(QUALITY_VIEWER_ONLY_ROLES);
			}
			return null; // null = all organisations
		}
		return Collections.emptyList(); // empty list = no organisations
	}
	
	private static List<OrganisationRef> getEditOrganisations(Roles roles, boolean canEdit) {
		if (canEdit) {
			OrganisationModule organisationModule = CoreSpringFactory.getImpl(OrganisationModule.class);
			if (organisationModule.isEnabled()) {
				return roles.getOrganisationsWithRoles(QUALITY_MANAGER_ROLES);
			}
			return null; // null = all organisations
		}
		return Collections.emptyList(); // empty list = no organisations
	}
	
	private static List<OrganisationRef> getLearnResourceManagerOrganisations(Roles roles) {
		OrganisationModule organisationModule = CoreSpringFactory.getImpl(OrganisationModule.class);
		if (organisationModule.isEnabled()) {
			return roles.getOrganisationsWithRoles(OrganisationRoles.learnresourcemanager);
		}
		return null; // all organisations
	}
	
	public static MainSecurityCallback createForbiddenSecurityCallback() {
		return FORBIDDEN_SECURITY_CALLBACK;
	}
	
	public static DataCollectionSecurityCallback createDataCollectionSecurityCallback(Roles roles,
			QualityDataCollectionLight dataCollection, Collection<? extends OrganisationRef> organisationRefs) {
		if (isQualityManager(roles, organisationRefs)) {
			return new DataCollectionStatusSecurityCallback(dataCollection.getStatus());
		}
		return DATA_COLLECTION_READ_ONLY_SECURITY_CALLBACK;
	}
	
	public static GeneratorSecurityCallback createGeneratorSecurityCallback(Roles roles, QualityGenerator generator,
			Collection<? extends OrganisationRef> organisationRefs) {
		if (isQualityManager(roles, organisationRefs)) {
			return new GeneratorStatusSecurityCallback(generator.isEnabled());
		}
		return GENERATOR_READ_ONLY_SECURITY_CALLBACK;
	}

	private static boolean isQualityManager(Roles roles, Collection<? extends OrganisationRef> organisationRefs) {
		OrganisationModule organisationModule = CoreSpringFactory.getImpl(OrganisationModule.class);
		if (organisationModule.isEnabled()) {
			for (OrganisationRef organisationRef : organisationRefs) {
				if (roles.hasSomeRoles(organisationRef, QUALITY_MANAGER_ROLES)) {
					return true;
				}
			}
		} else {
			return roles.isAdministrator() || roles.isQualityManager();
		}
		return false;
	}
}
