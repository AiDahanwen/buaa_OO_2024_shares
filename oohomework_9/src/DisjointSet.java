import com.oocourse.spec1.main.Person;

import java.util.HashMap;
import java.util.HashSet;

public class DisjointSet {
    private HashMap<Integer, Integer> parents;
    private HashMap<Integer, Integer> ranks;
    private int blockSum;

    public DisjointSet() {
        parents = new HashMap<>();
        ranks = new HashMap<>();
        blockSum = 0;
    }

    public void add(MyPerson person) {
        ranks.put(person.getId(), 0);
        parents.put(person.getId(), person.getId());
        blockSum++;
    }

    public int find(int id) {
        int tempId = id;
        while (parents.get(tempId) != tempId) {
            tempId = parents.get(tempId);
        }  //找到了根节点的id
        int begin = id; //起始id
        while (begin != tempId) {
            int parent = parents.get(begin);
            parents.put(begin, tempId);
            begin = parent;
        }
        return tempId; //因为删除换了新方法，所以可以实现路径压缩，按秩合并应该也可以
    }

    public void merge(int id1, int id2) { //注意这里的merge要改
        int root1 = find(id1);
        int root2 = find(id2);
        if (root1 != root2) {
            parents.put(root2, root1);
            blockSum--;
        }
    } //先这样写，到时候有bug了再改

    public void delete(MyPerson person1, MyPerson person2) {
        HashSet<Integer> allLinked = new HashSet<>();
        dFs(allLinked, person1);
        boolean change = true;
        int id1 = person1.getId();
        int id2 = person2.getId();
        for (Integer i : allLinked) {
            if (i == id2) {
                change = false;
            } //讨论这种情况的目的在于：按照本算法的设计，如果1和2能通过3再次link，那么2附带的人都会加到1里，
            //如果不能的话，则需要重置2所在的block
            parents.put(i, id1);
        }
        if (change) {
            allLinked.clear();
            dFs(allLinked, person2);
            for (Integer i : allLinked) {
                parents.put(i, id2);
            }
            blockSum++;
        }
    }

    public int getBlockSum() {
        return blockSum;
    }

    public void dFs(HashSet<Integer> allLinked, MyPerson person) {
        allLinked.add(person.getId());
        for (Person person1 : person.getAcquaintance().values()) {
            if (!allLinked.contains(person1.getId())) {
                dFs(allLinked, (MyPerson) person1);
            }
        }
    }
}
