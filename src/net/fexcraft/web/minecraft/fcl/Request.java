package net.fexcraft.web.minecraft.fcl;

import com.google.gson.JsonObject;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Request extends HttpServlet {

	private static final long serialVersionUID = 1L;
	//public static final String NULL_UUID_STRING = "00000000-0000-0000-0000-000000000000";
	//public static SimpleDateFormat format1 = new SimpleDateFormat("(dd-MM-yy) [HHmm!SS]");

	public Request(){ super(); }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObject reply = new JsonObject();
		String mode = request.getParameter("mode");
		if(mode == null){ mode = ""; } mode = mode.toLowerCase();
		String modid = request.getParameter("modid");
		if(modid == null){ modid = ""; }
		boolean pp = !(request.getParameter("pp") == null);
		response.setContentType("application/json");
		try{
			String id = modid;
			switch(mode){
				case "exists":
					reply.addProperty("exists", (Long)RTDB.get().table("mc_fcl_json").filter(obj -> obj.g("id").eq(id)).count().run(RTDB.conn()) > 0);
					break;
				case "requestdata":
					reply = JsonUtil.fromMapSubObject(RTDB.get().table("mc_fcl_json").get(id).run(RTDB.conn()), "data");
					break;
				case "update_version":
					//TODO
					break;
				case "getforgeupdatejson":
					reply.addProperty("error", "Currently Unavailable.");
					break;
				case "blacklist":
					reply = getBlackList(request, request.getParameter("id"));
					break;
				case "isdonor":
					reply = getDonorList(request.getParameter("id"));
					break;
				case "logserver": case "logclient":
					reply.addProperty("error", "Stat collection has been disabled, please check if there is an update for your FCL version available, or do disable the specific setting in config!");
					reply.addProperty("done", true);
					break;
				case "mc_server_status": {
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
			while ((b = in.read()) != -1) {
				if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
					str.append((char) b);
				}
			}
			String[] arr = str.toString().split("\u00A7");
			String string = "\u00A7" + arr[arr.length - 2] + "\u00A7" + arr[arr.length - 1];
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

	//public static long getTime() { return System.currentTimeMillis(); }

	private JsonObject getDonorList(String id){
		JsonObject object = new JsonObject();
		if(id == null || id.equals("")){
			object.add("list", JsonUtil.fromCursor(RTDB.get().table("mc_fcl_donorlist").filter(obj -> obj.g("expired").eq(0)).run(RTDB.conn())));
		}
		else{
			object = JsonUtil.fromMapObject(RTDB.get().table("").get(id).run(RTDB.conn()));
		}
		return object;
	}

	private JsonObject getBlackList(HttpServletRequest request, String id){
		JsonObject object = new JsonObject();
		if(id == null || id.equals("")){
			object.add("blacklist", JsonUtil.fromCursor(RTDB.get().table("mc_fcl_blacklist").filter(obj -> obj.g("expired").eq(0)).run(RTDB.conn())));
		}
		else{
			String sid = id.toLowerCase().equals("server") ? request.getRemoteAddr() : id;
			if((Long)RTDB.get().table("mc_fcl_blacklist").filter(obj -> obj.g("id").eq(sid)).count().run(RTDB.conn()) > 0){
				object = JsonUtil.fromMapObject(RTDB.get().table("mc_fcl_blacklist").get(sid).run(RTDB.conn()));
			}
			else{
				object.addProperty("unbanned", true);
			}
		}
		return object;
	}

}
