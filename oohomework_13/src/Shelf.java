import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class Shelf {
    private HashMap<LibraryBookId, Integer> leftBooks;

    public Shelf(HashMap<LibraryBookId, Integer> books) {
        leftBooks = books;
    } //可以直接修改

    public int getBookNumber(LibraryBookId bookId) {
        return leftBooks.get(bookId);
    }

    public boolean haveLeft(LibraryBookId bookId) {
        return leftBooks.get(bookId) != 0;
    }

    public void outOneBook(LibraryBookId bookId) {
        int old = leftBooks.get(bookId);
        leftBooks.put(bookId, old - 1);
    }

    public void inOneBook(LibraryBookId bookId) {
        int old = leftBooks.get(bookId);
        leftBooks.put(bookId, old + 1);
    }
}
