package nl.weeaboo.vn.sound;

import java.io.Serializable;

import nl.weeaboo.vn.core.IUpdateable;

public interface ISoundController extends Serializable, IUpdateable {

    /**
     * Stops all sounds.
     */
    void stopAll();

    /**
     * @see #stop(int, int)
     */
    void stop(int channel);

    /**
     * Stops the sound playing in the specified channel.
     *
     * @param channel The sound channel to stop.
     * @param fadeOutMillis Instead of stopping the sound immediately, fade it out slowly over the course of
     *        {@code fadeOutMillis}.
     */
    void stop(int channel, int fadeOutMillis);

    /**
     * @param channel The channel to request the contents of.
     * @return The sound in the specified channel, or {@code null} if none exists.
     */
    ISoundPart get(int channel);

    /**
     * @return The master volume for the specified type.
     *
     * @see #setMasterVolume(SoundType, double)
     */
    double getMasterVolume(SoundType type);

    /**
     * @see #setPaused(boolean)
     */
    boolean isPaused();

    int getFreeChannel();

    /**
     * @param channel The channel to store the sound in. If a sound is already in that slot, that sound is
     *        stopped first.
     * @param sound The sound object to store.
     */
    void set(int channel, ISoundPart sound);

    /**
     * Changes the master volume for the specified type. The master volume is multiplied together with the
     * sound's private volume to get the final playback volume.
     *
     * @param volume The master volume between {@code 0.0} and {@code 1.0}.
     */
    void setMasterVolume(SoundType type, double volume);

    /**
     * Pauses or unpauses this sound state. Any sounds that are/were already paused are left alone.
     */
    void setPaused(boolean p);

}