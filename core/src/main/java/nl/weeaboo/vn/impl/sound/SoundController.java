package nl.weeaboo.vn.impl.sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.sound.ISound;
import nl.weeaboo.vn.sound.ISoundController;
import nl.weeaboo.vn.sound.SoundType;

public class SoundController implements ISoundController {

    private static final long serialVersionUID = SoundImpl.serialVersionUID;

    private final Map<SoundType, Double> masterVolume;
    private final Map<Integer, ISound> sounds = new HashMap<>();
    private final List<ISound> pausedList = new ArrayList<>();

    private boolean paused;

    protected SoundController() {
        masterVolume = new EnumMap<>(SoundType.class);
        for (SoundType type : SoundType.values()) {
            masterVolume.put(type, 1.0);
        }
    }

    @Override
    public void update() {
        Iterator<Entry<Integer, ISound>> itr = sounds.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Integer, ISound> entry = itr.next();
            ISound s = entry.getValue();
            if (s.isStopped()) {
                pausedList.remove(s);
                itr.remove();
            }
        }
    }

    @Override
    public void stopAll() {
        for (int channel : ImmutableList.copyOf(sounds.keySet())) {
            stop(channel);
        }
    }

    @Override
    public void stop(int channel) {
        stop(channel, -1);
    }

    @Override
    public void stop(int channel, int fadeOutMillis) {
        ISound sound = sounds.remove(channel);
        if (sound != null) {
            pausedList.remove(sound);
            sound.stop(fadeOutMillis);
        }
    }

    @Override
    public ISound get(int channel) {
        return sounds.get(channel);
    }

    @Override
    public double getMasterVolume(SoundType type) {
        Preconditions.checkNotNull(type);
        return masterVolume.get(type);
    }

    protected Iterable<ISound> getSounds(SoundType type) {
        Collection<ISound> result = new ArrayList<>();
        for (ISound sound : sounds.values()) {
            if (sound.getSoundType() == type) {
                result.add(sound);
            }
        }
        return result;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public int getFreeChannel() {
        for (int n = MIN_CHANNEL; n <= MAX_CHANNEL; n++) {
            if (!sounds.containsKey(n)) {
                return n;
            }
        }
        throw new IllegalStateException("No free channels left");
    }

    @Override
    public void set(int channel, ISound sound) {
        Checks.checkState(!isPaused(), "Unable to add sounds while paused");

        stop(channel);

        if (sounds.containsKey(channel)) {
            throw new IllegalStateException("Attempt to overwrite an existing sound entry.");
        }

        double mvol = getMasterVolume(sound.getSoundType());
        sound.setMasterVolume(mvol);
        sounds.put(channel, sound);
    }

    @Override
    public void setMasterVolume(SoundType type, double vol) {
        masterVolume.put(type, vol);

        for (ISound sound : getSounds(type)) {
            sound.setMasterVolume(vol);
        }
    }

    @Override
    public void setPaused(boolean p) {
        paused = p;

        if (paused) {
            for (ISound sound : sounds.values()) {
                if (!sound.isPaused()) {
                    sound.pause();
                    pausedList.add(sound);
                }
            }
        } else {
            for (ISound sound : pausedList) {
                sound.resume();
            }
            pausedList.clear();
        }
    }

}
