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
package org.olat.modules.quality.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.quality.QualityDataCollection;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionEvent extends Event {

	private static final long serialVersionUID = -6758258801303070361L;
	
	public enum Action {
		CHANGED,
		DELETE
	}

	private final QualityDataCollection dataCollection;
	private final Action action;

	public DataCollectionEvent(QualityDataCollection dataCollection, Action action) {
		super("data-collection-event");
		this.dataCollection = dataCollection;
		this.action = action;
	}

	public QualityDataCollection getDataCollection() {
		return dataCollection;
	}

	public Action getAction() {
		return action;
	}

}
