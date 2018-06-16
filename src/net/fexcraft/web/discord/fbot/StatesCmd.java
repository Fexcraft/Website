package net.fexcraft.web.discord.fbot;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.fexcraft.web.discord.fbot.Command;

public class StatesCmd implements Command {

	@Override
	public void execute(MessageReceivedEvent event, MainListener.Server serv, String content, String[] args){
		if(args.length < 2){
			EmbedBuilder builder = getBuilder(event, "States Command", Color.BLUE);
			String str = "";
			str += "- data\n";
			str += "- set adress <adress>\n";
			str += "- set token <token/key> (same as in server config)\n";
			str += "- set channel <#channel>\n";
			str += "- set port <port>\n";
			builder.addField("Arguments", str, false);
			send(event, builder);
			return;
		}
		switch(args[1]){
			case "data":{
				EmbedBuilder builder = getBuilder(event, "States Command", Color.BLUE);
				Guild guild = event.getGuild();
				builder.addField("Adress", serv.states_adress + "", true);
				builder.addField("Port", serv.states_port + "", true);
				builder.addField("Token", serv.states_token == null || serv.states_token.equals("") ? "missing" : "exists", true);
				builder.addField("Channel", "`" + (serv.states_channel > 0 ? guild.getTextChannelById(serv.states_channel) : "none") + "`", true);
				send(event, builder);
				break;
			}
			case "set":{
				if(args.length < 4 || (args.length >= 3 && args[2].equals("help"))){
					EmbedBuilder builder = getBuilder(event, "States Command", Color.BLUE);
					String str = "";
					str += "- set adress <adress>\n";
					str += "- set token <token/key> (same as in server config)\n";
					str += "- set channel <#channel>\n";
					builder.addField("Arguments", str, false);
					send(event, builder);
				}
				else{
					switch(args[2]){
						case "adress":{
							serv.states_adress = args[3];
							serv.save();
							sendReply(event, "Set adress to: " + serv.states_adress);
							break;
						}
						case "token":{
							if(!args[3].equals(" ") && !args[3].equals("")){
								serv.states_token = args[3];
								serv.save();
								sendReply(event, "Keyword updated.");
								delete(event);
							}
						}
						case "channel":{
							String str = args[3];
							TextChannel channel = event.getMessage().getMentionedChannels().get(0);
							if(channel.getName().equals(str.replace("#", ""))){
								serv.states_channel = channel.getIdLong();
								serv.save();
								sendReply(event, "Success! (`" + channel.getIdLong() + "`);");
								delete(event);
							}
							else{
								sendError(event, "MENTION != NAME\n" + str + " != " + channel.getName());
							}
							break;
						}
						case "port":{
							try{
								serv.states_port = Integer.parseInt(args[3]);
								serv.save();
								sendReply(event, "Set port to: " + serv.states_port);
							}
							catch(Exception e){
								sendError(event, e);
							}
							break;
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
				//event.getTextChannel().sendMessage(serv.getWelcomeMessage(event.getMember().getEffectiveName())).queue();
				//event.getTextChannel().sendMessage(serv.getLeaveMessage(event.getMember().getEffectiveName())).queue();
				break;
			}
		}
	}

}
