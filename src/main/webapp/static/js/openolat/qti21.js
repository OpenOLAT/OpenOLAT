
function associateDrawResponse(containerId, responseValue) {
	var associationPairs = responseValue.split(',');
	var associationEls = jQuery('#' + containerId + '_panel .association');
	for(var i=associationPairs.length; i-->0; ) {
		var associationPair = associationPairs[i].split(' ');
		var associationEl = jQuery(associationEls.get(i));
		var association1 = jQuery('#' + associationPair[0]);
		var association2 = jQuery('#' + associationPair[1]);
		jQuery(association1).css('border','none');
		jQuery(association2).css('border','none');
		
		jQuery(associationEl.find('.association_box.left'))
			.css('border','3px solid grey')
			.append(association1);
		jQuery(associationEl.find('.association_box.right'))
			.css('border','3px solid grey')
			.append(association2);
	}
}

function associateItem(containerId, responseIdentifier) {
	jQuery("#" + containerId + "_items .o_associate_item").on('click', function(e, el) {
		var itemEl = jQuery(this);
		if(itemEl.hasClass('oo-choosed')) {
			itemEl.removeClass('oo-choosed');
			itemEl.parent('.association_box').each(function(index, el) {
				jQuery(el).css('border', '3px dotted grey');
			});
			itemEl.css('border', '2px solid grey');
			jQuery('#' + containerId +'_items').prepend(itemEl);
		} else {
			itemEl.css('border', '2px solid red');
			itemEl.addClass('oo-selected');
		}
	});
	
	jQuery("#" + containerId + "_panel .association_box").on('click', function(e, el) {
		var box = jQuery(this);
		jQuery("#" + containerId + "_items .o_associate_item.oo-selected").each(function(index, selectedEl) {
			jQuery(selectedEl)
				.css('border', 'none')
				.removeClass('oo-selected')
				.addClass('oo-choosed');
			box.append(selectedEl);
			box.css('border', '3px solid grey');
			recalculateAssociations(containerId, responseIdentifier);
		});
	});
}

function recalculateAssociations(containerId, responseIdentifier) {
	var divContainer = jQuery('#' + containerId + '_panel');
	divContainer.find("input[type='hidden']").remove();
	
	jQuery("#" + containerId + "_panel .association").each(function(index, associationEl) {
		var associations = jQuery(associationEl).find('.o_associate_item');
		if(associations.length == 2) {
			var id1 = jQuery(associations.get(0)).attr('id');
			var id2 = jQuery(associations.get(1)).attr('id');			
			var inputElement = jQuery('<input type="hidden"/>')
					.attr('name', 'qtiworks_response_' + responseIdentifier)
					.attr('value', id1 + " " + id2);
			divContainer.prepend(inputElement);
		}
	});
};

function positionObjectDrawResponse(containerId, responseIdentifier, responseValue) {
	var positions = responseValue.split(':');
	var items = jQuery('#' + containerId + ' .items_container .o_item.o_' + responseIdentifier);
	for(var i=positions.length; i-->0; ) {
		var pos = positions[i].split(' ');
		var item = jQuery(items.get(i));
		item.css('position', 'absolute');
		item.css('top', pos[1] + 'px');
		item.css('left', pos[0] + 'px');
	}
}

function positionObjectItem(containerId, responseIdentifier) {
	jQuery('#' + containerId + ' .items_container .o_item.o_' + responseIdentifier).each(function(index, el) {
		jQuery(el).attr('id','object-item-' + index);
	}).draggable({
		containment: "#" + containerId,
		scroll: false,
		stop: function( event, ui ) {
			var imgEl = jQuery('#' + containerId + '_img');
			var img_offset_t = jQuery(imgEl).offset().top - jQuery(window).scrollTop();
			var img_offset_l = jQuery(imgEl).offset().left - jQuery(window).scrollLeft();
			
			var offset_t = jQuery(this).offset().top - jQuery(window).scrollTop();
			var offset_l = jQuery(this).offset().left - jQuery(window).scrollLeft();

			var cx = Math.round( (offset_l - img_offset_l) );
			var cy = Math.round( (offset_t - img_offset_t) );
			
			var itemId = jQuery(this).attr('id');
			var inputId = 'in-' + itemId + '-' + responseIdentifier;
			var divContainer = jQuery('#' + containerId);
			var inputEl = divContainer.find(inputId);
			if(inputEl.length == 0) {
				var inputElement = jQuery('<input type="hidden"/>')
					.attr('id', inputId)
					.attr('name', 'qtiworks_response_' + responseIdentifier)
					.attr('value', cx + " " + cy);
				divContainer.prepend(inputElement);
			} else {
				inputEl.val(cx + " " + cy);
			}
		}
	});
}


