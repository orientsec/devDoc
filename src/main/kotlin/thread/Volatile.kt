package thread

/**
 * Volatile关键字保证可见性
 */
private class Print : Runnable {
    @Volatile
    var i = 1
    @Volatile
    var ok = false

    override fun run() {
        while (!ok) {
            Thread.yield()
        }
        println(i)
    }
}

fun main(args: Array<String>) {
    val print = Print()
    Thread(print).start()
    print.ok = true
    print.i = 10

}