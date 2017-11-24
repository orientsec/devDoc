package thread

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.system.measureNanoTime

/**
 * Product: ktdemo
 * Package: com.xiaomao.thread
 * Time: 2017/11/23 15:37
 * Author: Fredric
 * coding is art not science
 */
//非线程安全
open class Counter : Runnable {
    protected open var count = 0
    override fun run() {
        for (i in 1..loopTimes) {
            count++
        }
    }

    var loopTimes = 10000000

    open fun clean() {
        count = 0
    }

    open fun count() = count
}

//非线程安全 volatile关键字不提供原子性
open class CounterUnsafe : Counter() {
    @Volatile
    override var count = 0

}


//线程安全1 synchronized关键字
class CounterSafe1 : Counter() {
    override fun run() {
        synchronized(this) {
            for (i in 1..loopTimes) {
                count++
            }
        }
    }
}

//线程安全2 synchronized性能损耗
class CounterSafe2 : Counter() {
    @Synchronized
    override fun run() {
        for (i in 1..loopTimes) {
            count++
        }
    }
}

//线程安全3
class CounterSafe3 : Counter() {
    private val lock = ReentrantLock()
    override fun run() {
        lock.lock()
        try {
            for (i in 1..loopTimes) {
                count++
            }
        } finally {
            lock.unlock()
        }
    }

}

//线程安全4 synchronized性能损耗
class CounterSafe4 : Counter() {
    override fun run() {
        for (i in 1..loopTimes) {
            synchronized(this) {
                count++
            }
        }
    }
}

//线程安全5 AtomicInteger提供原子操作
class CounterSafe5 : Counter() {
    private val countAtomic = AtomicInteger()
    override fun run() {
        for (i in 1..loopTimes) {
            countAtomic.addAndGet(1)
        }
    }

    override fun count(): Int = countAtomic.get()

    override fun clean() {
        countAtomic.set(0)
    }
}


fun main(args: Array<String>) {
    listOf(Counter(), CounterUnsafe(), CounterSafe1(), CounterSafe2(), CounterSafe3(), CounterSafe4(), CounterSafe5())
            .forEach(::runTask)

}

fun runTask(counter: Counter) {
    println("${counter::class.java.simpleName} test start...")
    val time = measureNanoTime {
        val threads = List(10) {
            Thread(counter).apply { start() }
        }
        threads.forEach { it.join() }
    }
    println("mulity thread cost time $time count is ${counter.count()}")

    counter.clean()
    counter.loopTimes = 100000000
    val timeSingleThread = measureNanoTime {
        Thread(counter).apply { start(); join() }
    }
    println("single thread cost time $timeSingleThread count is ${counter.count()}")
}