package com.example.note;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {
    private List<Category> categories = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        Category currentCategory = categories.get(position);
        holder.textViewTitle.setText(currentCategory.getName());
    }

    @Override
    public int getItemCount() { return categories.size(); }

    @SuppressLint("NotifyDataSetChanged")
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category getCategoriesAt(int position) {
        return categories.get(position);
    }

    public void moveCategory(int from, int to) {
        if (from < getItemCount() && to < getItemCount()) {
            Category fromCategory = categories.get(from);
            categories.remove(from);
            categories.add(to, fromCategory);
            notifyItemMoved(from, to);
        }
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;

        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.tvCategoryName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(categories.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
