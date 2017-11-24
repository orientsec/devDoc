package thread

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Product: ktdemo
 * Package: com.xiaomao.thread
 * Time: 2017/11/23 14:58
 * Author: Fredric
 * coding is art not science
 */

/**
 *线程实现的两种方式Thread和Runnable
 */
class DemoThread : Thread() {
    override fun run() {
        println("${javaClass.simpleName} run in thread:${currentThread().name}")
    }
}

class DemoRunnable : Runnable {
    override fun run() {
        println("${javaClass.simpleName} run in thread:${Thread.currentThread().name}")
    }
}

/**
 * Thread的局限：
 * 1.Tread是class，单继承关系，runnable是接口，一个类可以实现多个接口
 * 2.Runnable实现线程可以可以运行在多个线程中，实现资源共享
 * 3.实际应用中，线程往往运行在控制更精确灵活的线程池中
 */

/**
 * Runnable优点1：可以实现多继承
 */
open class Dog {
    fun eat() {
        for (x in 1..5) {
            println("I'm ${javaClass.simpleName}, and I have $x bones")
            Thread.sleep(1000)
        }
    }
}

class Labrador : Dog(), Runnable {
    override fun run() {
        eat()
    }
}

/**
 * Runnable优点2:多线程运行，实现资源共享
 */
class BankCard1 : Thread() {
    private var balance = 100
    override fun run() {
        for (i in 1..3) {
            balance -= 10
            println("my balance is:$balance")
            Thread.sleep(1000)
        }
    }
}

class BankCard2 : Runnable {
    private var balance = AtomicInteger(100)
    override fun run() {
        for (i in 1..3) {
            println("my balance is:${balance.addAndGet(-10)}")
            Thread.sleep(1000)
        }
    }
}


val executorService: ExecutorService = Executors.newCachedThreadPool()

fun main(vararg args: String) {

    if (args.isEmpty()) return
    when (args[0]) {
        "1" -> {
            //直接启动线程
            DemoThread().start()
            //使用线程启动runnable
            Thread(DemoRunnable()).start()
            //使用线程池启动runnable
            executorService.submit(DemoRunnable())
        }
        "2" -> {
            //拉布拉多吃骨头
            Thread(Labrador()).start()
            Thread.sleep(5000)
        }
        "3" -> {
            //三张银行卡，分别取款
            BankCard1().start()
            BankCard1().start()
            BankCard1().start()
        }
        "4" -> {
            //同一张银行卡可以同时取款
            val bankcard = BankCard2()
            executorService.submit(bankcard)
            executorService.submit(bankcard)
            executorService.submit(bankcard)
        }
    }

    Thread.sleep(1000)
    executorService.shutdown()
}