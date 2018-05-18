package net.fexcraft.web.slash;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.MySql;

public class Download extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		String rq = request.getParameter("rq");
		String id = request.getParameterMap().containsKey("id") ? request.getParameter("id") : request.getParameter("modid");
		String mv = request.getParameter("mv");
		if(rq != null && !rq.equals("")){
			String reply = "{}";
			switch(rq){
				case "ids":{
					reply = MySql.WEB.getDistinctArray("downloads", "modid", "[\"SQL ERROR\"]").toString();
					break;
				}
				case "versions":{
					reply = MySql.WEB.getDistinctArray("downloads", "mc_version", "[\"SQL ERROR\"]").toString();
					break;
				}
				case "downloads":{
					reply = MySql.WEB.getArray("downloads", "where modid='" + id + "' and listed='1' "+ (mv == null || mv.equals("") ? "" : "and mc_version='" + mv + "'") + " order by mc_version;", "modid", "mc_version", "version", "mirror1_name", "mirror1_link", "mirror2_name", "mirror2_link", "mirror3_name", "mirror3_link").toString();
					break;
				}
			}
			response.setContentType("application/json");
			response.getWriter().append(reply);
			return;
		}
		Document doc = FileCache.newDocument(null, "Downloads");
		doc.getElementById("content").html(FileCache.getResource("download", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/download_menu", "html"));
		if(id != null && !id.equals("")){
			doc.getElementById("dlsdsl").attr("data-selected", id);
		}
		doc.getElementById("dllist").before(FileCache.getResource("ads/ad3-wide", "html"));
		doc.getElementById("dllist").after(FileCache.getResource("ads/ad3-wide", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
