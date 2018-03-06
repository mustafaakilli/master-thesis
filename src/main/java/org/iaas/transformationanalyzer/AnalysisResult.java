package org.iaas.transformationanalyzer;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult
{
    private String name;
    private boolean possibility;
    private String difficulty;
    private int difficultyPoint;
    private List<String> possibleLosses;
    private List<String> difficultyCalcHistory;


    public AnalysisResult()
    {
        possibleLosses = new ArrayList<String>();
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean getPossibility()
    {
        return this.possibility;
    }

    public void setPossibility(boolean possibility)
    {
        this.possibility = possibility;
    }


    public String getDifficulty()
    {
        return this.difficulty;
    }

    public void setDifficulty(String difficulty)
    {
        this.difficulty = difficulty;
    }


    public int getDifficultyPoint()
    {
        return this.difficultyPoint;
    }

    public void setDifficultyPoint(int difficultyPoint)
    {
        this.difficultyPoint = difficultyPoint;
    }


    public List<String> getPossibleLosses()
    {
        return this.possibleLosses;
    }

    public void setPossibleLosses(List<String> possibleLosses)
    {
        this.possibleLosses = possibleLosses;
    }


    public List<String> getDifficultyCalcHistory()
    {
        return this.difficultyCalcHistory;
    }

    public void setDifficultyCalcHistory(List<String> difficultyCalcHistory)
    {
        this.difficultyCalcHistory = difficultyCalcHistory;
    }
}
