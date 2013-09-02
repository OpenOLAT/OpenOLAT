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
package de.bps.course.nodes.den;

import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * 
 * Wrap a strong tag around the value
 * 
 * Initial date: 02.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StrongColumnDescriptor extends DefaultColumnDescriptor {
	
	public StrongColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale) {
		super(headerKey, dataColumn, action, locale);
	}

	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		sb.append("<strong>");
		super.renderValue(sb, row, renderer);
		sb.append("</strong>");
	}
	
	

}
