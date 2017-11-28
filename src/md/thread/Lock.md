#并发新特性—Lock 锁与条件变量
多线程和并发性并不是什么新内容，但是Java语言设计中的创新之一就是，它是第一个直接把跨平台线程模型和正规的内存模型集成到语言中的主流
语言。核心类库包含一个 Thread 类，可以用它来构建、启动和操纵线程，Java 语言包括了跨线程传达并发性约束的构造 —— synchronized 和
volatile 。
##概述
###synchronized
<br>把代码块声明为 synchronized，可以使该代码具有原子性（atomicity）和 可见性（visibility）。
> 参见线程安全章节

实现同步操作需要考虑安全更新多个共享变量所需的一切，不能有争用条件，不能破坏数据（假设同步的边界位置正确），而且要保证正确同步的其
他线程可以看到这些变量的最新值。_*不论什么时候，只要您将编写的变量接下来可能被另一个线程读取，或者您将读取的变量最后是被另一个线程写
入的，那么您必须进行同步。*_

###对 synchronized 的改进
synchronized并不完美，它有一些功能性的限制 —— 它无法中断一个正在等候获得锁的线程，也无法通过投票得到锁，如果不想等下去，也就没法得
到锁。同步还要求锁的释放只能在与获得锁所在的堆栈帧相同的堆栈帧中进行，多数情况下，这没问题（而且与异常处理交互得很好），但是，确实存
在一些非块结构的锁定更合适的情况。
<br>Java 5 中引入了新的锁机制——java.util.concurrent.locks 中的显式的互斥锁：Lock 接口，它提供了比synchronized 更加广泛的锁定操作。
Lock 框架是锁定的一个抽象，它允许把锁定的实现作为 Java 类，而不是作为语言的特性来实现。这就为 Lock 的多种实现留下了空间，各种实现可能
有不同的调度算法、性能特性或者锁定语义。
<br>Lock 接口有 3 个实现它的类：ReentrantLock、ReetrantReadWriteLock.ReadLock 和 ReetrantReadWriteLock.WriteLock，即重入锁、读锁和写锁。
lock 必须被显式地创建、锁定和释放，为了可以使用更多的功能，一般用 ReentrantLock 为其实例化 ，它拥有与 synchronized 相同的并发性和内存语
义，但是添加了类似锁投票、定时锁等候和可中断锁等候的一些特性。此外，它还提供了在激烈争用情况下更佳的性能。换句话说，当许多线程都想访
问共享资源时，JVM 可以花更少的时候来调度线程，把更多时间用在执行线程上。reentrant 锁意味着什么呢？简单来说，它有一个与锁相关的获取计数器，
如果拥有锁的某个线程再次得到锁，那么获取计数器就加1，然后锁需要被释放两次才能获得真正释放。这模仿了 synchronized 的语义；如果线程进入由
线程已经拥有的监控器保护的 synchronized 块，就允许线程继续进行，当线程退出第二个（或者后续） synchronized 块的时候，不释放锁，只有线程退
出它进入的监控器保护的第一个 synchronized 块时，才释放锁。
<br>为了保证锁最终一定会被释放（可能会有异常发生），要把互斥区放在 try 语句块内，并在 finally 语句块中释放锁，尤其当有 return 语句时，
return 语句必须放在 try 字句中，以确保unlock()不会过早发生，从而将数据暴露给第二个任务。因此，采用 lock 加锁和释放锁的一般形式如下：
```text
Lock lock = new ReentrantLock();  
lock.lock();  
try {   
  // update object state  
}  
finally {  
  lock.unlock();   
}  
```
这是lock和synchronized的一个明显区别。这一点区别看起来可能没什么，但是实际上，它极为重要。忘记在 finally 块中释放锁，可能会在程序中留下一
个定时炸弹，当有一天炸弹爆炸时，您要花费很大力气才有找到源头在哪。而使用同步，JVM 将确保锁会获得自动释放。
>在 JDK1.5 中，synchronized 是性能低效的。因为这是一个重量级操作，它对性能最大的影响是阻塞的是实现，挂起线程和恢复线程的操作都需要转入
内核态中完成，这些操作给系统的并发性带来了很大的压力。相比之下使用Java 提供的 Lock 对象，性能更高一些。Brian Goetz 对这两种锁在 JDK1.5、
单核处理器及双 Xeon 处理器环境下做了一组吞吐量对比的实验，发现多线程环境下，synchronized的吞吐量下降的非常严重，而ReentrankLock 则能基
本保持在同一个比较稳定的水平上(参照[引用1](http://blog.csdn.net/fw0124/article/details/6672522))。但与其说 ReetrantLock 性能好，倒不如
说 synchronized 还有非常大的优化余地，于是到了 JDK1.6，发生了变化，
对 synchronize 加入了很多优化措施，有自适应自旋，锁消除，锁粗化，轻量级锁，偏向锁等等。导致在 JDK1.6 上 synchronize 的性能并不比 Lock 差。
官方也表示，他们也更支持 synchronize，在未来的版本中还有优化余地，所以还是提倡在 synchronized 能实现需求的情况下，优先考虑使用
synchronized 来进行同步。

###两种锁的底层实现策略
互斥同步最主要的问题就是进行线程阻塞和唤醒所带来的性能问题，因而这种同步又称为阻塞同步，它属于一种悲观的并发策略，即线程获得的是独占锁。
独占锁意味着其他线程只能依靠阻塞来等待线程释放锁。而在 CPU 转换线程阻塞时会引起线程上下文切换，当有很多线程竞争锁的时候，会引起 CPU 频繁
的上下文切换导致效率很低。synchronized 采用的便是这种并发策略。
<br>随着指令集的发展，我们有了另一种选择：基于冲突检测的乐观并发策略，通俗地讲就是先进性操作，如果没有其他线程争用共享数据，那操作就成功
了，如果共享数据被争用，产生了冲突，那就再进行其他的补偿措施（最常见的补偿措施就是不断地重拾，直到试成功为止），这种乐观的并发策略的许多
实现都不需要把线程挂起，因此这种同步被称为非阻塞同步。ReetrantLock 采用的便是这种并发策略。
<br>在乐观的并发策略中，需要操作和冲突检测这两个步骤具备原子性，它靠硬件指令来保证，这里用的是 CAS 操作（Compare and Swap）。JDK1.5 之后，
Java 程序才可以使用CAS操作。我们可以进一步研究 ReentrantLock 的源代码，会发现其中比较重要的获得锁的一个方法是 compareAndSetState，这里其
实就是调用的 CPU 提供的特殊指令。现代的 CPU 提供了指令，可以自动更新共享数据，而且能够检测到其他线程的干扰，而 compareAndSet() 就用这些
代替了锁定。这个算法称作非阻塞算法，意思是一个线程的失败或者挂起不应该影响其他线程的失败或挂起。
<br>Java 5 中引入了注入 AutomicInteger、AutomicLong、AutomicReference 等特殊的原子性变量类，它们提供的如：compareAndSet()、incrementAndSet()
和getAndIncrement()等方法都使用了 CAS 操作。因此，它们都是由硬件指令来保证的原子方法。

###用途比较
基本语法上，ReentrantLock 与 synchronized 很相似，它们都具备一样的线程重入特性，只是代码写法上有点区别而已，一个表现为 API 层面的互斥
锁（Lock），一个表现为原生语法层面的互斥锁（synchronized）。ReentrantLock 相对 synchronized 而言还是增加了一些高级功能，主要有以下四项：
1. 等待可中断：当持有锁的线程长期不释放锁时，正在等待的线程可以选择放弃等待，改为处理其他事情，它对处理执行时间非常上的同步块很有帮助。
而在等待由 synchronized 产生的互斥锁时，会一直阻塞，是不能被中断的。
2. 可实现公平锁：多个线程在等待同一个锁时，必须按照申请锁的时间顺序排队等待，而非公平锁则不保证这点，在锁释放时，任何一个等待锁的线程都
有机会获得锁。synchronized 中的锁时非公平锁，ReentrantLock 默认情况下也是非公平锁，但可以通过构造方法 ReentrantLock（ture）来要求使用公
平锁。
3. 锁可以绑定多个条件：ReentrantLock 对象可以同时绑定多个 Condition 对象（名曰：条件变量或条件队列），而在 synchronized 中，锁对象的 
wait()和 notify()或 notifyAll()方法可以实现一个隐含条件，但如果要和多于一个的条件关联的时候，就不得不额外地添加一个锁，而 ReentrantLock 
则无需这么做，只需要多次调用 newCondition()方法即可。而且我们还可以通过绑定 Condition 对象来判断当前线程通知的是哪些线程（即与 Condition
 对象绑定在一起的其他线程）。
4. lock可以尝试获取锁，如果锁被其他线程持有，则返回 false，不会使当前线程休眠。

###不要抛弃 synchronized
java.util.concurrent.lock 中的锁定类是用于高级用户和高级情况的工具 。一般来说，除非您对 Lock 的某个高级特性有明确的需要，或者有明确的证
据（而不是仅仅是怀疑）表明在特定情况下，同步已经成为可伸缩性的瓶颈，否则还是应当继续使用 synchronized。对于 java.util.concurrent.lock 中
的锁定类来说，synchronized 仍然有一些优势。比如，在使用 synchronized 的时候，不能忘记释放锁；在退出 synchronized 块时，JVM 会为您做这件
事。您很容易忘记用 finally 块释放锁，这对程序非常有害。您的程序能够通过测试，但会在实际工作中出现死锁，那时会很难指出原因（这也是为什么根
本不让初级开发人员使用 Lock 的一个好理由。）另一个原因是因为，当 JVM 用 synchronized 管理锁定请求和释放时，JVM 在生成线程转储时能够包括锁
定信息。这些对调试非常有价值，因为它们能标识死锁或者其他异常行为的来源。Lock 类只是普通的类，JVM 不知道具体哪个线程拥有 Lock 对象。
###什么时候选择用 ReentrantLock
在确实需要一些 synchronized 所没有的特性的时候，比如时间锁等候、可中断锁等候、无块结构锁、多个条件变量或者锁投票。 ReentrantLock 还具有可
伸缩性的好处，应当在高度争用的情况下使用它，但大多数 synchronized 块几乎从来没有出现过争用，所以可以把高度争用放在一边。建议用 synchronized
开发，直到确实证明 synchronized 不合适，而不要仅仅是假设如果使用 ReentrantLock “性能会更好”。请记住，这些是供高级用户使用的高级工具。
（而且，真正的高级用户喜欢选择能够找到的最简单工具，直到他们认为简单的工具不适用为止。）。一如既往，首先要把事情做好，然后再考虑是不是有必
要做得更快。
Lock 框架是同步的兼容替代品，它提供了 synchronized 没有提供的许多特性，它的实现在争用下提供了更好的性能。但是，这些明显存在的好处，还不足以
成为用 ReentrantLock 代替 synchronized 的理由。相反，应当根据您是否 需要 ReentrantLock 的能力来作出选择。大多数情况下，您不应当选择
它——synchronized 工作得很好，可以在所有 JVM 上工作，更多的开发人员了解它，而且不太容易出错。只有在真正需要 Lock 的时候才用它。在这些情况下，
您会很高兴拥有这款工具。

##Lock接口源码分析
Lock接口如下
```java
public interface Lock {
    void lock();
    
    void lockInterruptibly() throws InterruptedException;
    
    boolean tryLock();
    
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    
    void unlock();
    
    Condition newCondition();
}
```

###tryLock 方法
- 尝试获得锁，如果成功，返回立即返回true，否则，返回立即返回false。
- tryLock(long time,TimeUnit unit)：在一定的时间内尝试获得锁，并且在这段时间直接可以被打断。如果成功获得，那么将返回true，
否则，返回 false。

lockInterruptibly 方法
这里首先需要了解两个概念才能更好的理解这个方法：

- 线程的打断机制
- Thread类的interrupt,interrupted,isInterrupted方法的区别

对于线程的打断机制， 每个线程都有一个打断标志。
- 如果线程在 sleep 或 wait 或 join 的时候，此时如果别的进程调用此线程的 interrupt() 方法，此线程会被唤醒并被要求
处理InterruptedException；
- 如果线程在运行，则不会收到提醒。但是 此线程的 “打断标志” 会被设置。

所以说，对于 interrupt() 方法：
- 不会中断一个正在运行的线程 。
- 不会中断一个正在运行的线程 。
- 不会中断一个正在运行的线程 。

当通过这个方法去获取锁时，如果线程正在等待获取锁，则这个线程能够响应中断，即中断线程的等待状 态。例如当两个线程同时通
过lock.lockInterruptibly()想获取某个锁时，假若此时线程A获取到了锁，而线程B只有在等待，那 么对线程B调用threadB.interrupt()
方法能够中断线程B的等待过程。

###newCondition()
用于获取一个 Conodition 对象。Condition 对象是比 Lock 更细粒度的控制。要很好的理解 condition，必须要知道，生产者消费者问题。
<br>简单来说就是，生成者在缓冲区满了的时候需要休眠，此时会再唤起一个线程，那么你此时唤醒的是生成者还是消费者呢，如果是消费者，
很好；但是如果是唤醒生产者，那还要再休眠，此时就浪费资源了。condition就可以用来解决这个问题，能保证每次唤醒的都是消费者。
>参见demo

###ReentrantLock
可重入锁：指同一个线程，外层函数获得锁之后，内层递归函数仍有获得该锁的代码，但是不受影响。

可重入锁的最大作用就是 可以避免死锁。 例如：A线程 有两个方法 a 和 b，其中 a 方法会调用 b 方法，假如 a，b 两个方法都需要获得锁，
那么首先 a 方法先执行，会获得锁，此时 b方法将永远获得不了锁，b 方法将一直阻塞住， a 方法由于 b 方法没有执行完，它本身也不释放锁，
此时就会造成一个 死锁。

ReentrantLock 就是一个可重入锁。真正使用 锁的时候，一般是 Lock lock ＝ new ReentrantLock()； 然后 使用 Lock 接口方法。

###ReadWriteLock
接口如下
```java
public interface ReadWriteLock {

    Lock readLock();

    Lock writeLock();
}
```
ReadWriteLock 可以算是 Lock 的一个细分，合理使用有利于提高效率。比如说， 对于一个变量 i， A，B 线程同时读，那么不会造成错误的结果，
所以此时是允许并发，但是如果是同时写操作，那么则是有可能造成错误。所以真正使用的时候，可以使用 细分需要的是读锁还是写锁，再相应地进
行加锁。