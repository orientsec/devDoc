package thread

/**
 * Product: ktdemo
 * Package: com.xiaomao.thread
 * Time: 2017/11/23 16:05
 * Author: Fredric
 * coding is art not science
 */

/**
 * 线程中断的方法
 */

/**
 * 通过自定义标志位结束任务
 */
class DemoRunnable2 : Runnable {
    var running = true
    override fun run() {
        println("work start")
        while (running) {
            val time = System.currentTimeMillis()
            while (System.currentTimeMillis() - time < 1000) {
                Thread.yield()
            }
            println("work on going...")
        }
        println("work stop!")
    }

    fun stop() {
        running = false
    }
}

/**
 * 通过isInterrupted标志位结束任务
 */
class DemoRunnable3 : Runnable {
    override fun run() {
        println("work start")
        while (!Thread.currentThread().isInterrupted) {
            val time = System.currentTimeMillis()
            while (System.currentTimeMillis() - time < 1000) {
                Thread.yield()
            }
            println("work on going...")
        }
        println("work stop!")
    }
}

/**
 * 通过InterruptedException结束任务
 */
class DemoRunnable4 : Runnable {
    override fun run() {
        try {
            while (true) {
                Thread.sleep(1000)
                println("I'm running")
            }
        } catch (e: InterruptedException) {
            //sleep被interrupt打断后，isInterrupt为false
            println("work stop! thread is interrupted:${Thread.currentThread().isInterrupted}")
        }

        //never stop
        /*while (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(1000)
                println("I'm running")
            } catch (e: InterruptedException) {
                println("stop running! thread is interrupted:${Thread.currentThread().isInterrupted}")
            }
        }*/

    }
}

/**
 * 不会打断
 */
class DemoRunnable5 : Runnable {
    override fun run() {
        while (true) {
            val time = System.currentTimeMillis()
            while (System.currentTimeMillis() - time < 1000) {
                Thread.yield()
            }
            println("work running! thread is interrupted:${Thread.currentThread().isInterrupted}")
        }
        //println("new arrive here")
    }
}


fun main(args: Array<String>) {
    if (args.isEmpty()) return
    val runnable = when (args[0]) {
        "1" -> {
            println("test 1 start....")
            DemoRunnable2()
        }
        "2" -> {
            println("test 2 start....")
            DemoRunnable3()
        }
        "3" -> {
            println("test 3 start....")
            DemoRunnable4()
        }
        "4" -> {
            println("test 4 start....")
            DemoRunnable5()
        }
        else -> Runnable { }
    }
    val thread = Thread(runnable)
    thread.start()
    Thread.sleep(3000)
    //interrupt thread

    if (runnable is DemoRunnable2) runnable.stop() else thread.interrupt()
    //wait for thread dead
    Thread.sleep(1000)
    println("thread is alive:${thread.isAlive}")
}