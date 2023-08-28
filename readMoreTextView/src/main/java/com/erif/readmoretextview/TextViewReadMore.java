package com.erif.readmoretextview;

import static java.lang.Math.max;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class TextViewReadMore extends AppCompatTextView {

    private static final String DEFAULT_EXPAND_TEXT = "Read More";
    private static final String DEFAULT_COLLAPSE_TEXT = "Close";
    private static final String DOTS_CODE = "…"; // u2026
    private static final String SPACE_CODE = "\u00A0";

    private static final int ELLIPSIS_TYPE_DOTS = 0;
    private static final int ELLIPSIS_TYPE_NONE = 1;

    private String text;

    private String expandText = DEFAULT_EXPAND_TEXT;
    private int expandTextColor;
    private int expandTextStyle = 0;
    private boolean expandTextUnderline = false;

    private String collapseText = DEFAULT_COLLAPSE_TEXT;
    private int collapseTextColor;
    private int collapseTextStyle = 0;
    private boolean collapseTextUnderline = false;

    private boolean collapsed;
    private boolean rebuild = true;
    private int lineWidth = 0;
    private int maxLines = 1;

    private int halfHeight = 0;
    private int fullHeight = 0;
    private boolean isAnimate = false;
    private boolean isEllipsized = false;
    private int durationExpand = 300;
    private int durationCollapse = 300;
    private ToggleListener toggleListener;

    private View.OnClickListener onClickExpand;
    private View.OnClickListener onClickCollapse;
    private int actionClickColor = 0;
    private int ellipsisType = ELLIPSIS_TYPE_DOTS;

    private SpannableStringBuilder spanCollapsed;
    private SpannableStringBuilder spanExpanded;

    private static long mLastClickTime = 0;

    private int interpolatorExpand = 0;
    private int interpolatorCollapse = 0;

    public TextViewReadMore(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public TextViewReadMore(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TextViewReadMore(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(
                    attrs, R.styleable.TextViewReadMore, defStyleAttr, 0
            );
            try {
                text = typedArray.getString(R.styleable.TextViewReadMore_android_text);
                int getMaxLines = typedArray.getInt(R.styleable.TextViewReadMore_readMoreMaxLines, maxLines);
                maxLines = max(getMaxLines, maxLines);
                collapsed = typedArray.getBoolean(R.styleable.TextViewReadMore_collapsed, true);

                String getExpandText = typedArray.getString(R.styleable.TextViewReadMore_expandText);
                expandText = TextUtils.isEmpty(getExpandText) ? DEFAULT_EXPAND_TEXT : getExpandText;
                expandTextColor = typedArray.getColor(R.styleable.TextViewReadMore_expandTextColor, Color.BLUE);
                expandTextStyle = typedArray.getInt(R.styleable.TextViewReadMore_expandTextStyle, 0);
                expandTextUnderline = typedArray.getBoolean(R.styleable.TextViewReadMore_expandTextUnderline, expandTextUnderline);

                String getCollapseText = typedArray.getString(R.styleable.TextViewReadMore_collapseText);
                collapseText = getCollapseText == null ? DEFAULT_COLLAPSE_TEXT : getCollapseText;
                collapseTextColor = typedArray.getColor(R.styleable.TextViewReadMore_collapseTextColor, Color.BLUE);
                collapseTextStyle = typedArray.getInt(R.styleable.TextViewReadMore_collapseTextStyle, 0);
                collapseTextUnderline = typedArray.getBoolean(R.styleable.TextViewReadMore_collapseTextUnderline, collapseTextUnderline);

                int defaultActionClickColor = ContextCompat.getColor(context, R.color.text_view_read_more_button_hover_color);
                actionClickColor = typedArray.getColor(R.styleable.TextViewReadMore_actionClickColor, defaultActionClickColor);

                int getDuration = typedArray.getInt(R.styleable.TextViewReadMore_duration, 300);
                durationExpand = typedArray.getInt(R.styleable.TextViewReadMore_durationExpand, getDuration);
                durationCollapse = typedArray.getInt(R.styleable.TextViewReadMore_durationCollapse, getDuration);

                ellipsisType = typedArray.getInt(R.styleable.TextViewReadMore_ellipsisType, ELLIPSIS_TYPE_DOTS);

                int interpolator = typedArray.getInt(R.styleable.TextViewReadMore_interpolator, 0);
                interpolatorExpand = typedArray.getInt(R.styleable.TextViewReadMore_interpolatorExpand, interpolator);
                interpolatorCollapse = typedArray.getInt(R.styleable.TextViewReadMore_interpolatorCollapse, interpolator);

            } finally {
                typedArray.recycle();
            }
            setHighlightColor(Color.TRANSPARENT);
        }
    }

    public void collapsed(boolean collapsed) {
        this.collapsed = collapsed;
        rebuild = true;
    }

    public boolean isCollapsed() {
        return this.collapsed;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isAnimate) {
            rebuild = false;
        } else {
            if (text instanceof Spanned) {
                rebuild = false;
            } else {
                if (text != null) {
                    this.text = text.toString();
                } else {
                    this.text = null;
                }
                rebuild = true;
            }
        }
        if (isEllipsized(text)) {
            if (spanExpanded != null && !collapsed) {
                super.setText(spanExpanded, type);
            } else if (spanCollapsed != null && collapsed) {
                super.setText(spanCollapsed, type);
            } else {
                super.setText(text, type);
            }
        } else {
            super.setText(text, type);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sidePadding = getCompoundPaddingStart() + getCompoundPaddingEnd();
        lineWidth = givenWidth - sidePadding;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rebuild)
            buildSpan();
    }

    private void buildSpan() {
        if (!TextUtils.isEmpty(text)) {
            isEllipsized = isEllipsized(text);
            if (isEllipsized) {
                if (rebuild) {
                    if (collapsed) {
                        StaticLayout layoutDefault = getStaticLayoutCollapsed(text);
                        int sumLineWidth = 0;
                        /* Unused
                        CharSequence lastLineLetter = null;
                        int lastLineLetterCount = 0;
                        */
                        for (int i = 0; i < maxLines; i++) {
                            int count = (int) layoutDefault.getLineWidth(i);
                            /* Unused
                            int start = layoutDefault.getLineStart(i);
                            int end = layoutDefault.getLineEnd(i);
                            lastLineLetter = text.subSequence(start, end);
                            lastLineLetterCount = lastLineLetter.length();
                            */
                            sumLineWidth += count;
                        }

                        float expandActionWidth = getPaint().measureText(" " + expandText);
                        float doubleExpandWith = expandActionWidth * 2;
                        /* Unused
                        if (lastLineLetterCount < 3) {
                            if (lastLineLetter != null) {
                                String lastChar = lastLineLetter.toString().replaceAll("\n", "");
                                float lastLineLetterAdd = getPaint().measureText(lastChar);
                                sumLineWidth += lastLineLetterAdd;
                            }
                        }
                        */

                        float truncatedTextWidth = sumLineWidth - expandActionWidth;
                        if (sumLineWidth < doubleExpandWith) {
                            truncatedTextWidth = sumLineWidth;
                        }

                        CharSequence truncatedText = TextUtils.ellipsize(text, getPaint(), truncatedTextWidth, TextUtils.TruncateAt.END);
                        Log.d("Collapse", truncatedText.toString());
                        String exp = expandText.replaceAll(" ", SPACE_CODE);
                        String finalText = truncatedText.toString();
                        if (ellipsisType == ELLIPSIS_TYPE_NONE)
                            finalText = truncatedText.toString().replace(DOTS_CODE, "");
                        String collapsedText = finalText + SPACE_CODE + exp;
                        StaticLayout layout = getStaticLayoutCollapsed(collapsedText);
                        spanCollapsed = spanCollapsed(collapsedText);
                        if (spanCollapsed != null) {
                            updateParam(layout.getHeight());
                            setText(spanCollapsed);
                        }
                        spanExpanded = spanExpanded();
                    } else {
                        spanExpanded = spanExpanded();
                        StaticLayout layout = getStaticLayout(spanExpanded.toString());
                        updateParam(layout.getHeight());
                        setText(spanExpanded);
                    }
                    if (isJustified()) {
                        setOnTouchListener(onTouchEvent());
                    } else {
                        setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    rebuild = false;
                }
            } else {
                rebuild = false;
                StaticLayout layout = getStaticLayout(text);
                post(() -> updateParam(layout.getHeight()));
            }
        }
    }

    private boolean isEllipsized(@Nullable CharSequence charSequence) {
        if (charSequence == null)
            return false;
        StaticLayout layout = getStaticLayout(charSequence.toString());
        return layout.getLineCount() > maxLines;
    }

    private boolean isEllipsized(@Nullable String text) {
        if (text == null)
            return false;
        StaticLayout layout = getStaticLayout(text);
        return layout.getLineCount() > maxLines;
    }

    private SpannableStringBuilder spanCollapsed(String text) {
        if (text == null)
            return null;
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        int start = text.length() - expandText.length();
        int end = text.length();
        span.setSpan(
                new ForegroundColorSpan(expandTextColor), start,
                end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        if (expandTextStyle == 1) {
            boldText(span, start, end);
        } else if (expandTextStyle == 2) {
            italicText(span, start, end);
        } else if (expandTextStyle == 3) {
            boldText(span, start, end);
            italicText(span, start, end);
        }
        if (expandTextUnderline) {
            underlineText(span, start, end);
        }
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (onClickExpand != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> onClickExpand.onClick(widget), 100
                    );
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                if (!expandTextUnderline)
                    ds.setUnderlineText(false);
                if (isPressed()) {
                    ds.setColor(actionClickColor);
                } else {
                    ds.setColor(expandTextColor);
                }
                invalidate();
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return span;
    }

    private SpannableStringBuilder spanExpanded() {
        String collapsedTextSpace = collapseText.replaceAll(" ", SPACE_CODE);
        String fullText = text + SPACE_CODE + collapsedTextSpace;
        spanExpanded = spanExpanded(fullText);
        return spanExpanded;
    }

    private SpannableStringBuilder spanExpanded(String text) {
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        int start = text.length() - collapseText.length();
        int end = text.length();
        span.setSpan(
                new ForegroundColorSpan(collapseTextColor), start,
                end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        if (collapseTextStyle == 1) {
            boldText(span, start, end);
        } else if (collapseTextStyle == 2) {
            italicText(span, start, end);
        } else if (collapseTextStyle == 3) {
            boldText(span, start, end);
            italicText(span, start, end);
        }
        if (collapseTextUnderline) {
            underlineText(span, start, end);
        }
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (!collapseText.isEmpty() && !collapseText.equals(" ")) {
                    if (onClickCollapse != null) {
                        new Handler(Looper.getMainLooper()).postDelayed(
                                () -> onClickCollapse.onClick(widget), 100
                        );
                    }
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                if (!collapseTextUnderline)
                    ds.setUnderlineText(false);
                if (isPressed()) {
                    ds.setColor(actionClickColor);
                } else {
                    ds.setColor(collapseTextColor);
                }
                invalidate();
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return span;
    }

    @SuppressLint("RestrictedApi")
    public void toggle() {
        int limitDuration = collapsed ? durationExpand : durationCollapse;
        int limit = max(limitDuration, 1000);
        if (SystemClock.elapsedRealtime() - mLastClickTime <= limit) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        setMovementMethod(null);

        if (collapsed) {
            String collapsedTextSpace = collapseText.replaceAll(" ", SPACE_CODE);
            String fullText = text+SPACE_CODE+collapsedTextSpace;

            StaticLayout layoutFull = getStaticLayout(fullText);
            fullHeight = layoutFull.getHeight() + getPaddingTop() + getPaddingBottom();
        } else {
            StaticLayout layoutCollapsed = getStaticLayoutCollapsed(text);
            halfHeight = layoutCollapsed.getHeight() + getPaddingTop() + getPaddingTop();
        }
        int end = collapsed ? fullHeight : halfHeight;
        ValueAnimator anim = ValueAnimator.ofInt(getHeight(), end);
        anim.setInterpolator(collapsed ? animationExpand() : animationCollapse());
        anim.setDuration(collapsed ? durationExpand : durationCollapse);
        ViewGroup.LayoutParams params = getLayoutParams();
        anim.addUpdateListener(animation -> {
            Object value = animation.getAnimatedValue();
            params.height = (int) value;
            setLayoutParams(params);
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimate = true;
                if (collapsed) {
                    collapsed = false;
                    setText(spanExpanded);
                    collapsed = true;
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimate = false;
                collapsed = !collapsed;
                rebuild = true;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (toggleListener != null)
                        toggleListener.onToggle(collapsed);
                }, 0);
            }
        });
        if (isEllipsized)
            anim.start();
    }

    public void setInterpolator(@InterpolatorType int interpolator) {
        this.interpolatorExpand = interpolator;
        this.interpolatorCollapse = interpolator;
    }

    public void setInterpolatorExpand(@InterpolatorType int interpolator) {
        this.interpolatorExpand = interpolator;
    }

    public void setInterpolatorCollapse(@InterpolatorType int interpolator) {
        this.interpolatorCollapse = interpolator;
    }

    private TimeInterpolator animationExpand() {
        return animation(interpolatorExpand);
    }

    private TimeInterpolator animationCollapse() {
        return animation(interpolatorCollapse);
    }

    private TimeInterpolator animation(int value) {
        return switch (value) {
            case 1 -> new AccelerateInterpolator();
            case 2 -> new AnticipateOvershootInterpolator();
            case 3 -> new AnticipateInterpolator();
            case 4 -> new BounceInterpolator();
            case 5 -> new FastOutLinearInInterpolator();
            case 6 -> new FastOutSlowInInterpolator();
            case 7 -> new LinearOutSlowInInterpolator();
            default -> new DecelerateInterpolator();
        };
    }

    private void italicText(SpannableStringBuilder builder, int start, int end) {
        builder.setSpan(new StyleSpan(Typeface.ITALIC),
                start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void boldText(SpannableStringBuilder builder, int start, int end) {
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void underlineText(SpannableStringBuilder builder, int start, int end) {
        builder.setSpan(new UnderlineSpan(),
                start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    public void toggleListener(ToggleListener toggleListener) {
        this.toggleListener = toggleListener;
    }
    public void onClickExpand(View.OnClickListener onClickExpand) {
        this.onClickExpand = onClickExpand;
    }
    public void onClickCollapse(View.OnClickListener onClickCollapse) {
        this.onClickCollapse = onClickCollapse;
    }

    public void setDuration(long millis) {
        int value = (int) millis;
        this.durationExpand = value;
        this.durationCollapse = value;
    }

    public void setDuration(double seconds) {
        int value = (int) seconds * 1000;
        this.durationExpand = value;
        this.durationCollapse = value;
    }

    public void setDurationExpand(long millis) {
        this.durationExpand = (int) millis;
    }

    public void setDurationExpand(double seconds) {
        this.durationExpand = (int) seconds * 1000;
    }

    public void setDurationCollapse(long millis) {
        this.durationCollapse = (int) millis;
    }

    public void setDurationCollapse(double seconds) {
        this.durationCollapse = (int) seconds * 1000;
    }

    private String ellipsize(String text, int size) {
        if (text.isEmpty() || size <= 0) {
            return "";
        } else if (text.length() <= size) {
            return text;
        } else {
            return text.substring(0, max(size - 1, 0))+"...";
        }
    }

    /*
    Unused function
    private void debug(String message) {
        Log.d("TextViewReadMore", message);
    }

    private String textReplaceSpace() {
        return text.replaceAll(" ", SPACE_CODE);
    }
    */

    private void updateParam(int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        int paddings = getPaddingTop() + getPaddingBottom();
        params.height = height + paddings;
        setLayoutParams(params);
    }

    private StaticLayout getStaticLayout(@Nullable String source) {
        String mSource = source == null ? "" : source;
        StaticLayout layout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout = StaticLayout.Builder
                    .obtain(mSource, 0, mSource.length(), getPaint(), lineWidth)
                    .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                    .setIncludePad(getIncludeFontPadding())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .build();
            /* Unused
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setJustificationMode(getJustificationMode());
            }
            layout = builder.build();*/
        } else {
            layout = new StaticLayout(mSource, getPaint(), lineWidth, Layout.Alignment.ALIGN_NORMAL,
                    getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding()
            );
        }
        return layout;
    }

    private StaticLayout getStaticLayoutCollapsed(@Nullable String source) {
        String mSource = source == null ? "" : source;
        StaticLayout layout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout = StaticLayout.Builder
                    .obtain(mSource, 0, mSource.length(), getPaint(), lineWidth)
                    .setMaxLines(maxLines)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                    .setIncludePad(getIncludeFontPadding())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .build();
            /* Unused
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setJustificationMode(getJustificationMode());
            }
            layout = builder.build();*/
        } else {
            int maxLength = mSource.length();
            do {
                layout = new StaticLayout(
                        ellipsize(mSource, maxLength), getPaint(), lineWidth, Layout.Alignment.ALIGN_NORMAL,
                        getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding()
                );
                maxLength -= 10;
            } while (layout.getLineCount() > 2);
        }
        return layout;
    }

    private boolean isJustified() {
        boolean isJustified = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getJustificationMode() == LineBreaker.JUSTIFICATION_MODE_INTER_WORD) {
                isJustified = true;
            }
        }
        return isJustified;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener onTouchEvent() {
        return (v, event) -> {
            if(v instanceof TextView tv){
                if(event.getAction() == MotionEvent.ACTION_UP){
                    int x = (int) (event.getX() - tv.getTotalPaddingLeft() + tv.getScrollX());
                    int y = (int) (event.getY() - tv.getTotalPaddingTop() + tv.getScrollY());
                    int line = tv.getLayout().getLineForVertical(y);
                    int offset = tv.getLayout().getOffsetForHorizontal(line, x);
                    if(tv.getText() instanceof Spanned spannable) {
                        ClickableSpan[] links = spannable.getSpans(offset, offset, ClickableSpan.class);
                        if (links.length > 0) {
                            links[0].onClick(tv);
                        }
                    }
                }
            }
            return true;
        };
    }

}
