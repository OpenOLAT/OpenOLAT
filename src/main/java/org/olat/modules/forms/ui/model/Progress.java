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
package org.olat.modules.forms.ui.model;

/**
 * 
 * Initial date: 22 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Progress {
	
	private static final Progress NONE = new Progress(0, 0);

	private final int current;
	private final int max;
	
	public static final Progress none() {
		return NONE;
	}
	
	public static final Progress of(int current, int max) {
		return new Progress(current, max);
	}

	private Progress(int current, int max) {
		this.current = current;
		this.max = max;
	}

	/**
	 *
	 * @returnThe value of the current progress
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 *
	 * @return The maximum possible progress
	 */
	public int getMax() {
		return max;
	}

}
