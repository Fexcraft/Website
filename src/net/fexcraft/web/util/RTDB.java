package net.fexcraft.web.util;

import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import net.fexcraft.web.Fexcraft;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.mindrot.jbcrypt.BCrypt;

public class RTDB {

	private static final RethinkDB instance = RethinkDB.r;
	private static final Connection conn = instance.connection().db("web").hostname("localhost").port(28015).connect();
	
	public static final RethinkDB get(){
		return instance;
	}
	
	public static final Connection conn(){
		return conn;
	}
	
	private static final ArrayList<String> def_tables = new ArrayList<>();
	static{
		def_tables.add("accounts");
		def_tables.add("sessions");
		def_tables.add("users");
		def_tables.add("news");
		def_tables.add("downloads");
		def_tables.add("download_tokens");
		def_tables.add("mc_fcl_json");
		def_tables.add("mc_fcl_blacklist");
		def_tables.add("discord_fbot");
		//def_tables.add("forums_forums");
		//def_tables.add("forums_topics");
		//def_tables.add("forums_posts");
	}
	
	public static final void prepare(){
		def_tables.forEach(def -> {
			if(!tableExists(def)){
				Fexcraft.info("Creating table '" + def + "'!");
				instance.tableCreate(def).run(conn);
			}
		});
		if((Long)instance.table("downloads").count().run(conn) == 0){
			try{
				ResultSet set = MySql.WEB.query("select * from downloads");
				while(set.next()){
					try{
						String modid = set.getString("modid");
						String version = set.getString("version");
						String mc_version = set.getString("mc_version");
						String mir1n = set.getString("mirror1_name");
						String mir1l = set.getString("mirror1_link");
						String mir2n = set.getString("mirror2_name");
						String mir2l = set.getString("mirror2_link");
						String mir3n = set.getString("mirror3_name");
						String mir3l = set.getString("mirror3_link");
						int listed = set.getInt("listed");
						ArrayList<MapObject> list = new ArrayList<>();
						if(mir1n != null && !mir1n.equals("") && !mir1n.equals(" ")){
							MapObject object = new MapObject();
							object.with("name", mir1n); object.with("link", mir1l);
							list.add(object);
						}
						if(mir2n != null && !mir2n.equals("") && !mir2n.equals(" ")){
							MapObject object = new MapObject();
							object.with("name", mir2n); object.with("link", mir2l);
							list.add(object);
						}
						if(mir3n != null && !mir3n.equals("") && !mir3n.equals(" ")){
							MapObject object = new MapObject();
							object.with("name", mir3n); object.with("link", mir3l);
							list.add(object);
						}
						instance.table("downloads")
							.insert(instance.hashMap("version", version)
							.with("modid", modid)
							.with("mc_version", mc_version)
							.with("mirrors", list)
							.with("listed", listed == 1)).run(conn);
					}
					catch (Exception e){
						Fexcraft.error(e);
						e.printStackTrace();
					}
				}
			}
			catch(Exception e){
				Fexcraft.error(e);
				e.printStackTrace();
			}
		}
		if((Long)instance.table("mc_fcl_json").count().run(conn) == 0){
			try{
				ResultSet set = MySql.WEB.query("select * from mc_fcl_json");
				while(set.next()){
					try{
						String modid = set.getString("id");
						JsonObject obj = JsonUtil.getObjectFromString(set.getString("data"));
						instance.table("mc_fcl_json")
							.insert(instance.hashMap("id", modid)
							.with("data", JsonUtil.toMapObject(obj))).run(conn);
					}
					catch (Exception e){
						Fexcraft.error(e);
						e.printStackTrace();
					}
				}
			}
			catch(Exception e){
				Fexcraft.error(e);
				e.printStackTrace();
			}
		}
		instance.table("sessions").delete().run(conn);
		instance.table("download_tokens").delete().run(conn);
		//
		if((Long)instance.table("accounts").count().run(conn) == 0){
			Fexcraft.info("Accounts table seems empty, inserting default Admin account.");
			MapObject map = new MapObject();
			map.with("id", "fercalo96@yahoo.de");
			map.with("password", BCrypt.hashpw(Fexcraft.INSTANCE.getProperty("admin_pass", "null").getAsString(), BCrypt.gensalt()));
			map.with("userid", 1);
			map.with("admin", true);
			instance.table("accounts").insert(map).run(conn);
		}
		if((Long)instance.table("users").count().run(conn) == 0){
			Fexcraft.info("Users table seems empty, inserting default Admin account.");
			MapObject map = new MapObject();
			map.with("id", 1);
			map.with("name", "Ferdinand");
			map.with("admin", true);
			instance.table("users").insert(map).run(conn);
		}
	}
	
	/** May be inacurate. */
	public static boolean dbExists(String str){
		try{
			return instance.db(str).run(conn) != null;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	/** May be inacurate. */
	public static boolean tableExists(String str){
		try{
			return instance.table(str).run(conn) != null;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static JsonObject get(String table, String field, String equals){
		Cursor<Object> cursor = instance.table(table).filter(pre -> pre.g(field).eq(equals)).run(conn);
		return cursor.hasNext() ? JsonUtil.getObjectFromString(cursor.next().toString()) : null;
	}

	public static Table table(String string){
		return instance.table(string);
	}

}
