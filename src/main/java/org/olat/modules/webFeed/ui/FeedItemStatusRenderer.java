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
package org.olat.modules.webFeed.ui;

import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.webFeed.Item;

/**
 * Initial date: Mai 24, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedItemStatusRenderer implements FlexiCellRenderer, CustomCellRenderer {

	private static final Logger log = Tracing.createLoggerFor(FeedItemStatusRenderer.class);

	private final Translator translator;

	public FeedItemStatusRenderer(Locale locale) {
		translator = Util.createPackageTranslator(FeedItemListController.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
					   int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof FeedItemStatusEnum statusEnum) {
			render(sb, statusEnum);
		}
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale,
					   int alignment, String action) {
		// use the FlexiCellRenderer method
		render(renderer, sb, val, -1, null, null, null);
	}

	public String renderItemStatus(FeedItemRow itemRow) {
		try (StringOutput sb = new StringOutput(32)) {
			render(sb, itemRow.getStatus());
			return sb.toString();
		} catch (IOException e) {
			log.error("", e);
			return "";
		}
	}

	public String renderItemStatus(Item feedItem) {
		try (StringOutput sb = new StringOutput(32)) {
			// draft is the default
			FeedItemStatusEnum feedItemStatusEnum = FeedItemStatusEnum.draft;
			if (feedItem.isScheduled()) {
				feedItemStatusEnum = FeedItemStatusEnum.planned;
			} else if (feedItem.isPublished()) {
				feedItemStatusEnum = FeedItemStatusEnum.published;
			}
			render(sb, feedItemStatusEnum);
			return sb.toString();
		} catch (IOException e) {
			log.error("", e);
			return "";
		}
	}

	public void render(StringOutput sb, FeedItemStatusEnum status) {
		sb.append("<span class='o_labeled_light o_status_table o_feed_status_").append(status.name())
				.append("' title=\"").append(StringHelper.escapeHtml(translator.translate("feed.item." + status.name()))).append("\">")
				.append(translator.translate("feed.item.".concat(status.name())))
				.append("</span></span>");
	}
}
