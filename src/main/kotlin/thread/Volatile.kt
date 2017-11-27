package thread

import java.util.*
import kotlin.jvm.Volatile

/**
 * Volatile关键字保证可见性
 */
private class Print {

    @Volatile
    var running = true

    var count = 0

    fun foo() {
        while (running) {
            Thread.yield()
        }
        if (count == 0) println("Thread finished, count is $count")
    }

    fun bar() {
        count = Random().nextInt(5) + 1
        running = false
    }

}

private fun test1() {
    while (true) {
        val print = Print()
        val t1 = Thread({ print.foo() })
        val t2 = Thread({ print.bar() })
        t1.start()
        t2.start()
        t1.join()
        t2.join()
    }
}


fun main(args: Array<String>) {
    test1()
}