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
package org.olat.portfolio.model.artefacts;

import org.olat.portfolio.EPAbstractHandler;

/**
 * Initial Date:  6 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPStructureElementArtefactHandler extends EPAbstractHandler<EPStructureElementArtefact> {
	
	public static final String EP_STRUCTURE_ELEMENT_TYPE = "ep-structure-element";

	@Override
	public String getType() {
		return EP_STRUCTURE_ELEMENT_TYPE;
	}

	@Override
	public EPStructureElementArtefact createArtefact() {
		return new EPStructureElementArtefact();
	}
}
