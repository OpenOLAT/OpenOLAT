/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 23 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMemberStatusCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(CommitteeMemberStatus.ok ==  cellValue) {
			target.append("<span><i class='o_icon o_icon-lg o_icon_member_valid'> </i></span>");
		} else if(CommitteeMemberStatus.notValid == cellValue) {
			target.append("<span><i class='o_icon o_icon-lg o_icon_member_not_valid'> </i></span>");
		} else if(CommitteeMemberStatus.skipped == cellValue) {
			target.append("<span><i class='o_icon o_icon-lg o_icon_member_skipped'> </i></span>");
		}
	}
}
