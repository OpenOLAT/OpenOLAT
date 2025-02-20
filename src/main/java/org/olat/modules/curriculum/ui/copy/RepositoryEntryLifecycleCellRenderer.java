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
package org.olat.modules.curriculum.ui.copy;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 19 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryLifecycleCellRenderer implements FlexiCellRenderer {
	
	private final Formatter formatter;
	
	public RepositoryEntryLifecycleCellRenderer(Locale locale) {
		formatter = Formatter.getInstance(locale);
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		
		if(cellValue instanceof RepositoryEntryLifecycle lifecycle) {
			render(target, lifecycle);
		}
	}
	
	private void render(StringOutput target, RepositoryEntryLifecycle lifecycle) {
		Date validFrom = lifecycle.getValidFrom();
		if(validFrom != null) {
			target.append(formatter.formatDate(validFrom));
		}
		if(lifecycle.getValidTo() != null && (validFrom == null || !DateUtils.isSameDay(validFrom, lifecycle.getValidTo()))) {
			if(validFrom != null) {
				target.append(" - ");
			}
			target.append(formatter.formatDate(lifecycle.getValidTo()));
		}
	}
}
