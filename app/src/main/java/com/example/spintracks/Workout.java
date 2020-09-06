package com.example.spintracks;

import com.example.spintracks.dal.Song;

import java.util.ArrayList;
import java.util.List;

import static com.example.spintracks.Workout.WorkoutCommandType.*;

public class Workout {
    public enum WorkoutCommandType {
        WARMUP, COUNTDOWN, SPRINT, CLIMB, RIDEITOUT, COOLDOWN
    }

    public class WorkoutCommand {
        public WorkoutCommandType commandType;
        public int positionMillis;

        WorkoutCommand(WorkoutCommandType commandType, int positionMillis) {
            this.commandType = commandType;
            this.positionMillis = positionMillis;
        }
    }

    public List<List<WorkoutCommand>> commandsBySong;
    private String workoutType;

    public Workout(List<? extends Song> songs, String workoutType) {
        this.workoutType = workoutType;
        commandsBySong = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            int durationMillis = song.getDuration();
            // we should have some special case if the song is < 30s long
            ArrayList<WorkoutCommand> commands = new ArrayList<>();
            int songPosition = 0;
            if (i == 0) {
                int warmUpDuration = Math.min(durationMillis, 15000);
                commands.add(new WorkoutCommand(WARMUP, 0));
                songPosition += warmUpDuration;
            }
            if (i == songs.size() - 1) {
                // save the last 15s of the last song for a cooldown
                durationMillis -= Math.min(durationMillis - songPosition, 15000);
            }
            while (songPosition + 15000 < durationMillis) {
                // Add a climb/sprint sequence
                double randomValue = Math.random();
                int minSequenceDuration = Math.min(durationMillis - songPosition, 60000);
                int maxSequenceDuration = Math.min(durationMillis - songPosition, 90000);
                int sequenceDuration = (int) (minSequenceDuration + randomValue * (maxSequenceDuration - minSequenceDuration));
                // round down to the nearest second
                sequenceDuration -= sequenceDuration % 1000;
                if (durationMillis - (songPosition + sequenceDuration) < 5000) {
                    // if there's less than 5s left, just extend it all the way to the end
                    sequenceDuration = durationMillis - songPosition;
                }
                WorkoutCommandType commandType = CLIMB;
                if (randomValue > 0.5 && !(i == 0 && songPosition < 60000)) {
                    // don't sprint within the first minute of the first song
                    commandType = SPRINT;
                }
                addSequence(commands, commandType, songPosition, sequenceDuration);
                songPosition += sequenceDuration;

                // Add a short cool-off period before the next sequence
                if (songPosition < durationMillis) {
                    int coolOffDuration = 5000;
                    commands.add(new WorkoutCommand(RIDEITOUT, songPosition));
                    songPosition += coolOffDuration;
                }
            }
            if (i == songs.size() - 1) {
                commands.add(new WorkoutCommand(COOLDOWN, songPosition));
            }
            commandsBySong.add(commands);
        }

    }

    private void addSequence(ArrayList<WorkoutCommand> commands, WorkoutCommandType commandType,
                             int songPosition, int sequenceDuration) {
        // sequenceDuration must be between 15000 and 90000
        // commandType must be CLIMB or SPRINT

        // Each interval must be at least 5 seconds
        // Each countdown must be at least 5 seconds
        // We don't want more than 3 intervals in a sequence
        int numIntervals = Math.min(3, sequenceDuration / 10000);
        ArrayList<Integer> intervals = new ArrayList<>();
        if (commandType == SPRINT) {
            // consecutive sprints have decreasing length
            int intervalLength = sequenceDuration / numIntervals;
            intervalLength -= intervalLength % 1000;
            for (int i = 0; i < numIntervals; i++) {
                intervals.add(intervalLength);
            }
            int intervalDiff = Math.min(5000, Math.max(0, intervalLength - 10000));
            // the first interval should be longest, the last should be shortest
            intervals.set(0, intervals.get(0) + intervalDiff);
            intervals.set(numIntervals - 1, intervals.get(numIntervals - 1) - intervalDiff);
        } else {
            int cumsum = 0;
            for (int i = 0; i < numIntervals; i++) {
                int intervalLength = sequenceDuration * (i + 1) / numIntervals - cumsum;
                // round down to nearest second
                intervalLength -= intervalLength % 1000;
                intervals.add(intervalLength);
                cumsum += intervalLength;
            }
        }
        for (int intervalLength : intervals) {
            int divisor;
            if (workoutType.equals("EASY")) divisor = 2;
            else if (workoutType.equals("MEDIUM")) divisor = 3;
            else divisor = 4;
            int countdownLength = Math.max(3000, Math.min(10000, intervalLength / divisor));
            countdownLength -= countdownLength % 1000;
            int commandLength = intervalLength - countdownLength;
            commands.add(new WorkoutCommand(COUNTDOWN, songPosition));
            songPosition += countdownLength;
            commands.add(new WorkoutCommand(commandType, songPosition));
            songPosition += commandLength;
        }
    }
}
