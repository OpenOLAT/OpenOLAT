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
package org.olat.modules.curriculum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.curriculum.site.CurriculumManagementContextEntryControllerCreator;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String CURRICULUM_ENABLED = "curriculum.enabled";
	private static final String MANAGED_CURRICULUM_ENABLED = "curriculum.managed";
	private static final String CURRICULUM_IN_MY_COURSES_ENABLED = "curriculum.in.my.courses.enabled";
	private static final String USER_OVERVIEW_RIGHTS = "curriculum.user.overview.rights";
	private static final String LINKED_TAXONOMIES = "curriculum.linked.taxonomies";
	private static final String DEFAULT_COURSE_RUNTIME_TYPE = "curriculum.default.course.runtime.type";
	
	@Value("${curriculum.enabled:true}")
	private boolean enabled;
	@Value("${curriculum.in.my.courses.enabled:true}")
	private boolean curriculumInMyCourses;
	@Value("${curriculum.managed:true}")
	private boolean managedCurriculums;
	@Value("${curriculum.user.overview.rights:showCoursesAndCurriculum}")
	private String userOverviewRights;
	@Value("${curriculum.linked.taxonomies}")
	private String linkedTaxonomies;
	private List<TaxonomyRef> taxonomyRefs;
	@Value("${curriculum.default.course.runtime.type}")
	private String defaultCourseRuntimeType;
	
	@Autowired
	private CurriculumModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("CurriculumAdmin",
				new CurriculumManagementContextEntryControllerCreator(this));
		
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(CURRICULUM_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String enabledInMyCoursesObj = getStringPropertyValue(CURRICULUM_IN_MY_COURSES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledInMyCoursesObj)) {
			curriculumInMyCourses = "true".equals(enabledInMyCoursesObj);
		}
		
		String enabledTaxonomiesObj = getStringPropertyValue(LINKED_TAXONOMIES, true);
		if (StringHelper.containsNonWhitespace(enabledTaxonomiesObj)) {
			linkedTaxonomies = enabledTaxonomiesObj;
			taxonomyRefs = null;
		}
		
		userOverviewRights = getStringPropertyValue(USER_OVERVIEW_RIGHTS, userOverviewRights);

		defaultCourseRuntimeType = getStringPropertyValue(DEFAULT_COURSE_RUNTIME_TYPE, defaultCourseRuntimeType);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(CURRICULUM_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isCurriculumManaged() {
		return managedCurriculums;
	}
	
	public void setCurriculumManaged(boolean enabled) {
		managedCurriculums = enabled;
		setStringProperty(MANAGED_CURRICULUM_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCurriculumInMyCourses() {
		return curriculumInMyCourses;
	}

	public void setCurriculumInMyCourses(boolean curriculumInMyCourses) {
		this.curriculumInMyCourses = curriculumInMyCourses;
		setStringProperty(CURRICULUM_IN_MY_COURSES_ENABLED, Boolean.toString(curriculumInMyCourses), true);
	}
	
	public List<String> getUserOverviewRightList() {
		List<String> rightList = new ArrayList<>();
		String rights = getUserOverviewRights();
		if(StringHelper.containsNonWhitespace(rights)) {
			String[] rightsArr = rights.split("[,]");
			for(String right:rightsArr) {
				if(StringHelper.containsNonWhitespace(right)) {
					rightList.add(right);
				}
			}
		}
		return rightList;
	}
	
	public void setUserOverviewRightList(Collection<String> rights) {
		StringBuilder sb = new StringBuilder();
		if(rights != null && !rights.isEmpty()) {
			for(String right:rights) {
				if(StringHelper.containsNonWhitespace(right)) {
					if(sb.length() > 0) sb.append(",");
					sb.append(right);
				}
			}
		}
		setUserOverviewRights(sb.toString());
	}

	public String getUserOverviewRights() {
		return "oo_empty_oo".equals(userOverviewRights) ? null : userOverviewRights;
	}

	public void setUserOverviewRights(String rights) {
		if(!StringHelper.containsNonWhitespace(rights)) {
			rights = "oo_empty_oo";
		}
		this.userOverviewRights = rights;
		setStringProperty(USER_OVERVIEW_RIGHTS, rights, true);
	}
	
	public List<TaxonomyRef> getTaxonomyRefs() {
		if (taxonomyRefs == null) {
			if (StringHelper.containsNonWhitespace(linkedTaxonomies)) {
				taxonomyRefs = Arrays.stream(linkedTaxonomies.split(","))
					.filter(StringHelper::isLong)
					.map(Long::valueOf)
					.map(TaxonomyRefImpl::new)
					.collect(Collectors.toList());
			} else {
				taxonomyRefs = Collections.emptyList();
			}
		}
		return taxonomyRefs;
	}
	
	public void setLinkedTaxonomies(Collection<String> collection) {
		String linkedTaxonomies = collection.stream().collect(Collectors.joining(","));
		
		this.linkedTaxonomies = linkedTaxonomies;
		setStringProperty(LINKED_TAXONOMIES, linkedTaxonomies, true);
		taxonomyRefs = null;
	}

	public RepositoryEntryRuntimeType getDefaultCourseRuntimeType() {
		return RepositoryEntryRuntimeType.secureValueOf(defaultCourseRuntimeType, RepositoryEntryRuntimeType.curricular);
	}

	public void setDefaultCourseRuntimeType(RepositoryEntryRuntimeType value) {
		defaultCourseRuntimeType = value.name();
		setStringProperty(DEFAULT_COURSE_RUNTIME_TYPE, defaultCourseRuntimeType, true);
	}
}