function graphicGapMatchDrawResponse(responseValue) {
	var pairs = responseValue.split(',');
	for(var i=pairs.length; i-->0; ) {
		var ids = pairs[i].split(' ');
		
		var item1 = jQuery('#' + ids[0]);
		var item2 = jQuery('#' + ids[1]);
		
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
	}
}

function graphicGapMatchitem(containerId, responseIdentifier) {
	jQuery(".gap_item").on('click', function(e, el) {
		var gapitem = jQuery(this);
		
		if(gapitem.hasClass('oo-choosed')) {
			gapitem.removeClass('oo-choosed');
			gapitem.css('position','relative');
			gapitem.css('left','auto');
			gapitem.css('top','auto');
			
			var gapitemId = gapitem.attr('id');
			//remove
			jQuery('#' + containerId).find("input[type='hidden']").each(function(index, el) {
				var value = jQuery(el).val();
				if(value.indexOf(gapitemId + ' ') == 0) {
					jQuery(el).remove();
				}
			});
		} else {
			gapitem.css('border','3px solid black');
			gapitem.addClass('oo-selected');
		}
	});
	
	jQuery("#" + containerId + " area").on('click', function(e, el) {
		var areaEl = jQuery(this);
		jQuery(".gap_item.oo-selected").each(function(index, el){
			var gapitem = jQuery(el);
			var coords = toCoords(areaEl);
			var areaId = areaEl.attr('id');
			var gapitemId = gapitem.attr('id');
			
			gapitem.css('position','absolute');
			gapitem.css('left', coords[0] + 'px');
			gapitem.css('top', coords[1] + 'px');
		
			gapitem.css('border', 'none');
			gapitem.removeClass('oo-selected');
			gapitem.addClass('oo-choosed');
			
			//add
			var divContainer = jQuery('#' + containerId);
			var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + responseIdentifier)
				.attr('value', gapitemId + " " + areaId);
			divContainer.prepend(inputElement);
		});
	});
}

function graphicAssociationDrawResponse(containerId, responseValue) {
	var canvas = document.getElementById(containerId + '_canvas');
	var c = canvas.getContext('2d');
	c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
	
	var drawedSpots = [];
	var pairs = responseValue.split(',');
	for(var i=pairs.length; i-->0; ) {
		var pair = pairs[i].split(' ');
		for(var j=pair.length; j-->0; ) {
			if(0 > drawedSpots.indexOf(pair[j])) {
				drawArea(c, pair[j]);
				drawedSpots.push(pair[j]);
			}
		}

		var coords1 = toCoords(jQuery('#' + pair[1]));
		var coords2 = toCoords(jQuery('#' + pair[0]));
		
		c.beginPath();
		c.moveTo(coords1[0], coords1[1]);
		c.lineTo(coords2[0], coords2[1]);
		c.lineWidth = 3;
		c.stroke();
	}
}

function graphicAssociationItem(containerId, maxAssociations, responseIdentifier) {
	jQuery('#' + containerId +'_container area').on("click", function(e) {
		var r = 8;

		var data = jQuery("#" + containerId + "_container").data("openolat") || {};
		if(data.listOfPairs == undefined) {
			data.currentSpot = '';
			data.listOfPairs = [];
			jQuery("#" + containerId + "_container").data('openolat', data);
		}

		var areaId = jQuery(this).attr('id');
		
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
			drawArea(c, data.currentSpot);
			drawedSpots.push(data.currentSpot);
		}

		for(var i=data.listOfPairs.length; i-->0; ) {
			var pair = data.listOfPairs[i];
			for(var j=pair.length; j-->0; ) {
				if(0 > drawedSpots.indexOf(pair[j])) {
					drawArea(c, pair[j]);
					drawedSpots.push(pair[j]);
				}
			}
			
			var coords1 = toCoords(jQuery('#' + pair[1]));
			var coords2 = toCoords(jQuery('#' + pair[0]));
			
			c.beginPath();
			c.moveTo(coords1[0], coords1[1]);
			c.lineTo(coords2[0], coords2[1]);
			c.lineWidth = 3;
			c.stroke();
			
			var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + responseIdentifier)
				.attr('value', pair[0] + " " + pair[1]);
			divContainer.prepend(inputElement);
		}
	});
	
}

