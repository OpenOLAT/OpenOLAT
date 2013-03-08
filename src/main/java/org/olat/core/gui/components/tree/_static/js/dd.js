function onTreeStartDrag(event, ui) {
	jQuery(event.target).addClass('b_dd_proxy');
}

function onTreeStopDrag(event, ui) {
	jQuery(event.target).removeClass('b_dd_proxy');
}

function onTreeDrop(event, ui) {
	var dragEl = jQuery(ui.draggable[0]);
	var el = jQuery(this);
	el.css({position:'', width:''});
	var url =  el.droppable('option','endUrl');
	if(url.lastIndexOf('/') == (url.length - 1)) {
		url = url.substring(0,url.length-1);
	}
	var dragId = dragEl.attr('id')
	var targetId = dragId.substring(2, dragId.length);
	url += '%3Atnidle%3A' + targetId;

	var droppableId = el.attr('id');
	if(droppableId.indexOf('ds') == 0) {
		url += '%3Asne%3Ayes';
	} else if(droppableId.indexOf('dt') == 0) {
		url += '%3Asne%3Aend';
	}
	frames['oaa0'].location.href = url + '/';
}

function treeAcceptDrop(el) {
	var dragEl = jQuery(el);
	var dragElId = dragEl.attr('id');
	if(dragElId != undefined && (dragElId.indexOf('dd') == 0 ||
		dragElId.indexOf('ds') == 0 || dragElId.indexOf('dt') == 0 ||
		dragElId.indexOf('da') == 0 || dragElId.indexOf('row') == 0)) {

		var dropEl = jQuery(this)
		var dropElId = dropEl.attr('id');//dropped
		var dragNodeId = dragElId.substring(2, dragElId.length);
		var dropId = dropElId.substring(2, dropElId.length);
		if(dragNodeId == dropId) {
			return false;
		} 
		
		var sibling = "";
		if(dropElId.indexOf('ds') == 0) {
			sibling = "yes";
		} else if(dropElId.indexOf('dt') == 0) {
			sibling = "end";
		}
		
		var dropAllowed = dropEl.data(dragNodeId + "-" + sibling);
		if(dropAllowed === undefined) {
			var url = dropEl.droppable('option', 'fbUrl');
			//use prototype for the Ajax call
			jQuery.ajax(url, { 
				async: false,
				data: { nidle:dragNodeId, tnidle:dropId, sne:sibling },
				dataType: "json",
				method:'GET',
				success: function(data) {
					dropAllowed = data.dropAllowed;
				}
	  		});
			dropEl.data(dragNodeId + "-" + sibling, dropAllowed);
		}
		return dropAllowed;
	}
	return false;
}