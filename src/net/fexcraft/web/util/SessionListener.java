package net.fexcraft.web.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event){
		//
    }
    
	@Override
    public void sessionDestroyed(HttpSessionEvent event){
		UserObject.removeUser(event.getSession());
    }

}
