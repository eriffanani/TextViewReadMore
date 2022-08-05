package com.erif.readmoretextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

public class TextViewReadMore extends AppCompatTextView {

    private static final String DEFAULT_EXPAND_TEXT = "Read More";
    private static final String DEFAULT_COLLAPSE_TEXT = "Close";

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
    private int maxLines = 2;

    private int halfHeight = 0;
    private int fullHeight = 0;
    private boolean isAnimate = false;
    private boolean isEllipsized = false;
    private int animationDuration = 200;
    private ToggleListener toggleListener;

    private SpannableStringBuilder spanCollapsed;
    private SpannableStringBuilder spanExpanded;

    private View.OnClickListener onClickExpand;
    private View.OnClickListener onClickCollapse;
    private int actionClickColor = 0;

    private static long mLastClickTime = 0;

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
                int getMaxLines = typedArray.getInt(R.styleable.TextViewReadMore_readMoreMaxLines, 2);
                maxLines = Math.max(getMaxLines, 1);
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

                int getAnimationDuration = typedArray.getInt(R.styleable.TextViewReadMore_android_animationDuration, animationDuration);
                if (getAnimationDuration > 1000) {
                    animationDuration = 1000;
                } else animationDuration = Math.max(getAnimationDuration, 100);
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

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isAnimate) {
            rebuild = false;
        } else {
            if (text instanceof Spanned) {
                rebuild = false;
            } else {
                this.text = text.toString();
                rebuild = true;
            }
        }
        super.setText(text, type);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);
        lineWidth = givenWidth - getCompoundPaddingStart() - getCompoundPaddingEnd();
        buildSpan();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void buildSpan() {
        if (!TextUtils.isEmpty(text)) {
            isEllipsized = isEllipsized(text);
            if (isEllipsized) {
                String fullText = text+collapseText;
                StaticLayout.Builder staticFull = StaticLayout.Builder
                        .obtain(fullText, 0, fullText.length(), getPaint(), lineWidth)
                        .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier());
                fullHeight = staticFull.build().getHeight() + getCompoundPaddingTop() + getCompoundPaddingBottom();

                StaticLayout.Builder staticHalf = StaticLayout.Builder
                        .obtain(text, 0, text.length(), getPaint(), lineWidth)
                        .setMaxLines(maxLines)
                        .setEllipsize(TextUtils.TruncateAt.END)
                        .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier());
                halfHeight = staticHalf.build().getHeight() + getCompoundPaddingTop() + getCompoundPaddingBottom();
                if (rebuild) {
                    if (collapsed) {
                        collapsedBuilder();
                        setText(spanCollapsed);
                    } else {
                        expandBuilder();
                        setText(spanExpanded);
                        ViewGroup.LayoutParams params = getLayoutParams();
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        setLayoutParams(params);
                    }
                    setMovementMethod(LinkMovementMethod.getInstance());
                    rebuild = false;
                }
            }
        }
    }

    private boolean isEllipsized(String text) {
        StaticLayout.Builder layout = StaticLayout.Builder
                .obtain(text, 0, text.length(), getPaint(), lineWidth)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier());
        int lines = maxLines - 1;
        return layout.build().getEllipsisCount(lines) > 0;
    }

    private void collapsedBuilder() {
        StaticLayout staticLayout = StaticLayout.Builder
                .obtain(text, 0, text.length(), getPaint(), lineWidth)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                .build();
        int sumOfLw = 0;
        for (int i=0; i<staticLayout.getLineCount(); i++) {
            int count = (int) staticLayout.getLineWidth(i);
            sumOfLw+=count;
        }
        float expandActionWidth = getPaint().measureText(expandText);
        float doubleExpandWith = expandActionWidth * 2;
        float truncatedTextWidth = sumOfLw - expandActionWidth;
        if (sumOfLw < doubleExpandWith) {
            truncatedTextWidth = sumOfLw;
        }
        CharSequence truncatedText = TextUtils.ellipsize(text, getPaint(), truncatedTextWidth, TextUtils.TruncateAt.END);
        spanCollapsed = spanCollapsed(truncatedText+expandText);
    }

    private SpannableStringBuilder spanCollapsed(String text) {
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
                            () -> onClickExpand.onClick(widget), 150
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

    private void expandBuilder() {
        String fullText = text+collapseText;
        spanExpanded = spanExpanded(fullText);
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
                if (onClickCollapse != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> onClickCollapse.onClick(widget), 150
                    );
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

    public void toggle() {
        if (SystemClock.elapsedRealtime() - mLastClickTime <= animationDuration) { // 1000 = 1second
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        setMovementMethod(null);
        int start = collapsed ? halfHeight : fullHeight;
        int end = collapsed ? fullHeight : halfHeight;
        ValueAnimator anim = ValueAnimator.ofInt(start, end);
        anim.setDuration(animationDuration);
        ViewGroup.LayoutParams params = getLayoutParams();
        anim.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimate = true;
                if (collapsed)
                    setText(text, BufferType.SPANNABLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimate = false;
                collapsed = !collapsed;
                rebuild = true;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (toggleListener != null) toggleListener.onToggle(collapsed);
                }, 100);
            }
        });
        if (isEllipsized)
            anim.start();
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

    private void debug(String message) {
        Log.d("TextViewReadMore", message);
    }

    public interface ToggleListener {
        public void onToggle(boolean collapsed);
    }

}
