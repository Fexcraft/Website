package net.fexcraft.web.files;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;

public class WebData extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
    	String file = request.getParameter("file");
    	if(file != null && !file.equals("")){
    		String[] split = file.split("\\.");
    		response.setContentType(split[1].equals("json") ? "application/json" : split[1].equals("js") ? "application/javascript" : "text/" + split[1]);
    		response.getWriter().append(FileCache.getResource(split[0], split[1]));
    		return;
    	}
		response.sendError(404, "Invalid Request.");
		return;
	}

}
