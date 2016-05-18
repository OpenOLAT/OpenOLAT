(function ($) {
    $.fn.selectPointInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		maxChoices: 1,
    		opened: false
        }, options );
    	
    	try {
    		if(!(typeof settings.responseValue === "undefined") && settings.responseValue.length > 0) {
    			drawPoints(this, settings);
    		}
    		if(settings.opened) {
    			selecPointItems(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawPoints( $obj, settings) {
    	var r = 8;
    	var containerId = $obj.attr('id');
		var divContainer = jQuery('#' + containerId);
    	var points = settings.responseValue.split(':');
    	var canvas = document.getElementById(containerId + '_canvas');
    	var c = canvas.getContext('2d');
    	c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
    	for(i=points.length; i-->0; ) {
    		if(points[i].length == 0) continue;
    		
    		var p = points[i].split(' ');
    		var x = p[0];
    		var y = p[1];
    		
    		c.beginPath();
    		c.arc(x, y, r, 0, Math.PI * 2, false);
    		c.stroke();
    		c.closePath();
    		
    		var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
				.attr('value', x + " " + y);
    		divContainer.append(inputElement);
    	}
    };

    function selecPointItems($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery('#' + containerId + '_canvas').on("click", function(e, t) {
    		var r = 8;
    	
    		var offset_t = jQuery(this).offset().top - jQuery(window).scrollTop();
    		var offset_l = jQuery(this).offset().left - jQuery(window).scrollLeft();

    		var cx = Math.round( (e.clientX - offset_l) );
    		var cy = Math.round( (e.clientY - offset_t) );
    		
    		var data = jQuery("#" + containerId).data("openolat") || {};
    		if(data.listOfPoints == undefined) {
    			data.listOfPoints = [];
    			jQuery("#" + containerId).data('openolat', data);
    		}
    		
    		var remove = false;
    		var newListOfPoints = [];
    		for(i=data.listOfPoints.length; i-->0;) {
    			var p = data.listOfPoints[i];
    			var rc = ((p.x - cx)*(p.x - cx)) + ((p.y - cy)*(p.y - cy));
    			if(Math.pow(r,2) > rc) {
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
    			data.listOfPoints.push({'x': cx, 'y': cy});
    		}

    		var canvas = document.getElementById(containerId + '_canvas');
    		var c = canvas.getContext('2d');
    		c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
    		
    		var divContainer = jQuery('#' + containerId);
    		divContainer.find("input[type='hidden']").remove();
    		
    		for(i=data.listOfPoints.length; i-->0;) {
    			var p = data.listOfPoints[i];
    			c.beginPath();
    			c.arc(p.x, p.y, r, 0, Math.PI * 2, false);
    			c.stroke();
    			c.closePath();
    			
    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    				.attr('value', p.x + " " + p.y);
    			divContainer.append(inputElement);
    		}
    		
            setFlexiFormDirty(settings.formDispatchFieldId);
    	});
    }  
}( jQuery ));
