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
package org.olat.modules.portfolio.ui.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.ExtendedMediaRenderingHints;
import org.olat.modules.portfolio.ui.BinderOnePageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * It makes a flat html file, copy all medias around it
 * and render it as PDF via phantomjs.
 * 
 * 
 * 
 * Initial date: 24 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportBinderAsPDFResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ExportBinderAsPDFResource.class);
	
	private File htmlDir;
	private File pdfFile;
	
	private final UserRequest ureq;
	private final Page selectedPage;
	private final BinderRef binderRef;
	private final Translator translator;
	
	@Autowired
	private PdfService pdfService;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private PortfolioService portfolioService;
	
	public ExportBinderAsPDFResource(BinderRef binderRef, UserRequest ureq, Locale locale) {
		CoreSpringFactory.autowireObject(this);
		translator = Util.createPackageTranslator(ExportBinderAsPDFResource.class, locale);	
		this.ureq = new SyntheticUserRequest(ureq.getIdentity(), locale, ureq.getUserSession());
		this.binderRef = binderRef;
		selectedPage = null;
	}
	
	public ExportBinderAsPDFResource(Page selectedPage, UserRequest ureq, Locale locale) {
		CoreSpringFactory.autowireObject(this);
		translator = Util.createPackageTranslator(ExportBinderAsPDFResource.class, locale);	
		this.ureq = new SyntheticUserRequest(ureq.getIdentity(), locale, ureq.getUserSession());
		binderRef = null;
		this.selectedPage = selectedPage;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/pdf";
	}

	@Override
	public Long getSize() {
		return null;
	}
	
	@Override
	public Long getLastModified() {
		return null;
	}
	
	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(pdfFile);
		} catch (FileNotFoundException e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public void release() {
		if(htmlDir != null && htmlDir.exists()) {
			FileUtils.deleteDirsAndFiles(htmlDir, true, true);
		}
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		htmlDir = prepareHtml();

		String label;
		if(selectedPage != null) {
			label = selectedPage.getTitle();
		} else {
			Binder binder = portfolioService.getBinderByKey(binderRef.getKey());
			label = binder.getTitle();
		}

		String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

		String file = secureLabel + ".pdf";
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
		hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
		
		try(OutputStream out = hres.getOutputStream()) {
			pdfService.convert(htmlDir, "index.html", out);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private File prepareHtml() {
		File outputDir = new File(WebappHelper.getTmpDir(), "pf" + UUID.randomUUID());
		outputDir.mkdirs();
		
		WindowControl mockwControl = new WindowControlMocker();
		BinderOnePageController printCtrl;
		if(selectedPage != null) {
			printCtrl = new BinderOnePageController(ureq, mockwControl, selectedPage, ExtendedMediaRenderingHints.toPdf(), false);
		} else {
			printCtrl = new BinderOnePageController(ureq, mockwControl, binderRef, ExtendedMediaRenderingHints.toPdf(), false);
		}
		Component content = printCtrl.getInitialComponent();
		String html = createResultHTML(content);
		
		File indexHtml = new File(outputDir, "index.html");
		html = exportMedia(html, outputDir);
		try(OutputStream out= new FileOutputStream(indexHtml)) {
			IOUtils.write(html, out, StandardCharsets.UTF_8);
		} catch(IOException e) {
			log.error("", e);
		}
		printCtrl.dispose();
		return outputDir;
	}

	private String createResultHTML(Component content) {
		String pagePath = Util.getPackageVelocityRoot(this.getClass()) + "/export.html";
		VelocityContainer mainVC = new VelocityContainer("html", pagePath, translator, null);
		mainVC.put("cmp", content);
		mainVC.contextPut("bodyCssClass", "o_portfolio_export");

		URLBuilder ubu = new URLBuilder("auth", "1", "0", "1");
		Renderer renderer = Renderer.getInstance(mainVC, translator, ubu, new RenderResult(), new DefaultGlobalSettings(), "");// no token here
		try(StringOutput sb = new StringOutput(32000);
				VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, mainVC, sb)) {
			mainVC.contextPut("r", vrdec);
			renderer.render(sb, mainVC, null);
			return sb.toString();
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}
	
	public String exportMedia(String html, File outputDir) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			ExportMedia contentHandler = new ExportMedia(outputDir);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(html)));
			 Map<String,String> replaces = contentHandler.getReplaces();
			 for(Map.Entry<String,String> replacement:replaces.entrySet()) {
				 html = html.replace(replacement.getKey(), replacement.getValue());
			 }
		} catch (Exception e) {
			log.error("", e);
		}
		 return html;
	}
	
	private class ExportMedia extends DefaultHandler {
		
		private File outputDir;
		private StringBuilder script;
		private Map<String,String> replaces = new HashMap<>();
		
		public ExportMedia(File outputDir) {
			this.outputDir = outputDir;
		}
		
		public Map<String,String> getReplaces() {
			return replaces;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
			if("img".equalsIgnoreCase(localName)) {
				String src = attributes.getValue("src");
				String cleanedSrc = processMedia(src);
				if(cleanedSrc != null) {
					replaces.put(src, cleanedSrc);
				}
			} else if("a".equalsIgnoreCase(localName)) {
				String href = attributes.getValue("href");
				String cleanedHref = processMedia(href);
				if(cleanedHref != null) {
					replaces.put(href, cleanedHref);
				}
			} else if("script".equalsIgnoreCase(localName)) {
				script = new StringBuilder();
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(script != null && start >= 0 && length > 0) {
				script.append(ch, start, length);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("script".equalsIgnoreCase(localName) && script != null) {
				processVideoScript(script);
				script = null;
			}
		}
		
		private void processVideoScript(StringBuilder content) {
			String player = "BPlayer.insertPlayer('";
			int playerIndex = content.indexOf(player);
			if(playerIndex > 0) {
				int mapperIndex = script.indexOf(DispatcherModule.PATH_MAPPED, playerIndex);
				int endVariable = script.indexOf("','", mapperIndex);
				String src = script.substring(mapperIndex, endVariable);
				String cleanedSrc = processMedia(src);
				if(cleanedSrc != null) {
					String url = script.substring(playerIndex + player.length(), endVariable);
					replaces.put(url, cleanedSrc);
				}
			}
		}

		private String processMedia(String src) {
			String serverContext = Settings.getServerContextPath();
			if(serverContext != null && serverContext.length() > 1 && src.startsWith(serverContext)) {
				src = src.substring(serverContext.length(), src.length());
			}
			
			if(!src.startsWith(DispatcherModule.PATH_MAPPED)) return null;

			String subInfo = src.substring(DispatcherModule.PATH_MAPPED.length());
			int slashPos = subInfo.indexOf('/');
			
			String id;
			if (slashPos == -1) {
				id = subInfo;
			} else {
				id = subInfo.substring(0, slashPos);
			}

			Mapper mapper = mapperService.getMapperById(ureq.getUserSession(), id);
			if(mapper == null) return null;

			String mod = slashPos > 0 ? subInfo.substring(slashPos) : "";
			MediaResource resource = mapper.handle(mod, null);
			if(resource == null) return null;
			
			String cleanedSrc = src.substring(1);
			int index = cleanedSrc.lastIndexOf('?');
			if(index > 0) {
				cleanedSrc = cleanedSrc.substring(0, index);
			}
			
			File entry = new File(outputDir, cleanedSrc);
			try (InputStream in = resource.getInputStream()) {
				FileUtils.copyToFile(in, entry, "Copy media");
			} catch (IOException e) {
				log.error("Error during copy of resource export", e);
			}
			return cleanedSrc;
		}
	}
}
