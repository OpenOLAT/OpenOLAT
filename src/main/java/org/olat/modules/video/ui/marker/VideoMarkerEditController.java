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
package org.olat.modules.video.ui.marker;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.model.VideoMarkerImpl;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayController.Marker;
import org.olat.modules.video.ui.VideoHelper;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.modules.video.ui.component.VideoMarkerTextCellRenderer;
import org.olat.modules.video.ui.component.VideoTimeCellRenderer;
import org.olat.modules.video.ui.event.MarkerMovedEvent;
import org.olat.modules.video.ui.event.MarkerResizedEvent;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.modules.video.ui.marker.VideoMarkersTableModel.MarkerCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 * Initial date: 27 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMarkerEditController extends BasicController {

	private VideoMarkersController markersCtrl;
	private VideoDisplayController videoDisplayCtr;
	
	private String currentTimeCode;
	private long durationInSeconds;
	private final RepositoryEntry entry;
	private final String videoElementId;
	
	public VideoMarkerEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale()));
		this.entry = entry;

		VelocityContainer mainVC = createVelocityContainer("markers_overview");
		videoDisplayCtr = new VideoDisplayController(ureq, getWindowControl(), entry, false, false, false, false, null, false, false, null, false);
		videoDisplayCtr.setAlwaysShowControls(true);
		videoDisplayCtr.setDragMarkers(true);
		videoDisplayCtr.setPosterEnabled(false);
		videoDisplayCtr.setClickToPlayPause(false);
		videoElementId = videoDisplayCtr.getVideoElementId();
		listenTo(videoDisplayCtr);
		mainVC.put("video", videoDisplayCtr.getInitialComponent());
		
		markersCtrl = new VideoMarkersController(ureq, getWindowControl());
		listenTo(markersCtrl);
		mainVC.put("markers", markersCtrl.getInitialComponent());

		durationInSeconds = VideoHelper.durationInSeconds(entry, videoDisplayCtr);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == videoDisplayCtr) {
			if (event instanceof VideoEvent) {
				VideoEvent videoEvent = (VideoEvent) event;
				currentTimeCode = videoEvent.getTimeCode();
				if(StringHelper.containsNonWhitespace(videoEvent.getDuration()) && !"NaN".equals(videoEvent.getDuration())) {
					try {
						durationInSeconds = Math.round(Double.parseDouble(videoEvent.getDuration()));
					} catch (NumberFormatException e) {
						//don't panic
					}
				}
			} else if(event instanceof MarkerMovedEvent || event instanceof MarkerResizedEvent) {
				markersCtrl.event(ureq, videoDisplayCtr, event);
			}
		}
	}
	
	private void selectTime(Date time) {
		long timeInSeconds = time.getTime() / 1000l;
		SelectTimeCommand selectTime = new SelectTimeCommand(videoElementId, timeInSeconds);
		getWindowControl().getWindowBackOffice().sendCommandTo(selectTime);
	}
	
	private void reloadMarkers() {
		List<Marker> markers = videoDisplayCtr.loadMarkers();
		ReloadMarkersCommand reloadMarkers = new ReloadMarkersCommand(videoElementId, markers);
		getWindowControl().getWindowBackOffice().sendCommandTo(reloadMarkers);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public class VideoMarkersController extends FormBasicController {
		
		private FormLink addMarkerEl;
		private FlexiTableElement tableEl;
		private VideoMarkersTableModel tableModel;
		
		private CloseableModalController cmc;
		private MarkerEditController markerAddCtrl;
		private MarkerEditController markerEditCtrl;
		
		private VideoMarkers markers;
		
		@Autowired
		private VideoManager videoManager;
		
		public VideoMarkersController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "markers_list", Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale()));
			
			initForm(ureq);
			loadModel(true);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MarkerCols.text, "select", new VideoMarkerTextCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MarkerCols.start, new VideoTimeCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
			tableModel = new VideoMarkersTableModel(columnsModel);

			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
			tableEl.setCustomizeColumns(false);
			tableEl.setNumOfRowsEnabled(false);
			
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonGroupLayout);
			addMarkerEl = uifactory.addFormLink("video.marker.add", buttonGroupLayout, Link.BUTTON);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(videoDisplayCtr == source) {
				if(event instanceof MarkerMovedEvent) {
					doMoveMarker((MarkerMovedEvent)event);
				} else if(event instanceof MarkerResizedEvent) {
					doResizeMarker((MarkerResizedEvent)event);
				}
			} else if(markerAddCtrl == source) {
				if(event == Event.DONE_EVENT) {
					doAddMarker(markerAddCtrl.getMarker());
				}
				cmc.deactivate();
				cleanUp();
			} else if(markerEditCtrl == source) {
				if(event == Event.DONE_EVENT) {
					doEditMarker(markerEditCtrl.getMarker());
				}
				cmc.deactivate();
				cleanUp();
			} else if(cmc == source) {
				cleanUp();
			}
			super.event(ureq, source, event);
		}
		
		private void cleanUp() {
			removeAsListenerAndDispose(markerEditCtrl);
			removeAsListenerAndDispose(markerAddCtrl);
			removeAsListenerAndDispose(cmc);
			markerEditCtrl = null;
			markerAddCtrl = null;
			cmc = null;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(addMarkerEl == source) {
				doAddMarker(ureq);
			} else if(tableEl == source) {
				if(event instanceof SelectionEvent) {
					SelectionEvent se = (SelectionEvent)event;
					if("edit".equals(se.getCommand())) {
						VideoMarker row = tableModel.getObject(se.getIndex());
						doEditMarker(ureq, row);
					} else if("delete".equals(se.getCommand())) {
						VideoMarker row = tableModel.getObject(se.getIndex());
						doDeleteMarker(row);
					} else if("select".equals(se.getCommand())) {
						VideoMarker row = tableModel.getObject(se.getIndex());
						selectTime(row.getBegin());
					}
				}
			}
			super.formInnerEvent(ureq, source, event);
		}
		
		private void loadModel(boolean reset) {
			markers = videoManager.loadMarkers(entry.getOlatResource());
			List<VideoMarker> rows = markers.getMarkers();
			if(rows.size() > 1) {
				Collections.sort(rows, new VideoMarkerRowComparator());
			}
			tableModel.setObjects(rows);
			tableEl.reset(reset, reset, true);
		}
		
		private void doMoveMarker(MarkerMovedEvent event) {
			VideoMarker marker = markers.getMarkerById(event.getMarkerId());
			if(marker != null) {
				marker.setTop(event.getTop());
				marker.setLeft(event.getLeft());
				videoManager.saveMarkers(markers, entry.getOlatResource());
			}
		}
		
		private void doResizeMarker(MarkerResizedEvent event) {
			VideoMarker marker = markers.getMarkerById(event.getMarkerId());
			if(marker != null) {
				marker.setWidth(event.getWidth());
				marker.setHeight(event.getHeight());
				videoManager.saveMarkers(markers, entry.getOlatResource());
			}
		}

		private void doAddMarker(UserRequest ureq) {
			if(markerAddCtrl != null) return;
			
			VideoMarkerImpl row = new VideoMarkerImpl();
			row.setId(UUID.randomUUID().toString());
			
			if(currentTimeCode != null) {
				long time = Math.round(Double.parseDouble(currentTimeCode)) * 1000l;
				row.setBegin(new Date(time));
			}
			markerAddCtrl = new MarkerEditController(ureq, getWindowControl(), row, durationInSeconds); 
			listenTo(markerAddCtrl);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"), markerAddCtrl.getInitialComponent(), 
					true, translate("video.marker.title.add"));
			listenTo(cmc);
			cmc.activate();
		}
		
		private void doAddMarker(VideoMarker marker) {
			markers.getMarkers().add(marker);
			videoManager.saveMarkers(markers, entry.getOlatResource());
			loadModel(true);
			reloadMarkers();
			selectTime(marker.getBegin());
		}
		
		private void doEditMarker(UserRequest ureq, VideoMarker row) {
			if(markerEditCtrl != null) return;

			markerEditCtrl = new MarkerEditController(ureq, getWindowControl(), row, durationInSeconds); 
			listenTo(markerEditCtrl);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"), markerEditCtrl.getInitialComponent(), 
					true, translate("video.marker.title.edit"));
			listenTo(cmc);
			cmc.activate();
		}
		
		private void doEditMarker(VideoMarker marker) {
			markers.getMarkers().remove(marker);
			markers.getMarkers().add(marker);
			videoManager.saveMarkers(markers, entry.getOlatResource());
			loadModel(false);
			reloadMarkers();
			selectTime(marker.getBegin());
		}
		
		private void doDeleteMarker(VideoMarker marker) {
			markers.getMarkers().remove(marker);
			videoManager.saveMarkers(markers, entry.getOlatResource());
			loadModel(true);
			reloadMarkers();
			selectTime(marker.getBegin());
		}
	}
}
