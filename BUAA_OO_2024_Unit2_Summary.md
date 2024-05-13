[TOC]

# BUAA_OO_2024_Unit2_Summary

无论如何，第二单元的电梯就这样结束了。不管结果是好是坏，都应总结经验并希望给予后人启发。

如果你希望我的博客对你的代码思路有所帮助，请关注前两部分，他们分别介绍了我代码中对锁的设置，以及调度器的实现。如果你正在debug，请关注第四部分，其中介绍了我作业过程中遇到的各种bug。

## 〇、三次作业背景

鉴于以下几部分会按照作业顺序来讲，因此首先明确三次作业的作业背景及其要求会对后文的理解很有帮助。

### hw5

指定乘客所乘坐的电梯。

### hw6

+ 电梯可以收到`RESET`请求以重置电梯的`capacity`和`move`参数。
+ 不再指定乘客所乘坐的电梯，而是自行设计策略进行分配。
+ 要求输出`RECEIVE`语句以确定乘客被分配到了哪部电梯中。
+ 正在`reset`的电梯不能输出`RECEIVE`

### hw7

+ `RESET`请求分为两种：除了hw6中常规的`NormalReset`操作外，新增可以将电梯重置为双轿厢的`DCEReset`请求。
+ 双轿厢电梯限制：
  + A电梯只能运行在换乘层及以下楼层，B电梯只能运行在换乘层及以上楼层。
  + AB电梯不可同时位于换乘层。
+ 乘客依然自由分配。

## 一、调度器

### 生产者-消费者模式

根据理论课所讲的生产者-消费者模式，再结合作业的要求，我们自然而然可以联想到，输入器，调度器和电梯是很经典的生产者-消费者模型。

其中，输入器作为生产者不断输入乘客请求，电梯作为消费者不断处理请求，而调度器则根据某种策略，将输入的请求分配到不同电梯的“托盘”上。

因此，我在全局设置了类型为`ArrayList<Request>`的`waitingList`变量作为生产者；新建`Dispatch`类，作为调度器，实现调度策略；新建`requestList`类，类中存放分配给电梯的重置请求和乘客请求，再将该类的对象作为电梯的一个属性，以便电梯随时访问。

### 调度策略

调度策略在这里需要实现两部分：一是决定电梯在某一刻如何运行（即电梯是否开门/关门/上下移动/进人/出人……）的电梯运行策略；二是决定乘客请求分配给哪一部电梯的分配电梯策略。

#### 电梯运行策略

##### LOOK

电梯运行算法使用经典的**LOOK算法**，该算法的核心思想如下：

+ 初始化电梯运行方向，使该电梯按此方向移动。由于电梯初始在1层，所以可以初始化为向上。

+ 每到达一个楼层，首先判断是否需要开门。这里的判断包括两层：

  + 电梯里有人到达目的地。
  + 该楼层有乘客正在等待电梯，且该乘客的方向与电梯方向一致。

  以上两个判断任意满足其一即可开门，开门后注意关门。

+ 接下来根据电梯内是否有人分情况讨论：

  + 电梯里有人：继续沿当前方向移动。
  + 电梯里没人：此时讨论请求队列（`requestList`）的情况:
    + 请求队列为空，输入未结束：`WAIT`（等待）
    + 请求队列为空，输入结束：`END`（结束）
    + 请求队列不为空，且某请求的出发地与电梯当前的方向一致：`MOVE`（沿当前方向移动）
    + 请求队列不为空，且所有请求的出发地都与电梯当前的方向不一致：`REVERSE`（掉头，取相反方向）

##### 具体实现

在真正实现该策略时，新建`Strategy`类具体实现以上判断。每个电梯都有自己的`strategy`对象，其在新建时传入`requestList`和电梯内部乘客`passengers`，而在具体获得建议时，传入电梯当前所在楼层和方向等信息。

