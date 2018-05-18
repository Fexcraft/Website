package net.fexcraft.web.util.user;

import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.MySql;

public class GenericUser extends User {

	private String name, profile_image;
	private HttpSession session;
	//private JsonObject data;
	private boolean changed;
	private int rank, id;
	
	public static final LoginResult validate(HttpSession session, String mail, String pass){
		LoginResult result = new LoginResult();
		String hash = MySql.WEB.getString("password", "users", "email", mail);
		if(hash == null){
			return result.setResult("Credentials not found in Database.");
		}
		if(BCrypt.checkpw(pass, hash)){
			int id = MySql.WEB.getInt("id", "users", "email", mail, -1);
			if(id == -1){
				return result.setResult("User ID not found! This is bad.");
			}
			else{
				return result.setResult(new GenericUser(session, id));
			}
		}
		else return result.setResult("Invalid E-Mail or Password.");
	}
	
	public static final class LoginResult {
		
		private GenericUser user;
		private String message;
		
		private LoginResult setResult(String str){
			this.message = str;
			return this;
		}
		
		private LoginResult setResult(GenericUser user){
			this.user = user;
			return this;
		}
		
		public boolean success(){
			return user != null;
		}
		
		public String getError(){
			return message;
		}
		
		public GenericUser getUser(){
			return user;
		}
		
	}
	
	private GenericUser(HttpSession session, int id){
		this.session = session; this.id = id;
		this.rank = MySql.WEB.getInt("rank", "users", "id", id, 0);
		//this.data = JsonUtil.getObjectFromString(MySql.WEB.getString("data", "users", "id", id, "{}"));
	}

	@Override
	public boolean isGuest(){
		return false;
	}

	@Override
	public boolean isAdmin(){
		return rank >= 4;
	}

	@Override
	public HttpSession getSession(){
		return session;
	}

	/*@Override
	public JsonObject getData(){
		return data;
	}*/

	@Override
	public void unload(){
		//TODO checks
		save();
	}

	@Override
	public void save(){
		if(!changed){ return; }
		try{
			//MySql.WEB.update("users", "data='" + data.toString() + "'", "id", id);
			MySql.WEB.update("users", "name='" + name + "'", "id", id);
		}
		catch(Exception e){
			e.printStackTrace();
			Fexcraft.error("Error while saving user with ID " + id + " and name '" + name + "'!");
		}
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public void markChanged(){
		this.changed = true;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void setName(String newname){
		this.name = newname;
		this.markChanged();
	}

	@Override
	public boolean hasChanged(){
		return changed;
	}

	@Override
	public String getProfileImage(){
		return profile_image;
	}

	@Override
	public void setProfileImage(String newimage){
		this.profile_image = newimage;
		this.markChanged();
	}

}
