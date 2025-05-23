package me.checkmatechris1.music;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class SongHandler implements AudioSendHandler {
    private final AudioPlayer player;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    private static TimescalePcmAudioFilter speedFilter = null;

    public SongHandler(AudioPlayer player) {
        this.player = player;
        this.buffer = ByteBuffer.allocate(1024);

        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);

        // Set filter factory to start implement filters to music
        //this.player.setFilterFactory((track, format, output) -> PlayerFilters.getInstance().buildChain(track,format,output));

/*        player.setFilterFactory((track, format, output)->{
            List<AudioFilter> list = new ArrayList<>();

            TimescalePcmAudioFilter audioFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            audioFilter.setSpeed(1.5); //1.5x normal speed
            list.add(audioFilter);

            VolumePcmAudioFilter volumeFilter = new VolumePcmAudioFilter(output, format.channelCount, format.sampleRate);
            volumeFilter.setVolume(0.25f);
            list.add(volumeFilter);

            return list.subList(0,list.size()-1);
        });*/

        System.out.println("Player speed: " + PlayerFilters.getInstance().getSpeed());
        System.out.println("Player volume: " + PlayerFilters.getInstance().getVolume());
    }

    @Override
    public boolean canProvide() {
        return this.player.provide(this.frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return this.buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    public void setVolume(int volume) {
        PlayerFilters.getInstance().setVolume(volume);
        this.player.setFilterFactory((track, format, output) -> PlayerFilters.getInstance().buildChain(track,format,output));
    }

    public void setSpeed(int speed) {
        PlayerFilters.getInstance().setSpeed(speed);
        this.player.setFilterFactory((track, format, output) -> PlayerFilters.getInstance().buildChain(track,format,output));
    }

    public void setPaused(boolean paused) {
        this.player.setPaused(paused);
    }
}