这样做的原因是，`requestList`和`passengers`传入的均为指针，`elevator`对二者做出的操作可以实时反馈到`stategy`中，而楼层和方向作为普通的变量，是值传递，在`Strategy`中维护代价较高，所以选择在获取运行建议时直接传入相应的值。

同时新建枚举`Suggestion`，其中有`END, WAIT, MOVE, OPEN, REVERSE, NORMALREST, DOUBLERESET`。

`Strategy`类中的`getSuggestion`方法，只需返回一个对应的建议即可。

具体实现可参考下方代码，注意：为了实现优先处理重置请求，在函数的开始首先判断是否需要重置。

```java
public Suggestion getSuggestion(int nowFloor, int direction, int capacity,
                                    int changeFloor, String type) {
        if (request.haveNormalReset()) {
            return Suggestion.NORMALRESET;
        }
        if (request.haveDoubleReset()) {
            return Suggestion.DOUBLERESET;
        }
        if (canOpenForOut(nowFloor) || canOpenForIn(nowFloor, direction, capacity)) {
            return Suggestion.OPEN;
        }
        if (!passengers.isEmpty()) {
            return Suggestion.MOVE;
        } else {
            synchronized (request) {
                if (request.isEmpty()) {
                    if (request.isEnd()) {
                        return Suggestion.END;
                    } else {
                        return Suggestion.WAIT;
                    }
                } else {
                    if (hasSameDirection(nowFloor, direction, changeFloor, type)) {
                        return Suggestion.MOVE;
                    } else {
                        return Suggestion.REVERSE;
                    }
                }
            }
        }
    }
```

#### 分配电梯策略

此处我所设计的分配电梯策略是根据电梯运行策略中的LOOK算法演化而来的。可以看到，在LOOK算法中，电梯始终朝着一个方向前进，因此，为了迎合这种特点，我们可以把与电梯具有相同方向的乘客分配给该电梯。而正在等待的电梯，我们可以看作其方向可上可下。并在这两种电梯中找距离该乘客最近的电梯。当然，也有可能所有的电梯方向都与其相反，此时我们找到一个请求队列中请求最少的电梯，以便该乘客能被尽快处理。

##### 双轿厢电梯

hw7中双轿厢电梯的乘客分配尤其需要注意，我在这里所采用的方法是：对于双轿厢电梯，乘客请求分为以下几种情况：

+ 出发点在换乘层，且目的地在换乘层以上：B
+ 出发点在换乘层，且目的地在换乘层以下：A
+ 出发点在换乘层以上：B
+ 出发点在换乘层以下：A

也即，在分配时即保证电梯至少可以接到乘客。

同时由于双轿厢电梯耗电量较少，在上述分配过程中，优先考虑双轿厢电梯。

##### 小结

将策略思想进行总结抽象为以下几个步骤：

+ 找到所有正在等待的电梯
+ 找到所有与乘客方向相同的电梯
+ 在以上两种电梯中，找到距离最近的电梯
+ 如果没有，在所有电梯中找请求最少的电梯
+ 在满足双轿厢电梯可达的条件下，优先考虑双轿厢电梯。

## 二、同步块和锁

### 何处加锁

在设置锁和同步块的过程中，主要有以下几点考量：

+ 是否有变量会出现多线程同时读写的情况？
+ 牵扯到该变量哪些语句应该放在同步块中？
+ 如何进行加锁？

分析代码，我们可以得到以下几点结论：

+ `InputHandler`线程和`Dispatch`线程以及`Elevator`线程共享`waitingList`数据
+ `Dispatch`线程和`Elevator`线程共享`requestList`和`waitingList`数据

因此，在所有线程使用到`waitingList`的时候，我都会采用`synchronized`关键字对其进行加锁。如下方代码所示：

````java
synchronized(waitingList){
	//...
}
````

而`requestList`由于是一个单独的类，所以只需为类中的每一个方法添加`synchronized`关键字修饰即可。

