package net.fexcraft.web.minecraft.fcl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.MySql;

public class Request extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static final String NULL_UUID_STRING = "00000000-0000-0000-0000-000000000000";
	public static SimpleDateFormat format1 = new SimpleDateFormat("(dd-MM-yy) [HHmm!SS]");
	
    public Request() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject reply = new JsonObject();
		String mode  = request.getParameter("mode");  if(mode  == null){ mode  = "";}
		String modid = request.getParameter("modid"); if(modid == null){ modid = "";}
		boolean pp = !(request.getParameter("pp") == null);
		response.setContentType("application/json");
		try{
			switch(mode){
				case "exists":
					reply.addProperty("exists", MySql.WEB.exists("id", "mc_fcl_json", "id", modid, true));
					break;
				case "requestdata":
					reply = JsonUtil.getObjectFromString(MySql.WEB.getString("data", "mc_fcl_json", "id", modid));
					break;
				case "update_version":
					//TODO
					break;
				case "getForgeUpdateJson":
					reply = JsonUtil.getObjectFromString(MySql.WEB.getString("data", "mc_fcl_forge_json", "id", modid));
					break;
				case "blacklist":
					reply = getBlackList(request, request.getParameter("id"));
					break;
				case "isDonor":
					reply = getDonorList(request.getParameter("id"));
					break;
				case "logServer":
					logServer(request, response);
					reply.addProperty("done", true);
					break;
				case "logClient":
					logClient(request, response);
					reply.addProperty("done", true);
					break;
				case "mc_server_status":{
					getServerStatus(modid, reply);
					break;
				}
				default:
					reply.addProperty("error", "empty-request");
					break;
			}
			response.getWriter().append(pp ? JsonUtil.setPrettyPrinting(reply).toString() : reply.toString());
		}
		catch(Exception e){
			response.setStatus(500);
			response.getWriter().append("{\"error\":\"" + e.getMessage() + "\"}");
		}
	}
	
	private void getServerStatus(String modid, JsonObject reply){
		String[] adr = modid.split(":");
		try{
			@SuppressWarnings("resource")
			Socket sock = new Socket(adr[0], adr.length >= 2 ? Integer.parseInt(adr[1]) : 25565);
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			DataInputStream in = new DataInputStream(sock.getInputStream());
			out.write(0xFE);
			int b;
			StringBuffer str = new StringBuffer();
			while((b = in.read()) != -1){
				if(b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
					str.append((char) b);
				}
			}
			String[] arr = str.toString().split("§");
			String string = "§" + arr[arr.length - 2] + "§" + arr[arr.length - 1 ];
			reply.addProperty("motd", str.toString().replace(string, ""));
			reply.addProperty("players", arr[arr.length - 2]);
			reply.addProperty("slots", arr[arr.length - 1]);
			reply.addProperty("online", true);
		}
		catch(UnknownHostException e){
			//e.printStackTrace();
			reply.addProperty("motd", "Server not found.");
			reply.addProperty("online", false);
		}
		catch(IOException e){
			//e.printStackTrace();
			reply.addProperty("motd", "Could not connect to Server.");
			reply.addProperty("online", false);
		}
		catch(Exception e){
			e.printStackTrace();
			reply.addProperty("motd", "Internal error.");
			reply.addProperty("online", false);
		}
	}

	private void logClient(HttpServletRequest request, HttpServletResponse response) {
		try{
			String ip = request.getRemoteAddr();
			String version = request.getParameter("version");
			String data = request.getParameter("data");
			String uuid = NULL_UUID_STRING;
			if(data == null || data.equals("")){
				data = "{}";
			}
			else{
				JsonObject obj = JsonUtil.getObjectFromString(data);
				if(obj.get("uuid").getAsString().equals(NULL_UUID_STRING)){
					return;
				}
				else if(obj.get("settings").getAsString().toLowerCase().equals("remove")){
					MySql.WEB.update("DELETE FROM " + client_log + " WHERE uuid='" + uuid + "';");
					return;
				}
				else{
					uuid = obj.get("uuid").getAsString();
				}
			}
			if(MySql.WEB.exists("uuid", client_log, "uuid='" + uuid + "'", false)){
				MySql.WEB.update("UPDATE " + client_log + " SET ip='" + ip + "', version='" + version + "', time='" + getTime() + "', data='" + data + "' WHERE uuid='" + uuid + "';");
			}
			else{
				MySql.WEB.insert(client_log, "ip, version, time, uuid, data", "'" + ip + "', '" + version + "', '" + getTime() + "', '" + uuid + "', '" + data + "'");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return;
		}
	}
	
	public static long getTime(){
		return System.currentTimeMillis();
	}

	private static final String client_log = "mc_fcl_clientlog";
	private static final String server_log = "mc_fcl_serverlog";
	
	private void logServer(HttpServletRequest request, HttpServletResponse response) {
		try{
			String ip = request.getRemoteAddr();
			String name = request.getParameter("hostname");
			String port = request.getParameter("port");
			String motd = request.getParameter("motd");
			String version = request.getParameter("version");
			String data = request.getParameter("data");
			if(name == null || data.equals("")){
				name = "null";
			}
			if(data == null || data.equals("")){
				data = "{}";
			}
			if(data.toLowerCase().equals("remove")){
				MySql.WEB.update("DELETE FROM " + server_log + " WHERE ip='" + ip + "' AND port='" + port + "';");
				return;
			}
			if(MySql.WEB.exists("ip", server_log, "ip='" + ip + "' AND port='" + port + "'", false)){
				MySql.WEB.update("UPDATE " + server_log + " SET version='" + version + "', hostname='" + name + "', motd='" + motd + "', last_update='" + getTime() + "', data='" + data + "' WHERE ip='" + ip + "' AND port='" + port + "';");
			}
			else{
				MySql.WEB.insert(server_log, "ip, version, hostname, port, motd, last_update, data", "'" + ip + "', '" + version + "', '" + name + "', '" + port + "', '" + motd + "', '" + getTime() + "', '" + data + "'");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return;
		}
		
	}
	
	private JsonObject getDonorList(String id){
		JsonObject object = new JsonObject();
		if(id == null || id.equals("")){
			JsonArray array = new JsonArray();
			try{
				ResultSet set = MySql.WEB.query("SELECT uuid FROM mc_fcl_donors WHERE expired = '0';");
				while(set.next()){
					array.add(new JsonPrimitive(set.getString("uuid")));
				}
			}
			catch(Exception ex){
				//
			}
			object.add("list", array);
		}
		else{
			try{
				ResultSet set = MySql.WEB.query("SELECT name, uuid, time, expires, expired FROM mc_fcl_donors WHERE uuid='" + id + "';");
				if(set.first()){
					object.addProperty("name", set.getString("name"));
					object.addProperty("uuid", set.getString("uuid"));
					object.addProperty("time", set.getString("time"));
					object.addProperty("expires", set.getString("expires"));
					object.addProperty("expired", set.getBoolean("expired"));
				}
				else{
					object.addProperty("expired", true);
				}
			}
			catch(Exception ex){
				//
			}
		}
		return object;
	}
	
	private JsonObject getBlackList(HttpServletRequest request, String id){
		JsonObject object = new JsonObject();
		if(id == null || id.equals("")){
			JsonArray array = new JsonArray();
			try{
				ResultSet set = MySql.WEB.query("SELECT uuid FROM mc_fcl_blacklist WHERE unbanned = '0';");
				while(set.next()){
					array.add(new JsonPrimitive(set.getString("uuid")));
				}
			}
			catch(Exception ex){
				//
			}
			object.add("blacklist", array);
		}
		else{
			if(id.toLowerCase().equals("server")){
				try{
					ResultSet set = MySql.WEB.query("SELECT name, uuid, time, reason, unbanned FROM mc_fcl_blacklist WHERE uuid='" + request.getRemoteAddr() + "';");
					if(set.first()){
						object.addProperty("name", set.getString("name"));
						object.addProperty("uuid", set.getString("uuid"));
						object.addProperty("time", set.getString("time"));
						object.addProperty("reason", set.getString("reason"));
						object.addProperty("unbanned", set.getBoolean("unbanned"));
					}
					else{
						object.addProperty("unbanned", true);
					}
				}
				catch(Exception ex){
					//
				}
			}
			else{
				try{
					ResultSet set = MySql.WEB.query("SELECT name, uuid, time, reason, unbanned FROM mc_fcl_blacklist WHERE uuid='" + id + "';");
					if(set.first()){
						object.addProperty("name", set.getString("name"));
						object.addProperty("uuid", set.getString("uuid"));
						object.addProperty("time", set.getString("time"));
						object.addProperty("reason", set.getString("reason"));
						object.addProperty("unbanned", set.getBoolean("unbanned"));
					}
					else{
						object.addProperty("unbanned", true);
					}
				}
				catch(Exception ex){
					//
				}
			}
		}
		return object;
	}

}
