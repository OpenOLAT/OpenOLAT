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
package org.olat.portfolio.ui.artefacts.collect;

import org.olat.core.gui.control.Event;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * event sent on changed reflexion delivering reflexion itself
 * 
 * <P>
 * Initial Date:  19.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPReflexionChangeEvent extends Event {

	private static final long serialVersionUID = -2751202942774501947L;
	private String refContent;
	private AbstractArtefact refArtefact;
	
	public EPReflexionChangeEvent(String reflexion, AbstractArtefact artefact) {
		super("reflexionchanged");
		setReflexion(reflexion);
		setRefArtefact(artefact);
	}

	public String getReflexion() {
		return refContent;
	}

	public void setReflexion(String reflexion) {
		this.refContent = reflexion;
	}

	public AbstractArtefact getRefArtefact() {
		return refArtefact;
	}

	public void setRefArtefact(AbstractArtefact refArtefact) {
		this.refArtefact = refArtefact;
	}
	
}
