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
package org.olat.modules.quality.manager;

import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 02.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ForwardingQualityContextBuilder implements QualityContextBuilder {

	protected final DefaultQualityContextBuilder builder;

	public ForwardingQualityContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation) {
		super();
		this.builder = DefaultQualityContextBuilder.builder(dataCollection, evaluationFormParticipation);
	}

	@Override
	public QualityContextBuilder withRole(QualityContextRole role) {
		builder.withRole(role);
		return this;
	}

	@Override
	public QualityContextBuilder withLocation(String location) {
		builder.withLocation(location);
		return this;
	}

	@Override
	public QualityContextBuilder addToDelete(QualityContext context) {
		builder.addToDelete(context);
		return this;
	}

	public QualityContextBuilder addCurriculum(Curriculum curriculum) {
		builder.addCurriculum(curriculum);
		return this;
	}

	@Override
	public QualityContextBuilder addCurriculumElement(CurriculumElement curriculumElement) {
		builder.addCurriculumElement(curriculumElement);
		return this;
	}

	@Override
	public QualityContextBuilder addOrganisation(Organisation organisation) {
		builder.addOrganisation(organisation);
		return this;
	}

	@Override
	public QualityContextBuilder addTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		builder.addTaxonomyLevel(taxonomyLevel);
		return this;
	}

	@Override
	public QualityContext build() {
		return builder.build();
	}

}