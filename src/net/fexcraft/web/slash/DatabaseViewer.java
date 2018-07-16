package net.fexcraft.web.slash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;

import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Cursor;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;
import net.fexcraft.web.util.UserObject;

public class DatabaseViewer extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		return;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(Fexcraft.redirect(request, response)){ return; }
		UserObject user = UserObject.fromSession(request.getSession());
		if(user.isGuest() || !user.isAdmin()){
			response.sendError(403, "You must be logged in to access this page.");
			return;
		}
		String rq = request.getParameter("rq");
		if(rq != null && !rq.equals("")){
			response.setContentType("application/json");
			String table = request.getParameter("table");
			if(table == null || table.equals("") || !RTDB.tableExists(table)){
				response.getWriter().append("{\"error\":\"table_missing\"}");
				return;
			}
			if(!RTDB.tableExists(table)){
				response.getWriter().append("{\"error\":\"table_null\"}");
				return;
			}
			String reply = "{}";
			String id = request.getParameter("id");
			if(id == null || id.equals("") || id.equals("ID")){
				response.getWriter().append("{\"error\":\"missing_id\"}");
				return;
			}
			try{
				switch(rq){
					case "update":{
						MapObject obj = JsonUtil.toMapObject(JsonUtil.getObjectFromString(request.getParameter("data")));
						RTDB.get().table(table).get(id).update(obj.with("id", id)).run(RTDB.conn());
						break;
					}
					case "delete":{
						RTDB.get().table(table).get(id).delete().run(RTDB.conn());
						break;
					}
					case "insert":{
						MapObject obj = JsonUtil.toMapObject(JsonUtil.getObjectFromString(request.getParameter("data")));
						RTDB.get().table(table).insert(obj.with("id", id)).run(RTDB.conn());
						break;
					}
				}
				response.getWriter().append(reply);
			}
			catch(Exception e){
				response.getWriter().append("{\"error\":\"" + e.getMessage() + "\"}");
				return;
			}
			return;
		}
		Document doc = FileCache.newDocument(user, "Database Viewer");
		String string = new String();
		String table = request.getParameter("table");
		if(table == null || table.equals("") || !RTDB.tableExists(table)){
			string = "<h2>Table not found.</h2>";
		}
		else{
			string += "<table id=database_table data-table=" + table + ">";
			Cursor<HashMap<String, Object>> cursor = RTDB.get().table(table).run(RTDB.conn());
			while(cursor.hasNext()){
				HashMap<String, Object> map = cursor.next();
				string += "<tr><td onClick='databaseTableUpdate(\"" + map.get("id").toString() + "\");'>Update</td><td onClick='databaseTableDelete(\"" + map.get("id").toString() + "\");'>Delete</td>";
				string += "<td class='database_table_rowcontent' id='dbt_" + map.get("id").toString() + "' contentEditable=\"true\">" + JsonUtil.fromMapObject(map).toString() + "</td></tr>";
			}
			string += "<tr><td onClick='databaseTableInsert();'>Insert</td><td id=database_table_insert_id contentEditable=\"true\">ID</td><td id=database_table_insert contentEditable=\"true\" style='padding:20px;font-size:16px;'>&lt;stuff-to-insert-here&gt;</td></tr>";
			string += "</table><script>convertDBJSON();</script>";
		}
		doc.getElementById("content").html(string);
		//
		String sidebar = new String();
		ArrayList<String> arr = RTDB.get().tableList().run(RTDB.conn());
		for(String str : arr){
			sidebar += String.format("<div class=sidebar_button onClick=\"location.href='/database?table=%s'\">%s</div>", str, str);
		}
		sidebar += FileCache.getResource("database_viewer", "html");
		doc.getElementById("sidebar").html(sidebar);
		response.getWriter().append(doc.toString());
		return;
	}

}
