package com.erif.readmoretextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

public class TextViewReadMore extends AppCompatTextView {

    private static final String DEFAULT_EXPAND_TEXT = "Read More";
    private static final String DEFAULT_COLLAPSE_TEXT = "Close";
    private static final String DOTS_CODE = "\u2026";
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
    private int animationDuration = 200;
    private ToggleListener toggleListener;

    private SpannableStringBuilder spanCollapsed;
    private SpannableStringBuilder spanExpanded;

    private View.OnClickListener onClickExpand;
    private View.OnClickListener onClickCollapse;
    private int actionClickColor = 0;
    private int ellipsisType = ELLIPSIS_TYPE_DOTS;

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
                int getMaxLines = typedArray.getInt(R.styleable.TextViewReadMore_readMoreMaxLines, maxLines);
                maxLines = Math.max(getMaxLines, maxLines);
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
                animationDuration = Math.max(getAnimationDuration, 100);

                ellipsisType = typedArray.getInt(R.styleable.TextViewReadMore_ellipsisType, ELLIPSIS_TYPE_DOTS);

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
                if (rebuild) {
                    if (collapsed) {
                        collapsedBuilder();
                        setText(spanCollapsed);
                    } else {
                        expandBuilder();
                        setText(spanExpanded);
                        setWrapLayout();
                    }
                    setMovementMethod(LinkMovementMethod.getInstance());
                    rebuild = false;
                }
            } else {
                setWrapLayout();
            }
        }
    }

    private boolean isEllipsized(String text) {
        StaticLayout layout;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout = StaticLayout.Builder
                    .obtain(text, 0, text.length(), getPaint(), lineWidth)
                    .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                    .build();
        } else {
            layout = new StaticLayout(text, getPaint(), lineWidth, Layout.Alignment.ALIGN_NORMAL,
                    getLineSpacingMultiplier(), getLineSpacingExtra(), true
            );
        }
        return layout.getLineCount() > maxLines;
    }

    private void collapsedBuilder() {
        StaticLayout layout;
        //String replaceSpace = textReplaceSpace();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layout = StaticLayout.Builder
                    .obtain(text, 0, text.length(), getPaint(), lineWidth)
                    .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                    .build();
        } else {
            layout = new StaticLayout(text, getPaint(), lineWidth, Layout.Alignment.ALIGN_NORMAL,
                    getLineSpacingMultiplier(), getLineSpacingExtra(), true
            );
        }
        int sumLineWidth = 0;
        CharSequence lastLineLetter = null;
        int lastLineLetterCount = 0;
        for (int i=0; i<maxLines; i++) {
            int count = (int) layout.getLineWidth(i);
            int start = layout.getLineStart(i);
            int end = layout.getLineEnd(i);
            lastLineLetter = text.subSequence(start, end);
            lastLineLetterCount = lastLineLetter.length();
            sumLineWidth+=count;
        }
        float expandActionWidth = getPaint().measureText(" "+expandText);
        float doubleExpandWith = expandActionWidth * 2;

        if (lastLineLetterCount < 3) {
            if (lastLineLetter != null) {
                String lastChar = lastLineLetter.toString().replaceAll("\n", "");
                float lastLineLetterAdd = getPaint().measureText(lastChar);
                sumLineWidth+=lastLineLetterAdd;
            }
        }

        float truncatedTextWidth = sumLineWidth - expandActionWidth;
        if (sumLineWidth < doubleExpandWith) {
            truncatedTextWidth = sumLineWidth;
        }
        CharSequence truncatedText = TextUtils.ellipsize(text, getPaint(), truncatedTextWidth, TextUtils.TruncateAt.END);
        String exp = expandText.replaceAll(" ", SPACE_CODE);
        String finalText = truncatedText.toString();
        if (ellipsisType == ELLIPSIS_TYPE_NONE) {
            finalText = truncatedText.toString().replace(DOTS_CODE, "");
        }

        spanCollapsed = spanCollapsed(finalText+SPACE_CODE+exp);
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
        //String replaceSpace = textReplaceSpace();
        String collapsedTextSpace = collapseText.replaceAll(" ", SPACE_CODE);
        String fullText = text+SPACE_CODE+collapsedTextSpace;
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

        if (collapsed) {
            String collapsedTextSpace = collapseText.replaceAll(" ", SPACE_CODE);
            String fullText = text+SPACE_CODE+collapsedTextSpace;

            StaticLayout staticFull;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                staticFull = StaticLayout.Builder
                        .obtain(fullText, 0, fullText.length(), getPaint(), lineWidth)
                        .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                        .setIncludePad(getIncludeFontPadding())
                        .build();
            } else {
                staticFull = new StaticLayout(
                        fullText, getPaint(), lineWidth, Layout.Alignment.ALIGN_NORMAL,
                        getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding()
                );
            }
            fullHeight = staticFull.getHeight() + getPaddingTop() + getPaddingBottom();
        } else {
            StaticLayout staticHalf;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                staticHalf = StaticLayout.Builder
                        .obtain(text, 0, text.length(), getPaint(), lineWidth)
                        .setMaxLines(maxLines)
                        .setEllipsize(TextUtils.TruncateAt.END)
                        .setLineSpacing(getLineSpacingExtra(), getLineSpacingMultiplier())
                        .setIncludePad(getIncludeFontPadding())
                        .build();
            } else {
                int maxLength = text.length();
                do {
                    staticHalf = new StaticLayout(
                            ellipsize(text, maxLength), getPaint(), lineWidth, Layout.Alignment.ALIGN_NORMAL,
                            getLineSpacingMultiplier(), getLineSpacingExtra(), getIncludeFontPadding()
                    );
                    maxLength -= 10;
                } while (staticHalf.getLineCount() > 2);
            }
            halfHeight = staticHalf.getHeight() + getPaddingTop() + getPaddingTop();
        }
        int end = collapsed ? fullHeight : halfHeight;
        ValueAnimator anim = ValueAnimator.ofInt(getHeight(), end);
        anim.setDuration(animationDuration);
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
                    setText(text, BufferType.SPANNABLE);
                }
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

    private String ellipsize(String text, int size) {
        if (text.isEmpty() || size <= 0) {
            return "";
        } else if (text.length() <= size) {
            return text;
        } else {
            return text.substring(0, Math.max(size - 1, 0))+"...";
        }
    }

    /* Unused function
    private void debug(String message) {
        Log.d("TextViewReadMore", message);
    }
    private String textReplaceSpace() {
        return text.replaceAll(" ", SPACE_CODE);
    }
    */

    private void setWrapLayout() {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(params);
    }

}
