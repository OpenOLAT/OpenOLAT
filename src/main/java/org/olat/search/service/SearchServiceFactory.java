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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.search.service;

import org.olat.core.commons.services.search.SearchService;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * 
 * @author Christian Guretzki
 */
public class SearchServiceFactory {
	
	private static SearchService searchService_;
	private static FileDocumentFactory fileDocumentFactory;
		
	/**
	 * [used by spring]
	 */
	private SearchServiceFactory(SearchService searchService) {
		searchService_ = searchService;
		if (searchService.getSearchModuleConfig() != null) {
			fileDocumentFactory = new FileDocumentFactory(searchService.getSearchModuleConfig());
		}
	}


	public static SearchService getService() {
		return searchService_;
	}
	
	public static boolean isServiceEnabled() {
		return searchService_.isEnabled();
	}
	
  public static FileDocumentFactory getFileDocumentFactory() {
  	return fileDocumentFactory;
  }
}
