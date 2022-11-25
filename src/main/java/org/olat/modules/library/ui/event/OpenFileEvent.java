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
package org.olat.modules.library.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.library.model.CatalogItem;

/**
 * 
 * Description:<br>
 * Event to open a folder
 * 
 * <P>
 * Initial Date:  4 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenFileEvent extends Event {

	private static final long serialVersionUID = 2421331211851602677L;
	private final CatalogItem item;

	public OpenFileEvent(String cmd, CatalogItem item) {
		super(cmd);
		this.item = item;
	}
	
	public CatalogItem getItem() {
		return item;
	}
}
