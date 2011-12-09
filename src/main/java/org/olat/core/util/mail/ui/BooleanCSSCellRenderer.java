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
package org.olat.core.util.mail.ui;

import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  25 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BooleanCSSCellRenderer extends CustomCssCellRenderer {
	
	private String trueCss;
	private String falseCss;
	private String trueHoverText;
	private String falseHoverText;
	
	private final Translator translator;
	
	public BooleanCSSCellRenderer(Translator translator, String trueCss, String falseCss,
			String trueHoverText, String falseHoverText) {
		this.trueCss = trueCss;
		this.falseCss = falseCss;
		this.trueHoverText = trueHoverText;
		this.falseHoverText = falseHoverText;
		this.translator = translator;
	}

	@Override
	protected String getCssClass(Object val) {
		if(val instanceof Boolean) {
			return ((Boolean)val).booleanValue() ? trueCss : falseCss;
		}
		return "";
	}

	@Override
	protected String getCellValue(Object val) {
		return "";
	}

	@Override
	protected String getHoverText(Object val) {
		if(val instanceof Boolean) {
			return ((Boolean)val).booleanValue() ? translator.translate(trueHoverText) : translator.translate(falseHoverText);
		}
		return "";
	}
}
