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
package org.olat.catalog.restapi;

import org.olat.catalog.CatalogEntry;

/**
 * 
 * Description:<br>
 * Some examples for the documentation
 * 
 * <P>
 * Initial Date:  5 may 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class Examples {
	
	public static final CatalogEntryVO SAMPLE_CATALOGENTRYVO = new CatalogEntryVO();
	public static final CatalogEntryVOes SAMPLE_CATALOGENTRYVOes = new CatalogEntryVOes();

	static {
		SAMPLE_CATALOGENTRYVO.setKey(new Long(478l));
		SAMPLE_CATALOGENTRYVO.setName("Category");
		SAMPLE_CATALOGENTRYVO.setDescription("Description of the category");
		SAMPLE_CATALOGENTRYVO.setType(CatalogEntry.TYPE_NODE);
		
		SAMPLE_CATALOGENTRYVOes.setCatalogEntries(new CatalogEntryVO[]{SAMPLE_CATALOGENTRYVO});
		SAMPLE_CATALOGENTRYVOes.setTotalCount(0);
	}
}
