package co.csadev.kwikpicker.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

class KwikEmptyRecyclerView(context: Context, attrs: AttributeSet? = null) : androidx.recyclerview.widget.RecyclerView(context, attrs) {
    private var emptyView: View? = null

    private val observer: androidx.recyclerview.widget.RecyclerView.AdapterDataObserver =
        object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIfEmpty()
            }
        }

    internal fun checkIfEmpty() {
        emptyView?.visibility = if ((adapter?.itemCount ?: 0) > 0) View.GONE else View.VISIBLE
    }

    override fun setAdapter(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
    }

    fun setEmptyView(emptyView: View?) {
        this.emptyView = emptyView
        checkIfEmpty()
    }
}