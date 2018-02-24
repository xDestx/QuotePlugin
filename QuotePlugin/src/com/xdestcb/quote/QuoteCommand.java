package com.xdestcb.quote;

import com.mdc.combot.ComBot;
import com.mdc.combot.command.Command;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class QuoteCommand implements Command {

	private QuotePlugin pl;
	
	public QuoteCommand(QuotePlugin qp) {
		pl = qp;
	}
	
	@Override
	public void called(ComBot bot, MessageReceivedEvent e) {
		if(bot.memberHasPerm("quote.create", e.getMember())) {
			String cmd = e.getMessage().getContentRaw();
			cmd = cmd.replace(bot.getCommandPrefix(e.getGuild()) + getLabel() + " ", "");
			String arg0 = cmd.split(" ")[0];
			String arg1 = cmd.split(" ")[1];
			String userTagged = arg0.replace("!","").replace("<","").replace(">", "").replace("@","");
			String channelTagged = arg1.replace("!","").replace("<","").replace(">", "").replace("#","");
			if(!arg1.startsWith("<#")) {
				//No channel specified
				channelTagged = e.getTextChannel().getId();
			}
			long userId;
			long channelId;
			try {
				userId = Long.parseLong(userTagged);
				channelId = Long.parseLong(channelTagged);
			} catch (NumberFormatException err) {
				err.printStackTrace();
				e.getChannel().sendMessage("You broke something").queue();
				return;
			}
			User usr = bot.getJDA().getUserById(userId);
			TextChannel ch = bot.getJDA().getTextChannelById(channelId);
			if(usr == null) {
				e.getChannel().sendMessage("Who's that...?").queue();
			} else if (ch == null) { 
				e.getChannel().sendMessage("Nice try punk, but that channel doesn't exist").queue();
			} else {
				String quotedMessage = cmd.replace(arg0, "").replace(arg1, "").trim();
				//Find if it exists
				QuoteCreator qc = new QuoteCreator(quotedMessage, usr,ch, e.getTextChannel(), bot.getJDA(), pl.getLookbackTime(), pl.getTimeZone());
				Thread searchThread = new Thread(qc);
				searchThread.start();
			}
		}
	}

	@Override
	public String getLabel() {
		return "quote";
	}

	
	/*
	 * What does a successful quote l~ook like?
	 * 
	 * ~quote @user #channel "A message they have typed"
	 * 
	 * Bot:
	 * Quote from @user at (time)
	 * (Message portion quoted)
	 * (Reactions)?
	 */

	
}
