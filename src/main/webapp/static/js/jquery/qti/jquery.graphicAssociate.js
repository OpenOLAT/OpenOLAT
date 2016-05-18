(function ($) {
    $.fn.graphicAssociateInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		maxAssociations: 1,
    		opened: false
        }, options);
    	
    	try {
    		if(!(typeof settings.responseValue === "undefined") && settings.responseValue.length > 0) {
    			initAssociations(this, settings.responseValue, settings.responseIdentifier);
    		}
    		if(settings.opened) {
    			associateGraphics(this, settings.maxAssociations, settings.responseIdentifier);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function associateGraphics($obj, maxAssociations, responseIdentifier) {
    	var containerId = $obj.attr('id');
    	jQuery('#' + containerId +'_container area').on("click", function(e) {
    		var r = 8;
    		
    		var data = jQuery("#" + containerId + "_container").data("openolat") || {};
    		if(data.listOfPairs == undefined) {
    			data.currentSpot = '';
    			data.listOfPairs = [];
    			jQuery("#" + containerId + "_container").data('openolat', data);
    		}

    		var areaId = jQuery(this).data('qti-id');
    		
    		if(data.currentSpot == '' || data.currentSpot == areaId) {
    			data.currentSpot = areaId;
    		} else {
    			var newPair = [data.currentSpot, areaId];
    			data.listOfPairs.push(newPair);
    			data.currentSpot = '';
    		}

    		var canvas = document.getElementById(containerId + '_canvas');
    		var c = canvas.getContext('2d');
    		c.clearRect(0,0,jQuery(canvas).width(),jQuery(canvas).height());
    		
    		var divContainer = jQuery('#' + containerId + '_container');
    		divContainer.find("input[type='hidden']").remove();
    		
    		var drawedSpots = [];
    		if(data.currentSpot != '') {
    			drawArea(c, 'ac_' + responseIdentifier + '_' + data.currentSpot);
    			drawedSpots.push(data.currentSpot);
    		}

    		for(var i=data.listOfPairs.length; i-->0; ) {
    			var pair = data.listOfPairs[i];
    			for(var j=pair.length; j-->0; ) {
    				if(0 > drawedSpots.indexOf(pair[j])) {
    					drawArea(c, 'ac_' + responseIdentifier + '_' + pair[j]);
    					drawedSpots.push(pair[j]);
    				}
    			}
    			
    			var pair1El = jQuery('#ac_' + responseIdentifier + '_' + pair[1]);
    			var pair2El = jQuery('#ac_' + responseIdentifier + '_' + pair[0]);
    			
    			var coords1 = toCoords(pair1El);
    			var coords2 = toCoords(pair2El);
    			
    			c.beginPath();
    			c.moveTo(coords1[0], coords1[1]);
    			c.lineTo(coords2[0], coords2[1]);
    			c.lineWidth = 3;
    			c.stroke();
    			
    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + responseIdentifier)
    				.attr('value', pair2El.data('qti-id') + " " + pair1El.data('qti-id'));
    			divContainer.prepend(inputElement);
    		}
    	});
    };

    function initAssociations($obj, responseValue, responseIdentifier) {
    	var containerId = $obj.attr('id');
    	var divContainer = jQuery('#' + containerId + '_container');
    	
    	var canvas = document.getElementById(containerId + '_canvas');
    	var c = canvas.getContext('2d');
    	c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
    	
    	var drawedSpots = [];
    	var pairs = responseValue.split(',');
    	for(var i=pairs.length; i-->0; ) {
    		var pair = pairs[i].split(' ');
    		for(var j=pair.length; j-->0; ) {
    			if(0 > drawedSpots.indexOf(pair[j])) {
    				drawArea(c, 'ac_' + responseIdentifier + '_' + pair[j]);
    				drawedSpots.push(pair[j]);
    			}
    		}

    		if(pair.length == 2) {
    			var pair1El = jQuery('#ac_' + responseIdentifier + '_' + pair[1]);
    			var pair2El = jQuery('#ac_' + responseIdentifier + '_' + pair[0]);
    			
	    		var coords1 = toCoords(pair1El);
	    		var coords2 = toCoords(pair2El);
	    		
	    		c.beginPath();
	    		c.moveTo(coords1[0], coords1[1]);
	    		c.lineTo(coords2[0], coords2[1]);
	    		c.lineWidth = 3;
	    		c.stroke();
	    		
	    		var inputElement = jQuery('<input type="hidden"/>')
					.attr('name', 'qtiworks_response_' + responseIdentifier)
					.attr('value', pair2El.data('qti-id') + " " + pair1El.data('qti-id'));
	    		divContainer.prepend(inputElement);
    		}
    	}
    };

    function drawArea(c, areaId) {
    	jQuery('#' + areaId).each(function(index, el) {
    		var areaEl = jQuery(el);
    		var coords = toCoords(areaEl);
        	var shape = areaEl.attr('shape');
        	drawShape(c, shape, coords, 0, 0);
    	})
    };
    
    function drawShape(context, shape, coords, x_shift, y_shift) {
    	x_shift = x_shift || 0;
    	y_shift = y_shift || 0;

    	context.beginPath();
    	if(shape == 'rect') {
    		// x, y, width, height
    		context.rect(coords[0] + x_shift, coords[1] + y_shift, coords[2] - coords[0], coords[3] - coords[1]);
    	} else if(shape == 'poly') {
    		context.moveTo(coords[0] + x_shift, coords[1] + y_shift);
    		for(i=2; i < coords.length; i+=2) {
    			context.lineTo(coords[i] + x_shift, coords[i+1] + y_shift);
    		}
    	} else if(shape == 'circ' || shape == 'circle') {
    		// x, y, radius, startAngle, endAngle, anticlockwise
    		context.arc(coords[0] + x_shift, coords[1] + y_shift, coords[2] - 2, 0, Math.PI * 2, false);
    	}
    	context.closePath();
    		context.lineWidth = 4;
    	context.strokeStyle = '#003300';
    		context.stroke();
    	context.fillStyle = 'green';
    		context.fill();
    };
    
    function toCoords(area) {
    	var coords = area.attr('coords').split(',');
    	for (i=coords.length; i-->0; ) {
    		coords[i] = parseFloat(coords[i]);
    	}
    	return coords;
    }
}( jQuery ));
