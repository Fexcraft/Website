package net.fexcraft.web.slash;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import com.google.gson.JsonObject;
import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.UserObject;

public class Register extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		return;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		UserObject user = UserObject.fromSession(request.getSession());
		if(!user.isGuest() || user.isAdmin()){
			response.sendError(403, "You are already logged in.");
			return;
		}
		String rq = request.getParameter("rq");
		if(rq != null && !rq.equals("")){
			response.setContentType("application/json");
			JsonObject reply = new JsonObject();
			//
			JsonUtil.reply(response, reply);
			return;
		}
		Document doc = FileCache.newDocument(user, "Register " + user.getSessionId());
		doc.getElementById("content").html(FileCache.getResource("register", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/session", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
