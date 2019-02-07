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
package org.olat.ims.qti21.ui.editor.overview;

/**
 * 
 * Initial date: 7 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OptionAndInheritance {
	
	private final OptionEnum option;
	private final OptionEnum inheritedValue;
	
	private OptionAndInheritance(OptionEnum option, OptionEnum inheritedValue) {
		this.option = option;
		this.inheritedValue = inheritedValue;
	}
	
	public static OptionAndInheritance yes() {
		return new OptionAndInheritance(OptionEnum.yes, null);
	}
	
	public static OptionAndInheritance no() {
		return new OptionAndInheritance(OptionEnum.no, null);
	}
	
	public static OptionAndInheritance inherited(OptionEnum inheritedValue) {
		return new OptionAndInheritance(OptionEnum.inherited, inheritedValue);
	}

	public OptionEnum getOption() {
		return option;
	}

	public OptionEnum getInheritedValue() {
		return inheritedValue;
	}
}
