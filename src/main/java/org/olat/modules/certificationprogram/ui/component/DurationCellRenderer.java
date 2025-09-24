/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui.component;

import java.io.IOException;
import java.io.Writer;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DurationCellRenderer implements FlexiCellRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(DurationCellRenderer.class);
	
	private final Translator translator;
	
	public DurationCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof Duration duration) {
			toString(target, duration, translator);
		}
	}
	
	public static final String toString(Duration duration, Translator transl) {
		StringOutput sb = new StringOutput();
		toString(sb, duration, transl);
		return sb.toString();
	}
	
	public static final void toString(Writer sb, Duration duration, Translator transl) {
		try {
			sb.append(Integer.toString(duration.value()))
			  .append(" ");
			
			if(duration.value() <= 1) {
				switch(duration.unit()) {
					case day: sb.append(transl.translate("unit.day")); break;
					case week: sb.append(transl.translate("unit.week")); break;
					case month: sb.append(transl.translate("unit.month")); break;
					case year: sb.append(transl.translate("unit.year")); break;
				}
			} else {
				switch(duration.unit()) {
					case day: sb.append(transl.translate("unit.days")); break;
					case week: sb.append(transl.translate("unit.weeks")); break;
					case month: sb.append(transl.translate("unit.months")); break;
					case year: sb.append(transl.translate("unit.years")); break;
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
