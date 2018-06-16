package net.fexcraft.web.discord.fbot;

import java.awt.Color;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.fexcraft.web.discord.fbot.Command;
import net.fexcraft.web.util.JsonUtil;
import net.fexcraft.web.util.RTDB;

public class FclCmd implements Command {

	@Override
	public void execute(MessageReceivedEvent event, MainListener.Server serv, String content, String[] args){
		if(args.length < 2){
			//
		}
		else{
			switch(args[1]){
				case "mv":
					JsonObject obj = JsonUtil.fromMapSubObject(RTDB.get().table("mc_fcl_json").get(args[2]).run(RTDB.conn()), "data");
					if(obj != null && obj.has("versions")){
						JsonArray array = obj.get("versions").getAsJsonArray();
						EmbedBuilder build = getBuilder(event, "FCL RQ REPLY", Color.CYAN);
						build.addField("Download", "https://fexcraft.net/download?modid=" + args[2].toLowerCase(), false);
						for(JsonElement elm : array){
							JsonObject jsn = elm.getAsJsonObject();
							build.addField("Minecraft - " + jsn.get("version").getAsString(), args[2].toUpperCase() + " - " + jsn.get("latest_version").getAsString(), false);
						}
						send(event, build);
					}
					else{
						send(event, "Modid or data not found.");
					}
					break;
			}
		}
	}

}
