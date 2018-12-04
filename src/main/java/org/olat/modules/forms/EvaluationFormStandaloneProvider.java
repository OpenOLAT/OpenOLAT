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
package org.olat.modules.forms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;

/**
 * Each module which uses an evaluation form can implement this provider to publish the form in the standalone environment.
 * 
 * Initial date: 23 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface EvaluationFormStandaloneProvider {
	
	/**
	 * Only the fallback provider should override this method.
	 *
	 * @return
	 */
	public default boolean isFallbackProvider() {
		return false;
	}
	
	/**
	 * Does the provider accept the OLATResourceable referenced in the evaluation form survey?
	 *
	 * @param ores
	 * @return
	 */
	public boolean accept(OLATResourceable ores);
	
	/**
	 * The provider may make his own decision if a participation is executable.
	 *
	 * @param participation
	 * @return
	 */
	public boolean isExecutable(EvaluationFormParticipation participation);
	
	/**
	 * 
	 *
	 * @param ureq
	 * @param wControl
	 * @param participation
	 * @return
	 */
	public Controller getExecutionHeader(UserRequest ureq, WindowControl wControl, EvaluationFormParticipation participation);

	/**
	 * Returns whether the provider is able to deliver a business path to launch by authenticated users.
	 *
	 * @param participation
	 * @return
	 */
	public boolean hasBusinessPath(EvaluationFormParticipation participation);

	/**
	 * Get the business path to launch the participation by authenticated users.
	 *
	 * @param participation
	 * @return
	 */
	public String getBusinessPath(EvaluationFormParticipation participation);

}
