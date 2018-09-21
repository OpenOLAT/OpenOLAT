/*
 * ========================================================
 *  <a href="http://www.openolat.org">
 *  OpenOLAT - Online Learning and Training</a><br>
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License"); <br>
 *  you may not use this file except in compliance with the License.<br>
 *  You may obtain a copy of the License at the
 *  <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 *  <p>
 *  Unless required by applicable law or agreed to in writing,<br>
 *  software distributed under the License is distributed on an "AS IS" BASIS, <br>
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 *  See the License for the specific language governing permissions and <br>
 *  limitations under the License.
 *  <p>
 *  Initial code contributed and copyrighted by<br>
 *  28.04.2014 by frentix GmbH, http://www.frentix.com
 *  <p>
 *  @author srosse, www.frentix.com
 *  @date Sept. 2018
 * ========================================================
 */
(function($) {
	"use strict";
    $.fn.ceditor = function(options) {
    	var editor = this.data("data-oo-ceditor");
    	if(typeof editor === "undefined") {
    		editor = new ContentEditor(this.get(0), options);
    		this.data("data-oo-ceditor", editor);
    	} else {
    		editor.initWindowListener();
    	}
    	return editor;
	};
	
	var ContentEditor = function(container, params) {
		this.settings = $.extend({
			componentUrl: ''
		}, params);
		
		initEdit();
		initWindowListener();
		this.container = container;
		this.drake = initDragAndDrop(container);
	};

	function initEdit() {
		jQuery(".o_page_part").each(function(index, el) {
			jQuery(el).on('click', function(e) {
				var element = jQuery(el);
				if(element.parents('.o_page_fragment_edit').length == 0
						&& jQuery(".o_page_fragment_edit", element).length == 0) {
					var componentUrl = jQuery(".o_page_content_editor").data("oo-content-editor-url");
					o_XHREvent(componentUrl, false, false, 'cid', 'edit_fragment', 'fragment', element.data('oo-page-fragment'));
				}
			});
		});
	}
	
	function initWindowListener() {
		if(o_info.contentEditorWindowListener === undefined || o_info.contentEditorWindowListener == null) {
			o_info.contentEditorWindowListener = function(e) {
				var componentUrl = jQuery(".o_page_content_editor").data("oo-content-editor-url");
				if(componentUrl === undefined || componentUrl == null) {
					jQuery(window).off('click', o_info.contentEditorWindowListener);
					o_info.contentEditorWindowListener = null;
				} else {
					var edited = jQuery(e.target).closest(".o_page_fragment_edit").length > 0
						|| jQuery(e.target).closest(".o_page_side_options").length > 0;
					if(!edited) {
						o_XHREvent(componentUrl, false, false, 'cid', 'close_edit_fragment');
					}
				}
			};
			jQuery(window).on('click', o_info.contentEditorWindowListener);
		}
	}
	
	function initDragAndDrop(container) {
		var drake = dragula([container], {
			isContainer: function(el) {
				return jQuery(el).hasClass('o_page_drop');
			},
			copy: function (el, source) {
			    return false;
			},
			accepts: function (el, target) {
				return jQuery(target).hasClass('o_page_drop');
			},
			moves: function (el, targetContainer, handle) {
				return jQuery(handle).hasClass('o_page_tools_dd') ;
			}
		});

		drake.on('dragend', function(el) {
			cleanAcceptMarker(container);
		}).on('cancel', function() {
			cleanAcceptMarker(container);
		}).on('over', function(el, target, source) {
			jQuery(target).addClass('oo-accepted');
		}).on('out', function(el, target, source) {
			jQuery(target).removeClass('oo-accepted');
		}).on('drop', function(el, target, source, sibling) {
			drake.destroy();// drop trigger a reload -> clean up all and more
			drop(el, target, source, sibling);
		});
		
		return drake;
	}
	
	function drop(el, target, source, sibling) {
		var draggedId = jQuery(el).data('oo-page-fragment');
		var sourceId = jQuery(source).data('oo-page-fragment');
		
		var slotId = null;
		var targetId = null;
		var siblingId = null;
		var containerId = null;

		var jElement = null;
		if(sibling != null) {
			jElement = jQuery(sibling);
			siblingId = jElement.closest(".o_page_drop").data('oo-page-fragment');
		} else {
			jElement = jQuery(target);
			if(jElement.hasClass('o_page_container_slot')) {
				slotId = jElement.data('oo-slot');
				containerId = jElement.closest(".o_page_part").data('oo-page-fragment');
			}
		}
		
		if(slotId == null) {
			targetId = jElement.data('oo-page-fragment');
			slotId = jElement.closest(".o_page_container_slot").data('oo-slot');
			if(slotId != null) {
				containerId = jElement.closest(".o_page_container_slot").closest('.o_page_part').data('oo-page-fragment');
			}
		}
		var componentUrl = jQuery(el).closest(".o_page_content_editor").data("oo-content-editor-url");
		o_XHREvent(componentUrl, false, false, "cid", "drop_fragment", "dragged", draggedId, "source", sourceId, "target", targetId, "sibling", siblingId, "container", containerId, "slot", slotId);
	}
	
	function cleanAcceptMarker(container) {
		jQuery("div.oo-accepted", container).each(function(index, acceptedEl) {
			jQuery(acceptedEl).removeClass('oo-accepted');
		});
	}
}(jQuery));