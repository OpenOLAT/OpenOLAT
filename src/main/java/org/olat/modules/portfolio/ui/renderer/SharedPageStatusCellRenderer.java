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
package org.olat.modules.portfolio.ui.renderer;

import java.io.IOException;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.ui.TableOfContentController.PageRow;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.olat.modules.portfolio.ui.shared.SharedPageRow;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedPageStatusCellRenderer implements FlexiCellRenderer {
	
	private static final OLog log = Tracing.createLoggerFor(SharedPageStatusCellRenderer.class);
	
	private final Translator translator;
	
	public SharedPageStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator trans) {
		if(cellValue instanceof SharedPageRow) {
			SharedPageRow pageRow = (SharedPageRow)cellValue;
			PageStatus status = pageRow.getStatus();
			PageUserStatus userStatus = pageRow.getUserStatus();
			render(target, status, userStatus);
		} else if(cellValue instanceof PortfolioElementRow) {
			PortfolioElementRow elementRow = (PortfolioElementRow)cellValue;
			if(elementRow.getPage() != null) {
				PageStatus status = elementRow.getPageStatus();
				PageUserStatus userStatus = elementRow.getUserInfosStatus();
				render(target, status, userStatus);
			}
		}
	}
	
	public String renderPageRow(PageRow pageRow) {
		if(pageRow == null || pageRow.getPage() == null) return "";
		
		try(StringOutput target = new StringOutput(64)) {
			PageStatus pageStatus = pageRow.getPage().getPageStatus();
			PageUserStatus userStatus = pageRow.getUserInfosStatus();
			render(target, pageStatus, userStatus) ;
			return target.toString();
		} catch(IOException e) {
			log.error("", e);
			return "ERROR";
		}
	}
	
	public String renderPortfolioElementRow(PortfolioElementRow elementRow) {
		if(elementRow == null || elementRow.getPage() == null) return "";
		
		try(StringOutput target = new StringOutput(64)) {
			PageStatus pageStatus = elementRow.getPage().getPageStatus();
			PageUserStatus userStatus = elementRow.getUserInfosStatus();
			render(target, pageStatus, userStatus) ;
			return target.toString();
		} catch(IOException e) {
			log.error("", e);
			return "ERROR";
		}
	}

	private final void render(StringOutput target, PageStatus status, PageUserStatus userStatus) {
		String cssClass = null;
		String title = null;
		if(status == null || status == PageStatus.draft) {
			cssClass = "o_portfolio_entry_draft";
			title = translator.translate(PageStatus.draft.i18nKey());
		} else if(status == PageStatus.inRevision) {
			cssClass = "o_portfolio_entry_revision";
			title = translator.translate(PageStatus.inRevision.i18nKey());	
		} else if(userStatus == null || userStatus == PageUserStatus.incoming) {
			cssClass = "o_portfolio_entry_incoming";
			title = translator.translate(PageUserStatus.incoming.i18nKey());	
		} else if(userStatus == PageUserStatus.inProcess) {
			cssClass = "o_portfolio_entry_inprocess";
			title = translator.translate(PageUserStatus.inProcess.i18nKey());	
		} else if(userStatus == PageUserStatus.done) {
			cssClass = "o_portfolio_entry_done";
			title = translator.translate(PageUserStatus.done.i18nKey());	
		} else if(status == PageStatus.closed) {
			cssClass = "o_portfolio_entry_closed";
			title = translator.translate(PageStatus.closed.i18nKey());	
		} else if(status == PageStatus.deleted) {
			cssClass = "o_portfolio_entry_deleted";
			title = translator.translate(PageStatus.deleted.i18nKey());	
		}
		
		if(cssClass != null && title != null) {
			target.append("<span class='badge ").append(cssClass).append("' title='").append(title).append("'>").append(title).append("</span>");
		}
	}
}
