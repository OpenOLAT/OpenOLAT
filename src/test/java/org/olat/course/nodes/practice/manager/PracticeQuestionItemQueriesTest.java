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
package org.olat.course.nodes.practice.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.nodes.practice.PracticeFilterRule.Operator;
import org.olat.course.nodes.practice.PracticeFilterRule.Type;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.PoolDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeQuestionItemQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private PracticeQuestionItemQueries practiceQuestionItemQueries;
	
	@Test
	public void searchItemsByKeywordsAndPool() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("practice-1");
		Pool pool = poolDao.createPool(id, "Practice search 1", true);

		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItemImpl item = questionItemDao.createAndPersist(id, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		item.setKeywords("004 005");
		item = questionItemDao.merge(item);
		poolDao.addItemToPool(item, List.of(pool), false);
		dbInstance.commitAndCloseSession();
		
		SearchPracticeItemParameters searchParams = new SearchPracticeItemParameters();
		List<PracticeFilterRule> rules = new ArrayList<>();
		rules.add(new PracticeFilterRule(Type.keyword, Operator.equals, "004"));
		rules.add(new PracticeFilterRule(Type.keyword, Operator.equals, "005"));
		searchParams.setRules(rules);

		List<QuestionItem> items = practiceQuestionItemQueries.searchItems(searchParams, null, List.of(pool), null, null);
		assertThat(items)
			.hasSize(1)
			.containsExactly(item);
	}

}
