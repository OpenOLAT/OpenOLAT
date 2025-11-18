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
package org.olat.modules.cemedia.ui.component;

import java.util.Date;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.cemedia.Citation;
import org.olat.modules.cemedia.ui.MediaCentersController;

/**
 * 
 * Initial date: 21.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationComponentRenderer extends DefaultComponentRenderer {

	private Translator getTranslator(Translator fallbackTranslator) {
		return Util.createPackageTranslator(
				MediaCentersController.class, fallbackTranslator.getLocale(), fallbackTranslator);
	}
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		CitationComponent cmp = (CitationComponent)source;
		String notes = null;
		String links = null;
		renderApa(sb, translator, cmp.getCitation(), cmp.getDublinCoreMetadata(), notes, links);
	}
	
	public void renderApa(StringOutput sb, Translator translator, Citation citation, DublinCoreMetadata dcData, String notes, String links) {
		Translator ownTranslator = getTranslator(translator);

		String authors = dcData.getCreators();		
		String edition = getEdition(citation.getEdition(), ownTranslator);

		sb.append("<div class='item ").append(citation.getItemType().name()).append("'><div class='bib'>");
		switch (citation.getItemType()) {
			case webpage:
				sb.append("<span class='authors'>").appendHtmlEscaped(authors).append(".</span>");
				sb.append("<span class='title'>").appendHtmlEscaped(dcData.getTitle()).append(".</span>");
				sb.append(getDate(dcData.getPublicationDate(), ownTranslator, ".")); 
				sb.append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").append(dcData.getUrl()).append("</a>");
				break;
			case book:
				sb.appendHtmlEscaped(authors);
				sb.append(getYear(dcData.getPublicationDate(), ownTranslator, "."));
				sb.append("<span class='title'>").appendHtmlEscaped(dcData.getTitle()).append(".</span>");
				if (StringHelper.containsNonWhitespace(citation.getVolume())) {
					sb.append("<span class='volume'>").append(ownTranslator.translate("apa.volume.short")).append(" ").append(StringHelper.escapeHtml(citation.getVolume())).append(".</span>");
				}
				if (StringHelper.containsNonWhitespace(citation.getSeries())) {
					sb.append("<span class='series'>").append(StringHelper.escapeHtml(citation.getSeries())).append(".</span>");
				}
				sb.append(edition, StringHelper.containsNonWhitespace(edition));
				if (StringHelper.containsNonWhitespace(dcData.getPlace())) {
					sb.append("<span class='place'>").appendHtmlEscaped(dcData.getPlace()).append(":</span>");
				}
				if (StringHelper.containsNonWhitespace(dcData.getPublisher())) {
					sb.append("<span class='publisher'>").appendHtmlEscaped(dcData.getPublisher()).append(".</span>");
				}
				if (StringHelper.containsNonWhitespace(dcData.getUrl())) {
					sb.append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").appendHtmlEscaped(dcData.getUrl()).append("</a>");
				}
				break;
			case journalArticle:					
				sb.appendHtmlEscaped(authors).append(getDate(dcData.getPublicationDate(), ownTranslator, ",")) 
				  .append("<span class='title'>").appendHtmlEscaped(dcData.getTitle()).append(",</span>") 
				  .append("<span class='publicationTitle'>").appendHtmlEscaped(citation.getPublicationTitle()).append(",</span>") 
				  .append("<span class='issue'>").appendHtmlEscaped(citation.getIssue()).append("</span>")
				  .append("<span class='pages'>").appendHtmlEscaped(citation.getPages()).append(".</span>")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").appendHtmlEscaped(dcData.getUrl()).append("</a>");
				break;
			case report:
				sb.appendHtmlEscaped(authors).append(getYear(dcData.getPublicationDate(), ownTranslator, ""))
				  .append("<span class='title'>").appendHtmlEscaped(dcData.getTitle()).append("</span>")
				  .append("<span class='place'>").appendHtmlEscaped(dcData.getPlace()).append(":</span>")
				  .append("<span class='institution'>").appendHtmlEscaped(citation.getInstitution()).append("</span>")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").appendHtmlEscaped(dcData.getUrl()).append("</a>");
				break;			
			case film:
				sb.append("<span class='authors'>").appendHtmlEscaped(authors).append("</span>");
				if (dcData.getPublicationDate() != null) {
					sb.append("<span class='date'>").append(getYearWithoutPunctuation(dcData.getPublicationDate())).append("</span>");
				}
				sb.append("<span class='title'>").appendHtmlEscaped(dcData.getTitle()).append("</span> ")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").appendHtmlEscaped(dcData.getUrl()).append("</a>");
				break;
			default:
				sb.append("<div>").append(citation.getItemType().name());
		}
		sb.append("</div>");

		if(StringHelper.containsNonWhitespace(links)) {
			sb.append("<div class='links'>").append(links).append("</div>");
		}
		if(StringHelper.containsNonWhitespace(notes)) {
			sb.append("<div class='notes'>").append(notes).append("</div>");
		}

		sb.append("</div>");
	}
	
	public static String getEdition(String edition, Translator translator) {
		if(StringHelper.containsNonWhitespace(edition)) {
			if (StringHelper.isLong(edition)){
				String editionWithNumber = "(" + edition + ". " + translator.translate("apa.edition.short") + ")";	
				if (editionWithNumber.equals("(. " + translator.translate("apa.edition.short") + ")")) {
					return "";
				}
				return "<span class='edition'>" + editionWithNumber + "</span>"; 
			}
			else {
				return "<span class='edition'>(" + StringHelper.escapeHtml(edition) + ")</span>";				
			}
		} else {
			return "";
		}
	}
	
	public static String getDate(Date publicationDate, Translator translator, String punctuation) {
		if (publicationDate != null) {
			return " <span class='date'>(" + 
					Formatter.getInstance(translator.getLocale()).formatDate(publicationDate) + ")" + punctuation + 
					"</span>";
		}
		return " <span class='date'>(" + translator.translate("apa.no.year") + ")" + punctuation + "</span>";
	}
	
	public static String getYear(Date publicationDate, Translator translator, String punctuation) {
		if (publicationDate != null) {
			int year = DateUtils.yearFromDate(publicationDate);
			return " <span class='date'>(" + year + ")" + punctuation + "</span>";
		}
		return " <span class='date'>(" + translator.translate("apa.no.year") + ")" + punctuation + "</span>";
	}

	public static String getYearWithoutPunctuation(Date publicationDate) {
		if (publicationDate != null) {
			int year = DateUtils.yearFromDate(publicationDate);
			return " <span class='date'>" + year + "</span>";
		}
		return "";
	}
}
