package net.fexcraft.web.util;

import com.google.gson.JsonObject;
import org.mindrot.jbcrypt.BCrypt;
import javax.servlet.http.HttpSession;
import java.util.*;

public class UserObject {

	private static TreeMap<String, UserObject> ONLINE_USERS = new TreeMap<>();
	private HttpSession session;

	public static UserObject fromSession(HttpSession session){
		UserObject obj = ONLINE_USERS.get(session.getId());
		return obj == null ? obj = new UserObject(session) : obj;
	}
	
	public UserObject(HttpSession session){
		ONLINE_USERS.put(session.getId(), this);
		this.session = session;
	}

	public long getLastActive(){
		return session.getLastAccessedTime();
	}

	public String getSessionId(){
		return session.getId();
	}

	public boolean isGuest(){
		return session.getAttribute("guest") == null ? true : (boolean)session.getAttribute("guest");
	}

	public boolean isAdmin(){
		if(isGuest()){ return false; }
		return session.getAttribute("admin") == null ? false : (boolean)session.getAttribute("admin");
	}
	
	public long getId(){
		return isGuest() ? -1 : (Long)session.getAttribute("user");
	}

	public JsonObject login(String mail, String pass){
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
					session.setAttribute("guest", false);
					session.setAttribute("user", (Long)res.get("userid"));
					session.setAttribute("admin", res.containsKey("admin") ? (boolean)res.get("admin") : false);
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
		session.setAttribute("guest", true);
		session.setAttribute("user", -1);
		session.setAttribute("admin", false);
		//
		obj.addProperty("status", "Logged out.");
		obj.addProperty("success", true);
		return obj;
	}

	public static void removeUser(HttpSession session){
		ONLINE_USERS.remove(session.getId());
	}

}
