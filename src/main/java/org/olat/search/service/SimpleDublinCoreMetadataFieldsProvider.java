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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * The simple dublin core metadata lists all supported dublin core metadata
 * elements that can be searched. Note that this is not supported for all search
 * type documents.
 * 
 * <P>
 * Initial Date: 10.07.2009 <br>
 * 
 * @author gnaegi
 */
public class SimpleDublinCoreMetadataFieldsProvider implements SearchMetadataFieldsProvider {
	public static final String DC_TITLE = "DC.title";
	public static final String DC_CREATOR =	"DC.creator";
	public static final String DC_SUBJECT = "DC.Subject";
	public static final String DC_DESCRIPTION = "DC.description";
	public static final String DC_PUBLISHER	= "DC.publisher";
	public static final String DC_CONTRIBUTOR	= "DC.contributor";
	public static final String DC_DATE	= "DC.date";
	public static final String DC_TYPE	= "DC.type";
	public static final String DC_FORMAT = "DC.format";
	public static final String DC_IDENTIFIER	= "DC.identifier";
	public static final String DC_SOURCE	= "DC.source";
	public static final String DC_LANGUAGE	= "DC.language";
	public static final String DC_RELATION	= "DC.relation";
	public static final String DC_COVERAGE	= "DC.coverage";
	public static final String DC_RIGHTS = "DC.rights";

	private static final List<String> searchableFields;
	private static final List<String> multiFieldSearchFields;

	static {
		searchableFields = new ArrayList<>();
		// add only the ones that are supported by OLAT
		searchableFields.add(DC_TITLE);
		searchableFields.add(DC_DESCRIPTION);
		searchableFields.add(DC_CREATOR);
		searchableFields.add(DC_PUBLISHER);
		searchableFields.add(DC_SOURCE);
		searchableFields.add(DC_LANGUAGE);
		searchableFields.add(DC_DATE);
		searchableFields.add(DC_FORMAT);

		multiFieldSearchFields = new ArrayList<>();
		multiFieldSearchFields.add(DC_TITLE);
		multiFieldSearchFields.add(DC_CREATOR);
		multiFieldSearchFields.add(DC_DESCRIPTION);
		multiFieldSearchFields.add(DC_PUBLISHER);
		multiFieldSearchFields.add(DC_SOURCE);		
	}

	/**
	 * Get all dublin core fields that are currently supported in OLAT that can
	 * be choosen from the drop down
	 * 
	 * @return
	 */
	public List<String> getAdvancedSearchableFields() {
		return searchableFields;
	}

	/**
	 * Get all dublin core fields that are used in the google-like fuzzy search
	 * form when the user does not use the advanced search
	 * 
	 * @return
	 */
	public List<String> getMultiFieldSearchFields() {
		return multiFieldSearchFields;
	}
	
	/**
	 * @see org.olat.search.service.SearchMetadataFieldsProvider#createFieldsTranslator(java.util.Locale)
	 */
	public Translator createFieldsTranslator(Locale locale) {
		return Util.createPackageTranslator(SimpleDublinCoreMetadataFieldsProvider.class, locale);
	}
}
