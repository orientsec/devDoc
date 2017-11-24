package thread

/**
 * Product: demo
 * Package: thread
 * Time: 2017/11/24 14:36
 * Author: Fredric
 * coding is art not science
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) return
    when (args[0]) {
        "1" -> test1()
        "2" -> test2()
    }
}

private fun test1() {
    val a = PrintThread("a")
    val b = PrintThread("b")
    a.start()
    b.start()
}

private val lock = java.lang.Object()

/**
 * 通过wait notify同步线程
 */
internal class PrintThread(name: String) : Thread() {
    init {
        this.name = name
    }

    override fun run() {
        synchronized(lock) {
            for (i in 1..100) {
                if (i % 10 == 0) {
                    lock.notify()//唤醒另外一个线程
                    lock.wait()   //暂时释放资源
                }
                println(this.name + ": " + i)
            }
        }
    }
}


private class DeadLockA(private val lock1: Object, private val lock2: Object) : Runnable {
    override fun run() {
        synchronized(lock1) {
            println("${Thread.currentThread().name}: DeatLockA block 1")
            Thread.sleep(2000)
            synchronized(lock2) {
                println("${Thread.currentThread().name}: DeatLockA block 2")
                Thread.sleep(2000)
            }
        }
    }

}

private class DeadLockB(private val lock1: Object, private val lock2: Object) : Runnable {
    override fun run() {
        synchronized(lock2) {
            println("${Thread.currentThread().name}: DeatLockB block 1")
            Thread.sleep(2000)
            synchronized(lock1) {
                println("${Thread.currentThread().name}: DeatLockB block 2")
                Thread.sleep(2000)
            }
        }
    }

}

private fun test2() {
    val lock1 = Object()
    val lock2 = Object()
    Thread(DeadLockA(lock1, lock2), "Thread -Dead lock A").start()
    Thread(DeadLockB(lock1, lock2), "Thread -Dead lock A").start()
}