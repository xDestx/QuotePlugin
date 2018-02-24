package com.xdestcb.quote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.mdc.combot.ComBot;
import com.mdc.combot.plugin.BotPlugin;
import com.mdc.combot.util.Util;

public class QuotePlugin implements BotPlugin {

	private int lookbackDuration;
	private String timeZone;
	
	@Override
	public void enable() {
		System.out.println("Quote Plugin enabled");
		ComBot.getBot().registerCommand(new QuoteCommand(this));
		loadConfig();
		
	}
	
	private void loadConfig() {
		File configFile = new File(Util.BOT_PATH + File.separatorChar + "plugins" + File.separatorChar + "Quote");
		if(!configFile.exists()) {
			configFile.mkdirs();
		}
		configFile = new File(configFile.getPath() + File.separatorChar + "config.txt");
		if(configFile.exists()) {
			try {
				Scanner s = new Scanner(new FileInputStream(configFile));
				Map<String,String> configMap = new HashMap<String,String>();
				while(s.hasNextLine()) {
					String line = s.nextLine();
					if(line.trim().equals("")) {
						continue;
					} else if(line.trim().contains(":")) {
						String key = line.trim().split(":")[0].trim();
						String value = line.trim().split(":")[1].trim();
						configMap.put(key, value);
					}
				}
				if(configMap.containsKey("max-search-in-seconds") && configMap.containsKey("time-zone")) {
					try {
						lookbackDuration = Integer.parseInt(configMap.get("max-search-in-seconds"));
					} catch (NumberFormatException e) {
						//Sucks
						e.printStackTrace();
						System.out.println("Invalid search time.");
						lookbackDuration = 2;
					}
					this.timeZone = configMap.get("time-zone");
					try {
						ZoneId.of(timeZone);
					} catch (Exception e) {
						e.printStackTrace();
						timeZone = "UTC-5";
						System.out.println("Invalid time zone, using UTC-5");
					}
				} else {
					System.out.println("Invalid config file. Using default");
					lookbackDuration = 2;
					timeZone = "UTC-5";
				}
				s.close();
			} catch (FileNotFoundException e) {
			
				e.printStackTrace();
			}
			
		} else {
			try {
				configFile.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
				bw.write("time-zone:UTC-5\nmax-search-in-seconds:2");
				bw.flush();
				bw.close();
				this.timeZone = "UTC-5";
				this.lookbackDuration = 2;
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("Failed to create config file");
			}
		}
	}
	
	public int getLookbackTime() {
		return this.lookbackDuration;
	}
	
	public ZoneId getTimeZone() {
		return ZoneId.of(timeZone);
	}
	
	@Override
	public void disable() {
		System.out.println("Quote plugin disabled");
	}

	
	
}
