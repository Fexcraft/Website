var adress = self.location.port != 80 || self.location.port != 433 ? "http://localhost:80" : self.location.hostname;
var months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ];

function getCurrentTime(){
	return new Date().getTime();
}

function post(url, func){
	post(url, func, true);
}

function post(url, func, bool){
	var xrq = new XMLHttpRequest();
	xrq.onreadystatechange = function(){
		if(this.readyState == 4 && this.status == 200){
			return func(this.responseText);
		}
		else{
			//
		}
	};
	xrq.open("POST", url, bool);
	xrq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xrq.send();
}

function get(url, func){
	var xrq = new XMLHttpRequest();
	xrq.onreadystatechange = function(){
		if(this.readyState == 4 && this.status == 200){
			return func(this.responseText);
		}
		else{
			//
		}
	};
	xrq.open("GET", url, true);
	xrq.send();
}

function isJson(str){
	try{JSON.parse(str);}
	catch(e){return false;}
	return true;
}

function getParam(name){
	 return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
}

function setLoading(elm){
	elm.innerHTML = '<div style="text-align:center; margin:auto; padding:5px;"><img src="/web/img/loading_500.gif" width=100 height=100></div>';
}

function getTime(time){
	var date = new Date(time);
    var sec = date.getUTCSeconds();
    var min = date.getUTCMinutes();
    var h = date.getUTCHours() + 1;
    sec = sec < 10 ? "0" + sec : sec;
    min = min < 10 ? "0" + min : min;
    h = h < 10 ? "0" + h : h;
    return h + ":" + min + ":" + sec + "  |  " + date.getUTCDate() + "-" + months[date.getUTCMonth()] + "-" + date.getUTCFullYear() + " (CET)";
}