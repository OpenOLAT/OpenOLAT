# jQuery periodic plugin

While this plugin is still considered to be "under development", it seems to be working and may suit your purposes fine.   But we reserve the right to change how things work and/or change names of things, etc.   Please give us feedback if you'd like to see some feature or tweak.

    $.periodic([options], callback);

The purpose of this plugin is to provide a simple wrapper around setTimeout to allow your application
to do any sort of polling task.   Most often, this would be used in conjunction with the jQuery ajax
(or related) function like this:

    $.periodic(options, function() {
      $.ajax(ajax_options);
    });

You may also set the default options once for your app and just call the plugin with the callback function like this:

    $.periodic.defaults.period = 8888;

    $.periodic(function() {
      $.ajax(ajax_options);
    });

The plugin provides built-in settings for decreasing the poll time via a "decay" setting (much like
prototype's PeriodicalExecuter).  However, the name of this plugin, 'periodic', is a more grammatically correct description of what it does (a "periodical" is something you read).

## Examples

Let's say you want to poll your server and update some part of your page by hitting the '/update.json' URL.
Also, let's set it up to poll the server every 2 seconds initially but to decay 20% until it reaches a maximum 
of 60 seconds if there are no changes to the content returned from the server.   This example would look like
this:

    $.periodic({period: 2000, decay: 1.2, max_period: 60000}, function() {
     $.ajax({
        url: '/update.json',
        success: function(data) { do_something_with_data(data) },
        complete: this.ajax_complete,
        dataType: 'json'
      });
    });

Notice that, in the above example, jQuery.periodic provides a utility callback function, ajax_complete that checks the return value of the ajax request to see if it has changed.   If it hasn't changed since the last call then
the time between calls will decay as per your settings (1.2 in this example).

If you prefer to set your options separately then the following example is equivalent to the above example:

    $.extend($.periodic.defaults, {period: 2000, decay: 1.2, max_period: 60000});
    $.periodic(function() {
     $.ajax({
        url: '/update.json',
        success: function(data) { do_something_with_data(data) },
        complete: this.ajax_complete,
        dataType: 'json'
      });
    });

If you need to define your own 'complete' function and would still like to use the plugin-provided function then you could do so by setting a property in the ajax object to the periodic object so that you can access it from the ajax callbacks.

    $.periodic(function() {
     $.ajax({
        periodic: this,
        url: '/update.json',
        success: function(data) { do_something_with_data(data) },
        complete: function(xhr, status) {
          // do your own thing
          do_something_on_complete();
          // use the utility callback via the periodic property in the ajax object
          this.periodic.ajax_complete(xhr, status);
        },
        dataType: 'json'
      });
    });

If you'd prefer to have more control over when the period should increment or should be reset to the initial value, then you can use the provided 'increment' and 'reset' functions instead.   For example, if you wanted to base it off of some condition from the returned ajax data:

    $.periodic(function() {
      $.ajax({
        periodic: this,
        url: '/update.json',
        success: function(data) {
          do_something_with_data(data);
          if (need_to_reset_timer()) {
            this.periodic.reset();
          } else {
            this.periodic.increment();
          }
        },
        dataType: 'json'
      });
    });
 
You can even set the current value of the period directly by accessing the 'cur_period' value.

    $.periodic(function() {
      $.ajax({
        periodic: this,
        url: '/update.json',
        success: function(data) {
          do_something_with_data(data);
          if (need_to_reset_timer()) {
            this.periodic.cur_period = this.periodic.period;
         } else {
            this.periodic.cur_period = 42;
         }
        },
        dataType: 'json'
      });
    });

These examples are just a starting point.   If you'd like to see other specific example, send us a message or open a ticket.

## Options

The plugin options can be set in one of two ways.   You can specify them as the first argument to the function,

    $.periodic(my_options, callback);

or you can set the default options for all subsequent calls:

   $.extend($.periodic.defaults, my_options);

Alternatively, you can just go with the provided defaults as described below.

The options object recognizes the following settings:

<table>
  <tr><th>Option</th><th>Description</th><th>Default Value</th></tr>
  <tr><td>period</td><td>Initial time in msec to wait between calls to the callback.</td><td>4000</td></tr>
  <tr><td>decay</td><td>Multiplier to increase the time if the request doesn't change.  A value of 1.0 means that the period never changes.</td><td>1.5</td></tr>
  <tr><td>max_period</td><td>The maximum value for the delay.</td><td>180000 (30 min)</td></tr>
  <tr><td>on_max</td><td>User supplied function to call when the max_period is reached.</td><td>undefined</td></tr>
</table>

## Utility Functions

This plugin also provides some control of it's functionality via the 'this' variable inside of your callback function.   You can access them via "this.fn" where "fn" is one of the functions described below.
<table>
  <tr><th>Utility Function</th><th>Description</th></tr>
  <tr><td>ajax_complete(xhr, status)</td><td>A function to be called upon completion of a jQuery.ajax call.   This function will compare the response of current request to that of the previous request and increment or reset the time period accordingly.</td></tr>
  <tr><td>reset()</td><td>Resets the time period to the configured minimum (options.period)</td></tr>
  <tr><td>increment()</td><td>Increases the time period as per the settings.</td></tr>
  <tr><td>cancel()</td><td>Cancels the periodic call.</td></tr>
</table>

## Installation

Simply download the file jquery.periodic.js to wherever your javascript files live.
