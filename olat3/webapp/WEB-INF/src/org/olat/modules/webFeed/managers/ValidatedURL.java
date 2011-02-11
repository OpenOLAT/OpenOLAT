/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed.managers;

/**
 * <P>
 * Initial Date:  25 feb. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ValidatedURL {
	private final String url;
	private final State state;
	
	public ValidatedURL(String url, State state) {
		this.url = url;
		this.state = state;
	}
	
	public String getUrl() {
		return url;
	}

	public State getState() {
		return state;
	}

	public enum State {
		VALID,
		NO_ENCLOSURE,
		NOT_FOUND,
		MALFORMED
	}
}
