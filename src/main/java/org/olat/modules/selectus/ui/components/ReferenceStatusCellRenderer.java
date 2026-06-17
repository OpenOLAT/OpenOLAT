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
package org.olat.modules.selectus.ui.components;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.reference.PositionReferenceRow;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ReferenceStatusCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {

	private Translator translator;
	private final StaticFlexiCellRenderer commentRenderer = new StaticFlexiCellRenderer("", "rcomments", "o_icon-lg o_icon_comment");
	
	public ReferenceStatusCellRenderer() {
		//
	}

	@Override
	public List<String> getActions() {
		return commentRenderer.getActions();
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(translator == null) {
			translator = Util.createPackageTranslator(PositionController.class, trans.getLocale());
		}
		
		if(cellValue instanceof ReferenceStatus) {
			render(target, (ReferenceStatus)cellValue, (ReferenceRequestStatus)null);
		}else if(cellValue instanceof PositionReferenceRow) {
			PositionReferenceRow refRow = (PositionReferenceRow)cellValue;
			Reference ref = refRow.getReference();
			if(refRow.isRefereeCommentAvailable()) {
				target.append("<div class='o_status_with_bubble'>");
				render(target, ref.getReferenceStatus(), ref.getRequestStatus());
				target.append(" ");
				commentRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
				target.append("</div>");
			} else {
				render(target, ref.getReferenceStatus(), ref.getRequestStatus());
			}
		} else if(cellValue instanceof Reference) {
			Reference ref = (Reference)cellValue;
			render(target, ref.getReferenceStatus(), ref.getRequestStatus());
		}
	}

	private void render(StringOutput sb, ReferenceStatus status, ReferenceRequestStatus requestStatus) {
		sb.append("<span>");
		if(status == ReferenceStatus.deactivated) {
			sb.append("<i class='o_icon o_reference_status_filter o_deactivated'> </i> ");
			sb.append(translator.translate("reference.status.".concat(status.name())));
		} else if(requestStatus == ReferenceRequestStatus.declined) {
			sb.append("<i class='o_icon o_reference_status_filter o_declined'> </i> ");
			sb.append(translator.translate("reference.status." + requestStatus));
		} else {
			switch(status) {
				case notSent:
					sb.append("<i class='o_icon o_reference_status_filter o_not_sent'> </i> ");
					sb.append(translator.translate("reference.status." + status));
					break;
				case sentAwaiting:
					if(requestStatus == ReferenceRequestStatus.accepted) {
						sb.append("<i class='o_icon o_reference_status_filter o_sent_awaiting_accepted'> </i> ");
						sb.append(translator.translate("reference.status.accepted." + status));
					} else {
						sb.append("<i class='o_icon o_reference_status_filter o_sent_awaiting'> </i> ");
						sb.append(translator.translate("reference.status." + status));
					}
					break;
				case submitted:
					sb.append("<i class='o_icon o_reference_status_filter o_submitted'> </i> ");
					sb.append(translator.translate("reference.status." + status));
					break;
				case late:
					sb.append("<i class='o_icon o_reference_status_filter o_late'> </i> ");
					sb.append(translator.translate("reference.status." + status));
					break;
				case deactivated:
					sb.append("<i class='o_icon o_reference_status_filter o_deactivated'> </i> ");
					sb.append(translator.translate("reference.status." + status));
					break;
			}
		}
		sb.append("</span>");
	}
}
