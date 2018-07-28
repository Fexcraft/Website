package net.fexcraft.web.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.ProxyServlet;

@SuppressWarnings("serial")
public class AuthProxy extends ProxyServlet {

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException{
		UserObject user = UserObject.fromSession(request.getSession());
		if(user == null || user.isGuest() || !user.isAdmin()){
			response.sendError(403, "No Permission to access the database.");
			return;
		}
		super.service(request, response);
    }
    
}
