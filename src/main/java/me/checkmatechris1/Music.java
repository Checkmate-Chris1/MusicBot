package me.checkmatechris1;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import me.checkmatechris1.music.GuildMusicManager;
import me.checkmatechris1.music.PlayerManager;
import me.checkmatechris1.music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Music extends ListenerAdapter {
    public final static String token = "TOKEN";

    public static JDA jda;
    public static String prefix = "$";
    public static boolean paused = false;

    // Main method (Immediately runs)
    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(token)
        .setAudioSendFactory(new NativeAudioSendFactory())
        .build();

        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.playing("Music!"));

        // Registering listeners, voice, etc.
        jda.addEventListener(new Music());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        String[] args = e.getMessage().getContentRaw().split("\\s+");

        if(args[0].equalsIgnoreCase(prefix + "join")) {
            if(!(e.getMember().getVoiceState().inVoiceChannel())) {
                e.getMessage().reply("You must be in a voice channel to use this command!").queue();
                return;
            }
            VoiceChannel channel = e.getMember().getVoiceState().getChannel();
            if(e.getGuild().getAudioManager().isConnected()) {
                e.getMessage().reply("Already connected to a channel! Use `" + prefix + "leave` to make me join a different channel.").queue();
                return;
            }
            e.getMessage().reply("Joining the channel! Get ready to get the party started! :partying_face:").queue();
            e.getGuild().getAudioManager().openAudioConnection(channel);
        } else if(args[0].equalsIgnoreCase(prefix + "leave")) {
            e.getGuild().getAudioManager().closeAudioConnection();
            //
            // NOTE: Working on saving and loading connected voice channels
            //
            e.getMessage().reply("Left the voice channel!").queue();
        } else if(args[0].equalsIgnoreCase(prefix + "play")) {
            if(args.length < 2) {
                if(paused == false) {
                    e.getMessage().reply("The player is already playing!").queue();
                    return;
                }

                paused = false;
                PlayerManager.getInstance().setPaused(e.getGuild(),paused);
                e.getMessage().reply(":loud_sound: `Player unpaused`").queue();
                return;
            }
            StringBuilder link = new StringBuilder("");
            for(int i = 1; i < args.length; i++) {
                link.append(args[i] + " ");
            }
            System.out.println(link.toString());

            if(!(isUrl(link.toString()))) {
                link.insert(0, "ytsearch:");
            }

            PlayerManager.getInstance().loadAndPlay(e.getChannel(), link.toString());
        } else if(args[0].equalsIgnoreCase(prefix + "volume")) {
            if(args.length < 2) {
                e.getMessage().reply("Hey! Include the volume you want it to be at! (1-100)").queue();
                return;
            } else if(!isInt(args[1])) {
                e.getMessage().reply("Hmm.. are you sure you typed in a number? (1-100)").queue();
                return;
            }
            int volume = Integer.parseInt(args[1]);
            if(!(volume <= 200 && volume >= 0)) {
                e.getMessage().reply("The volume has to be between 0 and 200, and a whole number. No more, no less!").queue();
                return;
            }

            e.getMessage().reply("Volume set to: `" + volume + "`").queue();
            PlayerManager.getInstance().setVolume(e.getGuild(),volume);
        } else if(args[0].equalsIgnoreCase(prefix + "pause")) {
            paused = true;
            PlayerManager.getInstance().setPaused(e.getGuild(),paused);
            e.getMessage().reply(":mute: `Player paused`").queue();

        } else if(args[0].equalsIgnoreCase(prefix + "skip")) {
            if(args.length > 1) {
                if(isInt(args[1])) {
                    int numSkips = Integer.parseInt(args[1]);
                    for(int i = 0; i < numSkips; i++) {
                        PlayerManager.getInstance().getMusicManager(e.getGuild()).scheduler.nextTrack();
                    }

                    e.getMessage().reply(":fast_forward: `" + numSkips + "` track(s) skipped!").queue();
                    return;
                }
            }

            PlayerManager.getInstance().getMusicManager(e.getGuild()).scheduler.nextTrack();
            e.getMessage().reply(":fast_forward: Track skipped!").queue();

        } else if(args[0].equalsIgnoreCase(prefix + "unpause")) {
            if(paused == false) {
                e.getMessage().reply("Player is already playing!").queue();
                return;
            }

            paused = false;
            PlayerManager.getInstance().setPaused(e.getGuild(),paused);
            e.getMessage().reply(":loud_sound: `Player unpaused`").queue();
        } else if(args[0].equalsIgnoreCase(prefix + "queue")) {
            TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(e.getGuild()).scheduler;
            StringBuilder descBuild = new StringBuilder();
            final int[] queueIndex = {0};

            if(scheduler.queue.size() < 1) {
                e.getMessage().reply("There is no queue! Start a song with `" + prefix + "play` while I'm in a voice channel.").queue();
                return;
            }
            scheduler.queue.forEach(track -> {
                queueIndex[0]++;
                descBuild.append(queueIndex[0] + ") " + track.getInfo().title + "\n");
            });

            String queueDesc = descBuild.toString();
            MessageEmbed list = new EmbedBuilder().setTitle(":musical_note: Music Queue :musical_note:").setDescription(queueDesc).setColor(Color.GRAY).build();

            e.getMessage().reply(list).queue();
        } else if(args[0].equalsIgnoreCase(prefix + "loop" ) || args[0].equalsIgnoreCase(prefix + "repeat")) {
            GuildMusicManager guildManager = PlayerManager.getInstance().getMusicManager(e.getGuild());

            guildManager.scheduler.repeat = !guildManager.scheduler.repeat;
            e.getMessage().reply(":repeat: Music Looping: `" + guildManager.scheduler.repeat + "`").queue();
        } else if(args[0].equalsIgnoreCase(prefix + "speed")) {
            if(args.length < 2) {
                e.getMessage().reply("Please put a speed of 50-200% in the command!").queue();
                return;
            } else if(!isInt(args[1])) {
                e.getMessage().reply("The speed has to be a number from 50-200!").queue();
                return;
            }
            int speed = Integer.parseInt(args[1]);
            if(!(speed >= 50) || !(speed <= 200)) {
                e.getMessage().reply("The speed has to be number from 50-200!").queue();
                return;
            }

            PlayerManager.setSpeed(e.getGuild(),speed/100);
            e.getMessage().reply("Player speed: `" + speed + "%` (Please wait for it to take effect)").queue();
        }
    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isInt(String num) {
        try {
            Integer.parseInt(num);
            return true;
        } catch(NumberFormatException ex) {
            return false;
        }
    }
}