package net.fexcraft.web.slash;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;
import org.jsoup.nodes.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
				case "versions":{
					reply = "[\"1.12.2\",\"1.12\",\"1.11.2\",\"1.11\",\"1.10.2\",\"1.9.4\",\"1.9\",\"1.8.9\",\"1.8\",\"1.7.10\"]";
					break;
				}
				case "downloads":{
					//reply = MySql.WEB.getArray("downloads", "where modid='" + id + "' and listed='1' "+ (mv == null || mv.equals("") ? "" : "and mc_version='" + mv + "'") + " order by mc_version;", "modid", "mc_version", "version", "mirror1_name", "mirror1_link", "mirror2_name", "mirror2_link", "mirror3_name", "mirror3_link").toString();
					if(mv == null || mv.equals("") || mv.equals("all")){
						reply = JsonUtil.fromList(RTDB.get().table("downloads").filter(obj -> obj.g("listed").eq(true)).filter(obj -> obj.g("modid").eq(id)).orderBy("version").orderBy("mc_version").run(RTDB.conn())).toString();
					}
					else{
						reply = JsonUtil.fromList(RTDB.get().table("downloads").filter(obj -> obj.g("listed").eq(true)).filter(obj -> obj.g("modid").eq(id)).filter(obj -> obj.g("mc_version").eq(mv)).orderBy("version").run(RTDB.conn())).toString();
					}
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
		//doc.getElementById("dllist").after(FileCache.getResource("ads/ad3-wide", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
