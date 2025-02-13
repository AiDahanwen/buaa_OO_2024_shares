import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.annotation.Trigger;

import static com.oocourse.library2.LibrarySystem.PRINTER;

import java.util.HashMap;

public class DriftCorner {
    private HashMap<LibraryBookId, Book> leftBooks;  //保证同号的书最多被捐献一次

    public DriftCorner() {
        leftBooks = new HashMap<>();
    }

    @Trigger(from = "InitState", to = "BDC")
    public void donateOneBook(LibraryBookId bookId, LibraryReqCmd reqCmd) {
        leftBooks.put(bookId, new Book(bookId));
        PRINTER.accept(reqCmd);
    }

    public int getBookNumber(LibraryBookId bookId) {
        return leftBooks.get(bookId).getNumber();
    }

    public boolean haveLeft(LibraryBookId bookId) {
        return leftBooks.get(bookId).getNumber() != 0;
    }

    public Book outOneBook(LibraryBookId bookId) {
        leftBooks.get(bookId).subNum();
        return leftBooks.get(bookId);
    }

    public void removeOneBook(LibraryBookId bookId) {
        leftBooks.remove(bookId);
    }

    @Trigger(from = "BRO", to = "BDC")
    public void returnBook(LibraryBookId bookId) {
        leftBooks.get(bookId).addNum();
    }
}