考虑哪些语句应该放在同步块中，下方代码是一个很典型的例子。同步块中均为使用到了`waitingList`的代码，而对于请求的分配（代码最后四行），其只是对于request的操作，在`request = waitingList.get(0);`后，剩下的操作已经与`waitingList`无关，并不需要放在同步块中。

`````java
synchronized (waitingList) {
     while (!end && waitingList.isEmpty()) {
         //...
     }
     if (end && waitingList.isEmpty()) {
         //...
         request = waitingList.get(0);
         waitingList.remove(request);
         waitingList.notifyAll();
     } 
}
     if (request instanceof PersonRequest) {
         dispatchPeopleRequest((PersonRequest) request);
     } else if (request instanceof ResetRequest) {
         dispatchResetRequest((ResetRequest) request);
     }
`````

### 何时notify

通过hw7的debug，对何时进行`notify`有了更深的理解。

先说结论：**在所有改变`wait`条件的地方进行`notify`操作**。

为了理解该结论，我们不妨先想一想，`notify()`的目的是什么？——唤醒正在等待的进程。那么为什么要唤醒？——`wait`的条件可能不满足了，一旦不满足，我们不再令其等待。

当然，改变条件的操作也可能不一定达到不再`wait`的要求，所以`wait`操作的外面往往伴随着`while`循环，每次被唤醒后都通过`while`循环检查条件是否被破坏，一旦破坏，`while`循环结束，也就是结束等待状态。

### 双轿厢不碰撞——lock的使用

在几次作业中我主要使用的是`synchronized`关键字实现加锁和同步，但有一处的实现例外：hw7中双轿厢电梯不能同时位于换乘层。

通过在AB两个电梯共享同一把`lock`，将进入换乘层的操作用`lock`上锁，进而确保两个电梯不会同时位于换乘层。具体实现如下：

