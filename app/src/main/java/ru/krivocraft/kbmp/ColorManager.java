package ru.krivocraft.kbmp;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorManager {

    private final Context context;
    private final Random random;
    private final List<Integer> availableColors;

    public static final int RED = 0;
    public static final int PINK = 1;
    public static final int PURPLE = 2;
    public static final int DEEP_PURPLE = 3;
    public static final int INDIGO = 4;
    public static final int BLUE = 5;
    public static final int LIGHT_BLUE = 6;
    public static final int CYAN = 7;
    public static final int TEAL = 8;
    public static final int GREEN = 9;
    public static final int LIGHT_GREEN = 10;
    public static final int LIME = 11;
    public static final int YELLOW = 12;
    public static final int AMBER = 13;
    public static final int ORANGE = 14;
    public static final int DEEP_ORANGE = 15;

    public ColorManager(Context context) {
        this.context = context;
        this.random = new Random();
        this.availableColors = new ArrayList<>();

        fillAvailableColors();
    }

    public int getRandomColor() {
        return random.nextInt(16);
    }

    public int getColor(int color) {
        int colorResource = ResourcesCompat.getColor(context.getResources(), availableColors.get(color), null);
        return Color.rgb(Color.red(colorResource), Color.green(colorResource), Color.blue(colorResource));
    }

    private void fillAvailableColors() {
        availableColors.add(R.color.red700);
        availableColors.add(R.color.pink700);
        availableColors.add(R.color.purple700);
        availableColors.add(R.color.deep_purple700);
        availableColors.add(R.color.indigo700);
        availableColors.add(R.color.blue700);
        availableColors.add(R.color.light_blue700);
        availableColors.add(R.color.cyan700);
        availableColors.add(R.color.teal700);
        availableColors.add(R.color.green700);
        availableColors.add(R.color.light_green700);
        availableColors.add(R.color.lime700);
        availableColors.add(R.color.yellow700);
        availableColors.add(R.color.amber700);
        availableColors.add(R.color.orange00);
        availableColors.add(R.color.deep_orange700);
    }


}
