package space.nerdsin.plugins.enhancedchat.util;

import space.nerdsin.plugins.enhancedchat.services.IgnoringService;
import space.nerdsin.plugins.enhancedchat.services.PlayerService;
import space.nerdsin.plugins.enhancedchat.services.WhisperService;

public interface IServiceProvider {
  PlayerService getPlayerService();
  IgnoringService getIgnoringService();
  WhisperService getWhisperService();
}
