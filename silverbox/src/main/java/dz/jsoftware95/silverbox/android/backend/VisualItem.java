package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

/**
 * An entity that it's instances are distinguishable by it's unique id and contents.
 */
public interface VisualItem extends Item {
    /**
     * A simple {@link DiffUtil.ItemCallback} that compare two implementations of this interface
     * using their {@linkplain #getId() ID} to tell if they are the same items and the
     * {@linkplain Object#equals(Object) equals method} to tell if two items have the same
     * content.
     */
    DiffUtil.ItemCallback<VisualItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<VisualItem>() {

        /**
         * Called to check whether two Values represent the same item
         * by comparing their {@linkplain VisualItem#getId() IDs}.
         * <p>
         * Note: {@code ull} items in the list are assumed to be the same as another {@code null}
         * item and are assumed to not be the same as a non-{@code null} item. This callback will
         * not be invoked for either of those cases.
         * </p>
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return True if the two items represent the same object or false if they are different.
         *
         * @see DiffUtil.Callback#areItemsTheSame(int, int)
         */
        @Override
        public boolean areItemsTheSame(@NonNull final VisualItem oldItem,
                                       @NonNull final VisualItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        /**
         * Called to check whether two items have the same data
         * by using the {@linkplain Object#equals(Object) equals method}.
         * <p>
         * For example, if you are using DiffUtil with a
         * {@link RecyclerView.Adapter RecyclerView.Adapter}, you should
         * return whether the items' visual representations are the same.
         * </p>
         * <p>
         * This method is called only if {@link #areItemsTheSame(VisualItem, VisualItem)} returns {@code true}
         * for these items.
         * </p>
         * <p>
         * Note: Two {@code null} items are assumed to represent the same contents. This callback
         * will not be invoked for this case.
         * </p>
         *
         * @param oldItem The item in the old list.
         * @param newItem The item in the new list.
         * @return True if the contents of the items are the same or false if they are different.
         *
         * @see DiffUtil.Callback#areContentsTheSame(int, int)
         */
        @Override
        @Contract(pure = true)
        public boolean areContentsTheSame(@NonNull final VisualItem oldItem,
                                          @NonNull final VisualItem newItem) {
            return oldItem.areContentsTheSame(newItem);
        }
    };

    /**
     * Reruns whether the given <var>other</var> item is equals to this instance
     * from the perspective of application.
     * <p>
     * For example, when using DiffUtil with a {@link RecyclerView.Adapter RecyclerView.Adapter}
     * the implementation of this method should return whether the items' visual
     * representations are the same.
     * </p>
     *
     * @param other the reference to the other item with which to compare.
     * @return {@code true} if this item is the same as the given <var>item</var>;
     * {@code false} otherwise.
     */
    boolean areContentsTheSame(@NonNull final VisualItem other);
}
