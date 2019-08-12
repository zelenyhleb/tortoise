package ru.krivocraft.kbmp;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        List<Integer> quests = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> answers = Arrays.asList(10, 20, 30, 40, 50);

        long seed = System.nanoTime();
        Collections.shuffle(quests, new Random(seed));
        Collections.shuffle(answers, new Random(seed));

        System.out.println(quests);
        System.out.println(answers);
    }
}