package thread

import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


/**
 * Product: demo
 * Package: thread
 * Time: 2017/11/28 14:29
 * Author: Fredric
 * coding is art not science
 */
internal class BoundedBuffer {
    private val lock: Lock = ReentrantLock()//锁对象
    private val notFull = lock.newCondition()!!//写线程条件
    private val notEmpty = lock.newCondition()!!//读线程条件

    private val items = arrayOfNulls<Any>(100)//缓存队列
    var putptr: Int = 0/*写索引*/
    var takeptr: Int = 0/*读索引*/
    var count/*队列中存在的数据个数*/: Int = 0

    @Throws(InterruptedException::class)
    fun put(x: Any) {
        lock.lock()
        try {
            while (count == items.size)
            //如果队列满了
                notFull.await()//阻塞写线程
            items[putptr] = x//赋值
            if (++putptr == items.size) putptr = 0//如果写索引写到队列的最后一个位置了，那么置为0
            ++count//个数++
            notEmpty.signal()//唤醒读线程
        } finally {
            lock.unlock()
        }
    }

    @Throws(InterruptedException::class)
    fun take(): Any {
        lock.lock()
        try {
            while (count == 0)
            //如果队列为空
                notEmpty.await()//阻塞读线程
            val x = items[takeptr]//取值
            if (++takeptr == items.size) takeptr = 0//如果读索引读到队列的最后一个位置了，那么置为0
            --count//个数--
            notFull.signal()//唤醒写线程
            return x!!
        } finally {
            lock.unlock()
        }
    }
}

fun main(args: Array<String>) {
    val executor = Executors.newCachedThreadPool()
    val buffer = BoundedBuffer()
    executor.submit({
        while (true) {
            Thread.sleep(1000)
            buffer.put("A")
        }
    })
    executor.submit({
        while (true) {
            Thread.sleep(2000)
            buffer.put("B")
        }
    })
    for (i in 1..20) {
        executor.submit({
            while (true) {
                Thread.sleep(100)
                println("consumer $i got an${buffer.take()}")
            }
        })
    }
}