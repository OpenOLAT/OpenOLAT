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
package org.olat.ims.lti13.ui.events;

import org.olat.core.gui.control.Event;
import org.olat.ims.lti13.LTI13SharedTool;

/**
 * 
 * Initial date: 10 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddDeploymentEvent extends Event {

	private static final long serialVersionUID = 6324735007728816813L;

	public static final String ADD_DEPLOYMENT = "add-deployment";
	
	private final LTI13SharedTool sharedTool;
	
	public AddDeploymentEvent(LTI13SharedTool sharedTool) {
		super(ADD_DEPLOYMENT);
		this.sharedTool = sharedTool;
	}
	
	public LTI13SharedTool getSharedTool() {
		return sharedTool;
	}

}
