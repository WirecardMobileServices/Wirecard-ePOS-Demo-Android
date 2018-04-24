package de.wirecard.eposdemo.adapter;

import android.support.annotation.Nullable;

public class SimpleItem {
    @Nullable
    private final String left;
    @Nullable
    private final String center;
    @Nullable
    private final String right;

    public SimpleItem(@Nullable String left, @Nullable String center, @Nullable String right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    @Nullable
    public String getLeft() {
        return left;
    }

    @Nullable
    public String getCenter() {
        return center;
    }

    @Nullable
    public String getRight() {
        return right;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleItem that = (SimpleItem) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (center != null ? !center.equals(that.center) : that.center != null) return false;
        return right != null ? right.equals(that.right) : that.right == null;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (center != null ? center.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleItem{");
        sb.append("left='").append(left).append('\'');
        sb.append(", center='").append(center).append('\'');
        sb.append(", right='").append(right).append('\'');
        sb.append('}');
        return sb.toString();
    }
}