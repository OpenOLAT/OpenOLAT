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
 * Copyright (c) since 2009 at frentix GmbH, Switzerland<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.htmlheader.jscss;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperRegistry;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;

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
public class CustomCSS extends LogDelegator implements Disposable {
	private String mapperBaseURI;
	private String relCssFilename;
	private Mapper cssUriMapper;
	private MapperRegistry registry;
	private JSAndCSSComponent jsAndCssComp;
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
		cssUriMapper = new Mapper() {
			public MediaResource handle(String relPath,
					HttpServletRequest request) {
				VFSItem vfsItem = cssBaseContainer.resolve(relPath);
				MediaResource mr;
				if (vfsItem == null || !(vfsItem instanceof VFSLeaf))
					mr = new NotFoundMediaResource(relPath);
				else
					mr = new VFSMediaResource((VFSLeaf) vfsItem);
				return mr;
			}
		};
		this.relCssFilename = relCssFilename;
		this.registry = MapperRegistry.getInstanceFor(uSess);
		// Register mapper as cacheable
		String mapperID = VFSManager.getRealPath(cssBaseContainer);
		if (mapperID == null) {
			// Can't cache mapper, no cacheable context available
			this.mapperBaseURI  = registry.register(cssUriMapper);
		} else {
			// Add classname to the file path to remove conflicts with other
			// usages of the same file path
			mapperID = this.getClass().getSimpleName() + ":" + mapperID;
			this.mapperBaseURI  = registry.registerCacheable(mapperID, cssUriMapper);				
		}
		// initialize js and css component
		this.jsAndCssComp = new JSAndCSSComponent("jsAndCssComp", this.getClass(),
				null, null, false, null);
		String fulluri = mapperBaseURI + relCssFilename;
		// load CSS after the theme
		this.jsAndCssComp.addAutoRemovedCssPathName(fulluri, JSAndCSSAdder.CSS_INDEX_AFTER_THEME);
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
		return mapperBaseURI + relCssFilename;
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	public void dispose() {
		synchronized (DISPOSE_LOCK) {
			if (registry != null && cssUriMapper != null) {
				registry.deregister(cssUriMapper);
				registry = null;
				cssUriMapper = null;			
				jsAndCssComp = null;				
			}
		}
	}
}
