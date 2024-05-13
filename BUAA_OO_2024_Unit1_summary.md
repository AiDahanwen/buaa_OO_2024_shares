# BUAA_OO_2024_Unit1_summary

22373321 张瀚文

第一单元的核心：**递归下降法处理复杂表达式**。在这篇文章中，我会从不同方面对自己第一单元的代码做出分析，并总结自己的收获。

如果你需要我在代码思路上有所帮助，请仔细阅读文章的第一部分。

## 一、架构设计、remake与程序bug

### hw1—stack

第一次作业的要求：单变量多项式的括号展开，表达式包括加、减、乘方以及括号。

第一次作业中，我的思路来源主要是课前给出的training练习。在training的代码中，输入的表达式通过递归下降法被构造成语法树的结构，又通过接口在每个因子中实现了`toString()`方法，最终将语法树以后缀表达式的形式输出。

同时，由于第一次作业的**要求较为简单**，只牵扯到了x的乘方。我非常自然地沿用了training中的代码，甚至认为**后缀表达式**是解决hw1的极佳策略。

（事实的确如此，即使后来remake了所有代码，我仍然认为hw1的结构十分优美。）基于training的代码，我新建了`Equation`类，并在其中设置`ArrayList`以存放表达式项的信息（此处有bug，后面会讲）。

具体来说，对于`expr`中的每个项，我将系数当作值存放在`ArrayList`中，而下标就是对应的x的次方。举个例子方便理解，`x^2+1`在`ArrayList`中的存储即为`0:1,1:0,2:1`。

此时，我只需要维护一个栈结构即可实现对后缀表达式的运算。每个进入栈的表达式都被我解析成ArrayList并新建Equation对象存储。而在Equation类中，我分别设置add，mult，power方法用以计算加，乘和乘方。

`````java
public Equation add(Equation equ){
	\\...
}

public Equation mult(Equation equ){
	\\...
}

public Equation power(\\...){
	\\...
}
`````

每当识别到运算符时，就取出栈顶的两个equation（或者1个），调用对应的方法，计算出新的结果，存回栈中。注意，这里为了计算，我把次方也理解成运算符。这样计算到最后，栈中剩下的唯一一个equation，就是最终的化简结果。

我在第一次编写代码时并没有意识到`ArrayList`的存储会有很大的问题。即使这样存储需要初始化（因为在`ArrayList`中我不可能跳过下标为1的元素而直接设置下标为2的元素的值）。甚至采用该方法顺利通过了公测。

但后来我将自己的代码放在大佬们写的测评机上跑的时候，终于发现，当指数不断叠加，例如`((x^8)^8)^8....`，我的代码就会出现问题。很明显，如果继续使用`ArrayList`，就必须花费心思判断究竟要初始化多少元素个数合适，因为根本无法找到一个统一的上界。但是真的要这样做吗？我们无非**只是想将系数和其x的次方相对应**。很明显，**`HashMap`**才是最好的选择。幸好，只是数据的存储方式有所变化，具体的实现方式无需做太大的改动。

### hw2—remake

第二次作业新增要求：支持嵌套多层括号、支持自定义函数、新增指数函数因子。

我在看完第二次作业要求的时候属实大吃一惊，因为我hw1的架构似乎完全无法支持新增的指数函数因子。我就算再怎么嵌套`HashMap`也无法像hw1那样把每一项都优雅地储存在数组中了。

但我们可以首先解决自定义函数的问题。考虑到开闭原则，我认为在进行解析之前就通过替换把函数代入表达式是比较正确的做法，这样可以避免对已经成型的解析表达式的函数进行大量的修改。替换的思路也非常简单，识别到函数名，将函数前后的字符串取出，解析出参数代入函数的定义式，再和原来的两部分字符串拼接。但是这里有两个地方需要注意：

+ 为了防止重复替换，我们需要提前将函数定义式中的x,y,z替换成没有出现过的字符，比如a,b,c。
+ 自定义函数因子可能出现在多个位置，在返回的时候需要在整个表达式的两侧加上括号。

对于整体的架构，在研讨课上和同组的大腿哥讨论过后，我认为确实有必要remake了，不单单是为了hw2，也为了即将到来的hw3。（可见迭代意识稍微增强了一点。）在remake的过程中，我首先参考了hw1中zyt的帖子，并顺着他的帖子去看了hygggge学长的博客。最终才算是把比较主流的代码思路稍微理清了一些。（PS：讨论区真的是好地方，对代码一筹莫展之际看看大佬们的帖子真的很让人茅塞顿开）

新的思路的核心观点是：**为表达式的每一个项设置统一的表达形式Mono，并设置Poly类对其进行管理**，在Poly和Mono中设置必要的方法以进行计算。

