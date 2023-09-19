/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.ui.events;

import org.olat.core.gui.control.Event;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ToolDeployment;

/**
 * 
 * Initial date: 13 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LTI13ContentItemStartEvent extends Event {
	
	private static final long serialVersionUID = 4125661626593759418L;

	public static final String START = "lti-13-content-item-start";
	
	private final LTI13ToolDeployment deployment;
	private final LTI13ContentItem contentItem;
	
	public LTI13ContentItemStartEvent(LTI13ToolDeployment deployment, LTI13ContentItem contentItem) {
		super(START);
		this.deployment = deployment;
		this.contentItem = contentItem;
	}

	public LTI13ToolDeployment getDeployment() {
		return deployment;
	}

	public LTI13ContentItem getContentItem() {
		return contentItem;
	}
}
