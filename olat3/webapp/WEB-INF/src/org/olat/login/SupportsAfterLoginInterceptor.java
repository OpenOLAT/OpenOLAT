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
package org.olat.login;

/**
 * Description:<br>
 * Marker Interface only. Controllers which are intended for use with
 * AfterLoginInterceptor-mechanism, implement this.
 * 
 * They have to send an Event.DONE_EVENT in order to let the workflow continue.
 * 
 * <P>
 * Initial Date: 25.11.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public interface SupportsAfterLoginInterceptor {
	// marker only
}
