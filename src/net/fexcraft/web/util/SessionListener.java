package net.fexcraft.web.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event){
		//event.getSession().setMaxInactiveInterval(30 * 60);
    	//UserCache.add(event.getSession(), new User(event.getSession(), "guest@fexcraft.net", false));
    }
    
	@Override
    public void sessionDestroyed(HttpSessionEvent event){
    	//UserCache.rem(event.getSession());
    }

}
