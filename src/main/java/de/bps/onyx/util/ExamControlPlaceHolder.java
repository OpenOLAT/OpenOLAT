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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.util;

/**
 * This class holds references to the used implementations for the {@link ExamPoolManager}  [either {@link ExamPoolManagerServer} or {@link ExamPoolManagerProxy}] that are initiated per spring.
 * 
 * This is basically a dummy but necessary for spring.
 */
public class ExamControlPlaceHolder {

	private final Object argument;

	public ExamControlPlaceHolder(Object arg) {
		this.argument = arg;
	}
}
/*
history:

$Log: ExamControlPlaceHolder.java,v $
Revision 1.3  2012-04-05 14:07:09  blaw
OLATCE-1425
* added history


*/