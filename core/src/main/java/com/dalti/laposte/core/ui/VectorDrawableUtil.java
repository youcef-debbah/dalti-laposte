package com.dalti.laposte.core.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Xml;
import android.widget.ImageView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.dalti.laposte.core.repositories.Teller;

import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

import dz.jsoftware95.silverbox.android.middleware.ContextUtils;

public class VectorDrawableUtil {

    /**
     * Create a vector drawable from bin data
     *
     * @return the vector drawable or null it couldn't be created.
     */
    public static Drawable getDrawable(byte[] binXml) {
        if (binXml != null)
            try {
                // Get the binary XML parser (XmlBlock.Parser) and use it to create the drawable
                // This is the equivalent of what AssetManager#getXml() does
                @SuppressLint("PrivateApi")
                Class<?> xmlBlock = Class.forName("android.content.res.XmlBlock");
                Constructor<?> xmlBlockConstr = xmlBlock.getConstructor(byte[].class);
                @SuppressLint("DiscouragedPrivateApi")
                Method xmlParserNew = xmlBlock.getDeclaredMethod("newParser");
                xmlBlockConstr.setAccessible(true);
                xmlParserNew.setAccessible(true);
                XmlPullParser parser = (XmlPullParser) xmlParserNew.invoke(xmlBlockConstr.newInstance((Object) binXml));

                Objects.requireNonNull(parser);
                Context context = AbstractQueueApplication.requireInstance();

                if (Build.VERSION.SDK_INT >= 24) {
                    return Drawable.createFromXml(context.getResources(), parser);
                } else {
                    // Before API 24, vector drawables aren't rendered correctly without compat lib
                    final AttributeSet attrs = Xml.asAttributeSet(parser);
                    int type = parser.next();
                    while (type != XmlPullParser.START_TAG) {
                        type = parser.next();
                    }
                    return VectorDrawableCompat.createFromXmlInner(context.getResources(), parser, attrs, null);
                }

            } catch (Exception e) {
                Teller.error("Vector Drawable creation failed", e);
            }
        return null;
    }

    @Nullable
    public static Drawable getDrawable(@DrawableRes int drawableID) {
        return getDrawable(AbstractQueueApplication.getInstance(), drawableID);
    }

    @Nullable
    public static Drawable getDrawable(@Nullable Context context, @DrawableRes int drawableID) {
        if (context != null)
            return AppCompatResources.getDrawable(context, drawableID);
        else
            return null;
    }

    public static Drawable getDrawable(Context context, @DrawableRes int drawableID,
                                       @AttrRes int colorAttribute, @ColorRes int defaultColor) {
        if (context != null) {
            Drawable drawable = AppCompatResources.getDrawable(context, drawableID);
            if (drawable != null) {
                int color = ContextUtils.getThemeColor(context, colorAttribute, defaultColor);
                Drawable wrapped = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrapped, color);
                return wrapped;
            }
        }

        return null;
    }

    public static Drawable getDrawable(Context context, @DrawableRes int drawableID, @ColorRes int colorStateListRes) {
        return getDrawable(context, drawableID, AppCompatResources.getColorStateList(context, colorStateListRes));
    }

    public static Drawable getDrawable(Context context, @DrawableRes int drawableID, ColorStateList color) {
        if (context != null) {
            Drawable drawable = AppCompatResources.getDrawable(context, drawableID);
            if (drawable != null && color != null) {
                Drawable coloredDrawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTintList(drawable, color);
                return coloredDrawable;
            }
            return drawable;
        }

        return null;
    }

    public static void setDrawable(ImageView imageView, @DrawableRes int drawableID, @ColorRes int color) {
        final Context context = imageView.getContext();
        ColorStateList colorStateList = AppCompatResources.getColorStateList(context, color);
        imageView.setImageDrawable(getDrawable(context, drawableID, colorStateList));
    }

    public static void setDrawable(ImageView imageView, @DrawableRes int drawableID, ColorStateList color) {
        imageView.setImageDrawable(getDrawable(imageView.getContext(), drawableID, color));
    }
}

