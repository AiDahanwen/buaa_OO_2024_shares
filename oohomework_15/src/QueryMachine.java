import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.util.HashMap;

import static com.oocourse.library3.LibrarySystem.PRINTER;

public class QueryMachine {
    private Shelf shelf;
    private DriftCorner driftCorner;
    private HashMap<String, Student> students;

    public QueryMachine(Shelf shelf, DriftCorner driftCorner, HashMap<String, Student> students) {
        this.shelf = shelf;
        this.driftCorner = driftCorner;
        this.students = students;
    }

    @SendMessage(from = "Library", to = "QueryMachine")
    public void queryLeftBook(LocalDate date, LibraryBookId bookId) {
        int res;
        if (!bookId.isFormal()) {
            res = driftCorner.getBookNumber(bookId);
        } else {
            res = shelf.getBookNumber(bookId);
        }
        PRINTER.info(date, bookId, res);
    }

    @SendMessage(from = "Library", to = "QueryMachine")
    public void queryCredit(String studentId, LibraryCommand cmd) {
        int res = students.get(studentId).getCredit();
        PRINTER.info(cmd, res);
    }
}
