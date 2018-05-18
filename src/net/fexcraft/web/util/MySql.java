package net.fexcraft.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.web.Fexcraft;

/**
 * @author Ferdinand (FEX___96)
 */
public class MySql {
	
	private final String user;
	private final String database;
	private final String password;
	private final String port;
	private final String hostname;
	
	private Connection c;
	private Statement s;
	
	public static MySql WEB;
	
	public MySql(String... data){
		user = data[0];
		password = data[1];
		port = data[2];
		hostname = data[3];
		database = data[4];
		WEB = this;
	}
	
	public Connection connect() throws Exception{
		if(isConnected()){
			return c;
		}
		String url = "jdbc:mysql://" + hostname + ":" + port + (database == null ? "" : "/" + database);
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		c = DriverManager.getConnection(url, user, password);
		return c;
	}

	public MySql copy(){
		return new MySql(user, password, port, hostname, database);
	}
	
	public boolean isConnected() throws SQLException{
		return c != null && !c.isClosed();
	}
	
	public boolean isClosed() throws SQLException{
		return c == null || c.isClosed();
	}

	public Connection getConnection(){
		return c;
	}

	public boolean disconnect() throws Exception{
		if(c == null){
			return false;
		}
		c.close();
		return true;
	}
	
	public Statement getStatement() throws Exception{
		if(!isConnected()){
			connect();
		}
		if(s == null || s.isClosed()){
			s = c.createStatement();
		}
		//s = c.createStatement();
		return s;
	}

	public ResultSet query(String query){
		try{
			return getStatement().executeQuery(query);
		}
		catch(Exception e){
			Fexcraft.error(query);
			e.printStackTrace();
			return null;
		}
	}
	
	public int query(String query, String id, int def){
		try{
			ResultSet set = query(query);
			return set.first() ? set.getInt(id) : def;
		}
		catch(Exception e){
			System.out.println(query);
			e.printStackTrace();
			return def;
		}
	}

	public int update(String query){
		try{
			return getStatement().executeUpdate(query);
		}
		catch(Exception e){
			Fexcraft.error(query);
			e.printStackTrace();
			return -1;
		}
	}
	
	public String getString(String target, String table, String compare, Object with, String def){
		try{
			ResultSet set = query("SELECT " + target + " FROM " + table + " WHERE " + compare + " = '" + with.toString() + "';");
			if(set.first()){
				String s = set.getString(target);
				return target == null ? def : s;
			}
			return def;
		}
		catch(Exception e){
			e.printStackTrace();
			return def;
		}
	}
	
