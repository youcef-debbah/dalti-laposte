package com.dalti.laposte.core.repositories;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.WebPageInfo;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@Entity(tableName = WebPage.NAME)
public class WebPage {

    public static final String NAME = "web_page";
    public static final String ID = "web_page_name";

    @NotNull
    @PrimaryKey
    @ColumnInfo(name = ID)
    private String name = "";
    private String data;
    private Long downloadTime;
    private String mimeType;
    private String encoding;

    public WebPage() {
    }

    @Ignore
    public WebPage(@NotNull String name, String data) {
        this.name = name;
        this.data = data;
    }

    @Ignore
    public WebPage(@NotNull String name, String data, String mimeType, String encoding) {
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.downloadTime = System.currentTimeMillis();
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public Long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(Long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public String getMimeType() {
        if (mimeType != null)
            return mimeType;
        else
            return WebPageInfo.DEFAULT_MIME_TYPE;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getEncoding() {
        if (encoding != null)
            return encoding;
        else
            return WebPageInfo.DEFAULT_ENCODING;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @NotNull
    @Override
    public String toString() {
        return "WebPage{" +
                "name='" + name + '\'' +
                ", downloadTime=" + TimeUtils.formatAsDateTime(downloadTime) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebPage webPage = (WebPage) o;
        return Objects.equals(name, webPage.name);
    }

    public boolean hasContent() {
        return data != null && !data.isEmpty();
    }

    public static WebPage from(WebPageInfo info) {
        if (info != null && GlobalUtil.notEmpty(info.getName()))
            return new WebPage(info.getName(), info.getContent(), info.getMimeType(), info.getEncoding());
        else
            return null;
    }
}
