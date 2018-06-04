package net.fexcraft.web.util;

import com.google.gson.JsonObject;
import com.rethinkdb.net.Cursor;

import org.mindrot.jbcrypt.BCrypt;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.servlet.http.HttpSession;
import java.util.*;

public class UserObject {

	private static TreeMap<String, UserObject> ONLINE_USERS = new TreeMap<>();
	private HashMap<String, Object> map;

	public static UserObject fromSession(HttpSession session){
		UserObject obj = ONLINE_USERS.get(session.getId());
		if(obj == null){
			obj = new UserObject(); ONLINE_USERS.put(session.getId(), obj);
		}
		obj.map = RTDB.get().table("sessions").get(session.getId()).run(RTDB.conn());
		obj.updateActivity();
		return obj;
	}

	public class ScheduledClearing implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			for(Map.Entry<String, UserObject> entry : ONLINE_USERS.entrySet()){
				if(entry.getValue().getLastActive() + 900000 < new Date().getTime()){
					ONLINE_USERS.remove(entry.getKey());
				}
			}
			//
			ArrayList<String> oldtokens = new ArrayList<>();
			Cursor<HashMap<String, Object>> cursor = RTDB.get().table("download_tokens").run(RTDB.conn());
			for(HashMap<String, Object> obj : cursor){
				JsonObject json = JsonUtil.fromMapObject(obj);
				if(json.get("time").getAsLong() < System.currentTimeMillis()){
					oldtokens.add(json.get("id").getAsString());
				}
			}
			for(String tok : oldtokens){
				RTDB.get().table("download_tokens").get(tok).delete().run(RTDB.conn());
			}
		}

	}

	public long getLastActive(){
		return (Long)map.get("activity");
	}

	public void updateActivity(){
		map.put("activity", new Date().getTime());
		RTDB.get().table("sessions").get(getSessionId()).update(RTDB.get().hashMap("activity", getLastActive())).run(RTDB.conn());
	}

	public String getSessionId(){
		return (String)map.get("id");
	}

	public boolean isGuest(){
		return (Boolean)map.get("guest");
	}

	public boolean isAdmin(){
		if(isGuest()){
			return false;
		}
		return (Boolean)RTDB.get().table("users").get((Long)map.get("user")).getField("admin").run(RTDB.conn());
	}
	
	public long getId(){
		return isGuest() ? -1 : (Long)map.get("user");
	}

	public JsonObject tryLogin(String mail, String pass){
		JsonObject obj = new JsonObject();
		obj.addProperty("success", false);
		if(mail == null || mail.equals("")){
			obj.addProperty("status", "Empty E-Mail adress.");
		}
		else if(pass == null || pass.equals("")){
			obj.addProperty("status", "Empty Password.");
		}
		else{
			HashMap<String, Object> res = RTDB.get().table("accounts").get(mail).run(RTDB.conn());
			if(res == null){
				obj.addProperty("status", "Account not found.");
			}
			else{
				if(BCrypt.checkpw(pass, (String)res.get("password"))){
					RTDB.get().table("sessions").get(getSessionId()).update(RTDB.get().hashMap("guest", false).with("user", (Long)res.get("userid"))).run(RTDB.conn());
					this.updateActivity();
					//
					obj.addProperty("status", "Successfully logged in!");
					obj.addProperty("success", true);
				}
				else{
					obj.addProperty("status", "Invalid E-Mail or password.");
				}
			}
		}
		return obj;
	}

	public JsonObject logout(){
		JsonObject obj = new JsonObject();
		RTDB.get().table("sessions").get(getSessionId()).update(RTDB.get().hashMap("guest", true).with("user", -1)).run(RTDB.conn());
		this.updateActivity();
		obj.addProperty("status", "Logged out.");
		obj.addProperty("success", true);
		return obj;
	}

}
