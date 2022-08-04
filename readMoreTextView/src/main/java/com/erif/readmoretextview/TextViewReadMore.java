package com.erif.readmoretextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextUtils;
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

public class TextViewReadMore extends AppCompatTextView {

    private static final String DOTS = "... ";
    private static final String DEFAULT_EXPAND_TEXT = "Read More";
    private static final String DEFAULT_COLLAPSE_TEXT = "Close";
    private SpannableStringBuilder builderExpanded;
    private SpannableStringBuilder builderCollapsed;
    private boolean collapsed = true;
    private int originalMaxLines = 0;
    private int originalHeight = 0;
    private String originalText = null;
    private String expandText = null;
    private boolean animating = false;
    private int expandTextColor;
    private int expandTextStyle = 0;
    private boolean expandTextUnderline = false;
    private String collapseText;
    private int collapseTextColor;
    private int collapseTextStyle = 0;
    private boolean collapseTextUnderline = false;
    private int animationDuration = 200;
    private TextViewReadMoreCallback callback;
    private boolean collapsable = true;
    private int fullHeight = 0;
    private boolean rebuild = true;

    public void actionListener(TextViewReadMoreCallback callback) {
        this.callback = callback;
    }

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

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(
                    attrs, R.styleable.TextViewReadMore, defStyleAttr, 0
            );
            try {
                originalText = typedArray.getString(R.styleable.TextViewReadMore_android_text);
                originalMaxLines = typedArray.getInt(R.styleable.TextViewReadMore_android_maxLines, 1);
                collapsed = typedArray.getBoolean(R.styleable.TextViewReadMore_collapsed, collapsed);

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
                collapsable = typedArray.getBoolean(R.styleable.TextViewReadMore_collapsable, collapsable);

                int getAnimationDuration = typedArray.getInt(R.styleable.TextViewReadMore_android_animationDuration, animationDuration);
                if (getAnimationDuration > 1000) {
                    animationDuration = 1000;
                } else animationDuration = Math.max(getAnimationDuration, 100);
            } finally {
                typedArray.recycle();
            }
        }
        if (collapsed)
            setMaxLines(originalMaxLines);
        else
            setMaxLines(Integer.MAX_VALUE);
    }

    private StaticLayout snippedStaticLayout(String text, int end) {
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(
                text, 0, end, getPaint(),
                getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight()
        );
        builder.setEllipsize(TextUtils.TruncateAt.END);
        builder.setMaxLines(originalMaxLines);
        builder.setAlignment(Layout.Alignment.ALIGN_NORMAL);
        return builder.build();
    }

    private boolean isEllipsized(String text) {
        StaticLayout layout = snippedStaticLayout(text, text.length());
        int lines = originalMaxLines - 1;
        return layout.getEllipsisCount(lines) > 0;
    }

    public void collapsed(boolean collapsed) {
        if (this.collapsed != collapsed) {
            this.collapsed = collapsed;
            rebuild = true;
            invalidate();
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void toggle() {
        if (!collapsable) {
            if (collapsed) {
                animateTextHeight();
            }
        } else {
            animateTextHeight();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text instanceof Spanned) {
            rebuild = false;
        } else {
            originalText = text.toString();
            rebuild = true;
        }
        super.setText(text, type);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!animating) {
            if (rebuild) {
                if (getVisibility() == View.VISIBLE) {
                    if (getLayout() != null) {
                        if (getWidth() > 0) {
                            originalHeight = snippedStaticLayout(originalText, originalText.length()).getHeight() + getCompoundPaddingTop() + getCompoundPaddingBottom();
                            createExpanded();
                        }
                        boolean isEllipsized = isEllipsized(originalText);
                        if (isEllipsized) {
                            buildCollapsed();
                            boolean emptyCollapse = TextUtils.isEmpty(collapseText);
                            String fullText = emptyCollapse ? originalText : originalText+" "+collapseText;
                            StaticLayout.Builder builder = StaticLayout.Builder.obtain(
                                    fullText, 0, fullText.length(), getPaint(),
                                    getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight()
                            );
                            builder.setAlignment(Layout.Alignment.ALIGN_NORMAL);
                            StaticLayout mLayout = builder.build();
                            int newFullHeight = mLayout.getHeight() + getCompoundPaddingTop() + getCompoundPaddingBottom();
                            if (fullHeight != newFullHeight)
                                fullHeight = newFullHeight;
                            if (collapsed) {
                                setMaxLines(originalMaxLines);
                                setText(builderCollapsed);
                            } else {
                                setMaxLines(Integer.MAX_VALUE);
                                setText(builderExpanded);
                                //setWrapContent();
                            }
                            rebuild = false;
                        } else {
                            setWrapContent();
                        }
                    }
                }
            }
        }
    }

    private void buildCollapsed() {
        Layout layout = snippedStaticLayout(originalText, originalText.length());
        if (layout != null) {
            int lastIndex = layout.getEllipsisCount(originalMaxLines - 1);
            if (lastIndex < (expandText.length() + DOTS.length())) {
                lastIndex = layout.getLineVisibleEnd(getLineCount() - 1);
            } else {
                lastIndex = originalText.length() - (lastIndex + DOTS.length() + expandText.length());
            }
            if (lastIndex > 0) {
                createCollapsed(lastIndex);
            }
        }
    }

    private void createCollapsed(int lastIndex) {
        if (lastIndex > 0) {
            String mainText = originalText.substring(0, lastIndex);
            String lastChar = mainText.substring(mainText.length() - 1);
            String newline = System.getProperty("line.separator");
            if (newline != null) {
                boolean hasNewline = lastChar.contains(newline);
                if (hasNewline)
                    mainText = originalText.substring(0, lastIndex - 1);
            }
            String textAndDots = mainText + DOTS;
            String snipped = textAndDots + expandText;
            builderCollapsed = new SpannableStringBuilder(snipped);

            int start = textAndDots.length();
            int end = expandText.length();
            builderCollapsed.setSpan(
                    new ForegroundColorSpan(expandTextColor), start,
                    start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
            if (expandTextStyle == 1) {
                boldText(builderCollapsed, start, end);
            } else if (expandTextStyle == 2) {
                italicText(builderCollapsed, start, end);
            } else if (expandTextStyle == 3) {
                boldText(builderCollapsed, start, end);
                italicText(builderCollapsed, start, end);
            }
            if (expandTextUnderline) {
                underlineText(builderCollapsed, start, end);
            }
        }
    }

    private void createExpanded() {
        boolean emptyCollapse = TextUtils.isEmpty(collapseText);
        String fullText = emptyCollapse ? originalText : originalText+" "+collapseText;
        builderExpanded = new SpannableStringBuilder(fullText);
        if (!emptyCollapse) {
            int start = (originalText+" ").length();
            int end = collapseText.length();
            builderExpanded.setSpan(
                    new ForegroundColorSpan(collapseTextColor), start,
                    start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
            if (collapseTextStyle == 1) {
                boldText(builderExpanded, start, end);
            } else if (collapseTextStyle == 2) {
                italicText(builderExpanded, start, end);
            } else if (collapseTextStyle == 3) {
                boldText(builderExpanded, start, end);
                italicText(builderExpanded, start, end);
            }
            if (collapseTextUnderline) {
                underlineText(builderExpanded, start, end);
            }
        }
    }

    private void animateTextHeight() {
        if (collapsed)
            setMaxLines(Integer.MAX_VALUE);
        SpannableStringBuilder span = new SpannableStringBuilder(originalText);
        setText(span);
        int fromHeight = collapsed ? originalHeight : fullHeight;
        int targetHeight = collapsed ? fullHeight : originalHeight;
        ValueAnimator animation = ValueAnimator.ofInt(fromHeight, targetHeight);
        ViewGroup.LayoutParams param = getLayoutParams();
        animation.setDuration(animationDuration);
        animation.addUpdateListener(valueAnimator -> {
            param.height = (int) valueAnimator.getAnimatedValue();
            setLayoutParams(param);
        });
        animation.addListener(animatorListener());
        animation.start();
    }

    private AnimatorListenerAdapter animatorListener() {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animating = false;
                collapsed = !collapsed;
                if (collapsed) {
                    setText(builderCollapsed);
                    setMaxLines(originalMaxLines);
                } else {
                    setText(builderExpanded);
                }
                if (callback != null) callback.actionListener(collapsed);
                invalidate();
            }
        };
    }

    private void italicText(SpannableStringBuilder builder, int start, int end) {
        builder.setSpan(new StyleSpan(Typeface.ITALIC),
                start, start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void boldText(SpannableStringBuilder builder, int start, int end) {
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                start, start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void underlineText(SpannableStringBuilder builder, int start, int end) {
        builder.setSpan(new UnderlineSpan(),
                start,start + end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
    }

    private void setWrapContent() {
        if (getLayoutParams() != null) {
            ViewGroup.LayoutParams param = getLayoutParams();
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(param);
        }
    }

    private void debug(String message) {
        Log.d("TextViewReadMore", message);
    }

}
