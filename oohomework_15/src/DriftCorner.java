import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.util.HashMap;

import static com.oocourse.library3.LibrarySystem.PRINTER;

public class DriftCorner {
    private HashMap<LibraryBookId, Book> leftBooks;  //保证同号的书最多被捐献一次

    public DriftCorner() {
        leftBooks = new HashMap<>();
    }

    @SendMessage(from = "Library", to = "DriftCorner")
    @Trigger(from = "InitState", to = "BDC")
    public void donateOneBook(LibraryBookId bookId, LibraryReqCmd reqCmd, String studentId) {
        leftBooks.put(bookId, new Book(bookId, studentId));
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

    public Book getOneBook(LibraryBookId bookId) {
        return leftBooks.get(bookId);
    }
}
