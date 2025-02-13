import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

import static com.oocourse.library1.LibrarySystem.PRINTER;

public class QueryMachine {
    private Shelf shelf;

    public QueryMachine(Shelf shelf) {
        this.shelf = shelf;
    }

    public void queryLeftBook(LocalDate date, LibraryBookId bookId) {
        int res = shelf.getBookNumber(bookId);
        PRINTER.info(date, bookId, res);
    }
}