````java
		if (shouldLock()) {
            lock.lock();
            try {
                //到达换乘层操作
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
````

## 三、DCERESET请求

如何实现`DCERESET`请求？我们首先分析`DCEReset`要求我们做些什么。

> 第二类重置请求将电梯修改为双轿厢电梯，重置参数包含需要重置的电梯ID，换乘楼层，两个轿厢的相关参数（移动一层的时间和满载人数）相同。当重置完成后，**轿厢 A 默认初始在换乘楼层的下面一层，轿厢B默认初始换乘楼层的上面一层**。

简而言之，执行`DCEReset`后，原来的电梯要被重置为双轿厢电梯。

但我们真的要新增两个电梯线程吗？是，也不是。

不是的原因是：为了便于管理，我们在调度器中应始终保持电梯的总量为6.

是的原因是：我们通过**继承`Elevator`类，实现`DoubleElevator`类**，而在该类中，确实新增了两个电梯线程。

这样一来，电梯进行重置时，只需要将原来电梯数组中的电梯替换为新建的双轿厢电梯即可。当进行电梯分配时，我们只需要额外判断当前电梯是否为双轿厢电梯，进行额外的操作即可。

需要注意，此时跟电梯有关的状态设置等方法在新建的`DoubleElevator`类中需要进行重写。

## 四、UML类图和协作图

### 三次作业的变和不变

不变：

+ 电梯内部的总体运行策略，一直都是LOOK算法。
+ 除hw5外hw6和7的电梯分配思路几近相同

变：

+ reset请求变化导致的调度器结束标识变化
+ 双轿厢电梯的加入使得调度器和电梯具体运行都进行了不同程度的调整。

### UML类图和协作图

#### hw5

![](D:\IntelliJ IDEA 2023.2.1\projects_oo\oohomework_5\hw5.png)

![](D:\IntelliJ IDEA 2023.2.1\projects_oo\oohomework_5\hw5_sequence.drawio.png)

#### hw6

![](D:\IntelliJ IDEA 2023.2.1\projects_oo\oohomework_6\hw6.png)

![](D:\IntelliJ IDEA 2023.2.1\projects_oo\oohomework_6\hw6_sequence.drawio.png)

#### hw7

![](D:\IntelliJ IDEA 2023.2.1\projects_oo\oohomework_7\hw7.png)

![](D:\IntelliJ IDEA 2023.2.1\projects_oo\oohomework_7\hw7_sequence.drawio.png)

## 五、Buggg！

### RTLE

笔者在debug过程中遇到的`RTLE`主要有以下两点原因：

+ 结束标志设置错误

+ 存在进程一直在`wait`没有被唤醒

下面就这两点分别阐述。

#### 结束标识

##### InputHandler

输入线程的结束标志较为简单：数据投喂结束，则`InputHandler`线程结束，可以`break`出while循环。

但我们考虑一个问题，此时仅仅令其`break`足够吗？显然不是，我们需要把这个输入结束的标识通知给调度器，让调度器知道此时输入已经结束了，以便调度器在合适的时刻结束她自己的线程。

因此，我们在调度器中设置一个`end`标记位（注意：该标记位只标记输入结束，不标记调度器线程最终结束），在输入线程结束之前，调用方法设置调度器的`end`标记位为`true`。

##### Dispatch

为了帮助后续理解bug，我首先介绍调度器在`break`结束线程之前需要干的一件事情：

+ 为所有电梯的请求列表设置`end`标记位。同样，这里的`end`标记位不代表电梯线程结束，而是告知电梯线程不会再有新的请求被分配。

从这里我们可以看出，**电梯能否正常结束，和调度器线程是否对其置`end`标记位有直接因果关系**。换言之，假如**调度器没有对电梯设置结束标记位，电梯将永远等待而不会结束**（这与后文hw7的bug有关）。

下面我们开始讨论调度器线程结束的条件：

+ 首先，一个很自然而然的想法：输入结束并且`waitingList`为空时，调度器结束。

此想法在hw5还勉强够用，因为此时没有`RESET`请求，`waitingList`为空确实可以标记乘客请求已经处理完毕，不会再有乘客请求需要调度器进行调度。但是在hw6，hw7中，该想法有一个致命的错误：**由重置请求导致的乘客换乘需要调度器重新调度，也就是说，即使此时等待队列为空，也无法说明乘客请求已经处理完毕。**

+ 于是我们抓住问题的要害，既然现在需要判断乘客请求是否已经处理完毕，我们就在原有条件的基础上，添加：**所有乘客均已到达目的地。**

为了实现上述判断，我们可以在调度器中维护一个`HashSet<Integer> allPassengers`，该容器储存所有输入中的乘客请求的乘客id。之所以选用`HashSet`，是为了利用其中元素的不可重复性，这样，即使有乘客换乘而重新进入调度器，也不会造成重复计算。

同样的，我们在每个电梯中维护一个`HashSet<Integer> solved`，每当有乘客因为到达目的地而离开电梯时，将该乘客的id填入容器。

最终，我们比较`allPassengers`的大小和六个电梯的`solved`大小之和，相等则说明乘客处理完毕。

此想法较为成功地完成了hw6的任务，但显然在hw7中仍不适用。因为我们忽略了一个问题：**假如全是`DCERESET`请求呢**。

根据上文`DCERESET`请求的具体实现，我们知道，每当有电梯需要重置为双轿厢电梯时，程序均会新建两个电梯线程开始工作。如果我们仍然按照上面的结束条件计算，就会发现，调度器线程在**输入结束后**就给每个电梯设置了end位，而**此时有的电梯还没有完成重置请求**。回顾本小节最开始的部分，我们知道，有且仅有调度器才能够给电梯置标志位。这就会导致一个问题——**重置后的双轿厢电梯一直在`WAIT`状态，始终不会结束，因为早在建立该电梯线程前，调度器就已经结束工作了**。

+ 解决办法类似，我们尝试在原有条件上再加一条：**RESET请求处理完成**。

就具体实现而言，我们在调度器中新增两个变量，一个记录重置请求个数`resetNum`，另一个记录已经完成的重置请求的个数`finishNUm`。

每当分配重置请求时，`resetNum++`；每当有重置请求完成时，由完成请求的电梯调用`addFinish()`方法以修改`finishNum`的值。

最后，当`resetNum == finishNum && end && waitingList.isEmpty() && allSolved() ` ，时，调度器线程结束。

自此，结束条件及其bug讨论完毕。

##### Elevator

电梯线程的结束标识较为简单，我们只需考虑其请求队列为空，且调度器结束工作即可。

#### 唤醒

此处考虑这样一个问题，当调度器线程并不符合结束条件时，我们需要做什么？

答案显而易见，我们需要令`waitingList`等待，直到条件满足。而根据第二部分对`notifyAll()`的阐释，我们知道，需要在令`wait`条件改变的地方添加相同对象的`notifyAll()`方法。

然而此处我只在有乘客到达目的地时进行了唤醒，而在重置请求完成后忘记唤醒，导致bug产生。添加后程序即可正常运行。

### Can not receive people when elevator is resetting

对这个问题的解决经历了以下几个版本的迭代：

+ 最初：在电梯中设置标记位记录电梯是否正在重置，该标记位在电梯实现重置方法的开头标记为`true`，而在方法结尾设置为`false`，在调度器中进行电梯分配时，假如检测到电梯正在重置，则跳过该电梯。

  问题：标记位设置得较晚，容易出现已经开始重置但仍然在接受请求的情况

+ 迭代1：在识别到重置请求后就设置标记位

  问题：这样做确实解决了不接人的情况，但忽略了假如五部电梯都开始重置，只剩一部电梯正在运作，恰好此时有大量请求进入，全部分给一个电梯进而导致超时的极端情况。

+ 迭代2：分配电梯时不再跳过正在重置的电梯，而是在电梯的请求队列`requestList`中新增容器`ArrayList<PersonRequest> buffer`，当电梯正在重置而又分配到乘客请求时，先将该请求放入缓冲器，当重置结束后，将缓冲器中的请求加入正常的请求队列，并输出`RECEIVE`。

### 本单元适合debug的方法

带有时间戳的打印！！！

+ 在线程结束的地方打印，可以根据打印结果判断是哪个线程没有结束。
+ 在`while`循环的开头打印，可以判断是哪个线程一直循环占用cpu资源导致`CTLE`。

## 五、心得体会

第二单元的每一次作业几乎都让我汗流浃背，但从中获取到的编程思路和多线程知识是让人受益无穷的。

### 线程安全

随着作业的进行，对锁的理解也在逐步深入。到底什么时候加锁，对什么对象进行加锁，同步块里该放什么语句，避免死锁等等问题都是在编程中随时随地都需要注意的问题。

### 层次化设计

本单元第三次作业使我对层次化设计有了深深的认识。。。

在第一单元，因为大家普遍有一个很共同的思路，这间接减少了我思考架构的难度。然而在第二单元，大家的实现方法各有不同，无法借鉴，我笨人架构的缺点也在第三次作业的电梯类中展现无遗——`Elevator`类超过了五百行。当然最终没有超过五百行，但也是牺牲了一些代码规范所得来的。

重看电梯类代码，现在感觉确实因为思路不清晰而导致一些代码冗余地放在`Elevator`类中，没有做到单一职责原则。同时可以看到，因为架构的原因，后两次迭代对代码的改动较大，不符合开闭原则。

### Ending

从hw6没进互测，到hw7因为一个bug被hack了11次，过程曲折坎坷，而我竟然开始逐渐享受oo写代码的过程。

我也不再懊悔自己这单元不尽人意的表现，我付出的每一滴汗水都做数。

感谢本单元给予莫大帮助的同学和助教，没有你们就没有现在还能平安无事坐在这里写博客的zhw/(ㄒoㄒ)/~~

最后感谢wtls，他是照进我时常黑暗世界里的一束光。


