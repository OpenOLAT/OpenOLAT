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
package org.olat.course.nodes.cl.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxConfigDataModel extends DefaultFlexiTableDataModel<CheckboxConfigRow> {
	
	private final Translator translator;
	
	public CheckboxConfigDataModel(Translator translator, FlexiTableColumnModel columnModel) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CheckboxConfigRow box = getObject(row);
		switch(Cols.values()[col]) {
			case title: return box.getTitle();
			case points: return box.getPoints();
			case release: {
				CheckboxReleaseEnum release = box.getRelease();
				if(release == null) {
					return "";
				}
				return translator.translate("release." + release.name());
			}
			case file: return box.getDownload();
			case up: return row == 0 ? Boolean.FALSE : Boolean.TRUE;
			case down: return row >= (getRowCount() - 1) ? Boolean.FALSE : Boolean.TRUE;
		}
		return box;
	}
	
	public enum Cols {
		title("checkbox.title"),
		points("points"),
		release("release"),
		file("file"),
		up("up"),
		down("down");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	

}
