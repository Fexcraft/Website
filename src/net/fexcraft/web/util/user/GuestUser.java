package net.fexcraft.web.util.user;

import javax.servlet.http.HttpSession;

public class GuestUser extends User {

	public GuestUser(){}

	@Override
	public boolean isGuest(){
		return true;
	}

	@Override
	public boolean isAdmin(){
		return false;
	}

	@Override
	public HttpSession getSession(){
		return null;
	}

	@Override
	public void unload(){
		//
	}

	/*@Override
	public JsonObject getData(){
		return new JsonObject();
	}*/

	@Override
	public void save(){
		// Guest user does not save.
	}

	@Override
	public String getId(){
		return "guest";
	}

	@Override
	public void markChanged(){
		// Guest user does not change.
	}

	@Override
	public String getName(){
		return "Guest";
	}

	@Override
	public void setName(String newname){
		// Guest user does not change names.
	}

	@Override
	public boolean hasChanged(){
		return false;
	}

	@Override
	public String getProfileImage(){
		return "";//TODO default image
	}

	@Override
	public void setProfileImage(String newimage){
		// Guest user image cannot be changes this way.
	}

}
