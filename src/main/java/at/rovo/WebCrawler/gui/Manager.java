package at.rovo.WebCrawler.gui;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import at.rovo.WebCrawler.IRLbot;
import at.rovo.WebCrawler.IRLbotListener;
import at.rovo.WebCrawler.gui.events.BEASTEventListener;
import at.rovo.WebCrawler.gui.events.IRLbotCreationListener;
import at.rovo.WebCrawler.gui.events.IRLbotEventListener;
import at.rovo.WebCrawler.gui.events.RobotsCacheEventListener;
import at.rovo.WebCrawler.gui.events.RobotsRequestedEventListener;
import at.rovo.WebCrawler.gui.events.STAREventListener;
import at.rovo.WebCrawler.gui.events.URLseenEventListener;
import at.rovo.caching.drum.event.DrumEvent;

public class Manager implements IRLbotListener
{
	private static Manager INSTANCE = null;
	private IRLbot crawler = null;
	private Thread crawlerThread = null;
	private Set<IRLbotEventListener> irlbotEventListeners = new CopyOnWriteArraySet<>();
	private Set<URLseenEventListener> urlseenEventListeners = new CopyOnWriteArraySet<>();
	private Set<STAREventListener> starEventListeners = new CopyOnWriteArraySet<>();
	private Set<BEASTEventListener> beastEventListeners = new CopyOnWriteArraySet<>();
	private Set<RobotsCacheEventListener> robotsCacheEventListeners = new CopyOnWriteArraySet<>();
	private Set<RobotsRequestedEventListener> robotsRequestedEventLisetenres = new CopyOnWriteArraySet<>();
	private Set<IRLbotCreationListener> irlbotCreationListener = new CopyOnWriteArraySet<>();
	
	private Manager()
	{
				
	}
	
	public static Manager getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new Manager();
		return INSTANCE;
	}
	
	public IRLbot getIRLbot()
	{
		return this.crawler;
	}
	
	public Thread createCrawler(String[] urls, int numCrawler, int numRobotDownloader, 
			int urlSeenBuckets, int urlSeenByteSize, int pldBuckets, int pldByteSize, 
			int robotsCacheBuckets, int robotsCacheByteSize, 
			int robotsRequestedBuckets, int robotsRequestedByteSize)
	{
		this.crawler = new IRLbot(urls, numCrawler, numRobotDownloader,
				urlSeenBuckets,pldBuckets, robotsCacheBuckets, robotsRequestedBuckets, 
				urlSeenByteSize, pldByteSize, robotsCacheByteSize, robotsRequestedByteSize);
		
		this.crawler.addIRLbotListener(this);
		if (crawlerThread == null)
		{
			this.crawlerThread = new Thread(this.crawler);
			this.crawlerThread.setName("IRLbot");
			this.crawlerThread.start();
		}
		
		for (IRLbotCreationListener listener : this.irlbotCreationListener)
			listener.irlbotCreated();
		
		return this.crawlerThread;
	}
	
	// Listener Section
	
	public void registerForIRLbotCreationEvents(IRLbotCreationListener listener)
	{
		this.irlbotCreationListener.add(listener);
	}
	
	public void unregisterForIRLbotCreatetionEvents(IRLbotCreationListener listener)
	{
		this.irlbotCreationListener.remove(listener);
	}
	
	public void registerForIRLbotEvents(IRLbotEventListener listener)
	{
		this.irlbotEventListeners.add(listener);
	}
	
	public void unregisterFromIRLbotEvents(IRLbotEventListener listener)
	{
		this.irlbotEventListeners.remove(listener);
	}
	
	public void registerForUrlSeenEvents(URLseenEventListener listener)
	{
		this.urlseenEventListeners.add(listener);
	}
	
	public void unregisterFromUrlSeenEvents(URLseenEventListener listener)
	{
		this.urlseenEventListeners.remove(listener);
	}
	
	public void registerForStarEvents(STAREventListener listener)
	{
		this.starEventListeners.add(listener);
	}
	
	public void unregisterFromStarEvents(STAREventListener listener)
	{
		this.starEventListeners.remove(listener);
	}
	
	public void registerForBeastEvents(BEASTEventListener listener)
	{
		this.beastEventListeners.add(listener);
	}
	
	public void unregisterFromBeastEvents(BEASTEventListener listener)
	{
		this.beastEventListeners.remove(listener);
	}
	
	public void registerForRobotsCacheEvents(RobotsCacheEventListener listener)
	{
		this.robotsCacheEventListeners.add(listener);
	}
	
	public void unregisterFromRobotsCacheEvents(RobotsCacheEventListener listener)
	{
		this.robotsCacheEventListeners.remove(listener);
	}
	
	public void registerForRobotsRequestedEvents(RobotsRequestedEventListener listener)
	{
		this.robotsRequestedEventLisetenres.add(listener);
	}
	
	public void unregisterFromRobotsRequestedEvents(RobotsRequestedEventListener listener)
	{
		this.robotsRequestedEventLisetenres.remove(listener);
	}
	
	@Override
	public void numberOfURLsCrawledTotalChanged(long newSize)
	{
		for (IRLbotEventListener listener : this.irlbotEventListeners)
			listener.numberOfURLsCrawledTotalChanged(newSize);
	}
	
	@Override
	public void numberOfURLsCrawledSuccessChanged(long newSize)
	{
		for (IRLbotEventListener listener : this.irlbotEventListeners)
			listener.numberOfURLsCrawledSuccessChanged(newSize);
	}
	
	@Override
	public void numberOfURLsToCrawlChanged(long newSize)
	{
		for (IRLbotEventListener listener : this.irlbotEventListeners)
			listener.numberOfURLsToCrawlChanged(newSize);
	}
	
	@Override
	public void sizeOfRobotTxtDownloadQueue(long newSize)
	{
		for (IRLbotEventListener listener : this.irlbotEventListeners)
			listener.numberOfRobotsTxtToDownload(newSize);
	}

	@Override
	public void drumUpdate(DrumEvent<? extends DrumEvent<?>> event)
	{
		switch(event.getDrumName())
		{
			case "urlSeen":
				for (URLseenEventListener listener : this.urlseenEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StateUpdate"))
						listener.stateChanged(event);
					else
						listener.actionPerformed(event);						
				}
				for (IRLbotEventListener listener : this.irlbotEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StorageEvent"))
						listener.numberOfUniqueURLs(event);
				}
				break;
			case "pldIndegree":
				for (STAREventListener listener : this.starEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StateUpdate"))
						listener.stateChanged(event);
					else
						listener.actionPerformed(event);
				}
				for (IRLbotEventListener listener : this.irlbotEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StorageEvent"))
						listener.numberOfUniquePLDs(event);
				}
				break;
			case "robotsCache":
				for (RobotsCacheEventListener listener : this.robotsCacheEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StateUpdate"))
						listener.stateChanged(event);
					else
						listener.actionPerformed(event);
				}
				for (IRLbotEventListener listener : this.irlbotEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StorageEvent"))
						listener.numberOfUniqueRobotEntries(event);
				}
				break;
			case "robotsRequested":
				for (RobotsRequestedEventListener listener : this.robotsRequestedEventLisetenres)
				{
					if (event.getRealClass().getName().endsWith("StateUpdate"))
						listener.stateChanged(event);
					else
						listener.actionPerformed(event);
				}
				for (IRLbotEventListener listener : this.irlbotEventListeners)
				{
					if (event.getRealClass().getName().endsWith("StorageEvent"))
						listener.numberOfRobotEntriesRequested(event);
				}
				break;
		}
	}
}
