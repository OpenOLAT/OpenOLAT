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

package org.olat.modules.wiki.gui.components.wikiToHtml;

import java.util.Collections;
import java.util.List;

import org.jamwiki.DataHandler;
import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.wiki.WikiContainer;
import org.olat.modules.wiki.WikiPage;

/**
 * Description:<br>
 * This component renders a string containing media wiki syntax to html
 * for a reference of the media wiki syntax see: http://meta.wikimedia.org/wiki/Help:Editing
 * <P>
 * Initial Date: May 17, 2006 <br>
 * 
 * @author guido
 */
public class WikiMarkupComponent extends AbstractComponent implements Disposable {
	// single renderer for all users, lazy creation upon first object cration of
	// this class.
	private static final ComponentRenderer RENDERER = new WikiMarkupRenderer();
	private String wikiContent;
	private int minHeight;
	private ParserInput parserInput;
	private JFlexParser parser;
	private OLATResourceable ores;
	private OlatWikiDataHandler datahandler;
	private String imageBaseUri;
	private MapperKey mapperKey;
	
	public WikiMarkupComponent(String name, OLATResourceable ores, int minHeight) {
		super(name);
		this.ores = ores;
		this.minHeight = Math.max(minHeight, 15);
		
		//configure wiki parser
		LocalFolderImpl tempFolder =  VFSManager.olatRootContainer("/tmp", null);
		Environment.setValue(Environment.PROP_BASE_FILE_DIR, tempFolder.getBasefile().getAbsolutePath());
		Environment.setValue(Environment.PROP_DB_TYPE, "org.olat.core.gui.components.wikiToHtml.OlatWikiDataHandler");
	}

	@Override
	public void dispose() {
		if(mapperKey != null) {
			List<MapperKey> mappers = Collections.<MapperKey>singletonList(mapperKey);
			CoreSpringFactory.getImpl(MapperService.class).cleanUp(mappers);
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String moduleUri = ureq.getModuleURI();
		//FIXME:gs:a access string constants by NameSpaceHandler
		if (moduleUri.startsWith("Special:Edit")) {
			String topic = moduleUri.substring(moduleUri.indexOf("topic=")+6,moduleUri.length());
			if (topic.length() > 175) fireEvent(ureq, new ErrorEvent("wiki.error.too.long"));
			else if (topic.length() == 0) fireEvent(ureq, new ErrorEvent("wiki.error.contains.bad.chars"));
			else fireEvent(ureq, new RequestNewPageEvent(topic));
			
		} else if( moduleUri.startsWith("Media:")) { // these are media links like pdf or audio files
			fireEvent(ureq, new RequestMediaEvent(moduleUri.substring(6, moduleUri.length())));
			
		} else if (moduleUri.startsWith("Image:")) {
			fireEvent(ureq, new RequestImageEvent(moduleUri.substring(6, moduleUri.length())));
			
			//trap special pages (like: Special:Upload) which are not yet implemented in OLAT
		} else if (moduleUri.startsWith("Special:Upload")) {
			fireEvent(ureq, new ErrorEvent("wiki.error.file.not.found"));
			
		} else if (moduleUri.equals("")) fireEvent(ureq, new RequestPageEvent(WikiPage.WIKI_INDEX_PAGE));
		
		//default is request a page
		else fireEvent(ureq, new RequestPageEvent(moduleUri));
		setDirty(true);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public String getWikiContent() {
		return wikiContent;
	}

	public void setWikiContent(String wikiContent) {
		this.wikiContent = wikiContent;
		setDirty(true);
	}

	/**
	 * if the wiki need to serve images you have to set the image mapper uri first!
	 * The mapper creates an user session based mapper for the media files which can be requested by calling @see getImageBaseUri()
	 * @param ureq
	 * @param wikiContainer
	 */
	public void setImageMapperUri(UserRequest ureq, final VFSContainer wikiContainer) {
		// get a usersession-local mapper for images in this wiki
		Mapper contentMapper = new VFSContainerMapper(wikiContainer);
		
		// Register mapper as cacheable
		String mapperID = VFSManager.getRealPath(wikiContainer);
		if (mapperID == null) {
			// Can't cache mapper, no cacheable context available
			mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), contentMapper);
		} else {
			// Add classname to the file path to remove conflicts with other
			// usages of the same file path
			mapperID = this.getClass().getSimpleName() + ":" + mapperID;
			mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(ureq.getUserSession(), mapperID, contentMapper);				
		}
		imageBaseUri = mapperKey.getUrl() + "/" + WikiContainer.MEDIA_FOLDER_NAME + "/";
	}
	
	/**
	 * 
	 * @return
	 */
	public String getImageBaseUri() {
		if (this.imageBaseUri == null ) throw new AssertException("the uri ist null, you must call setImageMapperUri first!");
		return this.imageBaseUri;
	}

	@Override
	public String getExtendedDebugInfo() {
		// see velocitycontainer on how to implement
		return null;
	}

	protected ParserInput getParserInput() {
		return parserInput;
	}

	protected JFlexParser getParser() {
		return parser;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/openolat/wiki.js");
	}

	/**
	 * @return the min height the wiki content display div should have
	 */
	protected int getMinHeight() {
		return minHeight;
	}

	protected OLATResourceable getOres() {
		return ores;
	}

	/**
	 * returns the datahandler for the jamwiki parser
	 * @see org.jamwiki.DataHandlerLookup#lookupDataHandler()
	 */
	public DataHandler lookupDataHandler() {
		return datahandler;
	}
}
