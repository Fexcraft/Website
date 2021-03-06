package net.fexcraft.web.util;

import com.google.gson.JsonObject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.ast.ReqlAst;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import net.fexcraft.web.Fexcraft;

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

	public static com.rethinkdb.gen.ast.Table table(String string){
		return instance.table(string);
	}

	public static com.rethinkdb.gen.ast.Table get(Table table){
		return instance.table(table.rtid);
	}
	
	public static <T> T run(ReqlAst reql){
		return reql.run(conn);
	}
	
	public static enum Table {
		ACCOUNTS("accounts"),
		USERS("users"),
		NEWS("news"),
		DOWNLOADS("downloads"),
		DOWNLOAD_TOKENS("download_tokens"),
		FCL_JSONS("mc_fcl_json"),
		FCL_BLACKLIST("mc_fcl_blacklist"),
		FBOT("discord_fbot"),
		F_FORUMS("forum_forums"),
		F_TOPICS("forum_topics"),
		F_POSTS("forum_posts");
		
		String rtid;
		Table(String id){ this.rtid = id; }
	}
	
	public static final void prepare(){
		for(Table table : Table.values()){
			if(!tableExists(table.rtid)){
				Fexcraft.info("Creating table '" + table.rtid + "'!");
				instance.tableCreate(table.rtid).run(conn);
			}
		}
		/*if((Long)instance.table("downloads").count().run(conn) == 0){
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
		}*/
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

}
