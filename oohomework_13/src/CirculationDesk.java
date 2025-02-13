import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static com.oocourse.library1.LibrarySystem.PRINTER;

public class CirculationDesk {
    private Shelf shelf;
    private HashMap<LibraryBookId, Integer> leftBooks;

    public CirculationDesk(Shelf shelf) {
        this.shelf = shelf;
        leftBooks = new HashMap<>();
    }

    public void borrowOneBook(LocalDate date, Student student,
                              LibraryBookId bookId, LibraryRequest request) {
        boolean fail = false;
        if (shelf.getBookNumber(bookId) == 0) {
            fail = true;
        } else if (bookId.isTypeA()) {
            fail = true;
        }
        if (bookId.isTypeB()) {
            if (student.haveB()) {
                fail = true;
            }
        }
        if (bookId.isTypeC()) {
            if (student.haveSameC(bookId.getUid())) {
                fail = true;
            }
        }
        if (fail) {
            PRINTER.reject(date, request);
            if (!bookId.isTypeA() && shelf.haveLeft(bookId)) {
                shelf.outOneBook(bookId);
                if (leftBooks.containsKey(bookId)) {
                    int old = leftBooks.get(bookId);
                    leftBooks.put(bookId, old + 1);
                } else {
                    leftBooks.put(bookId, 1);
                }
            }
        } else {
            shelf.outOneBook(bookId);
            student.addOneBook(bookId);
            PRINTER.accept(date, request);
        }
    }

    public void returnOneBook(LocalDate date, Student student,
                              LibraryBookId bookId, LibraryRequest request) {
        if (leftBooks.containsKey(bookId)) {
            int old = leftBooks.get(bookId);
            leftBooks.put(bookId, old + 1);
        } else {
            leftBooks.put(bookId, 1);
        }
        student.subOneBook(bookId);
        PRINTER.accept(date, request);
    }

    public void moveOut(List<LibraryMoveInfo> moveList) {
        for (LibraryBookId bookId : leftBooks.keySet()) {
            int number = leftBooks.get(bookId);
            for (int i = 0; i < number; i++) {
                shelf.inOneBook(bookId); //注意每条都要对应一个move
                moveList.add(new LibraryMoveInfo(bookId, "bro", "bs"));
            }
        }
        leftBooks.clear();
    }
}
