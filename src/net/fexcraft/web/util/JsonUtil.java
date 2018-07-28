package net.fexcraft.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;
import com.rethinkdb.model.MapObject;

import com.rethinkdb.net.Cursor;
import net.fexcraft.web.Fexcraft;

/**
 * @author Ferdinand (FEX___96)
 * @comment Main class for Json processing.
 */
@SuppressWarnings("unchecked")
public class JsonUtil{
	
	private static final JsonUtil instance = new JsonUtil();
	private static final JsonParser parser = new JsonParser();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	/**
	 * @return Current JsonParser instance;
	 */
	public static JsonParser getParser(){
		return parser;
	}
	
	/**
	 * @return Current gson instance used by JsonUtil;
	 */
	public static Gson getGson(){
		return gson;
	}
	
	/**
	 * Gets an JsonObject from given String
	 * @param string
	 * @return empty JsonObject if String == null
	 */
	public static JsonObject getObjectFromString(String string){
		JsonElement elm = getFromString(string);
		if(elm == null){
			return new JsonObject();
		}
		return elm.getAsJsonObject();
	}
	
	/**
	 * Gets an JsonElement from given String,<br>
	 * @param string
	 * @return null if String == null
	 */
	public static JsonElement getFromString(String string, boolean b){
		if(string == null){
			return null;
		}
		try{
			return parser.parse(string);
		}
		catch(Exception e){
			if(b){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static JsonElement getFromString(String string){
		return getFromString(string, true);
	}
	
	public static void addToArrayIfNotContains(JsonArray array, String string){
		boolean contains = false;
		for(JsonElement elm : array){
			if(elm.getAsString().equals(string)){
				contains = true;
				break;
			}
		}
		if(!contains){
			array.add(new JsonPrimitive(string));
		}
	}
	
	public static JsonObject getJsonForPacket(String target){
		JsonObject obj = new JsonObject();
		obj.addProperty("target_listener", target);
		return obj;
	}
	
	/**
	 * @param file the file to be parsed into json
	 * @param b notify in logs about error in parsing, or file missing
	 * @return JsonElement from given file, or null
	 */
	public static JsonElement read(File file, boolean b){
		return read(file, b, null);
	}
	public static JsonElement read(File file, boolean b, @Nullable JsonElement def){
		try{
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			FileReader fr = new FileReader(file);
			JsonElement obj = parser.parse(fr);
			fr.close();
			return obj;
		}
		catch (Exception e) {
			if(b){
				e.printStackTrace();
			}
			Fexcraft.info("File '" + file + "' not found or malformed.");
			return def;
		}
	}
	
	/**
	 * @param file file the file to be parsed into json
	 * @return JsonObject from file, or new JsonObject if there are errors in previous methods
	 */
	public static JsonObject get(File file){
		JsonElement e = read(file, false);
		if(e == null || e.isJsonObject() == false){
			return new JsonObject();
		}
		else return e.getAsJsonObject();
	}
	
	/**
	 * Writes a JsonObject into the speficied file (Gson Pretty Printing Configuration)
	 * @param file target file
	 * @param obj JsonElement to be written into the file
	 */
	public static void write(File file, JsonElement obj, boolean check){
		try{
			if(check){
				if(!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
			}
			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(obj));
			fw.flush();
			fw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void write(File file, JsonElement obj){
		write(file, obj, false);
	}
	
	/**
	 * @param file File to be updated
	 * @param string Target value/object
	 * @param value
	 */
	public static void update(File file, String string, String value){
		JsonObject obj = get(file);
		obj.addProperty(string, value);
		write(file, obj);
	}
	
	/**
	 * @param file File to be updated
	 * @param string Target value/object
	 * @param value
	 */
	public static void update(File file, String string, boolean value){
		JsonObject obj = get(file);
		obj.addProperty(string, value);
		write(file, obj);
	}
	
	/**
	 * @param file File to be updated
	 * @param string Target value/object
	 * @param value
	 */
	public static void update(File file, String string, Number value){
		JsonObject obj = get(file);
		obj.addProperty(string, value);
		write(file, obj);
	}
	
	/**
	 * @param file File to be updated
	 * @param string Target value/object
	 */
	public static void update(File file, String string, JsonElement element){
		JsonObject obj = get(file);
		obj.add(string, element);
		write(file, obj);
	}
	
	/**
	 * Gets a value from a JsonObject if it exists, else returns specified default value and adds it to the JsonObject
	 * @param obj
	 * @param target
	 * @param default_value
	 */
	public static String getIfExists(JsonObject obj, String target, Object default_value){
		if(!obj.has(target)){
			obj.addProperty(target, default_value.toString());
			return default_value.toString();
		}
		return obj.get(target).getAsString();
	}
	
	/**
	 * Gets a value from a JsonObject if it exists, else returns specified default value and adds it to the JsonObject
	 * @param obj
	 * @param target
	 * @param default_value
	 */
	public static Number getIfExists(JsonObject obj, String target, Number default_value){
		if(!obj.has(target)){
			obj.addProperty(target, default_value);
			return default_value;
		}
		return obj.get(target).getAsNumber();
	}
	
	/**
	 * Gets a value from a JsonObject if it exists, else returns specified default value and adds it to the JsonObject
	 * @param obj
	 * @param target
	 * @param default_value
	 */
	public static boolean getIfExists(JsonObject obj, String target, boolean default_value){
		if(!obj.has(target)){
			obj.addProperty(target, default_value);
			return default_value;
		}
		return obj.get(target).getAsBoolean();
	}
	
	public static boolean getIfExists(JsonObject obj, String[] targets, boolean default_value){
		for(String str : targets){
			if(obj.has(str)){
				return obj.get(str).getAsBoolean();
			}
		}
		obj.addProperty(targets[0], default_value);
		return default_value;
	}
	
	/**
	 * Gets a JsonElement from a JsonObject if it exists, else returns specified default JsonElement and adds it to the JsonObject
	 * @param obj
	 * @param target
	 * @param default_value
	 */
	public static JsonElement getIfExists(JsonObject obj, String target, JsonElement default_value){
		if(!obj.has(target)){
			obj.add(target, default_value);
			return default_value;
		}
		return obj.get(target);
	}
	
	/**
	 * Gets a value from a JsonObject if it exists, else returns null
	 * @param obj
	 * @param target
	 */
	public static String getStringIfExists(JsonObject obj, String target){
		if(obj.has(target)){
			return obj.get(target).getAsString();
		}
		return null;
	}
	
	/**
	 * Gets a value from a JsonObject if it exists, else returns -1
	 * @param obj
	 * @param target
	 */
	public static Number getNumberIfExists(JsonObject obj, String target){
		if(obj.has(target)){
			return obj.get(target).getAsNumber();
		}
		return -1;
	}
	
	/**
	 * Gets a value from a JsonObject if it exists, else returns false
	 * @param obj
	 * @param target
	 */
	public static boolean getBooleanIfExists(JsonObject obj, String target){
		if(obj.has(target)){
			return obj.get(target).getAsBoolean();
		}
		return false;
	}
	
	/**
	 * Gets a JsonElement from a JsonObject if it exists, else returns null
	 * @param obj
	 * @param target
	 */
	public static JsonElement getIfExists(JsonObject obj, String target){
		if(!obj.has(target)){
			return obj.get(target);
		}
		return null;
	}
	
	public static JsonElement getElementIfExists(JsonObject obj, String target, boolean b){
		JsonElement elm = getIfExists(obj, target);
		if(elm == null && b){
			return new JsonObject();
		}
		return elm;
	}
	
	////>>>> ARRAYS >>>>////
	
	/**
	 * Gets a sub-array from an Object in a File, returns new JsonArray if there are errors in previous methods or doesn't exists.
	 * @param file
	 * @param string
	 * @return
	 */
	public static JsonArray getArray(File file, String string){
		JsonObject obj = get(file);
		if(obj.get(string) != null){
			JsonElement el = obj.get(string);
			if(el.isJsonArray() == true){
				return obj.get(string).getAsJsonArray();
			}
			else{
				return new JsonArray();
			}
		}
		else{
			return new JsonArray();
		}
	}
	
	
	public static boolean contains(JsonArray array, String string){
		boolean result = false;
		for(JsonElement e : array){
			try{
				if(e.getAsString().equals(string)){
					result = true;
				}
			}
			catch(ClassCastException ex){
				ex.printStackTrace();
				result = false;
				continue;
			}
		}
		return result;
	}
	
	/**
	 * @param file File to be updated
	 * @param string Target value/object
	 * @param value
	 */
	public static void update(File file, String string, JsonArray value){
		JsonObject obj = get(file);
		obj.add(string, value);
		write(file, obj);
	}
	
	/**
	 * Checks if JsonArray contains String.
	 * @param array
	 * @return
	 */
	public static boolean contains(JsonArray array, Number number){
		boolean result = false;
		for(JsonElement e : array){
			try{
				if(e.getAsNumber() == number){
					result = true;
				}
			}
			catch(ClassCastException ex){
				ex.printStackTrace();
				result = false;
				continue;
			}
		}
		return result;
	}
	
	public static ArrayList<String> jsonArrayToStringArray(JsonArray array){
		ArrayList<String> list = new ArrayList<String>();
		if(array == null){
			return list;
		}
		for(JsonElement e : array){
			try{
				if(e.isJsonPrimitive()){
					String str = e.getAsString();
					list.add(str);
				}
			}
			catch(ClassCastException ex){
				//ex.printStackTrace();
				continue;
			}
		}
		return list;
	}
	
	public static ArrayList<UUID> jsonArrayToUUIDArray(JsonArray array){
		ArrayList<UUID> list = new ArrayList<UUID>();
		ArrayList<String> json = jsonArrayToStringArray(array);
		for(String string : json){
			try{
				list.add(UUID.fromString(string));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return list;
	}

	public static ArrayList<Class<?>> jsonArrayToClassArray(JsonArray array){
		ArrayList<Class<?>> list = new ArrayList<Class<?>>();
		ArrayList<String> json = jsonArrayToStringArray(array);
		for(String string : json){
			try{
				list.add(Class.forName(string.replace(".class", "")));
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public static ArrayList<Integer> jsonArrayToIntegerArray(JsonArray array){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(JsonElement e : array){
			try{
				if(e.isJsonPrimitive()){
					Integer num = e.getAsInt();
					list.add(num);
				}
			}
			catch(ClassCastException ex){
				//ex.printStackTrace();
				continue;
			}
		}
		return list;
	}
	
	public static ArrayList<Long> jsonArrayToLongArray(JsonArray array){
		ArrayList<Long> list = new ArrayList<Long>();
		for(JsonElement e : array){
			try{
				if(e.isJsonPrimitive()){
					list.add(e.getAsLong());
				}
			}
			catch(ClassCastException ex){
				//ex.printStackTrace();
				continue;
			}
		}
		return list;
	}
	
	public static void add(JsonArray array, String string){
		array.add(new JsonPrimitive(string));
	}
	
	public static void add(JsonArray array, Number number){
		array.add(new JsonPrimitive(number));
	}
	
	public static JsonArray getArrayFromStringList(ArrayList<String> array){
		JsonArray ja = new JsonArray();
		for(String s : array){
			add(ja, s);
		}
		return ja;
	}
	
	public static JsonElement getArrayFromUUIDList(ArrayList<UUID> array){
		JsonArray ja = new JsonArray();
		for(UUID s : array){
			add(ja, s.toString());
		}
		return ja;
	}

	public static JsonElement getArrayFromObjectList(ArrayList<?> array){
		JsonArray ja = new JsonArray();
		for(Object obj : array){
			add(ja, obj.toString());
		}
		return ja;
	}
	
	public static JsonArray getArrayFromIntegerList(ArrayList<Integer> array){
		JsonArray ja = new JsonArray();
		for(int i : array){
			add(ja, i);
		}
		return ja;
	}

	public static boolean isJsonFile(File file){
		try{
			FileReader fr = new FileReader(file);
			parser.parse(fr);
			fr.close();
			return true;
		}
		catch(IOException e){
			return false;
		}
	}

	public static JsonUtil getInstance(){
		return instance;
	}
	
	/**
	 * Transforms the InputStream into a String and then parses it into a JsonObject;
	 * <BR>Does also close the InputStream.
	 * @param stream
	 * @return
	 */
	public static JsonObject getObjectFromInputStream(InputStream stream){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String input;
			StringBuffer response = new StringBuffer();
			while ((input = in.readLine()) != null) {
				response.append(input);
			}
			in.close();
			return JsonUtil.getFromString(response.toString()).getAsJsonObject();	
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static JsonElement getElementFromInputStream(InputStream stream){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String input;
			StringBuffer response = new StringBuffer();
			while ((input = in.readLine()) != null) {
				response.append(input);
			}
			in.close();
			return JsonUtil.getFromString(response.toString());	
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String setPrettyPrinting(JsonObject obj){
		return gson.toJson(obj);
	}

	public static MapObject toMapObject(JsonObject json){
		MapObject obj = new MapObject();
		for(Entry<String, JsonElement> elm : json.entrySet()){
			if(elm.getValue().isJsonArray()){
				obj.put(elm.getKey(), toArray(elm.getValue().getAsJsonArray()));
			}
			else if(elm.getValue().isJsonObject()){
				obj.put(elm.getKey(), toMapObject(elm.getValue().getAsJsonObject()));
			}
			else if(elm.getValue().isJsonPrimitive()){
				obj.put(elm.getKey(), toPrimitive(elm.getValue().getAsJsonPrimitive()));
			}
			else if(elm.getValue().isJsonNull()){
				obj.with(elm.getKey(), "null");
			}
			else continue;
		}
		return obj;
	}

	private static List<Object> toArray(JsonArray array){
		ArrayList<Object> obj = new ArrayList<>();
		for(JsonElement elm : array){
			if(elm.isJsonArray()){
				obj.add(toArray(elm.getAsJsonArray()));
			}
			else if(elm.isJsonObject()){
				obj.add(toMapObject(elm.getAsJsonObject()));
			}
			else if(elm.isJsonPrimitive()){
				obj.add(toPrimitive(elm.getAsJsonPrimitive()));
			}
			else if(elm.isJsonNull()){
				obj.add("null");
			}
			else continue;
		}
		return obj;
	}

	private static Object toPrimitive(JsonPrimitive json){
		if(json.isBoolean()){
			return json.getAsBoolean();
		}
		if(json.isString()){
			return json.getAsString();
		}
		if(json.isNumber()){
			return json.getAsNumber();
		}
		return json.getAsString();
	}

	@SuppressWarnings("deprecation")
	public static JsonObject fromMapObject(HashMap<String, Object> map){
		JsonObject obj = new JsonObject();
		if(map == null){ return obj; }
		for(Entry<String, Object> entry : map.entrySet()){
			if(entry.getValue() instanceof String){
				obj.addProperty(entry.getKey(), (String)entry.getValue());
			}
			else if(entry.getValue() instanceof Number){
				obj.addProperty(entry.getKey(), (Number)entry.getValue());
			}
			else if(entry.getValue() instanceof Boolean){
				obj.addProperty(entry.getKey(), (Boolean)entry.getValue());
			}
			else if(entry.getValue() == null){
				obj.add(entry.getKey(), new JsonNull());
			}
			else if(entry.getValue() instanceof List){
				obj.add(entry.getKey(), fromList((List<Object>)entry.getValue()));
			}
			else if(entry.getValue() instanceof Map){
				obj.add(entry.getKey(), fromMapObject((HashMap<String, Object>)entry.getValue()));
			}
		}
		return obj;
	}

	@SuppressWarnings("deprecation")
	public static JsonArray fromList(List<Object> list){
		JsonArray array = new JsonArray();
		for(Object obj : list){
			if(obj instanceof String){
				array.add((String)obj);
			}
			else if(obj instanceof Number){
				array.add((Number)obj);
			}
			else if(obj instanceof Boolean){
				array.add((Boolean)obj);
			}
			else if(obj == null){
				array.add(new JsonNull());
			}
			else if(obj instanceof List){
				array.add(fromList((List<Object>)obj));
			}
			else if(obj instanceof Map){
				array.add(fromMapObject((HashMap<String, Object>)obj));
			}
		}
		return array;
	}

	public static JsonObject fromMapSubObject(Object obj, String data){
		return fromMapObject((HashMap<String, Object>)((HashMap<String, Object>)obj).get(data));
	}

	public static JsonArray fromCursor(Object run){
		Cursor<?> cursor = (Cursor<?>)run;
		JsonArray array = new JsonArray();
		for(Object obj : cursor){
			if(obj instanceof HashMap){
				array.add(fromMapObject((HashMap<String, Object>)obj));
			}
			else if(obj instanceof List){
				array.add(fromList((List<Object>)obj));
			}
			else if(obj instanceof String){
				array.add((String)obj);
			}
			else if(obj instanceof Number){
				array.add((Number)obj);
			}
			else if(obj instanceof Boolean){
				array.add((Boolean)obj);
			}
			else{
				array.add(obj.toString());
			}
		}
		return array;
	}

	public static void reply(HttpServletResponse response, JsonObject reply) throws IOException {
		response.getWriter().append(reply.toString());
	}
	
}