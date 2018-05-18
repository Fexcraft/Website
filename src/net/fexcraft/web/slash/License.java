package net.fexcraft.web.slash;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;

public class License extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doPost(req, resp);
		return;
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(Fexcraft.redirect(req, resp)){ return; }
		//Document document = Resources.getDocument(user, "Licenses");
		Document document = FileCache.newDocument(null, "Licenses");
		document.getElementById("content").append(FileCache.getResource("licenses/" + req.getParameter("id"), "html"));
		resp.getWriter().append(document.toString());
	}

}
