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
package org.olat.modules.qpool.ui.events;

import org.olat.core.gui.control.Event;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * Event to create a new item
 * 
 * Initial date: 26.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QItemCreationCmdEvent extends Event {

	private static final long serialVersionUID = -8559321741155502634L;
	public static final String CREATE_NEW_ITEM_CMD = "createNewItemAsap";
	
	private final String title;
	private final TaxonomyLevel taxonomyLevel;
	private final QItemFactory factory;

	public QItemCreationCmdEvent(String title, TaxonomyLevel taxonomyLevel, QItemFactory factory) {
		super(CREATE_NEW_ITEM_CMD);
		this.title = title;
		this.taxonomyLevel = taxonomyLevel;
		this.factory = factory;
	}

	public String getTitle() {
		return title;
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public QItemFactory getFactory() {
		return factory;
	}
}