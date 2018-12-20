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

import java.util.Collections;
import java.util.List;

import org.olat.search.model.ResultDocument;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EmptySearchResults implements SearchResults {

	private static final long serialVersionUID = 9056000658199264982L;
	
	private Exception exception;
	
	public EmptySearchResults() {
		//
	}
	
	public EmptySearchResults(Exception exception) {
		this.exception = exception;
	}
	
	@Override
	public List<ResultDocument> getList() {
		return Collections.emptyList();
	}
	
	@Override
	public int size() {
		return 0;
	}

	@Override
	public String getNumberOfIndexDocuments() {
		return "0";
	}

	@Override
	public int getTotalHits() {
		return 0;
	}

	@Override
	public int getTotalDocs() {
		return 0;
	}

	@Override
	public Exception getException() {
		return exception;
	}
}
