package org.olat.core.commons.services.vfs.ui.version;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 21 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCellRenderer implements FlexiCellRenderer {
	
	private final UserManager userManager;
	
	public IdentityCellRenderer(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Identity) {
			String fullName = userManager.getUserDisplayName((Identity)cellValue);
			if(fullName != null) {
				target.append(fullName);
			}
		}
	}
}
