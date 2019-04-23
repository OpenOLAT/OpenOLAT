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
package org.olat.modules.adobeconnect.ui;

import org.olat.core.gui.control.Event;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;

/**
 * 
 * Initial date: 23 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectErrorEvent extends Event {
	private static final long serialVersionUID = -798524427050432858L;

	private static final String ERROR = "adobe-connect-error-event";
	
	private final AdobeConnectErrors errors;
	
	public AdobeConnectErrorEvent(AdobeConnectErrors errors) {
		super(ERROR);
		this.errors = errors;
	}
	
	public AdobeConnectErrors getErrors() {
		return errors;
	}

}