	public String getString(String target, String table, String compare, Object with){
		try{
			return getString(target, table, compare, with, null);
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public JsonObject getObject(String target, String table, String compare, String with, String def){
		String s = getString(target, table, compare, with);
		return JsonUtil.getObjectFromString(s == null ? def : s);
	}
	
	public JsonElement getElement(String target, String table, String compare, String with, String def){
		String s = getString(target, table, compare, with);
		return JsonUtil.getFromString(s == null ? def : s);
	}
	
	public JsonObject getObject(String target, String table, String compare, String with){
		return getObject(target, table, compare, with, null);
	}
	
	public int getInt(String target, String table, String compare, Object to, int def){
		try{
			ResultSet set = query("SELECT " + target + " FROM " + table + " WHERE " + compare + " = '" + to.toString() + "';");
			return set.first() ? set.getInt(target) : def;
		}
		catch(Exception ex){
			//ex.printStackTrace();
			return def;
		}
	}
	
	public int getInt(String target, String table, String compare, Object topic){
		return getInt(target, table, compare, topic, -1);
	}
	
	public long getLong(String target, String table, String compare, Object to, long def){
		try{
			ResultSet set = query("SELECT " + target + " FROM " + table + " WHERE " + compare + " = '" + to.toString() + "';");
			return set.first() ? set.getLong(target) : def;
		}
		catch(Exception ex){
			//ex.printStackTrace();
			return def;
		}
	}
	
	public boolean exists(String target, String table, String compare, String with, boolean def){
		try{
			return query("SELECT " + target + " FROM " + table + " WHERE " + compare + " = '" + with + "' LIMIT 1;").first();
		}
		catch(Exception ex){
			ex.printStackTrace();
			return def;
		}
	}
	
	public boolean exists(String target, String table, String equals, boolean def){
		try{
			return query("SELECT " + target + " FROM " + table + " WHERE " + equals + " LIMIT 1;").first();
		}
		catch(Exception ex){
			ex.printStackTrace();
			return def;
		}
	}
	
	public int update(String table, String set, String compare, String with) throws Exception{
		return update("UPDATE " + table + " SET " + set + " WHERE " + compare + " = '" + with + "';");
	}

	public int update(String table, String set, Object to, String compare, Object with) throws Exception {
		return update("UPDATE " + table + " SET " + set + "='" + to.toString() + "' WHERE " + compare + " = '" + with.toString() + "';");
	}
	
	public int update(String table, String set, String compare, int with) throws Exception{
		return update("UPDATE " + table + " SET " + set + " WHERE " + compare + " = '" + with + "';");
	}

	public int insert(String table, String rows, String content) throws Exception {
		return update("INSERT INTO "+ table +" (" + rows + ") VALUES (" + content + ");");
	}

	public ArrayList<Integer> getArray(String select, String from, String where, int equals, String orderby, boolean desc){
		ArrayList<Integer> array = new ArrayList<Integer>();
		try{
			ResultSet set = query("SELECT " + select + " FROM " + from + " WHERE " + where + " = '" + equals + "' ORDER BY " + orderby + (desc ? " DESC;" : " ASC;"));
			while(set.next()){
				array.add(set.getInt(select));
			}
			return array;
		}
		catch(Exception e){
			return array;
		}
	}
	
	public ArrayList<Integer> getArray(String select, String from, String where, String orderby, boolean desc){
		ArrayList<Integer> array = new ArrayList<Integer>();
		try{
			ResultSet set = query("SELECT " + select + " FROM " + from + " WHERE " + where + " ORDER BY " + orderby + (desc ? " DESC;" : " ASC;"));
			while(set.next()){
				array.add(set.getInt(select));
			}
			return array;
		}
		catch(Exception e){
			return array;
		}
	}

	public ArrayList<Integer> getArray(String select, String from, String where, int equals, String orderby, boolean desc, int limit){
		ArrayList<Integer> array = new ArrayList<Integer>();
		try{
			int i = 0;
			ResultSet set = query("SELECT " + select + " FROM " + from + " WHERE " + where + " = '" + equals + "' ORDER BY " + orderby + (desc ? " DESC;" : " ASC;"));
			while(set.next() && i < limit){
				array.add(set.getInt(select));
				i++;
			}
			return array;
		}
		catch(Exception e){
			return array;
		}
	}
	
	public int count(String table, String what){
		return count(table, what, 0);
	}

	public int count(String table, String what, int def){
		try{
			ResultSet set = query("SELECT COUNT(" + what + ") AS count FROM " + table + ";");
			if(set.first()){
				return set.getInt("count");
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return def;
	}
	
	public int count(String table, String what, String where, int def){
		try{
			ResultSet set = query("SELECT COUNT(" + what + ") AS count FROM " + table + " WHERE " + where + ";");
			if(set.first()){
				return set.getInt("count");
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return def;
	}

	public boolean getBoolean(String target, String table, String compare, Object to, boolean def){
		try{
			ResultSet set = query("SELECT " + target + " FROM " + table + " WHERE " + compare + " = '" + to.toString() + "';");
			return set.first() ? set.getBoolean(target) : def;
		}
		catch(Exception ex){
			//ex.printStackTrace();
			return def;
		}
	}

	public ResultSet queryJsonArray(String table, String where, String... strings){
		String string = new String();
		for(int i = 0; i < strings.length; i++){
			string += String.format("'%s', %s", strings[i], strings[i]);
			if(i != strings.length - 1){
				string += ", ";
			}
		}
		return this.query("SELECT CONCAT('[',GROUP_CONCAT(JSON_OBJECT(" + string + ")),']') AS json FROM " + table + " " + where + ";");
	}
	
}