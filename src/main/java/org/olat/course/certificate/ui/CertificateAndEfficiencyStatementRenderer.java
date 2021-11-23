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
package org.olat.course.certificate.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement;

/**
 * Initial date: 23.11.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CertificateAndEfficiencyStatementRenderer extends TreeNodeFlexiCellRenderer {
	
	public CertificateAndEfficiencyStatementRenderer() {
		super();
	}
	
	public CertificateAndEfficiencyStatementRenderer(boolean focusEnabled) {
		super(focusEnabled);
	}
	
	public CertificateAndEfficiencyStatementRenderer(String action) {
		super(action);
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		FlexiTableElementImpl ftE = source.getFlexiTableElement();
		FlexiTreeTableDataModel<?> treeTableModel = ftE.getTreeTableDataModel();
		
		if(treeTableModel != null) {
			if(isFlat(ftE)) {
				labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			} else {
				Object tableRow = treeTableModel.getObject(row);
				
				if (tableRow instanceof CertificateAndEfficiencyStatement) {
					if (((CertificateAndEfficiencyStatement) tableRow).isTaxonomy()) {
						renderIndented(renderer, target, cellValue, row, source, ubu, translator, true);
						return;
					}
				}
				
				renderIndented(renderer, target, cellValue, row, source, ubu, translator, false);
			}
		} else {
			labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
		}
	}

}
