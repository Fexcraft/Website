package net.fexcraft.web.files;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.FileCache;
import net.fexcraft.web.util.FileCache.FileObject;
 
/**
 * ported from older webserver version
 */
public class MainFileServer extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

    public MainFileServer(){
        super();
    }
    
    private static final String[] array = new String[]{"localhost", "fexcraft.net", "adfoc.us", "adf.ly"};//TODO replace with load-able from config values
    private static final String[] rsi = new String[]{"/web/", "/launcher/", "/TXT/", "/Modpack/", "/not-my-mods/"};
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    	return;
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getRequestURL().toString();
		String ref = request.getHeader("referer");
		boolean valid = false;
		for(String s : rsi){ if(url.contains(s)){ valid = true; break; } }
		if(ref != null && !ref.equals("")){
			ref = ref.toLowerCase();
			for(String s : array){
				if(ref == null){ break; }
				if(ref.contains(s)){ valid = true; break; }
			}
		}
		if(valid || Fexcraft.dev()){
			try{
				FileObject obj = FileCache.getFile(url);
				if(obj == null){
		        	response.sendError(500, "File not found.\n    " + url.toString());
					return;
				}
		        response.setContentType("application/zip");
		        response.getOutputStream().write(obj.getFile());
		        return;
			}
			catch(IOException e){
	        	response.sendError(500, "Error while trying to fetch file.\n    " + url.toString() + "");
		        if(Fexcraft.dev()){
		        	e.printStackTrace();
		        }
			}
		}
		else{
			response.sendError(403, "Invalid request.\n"
					+ "    Make sure you are using a valid redirect link.\n"
					+ "    If you think this is an error, please report on our forums or discord server! (" + Fexcraft.INSTANCE.getProperty("discord_invite", "https://discord.gg/rMXcrsv").getAsString() + ")\n\n"
					+ "    If you are here to repost files... contact us! I'm sure you can use OUR redirect links, and still make profit on your website!");
		}
	}

}