容易得到，统一的表达形式可设置为`a*x^b*exp(polyofExp)`，Mono类中的属性也据此可以确定（注意数据类型，大腿姐因为数据类型没改强测被挂了几个点，非常可惜）。而Poly实际上是多个Mono的集合，因此在Poly中设置ArrayList以储存Mono。

那么，如何将解析好的各个项转化为Poly呢？仿照training中转化为后缀表达式的做法，我们在factor中设置`toPoly()`接口，再由各类因子分别实现这个接口。这实际上就是将解析好的语法树结构合在一起的过程。自上而下去看，expr是由term组成的，term和term是相加的关系；term是由factor组成的，factor和factor之间是相乘的关系。因此我们有：

````java
public Poly toPoly() { //expr的toPoly()方法
    	//...
        Poly poly = new Poly();
        for (Term term : terms) {
            //....
        }
        //...
}

public Poly toPoly(){ //Term中的toPoly方法
    //...
    for (Factor factor : factors) {
            //...
    }
    //...
}
````

此时需要我们提前在Poly中写好对应的运算方法，如addPoly，multPoly等。

这些方法看似简单，实则隐藏了一个非常难以察觉的问题：**深浅克隆**。此问题导致的bug令人摸不到头脑，除非一步步调试，否则硬看很难看出问题。

#### 深浅克隆

##### 问题分析

下面我举出hw2中我所错过的例子：

