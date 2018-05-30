package net.fexcraft.web.util.user;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.fexcraft.web.util.user.GenericUser.LoginResult;

public class UserCache {
	
	public class ScheduledSaving implements org.quartz.Job {

		@Override
		public void execute(JobExecutionContext arg0) throws JobExecutionException {
			for(Map.Entry<String, User> entry : ONLINE_USERS.entrySet()){
				if(entry.getValue().hasChanged()){
					entry.getValue().save();
				}
			}
		}

	}

	private static final TreeMap<String, User> ONLINE_USERS = new TreeMap<String, User>();

	public static void addGuest(HttpSession session){
		ONLINE_USERS.put(session.getId(), new GuestUser());
	}

	public static void removeUser(HttpSession session){
		User user = ONLINE_USERS.remove(session.getId());
		if(user != null){
			user.unload();
		}
	}
	
	/** Returns String with Login Result. */
	public static String tryLogin(HttpSession session, String mail, String pass){
		LoginResult result = GenericUser.validate(session, mail, pass);
		if(result.success()){
			ONLINE_USERS.put(session.getId(), result.getUser());
			return "Login successful.";
		}
		else return result.getError();
	}
	
	public static User getUser(HttpSession session){
		return getUser(session.getId());
	}
	
	public static User getUser(String session){
		User user = ONLINE_USERS.get(session);
		return user == null ? new GuestUser() : user;
	}

}
