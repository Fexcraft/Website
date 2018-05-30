package net.fexcraft.web.util.user;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

public abstract class User {
	
	public abstract boolean isGuest();
	
	public abstract boolean isAdmin();
	
	public abstract @Nullable HttpSession getSession();
	
	//public abstract JsonObject getData();
	
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("guest", isGuest());
		obj.addProperty("admin", isAdmin());
		obj.addProperty("username", getName());
		obj.addProperty("profile_image", getProfileImage());
		//obj.add("data", getData());
		return obj;
	}

	public abstract void unload();
	
	public abstract void save();
	
	public abstract String getId();
	
	public abstract void markChanged();
	
	public abstract boolean hasChanged();
	
	public abstract String getName();
	
	public abstract void setName(String newname);
	
	public abstract String getProfileImage();
	
	public abstract void setProfileImage(String newimage);

}
