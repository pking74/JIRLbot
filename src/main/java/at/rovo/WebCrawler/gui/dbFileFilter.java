package at.rovo.WebCrawler.gui;

import java.io.File;
import java.io.FileFilter;

public class dbFileFilter implements FileFilter
{

	@Override
	public boolean accept(File pathname)
	{
		if (pathname.getName().equals("cache.db"))
			return true;
		return false;
	}

}
