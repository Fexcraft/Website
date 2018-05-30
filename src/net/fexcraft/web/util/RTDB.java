package net.fexcraft.web.util;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import net.fexcraft.web.Fexcraft;

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
		def_tables.add("users");
		def_tables.add("downloads");
		def_tables.add("mc_fcl_json");
		def_tables.add("mc_fcl_donors");
		def_tables.add("mc_fcl_blacklist");
		def_tables.add("mc_fcl_serverlog");
		def_tables.add("mc_fcl_clientlog");
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
						MapObject object = new MapObject();
						object.with("name", mir1n); object.with("link", mir1l);
						if(mir2n != null && !mir2n.equals("") && !mir2n.equals(" ")){
							object.with("name", mir2n); object.with("link", mir2l);
						}
						if(mir3n != null && !mir3n.equals("") && !mir3n.equals(" ")){
							object.with("name", mir3n); object.with("link", mir3l);
						}
						instance.table("downloads")
							.insert(instance.hashMap("version", version)
							.with("modid", modid)
							.with("mc_version", mc_version)
							.with("mirrors", object)
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
							.insert(instance.hashMap()
							.with("modid", modid)
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

}