function toCoords(area) {
	var coords = area.attr('coords').split(',');
	for (i=coords.length; i-->0; ) {
		coords[i] = parseFloat(coords[i]);
	}
	return coords;
};

function draw_shape(context, shape, coords, x_shift, y_shift) {
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

function drawArea(c, areaId) {
	var areaEl = jQuery('#' + areaId);
	var shape = areaEl.attr('shape');
	var coords = toCoords(areaEl);
	draw_shape(c, shape, coords, 0, 0);
};
		
function selectPointDrawResponse(containerId, responseValue) {
	var r = 8;
	var points = responseValue.split(':');
	var canvas = document.getElementById(containerId + '_canvas');
	var c = canvas.getContext('2d');
	c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
	for(i=points.length; i-->0; ) {
		var p = points[i].split(' ');
		c.beginPath();
		c.arc(p[0], p[1], r, 0, Math.PI * 2, false);
		c.stroke();
		c.closePath();
	}
}

function selectPointItem(containerId, maxChoices, responseIdentifier) {
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
		} else if(data.listOfPoints.length >= maxChoices) {
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
				.attr('name', 'qtiworks_response_' + responseIdentifier)
				.attr('value', p.x + " " + p.y);
			divContainer.append(inputElement);
		}
	});
}

function graphicOrderDrawResponse(containerId, responseValue) {
	var canvas = document.getElementById(containerId + '_canvas');
	var c = canvas.getContext('2d');
	c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
	
	var areaIds = responseValue.split(',');
	for(var i=areaIds.length; i-->0; ) {
		var areaEl = jQuery('#' + areaIds[i]);
		var position = areaEl.attr('coords').split(',');
		var cx = position[0];
		var cy = position[1];
		
		c.font = "16px Arial";
		c.fillText("" + (i+1), cx, cy);
	}
}

function graphicOrderItem(containerId, maxChoices, responseIdentifier) {
	jQuery('#' + containerId + '_container area').on("click", function(e) {
		var r = 8;

		var areaId = jQuery(this).attr('id');
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
		} else if(data.listOfPoints.length >= maxChoices) {
			return false;
		} else {
			data.listOfPoints.push({'x': cx, 'y': cy, 'areaId': areaId});
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
				.attr('name', 'qtiworks_response_' + responseIdentifier)
				.attr('value', p.areaId);
			divContainer.prepend(inputElement);
		}
	});
	
}

function highlighHotspotAreas(responseValue) {
	var areaIds = responseValue.split(',');
	for(i=areaIds.length; i-->0; ) {
		var areaEl = jQuery('#' + areaIds[i])
		var data = areaEl.data('maphilight') || {};
		data.alwaysOn = true;
		areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');
	}
}

function clickHotspotArea(spot, containerId, responseIdentifier) {
	var areaEl = jQuery('#' + spot)
	var data = areaEl.data('maphilight') || {};
	if(!data.alwaysOn) {
		var numOfChoices = 1;
		if(numOfChoices > 0) {
			var countChoices = 0;
			jQuery("area", "map[name='" + containerId + "_map']").each(function(index, el) {
				var cData = jQuery(el).data('maphilight') || {};
				if(cData.alwaysOn) {
					countChoices++;
				}
			});
			if(countChoices >= numOfChoices) {
				return false;
			}
		}
	}
	data.alwaysOn = !data.alwaysOn;
	areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');

	var divContainer = jQuery('#' + containerId);
	divContainer.find("input[type='hidden']").remove();
	jQuery("area", "map[name='" +containerId + "_map']").each(function(index, el) {
		var cAreaEl = jQuery(el);
		var cData = cAreaEl.data('maphilight') || {};
		if(cData.alwaysOn) {
			var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + responseIdentifier)
				.attr('value', areaEl.attr('id'));
			divContainer.append(inputElement);
		}
	});
};