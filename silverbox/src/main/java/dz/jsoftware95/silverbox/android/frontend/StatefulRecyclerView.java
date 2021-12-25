/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package dz.jsoftware95.silverbox.android.frontend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dz.jsoftware95.cleaningtools.AutoCleanable;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.VisualItem;
import dz.jsoftware95.silverbox.android.common.Assert;

@UiThread
public class StatefulRecyclerView extends RecyclerView implements AutoCleanable {

    protected final String TAG = getClass().getSimpleName();

    @Nullable
    private StatefulAdapter<?> statefulAdapter = null;

    private boolean closed = false;

    public StatefulRecyclerView(@NonNull final Context context) {
        this(Assert.nonNull(context), null);
    }

    public StatefulRecyclerView(@NonNull final Context context,
                                @Nullable final AttributeSet attrs) {
        this(Assert.nonNull(context), attrs, 0);
    }

    public StatefulRecyclerView(@NonNull final Context context,
                                @Nullable final AttributeSet attrs,
                                final int defStyle) {
        super(Assert.nonNull(context), attrs, defStyle);
    }

    public void setStatefulAdapter(@Nullable final StatefulAdapter<?> statefulAdapter) {
        if (statefulAdapter != null)
            super.setAdapter(statefulAdapter.asAdapter());
        else {
            super.setAdapter(null);
        }

        this.statefulAdapter = statefulAdapter;
    }

    @Override
    @Contract("_ -> fail")
    @SuppressWarnings("rawtypes") // because the super method lakes a generic parameter
    public void setAdapter(final Adapter adapter) {
        throw new UnsupportedOperationException("use #setStatefulAdapter instead");
    }

    @Nullable
    @Contract(pure = true)
    public StatefulAdapter<?> getStatefulAdapter() {
        return statefulAdapter;
    }

    public void clearAdapterData() {
        if (statefulAdapter != null)
            statefulAdapter.clearData();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
        setStatefulAdapter(null);
        setLayoutManager(null);
    }

    @MainThread
    public static final class ViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final ViewDataBinding binding;

        public ViewHolder(@NonNull final ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @NonNull
        @Contract(pure = true)
        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    public interface StatefulAdapter<T> {

        void setData(@Nullable final T data);

        @Nullable
        T getCurrentData();

        void addUpdatesObserver(@NonNull final RecyclerObserver observer);

        void removeUpdatesObserver(@NonNull final RecyclerObserver observer);

        void setInflater(@Nullable final LayoutInflater inflater);

        void setInflater(@NonNull final Context context);

        int dataSize();

        boolean isDataEmpty();

        void clearData();

        Adapter<ViewHolder> asAdapter();

        void close();
    }

    @MainThread
    public static abstract class ListAdapter<C extends List<? extends Item>>
            extends Adapter<ViewHolder> implements StatefulAdapter<C> {

        private static final String INFLATER_MISSING = "you must set the inflater of this adapter before using it";

        private final int itemLayout;

        @Nullable
        private LayoutInflater inflater = null;

        @Nullable
        private C data = null;

        protected ListAdapter(final int itemLayout) {
            this.itemLayout = itemLayout;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            Item item = getItem(position);
            return item != null ? item.getId() : RecyclerView.NO_ID;
        }

        private Item getItem(int position) {
            return data != null ? data.get(position) : null;
        }

        @Override
        public void setInflater(@Nullable final LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public void setInflater(@NonNull final Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public void setData(@Nullable final C data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Nullable
        @Override
        public C getCurrentData() {
            return data;
        }

        @Override
        public void addUpdatesObserver(@NonNull final RecyclerObserver observer) {
            registerAdapterDataObserver(observer);
        }

        @Override
        public void removeUpdatesObserver(@NonNull final RecyclerObserver observer) {
            unregisterAdapterDataObserver(observer);
        }

        @Override
        public int dataSize() {
            return data == null ? 0 : data.size();
        }

        @Override
        public boolean isDataEmpty() {
            return dataSize() == 0;
        }

        @Override
        public void clearData() {
            if (data != null)
                data.clear();
        }

        @Override
        public void close() {
            inflater = null;
            data = null;
        }

        @Override
        public Adapter<ViewHolder> asAdapter() {
            return this;
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            if (inflater == null)
                throw new IllegalStateException(INFLATER_MISSING);

            final ViewDataBinding binding = DataBindingUtil.inflate(inflater, itemLayout, parent, false);
            Assert.nonNull(binding);

            return new ViewHolder(binding);
        }
    }

    @MainThread
    public static class PagedAdapter<V extends VisualItem>
            extends PagedListAdapter<V, ViewHolder>
            implements StatefulAdapter<PagedList<V>> {

        private final int itemLayout, itemVariable;

        private final Map<Integer, Object> variables = new HashMap<>(4);

        @Nullable
        private final V emptyData;

        @Nullable
        private LayoutInflater inflater = null;

        public PagedAdapter(final int itemLayout,
                            final int itemVariable) {
            this(itemLayout, itemVariable, null);
        }

        @SuppressWarnings("unchecked")
        public PagedAdapter(final int itemLayout,
                            final int itemVariable,
                            @Nullable final V emptyData) {
            super((DiffUtil.ItemCallback<V>) VisualItem.DIFF_CALLBACK);
            this.itemLayout = itemLayout;
            this.itemVariable = itemVariable;
            this.emptyData = emptyData;
        }

        public void addVariable(int varID, Object value) {
            variables.put(varID, value);
        }

        @Override
        public void setInflater(@Nullable final LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public void setInflater(@Nullable final Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            if (inflater == null)
                throw new IllegalStateException("you must set the inflater of this adapter before using it");

            final ViewDataBinding binding = DataBindingUtil.inflate(inflater, itemLayout, parent, false);
            for (Map.Entry<Integer, Object> entry : variables.entrySet())
                binding.setVariable(entry.getKey(), entry.getValue());

            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            final V data = getItem(position);

            final boolean isVarSet = holder.getBinding().setVariable(itemVariable, data != null ? data : emptyData);

            if (!isVarSet)
                throw new RuntimeException("Item variable is not set: " + itemVariable
                        + " for data: " + (data != null ? data.getClass().getName() : "null"));
        }

        @Override
        public void setData(final PagedList<V> data) {
            submitList(data);
        }

        @Override
        @Nullable
        public PagedList<V> getCurrentData() {
            return getCurrentList();
        }

        @Override
        public void addUpdatesObserver(@NonNull final RecyclerObserver observer) {
            registerAdapterDataObserver(observer);
        }

        @Override
        public void removeUpdatesObserver(@NonNull final RecyclerObserver observer) {
            unregisterAdapterDataObserver(observer);
        }

        @Override
        public int dataSize() {
            final List<V> currentList = getCurrentList();
            int size = 0;

            if (currentList != null)
                for (final V data : currentList)
                    if (data != null)
                        size++;

            return size;
        }

        @Override
        public boolean isDataEmpty() {
            return dataSize() == 0;
        }

        @Override
        public void clearData() {
            submitList(null);
        }

        @Override
        public Adapter<ViewHolder> asAdapter() {
            return this;
        }

        @Override
        public void close() {
            variables.clear();
            inflater = null;
        }
    }
}
