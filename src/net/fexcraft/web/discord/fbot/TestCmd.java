package net.fexcraft.web.discord.fbot;

import java.util.Date;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.fexcraft.web.discord.fbot.Command;

public class TestCmd implements Command {

	@Override
	public void execute(MessageReceivedEvent event, MainListener.Server serv, String content, String[] args) {
		send(event, "Test. `" + Command.getTime(new Date().getTime()) + "`");
	}

}
