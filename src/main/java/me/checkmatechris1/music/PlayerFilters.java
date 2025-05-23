package me.checkmatechris1.music;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.github.natanbc.lavadsp.volume.VolumePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.List;

public class PlayerFilters implements PcmFilterFactory {
    private int speed;
    private int volume;

    public static PlayerFilters INSTANCE;

    public PlayerFilters() {
        speed = 100;
        volume = 100;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    public int getVolume() {
        return this.volume;
    }
    public int getSpeed() {
        return this.speed;
    }

    public static PlayerFilters getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerFilters();
        }
        return INSTANCE;
    }

    @Override
    public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        List<AudioFilter> list = new ArrayList<>();

        TimescalePcmAudioFilter speedFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
        speedFilter.setSpeed(getInstance().speed); //1.5x normal speed
        list.add(0, speedFilter);
        System.out.println("[Debug] Speed: " + getInstance().getSpeed());

        VolumePcmAudioFilter volumeFilter = new VolumePcmAudioFilter(output, format.channelCount, format.sampleRate);
        volumeFilter.setVolume(getInstance().volume);
        list.add(0, volumeFilter);
        System.out.println("[Debug] Volume: " + getInstance().getVolume());

        return list.subList(0,list.size()-1);
    }
}

