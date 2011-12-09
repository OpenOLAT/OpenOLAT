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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.util.event;

/**
 * Description:<br>
 * When registering on this channel you get an FrameworkStartedEvent over the genericEventListener interface.
 * You have to check the event that it is from the same JVM otherwise you may process event several times (when in a cluster)
 * You can do this like: 
 * if (event instanceof FrameworkStartedEvent && ((FrameworkStartedEvent) event).isEventOnThisNode()) {... 
 * 
 * <P>
 * Initial Date:  25.06.2010 <br>
 * @author guido
 */
public class FrameworkStartedEvent extends MultiUserEvent {

	private static final long serialVersionUID = -578253098912838822L;

	protected FrameworkStartedEvent() {
		super("frameworkStartedEvent");
	}


}
