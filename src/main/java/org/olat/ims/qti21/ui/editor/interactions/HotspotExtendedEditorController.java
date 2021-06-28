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
package org.olat.ims.qti21.ui.editor.interactions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.types.Identifier;


/**
 * 
 * Initial date: 2 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotExtendedEditorController extends FormBasicController {
	
	private static final String SHAPE_SUFFIX = "_ext_shape";
	private static final String COORDS_SUFFIX = "_ext_coords";
	
	private FormLink cloneButton;
	private FormLink newRectButton;
	private FormLink newCircleButton;
	private MultipleSelectionElement selectedHotspotsEl;
	
	private final Size size;
	private final File objectImg;
	private final String layoutCssClass;
	private final String backgroundMapperUri;
	private final List<SpotWrapper> choiceWrappers = new ArrayList<>();

	@Autowired
	private ImageService imageService;
	
	public HotspotExtendedEditorController(UserRequest ureq, WindowControl wControl, File itemFile,
			File objectImg, List<HotspotWrapper> hotspotWrappers, String layoutCssClass) {
		super(ureq, wControl, "hotspots_ext", Util.createPackageTranslator(AssessmentTestEditorController.class, ureq.getLocale()));
		this.objectImg = objectImg;
		for(HotspotWrapper hotspotWrapper:hotspotWrappers) {
			choiceWrappers.add(new SpotWrapper(hotspotWrapper));
		}
		this.layoutCssClass = layoutCssClass;
		if(objectImg != null) {
			size = imageService.getSize(new LocalFileImpl(objectImg), null);
		} else {
			size = new Size(400, 300, false);
		}
		backgroundMapperUri = registerMapper(ureq, new BackgroundMapper(itemFile));
		
		initForm(ureq);
		rebuildSelectedSelection();
	}
	
	public List<SpotWrapper> getSpots()  {
		return new ArrayList<>(choiceWrappers);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("hotspots", choiceWrappers);
			layoutCont.contextPut("mapperUri", backgroundMapperUri);
			layoutCont.contextPut("layoutCssClass", layoutCssClass);
			layoutCont.contextPut("restrictedEdit", Boolean.FALSE);
			if(size != null && size.getHeight() > 0 && size.getWidth() > 0) {
				layoutCont.contextPut("height", Integer.toString(size.getHeight()));
				layoutCont.contextPut("width", Integer.toString(size.getWidth()));
			} else {
				layoutCont.contextPut("height", 400);
				layoutCont.contextPut("width", 300);
			}
			layoutCont.contextPut("hotspotSelections", "");
			if(objectImg != null) {
				layoutCont.contextPut("filename", objectImg.getName());
			}
			layoutCont.contextPut("hotspots", choiceWrappers);
			layoutCont.getFormItemComponent().addListener(this);
		}
		
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", new String[] {
				(Settings.isDebuging() ? "js/interactjs/interact.js" : "js/interactjs/interact.min.js"),
				"js/jquery/openolat/jquery.drawing.v2.js"
			});
		formLayout.add(js);
		
		newCircleButton = uifactory.addFormLink("new.circle", "new.circle", null, formLayout, Link.BUTTON);
		newCircleButton.setIconLeftCSS("o_icon o_icon-lg o_icon_circle");
		
		newRectButton = uifactory.addFormLink("new.rectangle", "new.rectangle", null, formLayout, Link.BUTTON);
		newRectButton.setIconLeftCSS("o_icon o_icon-lg o_icon_rectangle");
		
		cloneButton = uifactory.addFormLink("clone.hotspots", "clone.hotspots", null, formLayout, Link.BUTTON);
		
		String[] emptyKeys = new String[0];
		selectedHotspotsEl = uifactory.addCheckboxesHorizontal("form.imd.select.spots", null, formLayout, emptyKeys, emptyKeys);
		selectedHotspotsEl.setElementCssClass("o_sel_assessment_item_correct_spots");
		selectedHotspotsEl.addActionListener(FormEvent.ONCHANGE);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("transfert", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateHotspots(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newCircleButton == source) {
			updateHotspots(ureq);
			createHotspotChoice(Shape.CIRCLE, "60,60,25");
		} else if(newRectButton == source) {
			updateHotspots(ureq);
			createHotspotChoice(Shape.RECT, "50,50,100,100");	
		} else if(cloneButton == source) {
			updateHotspots(ureq);
			cloneHotspots(ureq.getParameter("hotspots_selection"));
		} else if(selectedHotspotsEl == source) {
			updateHotspots(ureq);
			doSelectAnswers(selectedHotspotsEl.getSelectedKeys());
			flc.setDirty(true);
		} else if(flc == source) {
			updateHotspots(ureq);
			String deleteHotspot = ureq.getParameter("delete-hotspot");
			if(StringHelper.containsNonWhitespace(deleteHotspot)) {
				doDeleteHotspot(deleteHotspot);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void cloneHotspots(String selection) {
		List<SpotWrapper> wrappersToClone = getWrappers(selection);
		for(SpotWrapper wrapper:wrappersToClone) {
			Shape shape = Shape.parseShape(wrapper.getShape());
			String translatedCoords = translateCoords(shape, wrapper.getCoords());
			createHotspotChoice(shape, translatedCoords);
		}
		rebuildSelectedSelection();
	}
	
	private String translateCoords(Shape shape, String coords) {
		if(shape == Shape.CIRCLE) {
			List<Integer> coordList = AssessmentItemFactory.coordsList(coords);
			if(coordList.size() == 3) {
				coords = translateCoords(5, coordList, 2);
			}
		} else if(shape == Shape.RECT) {
			List<Integer> coordList = AssessmentItemFactory.coordsList(coords);
			if(coordList.size() == 4) {
				coords = translateCoords(5, coordList, 4);
			}
		}
		return coords;
	}
	
	private String translateCoords(int pixels, List<Integer> coordList, int maxIndex) {
		for(int i=0; i<maxIndex; i++) {
			coordList.set(i, Integer.valueOf(pixels + coordList.get(i).intValue()));
		}
		return AssessmentItemFactory.coordsString(coordList);
	}
	
	private List<SpotWrapper> getWrappers(String selection) {
		Map<String,SpotWrapper> wrapperMap = new HashMap<>();
		for(SpotWrapper wrapper:choiceWrappers) {
			wrapperMap.put(wrapper.getIdentifier(), wrapper);
		}
		
		List<SpotWrapper> wrappers = new ArrayList<>();
		String[] ids = selection.split(" ");
		for(String id:ids) {
			SpotWrapper wrapper = wrapperMap.get(id);
			if(wrapper != null) {
				wrappers.add(wrapper);
			}
		}
		return wrappers;
	}
	
	private void createHotspotChoice(Shape shape, String coords) {
		Identifier identifier = IdentifierGenerator.newNumberAsIdentifier("hc");
		choiceWrappers.add(new SpotWrapper(shape, coords, identifier));
		rebuildSelectedSelection();
	}
	
	private void updateHotspots(UserRequest ureq) {
		Map<String,SpotWrapper> wrapperMap = new HashMap<>();
		for(SpotWrapper wrapper:choiceWrappers) {
			wrapperMap.put(wrapper.getIdentifier(), wrapper);
		}
		
		for(Enumeration<String> parameterNames = ureq.getHttpReq().getParameterNames(); parameterNames.hasMoreElements(); ) {
			String name = parameterNames.nextElement();
			String value = ureq.getHttpReq().getParameter(name);
			if(name.endsWith(SHAPE_SUFFIX)) {
				String hotspotIdentifier = name.substring(0, name.length() - SHAPE_SUFFIX.length());
				SpotWrapper spot = wrapperMap.get(hotspotIdentifier);
				if(spot != null) {
					spot.setShape(value);
				}
			} else if(name.endsWith(COORDS_SUFFIX)) {
				String hotspotIdentifier = name.substring(0, name.length() - COORDS_SUFFIX.length());
				SpotWrapper spot = wrapperMap.get(hotspotIdentifier);
				if(spot != null) {
					spot.setCoords(value);
				}
			}
		}
		
		String selection = ureq.getParameter("hotspots_selection");
		flc.contextPut("hotspotSelections", selection);
	}
	
	private void rebuildSelectedSelection() {
		KeyValues keyValues = new KeyValues();
		for(int i=0; i<choiceWrappers.size(); i++) {
			SpotWrapper choice = choiceWrappers.get(i);
			keyValues.add(KeyValues.entry(choice.getIdentifier(), translate("position.hotspot", new String[] { Integer.toString(i + 1) })));
		}
		selectedHotspotsEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		for(SpotWrapper spot:choiceWrappers) {
			selectedHotspotsEl.select(spot.getIdentifier(), spot.isSelected());
		}
		flc.setDirty(true);
	}
	
	private void doSelectAnswers(Collection<String> selectedResponseIds) {
		StringBuilder sb = new StringBuilder();
		for(SpotWrapper wrapper:choiceWrappers) {
			boolean selected = selectedResponseIds.contains(wrapper.getIdentifier());
			if(selected) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(wrapper.getIdentifier());
			}
			wrapper.setSelected(selected);
		}
		flc.contextPut("hotspotSelections", sb.toString());
	}
	
	private void doDeleteHotspot(String hotspotId) {
		choiceWrappers.removeIf(w -> w.getIdentifier().equals(hotspotId));
		rebuildSelectedSelection();
	}
	
	public static class SpotWrapper {
		
		private String shape;
		private String coords;
		private String identifier;
		private boolean selected = false;
		
		public SpotWrapper(HotspotWrapper hotspot) {
			coords = hotspot.getCoords();
			shape = hotspot.getShape();
			identifier = hotspot.getIdentifier();
		}
		
		public SpotWrapper(Shape shape, String coords, Identifier identifier) {
			this.shape = shape.toQtiString();
			this.coords = coords;
			this.identifier = identifier.toString();
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public String getShape() {
			return shape;
		}
		
		public void setShape(String shape) {
			this.shape = shape;
		}
		
		public String getCoords() {
			return coords;
		}
		
		public void setCoords(String coords) {
			this.coords = coords;
		}
		
		public String getIdentifier() {
			return identifier;
		}
	}
}
