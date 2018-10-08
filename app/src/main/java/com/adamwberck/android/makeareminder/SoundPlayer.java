package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundPlayer {
    private static final String TAG = "SoundPlayer";

    private static final int MAX_SOUNDS = 1;


    private final AssetManager mAssets;
    private final SoundPool mSoundPool;
    private List<Sound> mSounds = new ArrayList<>();
    private static final String SOUNDS_FOLDER = "sample_alarms";

    public SoundPlayer(Context context) {
        mAssets = context.getAssets();
        //This old  method is deprecated but is need for compatibility.
        mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC,0);
        loadSounds();
    }

    public void play(Sound sound,float volume){
        Integer soundId = sound.getSoundId();
        if (soundId==null) {
            return;
        }
        mSoundPool.play(soundId,volume,volume,1,0,1);
    }

    public List<Sound> getSounds() {
        return mSounds;
    }

    private void loadSounds() {
        String[] soundNames;
        try {
            soundNames = mAssets.list(SOUNDS_FOLDER);
            Log.i(TAG,"Found " + soundNames.length + " sounds");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets",ioe);
            return;
        }

        for (String filename : soundNames){
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Sound sound = new Sound(assetPath);
                load(sound);
                mSounds.add(sound);
            } catch (IOException ioe) {
                Log.e(TAG,"Could not load sound " + filename,ioe);
            }
        }
    }

    private void load(Sound sound) throws IOException {
        AssetFileDescriptor afd = mAssets.openFd(sound.getAssetPath());
        int soundId = mSoundPool.load(afd,1);
        sound.setSoundId(soundId);
    }
}
