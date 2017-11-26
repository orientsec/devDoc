package thread

/**
 * Volatile关键字保证可见性
 */
private class Print : Runnable {
    //@Volatile
    var i = 1
    //@Volatile
    var ok = false

    override fun run() {
        while (!ok) {
            Thread.yield()
        }
        if (i == 1) println(i)
    }
}

fun main(args: Array<String>) {
    for (i in 1..10000) {
        val print = Print()
        Thread(print).start()
        print.i = 10
        print.ok = true
    }

}