import com.oocourse.library2.LibraryBookId;

import java.time.LocalDate;

import static com.oocourse.library2.LibrarySystem.PRINTER;

public class QueryMachine {
    private Shelf shelf;
    private DriftCorner driftCorner;

    public QueryMachine(Shelf shelf, DriftCorner driftCorner) {
        this.shelf = shelf;
        this.driftCorner = driftCorner;
    }

    public void queryLeftBook(LocalDate date, LibraryBookId bookId) {
        int res;
        if (!bookId.isFormal()) {
            res = driftCorner.getBookNumber(bookId);
        } else {
            res = shelf.getBookNumber(bookId);
        }
        PRINTER.info(date, bookId, res);
    }
}
