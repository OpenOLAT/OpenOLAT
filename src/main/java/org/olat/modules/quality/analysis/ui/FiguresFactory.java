/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.analysis.ui;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.FiguresBuilder;
import org.olat.modules.quality.analysis.AnlaysisFigures;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Sep 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FiguresFactory {
	
	public static Figures createFigures(Translator translator, RepositoryEntry formEntry, AnlaysisFigures analyticFigures, boolean withFormName) {
		FiguresBuilder figuresBuilder = FiguresBuilder.builder();
		if (withFormName) {
			figuresBuilder.addCustomFigure(
					translator.translate("report.figure.form.name"),
					translator.translate("report.figure.form.name.value",
							formEntry.getDisplayname(),
							String.valueOf(formEntry.getKey())));
		}
		
		figuresBuilder.withNumberOfParticipations(analyticFigures.getParticipationCount());
		if (analyticFigures.getPublicParticipationCount().longValue() > 0) {
			figuresBuilder.withNumberOfPublicParticipations(analyticFigures.getPublicParticipationCount());
		}
		
		figuresBuilder.addCustomFigure(translator.translate("report.figure.number.data.collections"),
				analyticFigures.getDataCollectionCount().toString());
		
		return figuresBuilder.build();
	}

}
