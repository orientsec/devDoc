package thread

import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * CountDownLatch的作用
 */

class Worker(private val downLatch: CountDownLatch, private val name: String) : Runnable {

    override fun run() {
        this.doWork()
        try {
            TimeUnit.SECONDS.sleep(Random().nextInt(10).toLong())
        } catch (ie: InterruptedException) {
        }

        println(this.name + "活干完了！")
        this.downLatch.countDown()

    }

    private fun doWork() {
        println(this.name + "正在干活!")
    }

}

class Boss(private val downLatch: CountDownLatch) : Runnable {

    override fun run() {
        println("老板正在等所有的工人干完活......")
        try {
            this.downLatch.await()
        } catch (e: InterruptedException) {
        }

        println("工人活都干完了，老板开始检查了！")
    }

}

fun main(args: Array<String>) {
    val executor = Executors.newCachedThreadPool()

    val latch = CountDownLatch(3)

    val w1 = Worker(latch, "张三")
    val w2 = Worker(latch, "李四")
    val w3 = Worker(latch, "王二")

    val boss = Boss(latch)

    executor.execute(w3)
    executor.execute(w2)
    executor.execute(w1)
    executor.execute(boss)

    executor.shutdown()
}