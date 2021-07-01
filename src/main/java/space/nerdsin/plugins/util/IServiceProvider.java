package space.nerdsin.plugins.util;

import space.nerdsin.plugins.services.IgnoringService;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.services.WhisperService;

public interface IServiceProvider {
  PlayerService getPlayerService();
  IgnoringService getIgnoringService();
  WhisperService getWhisperService();
}
