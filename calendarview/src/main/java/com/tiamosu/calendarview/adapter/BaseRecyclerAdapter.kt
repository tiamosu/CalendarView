package com.tiamosu.calendarview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * 基本的适配器
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class BaseRecyclerAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: MutableList<T> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null
    private var onClickListener: OnClickListener

    init {
        onClickListener = object : OnClickListener() {
            override fun onClick(position: Int, itemId: Long) {
                onItemClickListener?.onItemClick(position, itemId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return onCreateDefaultViewHolder(parent, viewType).apply {
            itemView.tag = this
            itemView.setOnClickListener(onClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, items[position], position)
    }

    abstract fun onCreateDefaultViewHolder(parent: ViewGroup?, type: Int): RecyclerView.ViewHolder

    abstract fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: T, position: Int)

    override fun getItemCount() = items.size

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun addAll(items: List<T>) {
        if (items.isNotEmpty()) {
            this.items.addAll(items)
            notifyItemRangeInserted(this.items.size, items.size)
        }
    }

    fun addItem(item: T?) {
        if (item != null) {
            items.add(item)
            notifyItemChanged(items.size)
        }
    }

    fun getItems(): List<T> {
        return items
    }

    fun getItem(position: Int): T? {
        return if (position < 0 || position >= items.size) {
            null
        } else items[position]
    }

    internal abstract class OnClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val holder = v.tag as? RecyclerView.ViewHolder ?: return
            onClick(holder.adapterPosition, holder.itemId)
        }

        abstract fun onClick(position: Int, itemId: Long)
    }

    fun interface OnItemClickListener {
        fun onItemClick(position: Int, itemId: Long)
    }
}