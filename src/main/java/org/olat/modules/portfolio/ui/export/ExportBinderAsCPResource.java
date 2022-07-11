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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.resource.OresHelper;
import org.olat.imscp.xml.manifest.ItemType;
import org.olat.imscp.xml.manifest.ManifestMetadataType;
import org.olat.imscp.xml.manifest.ManifestType;
import org.olat.imscp.xml.manifest.OrganizationType;
import org.olat.imscp.xml.manifest.OrganizationsType;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.imscp.xml.manifest.ResourcesType;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageProvider;
import org.olat.modules.ceditor.ui.PageController;
import org.olat.modules.cp.CPOfflineReadableManager;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.handler.ContainerHandler;
import org.olat.modules.portfolio.handler.EvaluationFormHandler;
import org.olat.modules.portfolio.handler.HTMLRawPageElementHandler;
import org.olat.modules.portfolio.handler.MathPageElementHandler;
import org.olat.modules.portfolio.handler.ParagraphPageElementHandler;
import org.olat.modules.portfolio.handler.SpacerElementHandler;
import org.olat.modules.portfolio.handler.TablePageElementHandler;
import org.olat.modules.portfolio.handler.TitlePageElementHandler;
import org.olat.modules.portfolio.model.ExtendedMediaRenderingHints;
import org.olat.modules.portfolio.ui.AbstractPageListController;
import org.olat.modules.portfolio.ui.PageMetadataController;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * 
 * Initial date: 17 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportBinderAsCPResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ExportBinderAsCPResource.class);
	
	private static final String SCHEMA_LOCATIONS = "http://www.imsglobal.org/xsd/imscp_v1p1 http://www.imsglobal.org/xsd/imscp_v1p2.xsd";
	private static final org.olat.imscp.xml.manifest.ObjectFactory cpObjectFactory = new org.olat.imscp.xml.manifest.ObjectFactory();
	private static JAXBContext context;
	static {
		try {
			context = JAXBContext.newInstance("org.olat.imscp.xml.manifest");
		} catch (JAXBException e) {
			log.error("", e);
		}
	}
	
	private final UserRequest ureq;
	private final BinderRef binderRef;
	private final Translator translator;
	private final MapperService mapperService;
	
	private final PortfolioService portfolioService;
	
	public ExportBinderAsCPResource(BinderRef binderRef, UserRequest ureq, Locale locale) {
		this.ureq = new SyntheticUserRequest(ureq.getIdentity(), locale, ureq.getUserSession());
		this.binderRef = binderRef;
		this.translator = Util.createPackageTranslator(AbstractPageListController.class, locale);	
		portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		mapperService = CoreSpringFactory.getImpl(MapperService.class);
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
		return "application/zip";
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
		return null;
	}

	@Override
	public void release() {
		//
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}
		
		try {
			Binder binder = portfolioService.getBinderByKey(binderRef.getKey());
			String label = binder.getTitle();
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
			export(binder, hres.getOutputStream());
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void export(Binder binder, OutputStream out) {
		try(ZipOutputStream zout = new ZipOutputStream(out)) {
			//load pages
			List<Section> sections = portfolioService.getSections(binder);
			List<Page> pages = portfolioService.getPages(binder);
			
			//manifest
			ManifestType manifest = createImsManifest(binder, sections, pages);
			zout.putNextEntry(new ZipEntry("imsmanifest.xml"));
			writeManifest(manifest, zout);
			zout.closeEntry();
			
			//write pages
			for(Section section:sections) {
				exportSection(section, zout);
			}
			//write pages
			for(Page page:pages) {
				exportPage(page, zout);
			}
			
			//theme and javascripts
			exportCSSAndJs(zout);

			// make it readable offline
			ByteArrayOutputStream manifestOut = new ByteArrayOutputStream();
			write(manifest, manifestOut);
			String manifestXml = new String(manifestOut.toByteArray());
			String indexSrc = sectionFilename(sections.get(0));
			CPOfflineReadableManager.getInstance().makeCPOfflineReadable(manifestXml, indexSrc, zout);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void writeManifest(ManifestType manifest, ZipOutputStream zout) {
		try(OutputStream out=new ShieldOutputStream(zout)) {
			write(manifest, out);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private ManifestType createImsManifest(Binder binder, List<Section> sections, List<Page> pages) {
		ManifestType manifest = cpObjectFactory.createManifestType();
		manifest.setIdentifier(UUID.randomUUID().toString());
		//schema
		ManifestMetadataType metadataType = cpObjectFactory.createManifestMetadataType();
		manifest.setMetadata(metadataType);
		//organizations
		OrganizationsType organizations = cpObjectFactory.createOrganizationsType();
		manifest.setOrganizations(organizations);
		OrganizationType organization = cpObjectFactory.createOrganizationType();
		organization.setIdentifier("binder_" + binder.getKey());
		organization.setTitle(binder.getTitle());
		organization.setStructure("hierarchical");
		organizations.getOrganization().add(organization);
		organizations.setDefault(organization);
		
		ResourcesType resources = cpObjectFactory.createResourcesType();
		manifest.setResources(resources);

		Map<Section, ItemType> sectionToItemMap = new HashMap<>();
		for(Section section:sections) {
			ItemType sectionItem = cpObjectFactory.createItemType();
			String itemIdentifier = "section_" + section.getKey().toString();
			String resourceIdentifier = "res_" + itemIdentifier;
			sectionItem.setTitle(section.getTitle());
			sectionItem.setIdentifier(itemIdentifier);
			sectionItem.setIdentifierref(resourceIdentifier);
			sectionItem.setIsvisible(Boolean.TRUE);
			organization.getItem().add(sectionItem);
			sectionToItemMap.put(section, sectionItem);
			
			ResourceType resource = cpObjectFactory.createResourceType();
			resource.setIdentifier(resourceIdentifier);
			resource.setType("webcontent");
			resource.setHref(sectionFilename(section));
			resources.getResource().add(resource);
		}
		
		for(Page page:pages) {
			ItemType sectionItem = sectionToItemMap.get(page.getSection());
			if(sectionItem == null) {
				continue;
			}

			ItemType pageItem = cpObjectFactory.createItemType();
			pageItem.setTitle(page.getTitle());
			String itemIdentifier = "page_" + page.getKey().toString();
			String resourceIdentifier = "res_" + itemIdentifier;
			pageItem.setIdentifier(itemIdentifier);
			pageItem.setIdentifierref(resourceIdentifier);
			pageItem.setIsvisible(Boolean.TRUE);
			sectionItem.getItem().add(pageItem);
			
			ResourceType resource = cpObjectFactory.createResourceType();
			resource.setIdentifier(resourceIdentifier);
			resource.setType("webcontent");
			resource.setHref(pageFilename(page));
			resources.getResource().add(resource);
		}
		return manifest;
	}
	
	private String pageFilename(Page page) {
		String title = page.getTitle();
		String filename = StringHelper.transformDisplayNameToFileSystemName(title).toLowerCase();
		return filename + "_p" + page.getKey() + ".html";
	}
	
	private String sectionFilename(Section section) {
		String title = section.getTitle();
		String filename = StringHelper.transformDisplayNameToFileSystemName(title).toLowerCase();
		return filename + "_s" + section.getKey() + ".html";
	}
	
	private final void write(ManifestType manifest, OutputStream out) {
        try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATIONS);
			marshaller.marshal(cpObjectFactory.createManifest(manifest), out);
		} catch (JAXBException e) {
			log.error("", e);
		}
	}
	
	private void exportSection(Section section, ZipOutputStream zout) throws IOException {
		String pagePath = Util.getPackageVelocityRoot(AbstractPageListController.class) + "/portfolio_element_row.html";
		VelocityContainer rowVC = new VelocityContainer("html", pagePath, translator, null);

		AssessmentSection assessmentSection = null;
		PortfolioElementRow row = new PortfolioElementRow(section, assessmentSection, false, false);
		rowVC.contextPut("row", row);
		rowVC.contextPut("rowIndex", 0);

		String html = createResultHTML(null, rowVC, null, "o_section_export");
		convertToZipEntry(zout, sectionFilename(section), html);	
	}
	
	private void exportPage(Page page, ZipOutputStream zout) throws IOException {
		WindowControl mockwControl = new WindowControlMocker();
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getReadOnlyCallback();
		PageMetadataController metadatCtrl = new PageMetadataController(ureq, mockwControl, secCallback, page, false);

		PageController pageCtrl = new PageController(ureq, mockwControl, new PortfolioPageProvider(page), ExtendedMediaRenderingHints.toPrint());
		pageCtrl.loadElements(ureq);
		
		CommentAndRatingSecurityCallback commentSecCallback = new ReadOnlyCommentsSecurityCallback();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
		UserCommentsController commentsCtrl = new UserCommentsController(ureq, mockwControl, ores, null, null, commentSecCallback);
		Component metadata = metadatCtrl.getInitialComponent();
		Component component = pageCtrl.getInitialComponent();
		Component comments = commentsCtrl.getNumOfComments() > 0 ? commentsCtrl.getInitialComponent() : null;
		String html = createResultHTML(metadata, component, comments, "o_page_export");
		html = exportMedia(html, zout);
		convertToZipEntry(zout, pageFilename(page), html);	
		
		pageCtrl.dispose();
		metadatCtrl.dispose();
	}

	private String createResultHTML(Component metadata, Component content, Component comments, String bodyCssClass) {
		String pagePath = Util.getPackageVelocityRoot(this.getClass()) + "/export.html";
		VelocityContainer mainVC = new VelocityContainer("html", pagePath, translator, null);
		mainVC.contextPut("bodyCssClass", bodyCssClass);
		if(metadata != null) {
			mainVC.put("metadata", metadata);
		}
		if(content != null) {
			mainVC.put("content", content);
		}
		if(comments != null) {
			mainVC.put("comments", comments);
		}
		return renderVelocityContainer(mainVC) ;
	}
	
	private String renderVelocityContainer(VelocityContainer mainVC) {
		URLBuilder ubu = new URLBuilder("auth", "1", "0", "1");
		Renderer renderer = Renderer.getInstance(mainVC, translator, ubu, new RenderResult(), new DefaultGlobalSettings(), "-");
		try(StringOutput sb = new StringOutput(32000);
				VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, mainVC, sb)) {
			mainVC.contextPut("r", vrdec);
			renderer.render(sb, mainVC, null);
			return sb.toString();
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void convertToZipEntry(ZipOutputStream zout, String link, String content) throws IOException {
		zout.putNextEntry(new ZipEntry(link));
		try (InputStream in = new ByteArrayInputStream(content.getBytes())) {
			FileUtils.copy(in, zout);
		} catch (Exception e) {
			log.error("Error during copy of resource export", e);
		} finally {
			zout.closeEntry();
		}
	}
	
	private void exportCSSAndJs(ZipOutputStream zout) {
		//Copy resource files or file trees to export file tree 
		File sasstheme = new File(WebappHelper.getContextRealPath("/static/themes/light"));
		ZipUtil.addDirectoryToZip(sasstheme.toPath(), "css/offline/light", zout);
		File fontawesome = new File(WebappHelper.getContextRealPath("/static/font-awesome"));
		ZipUtil.addDirectoryToZip(fontawesome.toPath(), "css/font-awesome", zout);
		File jQueryJs = new File(WebappHelper.getContextRealPath("/static/js/jquery/"));
		ZipUtil.addDirectoryToZip(jQueryJs.toPath(), "js/jquery", zout);
		File d3Js = new File(WebappHelper.getContextRealPath("/static/js/d3/"));
		ZipUtil.addDirectoryToZip(d3Js.toPath(), "js/d3", zout);
	}
	
	public String exportMedia(String html, ZipOutputStream zout) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			ExportMedia contentHandler = new ExportMedia(zout);
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
		
		private ZipOutputStream zout;
		private StringBuilder script;
		private Map<String,String> replaces = new HashMap<>();
		
		public ExportMedia(ZipOutputStream zout) {
			this.zout = zout;
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
			
			if(!src.startsWith(DispatcherModule.PATH_MAPPED)) {
				return null;
			}

			String subInfo = src.substring(DispatcherModule.PATH_MAPPED.length());
			int slashPos = subInfo.indexOf('/');
			
			String id;
			if (slashPos == -1) {
				id = subInfo;
			} else {
				id = subInfo.substring(0, slashPos);
			}

			try {
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
				
				try (InputStream in = resource.getInputStream()) {
					zout.putNextEntry(new ZipEntry(cleanedSrc));
					FileUtils.copy(in, zout);
				} catch (Exception e) {
					log.error("Error during copy of resource export", e);
				} finally {
					zout.closeEntry();
				}
				return cleanedSrc;
			} catch (IOException e) {
				log.error("Error during copy of resource export", e);
				return null;
			}
		}
	}
	
	private class PortfolioPageProvider implements PageProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		
		private Page page;

		public PortfolioPageProvider(Page page) {
			this.page = page; 

			//handler for title
			TitlePageElementHandler titleRawHandler = new TitlePageElementHandler();
			handlers.add(titleRawHandler);
			//handler simple HTML
			ParagraphPageElementHandler paragraphHandler = new ParagraphPageElementHandler();
			handlers.add(paragraphHandler);
			//handler for spacer
			SpacerElementHandler hrHandler = new SpacerElementHandler();
			handlers.add(hrHandler);
			//handler for container
			ContainerHandler containerHandler = new ContainerHandler();
			handlers.add(containerHandler);
			//handler for form
			EvaluationFormHandler formHandler = new EvaluationFormHandler();
			handlers.add(formHandler);
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			//handler for table
			TablePageElementHandler tableHandler = new TablePageElementHandler();
			handlers.add(tableHandler);
			//handler for LaTeX
			MathPageElementHandler mathHandler = new MathPageElementHandler();
			handlers.add(mathHandler);
			
			List<MediaHandler> mediaHandlers = portfolioService.getMediaHandlers();
			for(MediaHandler mediaHandler:mediaHandlers) {
				if(mediaHandler instanceof PageElementHandler) {
					handlers.add((PageElementHandler)mediaHandler);
				}
			}
		}

		@Override
		public List<? extends PageElement> getElements() {
			return portfolioService.getPageParts(page);
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}
	}
}
