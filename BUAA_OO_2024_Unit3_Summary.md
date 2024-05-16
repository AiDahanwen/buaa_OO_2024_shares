# BUAA_OO_2024_Unit3_Summary

[TOC]

第三单元主要学习JML——对Java程序进程规格化设计的表示语言。其作用是为Java代码提供精确的规格描述。

通过三次作业的迭代开发，我们实现了一个简易的社交网络，并提供增加关系，发送信息的等功能以及各类查询服务。

如果读者需要在代码思路上获得提示，请仔细阅读第二部分；如果读者苦于不通过的junit测试点，请关注第三部分；此外，第四部分记录了作者在代码实现过程中出现的bug，读者可按需查看。

## 一、测试过程

### 黑箱测试和白箱测试

+ 黑箱测试，又称为功能测试。主要是对程序的功能和接口进行测试，而不考虑内部实现的细节。

  换句话说，黑箱测试就是在检测代码能否按照规格正常工作，给定输入，看程序的输出是否正确。

+ 白箱测试，也称为结构测试。通过阅读程序内部的代码，检测程序中的各个路径是否按照要求正常工作，是对每个模块进行的较为精细的测试。

根据以上描述我们可以发现，黑箱测试由于不考虑程序内部的细节，在构造测试样例时不可能检测到所有的代码，因此覆盖率较低。

而白箱测试虽然能大大提高测试的覆盖率，但测试基于代码，只能检测代码的编写是否正确，而不能测试代码功能的正确与否。

因此，黑箱测试和白箱测试是互为补充的关系，二者缺一不可。我们在互测时也需注意这一点，不能仅仅依靠评测机的黑箱测试，还要多阅读别人的代码，通过白箱测试寻找bug。

### 单元测试、功能测试、集成测试、压力测试和回归测试

+ 单元测试是针对软件的最小功能模块进行测试的方法。需要针对每个函数/方法编写测试用例，以验证其在各种输入条件下的正确性。

+ 功能测试是对整个软件系统的功能进行测试的方法。测试用例根据需求规格说明书编写，并通过输入不同的数据或者操作软件的不同功能来验证软件是否满足设计要求。

+ 集成测试是在单元测试之后，将各个功能模块组装在一起进行测试的方法。集成测试重点验证各个模块之间的接口和交互是否正常，并且整个系统是否能够正常工作。

+ 压力测试是对软件系统在高负载和高并发情况下进行测试的方法。测试中会模拟大量用户同时访问系统，并且增加系统负载，以评估系统的性能和稳定性。

+ 回归测试是在软件系统进行修改或升级后，重新运行之前的测试用例以验证修改是否引入新的错误或导致原有功能出现问题的方法。回归测试一般会在每次修改或升级后进行，以确保软件的稳定性和质量。

可以看到，这几类测试在软件开发的不同阶段进行，各有侧重，是不可或缺的测试方法。正是有了这么多检测不同方法的测试方法，才能保证开发出的软件的质量。

## 二、架构设计和性能

鉴于U3作业的基本架构已经被官方包限制，因此在这里我主要分析实现部分查询服务的算法和为了提高性能的代码策略。

### 算法

#### 并查集

`query_Block_Sum`指令查询当前社交网络中连通块的个数。为实现这一功能，可以使用并查集算法。

+ 概念：并查集是一种树型数据结构，用于处理不相交集合的合并以及查询问题。如判断图中的两个节点是否属于同一个连通分量，判断图中有多少联通的部分等。
+ 基本思想：并查集算法通过维护一个父节点的数组实现元素分组。
  + 初始时每个元素的父节点均为本身。
  + 当合并两个集合时，通过修改父节点数组，将其中一个集合的根节点的父节点指向另一个集合的根节点，进而实现两个集合的合并。
  + 在查询两个元素是否属于同一个集合时，可以通过查找他们的根节点，判断是否相同。

将该算法的思想置于社交网络的语境下分析，我们不难得到：

