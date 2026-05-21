package com.facem_bani_inc.daily_history_server.utils;

public final class Constants {

    public static final String DAILY_CONTENT_BY_DATE = "dailyContentByDate";
    public static final String PRO_DAILY_CONTENT_BY_DATE = "proDailyContentByDate";
    public static final String GAMIFICATION_BY_USER_ID = "gamificationByUserId";
    public static final String LEADERBOARD = "leaderboard";
    public static final String GUEST_TOP_EVENT = "guestTopEvent";
    public static final String QUIZ_BY_EVENT_ID = "quizByEventId";

    public static final int XP_PER_CORRECT_ANSWER = 20;
    public static final int XP_PER_WRONG_ANSWER = 5;
    public static final int XP_PERFECT_SCORE_BONUS = 50;

    // Quiz reward: 100 XP per correct answer, plus a 400 XP bonus for a perfect run.
    public static final int XP_QUIZ_PER_CORRECT = 100;
    public static final int XP_QUIZ_PERFECT_BONUS = 400;

    // Language served when a quiz has no questions in the requested language.
    public static final String DEFAULT_QUIZ_LANGUAGE = "en";
}
