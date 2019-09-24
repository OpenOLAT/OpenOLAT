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
package org.olat.portfolio.ui.structel;

import org.olat.core.gui.control.Event;
import org.olat.portfolio.model.structel.PortfolioStructure;

/**
 * Initial Date:  25.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPStructureChangeEvent extends Event {
	
	private static final long serialVersionUID = -7091171722782650074L;

	private PortfolioStructure portfolioStructure;

	public static final String ADDED = "added";
	public static final String REMOVED = "removed";
	public static final String CHANGED = "changed";
	public static final String SELECTED = "selected";

	public EPStructureChangeEvent(String command, PortfolioStructure portStruct) {
		super(command);
		this.portfolioStructure = portStruct;
	}
	
	/**
	 * @return Returns the portfolioStructure.
	 */
	public PortfolioStructure getPortfolioStructure() {
		return portfolioStructure;
	}
	
	@Override
	public boolean equals(Object obj) {
		// use same equals
		return super.equals(obj);
	}

}
