package net.fexcraft.web.discord.fbot;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.fexcraft.web.discord.fbot.Command;

public class BotCmd implements Command {

	@Override
	public void execute(MessageReceivedEvent event, MainListener.Server serv, String content, String[] args){
		if(args.length < 2){
			EmbedBuilder builder = getBuilder(event, "Fexcraft (Bot) Main Command", Color.BLUE);
			String str = "";
			str += "- server\n";
			str += "- myself\n";
			str += "- set help\n";
			str += "- set <option> <value...>\n";
			str += "- channels add <name>\n";
			str += "- channels rem <name>\n";
			str += "- channels list\n";
			str += "- channels help\n";
			str += "- send-join-test\n";
			builder.addField("Arguments", str, false);
			send(event, builder);
			return;
		}
		switch(args[1]){
			case "server":{
				EmbedBuilder builder = getBuilder(event, "Fexcraft (Bot) Main Command", Color.BLUE);
				Guild guild = event.getGuild();
				builder.addField("Name", guild.getName(), true);
				builder.addField("Def. Channel", guild.getDefaultChannel().getName(), true);
				builder.addField("ID", "`" + guild.getId() + "`", true);
				builder.addField("Owner", guild.getOwner().getUser().getName() + " (" + guild.getOwner().getNickname() + ")", true);
				builder.addBlankField(true);
				builder.addField("Members", guild.getMembers().size() + "", true);
				builder.addBlankField(false);
				builder.addField("Listeners (fbot channels)", serv.channels.size() + "", true);
				builder.addField("Tracks (in playlist)", "//TODO", true);
				builder.addField("Prefix", "`" + serv.prefix + "`", true);
				builder.addField("Entrance Hall", guild.getTextChannelById(serv.entrancehall).getName(), true);
				builder.addBlankField(true);
				builder.addField("Created", guild.getCreationTime().toString(), true);
				send(event, builder);
				break;
			}
			case "myself":{
				EmbedBuilder builder = getBuilder(event, "Fexcraft (Bot) Main Command", Color.BLUE);
				net.dv8tion.jda.core.entities.User usr = event.getAuthor();
				builder.addField("Name", usr.getName(), true);
				builder.addField("####", usr.getDiscriminator(), true);
				builder.addField("ID", "`" + usr.getId() + "`", true);
				builder.addBlankField(false);
				builder.addField("Linked (to Fexcraft.Net account)", "Feature Currently Disabled.", true);
				builder.addField("Registered", usr.getCreationTime().toString(), true);
				send(event, builder);
				break;
			}
			case "set":{
				if(args.length < 4 || (args.length >= 3 && args[2].equals("help"))){
					EmbedBuilder builder = getBuilder(event, "Settings Help", Color.GREEN);
					String str = "";
					str += "- set welcome_msg <message>\n";
					str += "   e.g. `:fbot set welcome_msg Welcome to our Server $username!`\n\n";
					str += "- set leave_msg <message>\n";
					str += "   e.g. `:fbot set leave_msg Sadly, $username left our Discord!`\n\n";
					str += "- set entrancehall <channel>\n";
					str += "   e.g. `:fbot set entracehall #general`\n\n";
					str += "- set do-greet <true/false>\n\n";
					str += "- set prefix <prefix>\n";
					str += "   e.g. `:fbot set prefix !`\n\n";
					builder.addField("Arguments", str, false);
					send(event, builder);
				}
				else{
					switch(args[2]){
						case "welcome_msg": case "leave_msg": {
							String str = args[3];
							for(int i = 4; i < args.length; i++){
								str += " " + args[i];
							}
							if(!str.contains("$username")){
								sendError(event, "WARNING: missing `$username`!");
							}
							if(args[2].equals("welcome_msg")){
								serv.welcome_msg = str;
								serv.save();
							}
							else{
								serv.leave_msg = str;
								serv.save();
							}
							sendReply(event, "Success!");
							break;
						}
						case "entrancehall":{
							String str = args[3];
							TextChannel channel = event.getMessage().getMentionedChannels().get(0);
							if(channel.getName().equals(str.replace("#", ""))){
								serv.entrancehall = channel.getIdLong();
								serv.save();
								sendReply(event, "Success! (`" + channel.getId() + "`);");
								delete(event);
							}
							else{
								sendError(event, "MENTION != NAME\n" + str + " != " + channel.getName());
							}
							break;
						}
						case "do-greet": case "do_greet":{
							try{
								serv.welcome_new_members = !Boolean.parseBoolean(args[3]);
								serv.save();
								sendReply(event, "Greeting new users is now `" + (serv.welcome_new_members ? "disabled" : "enabled") + "`!");
								delete(event);
							}
							catch(Exception e){
								sendError(event, e);
							}
							break;
						}
						case "prefix":{
							if(!args[3].equals(" ") && !args[3].equals("")){
								serv.prefix = args[3];
								serv.save();
								sendReply(event, "New prefix is now: `" + serv.prefix + "`");
								delete(event);
							}
						}
					}
				}
				break;
			}
			case "channels":{
				if(args.length < 3 || args[2].equals("help")){
					EmbedBuilder builder = getBuilder(event, "Channels Help", Color.GREEN);
					String str = "";
					str += "- channels add <channel>\n";
					str += "   e.g. `:fbot channels add #programming`\n\n";
					str += "- channels rem <channel>\n";
					str += "   e.g. `:fbot channels rem #off-topic`\n\n";
					str += "- channels list\n";
					builder.addField("Arguments", str, false);
					send(event, builder);
					return;
				}
				switch(args[2]){
					case "add": case "rem": {
						if(args.length < 4){
							sendError(event, "Missing Channel Argument.");
							return;
						}
						String str = args[3];
						TextChannel channel = event.getMessage().getMentionedChannels().get(0);
						if(channel.getName().equals(str.replace("#", ""))){
							if(args[2].equals("add")){
								serv.channels.add(channel.getIdLong());
								sendReply(event, "Success! (`" + channel.getId() + "`);");
							}
							else{
								serv.channels.remove(channel.getIdLong());
								sendReply(event, "Success! (`" + channel.getId() + "`);");
							}
							serv.save();
							delete(event);
						}
						else{
							sendError(event, "MENTION != NAME\n" + str + " != " + channel.getName());
						}
						break;
					}
					case "list":{
						EmbedBuilder builder = getBuilder(event, "Listened Channels on this Server", Color.GREEN);
						String str = serv.channels.size() > 0 ? "" : "No channels listened to, besides #fbot.";
						for(long st : serv.channels){
							str += "- " + event.getGuild().getTextChannelById(st).getName() + " (" + st + ");\n";
						}
						builder.addField("Channels", str, false);
						send(event, builder);
						delete(event);
						return;
					}
				}
				break;
			}
			case "send-join-test":{
				event.getTextChannel().sendMessage(serv.getWelcomeMessage(event.getMember().getEffectiveName())).queue();
				event.getTextChannel().sendMessage(serv.getLeaveMessage(event.getMember().getEffectiveName())).queue();
				break;
			}
		}
	}

}
