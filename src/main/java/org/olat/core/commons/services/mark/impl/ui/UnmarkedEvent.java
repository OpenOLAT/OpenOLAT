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
package org.olat.core.commons.services.mark.impl.ui;

import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 5 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UnmarkedEvent extends Event {

	private static final long serialVersionUID = 3503240359856342650L;

	private final OLATResourceable ores;
	private final String subPath;

	public UnmarkedEvent(OLATResourceable ores, String subPath) {
		super("unmarked");
		this.ores = ores;
		this.subPath = subPath;
	}

	public OLATResourceable getOres() {
		return ores;
	}

	public String getSubPath() {
		return subPath;
	}

}
