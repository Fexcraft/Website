package net.fexcraft.web.slash;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;

public class TermsOfService extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		Document doc = FileCache.newDocument(null, "Terms of Service");
		doc.getElementById("content").html(FileCache.getResource("tos", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/default", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
