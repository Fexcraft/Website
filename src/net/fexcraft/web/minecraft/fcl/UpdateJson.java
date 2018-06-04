package net.fexcraft.web.minecraft.fcl;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import com.google.gson.JsonObject;
import com.rethinkdb.model.MapObject;
import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;
import net.fexcraft.web.util.UserObject;

public class UpdateJson extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		return;
	}

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		UserObject user = UserObject.fromSession(request.getSession());
		if(user.isGuest()){
			response.sendError(403, "You must be logged in to access this page.");
			return;
		}
		String rq = request.getParameter("rq");
		if(rq != null && !rq.equals("")){
			String reply = "{}";
			switch (rq) {
				case "update":{
					long id = (Long)((HashMap<String, Object>)RTDB.get().table("mc_fcl_json").get(request.getParameter("id")).getField("data").run(RTDB.conn())).get("user");
					if(id != user.getId()){
						response.sendError(403, "Your User ID doesn't match the ID under which the specified Mod/Addon was registered.");
						return;
					}
					JsonObject obj = JsonUtil.getObjectFromString(request.getParameter("json"));
					/*if(!obj.has("user")){
						response.sendError(500, "You cannot leave the \"user\" field empty!");
						return;
					}*/
					MapObject map = JsonUtil.toMapObject(obj);
					reply = RTDB.get().table("mc_fcl_json").get(request.getParameter("id")).update(map).run(RTDB.conn()).toString();
					break;
				}
				case "get":{
					reply = JsonUtil.fromMapObject(RTDB.get().table("mc_fcl_json").get(request.getParameter("id")).run(RTDB.conn())).toString();
					break;
				}
			}
			response.setContentType("application/json");
			response.getWriter().append(reply);
			return;
		}
		Document doc = FileCache.newDocument(user, "Update Json");
		doc.getElementById("content").html(FileCache.getResource("mc/fcl/updatejson", "html"));
		doc.getElementById("sidebar").html(FileCache.getResource("sidebars/default", "html"));
		response.getWriter().append(doc.toString());
		return;
	}

}
