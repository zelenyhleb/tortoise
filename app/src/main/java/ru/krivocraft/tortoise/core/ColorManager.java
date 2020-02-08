/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.core;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.res.ResourcesCompat;
import ru.krivocraft.tortoise.R;

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

    public static final String ACTION_RESULT_COLOR = "result_color";
    public static final String ACTION_REQUEST_COLOR = "request_color";
    public static final String EXTRA_COLOR = "color";

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

    public int getColorResource(int color) {
        return availableColors.get(color);
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
