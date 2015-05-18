package at.rovo.test;

import at.rovo.caching.drum.DrumException;
import at.rovo.caching.drum.DrumOperation;
import at.rovo.caching.drum.data.StringSerializer;
import at.rovo.caching.drum.internal.InMemoryData;
import at.rovo.caching.drum.internal.backend.cacheFile.CacheFile;
import at.rovo.crawler.bean.PLDData;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheAppendUpdateTest extends BaseCacheTest
{
    private final static Logger LOG = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private File testDir = null;

    @Override
    public void initDataDir() throws Exception
    {
        // create the test cache directory
        this.testDir = new File(cacheDir + "/test");
        if (!this.testDir.exists())
        {
            if (!this.testDir.mkdir())
            {
                throw new Exception("Data directory of cache does not exist and could not get created!");
            }
        }
    }

    @Test
    public void test() throws DrumException
    {
        CacheFile<PLDData, StringSerializer> cache = new CacheFile<>(this.testDir + "/cache.db", "test", PLDData.class);

        // old file on disk to merge with: (1; 2; <3, 7>), (5; 2; <2, 19>), (76; 4; <5, 13, 22, 88)
        // parameters: (nodeId; number of linked elements; <each linked element>)
        LOG.debug("Creating original data - (1; 2; <3, 7>), (5; 2; <2, 19>), (76; 4; <5, 13, 22, 88)");

        PLDData data1 = new PLDData();
        data1.setHash(1);
        Set<Long> neighbor1 = new TreeSet<>();
        neighbor1.add(7L);
        neighbor1.add(3L);
        data1.setIndegreeNeighbors(neighbor1);
        InMemoryData<PLDData, StringSerializer> mem1 =
                new InMemoryData<>(1L, data1, new StringSerializer(""), DrumOperation.UPDATE);

        PLDData data2 = new PLDData();
        data2.setHash(5);
        Set<Long> neighbor2 = new TreeSet<>();
        neighbor2.add(2L);
        neighbor2.add(19L);
        data2.setIndegreeNeighbors(neighbor2);
        InMemoryData<PLDData, StringSerializer> mem2 =
                new InMemoryData<>(5L, data2, new StringSerializer(""), DrumOperation.UPDATE);

        PLDData data3 = new PLDData();
        data3.setHash(76);
        Set<Long> neighbor3 = new TreeSet<>();
        neighbor3.add(5L);
        neighbor3.add(13L);
        neighbor3.add(22L);
        neighbor3.add(88L);
        data3.setIndegreeNeighbors(neighbor3);
        InMemoryData<PLDData, StringSerializer> mem3 =
                new InMemoryData<>(76L, data3, new StringSerializer(""), DrumOperation.UPDATE);

        try
        {
            cache.writeEntry(mem1, false);
            cache.writeEntry(mem2, false);
            cache.writeEntry(mem3, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail(e.getLocalizedMessage());
        }

        List<Long> keys = new ArrayList<>();
        List<PLDData> values = new ArrayList<>();
        cache.printCacheContent(keys, values);

        assertEquals(3, keys.size());
        assertEquals(3, values.size());
        assertEquals(new Long(1), keys.get(0));
        assertEquals(new Long(5), keys.get(1));
        assertEquals(new Long(76), keys.get(2));
        assertTrue(values.get(0).getIndegreeNeighbors().containsAll(neighbor1));
        assertTrue(values.get(1).getIndegreeNeighbors().containsAll(neighbor2));
        assertTrue(values.get(2).getIndegreeNeighbors().containsAll(neighbor3));

        cache.reset();

        LOG.debug(
                "Adding new data to integrate into an existing entry - new batch: (5; 4; <2, 3, 7, 88>), (76; 2; <4, 13>)");

        // new batch, sorted as described above: (5; 4; <2, 3, 7, 88>), (76; 2; <4, 13>)
        PLDData data2v2 = new PLDData();
        data2v2.setHash(5L);
        Set<Long> neighbor2v2 = new TreeSet<>();
        neighbor2v2.add(2L);
        neighbor2v2.add(3L);
        neighbor2v2.add(7L);
        neighbor2v2.add(88L);
        data2v2.setIndegreeNeighbors(neighbor2v2);
        InMemoryData<PLDData, StringSerializer> mem2v2 =
                new InMemoryData<>(5L, data2v2, new StringSerializer(""), DrumOperation.APPEND_UPDATE);

        PLDData data3v2 = new PLDData();
        data3v2.setHash(76L);
        Set<Long> neighbor3v2 = new TreeSet<>();
        neighbor3v2.add(4L);
        neighbor3v2.add(13L);
        data3v2.setIndegreeNeighbors(neighbor3v2);
        InMemoryData<PLDData, StringSerializer> mem3v2 =
                new InMemoryData<>(76L, data3v2, new StringSerializer(""), DrumOperation.APPEND_UPDATE);

        try
        {
            cache.writeEntry(mem2v2, true);
            cache.writeEntry(mem3v2, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail(e.getLocalizedMessage());
        }

        // new file produced in one pass: (1; 2; <3, 7>), (5; 5; <2, 3,  7, 19, 88), (76; 5; <4, 5, 13, 22, 88>)
        keys.clear();
        values.clear();

        cache.printCacheContent(keys, values);

        assertEquals(3, keys.size());
        assertEquals(3, values.size());
        assertEquals(new Long(1), keys.get(0));
        assertEquals(new Long(5), keys.get(1));
        assertEquals(new Long(76), keys.get(2));
        assertEquals(2, values.get(0).getIndegreeNeighbors().size());
        assertEquals(5, values.get(1).getIndegreeNeighbors().size());
        assertEquals(5, values.get(2).getIndegreeNeighbors().size());
        // the first set wasn't modified so it should contain the original entries
        assertTrue(values.get(0).getIndegreeNeighbors().containsAll(neighbor1));
        assertTrue(values.get(1).getIndegreeNeighbors().containsAll(neighbor2v2));
        assertTrue(values.get(2).getIndegreeNeighbors().containsAll(neighbor3v2));

        // close the cache file so we can delete the cache.db file
        cache.close();
    }
}
