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
package org.olat.modules.portfolio.ui.component;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.portfolio.Citation;

/**
 * 
 * Initial date: 21.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		CitationComponent cmp = (CitationComponent)source;
		String notes = null;
		String links = null;
		renderAPAGerman(sb, cmp.getCitation(), cmp.getDublinCoreMetadata(), notes, links);
	}
	
	public static void renderAPAGerman(StringOutput sb, Citation citation, DublinCoreMetadata dcData, String notes, String links) {
		String authors = dcData.getCreators();		
		String authorString = getAuthors(authors/* " (Hrsg.). ", true*/);
		String edition = getOrdinalAPA(citation.getEdition());
		String volume = getVolumeAPA(citation.getVolume());
		String series = getSeries(citation.getSeries());
		String date = getDate(dcData.getDate());
		String dateAdded = "";

		sb.append("<div class='item ").append(citation.getItemType().name()).append("'><div class='bib'>");
		switch (citation.getItemType()) {
			case webpage:
				sb.append(authorString)
				  .append("<span class='title'>").append(dcData.getTitle()).append(".</span>").append(dateAdded) 
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").append(dcData.getUrl()).append("</a>");
				break;
			case book:
				sb.append(authorString).append(date) 
				  .append("<span class='title'>").append(dcData.getTitle()).append(".</span>")
				  .append(volume).append(series).append(edition)
				  .append("<span class='place'>").append(dcData.getPlace()).append(":</span>")
				  .append("<span class='publisher'>").append(dcData.getPublisher()).append("</span>")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").append(dcData.getUrl()).append("</a>");					
				break;
			case journalArticle:					
				sb.append(authorString).append(date) 
				  .append("<span class='title'>").append(dcData.getTitle()).append(",</span>") 
				  .append("<span class='publicationTitle'>").append(citation.getPublicationTitle()).append(",</span>") 
				  .append("<span class='issue'>").append(citation.getIssue()).append("</span>")
				  .append("<span class='pages'>").append(citation.getPages()).append(".</span>")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").append(dcData.getUrl()).append("</a>");
				break;
			case report:
				sb.append(authorString).append(date)
				  .append("<span class='title'>").append(dcData.getTitle()).append("</span>")
				  .append("<span class='place'>").append(dcData.getPlace()).append(":</span>")
				  .append("<span class='institution'>").append(citation.getInstitution()).append("</span>")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").append(dcData.getUrl()).append("</a>");
				break;			
			case film:
				sb.append(authorString).append("<span class='date'>").append(dcData.getDate()).append("</span>")
				  .append("<span class='title'>").append(dcData.getTitle()).append("</span> ")
				  .append("<a href='").append(dcData.getUrl()).append("' target='_blank' class='url'>").append(dcData.getUrl()).append("</a>");
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
	
	public static String getAuthors(String authors/*, String d, boolean apa*/) {
		return authors;
	}
	
	public static String getOrdinalAPA(String n) {
		if(StringHelper.containsNonWhitespace(n)) {
			if (StringHelper.isLong(n)){
				String w = "("+ n + ". Aufl.)";	
				if (w.equals("(. Aufl.)")) {
					return "";
				}
				return "<span class='edition'>" + w + "</span>"; 
			}
			else {
				return "<span class='edition'>(" + n + ")</span>";				
			}
		} else {
			return "";				
		}
	}
	
	public static String getVolumeAPA(String volume) {
		if (StringHelper.containsNonWhitespace(volume)) {
			return "<span class='volume'>Bd. " + volume + " </span>";
		}
		return "";
	}
	
	public static String getSeries(String series) {
		if (StringHelper.containsNonWhitespace(series)) {
			return "<span class='series'> " + series + ". </span>";
		}
		return "";
	}
	
	public static String getDate(String date) {
		if (StringHelper.containsNonWhitespace(date)) {
			return " <span class='date'>(" + date + ")</span>";
		}
		return " <span class='date'>(o. J.)</span>";
	}
}
