package me.checkmatechris1.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long,GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {


        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getHandler());

            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, String trackUrl) {
        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding track: `" + track.getInfo().title + "`").queue();
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                if(playlist.isSearchResult()) {
                    AudioTrack selectedTrack = tracks.get(0);
                    musicManager.scheduler.queue(selectedTrack);
                    channel.sendMessage("Adding track: `" + selectedTrack.getInfo().title + "`").queue();
                    return;
                }
                channel.sendMessage("Adding playlist: `" + playlist.getName() + "`").queue();

                for(final AudioTrack track : tracks) {
                    musicManager.scheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Couldn't find a match for that search! Try again with a different search.").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Hmm, that didn't work! Try again with a different link maybe? :thinking:").queue();
            }
        });
    }

    public static PlayerManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }

    public void setVolume(Guild guild, int volume) {
        GuildMusicManager musicManager = INSTANCE.getMusicManager(guild);
        musicManager.getHandler().setVolume(volume);
    }
    public void setPaused(Guild guild, boolean paused) {
        GuildMusicManager musicManager = INSTANCE.getMusicManager(guild);
        musicManager.getHandler().setPaused(paused);
    }
    public static void setSpeed(Guild guild, int speed) {
        GuildMusicManager musicManager = INSTANCE.getMusicManager(guild);
        musicManager.getHandler().setSpeed(speed);
    }
}
