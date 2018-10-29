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
package org.olat.modules.quality.analysis;

/**
 * 
 * Initial date: 24.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AvailableAttributes {
	
	private final boolean topicIdentity;
	private final boolean topicRepository;
	private final boolean topicOrganisation;
	private final boolean topicCurriculum;
	private final boolean topicCurriculumElement;
	private final boolean contextLocation;
	private final boolean contextExecutorOrganisation;
	private final boolean contextCurriculum;
	private final boolean contextCurriculumElement;
	private final boolean contextCurriculumOrganisation;
	private final boolean contextTaxonomyLevel;
	private final boolean seriesIndex;
	private final boolean dataCollection;

	public AvailableAttributes(boolean topicIdentity, boolean topicRepository, boolean topicOrganisation,
			boolean topicCurriculum, boolean topicCurriculumElement, Boolean contextLocation,
			boolean contextExecutorOrganisation, boolean contextCurriculum, boolean contextCurriculumElement,
			boolean contextCurriculumOrganisation, boolean contextTaxonomyLevel, boolean seriesIndex,
			boolean dataCollection) {
		this.topicIdentity = topicIdentity;
		this.topicRepository = topicRepository;
		this.topicOrganisation = topicOrganisation;
		this.topicCurriculum = topicCurriculum;
		this.topicCurriculumElement = topicCurriculumElement;
		this.contextLocation = contextLocation != null? contextLocation.booleanValue(): false;
		this.contextExecutorOrganisation = contextExecutorOrganisation;
		this.contextCurriculum = contextCurriculum;
		this.contextCurriculumElement = contextCurriculumElement;
		this.contextCurriculumOrganisation = contextCurriculumOrganisation;
		this.contextTaxonomyLevel = contextTaxonomyLevel;
		this.seriesIndex = seriesIndex;
		this.dataCollection = dataCollection;
	}

	public boolean isTopicIdentity() {
		return topicIdentity;
	}

	public boolean isTopicRepository() {
		return topicRepository;
	}

	public boolean isTopicOrganisation() {
		return topicOrganisation;
	}

	public boolean isTopicCurriculum() {
		return topicCurriculum;
	}

	public boolean isTopicCurriculumElement() {
		return topicCurriculumElement;
	}

	public boolean isContextLocation() {
		return contextLocation;
	}

	public boolean isContextExecutorOrganisation() {
		return contextExecutorOrganisation;
	}

	public boolean isContextCurriculum() {
		return contextCurriculum;
	}

	public boolean isContextCurriculumElement() {
		return contextCurriculumElement;
	}

	public boolean isContextCurriculumOrganisation() {
		return contextCurriculumOrganisation;
	}

	public boolean isContextTaxonomyLevel() {
		return contextTaxonomyLevel;
	}

	public boolean isSeriesIndex() {
		return seriesIndex;
	}

	public boolean isDataCollection() {
		return dataCollection;
	}

}