+ 查询的`Block_Sum`等于并查集中的集合个数
+ `add_Person`相当于在并查集中新添加了一个元素
+ `add_Relation`相当于在并查集中合并两个集合
+ 在`modify_Relation`中，如果`person1`和`person2`的关系解除，则需要将原来联通的集合分离。

具体实现上：

+ 在并查集类中维护变量`blockSum`，每当有元素新建或新的集合增加时加一，每当有集合合并时减一，`query_Block_Sum`只需在每次查询时返回该变量值。

+ 添加元素和合并元素的操作较为基础，并查集中给出了标准的写法，实现起来并不困难。

+ 显然，关键在于第四点，并查集没有提供删除操作。我们现在要做的就是实现这个方法。

  + 删除的核心要义在于：找到`person1`中所有联通的元素，将其父节点全部改为`person1`（包括`person1`本身）。这样，当`person1`和`person2`删除关系后，所有和`person1`联通的元素都将以`person1`为父节点，形成一个新的集合，从原来和`person2`联通的集合中脱离出来；同理，我们对`perosn2`也进行类似的重构。
  + 当然，这种做法有一种特殊情况：如果`person1`和`person2`即使删除关系后也通过一个中间人联通呢？我们只需要在找到所有与`person1`联通的元素后检查`person2`是否包含在其中，如果是，则说明`person1`的重构即包含`person2`及与其联通的元素，就不再需要第二部分的重构了。
  + 至于如何找到`person1`中所有联通的元素，可以参考dfs算法。

  示例代码如下：

  ```java
  public void delete(MyPerson person1, MyPerson person2) {
      HashSet<Integer> allLinked = new HashSet<>();
      //...
      boolean change = true;
      int id1 = person1.getId();
      int id2 = person2.getId();
      for (Integer i : allLinked) {
          if (i == id2) {
              change = false;
          } 
          //...
      }
      if (change) {
         //...
      }
  }
  ```

+ 除此之外，在`find`查找根节点的过程中，我们可以搭配使用路径压缩，以减小查找时间。示例如下：

  ```java
  public int find(int id) {
      int tempId = id;
      while (parents.get(tempId) != tempId) {
          tempId = parents.get(tempId);
      }  
      int begin = id; 
      while (begin != tempId) {
          int parent = parents.get(begin);
          parents.put(begin, tempId);
          begin = parent;
      } //路径压缩
      return tempId;
  }
  ```

#### BFS

