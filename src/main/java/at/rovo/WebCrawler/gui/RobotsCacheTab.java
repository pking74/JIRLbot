package at.rovo.WebCrawler.gui;

import at.rovo.WebCrawler.gui.events.RobotsCacheEventListener;

public class RobotsCacheTab extends DrumEventTab implements RobotsCacheEventListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8722442779313888912L;

	public RobotsCacheTab(int numBuckets)
	{
		super(numBuckets);
		
		Manager.getInstance().registerForRobotsCacheEvents(this);
	}
}
