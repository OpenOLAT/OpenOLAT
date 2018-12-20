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

package org.olat.search;

import java.io.Serializable;
import java.util.List;

import org.olat.search.model.ResultDocument;

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
	
	public int size();
	
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
	
	public Exception getException();
	
}