`query_Shortest_Path(id1, id2)`指令查询从`person1`到`person2`的最短路径。回顾大一学习的数据结构，我们可能会联想到Dijkstra算法。不过，鉴于这学期的路径没有权重，我们其实可以考虑更为简单的BFS算法。（sorryyy，其实只是因为作者水平太菜 :(  

BFS即广度优先搜索算法，其基本思想是：

+ 初始时，每个节点的`predecessor`为`null`，`searched`标记位为`false`

+ 维护一个`queue`队列
+ 将起点加入队列
+ 循环直至队列为空
  + 在循环中，我们每次取队首元素，如果是被搜索过的，则继续循环
  + 如果之前没有被搜索过，则：
    + 判断元素是否为终点元素，如果是则统计路径长度并返回
    + 如果不是，则遍历和该元素联通的点，如果这些点的`predecessor`为`null`，且不为起点，则设置其`predecessor`为该元素，并将这些点加入队尾
    + 最后标记该元素的`searched`位为`true`

代码示例：

```java
public int findShortestPath(Node start, Node end) {
    LinkedList<Node> queue = new LinkedList<>();
    queue.addLast(start);
    while (!queue.isEmpty()) {
        Node node = queue.pollFirst();
        if (node.isSearched()) {
            continue;
        }
        if (node.getId() == end.getId()) {
            return countPath(end);
        } else {
           //...
    }
    return -1;
}
```

+ 在统计路径时，我们使用`while`循环回溯终点的`predecessor`，直至其为`null`，也就是找到了起点。并在过程中用`count`计数。由于题目中问的是节点的个数，因此最后返回时需要将`count-1`
  + 隐藏bug：当起点和终点相同时，需要特判返回0
+ 还有个bug，在每次执行完后都要记得把所有元素初始化一遍，即`predecessor`重新设置为`null`，`searched`置位`false`，以便下次查询。

### 性能

观察社交网络中需要实现的功能，真正耗费时间的往往是复杂的query类指令。而在大多数情况下，query询问的只是一个特征值。如果我们能在数据变化时就对这个特征值进行修改，最终询问时只需返回储存计算好的特征值，将时间复杂度降为O(1)，这毫无疑问大大优化了性能。基于此，动态维护和改时更新的意义就不言而喻了。

#### 动态维护

+ `query_Triple_Sum`，该指令询问关系网络中三角关系网的个数。显然，只有`add_Relation`操作和`modify_Relation`中的`unlink`操作会使三角关系网的个数发生改变。因此，我们只需编写`maintainTriples`方法计算三角关系网的个数，并在对应的位置调用即可。

+ `query_Tag_AgeVar`，该指令询问Tag中包含person的年龄方差。根据方差的计算公式，我们只需每次在`add_Person_to_Tag`的时候更新`ageSum`和`agePowerSum`，即可以O(1)的复杂度计算得到方差。

+ `query_Best_Acquaintance`，该指令询问与`person`关系最好的id。可在每次`addAcquaintance`和`modifyValue`以及`unlink`时进行判断更新。同时，当不能直接判断而是要重新排序时，可将存好的数据结构改为数组，重写`compareTo`方法，接着调用数组自带的`sort`方法即可。

  ```java
  public void sortAndUpdate() {
     //...
      Value[] build = sortedValues.toArray(new Value[0]);
      Arrays.sort(build);
    //..
  }
  ```

以上是几个典型的例子，在本单元的作业中类似的例子还有很多。

#### 改时更新

改时更新的核心思想是：

+ 针对一些不便于动态维护的方法，可设置一个脏位标记。当对相关的数据有所修改时，将脏位设置为true，否则仍为false。
+ 当脏位为false时，返回旧值；否则，重新计算并返回新值。
+ 同时，该方法避免了不必要的更新，即只在调用该询问，且确实需要更新时，才全部进行重新计算。

本单元作业中最为典型的例子是`query_Couple_Sum`指令，该指令询问相互之间是`bestAcquaintance`的对子个数。可将脏位初值设为`true`，进行更新后设置为`false`。易知只有`add_Relation`和`modify_Relation`时需要重新维护，因此在对应操作后设置脏位为`true`，以便下次询问时进行更新。

## 三、Junit测试

### 数据构造

+ 数据的复杂度和全面度

  + `query_Triple_Sum`中，通过构造一个较为复杂的`network`，其中包括一条边同时存在于多个三角形中，同时使用大量的`add_Relation`和`modify_Relation`修改关键边，以判断方法实现的正确性

  + `query_Couple_Sum`中，通过不断的`add_Relation`，`modify_Relation`，修改不同`person`之间的`value`值和`link`关系

  + `delete_Cold_Emoji`中，不仅构造`emojiMessage`，同时也构造`redEnvelopeMessage`和`NoticeMessage`，以实现数据的全面度。

+ 分支的覆盖

  测试时需要针对方法中涉及的各种情况进行全面的测试，保证每一个分支都被覆盖到。

  主要体现在`queryCoupleSum`和`deleteColdEmoji`中

  + `queryCoupleSum`：

    + 修改value值的方法：`addRelation`和`modifyRelation`
    + 修改后的结果：等于/小于/大于`bestValue`
    + 修改的id：等于/小于/大于`bestId`

    以上三个方面，2\*3\*3 = 18种情况，均需在编写测试方法时覆盖

  + `deleteColdEmoji`：

    + `heat`值：等于/大于/小于`limit`值，以及为0.

### 断言构造

根据所要检测的方法的JML规格：

+ pure方法：**需要检测前后状态是否不变**

  以第一次作业和第二次作业的query类方法为例：`query_Couple_Sum`和`query_Triple_Sum`

  这两个方法均为pure类方法，于是我们可以写：

  ```java
  Person[] oldPersons = myNetwork1.getPersons();
          assertEquals(0, myNetwork1.queryTripleSum());
          Person[] newPersons = myNetwork1.getPersons();
          assertEquals(oldPersons.length, newPersons.length);
          for (int i = 0; i < oldPersons.length; i++) {
              assertTrue(((MyPerson) oldPersons[i]).strictEquals(newPersons[i]));
          }
  ```
  以测试是否方法前后的状态相同。
  
+ 针对方法的功能，测试其返回值，主要适用于query类方法的检测。

  ``assertEquals(...)`

+ 针对规格中每条`ensures`设计检测方法：以第三次作业的`delete_Cold_Emoji`为例，

  //注意插图

  在这个方法中，共有8条后置条件，这8条后置条件是不相互重叠的，每个后置条件都代表不同的意思。

  并且我们必须明确，在这次junit测试中，我们不能只关注答案的正确与否，例如仅仅构造数据然后判断方法的返回值，而必须在理解规格的基础上，根据规格所表示的意思进行判断。

  例如，第一个后置条件表示原来`heat`值大于等于`limit`的`emoji`的`emojiId`需要保留，那么我们的断言设计就应该按照下方所写。

  ```java
  boolean flag1 = false;
  boolean flag2 = false;
  for (int i = 0; i < newEmojiIdList.length; i++) {
      if (newEmojiIdList[i] == 1001) {
          flag1 = true;
      } else if (newEmojiIdList[i] == 1003) {
          flag2 = true;
      }
  }
  assertTrue(flag1 && flag2);
  ```
  再如，第二个后置条件保证新的`emojiIdList`中不得出现原来没有的`emojiId`，那么此时的断言设计应该为以下所写
  
  ```java
  for (int i = 0; i < newEmojiIdList.length; i++) {
      boolean flag = false;
      for (int j = 0; j < oldEmojiIdList.length; j++) {
          if (oldEmojiIdList[j] == newEmojiIdList[i] && oldEmojiHeatList[j] == newEmojiHeatList[i]) {
              flag = true;
              break;
          }
      }
      assertTrue(flag);
  }
  ```
  
  以及错误率很高的case9，想必是遗漏或者错写了对第5、6个后置条件的检查：
  
  ```java
  for (int i = 0; i < oldMessages.length; i++) {
              Message m = null;
              if (oldMessages[i] instanceof EmojiMessage) {
                  if (containsEmoId(((EmojiMessage) oldMessages[i]).getEmojiId(), newEmojiIdList)) {
                      m = oldMessages[i];
                  }
              } else {
                  m = oldMessages[i];
              }
              if (m == null) {
                  continue;
              }
              boolean mark = false;
              for (int j = 0; j < newMessages.length; j++) {
                  if (newMessages[j].getId() == m.getId()) {
                      mark = strictEqualMessage(m, newMessages[j]);
                      break;
                  }
              }
              assertTrue(mark);
          }
  ```

## 四、Bug分析

此部分记录我在写代码时出现的一些乱七八糟的错误，希望能对读者有些许参考价值。

+ 求平均和方差时忽略了除0的情况
+ 当`bestValue`值相等时忽略了`bestId`要求最小的条件。
+ 当删除人之后，数组为空，忽略了对`bestId`和`bestValue`的初始化。

## 五、本单元学习体会

+ 我其实挺喜欢JML的，因为不需要我动脑子，我只用对着写就行了🤤
+ 对代码性能的优化push我回顾了一下算法，感觉很好。
+ 谢谢课程组延迟公测，让我和wtls过了一个非常快乐的五一假期（比心比心，送花送花）