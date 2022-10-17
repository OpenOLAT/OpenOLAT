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

import jakarta.persistence.Query;

import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;

/**
 * 
 * Initial date: 11.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisSessionFilter implements SessionFilter {

	private final AnalysisSearchParameter searchParams;

	public AnalysisSessionFilter(AnalysisSearchParameter searchParams) {
		this.searchParams = searchParams;
	}

	@Override
	public String getSelectKeys() {
		QueryBuilder sb = new QueryBuilder();
		AnalysisFilterDAO.appendSelectSessionKeys(sb, searchParams);
		return sb.toString();
	}

	@Override
	public void addParameters(Query query) {
		AnalysisFilterDAO.appendParameters(query, searchParams);
	}

}
