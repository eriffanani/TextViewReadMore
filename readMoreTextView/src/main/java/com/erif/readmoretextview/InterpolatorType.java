package com.erif.readmoretextview;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import static com.erif.readmoretextview.Interpolator.*;

@IntDef({
        DECELERATE,
        ACCELERATE,
        ANTICIPATE_OVERSHOOT,
        ANTICIPATE,
        BOUNCE,
        FAST_OUT_LINEAR_IN,
        FAST_OUT_SLOW_IN,
        LINEAR_OUT_SLOW_IN,
})
@Retention(RetentionPolicy.SOURCE)
public @interface InterpolatorType {
}
