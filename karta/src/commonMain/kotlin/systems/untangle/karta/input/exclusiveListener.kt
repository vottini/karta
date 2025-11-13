package systems.untangle.karta.input

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

suspend fun exclusiveListener(
    flow: SharedFlow<Int>,
    setter: (Boolean) -> Unit
) : Job {
    val currentScope = CoroutineScope(coroutineContext)

    return currentScope.launch {
        var currentlyExclusive = false
        var lastSubCount = 0

        flow.collect { subCount ->

            /*
            When only the element itself is subscribed to dragging
            events, the counter passes from 0 to 1 (rising edge), so
            this is when it is still allowed to drag the map. When there
            is one more drag listener (it plus another element), the counter
            will be 2, immediately going from 2 to 1 (falling edge)
            when this element stops listening to drag events
        */

            val isExclusive =
                when (subCount) {
                    0 -> true
                    1 -> (lastSubCount == 0)
                    else -> false
                }

            lastSubCount = subCount
            if (isExclusive != currentlyExclusive) {
                currentlyExclusive = isExclusive
                setter(currentlyExclusive)
            }
        }
    }

}

