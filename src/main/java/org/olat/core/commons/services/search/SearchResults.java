/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.services.search;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface SearchResults extends Serializable {
	public static final EmptySearchResults EMPTY_SEARCH_RESULTS = new EmptySearchResults();
	
	public String getLength();
	
	public List<ResultDocument> getList();
	
	public String getNumberOfIndexDocuments();
	
	/**
	 * Number of documents found before access rights are checked and scoring
	 * @return
	 */
	public int getTotalHits();
	
	/**
	 * Number of documents found before access rights are checked
	 * @return
	 */
	public int getTotalDocs();
	
}
