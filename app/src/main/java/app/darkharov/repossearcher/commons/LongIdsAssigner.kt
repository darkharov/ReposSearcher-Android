package app.darkharov.repossearcher.commons

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class LongIdsAssigner<I : Any> {

    private var seed: Long = 1L
    private val ids = mutableMapOf<Any, Long>()

    private fun id(item: Any) = ids.getOrPut(item) { seed++ }

    private fun listDelegate(adapter: RecyclerView.Adapter<*>, initial: List<I> = emptyList()) =
        Delegates.observable(initial) { _, old, new ->
            DiffUtil.calculateDiff(createDiffCallback(old, new)).dispatchUpdatesTo(adapter)
        }

    private fun createDiffCallback(old: List<I>, new: List<I>): DiffUtil.Callback =
        DiffCallbackImpl(old, new)


    private inner class DiffCallbackImpl(
        private val old: List<I>,
        private val new: List<I>
    ) : DiffUtil.Callback() {

        override fun getNewListSize() = new.size
        override fun getOldListSize() = old.size

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = id(old[oldItemPosition]) == id(new[newItemPosition])

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ) = old[oldItemPosition] == new[newItemPosition]
    }


    companion object {

        fun <I : Any> listDelegate(
            adapter: RecyclerView.Adapter<*>,
            initial: List<I> = emptyList()
        ) =
            LongIdsAssigner<I>().listDelegate(adapter, initial)
    }
}
