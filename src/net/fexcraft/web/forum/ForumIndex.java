package net.fexcraft.web.forum;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.slash.Index;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;
import net.fexcraft.web.util.RTDB.Table;
import net.fexcraft.web.util.UserObject;
import org.jsoup.nodes.Document;

import com.google.gson.JsonArray;
import com.rethinkdb.model.MapObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ForumIndex extends HttpServlet {
	
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
			switch(rq){
				case "news":{
					reply = "{\"news\":" + (JsonUtil.fromList(RTDB.get().table("news").orderBy(RTDB.get().desc("time")).limit(12).run(RTDB.conn())).toString()) + "}";
					break;
				}
				case "background":{
					if(Index.images == null){ Index.images = Fexcraft.INSTANCE.getProperty("background_images", Index.images).getAsJsonArray(); }
					if(Index.images.size() == 0){
						reply = "https://cdn.discordapp.com/attachments/124456784193781760/257214392393793546/unknown.png";
					}
					else{
						reply = Index.images.get(Fexcraft.RANDOM.nextInt(Index.images.size())).getAsString();
					}
					break;
				}
				case "forums":{
					JsonArray array = new JsonArray();
					ArrayList<HashMap<String, Object>> cursor = RTDB.run(RTDB.get(Table.F_FORUMS).filter(
						new MapObject().with("parent", request.getParameter("parent") == null ? 0 : Integer.parseInt(request.getParameter("parent")))).orderBy("weight"));
					cursor.forEach(elm -> {
						array.add(JsonUtil.fromMapObject(elm));
					});
					reply = array.toString();
					break;
				}
				case "latest_post":{
					reply = "{\"author\":\"TestUser\", \"user\":0, \"title\":\"Some Random Topic Title\", \"topic\":1, \"date\":0}";
					break;
				}
			}
			response.setContentType("application/json");
			response.getWriter().append(reply);
			return;
		}
		Document doc = FileCache.newForumDocument(user, "Index");
		doc.getElementById("content").html(FileCache.getResource("forums/index", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
