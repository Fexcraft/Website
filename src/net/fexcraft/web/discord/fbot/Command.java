package net.fexcraft.web.discord.fbot;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface Command {

	public void execute(MessageReceivedEvent event, MainListener.Server serv, String content, String[] args);
	
	public default EmbedBuilder getBuilder(MessageReceivedEvent event, String title, Color color){
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title, "http://fexcraft.net/discord/fbot/home");
		builder.setColor(color);
		builder.setFooter("requested by " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
		return builder;
	}
	
	public default void send(MessageReceivedEvent event, String msg){
		event.getTextChannel().sendMessage(msg).queue();
	}
	
	public default void send(MessageReceivedEvent event, EmbedBuilder msg){
		event.getTextChannel().sendMessage(msg.build()).queue();
	}
	
	public default void delete(MessageReceivedEvent event){
		event.getMessage().delete().queue();
	}
	
	public default void sendError(MessageReceivedEvent event, String msg){
		event.getTextChannel().sendMessage("ERROR: " + msg).queue(con -> con.delete().completeAfter(8, TimeUnit.SECONDS));
	}
	
	public default void sendError(MessageReceivedEvent event, Exception e){
		EmbedBuilder builder = getBuilder(event, "An Error occured.", Color.BLACK);
		builder.addField("Error", e.getMessage(), true);
		event.getTextChannel().sendMessage(builder.build()).queue(con -> con.delete().completeAfter(12, TimeUnit.SECONDS));
	}
	
	public default void sendReply(MessageReceivedEvent event, String msg){
		event.getTextChannel().sendMessage("RESPONSE: " + msg).queue(con -> con.delete().completeAfter(10, TimeUnit.SECONDS));
	}
	
	public static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy HH:mm:ss z");
	
	public static String getTime(long leng){
		return format.format(leng);
	}

	public static String getTime(){
		return format.format(new Date().getTime());
	}

}
