import java.util.ArrayList;

public class Node {
    private ArrayList<Node> neighbors;
    private int id;
    private boolean isSearched;
    private Node predecessor;

    public Node(int id) {
        this.id = id;
        isSearched = false;
        neighbors = new ArrayList<>();
        predecessor = null;
    }

    public void setPredecessor(Node node) {
        predecessor = node;
    }

    public void addNeighbors(Node node) {
        neighbors.add(node);
    }

    public int getId() {
        return id;
    }

    public Node getPredecessor() {
        return predecessor;
    }

    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

    public void removeNeighbor(Node node) {
        neighbors.remove(node);
    }

    public void setSearched(boolean flag) {
        isSearched = flag;
    }

    public boolean isSearched() {
        return isSearched;
    }
}
