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
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  11 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPStructureEvent extends Event {

	private static final long serialVersionUID = 1732568799650825946L;
	public static final String SELECT = "select";
	public static final String SELECT_WITH_COMMENTS = "selectWithComments";
	public static final String CLOSE = "close";
	public static final String CHANGE = "change";
	public static final String SUBMIT = "submit";
	
	private final PortfolioStructure structure;
	
	public EPStructureEvent(String command, PortfolioStructure structure) {
		super(command);
		this.structure = structure;
	}

	public PortfolioStructure getStructure() {
		return structure;
	}
	
	@Override
	public boolean equals(Object obj) {
		// use same equals
		return super.equals(obj);
	}
}