``````java
 public Poly multPoly(Poly poly) {
        Poly poly1 = new Poly();
        for (Mono mono1 : monos) { 
            for (Mono mono2 : poly.getMonos()) {
                Mono resMono = mono1.multMono(mono2);
``````

其中`multMono`方法相关代码如下：

````java
 if (polyOfExp != null && temp != null) {
            res3 = polyOfExp.addPoly(temp);
````

更值得关心的是，笔者的`addPoly`方法实现如下

````java
 for (Mono mono1 : poly.getMonos()) { //要加进来的数字
           //...
            for (Mono mono2 : monos) {
                if (mono2.canMerge(mono1)) { //2和1可以合并
                mono2.setCoefficient(mono1.getCoefficient().add(mono2.getCoefficient()));
                    //...
                }
            }
````

可以看到，该`addPoly`方法为了实现上的便利，并没有真的新建一个`Poly`对象，而是在原有`Poly`的基础上修改其系数，合并同类项，也就是仅仅实现了浅克隆。

该做法毫无疑问给`multPoly`方法埋下了巨大隐患。我们首先分析`multMono`方法，当两个相乘的`Mono`都含有exp指数函数（即两个poly都不为null）时，我们此时应当做的是把两个poly相加。乍一看，可能会认为该方法并无问题，然而，正因为`addPoly`并没有new出一个新的对象，因此此时我们执行`addPoly`操作，实际上是改变了`mono1`的`polyofExp`的值。回到`multPoly`方法，由上述做法带来的后果是，当`mono2`循环结束，重新取`mono1`时，`mono1`的`polyofExp`已经是第一轮乘完之后的结果了，不再是我们想要的最初值，也就给计算带来了巨大的错误。

##### 解决方案

通过问题分析，实际上我们可以总结出以下这句话：**如果是对数据只读，传递指针即可，但如果是需要修改数据，则必须对数据进行深层次的克隆，以防对原始数据造成破坏**。

所谓深克隆，就是在内存中新开辟一段空间，用来储存和被克隆对象一模一样的数据。

`Poly`是由很多`Mono`组成的，因此深克隆方法从`Mono`开始写起：

````java
public Mono creatSame(){
    //...
		 if (polyOfExp == null) {
            newMono = new Mono(newExp, newCo, null);
        } else {
            Poly newPoly = polyOfExp.creatSame();
            newMono = new Mono(newExp, newCo, newPoly);
        }
}
````

那么在Poly中深克隆可以写作：

````java
public Poly creatSame() {
        Poly newPoly = new Poly();
        for (Mono mono : monos) {
            newPoly.addMono(mono.creatSame());
        }
        return newPoly;
    }
````

至此hw2的主要思路介绍完毕，除了深浅克隆的大bug之外，下面记录一些细微但很关键的小bug。

+ 在冗余符号处理的时候，忽略了变量已经从只有x到增加了yz。（此bug挂了我公测的一个点，de了一个多小时。。。。）
+ 在自定义函数解析的时候错误的认为只要扫到了右括号就可以结束，忽略了因子中也可以有括号的情况。事实上这个可以通过记录括号个数解决。具体来说，设置变量`count`，当扫到左括号，count+1；扫到右括号，count-1。count为0时，整个函数才算全部结束。

### hw3

第三次作业新增要求：支持自定义函数定义时使用已定义的函数，增加求导因子。

根据我hw2对自定义函数的处理，第一个要求已经自动满足。因为我在替换的时候使用while循环，只要表达式中仍然存在函数名，就进行相应的替换。难倒我的任务交给了求导因子。

但是根据我hw2的架构，其实求导思路已经相当清晰。我只需要新建求导因子类，并在Mono类和Poly类中新增求导方法即可。求导因子的`toPoly`过程实际上就是求导的过程。

````java
public Poly toPoly() { 
        return deFactor.toPoly().derivate();
}
````

至此，第一单元代码作业完成。

## 二、度量分析

分别统计三次作业的代码规模，以及各类复杂度信息。

### 复杂度含义解释

+ CogC(Cyclomatic Complexity)：圈复杂度，表示程序中的独立路径数目。衡量一个代码单元直观理解的难易程度。
+ ev(G)：基本复杂度，用来衡量程序**非结构化程度**。ev(G)值越高通常意味着代码结构混乱、逻辑不清晰，难以理解和维护。
+ iv(G)：模块设计复杂度，用于衡量模块之间的调用关系。复杂度越高，模块之间的耦合性越高，越难以隔离、维护和复用。
+ v(G)：圈复杂度，用于衡量一个模块判定结构的复杂程度。圈复杂度越大说明程序代码的判断逻辑越复杂，质量低，且难以测试和维护。

### 各次作业分析

+ 第一次作业：

  <img src="C:\Users\28952\AppData\Roaming\Typora\typora-user-images\image-20240321230535908.png" alt="image-20240321230535908" style="zoom:50%;" />

  <img src="C:\Users\28952\AppData\Roaming\Typora\typora-user-images\image-20240322141741594.png" alt="image-20240322141741594" style="zoom: 80%;" />

  第一次作业中，处理空格、前导0和冗余正负号的操作，以及主要的进栈出栈操作等都放在Other工具类中，导致Other类的行数较多。

  同时，Other类中的`removeSign`方法的功能是去除输入表达式中的冗余正负号。由于这个方法中我的思路不是太清晰，分了很多情况，导致这个方法的各种复杂度较高。

+ 第二次作业：

  <img src="C:\Users\28952\AppData\Roaming\Typora\typora-user-images\image-20240321230623352.png" alt="image-20240321230623352" style="zoom:50%;" />

  <img src="C:\Users\28952\AppData\Roaming\Typora\typora-user-images\image-20240322144116607.png" alt="image-20240322144116607" style="zoom:80%;" />

  第二次作业中，主要新增的Mono类和Poly类由于操作较多，因此行数较高。

  复杂度方面，Mono中负责转换`a*x^b`部分的`determineStr`方法由于分类较多，且条件判断时大量使用外部函数，导致代码的耦合度较高。

+ 第三次作业：

  <img src="C:\Users\28952\AppData\Roaming\Typora\typora-user-images\image-20240321230123360.png" alt="image-20240321230123360" style="zoom:50%;" />

  <img src="C:\Users\28952\AppData\Roaming\Typora\typora-user-images\image-20240322145200516.png" alt="image-20240322145200516" style="zoom:80%;" />

  由于hw3相对于hw2只是新增了求导方法，故而复杂度分析上并无太大差异。值得注意的是Mono类中新增的`derivate`求导方法的`ev(G)`复杂度较高，可能是因为该方法中`if-else`的嵌套较多，显得代码结构比较混乱。

## 三、UML类图

本次UML类图使用PlantUML生成。

+ hw1：

  ![](D:\OO正课\各单元总结\u1_hw1_uml3.png)

+ hw2：相较于hw1进行了全方位的重构

  <img src="D:\OO正课\各单元总结\ZLPRJzim57xlhx1u8OoM1nuZXDYc8g7HYrOqJPlsODBJrYAnCzk1Ljl--ywndSHfK-2XTVoTgu-_O_l6MAvjLUI3GPPpOzXuMshsu1_hw2_uml.png" style="zoom:150%;" />

+ hw3：

  <img src="D:\OO正课\各单元总结\u1_hw3_uml.png" style="zoom:150%;" />

## 四、优化策略/技巧

鉴于笨人水平较菜，每次把最基本的功能完成就已经是周五凌晨甚至是周六下午，因此并没有进行太多的优化，想要了解如何巧妙地缩小表达式长度的技巧可移步其他大佬的博客。下面介绍我认为最为基础也是非常容易实现的一些优化策略，或者一些技巧（？）

### 合并同类项

合并同类项应该是必须要做的优化，否则不仅很难和别人对拍，出现错误后由于繁杂的式子也很难定位bug。

首先是如何判断，能够合并同类项的式子具有以下特征：

+ x的指数相等
+ exp函数内部的指数相等

我们就此写出对应的判断方法即可。不过这里有一个很细微的点需要体会：鉴于exp函数内部的指数我们以Poly对象的形式存储，Mono类中其实涉及到两种类型的判断，一个需要系数也对应相等，另一个则不需要。前者用于判断exp是否可以合并，后者判断项能否合并同类项。

不难看出，这里的Poly和Mono相互嵌套。但是实际上，这个递归的思路和深克隆里面的代码思路非常类似，可以参考上方深克隆的代码写出判断相等/能否合并同类项的代码。

### 字符串的输出

最后poly结构解析完毕，我们便需要将每一项从一个一个的属性转化成字符串的形式进行输出，也就是`toString()`方法。下面介绍我所优化的几个点以及怎么写能让思路更清晰一些的技巧（？）

+ **把正项提前。这样可以减少一个加号的长度**。具体来说，我首先遍历找到poly中第一个为正的mono，将其存储并输出，在之后的输出中，如果再次遇到该mono，就跳过不再输出。
+ **多写判断函数**，以简化条件判断的内容，同时增加代码的可读性，便于debug。比如我在Mono类中就写了`isZero` `hasOneX` `hasX` `hasExp`等等判断方法用于处理分类不同的情况。
+ **输出有层次，有条理，有顺序**。以mono的`toString`为例：
  + 将`a*x^b*exp(polyOfExp)`划分成两部分，`a*x^b`和`exp(polyOfExp)`。
  + 对于第一部分，新建`determineStr`方法处理。
  + 对于第二部分，我们只需要将字符串`exp`和`polyOfExp`调用`toString`方法生成的字符串相连，并添加必要的括号即可，最后再用乘号将两部分相连。
  + 此处使用`isFactor`方法判断exp的指数是否是一个因子，如果是，则不需要添加括号，否则需要添加括号。
  + 除此之外，一些特殊情况，比如系数为0，可以放在方法的最开头特殊处理。

## 五、hack体验

三次互测我的情况如下：其中第一次和第三次互测我所在的房间都是全0房，即大家相互hack地非常起劲，但是没有一个是成功的。第二次互测我成功hack了5次，我记得非常清楚，当时一个0*0就成功hack了三个人。

但是非常遗憾，我**第一单元的互测手段非常盲目**。第一次是自己构造了一些乱七八糟的复杂数据，第二次是一直在试特殊情况，第三次是直接跑了别人的评测机，也没有跑出来什么有用的结果。

在第二单元的互测中，我应该改变自己盲目hack的行为，通过自己**构造尽可能全面的样例+阅读别人的代码**以提高hack的成功率。

## 六、我的收获

### 工具

首先总结一下本次博客中使用到的插件和工具：

+ 统计代码规模：statistic插件
+ 度量代码复杂度：MetricsReloaded插件
+ 绘制UML类图：PlantUML

### 心得体会

+ 在捋不清已成形的代码思路时，不要自己空想，而是拿一个例子放在代码里跑一跑，自己一步一步调试，**在实例去理解每一行代码的含义**。此条尤其适用于对Poly和Mono相互嵌套的地方。
+ **好的架构**至关重要！！！！如果不是hw2的remake，我的hw3也不会完成的那么顺利。在即将开始的第二单元，务必注意代码的可扩展性。
+ **多和同学交流，多看讨论区的精华帖。**
+ **输入和输出一定要单独设立方法，一定要和main函数分离**！

### 研讨课

+ 第二次研讨课上ysz同学的发言对我启发很大。他的大意是对于工具类，将方法都设置成静态的，这样在使用工具类中的方法时，无需new一个新的对象。而我并没有想到这一点，所以每次使用Other类时，都new了一个新的对象，这是非常没有必要的。

## 七、未来方向

+ 在第一单元因为各种各样的原因没搭成自己的评测机，而是一直蹭各位大腿哥的测评机，我感到非常惭愧。**在第二单元中一定要搭出自己的评测机**！哪怕非常简陋，也是在迈出勇敢的一步。
+ **多在讨论区分享自己的思路看法**，也许就可以给他人带来帮助，这于我而言是一件非常幸福的乐事。

## 八、特别感谢

第一单元作业能够顺利完成，离不开以下几位同学的帮助：深夜一起debug写代码的tx老师；对于我的一些问题给出解答的xm佬和yt佬；对我hw2重构帮助巨大的ting姐；在关键时刻施以援手的助教；以及一直支持（投喂）我的npy，在此一并谢过。(●'◡'●)


