import com.oocourse.library3.LibraryBookId;

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

    public void newOneBook(LibraryBookId bookId) {
        LibraryBookId newBookId = bookId.toFormal();
        if (leftBooks.containsKey(newBookId)) {
            inOneBook(newBookId);
        } else {
            leftBooks.put(newBookId, 1);
        }
    }
}
