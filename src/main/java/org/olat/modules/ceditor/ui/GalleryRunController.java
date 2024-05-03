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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryRunController extends BasicController implements PageRunElement {

	private static final String[] SWIPER_JS = new String[] { "js/swiper/swiper-bundle.min.js" };

	private final VelocityContainer mainVC;
	private GalleryPart galleryPart;
	private GalleryImages galleryImages;
	private final boolean editable;

	@Autowired
	private MediaToPagePartDAO mediaToPagePartDAO;
	@Autowired
	private MediaService mediaService;

	public GalleryRunController(UserRequest ureq, WindowControl wControl, GalleryPart galleryPart, boolean editable) {
		super(ureq, wControl);
		this.galleryPart = galleryPart;
		this.editable = editable;
		mainVC = createVelocityContainer("gallery_run");
		mainVC.setElementCssClass("o_gallery_run_element_css_class");
		setBlockLayoutClass(galleryPart.getSettings());
		putInitialPanel(mainVC);
		initUI(ureq);
		updateUI();
	}

	private void setBlockLayoutClass(GallerySettings gallerySettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(gallerySettings, false));
	}

	private void initUI(UserRequest ureq) {
		galleryImages = new GalleryImages(new ArrayList<>());

		JSAndCSSComponent js = new JSAndCSSComponent("js", SWIPER_JS, null);
		mainVC.put("js", js);

		String mapperUrl = registerCacheableMapper(ureq, "gallery-" + galleryPart.getId(),
				new GalleryMapper(galleryPart, galleryImages, mediaService));
		mainVC.contextPut("mapperUrl", mapperUrl);
	}

	private void updateUI() {
		GallerySettings gallerySettings = galleryPart.getSettings();

		mainVC.contextPut("title", gallerySettings.getTitle());
		updateSwiperConfiguration(gallerySettings);

		mainVC.put("gallery.images", TextFactory.createTextComponentFromString("gallery.images",
				"image slider placeholder", "o_hint", false, mainVC));

		List<GalleryImageItem> galleryImageItems = mediaToPagePartDAO.loadRelations(galleryPart).stream()
				.map(r -> r.getMedia().getVersions().get(0))
				.map(mv -> new GalleryImageItem(Long.toString(mv.getKey()), mv.getMedia().getType(), mv,
						mv.getMedia().getTitle(), mv.getMedia().getDescription()))
				.toList();
		mainVC.contextPut("galleryImageItems", galleryImageItems);
		galleryImages.items.clear();
		galleryImages.items.addAll(galleryImageItems);
	}

	private void updateSwiperConfiguration(GallerySettings gallerySettings) {
		mainVC.contextPut("showPagination", false);
		mainVC.contextPut("showTopNavButtons", false);
		mainVC.contextPut("showSideNavButtons", false);
		mainVC.contextPut("showThumbnails", false);
		mainVC.contextPut("showTitleAndDescription", false);
		mainVC.contextPut("showRows", false);
		mainVC.contextPut("rows", 1);
		mainVC.contextPut("columns", 1);

		switch (gallerySettings.getType()) {
			case preview -> {
				mainVC.contextPut("showTopNavButtons", true);
				mainVC.contextPut("showThumbnails", true);
			}
			case grid -> {
				mainVC.contextPut("showPagination", true);
				mainVC.contextPut("showTopNavButtons", true);
				mainVC.contextPut("showRows", gallerySettings.getRows() > 1);
				mainVC.contextPut("rows", gallerySettings.getRows());
				mainVC.contextPut("columns", gallerySettings.getColumns());
			}
			case slideshow -> {
				mainVC.contextPut("showPagination", true);
				mainVC.contextPut("showSideNavButtons", true);
				mainVC.contextPut("showTitleAndDescription", true);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof GalleryPart updatedGalleryPart) {
				galleryPart = updatedGalleryPart;
				setBlockLayoutClass(galleryPart.getSettings());
				updateUI();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}

	public record GalleryImageItem(String id, String type, MediaVersion mediaVersion, String title,
								   String description) {}

	private record GalleryImages(List<GalleryImageItem> items) {

		public GalleryImageItem getImageById(String id) {
			if (!StringHelper.containsNonWhitespace(id)) {
				return null;
			}
			for (GalleryImageItem image : items) {
				if (id.equals(image.id)) {
					return image;
				}
			}
			return null;
		}
	}

	private record GalleryMapper(GalleryPart galleryPart, GalleryImages galleryImages,
								 MediaService mediaService) implements Mapper {

		@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				MediaResource mediaResource = null;

				String id = relPath.startsWith("/") ? relPath.substring(1) : relPath;
				GalleryImageItem image = galleryImages.getImageById(id);
				if (image != null) {
					MediaHandler mediaHandler = mediaService.getMediaHandler(image.type());
					if (mediaHandler instanceof ImageHandler imageHandler) {
						 VFSItem imageVfsItem = imageHandler.getImage(image.mediaVersion);
						 if (imageVfsItem instanceof VFSLeaf imageVfsLeaf) {
							 mediaResource = new VFSMediaResource(imageVfsLeaf);
						 }
					}
				}
				return mediaResource == null ? new NotFoundMediaResource() : mediaResource;
			}
		}
}
