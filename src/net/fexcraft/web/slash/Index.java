package net.fexcraft.web.slash;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import com.google.gson.JsonArray;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.MySql;

public class Index extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    private static JsonArray images;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		String rq = request.getParameter("rq");
		if(rq != null && !rq.equals("")){
			String reply = "{}";
			switch(rq){
				case "news":{
					try{
						ResultSet set = MySql.WEB.queryJsonArray("news", "order by time desc limit 15", "time", "author", "title", "content");
						if(set.first()){
							reply = "{\"news\":" + set.getString("json") + "}";
						}
					}
					catch(SQLException e){
						reply = "{'news':[{'time':0,'author':-1,'title':'Error: " + e.getMessage() + "','content':'SQL Error which fetching News.'}]}";
					}
					break;
				}
				case "background":{
					if(images == null){
						images = Fexcraft.INSTANCE.getProperty("background_images", new JsonArray()).getAsJsonArray();
					}
					if(images.size() == 0){
						return;
					}
					reply = images.get(Fexcraft.RANDOM.nextInt(images.size())).getAsString();
					break;
				}
			}
			response.setContentType("application/json");
			response.getWriter().append(reply);
			return;
		}
		Document doc = FileCache.newDocument(null, "Home/Index");
		doc.getElementById("content").html(FileCache.getResource("index", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/default", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
