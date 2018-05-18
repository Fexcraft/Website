package net.fexcraft.web.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.fexcraft.web.util.user.UserCache;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event){
    	UserCache.addGuest(event.getSession());
    }
    
	@Override
    public void sessionDestroyed(HttpSessionEvent event){
		UserCache.removeUser(event.getSession());
    }

}
