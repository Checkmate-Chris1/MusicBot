package me.checkmatechris1.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    private final AudioPlayer player;
    public final TrackScheduler scheduler;
    private final SongHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        this.player.addListener(this.scheduler);
        this.sendHandler = new SongHandler(this.player);
    }

    public SongHandler getHandler() {
        return sendHandler;
    }
}
