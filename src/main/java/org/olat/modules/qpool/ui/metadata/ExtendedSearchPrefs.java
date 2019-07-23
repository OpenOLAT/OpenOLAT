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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 03.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtendedSearchPrefs implements Serializable {

	private static final long serialVersionUID = -1531232270649506181L;
	private List<ExtendedSearchPref> condQueries = new ArrayList<>();

	public List<ExtendedSearchPref> getCondQueries() {
		return condQueries;
	}

	public void setCondQueries(List<ExtendedSearchPref> condQueries) {
		this.condQueries = condQueries;
	}
}
