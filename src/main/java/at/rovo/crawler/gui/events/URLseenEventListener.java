package at.rovo.crawler.gui.events;

import at.rovo.drum.event.DrumEvent;

public interface URLseenEventListener
{
    void stateChanged(DrumEvent<? extends DrumEvent<?>> event);

    void actionPerformed(DrumEvent<? extends DrumEvent<?>> event);
}
