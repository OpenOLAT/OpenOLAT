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
package org.olat.modules.cemedia;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class MediaModule extends AbstractSpringModule {
	
	private static final String TAXONOMY_TREE_KEY = "taxonomy.tree.key";
	private static final String SHARE_WITH_USER = "media.center.share.with.user";
	private static final String SHARE_WITH_GROUP = "media.center.share.with.group";
	private static final String SHARE_WITH_COURSE = "media.center.share.with.course";
	private static final String SHARE_WITH_ORGANISATION = "media.center.share.with.organisation";
	private static final String CONFIG_FORCE_LICENSE_CHECK = "media.force.license.check";
	
	private String taxonomyTreeKey;
	private List<TaxonomyRef> taxonomyRefs;
	
	@Value("${media.center.share.with.user}")
	private String shareWithUser;
	@Value("${media.center.share.with.group}")
	private String shareWithGroup;
	@Value("${media.center.share.with.course}")
	private String shareWithCourse;
	@Value("${media.center.share.with.organisation}")
	private String shareWithOrganisation;
	@Value("${media.force.license.check:false}")
	private boolean forceLicenseCheck;
	
	@Autowired
	private RepositoryModule repositoryModule;
	
	@Autowired
	public MediaModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		String taxonomyTreeKeyObj = getStringPropertyValue(TAXONOMY_TREE_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyTreeKeyObj)) {
			taxonomyTreeKey = taxonomyTreeKeyObj;
			taxonomyRefs = null;
		}
		
		shareWithUser = getStringPropertyValue(SHARE_WITH_USER, shareWithUser);
		shareWithGroup = getStringPropertyValue(SHARE_WITH_GROUP, shareWithGroup);
		shareWithCourse = getStringPropertyValue(SHARE_WITH_COURSE, shareWithCourse);
		shareWithOrganisation = getStringPropertyValue(SHARE_WITH_ORGANISATION, shareWithOrganisation);

		String forceLicenseObj = getStringPropertyValue(CONFIG_FORCE_LICENSE_CHECK, true);
		if (StringHelper.containsNonWhitespace(forceLicenseObj)) {
			forceLicenseCheck = "true".equals(forceLicenseObj);
		}
	}
	
	public List<TaxonomyRef> getTaxonomyRefs() {
		return getTaxonomyRefs(true);
	}
	
	public boolean isTaxonomyLinked(Long taxonomyKey, boolean fallback) {
		List<TaxonomyRef> taxonomies = getTaxonomyRefs(fallback);
		for (TaxonomyRef taxonomy : taxonomies) {
			if (taxonomy.getKey().equals(taxonomyKey)) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized List<TaxonomyRef> getTaxonomyRefs(boolean fallback) {
		if (taxonomyRefs == null && StringHelper.containsNonWhitespace(taxonomyTreeKey)) {
			taxonomyRefs = Arrays.stream(taxonomyTreeKey.split(","))
				.filter(StringHelper::isLong)
				.map(Long::valueOf)
				.map(TaxonomyRefImpl::new)
				.map(TaxonomyRef.class::cast)
				.toList();
		}
		if((taxonomyRefs == null || taxonomyRefs.isEmpty()) && fallback) {
			return repositoryModule.getTaxonomyRefs();		
		}
		return taxonomyRefs == null ? List.of() : taxonomyRefs;
	}
	
	public synchronized void setTaxonomyRefs(List<TaxonomyRef> taxonomyRefs) {
		this.taxonomyTreeKey = taxonomyRefs != null && !taxonomyRefs.isEmpty()
				? taxonomyRefs.stream().map(TaxonomyRef::getKey).map(String::valueOf).collect(Collectors.joining(","))
				: null;
		setStringProperty(TAXONOMY_TREE_KEY, taxonomyTreeKey, true);
		this.taxonomyRefs = null;
	}

	public String getShareWithUser() {
		return shareWithUser;
	}
	
	public List<OrganisationRoles> getRolesAllowedToShareWithUser() {
		return toRoles(getShareWithUser()); 
	}
	
	public boolean isAllowedToShareWithUser(Roles roles) {
		return isAllowedToShare(roles, getRolesAllowedToShareWithUser()); 
	}

	public void setShareWithUser(String shareWithUser) {
		this.shareWithUser = shareWithUser;
		setStringProperty(SHARE_WITH_USER, shareWithUser, true);
	}

	public String getShareWithGroup() {
		return shareWithGroup;
	}
	
	public List<OrganisationRoles> getRolesAllowedToShareWithGroup() {
		return toRoles(getShareWithGroup()); 
	}
	
	public boolean isAllowedToShareWithGroup(Roles roles) {
		return isAllowedToShare(roles, getRolesAllowedToShareWithGroup()); 
	}

	public void setShareWithGroup(String shareWithGroup) {
		this.shareWithGroup = shareWithGroup;
		setStringProperty(SHARE_WITH_GROUP, shareWithGroup, true);
	}

	public String getShareWithCourse() {
		return shareWithCourse;
	}
	
	public List<OrganisationRoles> getRolesAllowedToShareWithCourse() {
		return toRoles(getShareWithCourse()); 
	}
	
	public boolean isAllowedToShareWithCourse(Roles roles) {
		return isAllowedToShare(roles, getRolesAllowedToShareWithCourse()); 
	}

	public void setShareWithCourse(String shareWithCourse) {
		this.shareWithCourse = shareWithCourse;
		setStringProperty(SHARE_WITH_COURSE, shareWithCourse, true);
	}

	public String getShareWithOrganisation() {
		return shareWithOrganisation;
	}
	
	public List<OrganisationRoles> getRolesAllowedToShareWithOrganisation() {
		return toRoles(getShareWithOrganisation()); 
	}
	
	public boolean isAllowedToShareWithOrganisation(Roles roles) {
		return isAllowedToShare(roles, getRolesAllowedToShareWithOrganisation()); 
	}

	public void setShareWithOrganisation(String shareWithOrganisation) {
		this.shareWithOrganisation = shareWithOrganisation;
		setStringProperty(SHARE_WITH_ORGANISATION, shareWithOrganisation, true);
	}
	
	private boolean isAllowedToShare(final Roles roles, List<OrganisationRoles> permittedList) {
		if(permittedList == null || permittedList.isEmpty()) {
			return false;
		}
		return permittedList.stream().anyMatch(roles::hasRole);
	}

	public boolean isForceLicenseCheck() {
		return forceLicenseCheck;
	}

	public void setForceLicense(boolean forceLicenseCheck) {
		this.forceLicenseCheck = forceLicenseCheck;
		setBooleanProperty(CONFIG_FORCE_LICENSE_CHECK, forceLicenseCheck, true);
	}
	
	private List<OrganisationRoles> toRoles(String val) {
		String[] values = val.split(",");
		return OrganisationRoles.arrayToValues(values);
	}
}
