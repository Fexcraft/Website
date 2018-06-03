package net.fexcraft.web.slash;

import com.google.gson.JsonArray;
import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;
import net.fexcraft.web.util.UserObject;
import org.jsoup.nodes.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Index extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    private static JsonArray images = new JsonArray();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		UserObject user = UserObject.fromSession(request.getSession());
		String rq = request.getParameter("rq");
		if(rq != null && !rq.equals("")){
			String reply = "{}";
			switch(rq){
				case "news":{
					reply = "{\"news\":" + (JsonUtil.fromList(RTDB.get().table("news").orderBy(RTDB.get().desc("time")).limit(12).run(RTDB.conn())).toString()) + "}";
					break;
				}
				case "background":{
					if(images == null){ images = Fexcraft.INSTANCE.getProperty("background_images", images).getAsJsonArray(); }
					if(images.size() == 0){
						reply = "https://cdn.discordapp.com/attachments/124456784193781760/257214392393793546/unknown.png";
					}
					else{
						reply = images.get(Fexcraft.RANDOM.nextInt(images.size())).getAsString();
					}
					break;
				}
			}
			response.setContentType("application/json");
			response.getWriter().append(reply);
			return;
		}
		Document doc = FileCache.newDocument(user, "Home/Index");
		doc.getElementById("content").html(FileCache.getResource("index", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/default", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
