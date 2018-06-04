package net.fexcraft.web.files;

import com.google.gson.JsonObject;
import com.rethinkdb.net.Cursor;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.FileCache.FileObject;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;
import net.fexcraft.web.util.UserObject;

import org.jsoup.nodes.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class MainFileServer extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

    public MainFileServer(){
        super();
    }

    private static final String[] rsi = new String[]{"/web/", "/launcher/", "/TXT/", "/Modpack/", "/not-my-mods/"};
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, UnsupportedEncodingException {
		String url = request.getRequestURL().toString();
		for(String s : rsi){
			if(url.contains(s)){
				sendFile(response, url);
				return;
			}
		}
		String token = request.getParameter("token");
		if(token == null || token.equals("")){
			if(FileCache.getFile(url) == null){
				response.sendError(500, "File not found.\n    " + url);
				return;
			}
			Document doc = FileCache.newDocument(UserObject.fromSession(request.getSession()), "Download File");
			doc.getElementById("content").html(FileCache.getResource("download_file", "html"));
			doc.getElementById("sidebar").html(FileCache.getResource("sidebars/default", "html"));
			doc.getElementById("dlstatus").before(FileCache.getResource("ads/ad3-wide", "html"));
			doc.getElementById("dlstatus").after(FileCache.getResource("ads/ad3-wide", "html"));
			doc.getElementById("dlstatus").attr("data-url", url);
			response.getWriter().append(doc.toString());
			return;
		}
		else if(token.equals("request")){
			JsonObject json = new JsonObject();
			try{
				String flurl = request.getParameter("url");
				if(FileCache.getFile(flurl) == null){
					json.addProperty("error", "File not found.");
				}
				else{
					Cursor<HashMap<String, Object>> cursor = RTDB.get().table("download_tokens").filter(obj -> obj.g("ip").eq(request.getRemoteAddr())).filter(obj2 -> obj2.g("url").eq(flurl)).run(RTDB.conn());
					if(cursor.hasNext()){
						json.addProperty("token", (String)cursor.next().get("id"));
					}
					else{
						JsonObject obj = JsonUtil.fromMapObject(RTDB.get().table("download_tokens").insert(RTDB.get().hashMap("ip", request.getRemoteAddr()).with("url", flurl).with("time", System.currentTimeMillis())).run(RTDB.conn()));
						if(obj.has("generated_keys")){
							json.addProperty("token", obj.get("generated_keys").getAsJsonArray().get(0).getAsString());
						}
						else{
							json.addProperty("error", "Unknown Database Error while creating token.");
						}
					}
				}
			}
			catch(Exception e){
				json.addProperty("error", e.getMessage());
				e.printStackTrace();
			}
			response.getWriter().append(json.toString());
			return;
		}
		else if(token.equals("tokens")){
			if(UserObject.fromSession(request.getSession()).isAdmin()){
				response.getWriter().append(JsonUtil.fromCursor(RTDB.get().table("download_tokens").run(RTDB.conn())).toString());
				return;
			}
			response.sendError(403, "Access denied.");
			return;
		}
		else{
			String string = URLDecoder.decode(url, "UTF-8");
			if(JsonUtil.fromCursor(RTDB.get().table("download_tokens").filter(obj -> obj.g("ip").eq(request.getRemoteAddr())).filter(obj2 -> obj2.g("url").eq(string)).run(RTDB.conn())).size() > 0){
				sendFile(response, url);
			}
			else{
				response.sendError(403, "Invalid Download Token.");
			}
			return;
		}
	}

	private void sendFile(HttpServletResponse response, String url) throws IOException {
		try{
			FileObject obj = FileCache.getFile(url);
			if(obj == null){
				response.sendError(500, "File not found.\n    " + url);
				return;
			}
			response.setContentType(obj.getContentType());
			response.getOutputStream().write(obj.getFile());
			return;
		}
		catch(IOException e){
			response.sendError(500, "Error while trying to fetch file.\n    " + url + "");
			if(Fexcraft.dev()){
				e.printStackTrace();
			}
		}
	}

}
