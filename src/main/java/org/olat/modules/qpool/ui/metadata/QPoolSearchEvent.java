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
package org.olat.modules.qpool.ui.metadata;

import org.olat.core.gui.control.Event;
import org.olat.modules.qpool.model.SearchQuestionItemParams;

/**
 * 
 * Initial date: 31 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QPoolSearchEvent extends Event {

	private static final long serialVersionUID = 331320339463397014L;
	public static final String QPOOL_EXTENDED_SEARCH = "qpool-extended-search";
	
	private final SearchQuestionItemParams params;
	
	public QPoolSearchEvent(SearchQuestionItemParams params) {
		super(QPOOL_EXTENDED_SEARCH);
		this.params = params;
	}
	
	public SearchQuestionItemParams getSearchParams() {
		return params;
	}
}
