(function ($) {
    $.fn.graphicOrderInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		maxChoices: 1
        }, options );
    	
    	try {
    		if(settings.responseValue == "") {
    			drawGraphicOrders(this, settings);
    			order(this, settings);
    		} else {
    			drawGraphicOrders(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawGraphicOrders($obj, settings) {
    	var containerId = $obj.attr('id');
    	var canvas = document.getElementById(containerId + '_canvas');
    	var c = canvas.getContext('2d');
    	c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
    	
    	var areaIds = settings.responseValue.split(',');
    	for(var i=areaIds.length; i-->0; ) {
    		if(areaIds[i].length == 0) continue;
    		
    		var areaEl = jQuery('#ac_' + settings.responseIdentifier + '_' + areaIds[i]);
    		var position = areaEl.attr('coords').split(',');
    		var cx = position[0];
    		var cy = position[1];
    		
    		c.font = "16px Arial";
    		c.fillText("" + (i+1), cx, cy);
    	}
    };
    
    function order($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery('#' + containerId + '_container area').on("click", function(e) {
    		var r = 8;

    		var areaId = jQuery(this).attr('id');
    		var spotQtiId = jQuery(this).data('qti-id');
    		var position = jQuery(this).attr('coords').split(',');
    		var cx = position[0];
    		var cy = position[1];

    		var data = jQuery("#" + containerId + "_container").data("openolat") || {};
    		if(data.listOfPoints == undefined) {
    			data.listOfPoints = [];
    			jQuery("#" + containerId + "_container").data('openolat', data);
    		}
    			
    		var remove = false;
    		var newListOfPoints = [];
    		for(var i=data.listOfPoints.length; i-->0;) {
    			var p = data.listOfPoints[i];
    			var rc = ((p.x - cx)*(p.x - cx)) + ((p.y - cy)*(p.y - cy));
    			if(r*r > rc) {
    				remove = true;
    			} else {
    				newListOfPoints.push(p);
    			}
    		}
    			
    		if(remove) {
    			data.listOfPoints = newListOfPoints;
    		} else if(data.listOfPoints.length >= settings.maxChoices) {
    			return false;
    		} else {
    			data.listOfPoints.push({'x': cx, 'y': cy, 'areaId': areaId, 'spotQtiId' : spotQtiId});
    		}

    		var canvas = document.getElementById(containerId + '_canvas');
    		var c = canvas.getContext('2d');
    		c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
    			
    		var divContainer = jQuery('#' + containerId + '_container');
    		divContainer.find("input[type='hidden']").remove();
    			
    		for(var i=data.listOfPoints.length; i-->0;) {
    			var p = data.listOfPoints[i];
    			c.font = "16px Arial";
    			c.fillText("" + (i+1), p.x, p.y);

    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    				.attr('value', p.spotQtiId);
    			divContainer.prepend(inputElement);
    		}
    	});
    }
}( jQuery ));
