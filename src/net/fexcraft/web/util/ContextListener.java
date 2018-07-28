package net.fexcraft.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;

import net.fexcraft.web.Fexcraft;

public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce){
        SessionCookieConfig scf = sce.getServletContext().getSessionCookieConfig();
        scf.setComment("Commentless.");
        scf.setDomain(!Fexcraft.dev() ? ".localhost" : ".fexcraft.net");
        scf.setHttpOnly(false);
        scf.setMaxAge(30000);
        scf.setPath("/session");
        //scf.setSecure(true);
        scf.setName("JSESSIONID");
        return;
    }

    public void contextDestroyed(ServletContextEvent sce){

    }
    
}