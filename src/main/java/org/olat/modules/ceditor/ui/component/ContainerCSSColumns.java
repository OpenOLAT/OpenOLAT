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
package org.olat.modules.ceditor.ui.component;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 12 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerCSSColumns {
	
	public static List<String> getCssColumns(int columns) {
		switch(columns) {
			case 1: return getCssColumns("col-xs-12", 1);
			case 2: return getCssColumns("col-md-6 col-xs-12", 2);
			case 3: return getCssColumns("col-md-4 col-xs-12", 3);
			case 4: return getCssColumns("col-md-3 col-xs-12", 4);
			case 5: return getCssColumns("col-md-2 col-xs-12", 5);
			case 6: return getCssColumns("col-md-2 col-xs-12", 6);
			default: return getCssColumns("col-md-1 col-xs-12", columns);
		}
	}
	
	private static List<String> getCssColumns(String cssClass, int columns) {
		List<String> cssColumns = new ArrayList<>(columns);
		for(int i=0; i<columns; i++) {
			cssColumns.add(cssClass);
		}
		return cssColumns;
	}

}
