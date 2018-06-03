package net.fexcraft.web.util;

import com.google.gson.JsonObject;
import com.rethinkdb.net.Cursor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.servlet.http.HttpSession;
import java.util.*;

public class UserObject {

	private static TreeMap<String, UserObject> ONLINE_USERS = new TreeMap<>();
	private HashMap<String, Object> map;

	public static UserObject fromSession(HttpSession session){
		UserObject obj = ONLINE_USERS.containsKey(session.getId()) ? ONLINE_USERS.get(session.getId()) : new UserObject();
		obj.map = RTDB.get().table("sessions").get(session.getId()).run(RTDB.conn());
		obj.updateActivity();
		ONLINE_USERS.put(session.getId(), obj);
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
			Cursor cursor = RTDB.get().table("download_tokens").run(RTDB.conn());
			for(Object obj : cursor){
				JsonObject json = JsonUtil.fromMapObject((HashMap<String, Object>)obj);
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

}
