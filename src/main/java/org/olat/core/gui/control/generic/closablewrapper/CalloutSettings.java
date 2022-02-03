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
package org.olat.core.gui.control.generic.closablewrapper;

/**
 * 
 * Initial date: 06.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalloutSettings {
	
	private final boolean arrow;
	private final boolean wider;
	private final CalloutOrientation orientation;
	
	public CalloutSettings() {
		this(true, CalloutOrientation.bottom, false);
	}
	
	public CalloutSettings(CalloutOrientation orientation) {
		this(true, orientation, false);
	}
	
	public CalloutSettings(boolean arrow) {
		this(arrow, CalloutOrientation.bottom, false);
	}
	
	public CalloutSettings(boolean arrow, CalloutOrientation orientation, boolean wider ) {
		this.orientation = orientation;
		this.arrow = arrow;
		this.wider = wider;
	}
	
	public boolean isArrow() {
		return arrow;
	}
	
	public boolean isWider() {
		return wider;
	}

	public CalloutOrientation getOrientation() {
		return orientation;
	}

	public enum CalloutOrientation {
		top,
		bottom
	}
}
