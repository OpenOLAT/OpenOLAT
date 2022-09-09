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
    	} else {// if the same DOM element exists
    		editor.initWindowListener(editor.settings);
    		editor.initInteractJs(editor.settings);
    	}
    	return editor;
	};
	
	var ContentEditor = function(container, params) {
		this.settings = $.extend({
			componentUrl: '',
			csrfToken: ''
		}, params);
		
		initWindowListener(this.settings);
		this.container = container;
		initInteractJs(this.settings);
	};
	
	function isTop(target, y) {
		var jElement = jQuery(target);
		var relativeY = y - jElement.offset().top;
		
		var middle;
		if(jElement.hasClass('oo-accepted-top')) {
			var placeHolderHeight = acceptedTopHeight(target);
			middle = ((jElement.height() - placeHolderHeight) / 2) + placeHolderHeight;
		} else {
			middle = jElement.height() / 2;
		}
		return relativeY > middle;
	}
	
	function acceptedTopHeight(target) {
		var style = window.getComputedStyle(target, ':before');
		return parseInt(style.height) + parseInt(style.marginTop) + parseInt(style.marginBottom);
	}
	
	function initInteractJs(settings) {
		var position = { x: 0, y: 0 };
		var $settings = settings;
		
		function setPositionClass(target, top) {
			if(top) {
				jQuery(target).addClass('oo-accepted');
				jQuery(target).removeClass('oo-accepted-top');
				jQuery(target).data('position', 'bottom');
			} else {
				jQuery(target).removeClass('oo-accepted');
				jQuery(target).addClass('oo-accepted-top');
				jQuery(target).data('position', 'top');
			}
		}
		
		interact('.o_page_part.o_page_part_view, .o_page_fragment_edit').draggable({
			autoScroll: true,
			ignoreFrom: '.o_page_part.o_page_edit form',
			allowFrom: '.o_page_drag_handle',
			modifiers: [
				interact.modifiers.restrict({
					restriction: '.o_page_content_editor'
				})
			],
			listeners: {
				start(event) {
					jQuery(event.target).addClass('oo-dragging');
				},
				move(event) {
					position.x += event.dx;
					position.y += event.dy;
					event.target.style.transform = `translate(${position.x}px, ${position.y}px)`;
				},
				end(event) {
					position.x = 0;
					position.y = 0;
					event.target.style.transform = 'none';
					jQuery(event.target).removeClass('oo-dragging');
					jQuery('.o_page_drop').removeClass('oo-accepted').removeClass('oo-accepted-top');
				}
			}
		});

		interact('.o_page_drop').dropzone({
			listeners: {
				move(event) {
					var top = isTop(event.target, event.dragEvent.page.y);
					setPositionClass(event.target, top);
				},
				drop: function(event) {
				    drop($settings, event, event.target, event.relatedTarget);
				}
			},
			checker: function (dragEvent, event, dropped, dropzone, dropElement, draggable, draggableElement) {
				var jDraggableElement = jQuery(draggableElement);
				var jDropElement = jQuery(dropElement);
				var isDragContainer = jDraggableElement.hasClass('o_page_container_edit');
				var isDragElement = jDraggableElement.hasClass('o_page_fragment_edit') && !isDragContainer;
				var isDropZoneContainerSlot = jDropElement.parents('.o_page_container_slot').length > 0
					|| jDropElement.hasClass('o_page_container_slot');
				return dropped && jDropElement.attr("id") != jDraggableElement.attr("id") &&
					((isDragContainer && !isDropZoneContainerSlot) || (isDragElement && isDropZoneContainerSlot));
			},
			ondragenter: function(event) {
				var top = isTop(event.target, event.dragEvent.page.y);
				setPositionClass(event.target, top);	
			},
			ondragleave: function(event) {
				jQuery(event.target)
					.removeClass('oo-accepted')
					.removeClass('oo-accepted-top')
					.data('position', null);
			}
		});
		return null;
	}
	
	function closeMathLive() {
		var mf = document.getElementById('mathlive');
		if(mf) {
			mf.executeCommand("hideVirtualKeyboard");
		}
	}
	
	function initWindowListener(settings) {
		if(o_info.contentEditorWindowListener === undefined || o_info.contentEditorWindowListener == null) {
			o_info.contentEditorWindowListener = function(e) {
				var componentUrl = jQuery(".o_page_content_editor").data("oo-content-editor-url");
				if(componentUrl === undefined || componentUrl == null) {
					jQuery(window).off('click', o_info.contentEditorWindowListener);
					o_info.contentEditorWindowListener = null;
				} else {
					var jTarget = jQuery(e.target);
					var excludedEls = jTarget.closest(".o_popover").length > 0
						|| jTarget.closest(".o_page_add_in_container").length > 0
						|| jTarget.closest(".tox-tinymce").length > 0
						|| jTarget.closest(".tox-dialog").length > 0
						|| jTarget.closest(".mce-content-body").length > 0
						|| jTarget.closest(".o_layered_panel .popover").length > 0
						|| jTarget.closest(".o_layered_panel .modal-dialog").length > 0
						|| jTarget.closest(".o_evaluation_editor_form").length > 0
						|| jTarget.closest(".o_page_with_side_options_wrapper").length > 0
						|| jTarget.closest(".tox-dialog-wrap__backdrop").length > 0
						|| jTarget.closest(".ML__keyboard").length > 0
						|| jTarget.closest(".o_ceditor_inspector").length > 0
						|| e.target.nodeName == 'BODY';
					
					if(!excludedEls) {
						var edited = jTarget.closest(".o_fragment_edited").length > 0;
						var parts = jTarget.closest(".o_page_part");
						if(jTarget.hasClass('o_page_container_tools') && !jTarget.parent().hasClass('o_page_container_edit')) {
							var containerUrl = jTarget.data("oo-content-editor-url");
							closeMathLive();
							o_XHREvent(containerUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'edit_fragment', 'fragment', jTarget.data('oo-page-fragment'));
						} else if(parts.length == 1) {
							var element = jQuery(parts.get(0));
							var elementUrl = element.data("oo-content-editor-url");
							closeMathLive();
							o_XHREvent(elementUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'edit_fragment', 'fragment', element.data('oo-page-fragment'));
						} else if(!edited) {
							closeMathLive();
							o_afterserver();
							o_XHREvent(componentUrl, false, false, '_csrf', settings.csrfToken, 'cid', 'close_edit_fragment', 'ignore-validating-error', 'oo-ignore-validating-error');
						}
					}
				}
			};
			jQuery(window).on('click', o_info.contentEditorWindowListener);
		}
	}
	
	function drop(settings, event, target, source) {
		var draggedId = jQuery(source).data('oo-page-fragment');
		
		var slotId = null;
		var containerId = null;
		var jElement = jQuery(target);
		var targetId = jElement.data('oo-page-fragment');
		if(jElement.hasClass('o_page_container_slot')) {
			slotId = jElement.data('oo-slot');
			containerId = jElement.closest(".o_page_part").data('oo-page-fragment');
		}

		var position = jElement.data('position');
		if(position == null) {
			var top = isTop(event.target, event.dragEvent.page.y);
			position = top ? "top": "bottom";	
		}

		var componentUrl = jElement.closest(".o_page_drop").data("oo-content-editor-url");

		jQuery(source).css('display', 'none');// seem successful, prevent transformation return of end listener
		o_XHREvent(componentUrl, false, false, "_csrf", settings.csrfToken, "cid", "drop_fragment", "fragment", targetId, "dragged", draggedId, "source", draggedId, "target", targetId, "container", containerId, "slot", slotId, "position", position);
	}
	
}(jQuery));