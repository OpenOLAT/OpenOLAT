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
package org.olat.modules.quality.analysis.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityAnalysisServiceImpl implements QualityAnalysisService {

	@Autowired
	private AnalysisFilterDAO filterDao;
	@Autowired
	private EvaluationFormDAO evaluationFromDao;
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public List<EvaluationFormView> loadEvaluationForms(EvaluationFormViewSearchParams searchParams) {
		return evaluationFromDao.load(searchParams);
	}

	@Override
	public List<Organisation> loadFilterOrganisations(AnalysisSearchParameter searchParams) {
		return filterDao.loadOrganisations(searchParams);
	}

	@Override
	public List<Curriculum> loadFilterCurriculums(AnalysisSearchParameter searchParams) {
		return filterDao.loadCurriculums(searchParams);
	}

	@Override
	public List<CurriculumElement> loadFilterCurriculumElements(AnalysisSearchParameter searchParams) {
		if (searchParams == null || searchParams.getCurriculumRefs() == null) {
			return new ArrayList<>(0);
		}

		List<CurriculumElement> elementsOfCurriculums = curriculumService
				.getCurriculumElementsByCurriculums(searchParams.getCurriculumRefs());
		List<String> pathes = filterDao.loadCurriculumElementPathes(searchParams);
		elementsOfCurriculums.removeIf(e -> isUnusedLeaf(e, pathes));
		return elementsOfCurriculums;
	}

	private boolean isUnusedLeaf(CurriculumElement e, List<String> pathsOfContexts) {
		for (String path : pathsOfContexts) {
			if (path.contains(e.getMaterializedPathKeys())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Long loadFilterDataCollectionCount(AnalysisSearchParameter searchParams) {
		return filterDao.loadFilterDataCollectionCount(searchParams);
	}
}
