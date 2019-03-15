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
package org.olat.modules.qpool.model;

import org.olat.search.model.OlatDocument;

/**
 * 
 * Initial date: 28.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QItemDocument extends OlatDocument {

	private static final long serialVersionUID = 2137366338712446727L;
	public static final String TYPE = "type.qp.item";
	
	public static final String OWNER_FIELD = "owner";
	public static final String SHARE_FIELD = "share";
	public static final String POOL_FIELD = "pool";
	public static final String TAXONOMIC_FIELD = "taxonomyid";
	public static final String TAXONOMIC_PATH_FIELD = "taxonomy";       // used in advanced search (subjects)
	public static final String TAXONOMIC_IDENTIFIER_FIELD = "taxonomy"; // used in full text search field

	public static final String IDENTIFIER_FIELD = "identifier";
	public static final String MASTER_IDENTIFIER_FIELD = "master";
	public static final String KEYWORDS_FIELD = "keywords";
	public static final String COVERAGE_FIELD = "coverage";
	public static final String ADD_INFOS_FIELD = "infos";
	public static final String LANGUAGE_FIELD = "language";
	public static final String EDU_CONTEXT_FIELD = "context";
	public static final String ITEM_TYPE_FIELD = "itemType";
	public static final String ASSESSMENT_TYPE_FIELD = "assessmentType";
	public static final String ITEM_VERSION_FIELD = "itemVersion";
	public static final String ITEM_STATUS_FIELD = "itemStatus";
	public static final String EDITOR_FIELD = "editor";
	public static final String EDITOR_VERSION_FIELD = "editorVersion";
	public static final String FORMAT_FIELD = "format";
	public static final String TOPIC_FIELD = "topic";
}
