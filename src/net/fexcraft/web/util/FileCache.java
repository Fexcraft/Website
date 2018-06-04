package net.fexcraft.web.util;

import net.fexcraft.web.Fexcraft;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class FileCache {
	
	public static final TreeMap<String, String> HTML = new TreeMap<>();
	public static final TreeMap<String, FileObject> FILES = new TreeMap<>();
	
	public static class FileObject {
		
		private long last_use;
		private byte[] file;
		private String contenttype;
		
		private FileObject(){}
		
		private FileObject(byte[] bytes, String type){
			file = bytes; contenttype = type;
			//temporary-use object
		}

		public byte[] getFile(){
			return file;
		}
		
		public long lastUsed(){
			return last_use;
		}
		
		public void setLastUsed(long leng){
			last_use = leng;
		}
		
		public String getContentType(){
			return contenttype;
		}
		
	}
	
	public static class ScheduledClearing implements org.quartz.Job {

		@Override
		public void execute(JobExecutionContext arg) throws JobExecutionException{
			ArrayList<String> remove = new ArrayList<>();
			for(Entry<String, FileObject> file : FILES.entrySet()){
				if(file.getValue().lastUsed() + 600000 < new Date().getTime()){
					remove.add(file.getKey());
				}
			}
			remove.forEach(elm -> { FILES.remove(elm); });
			//
			long size = getFilesSize();
			if(size > 536870912){ //512MB
				Collection<Entry<String, FileObject>> coll = FILES.entrySet().stream().sorted(new Comparator<Entry<String, FileObject>>(){
					@Override
					public int compare(Entry<String, FileObject> o1, Entry<String, FileObject> o2){
						return -Integer.compare(o1.getValue().getFile().length, o2.getValue().getFile().length);
					}
				}).collect(Collectors.toList());
				long limit = 268435456; //256MB
				long removed = 0;
				for(Entry<String, FileObject> obj : coll){
					if(removed < limit){
						FILES.remove(obj.getKey());
						removed += obj.getValue().getFile().length;
						continue;
					}
					break;
				}
				return;
			}
			return;
		}
		
	}
	
	public static final long getFilesSize(){
		long size = 0;
		for(FileObject file : FILES.values()){
			size += file.getFile().length;
		}
		return size;
	}
	
	public static final String getResource(String id, @Nullable String type){
		if(HTML.containsKey(id)){
			return HTML.get(id);
		}
		InputStream stream = getResourceAsStream(type == null ? "html" : type, id);
		if(stream != null){
			String str = toString(stream);
			if(!str.equals("") && !Fexcraft.dev()){ HTML.put(id, str); }
			return str;
		}
		else{
			return type != null && type.equals("json") ? "{'error':'resource not found','status':404}" : "Unable to fetch resource.";
		}
	}

	private static final InputStream getResourceAsStream(String type, String id){
		try{
			return new FileInputStream(new File("./resources/" + type + "/" + id + "." + type));
		}
		catch(FileNotFoundException e){
			//e.printStackTrace();
			return null;//new ByteArrayInputStream("Requested resource not found.".getBytes(StandardCharsets.UTF_8));
		}
	}
	
	private static final String toString(InputStream stream){
		try{
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		}
		catch(IOException e){
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public static final @Nullable FileObject getFile(String adress){
		if(FILES.containsKey(adress)){
			return FILES.get(adress);
		}
		try{
			File file = new File(new String((Fexcraft.INSTANCE.getProperty("files_location", "/var/www/html/").getAsString() + adress.substring(adress.indexOf("/files"))).getBytes("UTF-8")));
			if(file.exists() && !file.isDirectory()){
				if(file.length() > 10485760 || getFilesSize() > 536870912){ //10MB //512MB
					return new FileObject(FileUtils.readFileToByteArray(file), Files.probeContentType(file.toPath()));
				}
				FileObject obj = new FileObject();
				obj.contenttype = Files.probeContentType(file.toPath());
				obj.last_use = Instant.now().toEpochMilli();
				obj.file = FileUtils.readFileToByteArray(file);
				if(obj.file == null){
					return null;
				}
				else{
					FILES.put(adress, obj);
					return obj;
				}
			}
			return null;
		}
		catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}

	public static Document newDocument(UserObject user, String title){
		Document doc = Jsoup.parse(getResource("root", "html"));
		doc.head().append("<title>" + title + " - Fexcraft Network</title>");
		if(user != null){
			if(!user.isGuest()){
				doc.body().getElementById("top_right").html("<a href=\"/settings\">Settings</a><br><a href=\"/session?rq=logout\">Logout</a>");
			}
			else{
				doc.body().getElementById("top_right").html("<a href=\"/session/register\">Register</a><br><a href=\"/session\">Login</a>");
			}
		}
		else{
			doc.body().getElementById("top_right").html("<a href=\"/register\">ERROR</a><br><a href=\"/session\">ERROR</a>");
		}
		doc.getElementById("footer").getAllElements().get(0).prepend(getResource("ads/ad3-wide", "html"));
		return doc;
	}

}
