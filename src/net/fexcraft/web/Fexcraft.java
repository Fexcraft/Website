package net.fexcraft.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.Deflater;

import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.fexcraft.web.util.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rethinkdb.net.Cursor;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.fexcraft.web.discord.fbot.MainListener;
import net.fexcraft.web.files.MainFileServer;
import net.fexcraft.web.forum.ForumIndex;
import net.fexcraft.web.minecraft.fcl.AddDownload;
import net.fexcraft.web.minecraft.fcl.Request;
import net.fexcraft.web.minecraft.fcl.UpdateJson;
import net.fexcraft.web.slash.DatabaseViewer;
import net.fexcraft.web.slash.Download;
import net.fexcraft.web.slash.Index;
import net.fexcraft.web.slash.License;
import net.fexcraft.web.slash.Register;
import net.fexcraft.web.slash.Session;

public class Fexcraft extends Server {
	
	public static final Logger LOGGER = loadLogger("Main", "common");
	public static DefaultAudioPlayerManager AUDIOMANAGER;
    public static final Random RANDOM = new Random();
	public static Fexcraft INSTANCE;

	private static final Logger loadLogger(String name, String filename){
		LoggerContext context = (LoggerContext)LogManager.getContext(false);
		Configuration config = context.getConfiguration();
		Appender appender = RollingFileAppender.newBuilder()
		   	.setConfiguration(config)
		   	.withFileName("./logs/" + filename + ".log")
		   	.withFilePattern("./logs/" + filename + "-%d{yyyy-MM-dd}.%i.log.gz")
		   	.withName(name)
		   	.withAppend(true)
		   	.withImmediateFlush(true)
            .withBufferedIo(true)
            .withBufferSize(8192)
            .withCreateOnDemand(false)
		   	.withLocking(false)
		   	.withLayout(PatternLayout.newBuilder().withPattern("%d{(dd.MMM)[HH:mm:ss;SSS]} [%t/%p]: %m%n").withConfiguration(config).build())
		   	.withPolicy(CompositeTriggeringPolicy.createPolicy(SizeBasedTriggeringPolicy.createPolicy("256 M"), TimeBasedTriggeringPolicy.newBuilder().withInterval(1).build()))
		   	.withStrategy(DefaultRolloverStrategy.newBuilder().withMax(Integer.MAX_VALUE + "").withMin("1").withCompressionLevelStr(Deflater.NO_COMPRESSION + "").withConfig(config).build()).build();
		appender.start();
		config.addAppender(appender);
	    AppenderRef ref = AppenderRef.createAppenderRef(name, null, null);
	    LoggerConfig logcfg0 = LoggerConfig.createLogger(true, Level.ALL, name, "true", new AppenderRef[] { ref }, null, config, null);
	    config.addLogger(name, logcfg0);
	    context.getLogger(name).addAppender(appender);
		context.updateLoggers();
		//
		appender = ConsoleAppender.newBuilder().withName(name).withLayout(PatternLayout.newBuilder().withPattern("%d{[dd.MM.yy] [HH:mm:ss;SSS]} [%t/%p]: %m%n").withConfiguration(config).build()).setConfiguration(config).build();
		appender.start();
		context.getRootLogger().removeAppender(context.getRootLogger().getAppenders().values().iterator().next());
		context.getRootLogger().addAppender(appender);
		return context.getLogger(name);
	}
	
	public static final void info(Object obj){
		LOGGER.info(String.valueOf(obj));
	}
	
	public static final void error(Object obj){
		LOGGER.error(String.valueOf(obj));
	}
	
	public static final void debug(Object obj){
		LOGGER.debug(String.valueOf(obj));
	}
	
	public static void main(String[] args) throws InterruptedException {
		info("Preparing webserver start.");
		new Fexcraft(args);
	}
	
	/// ---- ///
	
	private JsonObject config;
	private int http_port, https_port;
	private boolean devmode;
	private JDA jda;
	private Scheduler scheduler;
	//private Server SERVER;
	
