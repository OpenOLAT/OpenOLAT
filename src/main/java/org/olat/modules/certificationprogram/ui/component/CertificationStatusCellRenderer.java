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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.certificationprogram.ui.CertificationIdentityStatus;
import org.olat.modules.certificationprogram.ui.CertificationStatus;

/**
 * 
 * Initial date: 12 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public CertificationStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof CertificationStatus status) {
			getStatus(target, "o_labeled_light", status, translator);
		} else if(cellValue instanceof CertificationIdentityStatus status) {
			getStatus(target, "o_labeled_light", status, translator);
		}
	}
	
	public static final void getStatus(StringOutput target, String type, Enum<?> status, Translator trans) {
		String name = status.name().toLowerCase();
		target.append("<span class=\"").append(type).append(" o_certification_status_")
		      .append(name).append("\">")
		      .append("<i class=\"o_icon o_icon-fw o_icon_certification_status_").append(name).append("\"> </i> ")
		      .append(trans.translate("certification.status.".concat(name)))
		      .append("</span>");
	}

}
