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
package org.olat.portfolio.ui.artefacts.view;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.filter.PortfolioFilterController;

/**
 * Description:<br>
 * presents image for artefact type in table
 * 
 * <P>
 * Initial Date:  02.12.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ArtefactTypeImageCellRenderer implements CustomCellRenderer {
	
	private final PortfolioModule portfolioModule;
	private final Translator translator;
	
	public ArtefactTypeImageCellRenderer(Locale locale) {
		portfolioModule = CoreSpringFactory.getImpl(PortfolioModule.class);
		translator = Util.createPackageTranslator(ArtefactTypeImageCellRenderer.class, locale);
	}

	/**
	 * @see org.olat.core.gui.components.table.CustomCellRenderer#render(org.olat.core.gui.render.StringOutput, org.olat.core.gui.render.Renderer, java.lang.Object, java.util.Locale, int, java.lang.String)
	 */
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if (val instanceof AbstractArtefact){
			AbstractArtefact artefact = (AbstractArtefact) val;
			EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
			
			Translator handlerTrans = artHandler.getHandlerTranslator(translator);
			String handlerClass = PortfolioFilterController.HANDLER_PREFIX + artHandler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
			String artType = handlerTrans.translate(handlerClass);
			String artIcon = artefact.getIcon();
			
			sb.append("<i class='o_icon o_icon-lg ").append(artIcon)
			  .append("' title=\"").append(artType)
			  .append("\"> </i>");
		}
	}
}