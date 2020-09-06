package com.example.spintracks;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;

import com.example.spintracks.dal.Song;
import com.example.spintracks.fragments.PlaylistCreateFragment;
import com.example.spintracks.Workout.WorkoutCommand;
import com.example.spintracks.viewmodels.PlaylistViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private int playlistSource = -1;
    private List<? extends Song> playlistSongs = null;
    private boolean songObserverInitialized = false;
    private String playlistName;
    private static String workoutType;
    private PlaylistViewModel playlistModel;
    private volatile MediaPlayer player = null;
    private Lock playerLock = new ReentrantLock();
    private AnimationController animationController;
    private int oldPosition = 0;
    private boolean wasPlaying = true;
    private volatile int trackIndex = 0;
    private Lock trackIndexLock = new ReentrantLock();

    private TextView instr_primary_textview;
    private TextView instr_secondary_textview;
    private ImageButton playButton;
    private ImageButton fastForwardButton;
    private ImageButton fastRewindButton;
    private ImageButton skipNextButton;
    private ImageButton skipPrevButton;
    private ProgressBar progressBar;
    private TextView positionTextView;
    private TextView durationTextView;

    private ScheduledExecutorService executorService;
    private Runnable progressBarUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent intent = getIntent();
        playlistSource = intent.getIntExtra(PlaylistCreateFragment.playlistSourceKey, -1);
        playlistName = intent.getStringExtra(PlaylistCreateFragment.playlistNameKey);
        playlistModel = new ViewModelProvider(this).get(PlaylistViewModel.class);

        instr_primary_textview = findViewById(R.id.instruction_primary_textview);
        instr_secondary_textview = findViewById(R.id.instruction_secondary_textview);
        playButton = findViewById(R.id.playButton);
        fastForwardButton = findViewById(R.id.fastForwardButton);
        fastRewindButton = findViewById(R.id.fastRewindButton);
        skipNextButton = findViewById(R.id.skipNextButton);
        skipPrevButton = findViewById(R.id.skipPrevButton);
        progressBar = findViewById(R.id.progressBar);
        positionTextView = findViewById(R.id.playedSoFarTextView);
        durationTextView = findViewById(R.id.durationTextView);

        instr_primary_textview.setText("");
        instr_secondary_textview.setText("");
        playButton.setOnClickListener(v -> {
            if (player == null) return;
            if (wasPlaying) pausePlayer();
            else playPlayer();
        });
        fastForwardButton.setOnClickListener(v -> {
            if (player == null) return;
            int position = player.getCurrentPosition();
            int duration = player.getDuration();
            int nextPosition = Math.min(position + 10000, duration);
            player.seekTo(nextPosition);
        });
        fastRewindButton.setOnClickListener(v -> {
            if (player == null) return;
            int position = player.getCurrentPosition();
            int nextPosition = Math.max(position - 10000, 0);
            player.seekTo(nextPosition);
        });
        skipNextButton.setOnClickListener(v -> {
            if (player == null) return;
            skipTrack(true);
        });
        skipPrevButton.setOnClickListener(v -> {
            if (player == null) return;
            skipTrack(false);
        });

        animationController = new AnimationController();
        getLifecycle().addObserver(animationController);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!songObserverInitialized) {
            playlistModel.getLocalPlaylistSongs(playlistName).observe(this, songs -> {
                playlistSongs = songs;
                initializePlayer();
                initializeExecutorService();
                animationController.start();
            });
            songObserverInitialized = true;
        } else if (playlistSongs != null) {
            initializePlayer();
            initializeExecutorService();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //hideSystemUi();
    }

    @Override
    public void onStop() {
        super.onStop();
        destroyPlayer();
        destroyExecutorService();
    }

    public static void setWorkoutType(String type) {
        workoutType = type;
    }

    private void updateProgressBar(int position) {
        positionTextView.setText(timePositionString(position));
        progressBar.setProgress(position);
    }

    private synchronized void initializeExecutorService() {
        if (executorService != null) return;
        executorService = Executors.newScheduledThreadPool(4);
        progressBarUpdater = () -> {
            try {
                if (player == null) return;
                int position = player.getCurrentPosition();
                PlayerActivity.this.runOnUiThread(() -> {
                    updateProgressBar(position);
                });
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        };
        executorService.scheduleAtFixedRate(
                progressBarUpdater, 0, 1, TimeUnit.SECONDS);
    }

    private synchronized void initializePlayer() {
        if (playlistSongs == null) return;
        if (player != null) return;
        playerLock.lock();
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        preparePlayer();
        startOnPrepared();
        playerLock.unlock();
    }

    private void startOnPrepared() {
        player.setOnPreparedListener(mp -> {
            beginTrack();
        });
    }

    private void skipTrack(boolean skipNext) {
        trackIndexLock.lock();
        if (skipNext) trackIndex++;
        else trackIndex--;
        trackIndexLock.unlock();
        playerLock.lock();
        player.reset();
        preparePlayer();
        startOnPrepared();
        playerLock.unlock();
    }

    private void preparePlayer() {
        Song song = playlistSongs.get(trackIndex);
        try {
            player.setDataSource(this, Uri.parse(song.getUri()));
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            finish();
        }
        player.setOnSeekCompleteListener(mp -> {
            int position = mp.getCurrentPosition();
            updateProgressBar(position);
        });
        if (trackIndex < playlistSongs.size() - 1) {
            player.setOnCompletionListener(mp -> skipTrack(true));
        } else {
            player.setOnCompletionListener(mp -> {
                Intent intent = new Intent(this, FinishWorkoutActivity.class);
                startActivity(intent);
            });
        }
        // remove the previous listener callback
        player.setOnPreparedListener(null);
        player.prepareAsync();
    }

    private void resetProgressBar() {
        int duration = player.getDuration();
        durationTextView.setText(timePositionString(duration));
        progressBar.setMax(duration);
        updateProgressBar(0);
    }

    private void pausePlayer() {
        if (player.isPlaying()) player.pause();
        wasPlaying = false;
        setPlayerButtonPlay();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void playPlayer() {
        if (!player.isPlaying()) player.start();
        wasPlaying = true;
        setPlayerButtonPause();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void destroyPlayer() {
        if (player != null) {
            oldPosition = player.getCurrentPosition();
            playerLock.lock();
            player.release();
            player = null;
            playerLock.unlock();
        }
    }

    private void destroyExecutorService() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    @SuppressLint("DefaultLocale")
    private String timePositionString(int millis) {
        int minutes = millis / 1000 / 60;
        int seconds = millis / 1000 % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setPlayerButtonPlay() {
        playButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
    }

    private void setPlayerButtonPause() {
        playButton.setImageResource(R.drawable.ic_pause_white_24dp);
    }

    private void disableButton(ImageButton button) {
        button.setEnabled(false);
        button.setColorFilter(Color.rgb(100,100, 100));
    }

    private void enableButton(ImageButton button) {
        button.setEnabled(true);
        button.setColorFilter(Color.rgb(255,255,255));
    }

    private void beginTrack() {
        if (trackIndex == playlistSongs.size() - 1) {
            disableButton(skipNextButton);
        } else {
            enableButton(skipNextButton);
        }
        if (trackIndex == 0) {
            disableButton(skipPrevButton);
        } else {
            enableButton(skipPrevButton);
        }
        resetProgressBar();
        player.seekTo(oldPosition);
        oldPosition = 0;
        if (wasPlaying) playPlayer();
        else pausePlayer();
    }

    class AnimationController implements LifecycleObserver {
        private final static String TAG = "AnimationController";

        private volatile boolean shouldStop = false;
        private volatile int[] beats = null;
        private boolean started = false;
        private int beatIndex = 0;
        private int trackIndexCopy = -1;
        private volatile List<WorkoutCommand> commands = null;
        private volatile int[] commandPositions = null;
        private int commandIndex = -1;

        private ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        private AnimatorSet animAppear, animDisappear;
        private String instr_primary_string, instr_secondary_string;
        private Workout workout;


        AnimationController() {
            Animator[] animators = new Animator[]{
                    ObjectAnimator.ofFloat(instr_primary_textview, "alpha", 0f, 1.0f),
                    ObjectAnimator.ofFloat(instr_secondary_textview, "alpha", 0f, 1.0f)
            };
            animAppear = new AnimatorSet();
            animAppear.playTogether(animators);
            animAppear.setDuration(250);

            animators = new Animator[]{
                    ObjectAnimator.ofFloat(instr_primary_textview, "alpha", 1.0f, 0f),
                    ObjectAnimator.ofFloat(instr_secondary_textview, "alpha", 1.0f, 0f)
            };
            animDisappear = new AnimatorSet();
            animDisappear.playTogether(animators);
            animDisappear.setDuration(250);
            animDisappear.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    instr_primary_textview.setText(instr_primary_string);
                    instr_secondary_textview.setText(instr_secondary_string);
                    animAppear.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }

        private void changeText(String primary, String secondary, boolean animate) {
            instr_primary_string = primary;
            instr_secondary_string = secondary;
            if (animate) {
                runOnUiThread(() -> animDisappear.start());
            } else {
                runOnUiThread(() -> {
                    instr_primary_textview.setText(instr_primary_string);
                    instr_secondary_textview.setText(instr_secondary_string);
                });
            }
        }

        private boolean loadBeats() {
            beats = null;
            String beatsString = playlistSongs.get(trackIndexCopy).getBeatsInfo();
            if (beatsString == null) {
                return false;
            }
            beatIndex = 0;
            try {
                JSONObject beatsJSONObject = new JSONObject(beatsString);
                if (!beatsJSONObject.has("beats")) {
                    return false;
                }
                JSONArray beatsJSONArray = beatsJSONObject.getJSONArray("beats");
                beats = new int[beatsJSONArray.length()];
                for (int i = 0; i < beatsJSONArray.length(); i++) {
                    beats[i] = (int) ((double) beatsJSONArray.get(i) * 1000);
                }
                return true;
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }

        private void loadCommands() {
            commands = workout.commandsBySong.get(trackIndexCopy);
            // set commandIndex to -1 so that the workoutLoop is forced to
            // show the warmup message once
            commandIndex = -1;
            commandPositions = new int[commands.size()];
            for (int i = 0; i < commands.size(); i++) {
                commandPositions[i] = commands.get(i).positionMillis;
            }
        }

        private void onTrackChanged() {
            Log.w(TAG, "TRACK CHANGED - now " + playlistSongs.get(trackIndexCopy).getTitle());
            loadBeats();
            loadCommands();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void stop() {
            shouldStop = true;
        }

        private void safeSleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Log.w(TAG, e.toString());
            }
        }

        void start() {
            if (started) return;
            workout = new Workout(playlistSongs, workoutType);
            trackIndexLock.lock();
            trackIndexCopy = trackIndex;
            trackIndexLock.unlock();
            loadBeats();
            loadCommands();
            executorService.execute(this::beatsLoop);
            executorService.execute(this::workoutLoop);
            started = true;
        }

        private void beatsLoop() {
            while (!shouldStop) {
                int position = -1;
                try {
                    position = getPositionOrThrow();
                } catch (IllegalStateException e) {
                    safeSleep(100);
                    continue;
                }
                trackIndexLock.lock();
                if (trackIndexCopy != trackIndex) {
                    trackIndexCopy = trackIndex;
                    trackIndexLock.unlock();
                    onTrackChanged();
                    continue;
                }
                trackIndexLock.unlock();
                if (beats == null) {
                    // Beats info is missing. Wait until the track changes.
                    safeSleep(100);
                    continue;
                }
                int oldBeatIndex = beatIndex;
                if (position < beats[oldBeatIndex]) {
                    // seekTo() was called to an earlier position
                    beatIndex = bisectRight(beats, position);
                } else {
                    while (beatIndex < beats.length && beats[beatIndex] < position) {
                        beatIndex++;
                    }
                }
                if (beatIndex == oldBeatIndex || beatIndex == beats.length) {
                    safeSleep(100);
                    continue;
                }
                int beat = beats[beatIndex];
                safeSleep(beat - position);
                // TODO: display some visual signal on each beat
                //toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
            }
        }

        private void workoutLoop() {
            while (!shouldStop) {
                int position = -1;
                try {
                    position = getPositionOrThrow();
                } catch (IllegalStateException e) {
                    safeSleep(100);
                    continue;
                }
                trackIndexLock.lock();
                if (trackIndexCopy != trackIndex) {
                    trackIndexCopy = trackIndex;
                    trackIndexLock.unlock();
                    onTrackChanged();
                    continue;
                }
                trackIndexLock.unlock();
                int oldCommandIndex = commandIndex;
                // Note that commandPositions[0] must equal 0, and position must be non-negative
                if (oldCommandIndex != -1 && position < commandPositions[oldCommandIndex]) {
                    // seekTo() was called to an earlier position
                    while (commandIndex > 0 && position < commandPositions[commandIndex]) {
                        commandIndex--;
                    }
                } else {
                    while (commandIndex + 1 < commandPositions.length && commandPositions[commandIndex + 1] < position) {
                        commandIndex++;
                    }
                }
                if (commandIndex == oldCommandIndex) {
                    safeSleep(100);
                    continue;
                }
                WorkoutCommand command = commands.get(commandIndex);
                switch (command.commandType) {
                    case WARMUP:
                        changeText("WARM-UP", "Nice and steady...", true);
                        break;
                    case RIDEITOUT:
                        changeText("RIDE IT OUT", "Keep a steady pace", true);
                        break;
                    case COOLDOWN:
                        changeText("COOL DOWN", "Slow down, you're almost done", true);
                        break;
                    default:
                        startCountdown(commands, commandIndex, position);
                }
            }
        }

        private int getPositionOrThrow() throws IllegalStateException {
            playerLock.lock();
            try {
                // this will throw an IllegalStateException if the player is created,
                // but not initialized
                return player.getCurrentPosition();
            } catch (NullPointerException e) {
                throw new IllegalStateException();
            } finally {
                playerLock.unlock();
            }
        }

        private void startCountdown(List<WorkoutCommand> commands, int commandIndex, int position) {
            int nextCommandPosition = 0;
            String nextCommandType = null;
            WorkoutCommand command = commands.get(commandIndex);
            if (commandIndex < commands.size() - 1) {
                WorkoutCommand nextCommand = commands.get(commandIndex + 1);
                nextCommandPosition = nextCommand.positionMillis;
                nextCommandType = nextCommand.commandType.name();
            } else {
                nextCommandPosition = playlistSongs.get(trackIndexCopy).getDuration();
            }
            String primaryMsg = null;
            String secondaryMsg = null;
            switch (command.commandType) {
                case COUNTDOWN:
                    // TODO: SIT DOWN
                    primaryMsg = "GET READY";
                    secondaryMsg = String.format("%s will start in %%d seconds...",
                            nextCommandType.substring(0, 1) + nextCommandType.substring(1).toLowerCase());
                    break;
                case SPRINT:
                    primaryMsg = "SPRINT";
                    secondaryMsg = "Hold it for %d seconds";
                    break;
                case CLIMB:
                    primaryMsg = "STAND UP";
                    secondaryMsg = "Hold it for %d seconds";
                    break;
            }
            int minPosition = position;
            int maxPosition = nextCommandPosition;
            int prevSecRemaining = (nextCommandPosition - position) / 1000 + 2;
            while (!shouldStop && prevSecRemaining > 1) {
                try {
                    position = getPositionOrThrow();
                } catch (IllegalStateException e) {
                    safeSleep(100);
                    continue;
                }
                trackIndexLock.lock();
                if (trackIndexCopy != trackIndex) {
                    // Track was changed. Bubble back up to the main loop to reset the commandIndex.
                    trackIndexLock.unlock();
                    break;
                }
                trackIndexLock.unlock();
                if (position < minPosition || position > maxPosition) {
                    // seekTo() was called. Command has changed.
                    break;
                }
                int secRemaining = (int)Math.ceil((double)(nextCommandPosition - position) / 1000);
                if (secRemaining != prevSecRemaining) {
                    changeText(primaryMsg, String.format(secondaryMsg, secRemaining), false);
                    prevSecRemaining = secRemaining;
                }
                safeSleep(100);
            }
        }

        /**
         * Returns the first index i for which arr[i] >= elem. If elem is greater than the
         * last element of arr, arr.length is returned.
         * @param arr a sorted array of integers
         * @param elem an integer to search for
         * @return a number between [0, arr.length]
         */
        private int bisectRight(int[] arr, int elem) {
            int lo = 0, hi = arr.length;
            while (lo < hi) {
                int mid = (lo + hi) / 2;
                if (arr[mid] < elem) {
                    lo = mid + 1;
                } else {
                    hi = mid;
                }
            }
            return lo;
        }
    }
}
