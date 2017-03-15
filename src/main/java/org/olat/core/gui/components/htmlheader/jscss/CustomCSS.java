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
package org.olat.core.gui.components.htmlheader.jscss;

import java.util.Collections;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSManager;

/**
 * Description:<br>
 * The custom CSS object is a container to hold everything the system needs to
 * know about a custom CSS: the mapper and the css URL <br>
 * This object is used by dynamic tabs or sites to define a custom CSS that
 * should only be loaded when this tab or site is shown.
 * 
 * <P>
 * Initial Date: 25.03.2009 <br>
 * 
 * @author gnaegi
 */
public class CustomCSS implements Disposable {

	private final String relCssFilename;
	private final String relCssFileIframe;
	private final Mapper cssUriMapper;
	private final MapperKey cssUriMapperKey;
	private final JSAndCSSComponent jsAndCssComp;
	private Object DISPOSE_LOCK = new Object();

	/**
	 * Constructor
	 * 
	 * @param cssBaseContainer
	 *            The base container where the CSS files and the resources are
	 *            located
	 * @param relCssFilename
	 *            The css file relative to the base container
	 * @param uSess
	 *            The user session (needed to register the mapper)
	 */
	public CustomCSS(final VFSContainer cssBaseContainer,
			final String relCssFilename, UserSession uSess) {
		cssUriMapper = createCSSUriMapper(cssBaseContainer);
		this.relCssFilename = relCssFilename;
		this.relCssFileIframe = null;
		cssUriMapperKey = registerMapper(cssBaseContainer, uSess);
		// initialize js and css component
		jsAndCssComp = new JSAndCSSComponent("jsAndCssComp", this.getClass(), false);
		String fulluri = cssUriMapperKey.getUrl() + relCssFilename;
		// load CSS after the theme
		jsAndCssComp.addAutoRemovedCssPathName(fulluri, JSAndCSSAdder.CSS_INDEX_AFTER_THEME);
	}
	
	public CustomCSS(final VFSContainer cssBaseContainer,
			final String relCssFileMain, final String relCssFileIFrame, UserSession uSess ) {
		cssUriMapper = createCSSUriMapper(cssBaseContainer);
		this.relCssFilename = relCssFileMain;
		this.relCssFileIframe = relCssFileIFrame;
		cssUriMapperKey = registerMapper(cssBaseContainer, uSess);
		
		// initialize js and css component
		jsAndCssComp = new JSAndCSSComponent("jsAndCssComp", this.getClass(), false);
		String fulluri = cssUriMapperKey.getUrl() + relCssFilename;
		// load CSS after the theme
		jsAndCssComp.addAutoRemovedCssPathName(fulluri, JSAndCSSAdder.CSS_INDEX_AFTER_THEME);
	}

	/**
	 * @param cssBaseContainer
	 * @param uSess
	 */
	private MapperKey registerMapper(final VFSContainer cssBaseContainer, UserSession uSess) {
		// Register mapper as cacheable
		String mapperID = VFSManager.getRealPath(cssBaseContainer);
		MapperKey mapperKey;
		if (mapperID == null) {
			// Can't cache mapper, no cacheable context available
			mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(uSess, cssUriMapper);
		} else {
			// Add classname to the file path to remove conflicts with other
			// usages of the same file path
			mapperID = this.getClass().getSimpleName() + ":" + mapperID + CodeHelper.getRAMUniqueID();
			mapperKey = CoreSpringFactory.getImpl(MapperService.class).register(uSess, mapperID, cssUriMapper);
		}
		return mapperKey;
	}

	/**
	 * @param cssBaseContainer
	 */
	private Mapper createCSSUriMapper(final VFSContainer cssBaseContainer) {
		return new VFSContainerMapper(cssBaseContainer);
	}

	/**
	 * Get the js and css component that embedds the CSS file
	 * 
	 * @return
	 */
	public JSAndCSSComponent getJSAndCSSComponent() {
		return jsAndCssComp;
	}

	/**
	 * Get the CSS URL to manually embedd the CSS (e.g. in an iframe)
	 * 
	 * @return
	 */
	public String getCSSURL() {
		return cssUriMapperKey.getUrl() + relCssFilename;
	}

	/**
	 * @return Returns the relCssFileIframe.
	 */
	public String getCSSURLIFrame() {
		if (relCssFileIframe != null) {
			return cssUriMapperKey.getUrl() + relCssFileIframe;
		} else {
			return getCSSURL();
		}
	}

	@Override
	public String toString() {
		return "customCss[url=" + cssUriMapperKey.getUrl() + "]" + super.toString();
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	@Override
	public void dispose() {
		synchronized (DISPOSE_LOCK) {
			if (cssUriMapperKey != null) {
				CoreSpringFactory.getImpl(MapperService.class).cleanUp(Collections.singletonList(cssUriMapperKey));				
			}
		}
	}
}
