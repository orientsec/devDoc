package thread

import java.util.concurrent.locks.ReentrantLock

/**
 * Product: demo
 * Package: thread
 * Time: 2017/11/28 13:16
 * Author: Fredric
 * coding is art not science
 */
private abstract class Buffer {
    abstract fun read()
    abstract fun write()
}

/**
 * 使用synchronized关键字，read会一直阻塞,无法打断
 */
private class BlockBuffer : Buffer() {
    override fun read() {
        synchronized(this) {
            println("read data...")
        }
    }

    override fun write() {
        synchronized(this) {
            println("start write...")
            val startTime = System.currentTimeMillis()
            while (true) {
                Thread.yield()
                if (System.currentTimeMillis() - startTime > 60000) break
            }
            println("write end...")
        }
    }
}

/**
 * 使用lock实现中断锁
 */
private class UnBlockBuffer : Buffer() {
    val lock = ReentrantLock()
    override fun read() {
        try {
            lock.lockInterruptibly()
            println("read data...")
        } catch (e: InterruptedException) {
            println("read data interrupted...")
        } finally {
            lock.unlock()
        }
        println("read end...")
    }

    override fun write() {
        try {
            lock.lock()
            print("start write...")
            val startTime = System.currentTimeMillis()
            while (true) {
                Thread.yield()
                if (System.currentTimeMillis() - startTime > 60000) break
            }
            println("write end...")
        } finally {
            lock.unlock()
        }
    }
}

private fun test(buffer: Buffer) {
    val readThread = Thread({ Thread.sleep(100);buffer.read() })
    val writeThread = Thread({ buffer.write() })
    readThread.start()
    writeThread.start()
    Thread.sleep(5000)
    println("lazy to wait...")
    readThread.interrupt()
    readThread.join()
    writeThread.join()
}

fun main(args: Array<String>) {
    //test(BlockBuffer())
    test(UnBlockBuffer())
}