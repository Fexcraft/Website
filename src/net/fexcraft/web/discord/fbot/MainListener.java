package net.fexcraft.web.discord.fbot;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rethinkdb.model.MapObject;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.fexcraft.web.Fexcraft;
import net.fexcraft.web.util.RTDB;

public class MainListener extends ListenerAdapter {
	
	private static final TreeMap<Long, Server> SERVERS = new TreeMap<Long, Server>();
	private static final TreeMap<String, Command> COMMANDS = new TreeMap<>();
	
	public MainListener(){
		COMMANDS.put("states", new StatesCmd());
		COMMANDS.put("fbot", new BotCmd());
		COMMANDS.put("fcl", new FclCmd());
		COMMANDS.put("test", new TestCmd());
	}
	
	@Override
	public void onReady(ReadyEvent event){
    	event.getJDA().getPresence().setGame(Game.of("|help"));
	}
	
	@Override
    public void onMessageReceived(MessageReceivedEvent event){
    	switch(event.getChannelType()){
			case GROUP:
	            System.out.printf("[%s][%s] %s: %s\ngg", event.getGuild().getName(), event.getTextChannel().getName(), event.getMember().getEffectiveName(), event.getMessage().getContent());
				break;
			case PRIVATE:
				System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(),event.getMessage().getContent());
				break;
			case TEXT:
	            if(event.getAuthor().isBot()){
					//processBotTextMessage(event);
				}
				else{
					System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(), event.getTextChannel().getName(), event.getMember().getUser().getName(), event.getMessage().getContent());
					processTextMessage(event);
				}
				break;
			case UNKNOWN:
			case VOICE:
			default:
				break;
    	}
    }
	
	public static class Server {
		
		public List<Long> channels = new ArrayList<>();
		private long id; public long states_channel, entrancehall;
		public String prefix, states_adress, states_token, welcome_msg, leave_msg;
		public int states_port;
		public boolean welcome_new_members;

		public Server(long leng){
			this.id = leng;
			refresh();
		}
		
		@SuppressWarnings("unchecked")
		private void refresh(){
			HashMap<String, Object> map = RTDB.get().table("discord_fbot").get(id + "").run(RTDB.conn());
			if(map == null){ map = new HashMap<>(); }
			channels = map.containsKey("channels") ? (List<Long>)map.get("channels") : null;
			prefix = map.containsKey("prefix") ? (String)map.get("prefix") : "|";
			states_channel = map.containsKey("states_channel") ? Long.parseLong((String)map.get("states_channel")) : 0l;
			states_adress = map.containsKey("states_adress") ? (String)map.get("states_adress") : "example.com";
			states_token = map.containsKey("states_token") ? (String)map.get("states_token") : "null";
			states_port = map.containsKey("states_port") ? Integer.parseInt((String)map.get("states_port")) : 0;
			welcome_new_members = map.containsKey("welcome_new_members") ? (boolean)map.get("welcome_new_members") : false;
			welcome_msg = map.containsKey("welcome_message") ? (String)map.get("welcome_message") : "Welcome $username!";
			leave_msg = map.containsKey("leave_message") ? (String)map.get("leave_message") : "$username left the Discord Server!";
			entrancehall = map.containsKey("entrancehall") ? Long.parseLong((String)map.get("entrancehall")) : 0l;
		}

		public void save(){
			MapObject object = new MapObject();
			object.with("prefix", prefix);
			object.with("states_channel", states_channel + "");
			object.with("states_adress", states_adress);
			object.with("states_token", states_token);
			object.with("states_port", states_port + "");
			object.with("welcome_new_members", welcome_new_members);
			object.with("welcome_message", welcome_msg);
			object.with("leave_message", leave_msg);
			object.with("entrancehall", entrancehall + "");
			//
			if((Long)RTDB.table("discord_fbot").filter(RTDB.get().hashMap("id", id + "")).count().run(RTDB.conn()) > 0){
				RTDB.table("discord_fbot").update(object).run(RTDB.conn());
			}
			else{
				RTDB.table("discord_fbot").insert(object.with("id", id + "")).run(RTDB.conn());
			}
			//
			refresh();
		}

		public String getWelcomeMessage(String name){
			return welcome_msg.replace("$username", name);
		}

		public String getLeaveMessage(String name){
			return leave_msg.replace("$username", name);
		}
		
	}

	private void processTextMessage(MessageReceivedEvent event){
		Server serv = SERVERS.get(event.getGuild().getIdLong());
		if(serv == null){
			SERVERS.put(event.getGuild().getIdLong(), serv = new Server(event.getGuild().getIdLong()));
		}
		Long channel = event.getTextChannel().getIdLong();
		if(serv.channels == null || serv.channels.contains(channel)){
			if(event.getMessage().getContent().startsWith(serv.prefix)){
				String[] args = event.getMessage().getContent().split(" ");
				Command command = COMMANDS.get(args[0].replace(serv.prefix, ""));
				if(command != null){
					command.execute(event, serv, event.getMessage().getContent(), args);
				}
			}
		}
		if(channel == serv.states_channel){
			if(serv.states_adress == null || serv.states_adress.equals("") || serv.states_token == null || serv.states_token.equals("")){ return; }
			String content = event.getMessage().getContent();
			if(!content.equals("")){
				JsonObject obj = new JsonObject();
				obj.addProperty("username", event.getAuthor().getName());
				obj.addProperty("content", content);
				obj.addProperty("token", serv.states_token);
				send(event, serv, obj);
			}
			if(event.getMessage().getAttachments().size() > 0){
				for(int i = 0; i < event.getMessage().getAttachments().size(); i++){
					JsonArray array = new JsonArray();
					array.add(event.getMessage().getAttachments().get(i).isImage() ? "&9&l<IMAGE " + i + ">" : "&c&l<FILE " + i + ">");
					array.add(event.getMessage().getAttachments().get(i).getUrl());
					JsonObject obj = new JsonObject();
					obj.addProperty("username", event.getAuthor().getName());
					obj.add("content", array);
					obj.addProperty("token", serv.states_token);
					send(event, serv, obj);
				}
			}
		}
	}
	
	private void send(MessageReceivedEvent event, Server serv, JsonObject obj){
		try{
			Socket socket = new Socket(serv.states_adress, serv.states_port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(obj.toString());
			out.close();
			socket.close();
			return;
		}
		catch(Exception e){
			//e.printStackTrace();
		}
	}
	
	@Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){
		Server serv = SERVERS.get(event.getGuild().getIdLong());
		if(serv == null){
			SERVERS.put(event.getGuild().getIdLong(), serv = new Server(event.getGuild().getIdLong()));
		}
		if(serv.welcome_new_members){
			TextChannel channel = event.getGuild().getTextChannelById(serv.entrancehall);
			if(channel != null){
				channel.sendMessage(serv.getWelcomeMessage(event.getMember().getEffectiveName())).queue();
			}
		}
	}
	
	@Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event){
		Server serv = SERVERS.get(event.getGuild().getIdLong());
		if(serv == null){
			SERVERS.put(event.getGuild().getIdLong(), serv = new Server(event.getGuild().getIdLong()));
		}
		if(serv.welcome_new_members){
			TextChannel channel = event.getGuild().getTextChannelById(serv.entrancehall);
			if(channel != null){
				channel.sendMessage(serv.getLeaveMessage(event.getMember().getEffectiveName())).queue();
			}
		}
	}
	
	/*@Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
		Server serv = SERVERS.get(event.getGuild().getIdLong());
		if(serv == null){
			SERVERS.put(event.getGuild().getIdLong(), serv = new Server(event.getGuild().getIdLong()));
		}
		if(event.getChannelLeft().getIdLong() == serv.getPlayer().playerchannel){
			if(event.getChannelLeft().getMembers().size() <= 1){
				serv.getPlayer().stop();
			}
		}
	}
	
	@Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event){
		Server serv = SERVERS.get(event.getGuild().getIdLong());
		if(serv == null){
			SERVERS.put(event.getGuild().getIdLong(), serv = new Server(event.getGuild().getIdLong()));
		}
		if(event.getChannelLeft().getIdLong() == serv.getPlayer().playerchannel){
			if(event.getChannelLeft().getMembers().size() <= 1){
				serv.getPlayer().stop();
			}
		}
	}*/

	public static String getNameForUser(long server, long user){
		return getNameForUser(Fexcraft.INSTANCE.getJavaDiscordApplicationProgrammingInterface().getGuildById(server), user);
	}

	public static String getNameForUser(Guild guild, long user){
		if(guild == null){
			return "G#N#F";
		}
		Member member = guild.getMemberById(user);
		if(member == null){
			return "M#N#F";
		}
		return member.getEffectiveName();
	}

	public static net.dv8tion.jda.core.entities.User getUserById(Guild guild, long user){
		Member member = guild.getMemberById(user);
		return member == null ? null : member.getUser();
	}

}
