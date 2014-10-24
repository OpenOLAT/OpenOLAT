/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.search.service.indexer;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.velocity.context.Context;
import org.olat.core.commons.contextHelp.ContextHelpModule;
import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.resource.OresHelper;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.ContextHelpDocument;

/**
 * Description:<br>
 * This indexer indexes the context sensitive help system
 * 
 * <P>
 * Initial Date:  05.11.2008 <br>
 * @author gnaegi
 */
public class ContextHelpIndexer extends AbstractHierarchicalIndexer {
	private static final OLog log = Tracing.createLoggerFor(ContextHelpIndexer.class);

	/**
	 * @see org.olat.search.service.indexer.Indexer#checkAccess(org.olat.core.id.context.ContextEntry, org.olat.core.id.context.BusinessControl, org.olat.core.id.Identity, org.olat.core.id.Roles)
	 */
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		// context help is visible to everybody, even not-logged in users
		return true;
	}

	/**
	 * @see org.olat.search.service.indexer.Indexer#getSupportedTypeName()
	 */
	@Override
	public String getSupportedTypeName() {
		return OresHelper.calculateTypeName(ContextHelpModule.class);	
	}

	
	/**
	 * @see org.olat.search.service.indexer.AbstractHierarchicalIndexer#doIndex(org.olat.search.service.SearchResourceContext,
	 *      java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
	 */
	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		if (!ContextHelpModule.isContextHelpEnabled()) {
  			// don't index context help when disabled
  			return;
  		}
		
		Set<String> helpPageIdentifyers = ContextHelpModule.getAllContextHelpPages();		
		Collection<String> languages = I18nModule.getEnabledLanguageKeys();
		GlobalSettings globalSettings = new HelpSettings();
		
		if (log.isDebug()) log.debug("ContextHelpIndexer helpPageIdentifyers.size::" + helpPageIdentifyers.size() + " and languages.size::" + languages.size());
  	// loop over all help pages
		for (String helpPageIdentifyer : helpPageIdentifyers) {			
			String[] identifyerSplit = helpPageIdentifyer.split(":");
			String bundleName = identifyerSplit[0];
			String page = identifyerSplit[1];
			//only index html pages
			if(page == null || !page.endsWith(".html")) {
				continue;
			}
			
			// Translator with default locale. Locale is set to each language in the
			// language iteration below
			Translator pageTranslator = new PackageTranslator(bundleName, I18nModule.getDefaultLocale());
			// Open velocity page for this help page
			String pagePath = bundleName.replace('.', '/') + ContextHelpModule.CHELP_DIR + page;
			VelocityContainer container =  new VelocityContainer("contextHelpPageVC", pagePath, pageTranslator, null);					
			Context ctx = container.getContext();		
			Renderer renderer = Renderer.getInstance(container, pageTranslator, new EmptyURLBuilder(), null, globalSettings);
			// Add render decorator with helper methods
			VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, container, null);			
			ctx.put("r", vrdec);
			// Add empty static dir url - only used to not generate error messages
			ctx.put("chelpStaticDirUrl", "");
			// Create document for each language using the velocity context
			for (String langCode : languages) {
				Locale locale = I18nManager.getInstance().getLocaleOrNull(langCode);
				String relPagePath = langCode + "/" + bundleName + "/" + page;
				if (log.isDebug()) log.debug("Indexing help page with path::" + relPagePath);
				SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
				searchResourceContext.setBusinessControlFor(OresHelper.createOLATResourceableType(ContextHelpModule.class.getSimpleName()));//to match the list of indexer
				// Create context help document and index now, set translator to current locale
				pageTranslator.setLocale(locale);
				Document document = ContextHelpDocument.createDocument(searchResourceContext, bundleName, page, pageTranslator, ctx, pagePath);
				indexWriter.addDocument(document);
			}
			IOUtils.closeQuietly(vrdec);
		}
	}
	
	public static class HelpSettings implements GlobalSettings {
		private final AJAXFlags empty = new EmptyAJAXFlags();
		
		@Override
		public int getFontSize() {
			return 100;
		}
		
		@Override
		public AJAXFlags getAjaxFlags() {
			return empty;
		}

		@Override
		public boolean isIdDivsForced() {
			return false;
		}
	}
	
	public static class EmptyAJAXFlags extends AJAXFlags {

		public EmptyAJAXFlags() {
			super(null);
		}

		@Override
		public boolean isIframePostEnabled() {
			return false;
		}
	}
}