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
package org.olat.search.service;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * The SearchMetadataFieldsProvider provides a list of additional supported
 * metadaa fields that can be searched in the search engine. Not all search
 * documents might support all metadata elements.
 * 
 * <P>
 * Initial Date: 10.07.2009 <br>
 * 
 * @author gnaegi
 */
public interface SearchMetadataFieldsProvider {

	/**
	 * Get all metadata fields that are currently supported in OLAT that can be
	 * choosen from the drop down list in the advanced search form
	 * 
	 * @return
	 */
	public List<String> getAdvancedSearchableFields();

	/**
	 * Get all metadata fields that are used in the google-like fuzzy search
	 * form when the user does not use the advanced search
	 * 
	 * @return
	 */
	public List<String> getMultiFieldSearchFields();

	/**
	 * Create a translator that can translate the fields for the GUI
	 * @param locale
	 * @return a translator
	 */
	public Translator createFieldsTranslator(Locale locale);

}