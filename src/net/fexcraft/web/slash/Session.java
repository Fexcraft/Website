package net.fexcraft.web.slash;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.UserObject;

public class Session extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		return;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		UserObject user = UserObject.fromSession(request.getSession());
		String rq = request.getParameter("rq");
		if(rq != null && !rq.equals("")){
			String reply = "{}";
			switch (rq) {
				case "login":{
					if(user.isGuest()){
						reply = user.login(request.getParameter("mail"), request.getParameter("pswd")).toString();
					}
					else{
						reply = "{\"status\":\"Already logged in.\", \"success\":false}";
					}
					break;
				}
				case "logout":{
					if(!user.isGuest()){
						reply = user.logout().toString();
					}
					else{
						reply = "{\"status\":\"Already logged out.\"}";
					}
					break;
				}
				case "userdata":{
					if(!user.isGuest()){
						//reply = user.getUserData().toString();
					}
					break;
				}
			}
			response.setContentType("application/json");
			response.getWriter().append(reply);
			return;
		}
		Document doc = FileCache.newDocument(user, "Session");
		doc.getElementById("content").html(FileCache.getResource("session", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/session", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