	public Fexcraft(String[] args) throws InterruptedException {
		INSTANCE = this; config = JsonUtil.get(new File("./configuration.json"));
		http_port = getProperty("http_port", 80).getAsInt();
		https_port = getProperty("https_port", 443).getAsInt();
		devmode = getProperty("local", false).getAsBoolean();
		//
		ServerConnector http = new ServerConnector(this);
		http.setHost(getProperty("host", "0.0.0.0").getAsString());
		http.setPort(http_port);
		http.setIdleTimeout(getProperty("idle_timeout", 30000).getAsLong());
		try{
			HttpConfiguration https_config = new HttpConfiguration();
			https_config.setSecureScheme("https");
			https_config.addCustomizer(new SecureRequestCustomizer());
			SslContextFactory ssl = new SslContextFactory("./keystore.jks");
			ssl.setKeyStorePassword(getProperty("keystore_password", "invalid").getAsString());
			ServerConnector https = new ServerConnector(this, new SslConnectionFactory(ssl, "http/1.1"), new HttpConnectionFactory(https_config));
			https.setPort(https_port);
			https.setReuseAddress(true);
			this.setConnectors(new Connector[]{ https, http });
		}
		catch(Exception e){
			e.printStackTrace();
			this.addConnector(http);
		}
		//
		/*ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase("./resources/public");
		debug(context.getResourceBase());
		context.getSessionHandler().addEventListener(new SessionListener());
		context.setHandler(this);
		this.getClass().getClassLoader().getResource("/org/eclipse/jetty/http/mime.properties");
		this.setHandler(context);*/
		//
		SessionListener shandler = new SessionListener();
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/"); context.setResourceBase("./resources/public");
		context.getSessionHandler().addEventListener(shandler);
		//
		ServletContextHandler forums = new ServletContextHandler(ServletContextHandler.SESSIONS);
		forums.setContextPath("/"); forums.setResourceBase("./resources/forums");
		forums.getSessionHandler().addEventListener(shandler);
		forums.setVirtualHosts(new String[]{ "forum.fexcraft.test", "forum.fexcraft.net" });
		//
		ServletContextHandler proxy = new ServletContextHandler(ServletContextHandler.SESSIONS);
		proxy.setContextPath("/"); proxy.setResourceBase("./resources/proxy");
		proxy.getSessionHandler().addEventListener(shandler);
		proxy.setVirtualHosts(new String[]{ "db.fexcraft.test", "db.fexcraft.net" });
		//
		info("Registering Servlets.");
		context.addServlet(Request.class, "/minecraft/fcl/request");
		context.addServlet(MainFileServer.class, "/files/*");
		context.addServlet(License.class, "/license");
		context.addServlet(Index.class, "/index");
		context.addServlet(Index.class, "/home");
		context.addServlet(Download.class, "/download");
		context.addServlet(Download.class, "/files/download");
		context.addServlet(DefaultServlet.class, "/");
		context.addServlet(Session.class, "/session");
		context.addServlet(AddDownload.class, "/minecraft/fcl/adddownload");
		context.addServlet(UpdateJson.class, "/minecraft/fcl/updatejson");
		context.addServlet(DatabaseViewer.class, "/database");
		context.addServlet(Register.class, "/register");
		//
		forums.addServlet(DefaultServlet.class, "/");
		forums.addServlet(ForumIndex.class, "/index");
		//
		ServletHolder proxyServlet = new ServletHolder(AuthProxy.class);
		proxyServlet.setInitParameter("proxyTo", "http://localhost:8080/");
		proxyServlet.setInitParameter("prefix", "/");
		proxy.addServlet(proxyServlet, "/*");
		//
		ContextHandlerCollection coll = new ContextHandlerCollection();
		coll.setHandlers(new Handler[]{ context, forums, proxy });
		this.getClass().getClassLoader().getResource("/org/eclipse/jetty/http/mime.properties");
		this.setHandler(coll);
		debug("\n" + context.getResourceBase() + "\n" + forums.getResourceBase());
		//
		try{
			info("Starting Webserver...");
			this.start();
		}
		catch(Exception e){
			error(e.getMessage());
			error("Running in NoSSL/Developement Mode!");
			devmode = true;
			e.printStackTrace();
		}
		info("Loading JDA (Discord Bot).");
		try{
			jda = new JDABuilder(AccountType.BOT).setToken(getProperty(devmode ? "discord_dev_token" : "discord_token", "null").getAsString()).addEventListener(new MainListener()).buildBlocking();
		}
		catch(LoginException | IllegalArgumentException | RateLimitedException e){ e.printStackTrace(); }
		AUDIOMANAGER = new DefaultAudioPlayerManager();
		info("Assigning AudioManager.");
		AudioSourceManagers.registerRemoteSources(AUDIOMANAGER);
		//info("Connecting to MySql Database...");
		//new MySql( getProperty("mysql_username", "root").getAsString(), getProperty("mysql_password", "passpass").getAsString(), getProperty("mysql_port", "0995").getAsString(), getProperty("mysql_hostname", "something.net").getAsString(), getProperty("mysql_database", "none").getAsString());
		info("Controlling Database.");
		RTDB.prepare();
		try{
			info("Starting Scheduler.");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			scheduler.scheduleJob(
				JobBuilder.newJob(FileCache.ScheduledClearing.class).withDescription("Removes files which wheren't used longer than 10 minutes.").withIdentity("file_clearer", "group0").build(),
				TriggerBuilder.newTrigger().withIdentity("10min", "group0").withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(10).repeatForever()).startNow().build());
			scheduler.scheduleJob(
				JobBuilder.newJob(ScheduledClearing.class).withDescription("Removes inactive data.").withIdentity("data_clearer", "group1").build(),
				TriggerBuilder.newTrigger().withIdentity("15min", "group1").withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(15).repeatForever()).startNow().build());
		}
		catch(SchedulerException e){
			error("Scheduler Setup Error: " + e.getMessage());
			e.printStackTrace();
		}
		info("Joining... (setup done)");
		this.join();
	}

	public JsonElement getProperty(String string, Object obj){
		if(config.has(string)){
			return config.get(string);
		}
		error(String.format("Entry '%s' in config not found, setting to supplied default '%s'!", string, obj));
		JsonElement elm = obj instanceof JsonElement ? (JsonElement)obj : new JsonPrimitive(obj.toString());
		config.add(string, elm);
		JsonUtil.write(new File("./configuration.json"), config);
		return elm;
	}
	
	public final static boolean dev(){
		return INSTANCE.devmode;
	}
	
	public static boolean redirect(HttpServletRequest request, HttpServletResponse response){
		Cookie cookie = new Cookie("JSESSIONID", request.getSession().getId());
		cookie.setDomain(dev() ? ".fexcraft.test" : ".fexcraft.net"); cookie.setHttpOnly(true);
	    response.addCookie(cookie);
		if((request.getServerPort() == INSTANCE.http_port || request.getScheme().equals("http")) && request.getParameter("nossl") == null && !dev()){
    		String str = "https://" + request.getServerName() + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            try{ response.sendRedirect(str); }
            catch(IOException e){ e.printStackTrace(); }
    		return true;
    	}
    	else{
			if(dev()){
				response.setHeader("Access-Control-Allow-Origin", "*");
			}
    		return false;
		}
	}
	
	public final JDA getJavaDiscordApplicationProgrammingInterface(){
		return jda;
	}

	public static class ScheduledClearing implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			ArrayList<String> oldtokens = new ArrayList<>();
			Cursor<HashMap<String, Object>> cursor = RTDB.get().table("download_tokens").run(RTDB.conn());
			for(HashMap<String, Object> obj : cursor){
				JsonObject json = JsonUtil.fromMapObject(obj);
				if(json.get("time").getAsLong() < System.currentTimeMillis()){
					oldtokens.add(json.get("id").getAsString());
				}
			}
			for(String tok : oldtokens){
				RTDB.get().table("download_tokens").get(tok).delete().run(RTDB.conn());
			}
		}

	}

}
