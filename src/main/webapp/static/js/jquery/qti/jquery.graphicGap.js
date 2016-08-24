(function ($) {
    $.fn.graphicGapInteraction = function(options) {
    	var settings = $.extend({
    		maphilight: null,
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		opened: false
        }, options );
    	
    	try {
    		if(!(typeof settings.responseValue === "undefined") && settings.responseValue.length > 0) {
    			drawGaps(this, settings);
    		}
    		if(settings.opened) {
    			associateGaps(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawGaps($obj, settings) {
    	var containerId = $obj.attr('id');
    	var divContainer = jQuery('#' + containerId);
    	
    	var pairs = settings.responseValue.split(',');
    	for(var i=pairs.length; i-->0; ) {
    		var ids = pairs[i].split(' ');
    		
    		var item1 = jQuery('#ac_' + settings.responseIdentifier + '_' + ids[0]);
    		var item2 = jQuery('#ac_' + settings.responseIdentifier + '_' + ids[1]);

    		var gapitem, areaEl;
    		if(item1.hasClass('gap_item')) {
    			gapitem = item1;
    			areaEl = item2;
    		} else {
    			gapitem = item2;
    			areaEl = item1;
    		}
    		
    		var coords = toCoords(areaEl);
    		gapitem.css('position','absolute');
    		gapitem.css('left', coords[0] + 'px');
    		gapitem.css('top', coords[1] + 'px');
    		gapitem.addClass('oo-choosed');

			var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
				.attr('value', gapitem.data('qti-id') + " " + areaEl.data('qti-id'));
			divContainer.prepend(inputElement);
    	}
    };

    function associateGaps($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery(".gap_item").on('click', function(e, el) {
    		var gapitem = jQuery(this);
    		
    		if(gapitem.hasClass('oo-choosed')) {
    			//timestamp
    			var dropTimestamp = gapitem.data('drop-timestamp');
    			if(!(typeof dropTimestamp === "undefined") && dropTimestamp != null) {
    				if((Date.now() - dropTimestamp) < 1500) {
    					gapitem.data('drop-timestamp', null)
    					return;
    				}
    			}

    			gapitem.removeClass('oo-choosed');
    			var gapitemIdentifier = gapitem.data('qti-id');
    			//remove
    			jQuery('#' + containerId).find("input[type='hidden']").each(function(index, el) {
    				var value = jQuery(el).val();
    				if(value.indexOf(gapitemIdentifier + ' ') == 0) {
    					jQuery(el).remove();
    				}
    			});
    			gapitem.css({'position':'relative', 'left': '0px', 'top': '0px' });
    		} else {
    			//gapitem.css('border','3px solid grey');
    			gapitem.addClass('oo-selected');
    		}
    	}).draggable({
    		containment: "#" + containerId,
    		scroll: false,
    		revert: "invalid",
    		stop: function(event, ui) {
    			if(!jQuery(this).hasClass('oo-choosed')) {
    				jQuery(this).css({'position':'relative', 'left': '0px', 'top': '0px' });
    			}
    		}
    	}).mousemove(function(event, ui) {
    		var itemEl = jQuery(this);
    		if(itemEl.hasClass("ui-draggable-dragging")) {
    			var containerEl = jQuery("#" + containerId + "_img");
    			var containerOffset = containerEl.offset();
    			var offset = itemEl.offset(); 
                var x1 = offset.left + (itemEl.width() / 2) - containerOffset.left;
                var y1 = offset.top + (itemEl.height() / 2) - containerOffset.top;
                var target = dropTarget(x1, y1, containerId);

                if("invalid" != target) {
                	jQuery("#" + containerId + " area").each(function(index, el) {
                		var areaEl = jQuery(el);
                		var areaId = areaEl.attr('id');
                		if(target == areaId) {
                			areaEl.mouseover();
                		} else {
                			areaEl.mouseout();
                		}
                	});
                	jQuery('#' + target).mouseover();
                } else {
                	jQuery("#" + containerId + " area").each(function(index, el) {
                		jQuery(el).mouseout();
                	});
                }
    		}
    	});
    	
    	jQuery("#" + containerId).droppable({
    		drop: function(event, ui) {
    			var containerEl = jQuery("#" + containerId + "_img");
    			var containerOffset = containerEl.offset();
    			
				var gapitem = jQuery(ui.draggable);
    			var offset = gapitem.offset();
                var x1 = offset.left + (gapitem.width() / 2) - containerOffset.left;
                var y1 = offset.top + (gapitem.height() / 2) - containerOffset.top;
    			var target = dropTarget(x1, y1, containerId);
    			if("invalid" != target) {
    				var areaEl = jQuery('#' + target);
    				var coords = toCoords(areaEl);
        			var areaId = areaEl.data('qti-id');
        			var gapitemId = gapitem.data('qti-id');
        			
        			gapitem.css('position','absolute');
        			gapitem.css('left', coords[0] + 'px');
        			gapitem.css('top', coords[1] + 'px');
        		
        			gapitem.css('border', 'none');
        			gapitem.removeClass('oo-selected');
        			gapitem.addClass('oo-choosed');
        			
        			//add
        			var inputElement = jQuery('<input type="hidden"/>')
        				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
        				.attr('value', gapitemId + " " + areaId);
        			jQuery(this).prepend(inputElement);
        			jQuery(areaEl).mouseout();
        			gapitem.data('drop-timestamp', Date.now());
    			}
    		}
    	});
    	
    	jQuery("#" + containerId + " area").on('click', function(e, el) {
    		var areaEl = jQuery(this);
    		jQuery(".gap_item.oo-selected").each(function(index, el){
    			var gapitem = jQuery(el);
    			var coords = toCoords(areaEl);
    			var areaId = areaEl.data('qti-id');
    			var gapitemId = gapitem.data('qti-id');
    			
    			gapitem.css('position','absolute');
    			gapitem.css('left', coords[0] + 'px');
    			gapitem.css('top', coords[1] + 'px');
    		
    			gapitem.css('border', 'none');
    			gapitem.removeClass('oo-selected');
    			gapitem.addClass('oo-choosed');
    			
    			//add
    			var divContainer = jQuery('#' + containerId);
    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    				.attr('value', gapitemId + " " + areaId);
    			divContainer.prepend(inputElement);
    		});
    	});
    };
    
    function dropTarget(dropX, dropY, containerId) {
    	var target = 'invalid';
    	jQuery("#" + containerId + " area").each(function(index, el) {
    		var areaEl = jQuery(el);
    		var areaId = areaEl.attr("id");
    		var coords = areaEl.attr('coords');
    		var shape = areaEl.attr('shape');
    		
    		var coordsArr = toCoords(areaEl);
    		if("circle" == shape) {
    			if(pncircle(dropX, dropY, coordsArr[0], coordsArr[1], coordsArr[2])) {
    				target = areaId;
    			}
    		} else if("rect" == shape) {
    			if(pnrect(dropX, dropY, coordsArr[0], coordsArr[1], coordsArr[2], coordsArr[3])) {
    				target = areaId;
    			}
    		} else if("poly" == shape) {
    			var area = {};
    		    area.x = [];
    		    area.y = [];
    		    var totalPairs = coordsArr.length / 2;
    		    var coordCounter = 0; //variable to double iterate
    		    for (ix = 0; ix < totalPairs; ix++) { //fill arrays of x/y coordinates for pnpoly
    		        area.x[ix] = coordsArr[coordCounter];
    		        area.y[ix] = coordsArr[coordCounter + 1];
    		        coordCounter += 2;
    		    }
    		    
    		    for (i = 0; i < area.length; i++) { //iterate through all of our area objects
    	    	    if (pnpoly(area.x.length, area.x, area.y, dropX, dropY)) {
    	    	        target = area.id;
    	    	        break;
    	    	     }
    	    	}
    		}
    	});
    	return target;
    }
    
    function pncircle(x, y, centerx, centery, radius) {
        var c = Math.pow(x - centerx, 2) + Math.pow(y - centery, 2);
        return Math.pow(radius, 2) >= c;
    };
    
    function pnrect(x, y, leftx, topy, rightx, bottomy) {
        if ((leftx <= x ) && ( x <= rightx) && (topy <= y) && (y <= bottomy)) {
            return true;
        } else {
            return false;
        }
    };
    
	//Point in Poly Test http://www.ecse.rpi.edu/~wrf/Research/Short_Notes/pnpoly.html
    function pnpoly(nvert, vertx, verty, testx, testy) {
    	var i, j, c = false;
    	for (i = 0, j = nvert - 1; i < nvert; j = i++) {
    	    if (((verty[i] > testy) != (verty[j] > testy)) &&
    	        (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i])) {
    	        c = !c;
    	    }
    	}
    	return c;
    }
    
    function toCoords(area) {
    	var coords = area.attr('coords').split(',');
    	for (i=coords.length; i-->0; ) {
    		coords[i] = parseFloat(coords[i]);
    	}
    	return coords;
    }
}( jQuery ));
