package net.fexcraft.web.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event){
		RTDB.get().table("sessions").insert(RTDB.get()
				.hashMap("id", event.getSession().getId())
				.with("guest", true)
				.with("activity", event.getSession().getCreationTime())).run(RTDB.conn());
    }
    
	@Override
    public void sessionDestroyed(HttpSessionEvent event){
		RTDB.get().table("sessions").get(event.getSession().getId()).delete().run(RTDB.conn());
    }

}
