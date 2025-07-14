package com.example.aquasaver.challenges;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.aquasaver.model.ChallengeProgress;
import com.example.aquasaver.model.Challenges;

public class ChallengeWithProgress {

    @Embedded
    public Challenges challenge;

    @Relation(
            parentColumn = "title",         // from Challenges
            entityColumn = "title",         // from ChallengeProgress
            entity = ChallengeProgress.class
    )
    public ChallengeProgress progress;
}
