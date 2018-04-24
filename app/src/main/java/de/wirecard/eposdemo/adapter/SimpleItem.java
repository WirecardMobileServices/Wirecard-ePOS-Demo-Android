package de.wirecard.eposdemo.adapter;

import android.support.annotation.Nullable;

public class SimpleItem {
    @Nullable
    private final String text1;
    @Nullable
    private final String text2;
    @Nullable
    private final String text3;
    @Nullable
    private final String text4;

    public SimpleItem(@Nullable String text1, @Nullable String text2, @Nullable String text3, @Nullable String text4) {
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.text4 = text4;
    }

    @Nullable
    public String getText1() {
        return text1;
    }

    @Nullable
    public String getText2() {
        return text2;
    }

    @Nullable
    public String getText3() {
        return text3;
    }

    @Nullable
    public String getText4() {
        return text4;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleItem that = (SimpleItem) o;

        if (text1 != null ? !text1.equals(that.text1) : that.text1 != null) return false;
        if (text2 != null ? !text2.equals(that.text2) : that.text2 != null) return false;
        if (text3 != null ? !text3.equals(that.text3) : that.text3 != null) return false;
        return text4 != null ? text4.equals(that.text4) : that.text4 == null;
    }

    @Override
    public int hashCode() {
        int result = text1 != null ? text1.hashCode() : 0;
        result = 31 * result + (text2 != null ? text2.hashCode() : 0);
        result = 31 * result + (text3 != null ? text3.hashCode() : 0);
        result = 31 * result + (text4 != null ? text4.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleItem{");
        sb.append("text1='").append(text1).append('\'');
        sb.append(", text2='").append(text2).append('\'');
        sb.append(", text3='").append(text3).append('\'');
        sb.append(", text4='").append(text4).append('\'');
        sb.append('}');
        return sb.toString();
    }
}