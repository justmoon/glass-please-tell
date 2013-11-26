/*
 * Copyright (C) 2013 Stefan Thomas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.justmoon.tell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class TellService extends Service implements TextToSpeech.OnInitListener {

    private static final String TAG = "TellService";
    
    private static final Map<String, String> WORD_REPLACEMENTS;
    static {
    	Map<String, String> aMap = new HashMap<String, String>();
    	
    	// First person to third person (male)
    	// TODO: Should be configurable male/female
        aMap.put("I", "he");
        aMap.put("my", "his");
        aMap.put("me", "him");
        
        // First person contractions
        aMap.put("I'm", "he's");
        
        // Second person to first person
        aMap.put("you", "I");
        aMap.put("your", "my");
        // aMap.put("you", "me"); <-- Ambiguity
        
        // Second person contractions
        aMap.put("you're", "I'm");
        
        // Third person to second person
        aMap.put("he", "you");
        aMap.put("she", "you");
        aMap.put("his", "your");
        aMap.put("her", "your");
        aMap.put("him", "you");
        // aMap.put("her", "you"); <-- Ambiguity
        
        // Third person contractions
        aMap.put("he's", "you're");
        aMap.put("she's", "you're");

        WORD_REPLACEMENTS = Collections.unmodifiableMap(aMap);
    }

    private TextToSpeech mSpeech;
    private String mNextPhrase;

    @Override
    public void onCreate() {
        super.onCreate();
        mSpeech = new TextToSpeech(this, this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	ArrayList<String> voiceResults = intent.getExtras()
                .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
    	
    	String bestResult = voiceResults.get(0);
    	
    	String[] words = TextUtils.split(bestResult, " ");
    	ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));

        Log.d(TAG, "Input: " + TextUtils.join(" ", words));
    	
        String targetName;
        // Template: please tell [Bob] to [shut up]
        // Response: shut up, Bob
        //
        // Template: please tell [Bob] that [he sucks]
        // Response: you sucks, Bob
    	if (wordsList.size() >= 3 && (wordsList.get(1).equals("to") || wordsList.get(1).equals("that"))) {
    		Log.d(TAG, "Detected 'to' phrase");

    		// Get name of the person we're supposed to address
    		targetName = wordsList.get(0);

    		// Get the text we're supposed to say to them
    		wordsList = new ArrayList<String>(wordsList.subList(2, wordsList.size()));
    		
    		mNextPhrase = TextUtils.join(" ", processWords(wordsList)) + ", " + targetName;
    	} else if (wordsList.size() >= 4 && wordsList.get(1).equals("not") && wordsList.get(2).equals("to")) {
    		// Get the name of the person we're supposed to address
    		targetName = wordsList.get(0);
    		
    		// Get the thing they're not supposed to do
    		wordsList = new ArrayList<String>(wordsList.subList(3, wordsList.size()));

    		mNextPhrase = "hey, " + targetName + ", don't " + TextUtils.join(" ", processWords(wordsList));
    	} else {
    		mNextPhrase = TextUtils.join(" ", processWords(wordsList));
    	}
        
    	// This will fail if the speech engine is not yet initialized, but in
    	// this case the onInit method will do the speaking instead.
        mSpeech.speak(mNextPhrase, TextToSpeech.QUEUE_FLUSH, null);
        
        Log.d(TAG, "Speaking: " + mNextPhrase);

        return START_STICKY;
    }
    
    private ArrayList<String> processWords(ArrayList<String> words) {
    	ArrayList<String> outputWords = new ArrayList<String>();
    	
    	// Replace certain known words, most to change viewpoint of the speaker.
    	// For example, third person needs to become second person.
    	for (String word: words) {
    		if (WORD_REPLACEMENTS.containsKey(word)) {
	    		outputWords.add(WORD_REPLACEMENTS.get(word));
    		} else {
	    		outputWords.add(word);
    		}
    	}
    	
    	return outputWords;
    }

    @Override
    public void onDestroy() {
        mSpeech.shutdown();
        mSpeech = null;

        super.onDestroy();
    }

	@Override
	public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mSpeech.speak(mNextPhrase, TextToSpeech.QUEUE_FLUSH, null);
        }
	}
}
