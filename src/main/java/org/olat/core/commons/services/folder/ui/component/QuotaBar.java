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
package org.olat.core.commons.services.folder.ui.component;

import java.util.Locale;

import org.olat.core.commons.services.folder.ui.FolderQuota;
import org.olat.core.commons.services.folder.ui.FolderUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 6 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class QuotaBar extends AbstractComponent {
	
	private static final long BYTE_UNIT = 1000l;

	private static final QuotaBarRenderer RENDERER = new QuotaBarRenderer();
	
	private final Translator quotaTranslator;
	private ProgressBar progressBar;
	private FolderQuota quota;

	public QuotaBar(String name, FolderQuota quota, Locale locale) {
		super(name);
		setDomReplacementWrapperRequired(false);
		quotaTranslator = Util.createPackageTranslator(FolderUIFactory.class, locale);
		
		progressBar = new ProgressBar(name + "_bar");
		progressBar.setLabelAlignment(LabelAlignment.none);
		progressBar.setRenderSize(RenderSize.small);
		progressBar.setWidth(200);
		
		setQuota(quota);
	}

	public FolderQuota getQuota() {
		return quota;
	}

	public void setQuota(FolderQuota quota) {
		this.quota = quota;
		setDirty(true);
		if (quota == null) {
			return;
		}
		
		if (isUnlimited()) {
			progressBar.setIsNoMax(true);
		} else if(quota.getQuotaKB() == 0) {
			progressBar.setIsNoMax(false);
			progressBar.setMax(quota.getQuotaKB());
		} else {
			progressBar.setIsNoMax(false);
			progressBar.setMax(quota.getQuotaKB() / BYTE_UNIT);
		}
		progressBar.setActual(quota.getActualUsage() / BYTE_UNIT);
	}
	
	boolean isUnlimited() {
		return quota == null || quota.isUnlimited();
	}
	
	String getLabel() {
		if (quota == null) {
			return "";
		}
		
		if (isUnlimited()) {
			return quotaTranslator.translate(
					"quota.label.unlimited",
					Formatter.formatBytes(quota.getActualUsage() * BYTE_UNIT));
		}
		
		return quotaTranslator.translate(
				"quota.label.limited",
				Formatter.formatBytes(quota.getActualUsage() * BYTE_UNIT),
				Formatter.formatBytes(quota.getQuotaKB() * BYTE_UNIT));
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

}
