package com.softteco.roadlabpro.algorithm;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * Created by ppp on 15.05.2015.
 */
public class IRITable {

    public static final int SOFT_SPN = 0;
    public static final int MEDIUM_SPN = 1;
    public static final int HARD_SPN = 2;
    public static final int FIXED_DEVICE = 0;
    public static final int NON_FIXED_DEVICE = 1;
    public static final double SD_THRESHOLD = 1;

    private float [][] interceptArray_2 = {
            // Fixed (Vertical)
            {
                    1.69459656f, // SOFT AUDI
                    1.258364f,   // MEDIUM FORD
                    1.879921f,   // HARD BMW (SUV)
            },
            // Non Fixed (Horizontal)
            {
                    2.045447f,  // SOFT AUDI
                    1.403616f,   // MEDIUM FORD
                    2.511125f,  // HARD BMW (SUV)
            }
    };

    private float [][] sdArray_2 = {
            // Fixed (Vertical)
            {
                    6.41623959f,  // SOFT AUDI
                    5.215198143f,  // MEDIUM FORD
                    5.852457679f,  // HARD BMW (SUV)
            },
            // Non Fixed (Horizontal)
            {
                    6.479570671f,  // SOFT AUDI
                    6.902960942f,   // MEDIUM FORD
                    6.177132368f,  // HARD BMW (SUV)
            }
    };

    private float [][] speedArray_2 = {
            // Fixed (Vertical)
            {
                    -0.02443565f,   // SOFT AUDI
                    -0.028562f,     // MEDIUM FORD
                    -0.01145f,      // HARD BMW (SUV)
            },
            // Non Fixed (Horizontal)
            {
                    -0.0291f,   // SOFT AUDI
                    -0.023693f,  // MEDIUM FORD
                    -0.03385f,  // HARD BMW (SUV)
            }
    };

    private float [][] sdArrayPow2_2 = {
            // Fixed (Vertical)
            {
                    0f,  // SOFT AUDI
                    0f,  // MEDIUM FORD
                    0f,  // HARD BMW (SUV)
            },
            // Non Fixed (Horizontal)
            {
                    0f,  // SOFT AUDI
                    0f,  // MEDIUM FORD
                    0f,  // HARD BMW (SUV)
            }
    };

    private float [][] sdSpeedArray_2 = {
            // Fixed (Vertical)
            {
                   -0.0347419f,     // SOFT AUDI
                   -0.020729451f,   // MEDIUM FORD
                   -0.03934122f,    // HARD BMW (SUV)
            },
            // Non Fixed (Horizontal)
            {
                   -0.032741179f,  // SOFT AUDI
                   -0.030271466f,  // MEDIUM FORD
                   -0.011064654f,  // HARD BMW (SUV)
            }
    };

    private PreferencesUtil.SUSPENSION_TYPES suspension = PreferencesUtil.SUSPENSION_TYPES.SOFT;

    public void init() {
        suspension = PreferencesUtil.
        getInstance(RAApplication.getInstance()).getSuspensionType();
    }

    private float[][] getInterceptArr(double sd) {
        return interceptArray_2;
    }

    private float[][] getSdArr(double sd) {
        return sdArray_2;
    }

    private float[][] getSpeedArr(double sd) {
        return speedArray_2;
    }

    private float[][] getSdPow2Arr(double sd) {
        return sdArrayPow2_2;
    }

    private float[][] getSdSpeedArr(double sd) {
        return sdSpeedArray_2;
    }

    public float getValue(float[][] array, boolean isFixed) {
        switch (suspension) {
            case SOFT:
                if (isFixed) {
                    return array[FIXED_DEVICE][SOFT_SPN];
                } else {
                    return array[NON_FIXED_DEVICE][SOFT_SPN];
                }
            case MEDIUM:
                if (isFixed) {
                    return array[FIXED_DEVICE][MEDIUM_SPN];
                } else {
                    return array[NON_FIXED_DEVICE][MEDIUM_SPN];
                }
            case HARD:
                if (isFixed) {
                    return array[FIXED_DEVICE][HARD_SPN];
                } else {
                    return array[NON_FIXED_DEVICE][HARD_SPN];
                }
            default:
                return 0;
        }
    }

    public float getIntercept(boolean isFixed, double sd) {
        float[][] interceptArray = getInterceptArr(sd);
        return getValue(interceptArray, isFixed);
    }

    public float getSd(boolean isFixed, double sd) {
        float[][] sdArray = getSdArr(sd);
        return getValue(sdArray, isFixed);
    }

    public float getSpeed(boolean isFixed, double sd) {
        float[][] speedArray = getSpeedArr(sd);
        return getValue(speedArray, isFixed);
    }

    public float getSdPow2(boolean isFixed, double sd) {
        float[][] sdArrayPow2 = getSdPow2Arr(sd);
        return getValue(sdArrayPow2, isFixed);
    }

    public float getSdSpeed(boolean isFixed, double sd) {
        float[][] sdSpeedArray = getSdSpeedArr(sd);
        return getValue(sdSpeedArray, isFixed);
    }
}
