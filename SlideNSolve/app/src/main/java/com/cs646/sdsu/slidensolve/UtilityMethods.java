package com.cs646.sdsu.slidensolve;

import java.util.Random;

/**
 * Created by skyrev on 4/28/18.
 */

public class UtilityMethods {

    public static int[] randomize( int[] arr, int n)
    {
        Random r = new Random();
        for (int i = n-1; i > 0; i--) {
            int j = r.nextInt(i);

            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        return arr;
    }
}
