package com.xdestcb.quote;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class QuoteCreator implements Runnable {

	private User u;
	private TextChannel ch, out;
	private JDA jda;
	private String message;
	private ZoneId timeZone;
	private int maxSearchTime;
	
	public QuoteCreator(String message, User u, TextChannel source, TextChannel out, JDA jda, int maxSearchTime, ZoneId timeZone) {
		this.u = u;
		this.ch = source;
		this.out = out;
		this.jda = jda;
		this.maxSearchTime = maxSearchTime;
		this.message = message;
		this.timeZone = timeZone;
	}
	
	@Override
	public void run() {
		long maxSearchNs = (long) (maxSearchTime * 1e9);
		if(jda == null || u == null || ch == null) return;
		List<Message> pastMessages = ch.getIterableHistory().complete();
		long startTime = System.nanoTime();
		long endTime = startTime + maxSearchNs;
		long currentTime = startTime;
		
		Message quotedMessage = null;
		pastMessages.remove(0);
		for(Message msg : pastMessages) {
			if(currentTime > endTime) {
				quotedMessage = null;
				break;
			}
			if(msg.getContentRaw().contains(message) && msg.getAuthor().getId().equals(u.getId())) {
				quotedMessage = msg;
				break;
			}
			currentTime = System.nanoTime();
		}
		
		if(quotedMessage == null) {
			ch.sendMessage("Couldn't find the message to quote within the allowed time, my apologies").complete();
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.green);
			eb.setTitle("Quote from " + u.getName() + "#" + u.getDiscriminator() + " in #" + ch.getName() + " at " + formatDate(quotedMessage.getCreationTime()));
			String quotedStr = quotedMessage.getContentRaw();
			eb.setDescription(quotedStr.substring(0, quotedStr.indexOf(message)) + "**" + message + "**" + quotedStr.substring(quotedStr.indexOf(message)+message.length()));
			MessageEmbed newMsg = eb.build();
			out.sendMessage(newMsg).complete();
		}
	}
	
	private String formatDate(OffsetDateTime d) {
		String date = "MM/dd/yyyy hh:mm";
		ZonedDateTime dateTime = d.atZoneSameInstant(timeZone);
		date = date.replace("MM", dateTime.getMonthValue()+"").replace("dd", dateTime.getDayOfMonth()+"").replace("yyyy", dateTime.getYear()+"").replace("hh", (dateTime.getHour() < 10 ? "0"+dateTime.getHour():""+dateTime.getHour())).replace("mm", dateTime.getMinute() < 10 ? "0"+dateTime.getMinute():""+dateTime.getMinute());
		
		return date;
	}
	
}
