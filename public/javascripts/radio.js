(function(mySoundManager,jQuery) {
	var $      = jQuery,
	    loveEl = $('#love'),
	    nextEl = $('#next'),
	    trackEl = $('#track'),
	    singerEl = $('#singer'),
	    pauseEl = $('#pause'),
	    apEl   = $('#audioPlayer'),
	    track = {},
	    soundManager = mySoundManager;
	
	soundManager.setup({
		  url: '/public/test/',
		  flashVersion: 9, // optional: shiny features (default = 8)
		  useFlashBlock: false, 
		  onready: function() {
			  next();
		  }
		});
	
	loveEl.click(function() {
		console.log("loveEl click");
		$.getJSON("/application/love", function(data) {
			console.log(data);
		});
	});
	nextEl.click(function() {
		console.log("nextEl click");
		next();
	});
	
	apEl.bind('ended',function(){
		console.log("apEl ended");
		next();
	});
	pauseEl.click(function() {
		console.log("pauseEl click");
		if(track.paused){
			track.play();
			pauseEl.removeClass("btn-success").addClass("btn-inverse").html("Pause");
			console.log("played");
		}else{
			track.pause();
			pauseEl.removeClass("btn-inverse").addClass("btn-success").pauseEl.html("Play");
			pauseEl.removeClass()
			console.log("paused");
		}
	});
	
	function next(){
		$.getJSON("/application/nextTrack",
	    	    function(data){
						if(track){
							soundManager.stop(track);
						}
	        			createSound(data.location);
	        			track = data.location;
	        			console.log(track);
	        			trackEl.html(data.title);
	        			singerEl.html(data.creator);
	        			$('#album').html(data.album);        			
	        	});
	}
	
	
	function createSound(url){
		soundManager.createSound({
			  id:url,
			  url: url,
			  autoLoad: true,
			  autoPlay: false,
			  onload: function() {

			  },
			  whileplaying: function() {
				  console.log('sound '+this.id+' playing, '+this.position+' of '+this.duration);
			  },
			  volume: 100
			}
		);
		soundManager.play(url,{
			onfinish: function(){
				next();	
			}
		});
	}
	
})(soundManager,jQuery);